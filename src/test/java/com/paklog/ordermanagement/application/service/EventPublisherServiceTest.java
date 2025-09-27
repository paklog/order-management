package com.paklog.ordermanagement.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paklog.ordermanagement.domain.event.FulfillmentOrderReceivedEvent;
import com.paklog.ordermanagement.domain.model.Address;
import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.domain.model.OrderItem;
import com.paklog.ordermanagement.domain.model.OutboxEvent;
import com.paklog.ordermanagement.domain.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventPublisherServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<OutboxEvent> outboxEventCaptor;

    private EventPublisherService eventPublisherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventPublisherService = new EventPublisherService(outboxEventRepository, kafkaTemplate, objectMapper);
    }

    @Test
    void testPublishEvent() throws Exception {
        // Given
        FulfillmentOrder order = createTestOrder();
        FulfillmentOrderReceivedEvent event = new FulfillmentOrderReceivedEvent(order);
        
        // Mock ObjectMapper to return JSON string
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"orderId\":\"123\"}");
        
        // Mock the repository to return a saved event with an ID
        OutboxEvent mockSavedEvent = new OutboxEvent("test.event", "{\"test\": \"data\"}");
        mockSavedEvent.setId("test-id-123");
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenReturn(mockSavedEvent);

        // When
        eventPublisherService.publishEvent(event);

        // Then
        verify(outboxEventRepository).save(outboxEventCaptor.capture());
        OutboxEvent capturedEvent = outboxEventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals(event.getType(), capturedEvent.getEventType());
        assertFalse(capturedEvent.isPublished());
        assertNotNull(capturedEvent.getCreatedAt());
    }

    @Test
    void testPublishOutboxEvents() {
        // Given
        OutboxEvent unpublishedEvent = new OutboxEvent("test.event", "{\"test\": \"data\"}");
        unpublishedEvent.setId("1");
        unpublishedEvent.setPublished(false);

        List<OutboxEvent> unpublishedEvents = new ArrayList<>();
        unpublishedEvents.add(unpublishedEvent);

        when(outboxEventRepository.findByPublishedFalse()).thenReturn(unpublishedEvents);

        // When
        eventPublisherService.publishOutboxEvents();

        // Then
        verify(kafkaTemplate).send("fulfillment.order_management.v1.events", unpublishedEvent.getEventData());
        verify(outboxEventRepository).save(unpublishedEvent);
        assertTrue(unpublishedEvent.isPublished());
    }

    @Test
    void testPublishOutboxEvents_WithException() {
        // Given
        OutboxEvent unpublishedEvent = new OutboxEvent("test.event", "{\"test\": \"data\"}");
        unpublishedEvent.setId("1");
        unpublishedEvent.setPublished(false);

        List<OutboxEvent> unpublishedEvents = new ArrayList<>();
        unpublishedEvents.add(unpublishedEvent);

        when(outboxEventRepository.findByPublishedFalse()).thenReturn(unpublishedEvents);
        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate)
                .send("fulfillment.order_management.v1.events", unpublishedEvent.getEventData());

        // When
        eventPublisherService.publishOutboxEvents();

        // Then
        verify(kafkaTemplate).send("fulfillment.order_management.v1.events", unpublishedEvent.getEventData());
        verify(outboxEventRepository, never()).save(unpublishedEvent);
        assertFalse(unpublishedEvent.isPublished());
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