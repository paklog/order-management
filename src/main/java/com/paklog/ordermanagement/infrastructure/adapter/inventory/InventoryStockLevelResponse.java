package com.paklog.ordermanagement.infrastructure.adapter.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for Inventory Service stock level response.
 * Maps to Inventory Service GET /stock_levels/{sku} response.
 */
public class InventoryStockLevelResponse {

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("quantity_on_hand")
    private Integer quantityOnHand;

    @JsonProperty("quantity_allocated")
    private Integer quantityAllocated;

    @JsonProperty("available_to_promise")
    private Integer availableToPromise;

    public InventoryStockLevelResponse() {
    }

    public InventoryStockLevelResponse(String sku, Integer quantityOnHand, Integer quantityAllocated, Integer availableToPromise) {
        this.sku = sku;
        this.quantityOnHand = quantityOnHand;
        this.quantityAllocated = quantityAllocated;
        this.availableToPromise = availableToPromise;
    }

    // Getters and Setters

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(Integer quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public Integer getQuantityAllocated() {
        return quantityAllocated;
    }

    public void setQuantityAllocated(Integer quantityAllocated) {
        this.quantityAllocated = quantityAllocated;
    }

    public Integer getAvailableToPromise() {
        return availableToPromise;
    }

    public void setAvailableToPromise(Integer availableToPromise) {
        this.availableToPromise = availableToPromise;
    }
}
