package com.chatspot.chatapp.entity.message;

import com.chatspot.chatapp.entity.user.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDto {
    private Long id;
    private Long chatId;
    private UserResponseDto sender;
    private MessageType type;
    private String content;
    
    // Media fields (file metadata)
    private String fileName;
    private String filePath;
    private String fileUrl;
    private Long fileSize;
    private String mimeType;
    
    // Media data for API response
    private byte[][] mediaData; // Array of media files as byte arrays
    
    private MessageStatus status;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private List<MessageReactionDto> reactions;
    private List<MessageDeliveryDto> deliveryDtos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor without media data (for cases where we don't need to load media files)
    public MessageResponseDto(Long id, Long chatId, UserResponseDto sender, MessageType type, 
                             String content, String fileName, String filePath, String fileUrl, 
                             Long fileSize, String mimeType, MessageStatus status, Boolean isDeleted, 
                             LocalDateTime deletedAt, List<MessageReactionDto> reactions,
                             List<MessageDeliveryDto> deliveryDtos, LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
        this.id = id;
        this.chatId = chatId;
        this.sender = sender;
        this.type = type;
        this.content = content;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.status = status;
        this.isDeleted = isDeleted;
        this.deletedAt = deletedAt;
        this.reactions = reactions;
        this.deliveryDtos = deliveryDtos;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
