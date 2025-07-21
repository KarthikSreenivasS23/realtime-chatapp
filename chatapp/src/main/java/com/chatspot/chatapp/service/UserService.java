package com.chatspot.chatapp.service;

import com.chatspot.chatapp.entity.user.User;
import com.chatspot.chatapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileService fileService;
    
    public User getCurrentUser(Authentication authentication) {
        String userId = authentication.getName();
        return userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
    
//    public User createOrUpdateUser(String id, String firstName, String lastName, String email) {
//        return userRepository.findById(id)
//            .map(user -> {
//                user.setFirstName(firstName);
//                user.setLastName(lastName);
//                user.setEmail(email);
//                return userRepository.save(user);
//            })
//            .orElseGet(() -> {
//                User newUser = new User();
//                newUser.setId(id);
//                newUser.setFirstName(firstName);
//                newUser.setLastName(lastName);
//                newUser.setEmail(email);
//                return userRepository.save(newUser);
//            });
//    }

    public User getUserById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
    
    public User updateProfile(String userId, String firstName, String lastName, MultipartFile profilePicture) throws IOException {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        if(firstName!=null && !user.getFirstName().equals(firstName)) user.setFirstName(firstName);
        if(lastName!=null && !user.getLastName().equals(lastName)) user.setLastName(lastName);
        if (profilePicture != null && !profilePicture.isEmpty()) {
            user.setProfilePicture(fileService.saveProfilePicture(profilePicture,userId));
        }

        return userRepository.save(user);
    }
    
    public List<User> searchUsers(String name) {
        return userRepository.findByName(name);
    }
}