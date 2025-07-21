package com.chatspot.chatapp.service;

import com.chatspot.chatapp.controller.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileService {

    @Value("${application.media.picture.output-path:./root/picture}")
    private String pictureOutputPath;

    @Value("${application.media.video.output-path:./root/video}")
    private String videoOutputPath;

    @Value("${application.media.profile.output-path:./root/profile}")
    private String profileOutputPath;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    public String saveProfilePicture(MultipartFile file, String userId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(profileOutputPath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? 
            originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String filename = userId + "_" + UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        logger.info("Profile picture saved to: " + filePath);
        return filePath.toString();
    }

    public String saveMediaFile(MultipartFile file, String messageId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        String outputPath;

        // Determine output path based on content type
        if (contentType != null && contentType.startsWith("image/")) {
            outputPath = pictureOutputPath;
        } else if (contentType != null && contentType.startsWith("video/")) {
            outputPath = videoOutputPath;
        } else {
            outputPath = pictureOutputPath; // default to picture path
        }

        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(outputPath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? 
            originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String filename = messageId + "_" + UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        logger.info("Profile picture saved to: " + filePath);

        return filePath.toString();
    }

    public byte[] loadFileAsBytes(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            return null;
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return null;
        }

        return Files.readAllBytes(path);
    }

    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }

        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

    public boolean fileExists(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }

        Path path = Paths.get(filePath);
        return Files.exists(path);
    }
}
