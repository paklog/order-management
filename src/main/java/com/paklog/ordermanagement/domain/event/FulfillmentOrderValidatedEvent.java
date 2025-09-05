package com.paklog.ordermanagement.domain.event;

import com.paklog.ordermanagement.domain.model.FulfillmentOrder;

public class FulfillmentOrderValidatedEvent extends FulfillmentOrderEvent {
    public static final String EVENT_TYPE = "com.example.fulfillment.order.validated";

    public FulfillmentOrderValidatedEvent() {
        super();
        this.type = EVENT_TYPE;
    }

    public FulfillmentOrderValidatedEvent(FulfillmentOrder order) {
        super(order);
        this.type = EVENT_TYPE;
        this.data = new FulfillmentOrderData(order);
    }

    public static class FulfillmentOrderData {
        private FulfillmentOrder order;

        public FulfillmentOrderData() {
        }

        public FulfillmentOrderData(FulfillmentOrder order) {
            this.order = order;
        }

        public FulfillmentOrder getOrder() {
            return order;
        }

        public void setOrder(FulfillmentOrder order) {
            this.order = order;
        }
    }
}