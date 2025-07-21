package com.chatspot.chatapp.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    
    @Value("${kafka.chat.topic.name}")
    private String chatTopicName;
    @Value("${kafka.delivery.topic.name}")
    private String deliveryTopicName;
    @Value("${kafka.message.topic.name}")
    private String messageTopicName;

    
    // Topic Configuration
    @Bean
    public NewTopic chatMessageTopic() {
        return TopicBuilder.name(chatTopicName)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic deliveryStatusTopic() {
        return TopicBuilder.name(deliveryTopicName)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic reactionTopic() {
        return TopicBuilder.name(messageTopicName)
                .partitions(3)
                .replicas(1)
                .build();
    }
}