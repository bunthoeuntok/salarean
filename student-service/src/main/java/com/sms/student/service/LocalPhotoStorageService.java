package com.sms.student.service;

import com.sms.student.exception.InvalidPhotoFormatException;
import com.sms.student.exception.PhotoProcessingException;
import com.sms.student.exception.PhotoSizeExceededException;
import com.sms.student.service.PhotoStorageService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Local file system implementation of PhotoStorageService.
 * Stores photos in the uploads/students directory with automatic resizing.
 */
@Service
@Slf4j
public class LocalPhotoStorageService implements PhotoStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList("image/jpeg", "image/png");
    private static final int STANDARD_SIZE = 400;
    private static final int THUMBNAIL_SIZE = 100;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Value("${app.upload.base-path:uploads/students}")
    private String basePath;

    @Override
    public String savePhoto(UUID studentId, byte[] photoData, String contentType) {
        log.info("Saving photo for student: {}, size: {} bytes, contentType: {}",
                 studentId, photoData.length, contentType);

        // T095: Validate MIME type
        validateMimeType(contentType);

        // T096: Validate file size
        validateFileSize(photoData.length);

        try {
            // Delete existing photos first (T099)
            deletePhotos(studentId);

            // T097: Generate filename
            String filename = generateFilename(studentId);
            String extension = getExtension(contentType);

            // Create directory if it doesn't exist
            Path uploadDir = Paths.get(basePath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("Created upload directory: {}", uploadDir);
            }

            // T098: Resize and save standard version (400x400)
            Path standardPath = uploadDir.resolve(filename + extension);
            Thumbnails.of(new ByteArrayInputStream(photoData))
                    .size(STANDARD_SIZE, STANDARD_SIZE)
                    .keepAspectRatio(true)
                    .toFile(standardPath.toFile());
            log.info("Saved standard photo: {}", standardPath);

            // T098: Resize and save thumbnail version (100x100)
            Path thumbnailPath = uploadDir.resolve(filename + "_thumb" + extension);
            Thumbnails.of(new ByteArrayInputStream(photoData))
                    .size(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                    .keepAspectRatio(true)
                    .toFile(thumbnailPath.toFile());
            log.info("Saved thumbnail photo: {}", thumbnailPath);

            // Return relative path
            String relativePath = "students/" + filename + extension;
            log.info("Photo saved successfully for student: {}, path: {}", studentId, relativePath);
            return relativePath;

        } catch (IOException e) {
            log.error("Failed to save photo for student: {}", studentId, e);
            throw new PhotoProcessingException("Failed to process and save photo", e);
        }
    }

    @Override
    public void deletePhotos(UUID studentId) {
        log.info("Deleting photos for student: {}", studentId);

        try {
            Path uploadDir = Paths.get(basePath);
            if (!Files.exists(uploadDir)) {
                log.debug("Upload directory does not exist, nothing to delete");
                return;
            }

            // Find and delete all files matching the student ID pattern
            String studentIdPrefix = studentId.toString();
            Files.list(uploadDir)
                    .filter(path -> path.getFileName().toString().startsWith(studentIdPrefix))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.debug("Deleted photo: {}", path);
                        } catch (IOException e) {
                            log.warn("Failed to delete photo: {}", path, e);
                        }
                    });

            log.info("Deleted all photos for student: {}", studentId);

        } catch (IOException e) {
            log.error("Failed to list files for deletion: {}", studentId, e);
            // Don't throw exception for deletion failures
        }
    }

    @Override
    public String getFullPath(String relativePath) {
        if (relativePath == null) {
            return null;
        }

        // Remove "students/" prefix if present
        String filename = relativePath.replace("students/", "");
        return Paths.get(basePath, filename).toString();
    }

    @Override
    public boolean photoExists(UUID studentId) {
        try {
            Path uploadDir = Paths.get(basePath);
            if (!Files.exists(uploadDir)) {
                return false;
            }

            String studentIdPrefix = studentId.toString();
            return Files.list(uploadDir)
                    .anyMatch(path -> path.getFileName().toString().startsWith(studentIdPrefix)
                                   && !path.getFileName().toString().contains("_thumb"));

        } catch (IOException e) {
            log.error("Failed to check photo existence for student: {}", studentId, e);
            return false;
        }
    }

    /**
     * T095: Validate MIME type (image/jpeg or image/png only)
     */
    private void validateMimeType(String contentType) {
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.error("Invalid MIME type: {}", contentType);
            throw new InvalidPhotoFormatException("Only JPEG and PNG images are allowed");
        }
    }

    /**
     * T096: Validate file size (max 5MB)
     */
    private void validateFileSize(long size) {
        if (size > MAX_FILE_SIZE) {
            log.error("File size {} bytes exceeds maximum {} bytes", size, MAX_FILE_SIZE);
            throw new PhotoSizeExceededException("Photo size must not exceed 5MB");
        }
    }

    /**
     * T097: Generate filename with pattern: {studentId}_{timestamp}
     */
    private String generateFilename(UUID studentId) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        return studentId.toString() + "_" + timestamp;
    }

    /**
     * Get file extension from MIME type
     */
    private String getExtension(String contentType) {
        if ("image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        return ".jpg"; // Default to jpg for image/jpeg
    }
}
