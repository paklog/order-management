package com.paklog.ordermanagement.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "fulfillment_orders")
public class FulfillmentOrder {
    @Id
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

    public FulfillmentOrder() {
        // Default constructor for frameworks
    }

    public FulfillmentOrder(UUID orderId, String sellerFulfillmentOrderId, String displayableOrderId,
                            LocalDateTime displayableOrderDate, String displayableOrderComment,
                            String shippingSpeedCategory, Address destinationAddress,
                            List<OrderItem> items) {
        this.orderId = orderId;
        this.sellerFulfillmentOrderId = sellerFulfillmentOrderId;
        this.displayableOrderId = displayableOrderId;
        this.displayableOrderDate = displayableOrderDate;
        this.displayableOrderComment = displayableOrderComment;
        this.shippingSpeedCategory = shippingSpeedCategory;
        this.destinationAddress = destinationAddress;
        this.items = items;
        this.status = FulfillmentOrderStatus.NEW;
        this.receivedDate = LocalDateTime.now();
    }

    // Business methods
    public void receive() {
        if (this.status != FulfillmentOrderStatus.NEW) {
            throw new IllegalStateException("Order must be in NEW status to be received");
        }
        this.status = FulfillmentOrderStatus.RECEIVED;
        this.receivedDate = LocalDateTime.now();
    }

    public void validate() {
        if (this.status != FulfillmentOrderStatus.RECEIVED) {
            throw new IllegalStateException("Order must be in RECEIVED status to be validated");
        }
        this.status = FulfillmentOrderStatus.VALIDATED;
    }

    public void invalidate(String reason) {
        if (this.status != FulfillmentOrderStatus.RECEIVED) {
            throw new IllegalStateException("Order must be in RECEIVED status to be invalidated");
        }
        this.status = FulfillmentOrderStatus.INVALIDATED;
    }

    public void cancel() {
        if (this.status == FulfillmentOrderStatus.SHIPPED) {
            throw new IllegalStateException("Cannot cancel an order that has already been shipped");
        }
        if (this.status == FulfillmentOrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }
        this.status = FulfillmentOrderStatus.CANCELLED;
    }

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
}