package com.paklog.ordermanagement.domain.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FulfillmentOrderTest {

    @Test
    void testCreateOrder() {
        // Given
        UUID orderId = UUID.randomUUID();
        String sellerFulfillmentOrderId = "seller-123";
        String displayableOrderId = "display-123";
        LocalDateTime displayableOrderDate = LocalDateTime.now();
        String displayableOrderComment = "Test order";
        String shippingSpeedCategory = "STANDARD";
        Address destinationAddress = createTestAddress();
        List<OrderItem> items = createTestItems();

        // When
        FulfillmentOrder order = new FulfillmentOrder(
                orderId,
                sellerFulfillmentOrderId,
                displayableOrderId,
                displayableOrderDate,
                displayableOrderComment,
                shippingSpeedCategory,
                destinationAddress,
                items
        );

        // Then
        assertEquals(orderId, order.getOrderId());
        assertEquals(sellerFulfillmentOrderId, order.getSellerFulfillmentOrderId());
        assertEquals(displayableOrderId, order.getDisplayableOrderId());
        assertEquals(displayableOrderDate, order.getDisplayableOrderDate());
        assertEquals(displayableOrderComment, order.getDisplayableOrderComment());
        assertEquals(shippingSpeedCategory, order.getShippingSpeedCategory());
        assertEquals(destinationAddress, order.getDestinationAddress());
        assertEquals(items, order.getItems());
        assertEquals(FulfillmentOrderStatus.NEW, order.getStatus());
        assertNotNull(order.getReceivedDate());
    }

    @Test
    void testReceiveOrder() {
        // Given
        FulfillmentOrder order = createTestOrder();
        assertEquals(FulfillmentOrderStatus.NEW, order.getStatus());

        // When
        order.receive();

        // Then
        assertEquals(FulfillmentOrderStatus.RECEIVED, order.getStatus());
        assertNotNull(order.getReceivedDate());
    }

    @Test
    void testReceiveOrderWhenNotNew() {
        // Given
        FulfillmentOrder order = createTestOrder();
        order.receive(); // First receive
        assertEquals(FulfillmentOrderStatus.RECEIVED, order.getStatus());

        // When & Then
        assertThrows(IllegalStateException.class, () -> order.receive());
    }

    @Test
    void testCancelOrder() {
        // Given
        FulfillmentOrder order = createTestOrder();
        order.receive();
        assertEquals(FulfillmentOrderStatus.RECEIVED, order.getStatus());

        // When
        order.cancel();

        // Then
        assertEquals(FulfillmentOrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void testCancelOrderWhenShipped() {
        // Given
        FulfillmentOrder order = createTestOrder();
        order.receive();
        order.setStatus(FulfillmentOrderStatus.SHIPPED); // Manually set to shipped

        // When & Then
        assertThrows(IllegalStateException.class, () -> order.cancel());
    }

    @Test
    void testCancelOrderWhenAlreadyCancelled() {
        // Given
        FulfillmentOrder order = createTestOrder();
        order.receive();
        order.cancel(); // First cancel
        assertEquals(FulfillmentOrderStatus.CANCELLED, order.getStatus());

        // When & Then
        assertThrows(IllegalStateException.class, () -> order.cancel());
    }

    private FulfillmentOrder createTestOrder() {
        return new FulfillmentOrder(
                UUID.randomUUID(),
                "seller-123",
                "display-123",
                LocalDateTime.now(),
                "Test order",
                "STANDARD",
                createTestAddress(),
                createTestItems()
        );
    }

    private Address createTestAddress() {
        return new Address(
                "John Doe",
                "123 Main St",
                "Apt 4B",
                "New York",
                "NY",
                "10001",
                "US"
        );
    }

    private List<OrderItem> createTestItems() {
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(
                "SKU-123",
                "item-1",
                2,
                "Happy Birthday!",
                "Fragile"
        ));
        return items;
    }
}