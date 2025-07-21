package com.chatspot.chatapp.controller;

import com.chatspot.chatapp.common.dto.request.UpdateProfileRequest;
import com.chatspot.chatapp.entity.user.User;
import com.chatspot.chatapp.entity.user.UserResponseDto;
import com.chatspot.chatapp.service.DtoMapperService;
import com.chatspot.chatapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for managing user profiles and searching for users.")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private DtoMapperService dtoMapperService;

    @GetMapping("/profile")
    @Operation(summary = "Get current user's profile", description = "Fetches the complete profile for the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token.", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found.", content = @Content)
    })
    public ResponseEntity<UserResponseDto> getMyProfile(Authentication authentication) {
        logger.info("Request received for GET /api/users/profile by principal: {}", authentication.getName());
        try {
            User user = userService.getCurrentUser(authentication);
            UserResponseDto dto = dtoMapperService.toUserResponseDto(user, true);
            logger.info("Successfully retrieved profile for user: {}", user.getId());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Error fetching profile for principal: {}", authentication.getName(), e);
            if (e instanceof RuntimeException && e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user profile by ID", description = "Fetches the public profile for a user by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token.", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    public ResponseEntity<UserResponseDto> getUserById(
            @Parameter(description = "The unique ID of the user.", required = true) @PathVariable String id,
            Authentication authentication) {
        logger.info("Request received for GET /api/users/{} by principal: {}", id, authentication.getName());
        try {
            User user = userService.getUserById(id);
            UserResponseDto dto = dtoMapperService.toUserResponseDto(user, false);
            logger.info("Successfully retrieved profile for user ID: {}", id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            logger.warn("User not found for ID: {}. Error: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }


    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update current user's profile", description = "Updates the first name, last name, or profile picture for the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated user profile.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token.", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    public ResponseEntity<UserResponseDto> updateMyProfile(
            @Parameter(description = "The profile update request containing first name")
            @RequestPart(value="firstName",required = false) String firstName,
            @Parameter(description = "The profile update request containing first name")
            @RequestPart(value = "lastName",required = false) String lastName,
            @Parameter(description = "The profile picture file to be uploaded.")
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            Authentication authentication) {
        String principalName = authentication.getName();
        logger.info("Request received for PUT /api/users/profile by principal: {}", principalName);
        try {
            User user = userService.updateProfile(principalName, firstName,
                    lastName, profilePicture);
            UserResponseDto dto = dtoMapperService.toUserResponseDto(user, true);
            logger.info("Successfully updated profile for user: {}", user.getId());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Error updating profile for principal: {}", principalName, e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search for users by username", description = "Searches for users whose username contains the provided query string. The search is case-insensitive.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users.",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    public ResponseEntity<List<UserResponseDto>> searchUsers(
            @Parameter(description = "The search term to find in usernames.", required = true) @RequestParam String query,
            Authentication authentication) {
        logger.info("Request received for GET /api/users/search with query '{}' by principal: {}", query, authentication.getName());
        try {
            List<User> users = userService.searchUsers(query);
            List<UserResponseDto> dtos = dtoMapperService.toUserResponseDtoList(users, false);
            logger.info("Found {} users for query: '{}'", users.size(), query);
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Error searching for users with query: '{}'", query, e);
            return ResponseEntity.status(500).build();
        }
    }
}
