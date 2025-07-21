package com.chatspot.chatapp.controller;

import com.chatspot.chatapp.common.dto.request.AddParticipantRequest;
import com.chatspot.chatapp.common.dto.request.CreateGroupChatRequest;
import com.chatspot.chatapp.common.dto.request.CreateIndividualChatRequest;
import com.chatspot.chatapp.common.dto.response.ChatResponseDto;
import com.chatspot.chatapp.entity.chat.Chat;
import com.chatspot.chatapp.service.ChatService;
import com.chatspot.chatapp.service.DtoMapperService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
@Tag(name = "Chat Management", description = "Endpoints for creating, retrieving, and managing chats.")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private DtoMapperService dtoMapperService;

    @GetMapping
    @Operation(summary = "Get all chats for the current user", description = "Retrieves a paginated list of all chats (private and group) that the currently authenticated user is a member of.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved chats.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class, subTypes = {ChatResponseDto.class}))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token.", content = @Content)
    })
    public ResponseEntity<Page<ChatResponseDto>> getChats(
            Authentication authentication,
            @Parameter(description = "The page number to retrieve.") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "The number of chats per page.") @RequestParam(defaultValue = "20") int size) {
        String principalName = authentication.getName();
        logger.info("Request received for GET /api/chats by principal: {}. Page: {}, Size: {}", principalName, page, size);
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Chat> chats = chatService.getUserChats(principalName, pageable);
            Page<ChatResponseDto> chatDtos = dtoMapperService.toChatResponseDtoPage(chats);
            logger.info("Successfully retrieved {} chats for principal: {}", chats.getTotalElements(), principalName);
            return ResponseEntity.ok(chatDtos);
        } catch (Exception e) {
            logger.error("Error retrieving chats for principal: {}", principalName, e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{chatId}")
    @Operation(summary = "Get a specific chat by its ID", description = "Retrieves the details of a single chat, provided the authenticated user is a member.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved chat.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not a member of this chat.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chat not found.", content = @Content)
    })
    public ResponseEntity<ChatResponseDto> getChat(
            @Parameter(description = "The unique ID of the chat.", required = true) @PathVariable Long chatId,
            Authentication authentication) {
        String principalName = authentication.getName();
        logger.info("Request received for GET /api/chats/{} by principal: {}", chatId, principalName);
        try {
            Chat chat = chatService.getChatById(chatId, principalName);
            ChatResponseDto dto = dtoMapperService.toChatResponseDto(chat);
            logger.info("Successfully retrieved chat ID: {} for principal: {}", chatId, principalName);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            logger.warn("Failed to retrieve chat ID: {}. Error: {}", chatId, e.getMessage());
            // This could be a 403 or 404 depending on the service layer's exception type
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/individual")
    @Operation(summary = "Create a new private chat", description = "Creates a new one-on-one chat with another user. If a chat already exists, it returns the existing one.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created or retrieved private chat.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Target user not found.", content = @Content)
    })
    public ResponseEntity<ChatResponseDto> createIndividualChat(
            @RequestBody CreateIndividualChatRequest request,
            Authentication authentication) {
        String principalName = authentication.getName();
        logger.info("Request received for POST /api/chats/individual from principal: {} to user: {}", principalName, request.getParticipantId());
        try {
            Chat chat = chatService.createIndividualChat(principalName, request.getParticipantId());
            ChatResponseDto dto = dtoMapperService.toChatResponseDto(chat);
            logger.info("Successfully created/retrieved private chat with ID: {} for users: {}, {}", chat.getId(), principalName, request.getParticipantId());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Error creating private chat between principal: {} and user: {}", principalName, request.getParticipantId(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/group")
    @Operation(summary = "Create a new group chat", description = "Creates a new group chat with a name, description, and a list of initial participants.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created group chat.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token.", content = @Content)
    })
    public ResponseEntity<ChatResponseDto> createGroupChat(
            @RequestBody CreateGroupChatRequest request,
            Authentication authentication) {
        String principalName = authentication.getName();
        logger.info("Request received for POST /api/chats/group by principal: {} with name: '{}'", principalName, request.getName());
        try {
            Chat chat = chatService.createGroupChat(request.getName(), request.getDescription(),
                    principalName, request.getParticipantIds());
            ChatResponseDto dto = dtoMapperService.toChatResponseDto(chat);
            logger.info("Successfully created group chat with ID: {} and name: '{}'", chat.getId(), chat.getName());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Error creating group chat with name: '{}' by principal: {}", request.getName(), principalName, e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/{chatId}/participants")
    @Operation(summary = "Add a participant to a group chat", description = "Adds a new user to an existing group chat. The authenticated user must be a member of the chat.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully added participant.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not a member of this chat.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chat or target user not found.", content = @Content)
    })
    public ResponseEntity<Void> addParticipant(
            @Parameter(description = "The unique ID of the group chat.", required = true) @PathVariable Long chatId,
            @RequestBody AddParticipantRequest request,
            Authentication authentication) {
        String principalName = authentication.getName();
        logger.info("Request received to add participant: {} to chat: {} by principal: {}", request.getParticipantId(), chatId, principalName);
        try {
            chatService.addParticipant(chatId, principalName, request.getParticipantId());
            logger.info("Successfully added participant: {} to chat: {}", request.getParticipantId(), chatId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            logger.error("Error adding participant: {} to chat: {} by principal: {}", request.getParticipantId(), chatId, principalName, e);
            return ResponseEntity.notFound().build(); // Or a more specific error
        }
    }

    @DeleteMapping("/{chatId}/participants/{participantId}")
    @Operation(summary = "Remove a participant from a group chat", description = "Removes a user from a group chat. The authenticated user must be the one performing the action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully removed participant.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not allowed to remove this participant.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chat or participant not found.", content = @Content)
    })
    public ResponseEntity<Void> removeParticipant(
            @Parameter(description = "The unique ID of the group chat.", required = true) @PathVariable Long chatId,
            @Parameter(description = "The unique ID of the participant to remove.", required = true) @PathVariable String participantId,
            Authentication authentication) {
        String principalName = authentication.getName();
        logger.info("Request received to remove participant: {} from chat: {} by principal: {}", participantId, chatId, principalName);
        try {
            chatService.removeParticipant(chatId, principalName, participantId);
            logger.info("Successfully removed participant: {} from chat: {}", participantId, chatId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            logger.error("Error removing participant: {} from chat: {} by principal: {}", participantId, chatId, principalName, e);
            return ResponseEntity.notFound().build(); // Or a more specific error
        }
    }
}
