package com.paklog.ordermanagement.infrastructure.adapter.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.paklog.ordermanagement.domain.port.InventoryServicePort;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

/**
 * Adapter implementation for Inventory Service integration.
 * Implements the InventoryServicePort using REST API calls.
 *
 * This adapter is always enabled to support partial fulfillment policy decisions.
 */
@Component
public class InventoryServiceAdapter implements InventoryServicePort {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceAdapter.class);

    private final RestTemplate restTemplate;
    private final String inventoryServiceUrl;

    public InventoryServiceAdapter(RestTemplate restTemplate,
                                   @Value("${order-management.integration.inventory-service.url}") String inventoryServiceUrl) {
        this.restTemplate = restTemplate;
        this.inventoryServiceUrl = inventoryServiceUrl;
        logger.info("InventoryServiceAdapter initialized - URL: {}", inventoryServiceUrl);
    }

    @Override
    public InventoryCheckResult checkAvailability(Map<String, Integer> items) {
        logger.debug("Checking inventory availability for {} SKUs", items.size());

        List<UnavailableItem> unavailableItems = new ArrayList<>();
        int checkedCount = 0;

        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            String sku = entry.getKey();
            Integer requestedQty = entry.getValue();

            try {
                String url = inventoryServiceUrl + "/stock_levels/" + sku;
                logger.debug("Fetching stock level - SKU: {}, URL: {}", sku, url);

                InventoryStockLevelResponse response = restTemplate.getForObject(
                    url,
                    InventoryStockLevelResponse.class
                );

                if (response != null) {
                    Integer available = response.getAvailableToPromise();

                    if (available == null || available < requestedQty) {
                        int actualAvailable = available != null ? available : 0;
                        unavailableItems.add(new UnavailableItem(sku, requestedQty, actualAvailable));

                        logger.warn("Insufficient inventory - SKU: {}, Requested: {}, Available: {}",
                            sku, requestedQty, actualAvailable);
                    } else {
                        logger.debug("Sufficient inventory - SKU: {}, Requested: {}, Available: {}",
                            sku, requestedQty, available);
                    }
                }

                checkedCount++;

            } catch (HttpClientErrorException.NotFound e) {
                // SKU not found in inventory - treat as unavailable
                unavailableItems.add(new UnavailableItem(sku, requestedQty, 0));
                logger.warn("SKU not found in inventory - SKU: {}", sku);
                checkedCount++;

            } catch (Exception e) {
                logger.error("Error checking inventory for SKU: {} - Error: {}", sku, e.getMessage(), e);
                // For circuit breaker/timeout scenarios, we might want to fail fast
                // For now, we'll treat as unavailable
                unavailableItems.add(new UnavailableItem(sku, requestedQty, 0));
                checkedCount++;
            }
        }

        if (unavailableItems.isEmpty()) {
            logger.info("All items available - Checked: {} SKUs", checkedCount);
            return InventoryCheckResult.available();
        } else {
            String message = String.format("Inventory check failed: %d of %d items unavailable",
                unavailableItems.size(), items.size());
            logger.warn(message);
            return InventoryCheckResult.unavailable(unavailableItems, message);
        }
    }

    @Override
    @CircuitBreaker(name = "inventory", fallbackMethod = "isAvailableFallback")
    @Retry(name = "inventory")
    public boolean isAvailable(String sku, int quantity) {
        try {
            String url = inventoryServiceUrl + "/stock_levels/" + sku;
            logger.debug("Checking single SKU availability - SKU: {}, Quantity: {}", sku, quantity);

            InventoryStockLevelResponse response = restTemplate.getForObject(
                url,
                InventoryStockLevelResponse.class
            );

            if (response != null && response.getAvailableToPromise() != null) {
                boolean available = response.getAvailableToPromise() >= quantity;
                logger.debug("SKU {} availability check: {} (available: {}, requested: {})",
                    sku, available, response.getAvailableToPromise(), quantity);
                return available;
            }

            return false;

        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("SKU not found in inventory - SKU: {}", sku);
            return false;

        } catch (Exception e) {
            logger.error("Error checking inventory for SKU: {} - Error: {}", sku, e.getMessage(), e);
            throw e; // Throw to trigger circuit breaker
        }
    }

    /**
     * Fallback method for isAvailable when circuit is open or call fails.
     * Fails safe by returning false (not available).
     */
    private boolean isAvailableFallback(String sku, int quantity, Exception e) {
        logger.warn("Inventory circuit breaker activated for isAvailable - SKU: {}, Quantity: {}, Error: {}. Returning false (fail-safe).",
            sku, quantity, e.getMessage());
        return false;
    }
}
