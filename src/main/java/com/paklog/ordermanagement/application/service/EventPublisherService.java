package com.paklog.ordermanagement.application.service;

import java.net.URI;
import java.util.List;

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
    
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    public EventPublisherService(OutboxEventRepository outboxEventRepository, 
                                KafkaTemplate<String, String> kafkaTemplate,
                                ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    public void publishEvent(FulfillmentOrderEvent event) {
        try {
            // Convert event to CloudEvent format
            CloudEvent cloudEvent = convertToCloudEvent(event);
            
            // Serialize CloudEvent to JSON
            JsonFormat jsonFormat = new JsonFormat();
            byte[] serializedEvent = jsonFormat.serialize(cloudEvent);
            String eventData = new String(serializedEvent);
            
            // Create outbox event
            OutboxEvent outboxEvent = new OutboxEvent(event.getType(), eventData);
            
            // Save to outbox
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event", e);
        }
    }
    
    public void publishOutboxEvents() {
        // Get unpublished events
        List<OutboxEvent> unpublishedEvents = outboxEventRepository.findByPublishedFalse();
        
        // Publish each event
        for (OutboxEvent outboxEvent : unpublishedEvents) {
            try {
                // Publish to Kafka
                kafkaTemplate.send("fulfillment.order_management.v1.events", outboxEvent.getEventData());
                
                // Mark as published
                outboxEvent.setPublished(true);
                outboxEventRepository.save(outboxEvent);
            } catch (Exception e) {
                // Log error but continue with other events
                System.err.println("Failed to publish event: " + e.getMessage());
            }
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