package com.paklog.ordermanagement.domain.model;

/**
 * Reasons why an item cannot be fulfilled.
 */
public enum UnfulfillableReason {

    /**
     * Insufficient stock available to fulfill the requested quantity.
     */
    INSUFFICIENT_STOCK,

    /**
     * The SKU was not found in the inventory system.
     */
    SKU_NOT_FOUND,

    /**
     * The inventory service is unavailable or returned an error.
     */
    INVENTORY_SERVICE_ERROR,

    /**
     * The item has been discontinued and is no longer available.
     */
    DISCONTINUED,

    /**
     * The item is backordered and not currently available.
     */
    BACKORDERED
}
