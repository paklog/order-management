package com.paklog.ordermanagement.domain.event;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FulfillmentOrderCancelledEventTest {

    @Test
    void testDefaultConstructor() {
        LocalDateTime before = LocalDateTime.now();
        FulfillmentOrderCancelledEvent event = new FulfillmentOrderCancelledEvent();
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(event);
        assertNotNull(event.getId());
        assertEquals(FulfillmentOrderCancelledEvent.EVENT_TYPE, event.getType());
        assertEquals("/fulfillment/order-management-service", event.getSource());
        assertNull(event.getSubject());
        assertNotNull(event.getTime());
        assertTrue(event.getTime().isAfter(before.minusSeconds(1)));
        assertTrue(event.getTime().isBefore(after.plusSeconds(1)));
        assertNull(event.getData());
    }

    @Test
    void testParameterizedConstructor() {
        String orderId = "order-123";
        String sellerFulfillmentOrderId = "seller-456";
        String cancellationReason = "Customer requested cancellation";

        LocalDateTime before = LocalDateTime.now();
        FulfillmentOrderCancelledEvent event = new FulfillmentOrderCancelledEvent(
            orderId, sellerFulfillmentOrderId, cancellationReason);
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(event);
        assertNotNull(event.getId());
        assertEquals(FulfillmentOrderCancelledEvent.EVENT_TYPE, event.getType());
        assertEquals("/fulfillment/order-management-service", event.getSource());
        assertEquals(orderId, event.getSubject());
        assertNotNull(event.getTime());
        assertTrue(event.getTime().isAfter(before.minusSeconds(1)));
        assertTrue(event.getTime().isBefore(after.plusSeconds(1)));
        assertNotNull(event.getData());
        assertTrue(event.getData() instanceof FulfillmentOrderCancelledEvent.FulfillmentOrderCancelledData);

        FulfillmentOrderCancelledEvent.FulfillmentOrderCancelledData data =
            (FulfillmentOrderCancelledEvent.FulfillmentOrderCancelledData) event.getData();
        assertEquals(orderId, data.getOrderId());
        assertEquals(sellerFulfillmentOrderId, data.getSellerFulfillmentOrderId());
        assertEquals(cancellationReason, data.getCancellationReason());
    }

    @Test
    void testEventTypeConstant() {
        assertEquals("com.example.fulfillment.order.cancelled", FulfillmentOrderCancelledEvent.EVENT_TYPE);
    }

    @Test
    void testIdIsUnique() {
        FulfillmentOrderCancelledEvent event1 = new FulfillmentOrderCancelledEvent();
        FulfillmentOrderCancelledEvent event2 = new FulfillmentOrderCancelledEvent();

        assertNotEquals(event1.getId(), event2.getId());
    }

    @Test
    void testSettersAndGetters() {
        FulfillmentOrderCancelledEvent event = new FulfillmentOrderCancelledEvent();
        LocalDateTime testTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0);

        event.setId("test-id");
        event.setType("test.type");
        event.setSource("test-source");
        event.setSubject("test-subject");
        event.setTime(testTime);
        event.setData("test-data");

        assertEquals("test-id", event.getId());
        assertEquals("test.type", event.getType());
        assertEquals("test-source", event.getSource());
        assertEquals("test-subject", event.getSubject());
        assertEquals(testTime, event.getTime());
        assertEquals("test-data", event.getData());
    }

    @Test
    void testCancelledDataDefaultConstructor() {
        FulfillmentOrderCancelledEvent.FulfillmentOrderCancelledData data =
            new FulfillmentOrderCancelledEvent.FulfillmentOrderCancelledData();

        assertNotNull(data);
        assertNull(data.getOrderId());
        assertNull(data.getSellerFulfillmentOrderId());
        assertNull(data.getCancellationReason());
    }

    @Test
    void testCancelledDataParameterizedConstructor() {
        String orderId = "order-123";
        String sellerFulfillmentOrderId = "seller-456";
        String cancellationReason = "Customer requested cancellation";

        FulfillmentOrderCancelledEvent.FulfillmentOrderCancelledData data =
            new FulfillmentOrderCancelledEvent.FulfillmentOrderCancelledData(
                orderId, sellerFulfillmentOrderId, cancellationReason);

        assertNotNull(data);
        assertEquals(orderId, data.getOrderId());
        assertEquals(sellerFulfillmentOrderId, data.getSellerFulfillmentOrderId());
        assertEquals(cancellationReason, data.getCancellationReason());
    }

    @Test
    void testCancelledDataSettersAndGetters() {
        String orderId = "order-789";
        String sellerFulfillmentOrderId = "seller-101";
        String cancellationReason = "Out of stock";

        FulfillmentOrderCancelledEvent.FulfillmentOrderCancelledData data =
            new FulfillmentOrderCancelledEvent.FulfillmentOrderCancelledData();

        data.setOrderId(orderId);
        data.setSellerFulfillmentOrderId(sellerFulfillmentOrderId);
        data.setCancellationReason(cancellationReason);

        assertEquals(orderId, data.getOrderId());
        assertEquals(sellerFulfillmentOrderId, data.getSellerFulfillmentOrderId());
        assertEquals(cancellationReason, data.getCancellationReason());
    }

    @Test
    void testWithNullValues() {
        FulfillmentOrderCancelledEvent event = new FulfillmentOrderCancelledEvent(null, null, null);

        assertNotNull(event);
        assertNull(event.getSubject());
        assertNotNull(event.getData());

        FulfillmentOrderCancelledEvent.FulfillmentOrderCancelledData data =
            (FulfillmentOrderCancelledEvent.FulfillmentOrderCancelledData) event.getData();
        assertNull(data.getOrderId());
        assertNull(data.getSellerFulfillmentOrderId());
        assertNull(data.getCancellationReason());
    }

    @Test
    void testWithEmptyValues() {
        FulfillmentOrderCancelledEvent event = new FulfillmentOrderCancelledEvent("", "", "");

        assertNotNull(event);
        assertEquals("", event.getSubject());
        assertNotNull(event.getData());

        FulfillmentOrderCancelledEvent.FulfillmentOrderCancelledData data =
            (FulfillmentOrderCancelledEvent.FulfillmentOrderCancelledData) event.getData();
        assertEquals("", data.getOrderId());
        assertEquals("", data.getSellerFulfillmentOrderId());
        assertEquals("", data.getCancellationReason());
    }

    @Test
    void testCancelledDataWithNullValues() {
        FulfillmentOrderCancelledEvent.FulfillmentOrderCancelledData data =
            new FulfillmentOrderCancelledEvent.FulfillmentOrderCancelledData(null, null, null);

        assertNotNull(data);
        assertNull(data.getOrderId());
        assertNull(data.getSellerFulfillmentOrderId());
        assertNull(data.getCancellationReason());
    }
}