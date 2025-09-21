package com.paklog.ordermanagement.interfaces.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CancelFulfillmentOrderRequestTest {

    @Test
    void testDefaultConstructor() {
        CancelFulfillmentOrderRequest request = new CancelFulfillmentOrderRequest();

        assertNotNull(request);
        assertNull(request.getCancellationReason());
    }

    @Test
    void testParameterizedConstructor() {
        String cancellationReason = "Customer requested cancellation";
        CancelFulfillmentOrderRequest request = new CancelFulfillmentOrderRequest(cancellationReason);

        assertNotNull(request);
        assertEquals(cancellationReason, request.getCancellationReason());
    }

    @Test
    void testSettersAndGetters() {
        CancelFulfillmentOrderRequest request = new CancelFulfillmentOrderRequest();
        String cancellationReason = "Out of stock";

        request.setCancellationReason(cancellationReason);

        assertEquals(cancellationReason, request.getCancellationReason());
    }

    @Test
    void testWithNullReason() {
        CancelFulfillmentOrderRequest request = new CancelFulfillmentOrderRequest(null);

        assertNull(request.getCancellationReason());

        request.setCancellationReason(null);
        assertNull(request.getCancellationReason());
    }

    @Test
    void testWithEmptyReason() {
        CancelFulfillmentOrderRequest request = new CancelFulfillmentOrderRequest("");

        assertEquals("", request.getCancellationReason());

        request.setCancellationReason("");
        assertEquals("", request.getCancellationReason());
    }

    @Test
    void testWithLongReason() {
        StringBuilder longReason = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longReason.append("reason");
        }
        String cancellationReason = longReason.toString();

        CancelFulfillmentOrderRequest request = new CancelFulfillmentOrderRequest(cancellationReason);

        assertEquals(cancellationReason, request.getCancellationReason());
        assertEquals(6000, request.getCancellationReason().length());
    }

    @Test
    void testWithSpecialCharacters() {
        String cancellationReason = "Customer said: \"I don't want this anymore!\" @#$%^&*()";
        CancelFulfillmentOrderRequest request = new CancelFulfillmentOrderRequest(cancellationReason);

        assertEquals(cancellationReason, request.getCancellationReason());
    }

    @Test
    void testWithMultilineReason() {
        String cancellationReason = "Line 1\nLine 2\nLine 3";
        CancelFulfillmentOrderRequest request = new CancelFulfillmentOrderRequest(cancellationReason);

        assertEquals(cancellationReason, request.getCancellationReason());
        assertTrue(request.getCancellationReason().contains("\n"));
    }

    @Test
    void testReasonModification() {
        CancelFulfillmentOrderRequest request = new CancelFulfillmentOrderRequest("Initial reason");

        assertEquals("Initial reason", request.getCancellationReason());

        request.setCancellationReason("Modified reason");
        assertEquals("Modified reason", request.getCancellationReason());

        request.setCancellationReason("Final reason");
        assertEquals("Final reason", request.getCancellationReason());
    }

    @Test
    void testWithWhitespaceReason() {
        String cancellationReason = "   Reason with spaces   ";
        CancelFulfillmentOrderRequest request = new CancelFulfillmentOrderRequest(cancellationReason);

        assertEquals(cancellationReason, request.getCancellationReason());
        assertTrue(request.getCancellationReason().startsWith("   "));
        assertTrue(request.getCancellationReason().endsWith("   "));
    }

    @Test
    void testCommonCancellationReasons() {
        String[] commonReasons = {
            "Customer requested cancellation",
            "Out of stock",
            "Payment failed",
            "Shipping address invalid",
            "Duplicate order",
            "Item discontinued",
            "Customer changed mind"
        };

        for (String reason : commonReasons) {
            CancelFulfillmentOrderRequest request = new CancelFulfillmentOrderRequest(reason);
            assertEquals(reason, request.getCancellationReason());
        }
    }
}