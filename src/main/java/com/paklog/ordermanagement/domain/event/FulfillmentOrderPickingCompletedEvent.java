package com.paklog.ordermanagement.domain.event;

import com.paklog.ordermanagement.domain.model.FulfillmentOrder;

public class FulfillmentOrderPickingCompletedEvent extends FulfillmentOrderEvent {

    public FulfillmentOrderPickingCompletedEvent(FulfillmentOrder data) {
        super(data);
        this.type = "FulfillmentOrderPickingCompleted";
        this.data = data;
    }
}
