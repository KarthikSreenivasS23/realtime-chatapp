package com.chatspot.chatapp.websocket;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketMessageController {

    private final SimpMessagingTemplate messagingTemplate;


    @MessageMapping("/chat.typing")
    public void handleTypingIndicator(@Payload Map<String, Object> message, Principal principal) {
        try {
            String chatId = (String) message.get("chatId");
            String userId = principal.getName();
            
            // Broadcast typing indicator to chat participants
            messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/typing", 
                Map.of("userId", userId, "isTyping", message.get("isTyping")));
                
        } catch (Exception e) {
            log.error("Error handling typing indicator: {}", e.getMessage());
        }
    }

    @MessageMapping("/chat.presence")
    public void handlePresenceUpdate(@Payload Map<String, Object> message, Principal principal) {
        try {
            String userId = principal.getName();
            String status = (String) message.get("status");
            
            // Broadcast presence update
            messagingTemplate.convertAndSend("/topic/user/" + userId + "/presence", 
                Map.of("userId", userId, "status", status));
                
        } catch (Exception e) {
            log.error("Error handling presence update: {}", e.getMessage());
        }
    }

    @MessageMapping("/chat.join")
    public void handleChatJoin(@Payload Map<String, Object> message, Principal principal) {
        try {
            String chatId = (String) message.get("chatId");
            String userId = principal.getName();
            
            // Notify chat participants that user joined
            messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/events", 
                Map.of("type", "USER_JOINED", "userId", userId, "chatId", chatId));
                
        } catch (Exception e) {
            log.error("Error handling chat join: {}", e.getMessage());
        }
    }
}
