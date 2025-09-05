package com.paklog.ordermanagement.application.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventPublisherScheduler {
    
    private final EventPublisherService eventPublisherService;
    
    public OutboxEventPublisherScheduler(EventPublisherService eventPublisherService) {
        this.eventPublisherService = eventPublisherService;
    }
    
    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    public void publishOutboxEvents() {
        eventPublisherService.publishOutboxEvents();
    }
}