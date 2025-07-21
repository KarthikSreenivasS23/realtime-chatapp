package com.chatspot.chatapp.service;

import com.chatspot.chatapp.common.dto.response.ChatResponseDto;
import com.chatspot.chatapp.entity.chat.Chat;
import com.chatspot.chatapp.entity.chat.ChatParticipantInfo;
import com.chatspot.chatapp.entity.message.Message;
import com.chatspot.chatapp.entity.message.MessageDeliveryDto;
import com.chatspot.chatapp.entity.message.MessageReactionDto;
import com.chatspot.chatapp.entity.message.MessageResponseDto;
import com.chatspot.chatapp.entity.user.User;
import com.chatspot.chatapp.entity.user.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DtoMapperService {

    private final FileService fileService;

    public DtoMapperService(FileService fileService) {
        this.fileService = fileService;
    }

    public UserResponseDto toUserResponseDto(User user, boolean includeProfilePicture) {
        UserResponseDto dto = new UserResponseDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getLastSeen(),
            user.getCreatedDate(),
            user.getLastModifiedDate()
        );

        // Load profile picture data if requested and path exists
        System.err.println("profile picture for user path" + user.getProfilePicture());
        if (includeProfilePicture && user.getProfilePicture() != null) {
            try {
                byte[] profileData = fileService.loadFileAsBytes(user.getProfilePicture());
                dto.setProfilePictureData( Base64.getEncoder().encodeToString(profileData));
            } catch (IOException e) {
                // Log error but don't fail the entire operation
                System.err.println("Error loading profile picture for user " + user.getId() + ": " + e.getMessage());
            }
        }

        return dto;
    }

    public MessageResponseDto toMessageResponseDto(Message message, boolean includeMediaData) {
        // Convert sender to UserResponseDto (without profile picture to avoid nested loading)
        UserResponseDto senderDto = toUserResponseDto(message.getSender(), false);

        // Map delivery info to safe DTOs
        List<MessageDeliveryDto> deliveryDtos = message.getDeliveryStatus().stream()
                .map(info -> {
                    return new MessageDeliveryDto(info.getUser().getId(),info.getUser().getFirstName(),info.getStatus());
                })
                .collect(Collectors.toList());

        // Map reaction info to safe DTOs
        List<MessageReactionDto> reactionDtos = message.getReactions().stream()
                .map(reaction -> {
                    return new MessageReactionDto(reaction.getUser().getId(),reaction.getReactionType().name());
                })
                .collect(Collectors.toList());

        MessageResponseDto dto = new MessageResponseDto(
            message.getId(),
            message.getChat().getId(),
            senderDto,
            message.getType(),
            message.getContent(),
            message.getFileName(),
            message.getFilePath(),
            message.getFileUrl(),
            message.getFileSize(),
            message.getMimeType(),
            message.getStatus(),
            message.getIsDeleted(),
            message.getDeletedAt(),
            reactionDtos, deliveryDtos,
            message.getCreatedDate(),
            message.getLastModifiedDate()
        );


        // Load media data if requested and file path exists
        if (includeMediaData && message.getFilePath() != null) {
            try {
                byte[] mediaBytes = fileService.loadFileAsBytes(message.getFilePath());
                if (mediaBytes != null) {
                    // For now, we support single media file per message
                    // Can be extended to support multiple files
                    dto.setMediaData(new byte[][]{mediaBytes});
                }
            } catch (IOException e) {
                // Log error but don't fail the entire operation
                System.err.println("Error loading media for message " + message.getId() + ": " + e.getMessage());
            }
        }

        return dto;
    }

    public List<UserResponseDto> toUserResponseDtoList(List<User> users, boolean includeProfilePictures) {
        List<UserResponseDto> dtos = new ArrayList<>();
        for (User user : users) {
            dtos.add(toUserResponseDto(user, includeProfilePictures));
        }
        return dtos;
    }

        public ChatResponseDto toChatResponseDto(Chat chat) {
        List<User> users = getUsersFromParticipants(chat.getParticipants());
        List<UserResponseDto> participantDtos = toUserResponseDtoList(users, true);

        ChatResponseDto dto = new ChatResponseDto();
        dto.setId(chat.getId());
        dto.setName(chat.getName());
        dto.setDescription(chat.getDescription());
        dto.setType(chat.getType());
        dto.setParticipants(participantDtos);
        dto.setCreatedAt(chat.getCreatedDate());
        dto.setUpdatedAt(chat.getLastModifiedDate());

        return dto;
    }

    private List<User> getUsersFromParticipants(Set<ChatParticipantInfo> participants) {
        if (participants == null) {
            return new ArrayList<>();
        }
        return participants.stream()
                .map(ChatParticipantInfo::getUser)
                .collect(Collectors.toList());
    }

    public List<MessageResponseDto> toMessageResponseDtoList(List<Message> messages, boolean includeMediaData) {
        List<MessageResponseDto> dtos = new ArrayList<>();
        for (Message message : messages) {
            dtos.add(toMessageResponseDto(message, includeMediaData));
        }
        return dtos;
    }



    public Page<ChatResponseDto> toChatResponseDtoPage(Page<Chat> chatPage) {
        return chatPage.map(this::toChatResponseDto);
    }
}
