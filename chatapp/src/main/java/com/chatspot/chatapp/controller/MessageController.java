package com.chatspot.chatapp.controller;

import com.chatspot.chatapp.common.dto.request.AddReactionRequest;
import com.chatspot.chatapp.entity.message.Message;
import com.chatspot.chatapp.entity.message.MessageType;
import com.chatspot.chatapp.entity.message.MessageResponseDto;
import com.chatspot.chatapp.service.DtoMapperService;
import com.chatspot.chatapp.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Message Management", description = "Endpoints for sending, retrieving, and interacting with messages.")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DtoMapperService dtoMapperService;

    @GetMapping("/chat/{chatId}")
    @Operation(summary = "Get messages for a chat", description = "Retrieves a paginated list of messages for a specific chat. The user must be a member of the chat.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved messages.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not a member of this chat.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chat not found.", content = @Content)
    })
    public ResponseEntity<Page<MessageResponseDto>> getChatMessages(
            @Parameter(description = "The unique ID of the chat.", required = true) @PathVariable Long chatId,
            Authentication authentication,
            @Parameter(description = "The page number to retrieve.") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "The number of messages per page.") @RequestParam(defaultValue = "20") int size) {
        String principalName = authentication.getName();
        logger.info("Request received for GET /api/messages/chat/{} by principal: {}. Page: {}, Size: {}", chatId, principalName, page, size);
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Message> messages = messageService.getChatMessages(chatId, principalName, pageable);
            List<MessageResponseDto> dtos = dtoMapperService.toMessageResponseDtoList(messages.getContent(), true);
            Page<MessageResponseDto> dtoPage = new PageImpl<>(dtos, pageable, messages.getTotalElements());
            logger.info("Successfully retrieved {} messages for chat ID: {}", dtoPage.getTotalElements(), chatId);
            return ResponseEntity.ok(dtoPage);
        } catch (Exception e) {
            logger.error("Error retrieving messages for chat ID: {} for principal: {}", chatId, principalName, e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping(value = "/chat/{chatId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Send a message to a chat", description = "Sends a new message to a chat. The message can be plain text, a media file, or both.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully sent message.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Message content is empty or invalid.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not a member of this chat.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chat not found.", content = @Content)
    })
    public ResponseEntity<MessageResponseDto> sendMessage(
            @Parameter(description = "The unique ID of the chat.", required = true) @PathVariable Long chatId,
            @Parameter(description = "The type of the message (TEXT, IMAGE, VIDEO, FILE).", required = true) @RequestParam("type") MessageType type,
            @Parameter(description = "The text content of the message.") @RequestParam(value = "text", required = false) String text,
            @Parameter(description = "The media file to be uploaded.") @RequestParam(value = "media", required = false) MultipartFile media,
            Authentication authentication) {
        String principalName = authentication.getName();
        logger.info("Request received for POST /api/messages/chat/{} by principal: {} with type: {}", chatId, principalName, type);
        if ((text == null || text.isEmpty()) && (media == null || media.isEmpty())) {
            logger.warn("Send message attempt failed in chat {}: both text and media are empty.", chatId);
            return ResponseEntity.badRequest().build();
        }
        try {
            Message message = messageService.sendMessage(chatId, principalName, text, media);
            MessageResponseDto dto = dtoMapperService.toMessageResponseDto(message, true);
            logger.info("Successfully sent message ID: {} to chat ID: {}", message.getId(), chatId);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Error sending message in chat ID: {} by principal: {}", chatId, principalName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{messageId}/reactions")
    @Operation(summary = "Add a reaction to a message", description = "Adds a reaction (e.g., 'like', 'love') to a specific message. Users can only have one reaction per message.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully added reaction.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Message not found.", content = @Content)
    })
    public ResponseEntity<MessageResponseDto> addReaction(
            @Parameter(description = "The unique ID of the message.", required = true) @PathVariable Long messageId,
            @RequestBody AddReactionRequest request,
            Authentication authentication) {
        String principalName = authentication.getName();
        logger.info("Request received to add reaction '{}' to message: {} by principal: {}", request.getReactionType(), messageId, principalName);
        try {
            Message message = messageService.addReaction(messageId, principalName, request.getReactionType());
            MessageResponseDto dto = dtoMapperService.toMessageResponseDto(message, false);
            logger.info("Successfully added reaction to message: {}", messageId);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Error adding reaction to message: {} by principal: {}", messageId, principalName, e);
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{messageId}/reactions")
    @Operation(summary = "Remove a reaction from a message", description = "Removes the authenticated user's reaction from a specific message.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully removed reaction.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Message or reaction not found.", content = @Content)
    })
    public ResponseEntity<MessageResponseDto> removeReaction(
            @Parameter(description = "The unique ID of the message.", required = true) @PathVariable Long messageId,
            Authentication authentication) {
        String principalName = authentication.getName();
        logger.info("Request received to remove reaction from message: {} by principal: {}", messageId, principalName);
        try {
            Message message = messageService.removeReaction(messageId, principalName);
            MessageResponseDto dto = dtoMapperService.toMessageResponseDto(message, false);
            logger.info("Successfully removed reaction from message: {}", messageId);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Error removing reaction from message: {} by principal: {}", messageId, principalName, e);
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{messageId}/read")
    @Operation(summary = "Mark a message as read", description = "Marks a specific message as read by the authenticated user. This is typically used for tracking read receipts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully marked message as read.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Message not found.", content = @Content)
    })
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "The unique ID of the message.", required = true) @PathVariable Long messageId,
            Authentication authentication) {
        String principalName = authentication.getName();
        logger.info("Request received to mark message: {} as read by principal: {}", messageId, principalName);
        try {
            messageService.markAsRead(messageId, principalName);
            logger.info("Successfully marked message: {} as read.", messageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error marking message: {} as read by principal: {}", messageId, principalName, e);
            return ResponseEntity.status(500).build();
        }
    }
}