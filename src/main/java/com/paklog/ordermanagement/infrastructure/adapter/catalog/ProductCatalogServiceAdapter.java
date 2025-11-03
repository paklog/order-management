package com.paklog.ordermanagement.infrastructure.adapter.catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.paklog.ordermanagement.domain.port.ProductCatalogServicePort;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

/**
 * Adapter implementation for Product Catalog Service integration.
 * Implements the ProductCatalogServicePort using REST API calls.
 *
 * This adapter is only enabled when product catalog checks are configured.
 */
@Component
@ConditionalOnProperty(name = "order-management.validation.check-product-catalog", havingValue = "true")
public class ProductCatalogServiceAdapter implements ProductCatalogServicePort {

    private static final Logger logger = LoggerFactory.getLogger(ProductCatalogServiceAdapter.class);

    private final RestTemplate restTemplate;
    private final String productCatalogServiceUrl;

    public ProductCatalogServiceAdapter(RestTemplate restTemplate,
                                       @Value("${order-management.integration.product-catalog-service.url}") String productCatalogServiceUrl) {
        this.restTemplate = restTemplate;
        this.productCatalogServiceUrl = productCatalogServiceUrl;
        logger.info("ProductCatalogServiceAdapter initialized - URL: {}", productCatalogServiceUrl);
    }

    @Override
    public ProductValidationResult validateProducts(List<String> skus) {
        logger.debug("Validating {} SKUs against product catalog", skus.size());

        List<String> invalidSkus = new ArrayList<>();

        for (String sku : skus) {
            if (!productExists(sku)) {
                invalidSkus.add(sku);
            }
        }

        if (invalidSkus.isEmpty()) {
            logger.info("All {} SKUs are valid", skus.size());
            return ProductValidationResult.valid();
        } else {
            String message = String.format("%d of %d SKUs not found in catalog",
                invalidSkus.size(), skus.size());
            logger.warn(message + " - Invalid SKUs: {}", invalidSkus);
            return ProductValidationResult.invalid(invalidSkus, message);
        }
    }

    @Override
    @CircuitBreaker(name = "productCatalog", fallbackMethod = "productExistsFallback")
    @Retry(name = "productCatalog")
    public boolean productExists(String sku) {
        try {
            String url = productCatalogServiceUrl + "/products/" + sku;
            logger.debug("Checking if product exists - SKU: {}, URL: {}", sku, url);

            ProductResponse response = restTemplate.getForObject(url, ProductResponse.class);

            boolean exists = response != null && response.getSku() != null;
            logger.debug("Product exists check - SKU: {}, Exists: {}", sku, exists);

            return exists;

        } catch (HttpClientErrorException.NotFound e) {
            logger.debug("Product not found in catalog - SKU: {}", sku);
            return false;

        } catch (Exception e) {
            logger.error("Error checking product existence - SKU: {}, Error: {}", sku, e.getMessage(), e);
            throw e; // Throw to trigger circuit breaker
        }
    }

    /**
     * Fallback method for productExists when circuit is open or call fails.
     * Fails safe by returning false (product doesn't exist).
     */
    private boolean productExistsFallback(String sku, Exception e) {
        logger.warn("Product Catalog circuit breaker activated for productExists - SKU: {}, Error: {}. Returning false (fail-safe).",
            sku, e.getMessage());
        return false;
    }

    @Override
    @CircuitBreaker(name = "productCatalog", fallbackMethod = "getProductDetailsFallback")
    @Retry(name = "productCatalog")
    public Optional<ProductDetails> getProductDetails(String sku) {
        try {
            String url = productCatalogServiceUrl + "/products/" + sku;
            logger.debug("Fetching product details - SKU: {}", sku);

            ProductResponse response = restTemplate.getForObject(url, ProductResponse.class);

            if (response != null) {
                ProductDetails details = new ProductDetails(
                    response.getSku(),
                    response.getTitle(),
                    response.getPrice(),
                    response.getActive() != null ? response.getActive() : false,
                    response.getCategory()
                );

                logger.debug("Retrieved product details - SKU: {}, Title: {}, Price: {}",
                    sku, details.getName(), details.getPrice());

                return Optional.of(details);
            }

            return Optional.empty();

        } catch (HttpClientErrorException.NotFound e) {
            logger.debug("Product not found - SKU: {}", sku);
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Error fetching product details - SKU: {}, Error: {}", sku, e.getMessage(), e);
            throw e; // Throw to trigger circuit breaker
        }
    }

    /**
     * Fallback method for getProductDetails when circuit is open or call fails.
     */
    private Optional<ProductDetails> getProductDetailsFallback(String sku, Exception e) {
        logger.warn("Product Catalog circuit breaker activated for getProductDetails - SKU: {}, Error: {}. Returning empty.",
            sku, e.getMessage());
        return Optional.empty();
    }

    @Override
    public Map<String, ProductDetails> getProductDetails(List<String> skus) {
        logger.debug("Fetching details for {} SKUs", skus.size());

        Map<String, ProductDetails> results = new HashMap<>();

        for (String sku : skus) {
            getProductDetails(sku).ifPresent(details -> results.put(sku, details));
        }

        logger.info("Retrieved details for {} of {} SKUs", results.size(), skus.size());

        return results;
    }
}
