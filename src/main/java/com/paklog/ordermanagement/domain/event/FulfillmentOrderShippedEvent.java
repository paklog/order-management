package com.paklog.ordermanagement.domain.event;

import com.paklog.ordermanagement.domain.model.FulfillmentOrder;

public class FulfillmentOrderShippedEvent extends FulfillmentOrderEvent {

    public FulfillmentOrderShippedEvent(FulfillmentOrder data) {
        super(data);
        this.type = "FulfillmentOrderShipped";
        this.data = data;
    }
}
