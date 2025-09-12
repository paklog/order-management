package com.paklog.ordermanagement.interfaces.dto;

public class CancelFulfillmentOrderRequest {
    private String cancellationReason;

    public CancelFulfillmentOrderRequest() {
        // Default constructor for frameworks
    }

    public CancelFulfillmentOrderRequest(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}

