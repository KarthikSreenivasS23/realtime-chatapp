package com.chatspot.chatapp;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ChatappApplication {
    @Value("${KAFKA_BOOTSTRAP_SERVERS:NOT SET}")
    private String kafkaServers;

    public static void main(String[] args) {
        SpringApplication.run(ChatappApplication.class, args);
    }

    @PostConstruct
    public void init() {
        System.out.println("Kafka servers used by Spring: " + kafkaServers);
    }

}
