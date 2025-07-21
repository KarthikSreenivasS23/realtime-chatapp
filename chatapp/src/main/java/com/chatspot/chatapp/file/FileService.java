package com.chatspot.chatapp.file; // legacy util, not a Spring bean

import com.chatspot.chatapp.entity.message.MessageType;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class FileService {

    public static String saveMessageMediaFile(
            @Nonnull MultipartFile sourceFile,
            @Nonnull MessageType type
    ) throws IOException {
        if(!(type == MessageType.IMAGE || type == MessageType.VIDEO)) throw new IllegalArgumentException("Invalid media type");
        String directory = type == MessageType.IMAGE ?
                System.getProperty("application.media.picture.output-path", "./root/picture") :
                System.getProperty("application.media.video.output-path", "./root/video");
        return saveFile(directory,sourceFile);
    }

    public static String saveFile(
            @Nonnull String rootDirPath,
            @Nonnull MultipartFile sourceFile
    ) throws IOException {
        String fileName = UUID.randomUUID() + "_" + sourceFile.getOriginalFilename();
        Path path = Paths.get(rootDirPath, fileName);
        Files.createDirectories(path.getParent());

        try {
            Files.write(path, sourceFile.getBytes());
            log.info("File saved to: " + path);
            return path.toString();
        } catch (IOException e) {
            log.error("File was not saved", e);
        }
        return null;
    }

    public  static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}