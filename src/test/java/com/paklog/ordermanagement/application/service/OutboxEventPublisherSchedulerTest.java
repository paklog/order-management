package com.paklog.ordermanagement.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

class OutboxEventPublisherSchedulerTest {

    @Mock
    private EventPublisherService eventPublisherService;

    private OutboxEventPublisherScheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduler = new OutboxEventPublisherScheduler(eventPublisherService);
    }

    @Test
    void testPublishOutboxEvents() {
        // When
        scheduler.publishOutboxEvents();

        // Then
        verify(eventPublisherService).publishOutboxEvents();
    }
}