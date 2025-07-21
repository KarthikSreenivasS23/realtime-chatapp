package com.chatspot.chatapp.entity.message;

import com.chatspot.chatapp.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class MessageDeliveryInfo {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status = DeliveryStatus.SENT;
    
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;

    public MessageDeliveryInfo(User user,DeliveryStatus status) {
        this.user = user;
        this.status = status;
    }
}