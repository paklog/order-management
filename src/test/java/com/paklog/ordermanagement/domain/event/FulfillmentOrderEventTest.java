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

class FulfillmentOrderEventTest {

    static class TestFulfillmentOrderEvent extends FulfillmentOrderEvent {
        public TestFulfillmentOrderEvent() {
            super();
        }

        public TestFulfillmentOrderEvent(FulfillmentOrder order) {
            super(order);
        }
    }

    @Test
    void testDefaultConstructor() {
        LocalDateTime before = LocalDateTime.now();
        TestFulfillmentOrderEvent event = new TestFulfillmentOrderEvent();
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(event);
        assertNotNull(event.getId());
        assertNull(event.getType());
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
        TestFulfillmentOrderEvent event = new TestFulfillmentOrderEvent(order);

        assertNotNull(event);
        assertNotNull(event.getId());
        assertEquals("/fulfillment/order-management-service", event.getSource());
        assertEquals(order.getOrderId().toString(), event.getSubject());
        assertNotNull(event.getTime());
        assertNull(event.getData()); // Not set by base class
    }

    @Test
    void testSettersAndGetters() {
        TestFulfillmentOrderEvent event = new TestFulfillmentOrderEvent();
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
    void testIdIsUnique() {
        TestFulfillmentOrderEvent event1 = new TestFulfillmentOrderEvent();
        TestFulfillmentOrderEvent event2 = new TestFulfillmentOrderEvent();

        assertNotEquals(event1.getId(), event2.getId());
    }

    @Test
    void testWithNullOrder() {
        assertThrows(NullPointerException.class, () -> {
            new TestFulfillmentOrderEvent(null);
        });
    }

    @Test
    void testTimeIsSetInConstructor() {
        TestFulfillmentOrderEvent event1 = new TestFulfillmentOrderEvent();

        // Small delay to ensure different timestamps
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        TestFulfillmentOrderEvent event2 = new TestFulfillmentOrderEvent();

        assertNotNull(event1.getTime());
        assertNotNull(event2.getTime());
        assertTrue(event2.getTime().isAfter(event1.getTime()) ||
                  event2.getTime().isEqual(event1.getTime()));
    }

    private FulfillmentOrder createTestOrder() {
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("SKU-123", "item-1", 2, "Gift", "Comment"));

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