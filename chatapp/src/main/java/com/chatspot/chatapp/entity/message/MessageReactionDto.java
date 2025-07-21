package com.chatspot.chatapp.entity.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageReactionDto {
    private String userId;
    private String emoji;
}
