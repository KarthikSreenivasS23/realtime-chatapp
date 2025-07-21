package com.chatspot.chatapp.entity.chat;

import com.chatspot.chatapp.common.BaseAuditing;
import com.chatspot.chatapp.entity.message.Message;
import com.chatspot.chatapp.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "chats")
@Data

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = "participants")
public class Chat extends BaseAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private ChatType type;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    
    private Set<ChatParticipantInfo> participants = new HashSet<>();

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

        public void addParticipant(User user, ParticipantRole role) {
        ChatParticipantInfo participantInfo = new ChatParticipantInfo(this, user, role);
        participants.add(participantInfo);
    }

    public void removeParticipant(User user) {
        participants.removeIf(p -> p.getUser().getId().equals(user.getId()));
    }

    public boolean isParticipant(User user) {
        return participants.stream()
                .anyMatch(p -> p.getUser().getId().equals(user.getId()));
    }
}
