package com.paklog.ordermanagement.domain.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * Represents an order item that cannot be fulfilled due to inventory constraints.
 *
 * This value object captures the details of why an item could not be fulfilled,
 * including the shortage information.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UnfulfillableItem {

    @JsonProperty("seller_sku")
    private String sellerSku;

    @JsonProperty("seller_fulfillment_order_item_id")
    private String sellerFulfillmentOrderItemId;

    @JsonProperty("requested_quantity")
    private int requestedQuantity;

    @JsonProperty("available_quantity")
    private int availableQuantity;

    @JsonProperty("unfulfillable_quantity")
    private int unfulfillableQuantity;

    @JsonProperty("reason")
    private UnfulfillableReason reason;

    public UnfulfillableItem() {
        // Default constructor for frameworks
    }

    public UnfulfillableItem(String sellerSku, String sellerFulfillmentOrderItemId,
                             int requestedQuantity, int availableQuantity,
                             UnfulfillableReason reason) {
        this.sellerSku = sellerSku;
        this.sellerFulfillmentOrderItemId = sellerFulfillmentOrderItemId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
        this.unfulfillableQuantity = requestedQuantity - availableQuantity;
        this.reason = reason;
    }

    // Getters and setters

    public String getSellerSku() {
        return sellerSku;
    }

    public void setSellerSku(String sellerSku) {
        this.sellerSku = sellerSku;
    }

    public String getSellerFulfillmentOrderItemId() {
        return sellerFulfillmentOrderItemId;
    }

    public void setSellerFulfillmentOrderItemId(String sellerFulfillmentOrderItemId) {
        this.sellerFulfillmentOrderItemId = sellerFulfillmentOrderItemId;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(int requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public int getUnfulfillableQuantity() {
        return unfulfillableQuantity;
    }

    public void setUnfulfillableQuantity(int unfulfillableQuantity) {
        this.unfulfillableQuantity = unfulfillableQuantity;
    }

    public UnfulfillableReason getReason() {
        return reason;
    }

    public void setReason(UnfulfillableReason reason) {
        this.reason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnfulfillableItem that = (UnfulfillableItem) o;
        return requestedQuantity == that.requestedQuantity &&
                availableQuantity == that.availableQuantity &&
                unfulfillableQuantity == that.unfulfillableQuantity &&
                Objects.equals(sellerSku, that.sellerSku) &&
                Objects.equals(sellerFulfillmentOrderItemId, that.sellerFulfillmentOrderItemId) &&
                reason == that.reason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sellerSku, sellerFulfillmentOrderItemId, requestedQuantity,
                availableQuantity, unfulfillableQuantity, reason);
    }

    @Override
    public String toString() {
        return "UnfulfillableItem{" +
                "sellerSku='" + sellerSku + '\'' +
                ", sellerFulfillmentOrderItemId='" + sellerFulfillmentOrderItemId + '\'' +
                ", requestedQuantity=" + requestedQuantity +
                ", availableQuantity=" + availableQuantity +
                ", unfulfillableQuantity=" + unfulfillableQuantity +
                ", reason=" + reason +
                '}';
    }
}
