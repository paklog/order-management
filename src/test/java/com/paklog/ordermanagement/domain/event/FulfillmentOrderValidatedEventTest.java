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

class FulfillmentOrderValidatedEventTest {

    @Test
    void testDefaultConstructor() {
        FulfillmentOrderValidatedEvent event = new FulfillmentOrderValidatedEvent();

        assertNotNull(event);
        assertEquals(FulfillmentOrderValidatedEvent.EVENT_TYPE, event.getType());
        assertEquals("/fulfillment/order-management-service", event.getSource());
        assertNull(event.getData());
    }

    @Test
    void testParameterizedConstructor() {
        FulfillmentOrder order = createTestOrder();
        FulfillmentOrderValidatedEvent event = new FulfillmentOrderValidatedEvent(order);

        assertNotNull(event);
        assertEquals(FulfillmentOrderValidatedEvent.EVENT_TYPE, event.getType());
        assertEquals(order.getOrderId().toString(), event.getSubject());
        assertNotNull(event.getData());
    }

    @Test
    void testEventTypeConstant() {
        assertEquals("com.example.fulfillment.order.validated", FulfillmentOrderValidatedEvent.EVENT_TYPE);
    }

    @Test
    void testFulfillmentOrderData() {
        FulfillmentOrder order = createTestOrder();
        FulfillmentOrderValidatedEvent.FulfillmentOrderData data =
            new FulfillmentOrderValidatedEvent.FulfillmentOrderData(order);

        assertEquals(order, data.getOrder());
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