package com.chatspot.chatapp.service;

import com.chatspot.chatapp.entity.chat.Chat;
import com.chatspot.chatapp.entity.message.DeliveryStatus;
import com.chatspot.chatapp.entity.message.Message;
import com.chatspot.chatapp.entity.message.MessageType;
import com.chatspot.chatapp.entity.message.ReactionType;
import com.chatspot.chatapp.entity.user.User;

import com.chatspot.chatapp.kafka.KafkaProducerService;
import com.chatspot.chatapp.kafka.event.DeliveryStatusEvent;
import com.chatspot.chatapp.kafka.event.MessageEvent;
import com.chatspot.chatapp.kafka.event.ReactionEvent;
import com.chatspot.chatapp.repository.MessageRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private FileService fileService;

    
    public Page<Message> getChatMessages(Long chatId, String userId, Pageable pageable) {
        // Verify user has access to chat
        chatService.getChatById(chatId, userId);
        return messageRepository.findMessagesByChatId(chatId, pageable);
    }
    
    public Message sendMessage(Long chatId, String senderId, String content, MultipartFile file) throws IOException {
        Chat chat = chatService.getChatById(chatId, senderId);
        User sender = userService.getUserById(senderId);

        // First, save the message to get an ID
        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);

        // Now, create the message content (text and/or media)
        createMessageContent(message, content, file);

        // Initialize delivery status for all participants
        List<String> recipientIds = chat.getParticipants().stream()
                .map(p -> p.getUser().getId())
                .filter(id -> !id.equals(senderId))
                .collect(Collectors.toList());

        recipientIds.forEach(recipientId -> {
            User participant = userService.getUserById(recipientId);
            message.updateDeliveryStatus(participant, DeliveryStatus.SENT);
        });

        // Save the final message with all properties
        final Message savedMessage = messageRepository.save(message);

        // Publish Kafka event for real-time delivery
        MessageEvent event = new MessageEvent();
        event.setMessageId(savedMessage.getId());
        event.setChatId(chatId);
        event.setSenderId(senderId);
        event.setContent(savedMessage.getContent());
        event.setMessageType(savedMessage.getType());
        event.setRecipients(recipientIds);

        kafkaProducerService.publishMessageEvent(event);

        return savedMessage;
    }

    private void createMessageContent(Message message, String content, MultipartFile file) throws IOException {
        setMessageTextProperties(message, content);
        setMesssageMediaProperties(message, file);
        // Default to TEXT if no content is set
        if (message.getType() == null) {
            message.setType(MessageType.TEXT);
        }
    }
    private void setMessageTextProperties(Message message, String content) {
        if(content!=null && !content.isEmpty()) {
            message.setContent(content);
            message.setType(message.getType()==null?MessageType.TEXT:MessageType.MULTIMODAL);
        }
    }

    private void setMesssageMediaProperties(Message message, MultipartFile file) throws IOException {
        if(file!=null) {
            message.setFileName(file.getOriginalFilename());
            message.setFilePath(fileService.saveMediaFile(file, message.getId() != null ? message.getId().toString() : "temp"));
            message.setFileSize(file.getSize());
            message.setMimeType(file.getContentType());
            message.setType(message.getType() == null ? MessageType.FILE : MessageType.MULTIMODAL);
        }
    }
    
    public Message addReaction(Long messageId, String userId, ReactionType reactionType) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        // Authorize: check if user is in the chat
        chatService.getChatById(message.getChat().getId(), userId);

        User user = userService.getUserById(userId);

        message.addReaction(user, reactionType);
        Message savedMessage =  messageRepository.save(message);

        // Publish Kafka reaction event
        ReactionEvent event = new ReactionEvent();
        event.setMessageId(messageId);
        event.setUserId(userId);
        event.setReactionType(reactionType);
        event.setRemoved(false);

        kafkaProducerService.publishReactionEvent(event);

        return savedMessage;
    }
    
    public Message removeReaction(Long messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        // Authorize: check if user is in the chat
        chatService.getChatById(message.getChat().getId(), userId);

        User user = userService.getUserById(userId);

        message.removeReaction(user);

        Message savedMessage = messageRepository.save(message);

        // Publish Kafka reaction removal event
        ReactionEvent event = new ReactionEvent();
        event.setMessageId(messageId);
        event.setUserId(userId);
        event.setRemoved(true);

        kafkaProducerService.publishReactionEvent(event);

        return savedMessage;
    }
    
    public void markAsRead(Long messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        // Authorize: check if user is in the chat
        chatService.getChatById(message.getChat().getId(), userId);

        User user = userService.getUserById(userId);

        message.updateDeliveryStatus(user, DeliveryStatus.READ);
        messageRepository.save(message);

        // Publish Kafka delivery status event
        DeliveryStatusEvent event = new DeliveryStatusEvent();
        event.setMessageId(messageId);
        event.setUserId(userId);
        event.setStatus(DeliveryStatus.READ);

        kafkaProducerService.publishDeliveryStatusEvent(event);
    }

}