package com.chatspot.chatapp.entity.user;

import com.chatspot.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSynchronizer {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public void synchronizeWithIdp(Jwt token) {
        log.info("Synchronizing user with idp");

        getUserEmail(token).ifPresent(email -> {
            log.info("Checking if user with email {} already exists", email);

            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isEmpty()) {
                // Only create user if not exists
                User newUser = userMapper.fromTokenAttributes(token.getClaims());
                userRepository.save(newUser);
                log.info("User created for email: {}", email);
            } else {
                log.info("User already exists. Skipping synchronization.");
            }
        });

    }

    private Optional<String> getUserEmail(Jwt token) {
        Map<String, Object> attributes = token.getClaims();
        if (attributes.containsKey("email")) {
            return Optional.of(attributes.get("email").toString());
        }
        return Optional.empty();

    }
}