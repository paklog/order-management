package com.paklog.ordermanagement.domain.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "outbox")
public class OutboxEvent {
    @Id
    private String id;
    private String eventType;
    private String eventData;
    private LocalDateTime createdAt;
    private boolean published;

    public OutboxEvent() {
        this.createdAt = LocalDateTime.now();
        this.published = false;
    }

    public OutboxEvent(String eventType, String eventData) {
        this();
        this.eventType = eventType;
        this.eventData = eventData;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventData() {
        return eventData;
    }

    public void setEventData(String eventData) {
        this.eventData = eventData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }
}