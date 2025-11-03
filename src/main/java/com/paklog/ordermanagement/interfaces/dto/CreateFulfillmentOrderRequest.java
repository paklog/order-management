package com.paklog.ordermanagement.interfaces.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import com.paklog.ordermanagement.domain.model.Address;
import com.paklog.ordermanagement.domain.model.OrderItem;

public class CreateFulfillmentOrderRequest {

    @NotBlank(message = "Seller fulfillment order ID is required")
    @Size(max = 255, message = "Seller fulfillment order ID must not exceed 255 characters")
    private String sellerFulfillmentOrderId;

    @NotBlank(message = "Displayable order ID is required")
    @Size(max = 255, message = "Displayable order ID must not exceed 255 characters")
    private String displayableOrderId;

    @NotNull(message = "Displayable order date is required")
    @PastOrPresent(message = "Order date cannot be in the future")
    private LocalDateTime displayableOrderDate;

    @Size(max = 500, message = "Displayable order comment must not exceed 500 characters")
    private String displayableOrderComment;

    @NotBlank(message = "Shipping speed category is required")
    private String shippingSpeedCategory;

    @NotNull(message = "Destination address is required")
    @Valid
    private Address destinationAddress;

    @NotEmpty(message = "Order must have at least one item")
    @Size(max = 100, message = "Maximum 100 items per order")
    @Valid
    private List<OrderItem> items;

    // Getters and setters
    public String getSellerFulfillmentOrderId() {
        return sellerFulfillmentOrderId;
    }

    public void setSellerFulfillmentOrderId(String sellerFulfillmentOrderId) {
        this.sellerFulfillmentOrderId = sellerFulfillmentOrderId;
    }

    public String getDisplayableOrderId() {
        return displayableOrderId;
    }

    public void setDisplayableOrderId(String displayableOrderId) {
        this.displayableOrderId = displayableOrderId;
    }

    public LocalDateTime getDisplayableOrderDate() {
        return displayableOrderDate;
    }

    public void setDisplayableOrderDate(LocalDateTime displayableOrderDate) {
        this.displayableOrderDate = displayableOrderDate;
    }

    public String getDisplayableOrderComment() {
        return displayableOrderComment;
    }

    public void setDisplayableOrderComment(String displayableOrderComment) {
        this.displayableOrderComment = displayableOrderComment;
    }

    public String getShippingSpeedCategory() {
        return shippingSpeedCategory;
    }

    public void setShippingSpeedCategory(String shippingSpeedCategory) {
        this.shippingSpeedCategory = shippingSpeedCategory;
    }

    public Address getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(Address destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}