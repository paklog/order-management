package com.paklog.ordermanagement.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    @Test
    void testDefaultConstructor() {
        OrderItem orderItem = new OrderItem();

        assertNotNull(orderItem);
        assertNull(orderItem.getSellerSku());
        assertNull(orderItem.getSellerFulfillmentOrderItemId());
        assertNull(orderItem.getQuantity());
        assertNull(orderItem.getGiftMessage());
        assertNull(orderItem.getDisplayableComment());
    }

    @Test
    void testParameterizedConstructor() {
        String sellerSku = "SKU-123";
        String sellerFulfillmentOrderItemId = "item-1";
        Integer quantity = 2;
        String giftMessage = "Happy Birthday!";
        String displayableComment = "Fragile";

        OrderItem orderItem = new OrderItem(sellerSku, sellerFulfillmentOrderItemId, quantity, giftMessage, displayableComment);

        assertEquals(sellerSku, orderItem.getSellerSku());
        assertEquals(sellerFulfillmentOrderItemId, orderItem.getSellerFulfillmentOrderItemId());
        assertEquals(quantity, orderItem.getQuantity());
        assertEquals(giftMessage, orderItem.getGiftMessage());
        assertEquals(displayableComment, orderItem.getDisplayableComment());
    }

    @Test
    void testSettersAndGetters() {
        OrderItem orderItem = new OrderItem();

        orderItem.setSellerSku("SKU-456");
        orderItem.setSellerFulfillmentOrderItemId("item-2");
        orderItem.setQuantity(5);
        orderItem.setGiftMessage("Congratulations!");
        orderItem.setDisplayableComment("Handle with care");

        assertEquals("SKU-456", orderItem.getSellerSku());
        assertEquals("item-2", orderItem.getSellerFulfillmentOrderItemId());
        assertEquals(Integer.valueOf(5), orderItem.getQuantity());
        assertEquals("Congratulations!", orderItem.getGiftMessage());
        assertEquals("Handle with care", orderItem.getDisplayableComment());
    }

    @Test
    void testEqualsAndHashCode() {
        OrderItem item1 = new OrderItem("SKU-123", "item-1", 2, "Happy Birthday!", "Fragile");
        OrderItem item2 = new OrderItem("SKU-123", "item-1", 2, "Happy Birthday!", "Fragile");
        OrderItem item3 = new OrderItem("SKU-456", "item-2", 3, "Congratulations!", "Handle with care");

        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
        assertNotEquals(item1, item3);
        assertNotEquals(item1.hashCode(), item3.hashCode());
    }

    @Test
    void testEqualsWithNull() {
        OrderItem orderItem = new OrderItem("SKU-123", "item-1", 2, "Happy Birthday!", "Fragile");

        assertNotEquals(null, orderItem);
        assertEquals(orderItem, orderItem);
    }

    @Test
    void testEqualsWithDifferentClass() {
        OrderItem orderItem = new OrderItem("SKU-123", "item-1", 2, "Happy Birthday!", "Fragile");
        String notAnOrderItem = "not an order item";

        assertNotEquals(orderItem, notAnOrderItem);
    }

    @Test
    void testEqualsWithNullFields() {
        OrderItem item1 = new OrderItem(null, null, null, null, null);
        OrderItem item2 = new OrderItem(null, null, null, null, null);
        OrderItem item3 = new OrderItem("SKU-123", null, null, null, null);

        assertEquals(item1, item2);
        assertNotEquals(item1, item3);
    }

    @Test
    void testHashCodeConsistency() {
        OrderItem orderItem = new OrderItem("SKU-123", "item-1", 2, "Happy Birthday!", "Fragile");

        int hashCode1 = orderItem.hashCode();
        int hashCode2 = orderItem.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testConstructorWithNullValues() {
        OrderItem orderItem = new OrderItem(null, null, null, null, null);

        assertNotNull(orderItem);
        assertNull(orderItem.getSellerSku());
        assertNull(orderItem.getSellerFulfillmentOrderItemId());
        assertNull(orderItem.getQuantity());
        assertNull(orderItem.getGiftMessage());
        assertNull(orderItem.getDisplayableComment());
    }

    @Test
    void testWithZeroQuantity() {
        OrderItem orderItem = new OrderItem("SKU-123", "item-1", 0, null, null);

        assertEquals(Integer.valueOf(0), orderItem.getQuantity());
    }

    @Test
    void testWithNegativeQuantity() {
        OrderItem orderItem = new OrderItem("SKU-123", "item-1", -1, null, null);

        assertEquals(Integer.valueOf(-1), orderItem.getQuantity());
    }

    @Test
    void testWithEmptyStrings() {
        OrderItem orderItem = new OrderItem("", "", 1, "", "");

        assertEquals("", orderItem.getSellerSku());
        assertEquals("", orderItem.getSellerFulfillmentOrderItemId());
        assertEquals("", orderItem.getGiftMessage());
        assertEquals("", orderItem.getDisplayableComment());
    }
}