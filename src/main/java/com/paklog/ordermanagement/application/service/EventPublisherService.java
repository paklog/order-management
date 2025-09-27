package com.paklog.ordermanagement.application.service;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paklog.ordermanagement.domain.event.FulfillmentOrderEvent;
import com.paklog.ordermanagement.domain.model.OutboxEvent;
import com.paklog.ordermanagement.domain.repository.OutboxEventRepository;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonFormat;

@Service
public class EventPublisherService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventPublisherService.class);
    private static final String KAFKA_TOPIC = "fulfillment.order_management.v1.events";
    
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    public EventPublisherService(OutboxEventRepository outboxEventRepository, 
                                KafkaTemplate<String, String> kafkaTemplate,
                                ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        logger.info("EventPublisherService initialized with Kafka topic: {}", KAFKA_TOPIC);
    }
    
    public void publishEvent(FulfillmentOrderEvent event) {
        Instant startTime = Instant.now();
        logger.info("Publishing event to outbox - EventType: {}, EventId: {}", event.getType(), event.getId());
        
        try {
            // Convert event to CloudEvent format
            logger.debug("Converting to CloudEvent format - EventId: {}", event.getId());
            CloudEvent cloudEvent = convertToCloudEvent(event);
            
            // Serialize CloudEvent to JSON
            logger.debug("Serializing CloudEvent to JSON - EventId: {}", event.getId());
            JsonFormat jsonFormat = new JsonFormat();
            byte[] serializedEvent = jsonFormat.serialize(cloudEvent);
            String eventData = new String(serializedEvent);
            
            // Create outbox event
            logger.debug("Creating outbox event - EventId: {}, Size: {} bytes", event.getId(), eventData.length());
            OutboxEvent outboxEvent = new OutboxEvent(event.getType(), eventData);
            
            // Save to outbox
            logger.debug("Persisting event to outbox - EventId: {}", event.getId());
            OutboxEvent savedEvent = outboxEventRepository.save(outboxEvent);
            
            Duration duration = Duration.between(startTime, Instant.now());
            logger.info("Successfully published event to outbox - EventId: {}, OutboxId: {}, Duration: {}ms", 
                    event.getId(), savedEvent.getId(), duration.toMillis());
            
        } catch (Exception e) {
            Duration duration = Duration.between(startTime, Instant.now());
            logger.error("Failed to publish event to outbox - EventType: {}, EventId: {}, Error: {}, Duration: {}ms", 
                    event.getType(), event.getId(), e.getMessage(), duration.toMillis(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
    
    public void publishOutboxEvents() {
        Instant startTime = Instant.now();
        logger.debug("Starting outbox event publishing cycle");
        
        try {
            // Get unpublished events
            logger.debug("Retrieving unpublished events from outbox");
            List<OutboxEvent> unpublishedEvents = outboxEventRepository.findByPublishedFalse();
            
            if (unpublishedEvents.isEmpty()) {
                logger.debug("No unpublished events found in outbox");
                return;
            }
            
            logger.info("Processing {} unpublished events from outbox", unpublishedEvents.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            // Publish each event
            for (OutboxEvent outboxEvent : unpublishedEvents) {
                Instant eventStartTime = Instant.now();
                try {
                    logger.debug("Publishing event to Kafka - OutboxId: {}, EventType: {}", 
                            outboxEvent.getId(), outboxEvent.getEventType());
                    
                    // Publish to Kafka
                    kafkaTemplate.send(KAFKA_TOPIC, outboxEvent.getEventData());
                    
                    // Mark as published
                    outboxEvent.setPublished(true);
                    outboxEventRepository.save(outboxEvent);
                    
                    Duration eventDuration = Duration.between(eventStartTime, Instant.now());
                    logger.debug("Successfully published event to Kafka - OutboxId: {}, Duration: {}ms", 
                            outboxEvent.getId(), eventDuration.toMillis());
                    
                    successCount++;
                    
                } catch (Exception e) {
                    Duration eventDuration = Duration.between(eventStartTime, Instant.now());
                    logger.error("Failed to publish event to Kafka - OutboxId: {}, EventType: {}, Error: {}, Duration: {}ms", 
                            outboxEvent.getId(), outboxEvent.getEventType(), e.getMessage(), eventDuration.toMillis(), e);
                    failureCount++;
                }
            }
            
            Duration totalDuration = Duration.between(startTime, Instant.now());
            logger.info("Completed outbox event publishing cycle - Success: {}, Failures: {}, Duration: {}ms", 
                    successCount, failureCount, totalDuration.toMillis());
            
        } catch (Exception e) {
            Duration duration = Duration.between(startTime, Instant.now());
            logger.error("Failed to process outbox events - Error: {}, Duration: {}ms", 
                    e.getMessage(), duration.toMillis(), e);
        }
    }
    
    private CloudEvent convertToCloudEvent(FulfillmentOrderEvent event) throws JsonProcessingException {
        CloudEventBuilder builder = CloudEventBuilder.v1()
                .withId(event.getId())
                .withSource(URI.create(event.getSource()))
                .withType(event.getType())
                .withTime(event.getTime().atOffset(java.time.ZoneOffset.UTC))
                .withDataContentType("application/json");
        
        if (event.getSubject() != null) {
            builder.withSubject(event.getSubject());
        }
        
        if (event.getData() != null) {
            String jsonData = objectMapper.writeValueAsString(event.getData());
            builder.withData(jsonData.getBytes());
        }
        
        return builder.build();
    }
}