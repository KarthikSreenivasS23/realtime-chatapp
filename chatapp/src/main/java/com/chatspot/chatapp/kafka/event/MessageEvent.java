package com.chatspot.chatapp.kafka.event;

import com.chatspot.chatapp.entity.message.MessageType;

import java.util.List;

public class MessageEvent extends ChatEvent {
    private Long messageId;
    private Long chatId;
    private String senderId;
    private String content;
    private MessageType messageType;
    private List<String> recipients;
    private Long replyToMessageId;
    
    public MessageEvent() {
        setEventType("MESSAGE");
    }
    
    // Getters and setters
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    
    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
    
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }
    
    public List<String> getRecipients() { return recipients; }
    public void setRecipients(List<String> recipients) { this.recipients = recipients; }
    
    public Long getReplyToMessageId() { return replyToMessageId; }
    public void setReplyToMessageId(Long replyToMessageId) { this.replyToMessageId = replyToMessageId; }
}
