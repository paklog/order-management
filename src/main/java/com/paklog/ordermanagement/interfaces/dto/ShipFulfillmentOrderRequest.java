package com.paklog.ordermanagement.interfaces.dto;

public class ShipFulfillmentOrderRequest {
    private String carrier;
    private String trackingNumber;

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
}
