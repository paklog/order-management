package com.paklog.ordermanagement.domain.event;

public class FulfillmentOrderCancelledEvent extends FulfillmentOrderEvent {
    public static final String EVENT_TYPE = "com.paklog.fulfillment.order.cancelled";

    public FulfillmentOrderCancelledEvent() {
        super();
        this.type = EVENT_TYPE;
    }

    public FulfillmentOrderCancelledEvent(String orderId, String sellerFulfillmentOrderId, String cancellationReason) {
        super();
        this.type = EVENT_TYPE;
        this.subject = orderId;
        this.data = new FulfillmentOrderCancelledData(orderId, sellerFulfillmentOrderId, cancellationReason);
    }

    public static class FulfillmentOrderCancelledData {
        private String orderId;
        private String sellerFulfillmentOrderId;
        private String cancellationReason;

        public FulfillmentOrderCancelledData() {
        }

        public FulfillmentOrderCancelledData(String orderId, String sellerFulfillmentOrderId, String cancellationReason) {
            this.orderId = orderId;
            this.sellerFulfillmentOrderId = sellerFulfillmentOrderId;
            this.cancellationReason = cancellationReason;
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

        public String getCancellationReason() {
            return cancellationReason;
        }

        public void setCancellationReason(String cancellationReason) {
            this.cancellationReason = cancellationReason;
        }
    }
}