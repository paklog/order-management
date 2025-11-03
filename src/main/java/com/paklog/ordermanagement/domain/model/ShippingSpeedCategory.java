package com.paklog.ordermanagement.domain.model;

/**
 * Enumeration of shipping speed categories for fulfillment orders.
 * Defines the service level agreement for order delivery.
 */
public enum ShippingSpeedCategory {

    /**
     * Standard shipping (5-7 business days)
     */
    STANDARD,

    /**
     * Expedited shipping (2-3 business days)
     */
    EXPEDITED,

    /**
     * Priority shipping (1-2 business days)
     */
    PRIORITY,

    /**
     * Same-day delivery
     */
    SAME_DAY,

    /**
     * Next-day delivery
     */
    NEXT_DAY,

    /**
     * Scheduled delivery with specific date
     */
    SCHEDULED;

    /**
     * Convert string to enum, case-insensitive
     */
    public static ShippingSpeedCategory fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return ShippingSpeedCategory.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid shipping speed category: " + value +
                ". Valid values are: STANDARD, EXPEDITED, PRIORITY, SAME_DAY, NEXT_DAY, SCHEDULED"
            );
        }
    }
}
