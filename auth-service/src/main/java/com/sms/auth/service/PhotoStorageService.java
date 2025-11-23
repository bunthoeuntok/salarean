package com.sms.auth.service;

import com.sms.common.constants.CommonConstants;
import com.sms.common.dto.ErrorCode;
import com.sms.common.util.FileUtils;
import com.sms.auth.exception.PhotoUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class PhotoStorageService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoStorageService.class);

    @Value("${app.photo.upload-dir}")
    private String uploadDir;

    /**
     * Validate photo file (size, format, content type)
     */
    public void validatePhoto(MultipartFile file) {
        // Validate file size using FileUtils
        if (!FileUtils.isValidPhotoSize(file)) {
            logger.warn("Photo upload failed - file size exceeded: {} bytes", file.getSize());
            throw new PhotoUploadException(ErrorCode.PHOTO_SIZE_EXCEEDED,
                    "File size exceeds maximum allowed size of " +
                    (CommonConstants.MAX_PHOTO_SIZE_BYTES / (1024 * 1024)) + "MB");
        }

        // Validate file is not empty using FileUtils
        if (FileUtils.isEmpty(file)) {
            logger.warn("Photo upload failed - empty file");
            throw new PhotoUploadException(ErrorCode.INVALID_PHOTO_FORMAT,
                    "File is empty");
        }

        // Detect actual MIME type using FileUtils
        String detectedMimeType = FileUtils.detectMimeType(file);
        if (detectedMimeType == null) {
            logger.error("Failed to detect MIME type");
            throw new PhotoUploadException(ErrorCode.CORRUPTED_IMAGE,
                    "Failed to read file content");
        }

        // Validate MIME type using FileUtils
        if (!FileUtils.isValidPhotoMimeType(file)) {
            logger.warn("Photo upload failed - invalid MIME type: {}", detectedMimeType);
            throw new PhotoUploadException(ErrorCode.INVALID_PHOTO_FORMAT,
                    "Only JPEG, PNG, and WebP images are allowed");
        }

        // Verify content type matches extension using FileUtils
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !FileUtils.isExtensionMatchingContent(originalFilename, detectedMimeType)) {
            String extension = FileUtils.getFileExtension(originalFilename);
            logger.warn("Photo upload failed - content type mismatch. Extension: {}, Detected: {}",
                    extension, detectedMimeType);
            throw new PhotoUploadException(ErrorCode.CORRUPTED_IMAGE,
                    "File content does not match extension");
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

            // Determine file extension from MIME type
            String detectedMimeType = FileUtils.detectMimeType(file);
            String extension = FileUtils.getExtensionFromMimeType(detectedMimeType);

            // Save new photo using FileUtils
            String filename = "profile." + extension;
            Path targetPath = userDir.resolve(filename);

            if (!FileUtils.saveFile(file, targetPath)) {
                throw new IOException("Failed to save file");
            }

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
                // Delete all profile photos using FileUtils
                int deletedCount = FileUtils.deleteFilesMatching(userDir, "profile.*");
                if (deletedCount > 0) {
                    logger.info("Deleted {} old photo(s) for user: {}", deletedCount, userId);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to delete old photos for user: {}", userId, e);
            // Don't throw exception - this is a cleanup operation
        }
    }
}
