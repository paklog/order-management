package com.paklog.ordermanagement.domain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paklog.ordermanagement.domain.config.OrderValidationConfig;
import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.domain.model.OrderItem;
import com.paklog.ordermanagement.domain.model.ShippingSpeedCategory;
import com.paklog.ordermanagement.domain.model.UnfulfillableItem;
import com.paklog.ordermanagement.domain.model.UnfulfillableReason;
import com.paklog.ordermanagement.domain.port.InventoryServicePort;
import com.paklog.ordermanagement.domain.port.ProductCatalogServicePort;

/**
 * Domain service for validating business rules on fulfillment orders.
 * This service encapsulates complex validation logic that spans multiple aggregates
 * or requires domain knowledge and external service integrations.
 */
@Service
public class OrderValidationService {

    private static final Logger logger = LoggerFactory.getLogger(OrderValidationService.class);

    private final OrderValidationConfig config;
    private final InventoryServicePort inventoryService;
    private final ProductCatalogServicePort productCatalogService;

    public OrderValidationService(OrderValidationConfig config,
                                 @Autowired(required = false) InventoryServicePort inventoryService,
                                 @Autowired(required = false) ProductCatalogServicePort productCatalogService) {
        this.config = config;
        this.inventoryService = inventoryService;
        this.productCatalogService = productCatalogService;
        logger.info("OrderValidationService initialized - InventoryServiceAvailable: {}, ProductCatalogCheck: {}",
            inventoryService != null, config.isCheckProductCatalog());
    }

    /**
     * Validates a fulfillment order against all business rules.
     *
     * @param order the order to validate
     * @return ValidationResult containing any validation errors
     */
    public ValidationResult validate(FulfillmentOrder order) {
        logger.debug("Validating fulfillment order - OrderId: {}", order.getOrderId());

        List<String> errors = new ArrayList<>();

        // Validate items (SKU duplicates, quantities)
        validateItems(order.getItems(), errors);

        // Validate shipping speed category
        validateShippingSpeedCategory(order.getShippingSpeedCategory(), errors);

        // Validate order value (if enabled)
        if (config.isEnableOrderValueValidation()) {
            validateOrderValue(order, errors);
        }

        // Validate product catalog (if enabled)
        if (config.isCheckProductCatalog() && productCatalogService != null) {
            validateProductCatalog(order, errors);
        }

        // Note: Inventory availability is no longer validated here.
        // It's checked separately via checkInventoryAvailability() method
        // and handled based on fulfillment policy.

        ValidationResult result = errors.isEmpty()
            ? ValidationResult.success()
            : ValidationResult.failure(errors);

        if (result.isValid()) {
            logger.debug("Order validation successful - OrderId: {}", order.getOrderId());
        } else {
            logger.warn("Order validation failed - OrderId: {}, Errors: {}",
                order.getOrderId(), errors);
        }

        return result;
    }

    /**
     * Validates order items for business rule compliance.
     */
    private void validateItems(List<OrderItem> items, List<String> errors) {
        if (items == null || items.isEmpty()) {
            errors.add("Order must contain at least one item");
            return;
        }

        // Check for duplicate SKUs
        Set<String> seenSkus = new HashSet<>();
        Set<String> duplicateSkus = new HashSet<>();

        for (OrderItem item : items) {
            if (item.getSellerSku() != null) {
                if (!seenSkus.add(item.getSellerSku())) {
                    duplicateSkus.add(item.getSellerSku());
                }
            }
        }

        if (!duplicateSkus.isEmpty()) {
            errors.add("Duplicate SKUs found in order: " + String.join(", ", duplicateSkus) +
                ". Please consolidate quantities for duplicate items.");
        }

        // Validate total quantity
        int totalQuantity = items.stream()
            .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
            .sum();

        if (totalQuantity > config.getMaxTotalQuantity()) {
            errors.add("Total order quantity (" + totalQuantity +
                ") exceeds maximum allowed (" + config.getMaxTotalQuantity() + ")");
        }

        if (totalQuantity == 0) {
            errors.add("Total order quantity must be greater than 0");
        }
    }

    /**
     * Validates shipping speed category is valid.
     */
    private void validateShippingSpeedCategory(String shippingSpeedCategory, List<String> errors) {
        if (shippingSpeedCategory == null || shippingSpeedCategory.isBlank()) {
            errors.add("Shipping speed category is required");
            return;
        }

        try {
            ShippingSpeedCategory.fromString(shippingSpeedCategory);
        } catch (IllegalArgumentException e) {
            errors.add(e.getMessage());
        }
    }

    /**
     * Validates total order value is within acceptable limits.
     * Requires product catalog integration to fetch prices.
     */
    private void validateOrderValue(FulfillmentOrder order, List<String> errors) {
        if (productCatalogService == null) {
            logger.warn("Cannot validate order value - Product Catalog service not available");
            return;
        }

        try {
            List<String> skus = order.getItems().stream()
                .map(OrderItem::getSellerSku)
                .collect(Collectors.toList());

            Map<String, ProductCatalogServicePort.ProductDetails> productDetails =
                productCatalogService.getProductDetails(skus);

            BigDecimal totalValue = BigDecimal.ZERO;

            for (OrderItem item : order.getItems()) {
                ProductCatalogServicePort.ProductDetails details = productDetails.get(item.getSellerSku());
                if (details != null && details.getPrice() != null) {
                    BigDecimal itemTotal = details.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                    totalValue = totalValue.add(itemTotal);
                }
            }

            if (totalValue.compareTo(config.getMinOrderValue()) < 0) {
                errors.add(String.format("Order value ($%.2f) is below minimum ($%.2f)",
                    totalValue, config.getMinOrderValue()));
            }

            if (totalValue.compareTo(config.getMaxOrderValue()) > 0) {
                errors.add(String.format("Order value ($%.2f) exceeds maximum ($%.2f)",
                    totalValue, config.getMaxOrderValue()));
            }

            logger.debug("Order value validation - OrderId: {}, TotalValue: ${}",
                order.getOrderId(), totalValue);

        } catch (Exception e) {
            logger.error("Error validating order value - OrderId: {}, Error: {}",
                order.getOrderId(), e.getMessage(), e);
            errors.add("Unable to validate order value: " + e.getMessage());
        }
    }

    /**
     * Validates all SKUs exist in the product catalog.
     */
    private void validateProductCatalog(FulfillmentOrder order, List<String> errors) {
        try {
            List<String> skus = order.getItems().stream()
                .map(OrderItem::getSellerSku)
                .collect(Collectors.toList());

            ProductCatalogServicePort.ProductValidationResult result =
                productCatalogService.validateProducts(skus);

            if (!result.isAllValid()) {
                errors.add("Invalid SKUs found: " + String.join(", ", result.getInvalidSkus()) +
                    ". These products do not exist in the catalog.");
                logger.warn("Product catalog validation failed - OrderId: {}, InvalidSKUs: {}",
                    order.getOrderId(), result.getInvalidSkus());
            } else {
                logger.debug("Product catalog validation successful - OrderId: {}", order.getOrderId());
            }

        } catch (Exception e) {
            logger.error("Error validating product catalog - OrderId: {}, Error: {}",
                order.getOrderId(), e.getMessage(), e);
            errors.add("Unable to validate product catalog: " + e.getMessage());
        }
    }

    /**
     * Checks inventory availability for all items in the order.
     * Returns a list of unfulfillable items that can be used for policy decisions.
     *
     * @param order the order to check
     * @return InventoryAvailabilityResult containing unfulfillable items
     */
    public InventoryAvailabilityResult checkInventoryAvailability(FulfillmentOrder order) {
        if (inventoryService == null) {
            logger.warn("Inventory service not available - OrderId: {}. Treating all items as available.",
                order.getOrderId());
            return InventoryAvailabilityResult.allAvailable();
        }

        try {
            Map<String, Integer> itemsToCheck = new HashMap<>();
            Map<String, OrderItem> itemsBySku = new HashMap<>();

            for (OrderItem item : order.getItems()) {
                itemsToCheck.put(item.getSellerSku(), item.getQuantity());
                itemsBySku.put(item.getSellerSku(), item);
            }

            InventoryServicePort.InventoryCheckResult result =
                inventoryService.checkAvailability(itemsToCheck);

            if (result.isAllAvailable()) {
                logger.info("All items available in inventory - OrderId: {}", order.getOrderId());
                return InventoryAvailabilityResult.allAvailable();
            }

            // Convert to domain unfulfillable items
            List<UnfulfillableItem> unfulfillableItems = new ArrayList<>();
            for (InventoryServicePort.UnavailableItem unavailable : result.getUnavailableItems()) {
                OrderItem originalItem = itemsBySku.get(unavailable.getSku());
                UnfulfillableReason reason = unavailable.getAvailable() == 0
                    ? UnfulfillableReason.SKU_NOT_FOUND
                    : UnfulfillableReason.INSUFFICIENT_STOCK;

                UnfulfillableItem unfulfillable = new UnfulfillableItem(
                    unavailable.getSku(),
                    originalItem != null ? originalItem.getSellerFulfillmentOrderItemId() : "unknown",
                    unavailable.getRequested(),
                    unavailable.getAvailable(),
                    reason
                );
                unfulfillableItems.add(unfulfillable);
            }

            logger.info("Inventory check completed - OrderId: {}, UnavailableItems: {}",
                order.getOrderId(), unfulfillableItems.size());

            return InventoryAvailabilityResult.partiallyAvailable(unfulfillableItems);

        } catch (Exception e) {
            logger.error("Error checking inventory availability - OrderId: {}, Error: {}",
                order.getOrderId(), e.getMessage(), e);
            // In case of error, treat as all unavailable for safety
            List<UnfulfillableItem> allUnavailable = new ArrayList<>();
            for (OrderItem item : order.getItems()) {
                allUnavailable.add(new UnfulfillableItem(
                    item.getSellerSku(),
                    item.getSellerFulfillmentOrderItemId(),
                    item.getQuantity(),
                    0,
                    UnfulfillableReason.INVENTORY_SERVICE_ERROR
                ));
            }
            return InventoryAvailabilityResult.serviceError(allUnavailable, e.getMessage());
        }
    }

    /**
     * Result of inventory availability check.
     */
    public static class InventoryAvailabilityResult {
        private final boolean allAvailable;
        private final List<UnfulfillableItem> unfulfillableItems;
        private final String errorMessage;

        private InventoryAvailabilityResult(boolean allAvailable, List<UnfulfillableItem> unfulfillableItems, String errorMessage) {
            this.allAvailable = allAvailable;
            this.unfulfillableItems = unfulfillableItems != null ? new ArrayList<>(unfulfillableItems) : new ArrayList<>();
            this.errorMessage = errorMessage;
        }

        public static InventoryAvailabilityResult allAvailable() {
            return new InventoryAvailabilityResult(true, new ArrayList<>(), null);
        }

        public static InventoryAvailabilityResult partiallyAvailable(List<UnfulfillableItem> unfulfillableItems) {
            return new InventoryAvailabilityResult(false, unfulfillableItems, null);
        }

        public static InventoryAvailabilityResult serviceError(List<UnfulfillableItem> unfulfillableItems, String errorMessage) {
            return new InventoryAvailabilityResult(false, unfulfillableItems, errorMessage);
        }

        public boolean isAllAvailable() {
            return allAvailable;
        }

        public List<UnfulfillableItem> getUnfulfillableItems() {
            return new ArrayList<>(unfulfillableItems);
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public boolean hasServiceError() {
            return errorMessage != null;
        }
    }

    /**
     * Result of validation containing success status and error messages.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        private ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        }

        public static ValidationResult success() {
            return new ValidationResult(true, new ArrayList<>());
        }

        public static ValidationResult failure(List<String> errors) {
            return new ValidationResult(false, errors);
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}
