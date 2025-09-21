package com.paklog.ordermanagement.interfaces.dto;

import com.paklog.ordermanagement.domain.model.Address;
import com.paklog.ordermanagement.domain.model.OrderItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CreateFulfillmentOrderRequestTest {

    @Test
    void testDefaultConstructor() {
        CreateFulfillmentOrderRequest request = new CreateFulfillmentOrderRequest();

        assertNotNull(request);
        assertNull(request.getSellerFulfillmentOrderId());
        assertNull(request.getDisplayableOrderId());
        assertNull(request.getDisplayableOrderDate());
        assertNull(request.getDisplayableOrderComment());
        assertNull(request.getShippingSpeedCategory());
        assertNull(request.getDestinationAddress());
        assertNull(request.getItems());
    }

    @Test
    void testSettersAndGetters() {
        CreateFulfillmentOrderRequest request = new CreateFulfillmentOrderRequest();

        String sellerFulfillmentOrderId = "seller-123";
        String displayableOrderId = "display-456";
        LocalDateTime displayableOrderDate = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
        String displayableOrderComment = "Test order comment";
        String shippingSpeedCategory = "STANDARD";
        Address destinationAddress = createTestAddress();
        List<OrderItem> items = createTestItems();

        request.setSellerFulfillmentOrderId(sellerFulfillmentOrderId);
        request.setDisplayableOrderId(displayableOrderId);
        request.setDisplayableOrderDate(displayableOrderDate);
        request.setDisplayableOrderComment(displayableOrderComment);
        request.setShippingSpeedCategory(shippingSpeedCategory);
        request.setDestinationAddress(destinationAddress);
        request.setItems(items);

        assertEquals(sellerFulfillmentOrderId, request.getSellerFulfillmentOrderId());
        assertEquals(displayableOrderId, request.getDisplayableOrderId());
        assertEquals(displayableOrderDate, request.getDisplayableOrderDate());
        assertEquals(displayableOrderComment, request.getDisplayableOrderComment());
        assertEquals(shippingSpeedCategory, request.getShippingSpeedCategory());
        assertEquals(destinationAddress, request.getDestinationAddress());
        assertEquals(items, request.getItems());
    }

    @Test
    void testWithNullValues() {
        CreateFulfillmentOrderRequest request = new CreateFulfillmentOrderRequest();

        request.setSellerFulfillmentOrderId(null);
        request.setDisplayableOrderId(null);
        request.setDisplayableOrderDate(null);
        request.setDisplayableOrderComment(null);
        request.setShippingSpeedCategory(null);
        request.setDestinationAddress(null);
        request.setItems(null);

        assertNull(request.getSellerFulfillmentOrderId());
        assertNull(request.getDisplayableOrderId());
        assertNull(request.getDisplayableOrderDate());
        assertNull(request.getDisplayableOrderComment());
        assertNull(request.getShippingSpeedCategory());
        assertNull(request.getDestinationAddress());
        assertNull(request.getItems());
    }

    @Test
    void testWithEmptyValues() {
        CreateFulfillmentOrderRequest request = new CreateFulfillmentOrderRequest();

        request.setSellerFulfillmentOrderId("");
        request.setDisplayableOrderId("");
        request.setDisplayableOrderComment("");
        request.setShippingSpeedCategory("");
        request.setItems(new ArrayList<>());

        assertEquals("", request.getSellerFulfillmentOrderId());
        assertEquals("", request.getDisplayableOrderId());
        assertEquals("", request.getDisplayableOrderComment());
        assertEquals("", request.getShippingSpeedCategory());
        assertNotNull(request.getItems());
        assertTrue(request.getItems().isEmpty());
    }

    @Test
    void testWithMultipleItems() {
        CreateFulfillmentOrderRequest request = new CreateFulfillmentOrderRequest();

        List<OrderItem> items = Arrays.asList(
            new OrderItem("SKU-1", "item-1", 1, "Gift 1", "Comment 1"),
            new OrderItem("SKU-2", "item-2", 2, "Gift 2", "Comment 2"),
            new OrderItem("SKU-3", "item-3", 3, "Gift 3", "Comment 3")
        );

        request.setItems(items);

        assertEquals(3, request.getItems().size());
        assertEquals("SKU-1", request.getItems().get(0).getSellerSku());
        assertEquals("SKU-2", request.getItems().get(1).getSellerSku());
        assertEquals("SKU-3", request.getItems().get(2).getSellerSku());
    }

    @Test
    void testWithCompleteData() {
        CreateFulfillmentOrderRequest request = createCompleteRequest();

        assertNotNull(request.getSellerFulfillmentOrderId());
        assertNotNull(request.getDisplayableOrderId());
        assertNotNull(request.getDisplayableOrderDate());
        assertNotNull(request.getDisplayableOrderComment());
        assertNotNull(request.getShippingSpeedCategory());
        assertNotNull(request.getDestinationAddress());
        assertNotNull(request.getItems());
        assertFalse(request.getItems().isEmpty());
    }

    @Test
    void testItemsListModification() {
        CreateFulfillmentOrderRequest request = new CreateFulfillmentOrderRequest();
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("SKU-1", "item-1", 1, null, null));

        request.setItems(items);
        assertEquals(1, request.getItems().size());

        // Modify the original list
        items.add(new OrderItem("SKU-2", "item-2", 2, null, null));

        // The request should reflect the change (same reference)
        assertEquals(2, request.getItems().size());
    }

    @Test
    void testAddressReference() {
        CreateFulfillmentOrderRequest request = new CreateFulfillmentOrderRequest();
        Address address = createTestAddress();

        request.setDestinationAddress(address);
        assertSame(address, request.getDestinationAddress());

        // Modify the address
        address.setName("Modified Name");
        assertEquals("Modified Name", request.getDestinationAddress().getName());
    }

    @Test
    void testDateHandling() {
        CreateFulfillmentOrderRequest request = new CreateFulfillmentOrderRequest();

        LocalDateTime pastDate = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
        LocalDateTime futureDate = LocalDateTime.of(2030, 12, 31, 23, 59, 59);

        request.setDisplayableOrderDate(pastDate);
        assertEquals(pastDate, request.getDisplayableOrderDate());

        request.setDisplayableOrderDate(futureDate);
        assertEquals(futureDate, request.getDisplayableOrderDate());
    }

    @Test
    void testShippingSpeedCategories() {
        CreateFulfillmentOrderRequest request = new CreateFulfillmentOrderRequest();

        String[] categories = {"STANDARD", "EXPEDITED", "PRIORITY", "SAME_DAY"};

        for (String category : categories) {
            request.setShippingSpeedCategory(category);
            assertEquals(category, request.getShippingSpeedCategory());
        }
    }

    private CreateFulfillmentOrderRequest createCompleteRequest() {
        CreateFulfillmentOrderRequest request = new CreateFulfillmentOrderRequest();

        request.setSellerFulfillmentOrderId("seller-123");
        request.setDisplayableOrderId("display-456");
        request.setDisplayableOrderDate(LocalDateTime.now());
        request.setDisplayableOrderComment("Complete test order");
        request.setShippingSpeedCategory("STANDARD");
        request.setDestinationAddress(createTestAddress());
        request.setItems(createTestItems());

        return request;
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