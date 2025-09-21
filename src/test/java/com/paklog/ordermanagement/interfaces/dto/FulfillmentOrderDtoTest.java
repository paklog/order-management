package com.paklog.ordermanagement.interfaces.dto;

import com.paklog.ordermanagement.domain.model.Address;
import com.paklog.ordermanagement.domain.model.FulfillmentOrderStatus;
import com.paklog.ordermanagement.domain.model.OrderItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FulfillmentOrderDtoTest {

    @Test
    void testDefaultConstructor() {
        FulfillmentOrderDto dto = new FulfillmentOrderDto();

        assertNotNull(dto);
        assertNull(dto.getOrderId());
        assertNull(dto.getSellerFulfillmentOrderId());
        assertNull(dto.getDisplayableOrderId());
        assertNull(dto.getDisplayableOrderDate());
        assertNull(dto.getDisplayableOrderComment());
        assertNull(dto.getShippingSpeedCategory());
        assertNull(dto.getDestinationAddress());
        assertNull(dto.getStatus());
        assertNull(dto.getItems());
        assertNull(dto.getReceivedDate());
        assertNull(dto.getCancellationReason());
    }

    @Test
    void testSettersAndGetters() {
        FulfillmentOrderDto dto = new FulfillmentOrderDto();

        UUID orderId = UUID.randomUUID();
        String sellerFulfillmentOrderId = "seller-123";
        String displayableOrderId = "display-456";
        LocalDateTime displayableOrderDate = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
        String displayableOrderComment = "Test order comment";
        String shippingSpeedCategory = "STANDARD";
        Address destinationAddress = createTestAddress();
        FulfillmentOrderStatus status = FulfillmentOrderStatus.RECEIVED;
        List<OrderItem> items = createTestItems();
        LocalDateTime receivedDate = LocalDateTime.of(2023, 1, 1, 13, 0, 0);
        String cancellationReason = "Customer requested";

        dto.setOrderId(orderId);
        dto.setSellerFulfillmentOrderId(sellerFulfillmentOrderId);
        dto.setDisplayableOrderId(displayableOrderId);
        dto.setDisplayableOrderDate(displayableOrderDate);
        dto.setDisplayableOrderComment(displayableOrderComment);
        dto.setShippingSpeedCategory(shippingSpeedCategory);
        dto.setDestinationAddress(destinationAddress);
        dto.setStatus(status);
        dto.setItems(items);
        dto.setReceivedDate(receivedDate);
        dto.setCancellationReason(cancellationReason);

        assertEquals(orderId, dto.getOrderId());
        assertEquals(sellerFulfillmentOrderId, dto.getSellerFulfillmentOrderId());
        assertEquals(displayableOrderId, dto.getDisplayableOrderId());
        assertEquals(displayableOrderDate, dto.getDisplayableOrderDate());
        assertEquals(displayableOrderComment, dto.getDisplayableOrderComment());
        assertEquals(shippingSpeedCategory, dto.getShippingSpeedCategory());
        assertEquals(destinationAddress, dto.getDestinationAddress());
        assertEquals(status, dto.getStatus());
        assertEquals(items, dto.getItems());
        assertEquals(receivedDate, dto.getReceivedDate());
        assertEquals(cancellationReason, dto.getCancellationReason());
    }

    @Test
    void testWithAllStatusValues() {
        FulfillmentOrderDto dto = new FulfillmentOrderDto();

        for (FulfillmentOrderStatus status : FulfillmentOrderStatus.values()) {
            dto.setStatus(status);
            assertEquals(status, dto.getStatus());
        }
    }

    @Test
    void testWithNullValues() {
        FulfillmentOrderDto dto = new FulfillmentOrderDto();

        dto.setOrderId(null);
        dto.setSellerFulfillmentOrderId(null);
        dto.setDisplayableOrderId(null);
        dto.setDisplayableOrderDate(null);
        dto.setDisplayableOrderComment(null);
        dto.setShippingSpeedCategory(null);
        dto.setDestinationAddress(null);
        dto.setStatus(null);
        dto.setItems(null);
        dto.setReceivedDate(null);
        dto.setCancellationReason(null);

        assertNull(dto.getOrderId());
        assertNull(dto.getSellerFulfillmentOrderId());
        assertNull(dto.getDisplayableOrderId());
        assertNull(dto.getDisplayableOrderDate());
        assertNull(dto.getDisplayableOrderComment());
        assertNull(dto.getShippingSpeedCategory());
        assertNull(dto.getDestinationAddress());
        assertNull(dto.getStatus());
        assertNull(dto.getItems());
        assertNull(dto.getReceivedDate());
        assertNull(dto.getCancellationReason());
    }

    @Test
    void testWithEmptyValues() {
        FulfillmentOrderDto dto = new FulfillmentOrderDto();

        dto.setSellerFulfillmentOrderId("");
        dto.setDisplayableOrderId("");
        dto.setDisplayableOrderComment("");
        dto.setShippingSpeedCategory("");
        dto.setItems(new ArrayList<>());
        dto.setCancellationReason("");

        assertEquals("", dto.getSellerFulfillmentOrderId());
        assertEquals("", dto.getDisplayableOrderId());
        assertEquals("", dto.getDisplayableOrderComment());
        assertEquals("", dto.getShippingSpeedCategory());
        assertNotNull(dto.getItems());
        assertTrue(dto.getItems().isEmpty());
        assertEquals("", dto.getCancellationReason());
    }

    @Test
    void testWithMultipleItems() {
        FulfillmentOrderDto dto = new FulfillmentOrderDto();

        List<OrderItem> items = Arrays.asList(
            new OrderItem("SKU-1", "item-1", 1, "Gift 1", "Comment 1"),
            new OrderItem("SKU-2", "item-2", 2, "Gift 2", "Comment 2"),
            new OrderItem("SKU-3", "item-3", 3, "Gift 3", "Comment 3")
        );

        dto.setItems(items);

        assertEquals(3, dto.getItems().size());
        assertEquals("SKU-1", dto.getItems().get(0).getSellerSku());
        assertEquals("SKU-2", dto.getItems().get(1).getSellerSku());
        assertEquals("SKU-3", dto.getItems().get(2).getSellerSku());
    }

    @Test
    void testDateHandling() {
        FulfillmentOrderDto dto = new FulfillmentOrderDto();

        LocalDateTime orderDate = LocalDateTime.of(2023, 1, 1, 10, 0, 0);
        LocalDateTime receivedDate = LocalDateTime.of(2023, 1, 1, 11, 0, 0);

        dto.setDisplayableOrderDate(orderDate);
        dto.setReceivedDate(receivedDate);

        assertEquals(orderDate, dto.getDisplayableOrderDate());
        assertEquals(receivedDate, dto.getReceivedDate());
        assertTrue(dto.getReceivedDate().isAfter(dto.getDisplayableOrderDate()));
    }

    @Test
    void testUuidHandling() {
        FulfillmentOrderDto dto = new FulfillmentOrderDto();

        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        dto.setOrderId(orderId1);
        assertEquals(orderId1, dto.getOrderId());

        dto.setOrderId(orderId2);
        assertEquals(orderId2, dto.getOrderId());
        assertNotEquals(orderId1, dto.getOrderId());
    }

    @Test
    void testCompleteOrderLifecycle() {
        FulfillmentOrderDto dto = createCompleteDto();

        // Initial state
        assertEquals(FulfillmentOrderStatus.RECEIVED, dto.getStatus());
        assertNull(dto.getCancellationReason());

        // Update status
        dto.setStatus(FulfillmentOrderStatus.VALIDATED);
        assertEquals(FulfillmentOrderStatus.VALIDATED, dto.getStatus());

        // Cancel order
        dto.setStatus(FulfillmentOrderStatus.CANCELLED);
        dto.setCancellationReason("Customer requested cancellation");
        assertEquals(FulfillmentOrderStatus.CANCELLED, dto.getStatus());
        assertEquals("Customer requested cancellation", dto.getCancellationReason());
    }

    @Test
    void testItemsListModification() {
        FulfillmentOrderDto dto = new FulfillmentOrderDto();
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("SKU-1", "item-1", 1, null, null));

        dto.setItems(items);
        assertEquals(1, dto.getItems().size());

        // Modify the original list
        items.add(new OrderItem("SKU-2", "item-2", 2, null, null));

        // The DTO should reflect the change (same reference)
        assertEquals(2, dto.getItems().size());
    }

    @Test
    void testAddressReference() {
        FulfillmentOrderDto dto = new FulfillmentOrderDto();
        Address address = createTestAddress();

        dto.setDestinationAddress(address);
        assertSame(address, dto.getDestinationAddress());

        // Modify the address
        address.setName("Modified Name");
        assertEquals("Modified Name", dto.getDestinationAddress().getName());
    }

    @Test
    void testShippingSpeedCategories() {
        FulfillmentOrderDto dto = new FulfillmentOrderDto();

        String[] categories = {"STANDARD", "EXPEDITED", "PRIORITY", "SAME_DAY"};

        for (String category : categories) {
            dto.setShippingSpeedCategory(category);
            assertEquals(category, dto.getShippingSpeedCategory());
        }
    }

    @Test
    void testCancellationReasonHandling() {
        FulfillmentOrderDto dto = new FulfillmentOrderDto();

        String[] reasons = {
            "Customer requested",
            "Out of stock",
            "Payment failed",
            "Address invalid"
        };

        for (String reason : reasons) {
            dto.setCancellationReason(reason);
            assertEquals(reason, dto.getCancellationReason());
        }
    }

    private FulfillmentOrderDto createCompleteDto() {
        FulfillmentOrderDto dto = new FulfillmentOrderDto();

        dto.setOrderId(UUID.randomUUID());
        dto.setSellerFulfillmentOrderId("seller-123");
        dto.setDisplayableOrderId("display-456");
        dto.setDisplayableOrderDate(LocalDateTime.now().minusHours(1));
        dto.setDisplayableOrderComment("Complete test order");
        dto.setShippingSpeedCategory("STANDARD");
        dto.setDestinationAddress(createTestAddress());
        dto.setStatus(FulfillmentOrderStatus.RECEIVED);
        dto.setItems(createTestItems());
        dto.setReceivedDate(LocalDateTime.now());

        return dto;
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
        items.add(new OrderItem("SKU-123", "item-1", 2, "Happy Birthday!", "Fragile"));
        return items;
    }
}