package com.chatspot.chatapp.kafka.event;

import com.chatspot.chatapp.entity.message.ReactionType;

public class ReactionEvent extends ChatEvent {
    private Long messageId;
    private String userId;
    private ReactionType reactionType;
    private boolean isRemoved;
    
    public ReactionEvent() {
        setEventType("REACTION");
    }
    
    // Getters and setters
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public ReactionType getReactionType() { return reactionType; }
    public void setReactionType(ReactionType reactionType) { this.reactionType = reactionType; }
    
    public boolean isRemoved() { return isRemoved; }
    public void setRemoved(boolean removed) { isRemoved = removed; }
}