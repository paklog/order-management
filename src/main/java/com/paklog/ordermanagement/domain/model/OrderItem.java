package com.paklog.ordermanagement.domain.model;

import java.util.Objects;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderItem {

    @NotBlank(message = "Seller SKU is required")
    @Size(max = 255, message = "Seller SKU must not exceed 255 characters")
    @com.fasterxml.jackson.annotation.JsonProperty("seller_sku")
    private String sellerSku;

    @NotBlank(message = "Seller fulfillment order item ID is required")
    @Size(max = 255, message = "Seller fulfillment order item ID must not exceed 255 characters")
    @com.fasterxml.jackson.annotation.JsonProperty("seller_fulfillment_order_item_id")
    private String sellerFulfillmentOrderItemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10000, message = "Quantity must not exceed 10,000")
    @com.fasterxml.jackson.annotation.JsonProperty("quantity")
    private Integer quantity;

    @Size(max = 500, message = "Gift message must not exceed 500 characters")
    @com.fasterxml.jackson.annotation.JsonProperty("gift_message")
    private String giftMessage;

    @Size(max = 500, message = "Displayable comment must not exceed 500 characters")
    @com.fasterxml.jackson.annotation.JsonProperty("displayable_comment")
    private String displayableComment;

    public OrderItem() {
        // Default constructor for frameworks
    }

    public OrderItem(String sellerSku, String sellerFulfillmentOrderItemId, Integer quantity, 
                     String giftMessage, String displayableComment) {
        this.sellerSku = sellerSku;
        this.sellerFulfillmentOrderItemId = sellerFulfillmentOrderItemId;
        this.quantity = quantity;
        this.giftMessage = giftMessage;
        this.displayableComment = displayableComment;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getGiftMessage() {
        return giftMessage;
    }

    public void setGiftMessage(String giftMessage) {
        this.giftMessage = giftMessage;
    }

    public String getDisplayableComment() {
        return displayableComment;
    }

    public void setDisplayableComment(String displayableComment) {
        this.displayableComment = displayableComment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(sellerSku, orderItem.sellerSku) &&
                Objects.equals(sellerFulfillmentOrderItemId, orderItem.sellerFulfillmentOrderItemId) &&
                Objects.equals(quantity, orderItem.quantity) &&
                Objects.equals(giftMessage, orderItem.giftMessage) &&
                Objects.equals(displayableComment, orderItem.displayableComment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sellerSku, sellerFulfillmentOrderItemId, quantity, giftMessage, displayableComment);
    }
}