package com.paklog.ordermanagement.domain.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FulfillmentOrderInvalidatedEventTest {

    @Test
    void testDefaultConstructor() {
        FulfillmentOrderInvalidatedEvent event = new FulfillmentOrderInvalidatedEvent();

        assertNotNull(event);
        assertEquals(FulfillmentOrderInvalidatedEvent.EVENT_TYPE, event.getType());
        assertEquals("/fulfillment/order-management-service", event.getSource());
        assertNull(event.getData());
    }

    @Test
    void testParameterizedConstructor() {
        String orderId = "order-123";
        String sellerFulfillmentOrderId = "seller-456";
        String reason = "Invalid address";

        FulfillmentOrderInvalidatedEvent event = new FulfillmentOrderInvalidatedEvent(
            orderId, sellerFulfillmentOrderId, reason);

        assertNotNull(event);
        assertEquals(FulfillmentOrderInvalidatedEvent.EVENT_TYPE, event.getType());
        assertEquals(orderId, event.getSubject());
        assertNotNull(event.getData());
    }

    @Test
    void testEventTypeConstant() {
        assertEquals("com.example.fulfillment.order.invalidated", FulfillmentOrderInvalidatedEvent.EVENT_TYPE);
    }

    @Test
    void testInvalidatedData() {
        String orderId = "order-123";
        String sellerFulfillmentOrderId = "seller-456";
        String reason = "Invalid address";

        FulfillmentOrderInvalidatedEvent.FulfillmentOrderInvalidatedData data =
            new FulfillmentOrderInvalidatedEvent.FulfillmentOrderInvalidatedData(
                orderId, sellerFulfillmentOrderId, reason);

        assertEquals(orderId, data.getOrderId());
        assertEquals(sellerFulfillmentOrderId, data.getSellerFulfillmentOrderId());
        assertEquals(reason, data.getReason());
    }

    @Test
    void testInvalidatedDataSetters() {
        FulfillmentOrderInvalidatedEvent.FulfillmentOrderInvalidatedData data =
            new FulfillmentOrderInvalidatedEvent.FulfillmentOrderInvalidatedData();

        data.setOrderId("order-456");
        data.setSellerFulfillmentOrderId("seller-789");
        data.setReason("Payment failed");

        assertEquals("order-456", data.getOrderId());
        assertEquals("seller-789", data.getSellerFulfillmentOrderId());
        assertEquals("Payment failed", data.getReason());
    }
}