package com.chatspot.chatapp.websocket;

import com.chatspot.chatapp.kafka.event.MessageEvent;
import com.chatspot.chatapp.kafka.event.ReactionEvent;
import com.chatspot.chatapp.kafka.event.DeliveryStatusEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketMessageBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastNewMessage(MessageEvent event) {
        try {
            // Broadcast to chat topic
            messagingTemplate.convertAndSend("/topic/chat/" + event.getChatId(), event);
            
            // Send to each recipient's personal queue
            if (event.getRecipients() != null) {
                event.getRecipients().forEach(recipientId -> {
                    messagingTemplate.convertAndSendToUser(recipientId, "/queue/messages", event);
                });
            }
            
            log.info("Broadcasted new message {} to chat {}", event.getMessageId(), event.getChatId());
        } catch (Exception e) {
            log.error("Error broadcasting new message: {}", e.getMessage());
        }
    }

    public void broadcastReaction(ReactionEvent event) {
        try {
            // Broadcast reaction to message topic
            messagingTemplate.convertAndSend("/topic/message/" + event.getMessageId() + "/reactions", event);
            
            log.info("Broadcasted reaction for message {}", event.getMessageId());
        } catch (Exception e) {
            log.error("Error broadcasting reaction: {}", e.getMessage());
        }
    }

    public void broadcastDeliveryStatus(DeliveryStatusEvent event) {
        try {
            // Send delivery status to message sender
            messagingTemplate.convertAndSend("/topic/message/" + event.getMessageId() + "/delivery", event);
            
            log.info("Broadcasted delivery status for message {}", event.getMessageId());
        } catch (Exception e) {
            log.error("Error broadcasting delivery status: {}", e.getMessage());
        }
    }

    public void broadcastTypingIndicator(String chatId, String userId, boolean isTyping) {
        try {
            Map<String, Object> typingEvent = Map.of(
                "userId", userId,
                "isTyping", isTyping,
                "timestamp", System.currentTimeMillis()
            );
            
            messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/typing", typingEvent);
        } catch (Exception e) {
            log.error("Error broadcasting typing indicator: {}", e.getMessage());
        }
    }

    public void broadcastUserPresence(String userId, String status) {
        try {
            Map<String, Object> presenceEvent = Map.of(
                "userId", userId,
                "status", status,
                "timestamp", System.currentTimeMillis()
            );
            
            messagingTemplate.convertAndSend("/topic/user/" + userId + "/presence", presenceEvent);
        } catch (Exception e) {
            log.error("Error broadcasting user presence: {}", e.getMessage());
        }
    }
}
