package com.chatspot.chatapp.common.dto.request;

import com.chatspot.chatapp.entity.message.ReactionType;
import lombok.Data;

@Data
public class AddReactionRequest {
    private ReactionType reactionType;
}