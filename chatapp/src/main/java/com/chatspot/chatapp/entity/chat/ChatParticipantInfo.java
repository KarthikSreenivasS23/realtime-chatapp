package com.chatspot.chatapp.entity.chat;

import com.chatspot.chatapp.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "chat")
@Entity
@Table(name = "chat_participants")
public class ChatParticipantInfo {

    @EmbeddedId
    private ChatParticipantId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("chatId")
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private ParticipantRole role = ParticipantRole.MEMBER;

    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private LocalDateTime lastReadAt;

    public ChatParticipantInfo(Chat chat, User user, ParticipantRole role) {
        this.id = new ChatParticipantId(chat.getId(), user.getId());
        this.chat = chat;
        this.user = user;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }
}