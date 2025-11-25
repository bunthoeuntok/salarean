package com.sms.student.service.interfaces;

import java.util.UUID;

/**
 * Service interface for managing student photo storage.
 * Handles photo upload, validation, resizing, and deletion.
 */
public interface IPhotoStorageService {

    /**
     * Store a student photo with automatic resizing.
     * Creates both standard (400x400) and thumbnail (100x100) versions.
     * Deletes any existing photos for the student.
     *
     * @param studentId Student UUID
     * @param photoData Photo file bytes
     * @param contentType MIME type (image/jpeg or image/png)
     * @return Relative path to the stored photo (e.g., "students/uuid_timestamp.jpg")
     * @throws com.sms.student.exception.PhotoSizeExceededException if file > 5MB
     * @throws com.sms.student.exception.InvalidPhotoFormatException if invalid MIME type
     * @throws com.sms.student.exception.PhotoProcessingException if image processing fails
     */
    String savePhoto(UUID studentId, byte[] photoData, String contentType);

    /**
     * Delete all photos for a student (standard and thumbnail versions).
     *
     * @param studentId Student UUID
     */
    void deletePhotos(UUID studentId);

    /**
     * Get the full file system path for a photo.
     *
     * @param relativePath Relative path from savePhoto()
     * @return Full file system path
     */
    String getFullPath(String relativePath);

    /**
     * Check if a photo exists for a student.
     *
     * @param studentId Student UUID
     * @return true if photo exists, false otherwise
     */
    boolean photoExists(UUID studentId);
}
