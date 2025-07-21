package com.chatspot.chatapp.kafka;

import com.chatspot.chatapp.kafka.event.DeliveryStatusEvent;
import com.chatspot.chatapp.kafka.event.MessageEvent;
import com.chatspot.chatapp.kafka.event.ReactionEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void publishMessageEvent(MessageEvent event) {
        kafkaTemplate.send("chat-messages", event.getChatId().toString(), event);
    }
    
    public void publishDeliveryStatusEvent(DeliveryStatusEvent event) {
        kafkaTemplate.send("delivery-status", event.getMessageId().toString(), event);
    }
    
    public void publishReactionEvent(ReactionEvent event) {
        kafkaTemplate.send("message-reactions", event.getMessageId().toString(), event);
    }
}