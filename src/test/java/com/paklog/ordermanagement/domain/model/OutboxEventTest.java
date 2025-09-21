package com.paklog.ordermanagement.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OutboxEventTest {

    @Test
    void testDefaultConstructor() {
        LocalDateTime before = LocalDateTime.now();
        OutboxEvent event = new OutboxEvent();
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(event);
        assertNull(event.getId());
        assertNull(event.getEventType());
        assertNull(event.getEventData());
        assertNotNull(event.getCreatedAt());
        assertTrue(event.getCreatedAt().isAfter(before.minusSeconds(1)));
        assertTrue(event.getCreatedAt().isBefore(after.plusSeconds(1)));
        assertFalse(event.isPublished());
    }

    @Test
    void testParameterizedConstructor() {
        String eventType = "order.created";
        String eventData = "{\"orderId\": \"123\"}";

        LocalDateTime before = LocalDateTime.now();
        OutboxEvent event = new OutboxEvent(eventType, eventData);
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(event);
        assertNull(event.getId());
        assertEquals(eventType, event.getEventType());
        assertEquals(eventData, event.getEventData());
        assertNotNull(event.getCreatedAt());
        assertTrue(event.getCreatedAt().isAfter(before.minusSeconds(1)));
        assertTrue(event.getCreatedAt().isBefore(after.plusSeconds(1)));
        assertFalse(event.isPublished());
    }

    @Test
    void testSettersAndGetters() {
        OutboxEvent event = new OutboxEvent();
        LocalDateTime testTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0);

        event.setId("test-id");
        event.setEventType("order.updated");
        event.setEventData("{\"orderId\": \"456\"}");
        event.setCreatedAt(testTime);
        event.setPublished(true);

        assertEquals("test-id", event.getId());
        assertEquals("order.updated", event.getEventType());
        assertEquals("{\"orderId\": \"456\"}", event.getEventData());
        assertEquals(testTime, event.getCreatedAt());
        assertTrue(event.isPublished());
    }

    @Test
    void testPublishedToggle() {
        OutboxEvent event = new OutboxEvent();

        assertFalse(event.isPublished());

        event.setPublished(true);
        assertTrue(event.isPublished());

        event.setPublished(false);
        assertFalse(event.isPublished());
    }

    @Test
    void testConstructorWithNullValues() {
        OutboxEvent event = new OutboxEvent(null, null);

        assertNotNull(event);
        assertNull(event.getId());
        assertNull(event.getEventType());
        assertNull(event.getEventData());
        assertNotNull(event.getCreatedAt());
        assertFalse(event.isPublished());
    }

    @Test
    void testConstructorWithEmptyStrings() {
        OutboxEvent event = new OutboxEvent("", "");

        assertEquals("", event.getEventType());
        assertEquals("", event.getEventData());
        assertFalse(event.isPublished());
    }

    @Test
    void testCreatedAtIsSetInDefaultConstructor() {
        OutboxEvent event1 = new OutboxEvent();

        // Small delay to ensure different timestamps
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        OutboxEvent event2 = new OutboxEvent();

        assertNotNull(event1.getCreatedAt());
        assertNotNull(event2.getCreatedAt());
        assertTrue(event2.getCreatedAt().isAfter(event1.getCreatedAt()) ||
                  event2.getCreatedAt().isEqual(event1.getCreatedAt()));
    }

    @Test
    void testIdCanBeNull() {
        OutboxEvent event = new OutboxEvent("test.event", "test data");

        assertNull(event.getId());

        event.setId("new-id");
        assertEquals("new-id", event.getId());

        event.setId(null);
        assertNull(event.getId());
    }

    @Test
    void testLongEventData() {
        StringBuilder longData = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longData.append("data");
        }
        String eventData = longData.toString();

        OutboxEvent event = new OutboxEvent("test.event", eventData);

        assertEquals(eventData, event.getEventData());
        assertEquals(4000, event.getEventData().length());
    }

    @Test
    void testSpecialCharactersInEventData() {
        String eventData = "{\"message\": \"Hello, World! @#$%^&*()_+-=[]{}|;':,.<>?\"}";
        OutboxEvent event = new OutboxEvent("test.event", eventData);

        assertEquals(eventData, event.getEventData());
    }
}