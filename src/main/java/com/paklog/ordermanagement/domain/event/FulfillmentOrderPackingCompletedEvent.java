package com.paklog.ordermanagement.domain.event;

import com.paklog.ordermanagement.domain.model.FulfillmentOrder;

public class FulfillmentOrderPackingCompletedEvent extends FulfillmentOrderEvent {

    public FulfillmentOrderPackingCompletedEvent(FulfillmentOrder data) {
        super(data);
        this.type = "FulfillmentOrderPackingCompleted";
        this.data = data;
    }
}
