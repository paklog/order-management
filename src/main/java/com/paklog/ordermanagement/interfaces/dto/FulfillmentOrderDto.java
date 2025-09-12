package com.paklog.ordermanagement.interfaces.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.paklog.ordermanagement.domain.model.Address;
import com.paklog.ordermanagement.domain.model.FulfillmentOrderStatus;
import com.paklog.ordermanagement.domain.model.OrderItem;

public class FulfillmentOrderDto {
    private UUID orderId;
    private String sellerFulfillmentOrderId;
    private String displayableOrderId;
    private LocalDateTime displayableOrderDate;
    private String displayableOrderComment;
    private String shippingSpeedCategory;
    private Address destinationAddress;
    private FulfillmentOrderStatus status;
    private List<OrderItem> items;
    private LocalDateTime receivedDate;
    private String cancellationReason;

    // Getters and setters
    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

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

    public FulfillmentOrderStatus getStatus() {
        return status;
    }

    public void setStatus(FulfillmentOrderStatus status) {
        this.status = status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public LocalDateTime getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDateTime receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}