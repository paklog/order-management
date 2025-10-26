package com.paklog.ordermanagement.domain.event;

import com.paklog.ordermanagement.domain.model.Address;
import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.domain.model.OrderItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FulfillmentOrderReceivedEventTest {

    @Test
    void testDefaultConstructor() {
        LocalDateTime before = LocalDateTime.now();
        FulfillmentOrderReceivedEvent event = new FulfillmentOrderReceivedEvent();
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(event);
        assertNotNull(event.getId());
        assertEquals(FulfillmentOrderReceivedEvent.EVENT_TYPE, event.getType());
        assertEquals("/fulfillment/order-management-service", event.getSource());
        assertNull(event.getSubject());
        assertNotNull(event.getTime());
        assertTrue(event.getTime().isAfter(before.minusSeconds(1)));
        assertTrue(event.getTime().isBefore(after.plusSeconds(1)));
        assertNull(event.getData());
    }

    @Test
    void testParameterizedConstructor() {
        FulfillmentOrder order = createTestOrder();
        LocalDateTime before = LocalDateTime.now();
        FulfillmentOrderReceivedEvent event = new FulfillmentOrderReceivedEvent(order);
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(event);
        assertNotNull(event.getId());
        assertEquals(FulfillmentOrderReceivedEvent.EVENT_TYPE, event.getType());
        assertEquals("/fulfillment/order-management-service", event.getSource());
        assertEquals(order.getOrderId().toString(), event.getSubject());
        assertNotNull(event.getTime());
        assertTrue(event.getTime().isAfter(before.minusSeconds(1)));
        assertTrue(event.getTime().isBefore(after.plusSeconds(1)));
        assertNotNull(event.getData());
        assertTrue(event.getData() instanceof FulfillmentOrderReceivedEvent.FulfillmentOrderData);

        FulfillmentOrderReceivedEvent.FulfillmentOrderData data =
            (FulfillmentOrderReceivedEvent.FulfillmentOrderData) event.getData();
        assertEquals(order, data.getOrder());
    }

    @Test
    void testEventTypeConstant() {
        assertEquals("com.paklog.fulfillment.order.received", FulfillmentOrderReceivedEvent.EVENT_TYPE);
    }

    @Test
    void testIdIsUnique() {
        FulfillmentOrderReceivedEvent event1 = new FulfillmentOrderReceivedEvent();
        FulfillmentOrderReceivedEvent event2 = new FulfillmentOrderReceivedEvent();

        assertNotEquals(event1.getId(), event2.getId());
    }

    @Test
    void testSettersAndGetters() {
        FulfillmentOrderReceivedEvent event = new FulfillmentOrderReceivedEvent();
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
    void testFulfillmentOrderDataDefaultConstructor() {
        FulfillmentOrderReceivedEvent.FulfillmentOrderData data =
            new FulfillmentOrderReceivedEvent.FulfillmentOrderData();

        assertNotNull(data);
        assertNull(data.getOrder());
    }

    @Test
    void testFulfillmentOrderDataParameterizedConstructor() {
        FulfillmentOrder order = createTestOrder();
        FulfillmentOrderReceivedEvent.FulfillmentOrderData data =
            new FulfillmentOrderReceivedEvent.FulfillmentOrderData(order);

        assertNotNull(data);
        assertEquals(order, data.getOrder());
    }

    @Test
    void testFulfillmentOrderDataSettersAndGetters() {
        FulfillmentOrder order = createTestOrder();
        FulfillmentOrderReceivedEvent.FulfillmentOrderData data =
            new FulfillmentOrderReceivedEvent.FulfillmentOrderData();

        data.setOrder(order);
        assertEquals(order, data.getOrder());

        data.setOrder(null);
        assertNull(data.getOrder());
    }

    @Test
    void testWithNullOrder() {
        assertThrows(NullPointerException.class, () -> {
            new FulfillmentOrderReceivedEvent(null);
        });
    }

    private FulfillmentOrder createTestOrder() {
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("SKU-123", "item-1", 2, "Happy Birthday!", "Fragile"));

        Address address = new Address("John Doe", "123 Main St", "Apt 4B", "New York", "NY", "10001", "US");

        return new FulfillmentOrder(
                UUID.randomUUID(),
                "seller-123",
                "display-123",
                LocalDateTime.now(),
                "Test order",
                "STANDARD",
                address,
                items
        );
    }
}