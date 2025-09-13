package com.paklog.ordermanagement.domain.event;

import com.paklog.ordermanagement.domain.model.FulfillmentOrder;

public class FulfillmentOrderReleasedEvent extends FulfillmentOrderEvent {

    public FulfillmentOrderReleasedEvent(FulfillmentOrder data) {
        super(data);
        this.type = "FulfillmentOrderReleased";
        this.data = data;
    }
}
