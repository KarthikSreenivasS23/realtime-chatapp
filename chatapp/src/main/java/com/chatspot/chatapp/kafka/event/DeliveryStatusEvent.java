package com.chatspot.chatapp.kafka.event;

import com.chatspot.chatapp.entity.message.DeliveryStatus;

public class DeliveryStatusEvent extends ChatEvent {
    private Long messageId;
    private String userId;
    private DeliveryStatus status;
    
    public DeliveryStatusEvent() {
        setEventType("DELIVERY_STATUS");
    }
    
    // Getters and setters
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
}
