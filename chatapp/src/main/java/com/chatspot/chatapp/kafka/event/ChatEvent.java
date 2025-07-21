package com.chatspot.chatapp.kafka.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = MessageEvent.class, name = "MESSAGE"),
    @JsonSubTypes.Type(value = DeliveryStatusEvent.class, name = "DELIVERY_STATUS"),
    @JsonSubTypes.Type(value = ReactionEvent.class, name = "REACTION")
})
public abstract class ChatEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    
    public ChatEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}