package com.sms.auth.service;

import com.sms.common.dto.ErrorCode;
import com.sms.auth.exception.PhotoUploadException;
import org.apache.tika.Tika;
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
import java.util.Set;
import java.util.UUID;

@Service
public class PhotoStorageService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoStorageService.class);
    private static final long MAX_FILE_SIZE = 5242880; // 5MB in bytes
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png");
    private static final Tika tika = new Tika();

    @Value("${app.photo.upload-dir}")
    private String uploadDir;

    /**
     * Validate photo file (size, format, content type)
     */
    public void validatePhoto(MultipartFile file) {
        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            logger.warn("Photo upload failed - file size exceeded: {} bytes", file.getSize());
            throw new PhotoUploadException(ErrorCode.PHOTO_SIZE_EXCEEDED,
                    "File size exceeds maximum allowed size of 5MB");
        }

        // Validate file is not empty
        if (file.isEmpty()) {
            logger.warn("Photo upload failed - empty file");
            throw new PhotoUploadException(ErrorCode.INVALID_PHOTO_FORMAT,
                    "File is empty");
        }

        // Detect actual MIME type using Apache Tika
        String detectedMimeType;
        try {
            detectedMimeType = tika.detect(file.getBytes());
        } catch (IOException e) {
            logger.error("Failed to detect MIME type", e);
            throw new PhotoUploadException(ErrorCode.CORRUPTED_IMAGE,
                    "Failed to read file content");
        }

        // Validate MIME type
        if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
            logger.warn("Photo upload failed - invalid MIME type: {}", detectedMimeType);
            throw new PhotoUploadException(ErrorCode.INVALID_PHOTO_FORMAT,
                    "Only JPG and PNG images are allowed");
        }

        // Verify content type matches extension (detect spoofing)
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            boolean isValidMatch = (extension.equals("jpg") || extension.equals("jpeg"))
                                    && detectedMimeType.equals("image/jpeg")
                                    || extension.equals("png") && detectedMimeType.equals("image/png");

            if (!isValidMatch) {
                logger.warn("Photo upload failed - content type mismatch. Extension: {}, Detected: {}",
                        extension, detectedMimeType);
                throw new PhotoUploadException(ErrorCode.CORRUPTED_IMAGE,
                        "File content does not match extension");
            }
        }
    }

    /**
     * Save photo to filesystem and return relative URL path
     */
    public String savePhoto(UUID userId, MultipartFile file) {
        try {
            // Create user-specific directory
            Path userDir = Paths.get(uploadDir, userId.toString());
            Files.createDirectories(userDir);

            // Delete old photo if exists
            deletePhoto(userId);

            // Determine file extension
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? getFileExtension(originalFilename) : "jpg";

            // Save new photo
            String filename = "profile." + extension;
            Path targetPath = userDir.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative URL path
            String photoUrl = String.format("/uploads/profile-photos/%s/%s", userId, filename);

            logger.info("Photo saved successfully - User ID: {}, Path: {}, Size: {} bytes",
                    userId, photoUrl, file.getSize());

            return photoUrl;

        } catch (IOException e) {
            logger.error("Failed to save photo for user: {}", userId, e);
            throw new PhotoUploadException(ErrorCode.INTERNAL_ERROR,
                    "Failed to save photo");
        }
    }

    /**
     * Delete photo from filesystem
     */
    public void deletePhoto(UUID userId) {
        try {
            Path userDir = Paths.get(uploadDir, userId.toString());
            if (Files.exists(userDir)) {
                // Delete all files in user directory (profile.jpg, profile.png, etc.)
                Files.list(userDir)
                        .filter(path -> path.getFileName().toString().startsWith("profile."))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                                logger.info("Deleted old photo: {}", path);
                            } catch (IOException e) {
                                logger.warn("Failed to delete old photo: {}", path, e);
                            }
                        });
            }
        } catch (IOException e) {
            logger.warn("Failed to delete old photos for user: {}", userId, e);
            // Don't throw exception - this is a cleanup operation
        }
    }

    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }
}
