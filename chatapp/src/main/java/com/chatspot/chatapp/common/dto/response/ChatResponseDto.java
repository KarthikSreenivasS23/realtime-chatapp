package com.chatspot.chatapp.common.dto.response;

import com.chatspot.chatapp.entity.chat.ChatType;
import com.chatspot.chatapp.entity.user.UserResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public class ChatResponseDto {
    private Long id;
    private String name;
    private String description;
    private ChatType type;
    private List<UserResponseDto> participants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ChatType getType() {
        return type;
    }

    public void setType(ChatType type) {
        this.type = type;
    }

    public List<UserResponseDto> getParticipants() {
        return participants;
    }

    public void setParticipants(List<UserResponseDto> participants) {
        this.participants = participants;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
