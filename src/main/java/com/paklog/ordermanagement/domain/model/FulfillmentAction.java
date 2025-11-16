package com.paklog.ordermanagement.domain.model;

/**
 * Represents the fulfillment action status after inventory check.
 *
 * This enum indicates the result of applying the fulfillment policy
 * based on inventory availability.
 */
public enum FulfillmentAction {

    /**
     * All items in the order can be fulfilled completely.
     * Full inventory is available for all requested items.
     */
    COMPLETE,

    /**
     * Some items can be fulfilled, but not all.
     * Partial inventory is available - some items will be fulfilled,
     * others will be marked as unfulfillable.
     */
    PARTIAL,

    /**
     * No items can be fulfilled.
     * No inventory is available for any requested items.
     */
    UNFULFILLABLE
}
