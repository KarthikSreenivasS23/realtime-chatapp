package com.chatspot.chatapp.service;

import com.chatspot.chatapp.entity.chat.Chat;
import com.chatspot.chatapp.entity.chat.ChatType;
import com.chatspot.chatapp.entity.chat.ParticipantRole;
import com.chatspot.chatapp.entity.user.User;
import com.chatspot.chatapp.repository.ChatRepository;

import com.chatspot.chatapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {
    
    @Autowired
    private ChatRepository chatRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Page<Chat> getUserChats(String userId, Pageable pageable) {
        return chatRepository.findChatsByUserId(userId, pageable);
    }
    
    public Chat getChatById(Long chatId, String userId) {
        return chatRepository.findByIdAndUserId(chatId, userId)
            .orElseThrow(() -> new SecurityException("Access denied to chat"));
    }
    
    public Chat createIndividualChat(String userId1, String userId2) {
        // Check if chat already exists
        Optional<Chat> existingChat = chatRepository.findIndividualChatBetweenUsers(userId1, userId2);
        if (existingChat.isPresent()) {
            return existingChat.get();
        }
        
        User user1 = userRepository.findById(userId1)
            .orElseThrow(() -> new EntityNotFoundException("User1 not found"));
        User user2 = userRepository.findById(userId2)
            .orElseThrow(() -> new EntityNotFoundException("User2 not found"));
        
        Chat chat = new Chat();
        chat.setType(ChatType.INDIVIDUAL);
        chat.setCreatedBy(user1);
        chat.addParticipant(user1, ParticipantRole.MEMBER);
        chat.addParticipant(user2, ParticipantRole.MEMBER);
        
        return chatRepository.save(chat);
    }
    
    public Chat createGroupChat(String name,String groupIcon, String createdById, List<String> participantIds) {
        User createdBy = userRepository.findById(createdById)
            .orElseThrow(() -> new EntityNotFoundException("Creator not found"));
        
        List<User> participants = userRepository.findAllByIds(participantIds);
        
        Chat chat = new Chat();
        chat.setType(ChatType.GROUP);
        chat.setName(name);
        chat.setCreatedBy(createdBy);
        chat.addParticipant(createdBy, ParticipantRole.ADMIN);
        
        participants.forEach(participant -> {
            if (!participant.getId().equals(createdById)) {
                chat.addParticipant(participant, ParticipantRole.MEMBER);
            }
        });
        
        return chatRepository.save(chat);
    }
    
    public void addParticipant(Long chatId, String userId, String participantId) {
        Chat chat = getChatById(chatId, userId);
        
        if (chat.getType() == ChatType.INDIVIDUAL) {
            throw new IllegalArgumentException("Cannot add participants to individual chat");
        }
        
        // Check if user is admin
        boolean isAdmin = chat.getParticipants().stream()
            .anyMatch(p -> p.getUser().getId().equals(userId) && p.getRole() == ParticipantRole.ADMIN);
        
        if (!isAdmin) {
            throw new SecurityException("Only admins can add participants");
        }
        
        User participant = userRepository.findById(participantId)
            .orElseThrow(() -> new EntityNotFoundException("Participant not found"));
        
        chat.addParticipant(participant, ParticipantRole.MEMBER);
        chatRepository.save(chat);
    }
    
    public void removeParticipant(Long chatId, String userId, String participantId) {
        Chat chat = getChatById(chatId, userId);
        
        if (chat.getType() == ChatType.INDIVIDUAL) {
            throw new IllegalArgumentException("Cannot remove participants from individual chat");
        }
        
        // Check if user is admin or removing themselves
        boolean isAdmin = chat.getParticipants().stream()
            .anyMatch(p -> p.getUser().getId().equals(userId) && p.getRole() == ParticipantRole.ADMIN);
        
        if (!isAdmin && !userId.equals(participantId)) {
            throw new SecurityException("Only admins can remove other participants");
        }
        
        User participant = userRepository.findById(participantId)
            .orElseThrow(() -> new EntityNotFoundException("Participant not found"));
        
        chat.removeParticipant(participant);
        chatRepository.save(chat);
    }
}