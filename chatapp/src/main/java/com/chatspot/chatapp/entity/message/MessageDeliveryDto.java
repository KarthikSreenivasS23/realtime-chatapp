package com.chatspot.chatapp.entity.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDeliveryDto {
    private String userId;
    private String userName;
    private DeliveryStatus status;
}
