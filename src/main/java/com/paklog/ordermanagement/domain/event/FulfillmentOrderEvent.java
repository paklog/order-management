package com.paklog.ordermanagement.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

import com.paklog.ordermanagement.domain.model.FulfillmentOrder;

public abstract class FulfillmentOrderEvent {
    protected String id;
    protected String type;
    protected String source;
    protected String subject;
    protected LocalDateTime time;
    protected Object data;

    public FulfillmentOrderEvent() {
        this.id = UUID.randomUUID().toString();
        this.time = LocalDateTime.now();
        this.source = "/fulfillment/order-management-service";
    }

    public FulfillmentOrderEvent(FulfillmentOrder order) {
        this();
        this.subject = order.getOrderId().toString();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}