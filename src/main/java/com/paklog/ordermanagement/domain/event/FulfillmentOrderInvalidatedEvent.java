package com.paklog.ordermanagement.domain.event;

public class FulfillmentOrderInvalidatedEvent extends FulfillmentOrderEvent {
    public static final String EVENT_TYPE = "com.example.fulfillment.order.invalidated";

    public FulfillmentOrderInvalidatedEvent() {
        super();
        this.type = EVENT_TYPE;
    }

    public FulfillmentOrderInvalidatedEvent(String orderId, String sellerFulfillmentOrderId, String reason) {
        super();
        this.type = EVENT_TYPE;
        this.subject = orderId;
        this.data = new FulfillmentOrderInvalidatedData(orderId, sellerFulfillmentOrderId, reason);
    }

    public static class FulfillmentOrderInvalidatedData {
        private String orderId;
        private String sellerFulfillmentOrderId;
        private String reason;

        public FulfillmentOrderInvalidatedData() {
        }

        public FulfillmentOrderInvalidatedData(String orderId, String sellerFulfillmentOrderId, String reason) {
            this.orderId = orderId;
            this.sellerFulfillmentOrderId = sellerFulfillmentOrderId;
            this.reason = reason;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getSellerFulfillmentOrderId() {
            return sellerFulfillmentOrderId;
        }

        public void setSellerFulfillmentOrderId(String sellerFulfillmentOrderId) {
            this.sellerFulfillmentOrderId = sellerFulfillmentOrderId;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}