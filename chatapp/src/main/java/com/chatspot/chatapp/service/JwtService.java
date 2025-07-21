package com.chatspot.chatapp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.util.Base64;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class JwtService {

    public String extractUserIdFromToken(String token) {
        try {
            // For Keycloak JWT tokens, we need to parse them differently
            // This is a simplified version - in production, you'd validate the signature
            Claims claims = parseTokenClaims(token);
            
            // Keycloak typically uses 'sub' claim for user ID
            String userId = claims.getSubject();
            if (userId == null) {
                // Try alternative claim names
                userId = (String) claims.get("preferred_username");
                if (userId == null) {
                    userId = (String) claims.get("email");
                }
            }
            
            return userId;
        } catch (Exception e) {
            System.err.println("Error extracting user ID from token: " + e.getMessage());
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseTokenClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        try {
            Claims claims = parseTokenClaims(token);
            return (String) claims.get("email");
        } catch (Exception e) {
            return null;
        }
    }

    public String extractPreferredUsername(String token) {
        try {
            Claims claims = parseTokenClaims(token);
            return (String) claims.get("preferred_username");
        } catch (Exception e) {
            return null;
        }
    }

    private Claims parseTokenClaims(String token) {
        try {
            // The token signature is already verified by Spring Security's filter chain.
            // We can safely decode the payload without re-validating the signature.
            String[] chunks = token.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            
            // Use ObjectMapper to parse the JSON payload into a Map, then into Claims
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> claimsMap = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
            return Jwts.claims().add(claimsMap).build();
        } catch (Exception e) {
            log.error("Failed to parse JWT claims: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }


}
