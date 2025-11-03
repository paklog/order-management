package com.paklog.ordermanagement.domain.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Port interface for Product Catalog Service integration.
 * Defines operations needed to validate products for order processing.
 *
 * This is a hexagonal architecture port - implementations will be in infrastructure layer.
 */
public interface ProductCatalogServicePort {

    /**
     * Validates that all SKUs in the order exist in the product catalog.
     *
     * @param skus list of SKUs to validate
     * @return ProductValidationResult containing validation details
     */
    ProductValidationResult validateProducts(List<String> skus);

    /**
     * Checks if a single SKU exists in the catalog.
     *
     * @param sku the product SKU
     * @return true if SKU exists
     */
    boolean productExists(String sku);

    /**
     * Gets product details including price for a SKU.
     *
     * @param sku the product SKU
     * @return Optional containing product details if found
     */
    Optional<ProductDetails> getProductDetails(String sku);

    /**
     * Gets product details for multiple SKUs.
     *
     * @param skus list of SKUs
     * @return map of SKU to ProductDetails
     */
    Map<String, ProductDetails> getProductDetails(List<String> skus);

    /**
     * Result of product validation.
     */
    class ProductValidationResult {
        private final boolean allValid;
        private final List<String> invalidSkus;
        private final String message;

        public ProductValidationResult(boolean allValid, List<String> invalidSkus, String message) {
            this.allValid = allValid;
            this.invalidSkus = invalidSkus;
            this.message = message;
        }

        public static ProductValidationResult valid() {
            return new ProductValidationResult(true, List.of(), "All products valid");
        }

        public static ProductValidationResult invalid(List<String> invalidSkus, String message) {
            return new ProductValidationResult(false, invalidSkus, message);
        }

        public boolean isAllValid() {
            return allValid;
        }

        public List<String> getInvalidSkus() {
            return invalidSkus;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Product details from catalog.
     */
    class ProductDetails {
        private final String sku;
        private final String name;
        private final BigDecimal price;
        private final boolean active;
        private final String category;

        public ProductDetails(String sku, String name, BigDecimal price, boolean active, String category) {
            this.sku = sku;
            this.name = name;
            this.price = price;
            this.active = active;
            this.category = category;
        }

        public String getSku() {
            return sku;
        }

        public String getName() {
            return name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public boolean isActive() {
            return active;
        }

        public String getCategory() {
            return category;
        }
    }
}
