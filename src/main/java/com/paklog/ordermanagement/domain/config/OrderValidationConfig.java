package com.paklog.ordermanagement.domain.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for order validation rules.
 * Allows externalization of business validation thresholds.
 */
@Configuration
@ConfigurationProperties(prefix = "order-management.validation")
public class OrderValidationConfig {

    /**
     * Maximum total quantity allowed per order
     */
    private int maxTotalQuantity = 100000;

    /**
     * Maximum number of items allowed per order
     */
    private int maxItemsPerOrder = 100;

    /**
     * Minimum order value (in USD)
     */
    private BigDecimal minOrderValue = BigDecimal.valueOf(0.01);

    /**
     * Maximum order value (in USD)
     */
    private BigDecimal maxOrderValue = BigDecimal.valueOf(1000000.00);

    /**
     * Enable product catalog validation
     */
    private boolean checkProductCatalog = false;

    /**
     * Enable strict duplicate SKU rejection
     */
    private boolean rejectDuplicateSkus = true;

    /**
     * Enable order value validation
     */
    private boolean enableOrderValueValidation = false;

    /**
     * Time window (in hours) for duplicate order detection.
     * Orders within this window with similar characteristics are flagged as duplicates.
     */
    private int duplicateDetectionWindowHours = 24;

    // Getters and Setters

    public int getMaxTotalQuantity() {
        return maxTotalQuantity;
    }

    public void setMaxTotalQuantity(int maxTotalQuantity) {
        this.maxTotalQuantity = maxTotalQuantity;
    }

    public int getMaxItemsPerOrder() {
        return maxItemsPerOrder;
    }

    public void setMaxItemsPerOrder(int maxItemsPerOrder) {
        this.maxItemsPerOrder = maxItemsPerOrder;
    }

    public BigDecimal getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(BigDecimal minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public BigDecimal getMaxOrderValue() {
        return maxOrderValue;
    }

    public void setMaxOrderValue(BigDecimal maxOrderValue) {
        this.maxOrderValue = maxOrderValue;
    }

    public boolean isCheckProductCatalog() {
        return checkProductCatalog;
    }

    public void setCheckProductCatalog(boolean checkProductCatalog) {
        this.checkProductCatalog = checkProductCatalog;
    }

    public boolean isRejectDuplicateSkus() {
        return rejectDuplicateSkus;
    }

    public void setRejectDuplicateSkus(boolean rejectDuplicateSkus) {
        this.rejectDuplicateSkus = rejectDuplicateSkus;
    }

    public boolean isEnableOrderValueValidation() {
        return enableOrderValueValidation;
    }

    public void setEnableOrderValueValidation(boolean enableOrderValueValidation) {
        this.enableOrderValueValidation = enableOrderValueValidation;
    }

    public int getDuplicateDetectionWindowHours() {
        return duplicateDetectionWindowHours;
    }

    public void setDuplicateDetectionWindowHours(int duplicateDetectionWindowHours) {
        this.duplicateDetectionWindowHours = duplicateDetectionWindowHours;
    }

    // Removed: checkInventoryAvailability - inventory check is now always performed,
    // but order acceptance is controlled by fulfillment policy
}

