package com.chatspot.chatapp.kafka;

import com.chatspot.chatapp.entity.message.DeliveryStatus;
import com.chatspot.chatapp.entity.message.Message;
import com.chatspot.chatapp.entity.user.User;
import com.chatspot.chatapp.kafka.event.DeliveryStatusEvent;
import com.chatspot.chatapp.kafka.event.MessageEvent;
import com.chatspot.chatapp.kafka.event.ReactionEvent;
import com.chatspot.chatapp.repository.MessageRepository;
import com.chatspot.chatapp.repository.UserRepository;
import com.chatspot.chatapp.websocket.WebSocketMessageBroadcaster;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    
    private final WebSocketMessageBroadcaster webSocketBroadcaster;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    
    public KafkaConsumerService(WebSocketMessageBroadcaster webSocketBroadcaster,
                               MessageRepository messageRepository,
                               UserRepository userRepository) {
        this.webSocketBroadcaster = webSocketBroadcaster;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }
    
    @KafkaListener(topics = "chat-messages", groupId = "chatapp-group")
    public void consumeMessageEvent(MessageEvent event) {
        // Process message event and broadcast via WebSocket
        try {
            // Broadcast to chat participants via WebSocket
            webSocketBroadcaster.broadcastNewMessage(event);
            
            // Update delivery status for offline users
            updateDeliveryStatusForOfflineUsers(event);
            
        } catch (Exception e) {
            // Handle error and possibly retry
            System.err.println("Error processing message event: " + e.getMessage());
        }
    }
    
    @KafkaListener(topics = "delivery-status", groupId = "chatapp-group")
    public void consumeDeliveryStatusEvent(DeliveryStatusEvent event) {
        try {
            // Update delivery status in database
            updateMessageDeliveryStatus(event);
            
            // Broadcast delivery status to message sender
            webSocketBroadcaster.broadcastDeliveryStatus(event);
            
        } catch (Exception e) {
            System.err.println("Error processing delivery status event: " + e.getMessage());
        }
    }
    
    @KafkaListener(topics = "message-reactions", groupId = "chatapp-group")
    public void consumeReactionEvent(ReactionEvent event) {
        try {
            // Update reaction in database
            updateMessageReaction(event);
            
            // Broadcast reaction to chat participants
            webSocketBroadcaster.broadcastReaction(event);
            
        } catch (Exception e) {
            System.err.println("Error processing reaction event: " + e.getMessage());
        }
    }
    
    private void updateDeliveryStatusForOfflineUsers(MessageEvent event) {
        // Update delivery status for users who are not currently online
        event.getRecipients().forEach(userId -> {
            try {
                Message message = messageRepository.findByIdWithDeliveryStatus(event.getMessageId())
                    .orElseThrow(() -> new EntityNotFoundException("Message not found"));
                
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
                
                message.updateDeliveryStatus(user, DeliveryStatus.DELIVERED);
                messageRepository.save(message);
                
                // Publish delivery status event
                DeliveryStatusEvent statusEvent = new DeliveryStatusEvent();
                statusEvent.setMessageId(event.getMessageId());
                statusEvent.setUserId(userId);
                statusEvent.setStatus(DeliveryStatus.DELIVERED);
                
                // This would trigger another Kafka event
                
            } catch (Exception e) {
                System.err.println("Error updating delivery status: " + e.getMessage());
            }
        });
    }
    
    private void updateMessageDeliveryStatus(DeliveryStatusEvent event) {
        // Update delivery status in database
        Message message = messageRepository.findByIdWithDeliveryStatus(event.getMessageId())
            .orElseThrow(() -> new EntityNotFoundException("Message not found"));
        
        User user = userRepository.findById(event.getUserId())
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        message.updateDeliveryStatus(user, event.getStatus());
        messageRepository.save(message);
    }
    

    private void updateMessageReaction(ReactionEvent event) {
        Message message = messageRepository.findById(event.getMessageId())
            .orElseThrow(() -> new EntityNotFoundException("Message not found"));
        
        User user = userRepository.findById(event.getUserId())
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        if (event.isRemoved()) {
            message.removeReaction(user);
        } else {
            message.addReaction(user, event.getReactionType());
        }
        
        messageRepository.save(message);
    }

}