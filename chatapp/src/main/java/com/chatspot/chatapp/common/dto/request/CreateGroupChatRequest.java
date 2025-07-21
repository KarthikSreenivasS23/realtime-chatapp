package com.chatspot.chatapp.common.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateGroupChatRequest {
    private String name;
    private String description;
    private List<String> participantIds;
}