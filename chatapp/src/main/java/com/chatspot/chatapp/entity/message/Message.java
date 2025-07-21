package com.chatspot.chatapp.entity.message;

import com.chatspot.chatapp.common.BaseAuditing;
import com.chatspot.chatapp.entity.chat.Chat;
import com.chatspot.chatapp.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "messages")
public class Message extends BaseAuditing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Column(columnDefinition = "TEXT")
    private String content;

    // Media fields
    private String fileName;
    private String filePath;
    private String fileUrl;
    private Long fileSize;
    private String mimeType;

    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.SENT;

    private Boolean isDeleted = false;

    private LocalDateTime deletedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "message_reactions",
            joinColumns = @JoinColumn(name = "message_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"message_id", "user_id"})
    )
    private List<MessageReaction> reactions = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "message_delivery_status",
            joinColumns = @JoinColumn(name = "message_id")
    )
    private List<MessageDeliveryInfo> deliveryStatus = new ArrayList<>();


    public void addReaction(User user, ReactionType reactionType) {
        // Remove existing and update new reaction for the same user
        removeReaction(user);
        reactions.add(new MessageReaction(user, reactionType));
    }

    public void removeReaction(User user) {
        reactions.removeIf(r -> r.getUser().getId().equals(user.getId()));
    }


    public void updateDeliveryStatus(User user, DeliveryStatus status) {
        MessageDeliveryInfo info = deliveryStatus.stream()
                .filter(d -> d.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElse(null);

        if (info == null) {
            info = new MessageDeliveryInfo(user, status);
            deliveryStatus.add(info);
        } else {
            info.setStatus(status);
            if (status == DeliveryStatus.DELIVERED) {
                info.setDeliveredAt(LocalDateTime.now());
            } else if (status == DeliveryStatus.READ) {
                info.setReadAt(LocalDateTime.now());
            }
        }
    }

}
