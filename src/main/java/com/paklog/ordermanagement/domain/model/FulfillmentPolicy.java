package com.paklog.ordermanagement.domain.model;

/**
 * Defines the fulfillment policy for handling inventory availability.
 *
 * This enum controls how the system behaves when requested items
 * are not fully available in inventory.
 */
public enum FulfillmentPolicy {

    /**
     * Fill or Kill - Reject the entire order if any item is unavailable.
     * This is the strictest policy where all items must be available.
     */
    FILL_OR_KILL,

    /**
     * Fill All - Accept the order even if items are unavailable.
     * Publishes a stock unavailable event when items cannot be fulfilled.
     * The order is accepted but may not be processed until stock is available.
     */
    FILL_ALL,

    /**
     * Fill All Available - Accept the order and fulfill only available items.
     * This enables partial fulfillment where available items are processed
     * and unavailable items are tracked separately.
     * Publishes a partial fulfillment event when not all items can be fulfilled.
     */
    FILL_ALL_AVAILABLE
}
