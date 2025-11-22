package com.sms.student.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration for file upload settings, particularly for student photos.
 * Manages upload directory initialization and configuration values.
 */
@Configuration
@Getter
public class FileUploadConfig {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.max-file-size}")
    private long maxFileSize;

    @Value("${app.upload.allowed-extensions}")
    private String[] allowedExtensions;

    @Value("${app.upload.standard-size}")
    private int standardSize;

    @Value("${app.upload.thumbnail-size}")
    private int thumbnailSize;

    /**
     * Initialize the upload directory on application startup.
     * Creates the directory if it doesn't exist.
     */
    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created upload directory: " + uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }

    /**
     * Get the full path for the upload directory.
     */
    public Path getUploadPath() {
        return Paths.get(uploadDir).toAbsolutePath();
    }

    /**
     * Check if a file extension is allowed.
     */
    public boolean isAllowedExtension(String extension) {
        if (extension == null) {
            return false;
        }
        String ext = extension.toLowerCase().replaceFirst("^\\.", "");
        for (String allowed : allowedExtensions) {
            if (allowed.equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }
}
