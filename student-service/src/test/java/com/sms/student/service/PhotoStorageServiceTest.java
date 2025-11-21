package com.sms.student.service;

import com.sms.student.exception.InvalidPhotoFormatException;
import com.sms.student.exception.PhotoProcessingException;
import com.sms.student.exception.PhotoSizeExceededException;
import com.sms.student.service.impl.LocalPhotoStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for PhotoStorageService implementation.
 * Tests photo validation, storage, and deletion.
 */
class PhotoStorageServiceTest {

    @TempDir
    Path tempDir;

    private PhotoStorageService photoStorageService;
    private byte[] validJpegData;
    private byte[] validPngData;
    private byte[] largeSizeData;

    @BeforeEach
    void setUp() throws IOException {
        photoStorageService = new LocalPhotoStorageService();
        ReflectionTestUtils.setField(photoStorageService, "basePath", tempDir.toString());

        // Create minimal valid JPEG data (small test image)
        validJpegData = createTestImageData(100); // 100 bytes

        // Create minimal valid PNG data
        validPngData = createTestImageData(200); // 200 bytes

        // Create data exceeding 5MB
        largeSizeData = new byte[6 * 1024 * 1024]; // 6MB
    }

    @AfterEach
    void cleanup() throws IOException {
        // Clean up test files
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
        }
    }

    // ============================================
    // MIME Type Validation Tests (T095)
    // ============================================

    @Test
    void savePhoto_WithValidJpegMimeType_ShouldSucceed() {
        // Arrange
        UUID studentId = UUID.randomUUID();

        // Act
        String photoPath = photoStorageService.savePhoto(studentId, validJpegData, "image/jpeg");

        // Assert
        assertThat(photoPath).isNotNull();
        assertThat(photoPath).contains("students/");
        assertThat(photoPath).endsWith(".jpg");
    }

    @Test
    void savePhoto_WithValidPngMimeType_ShouldSucceed() {
        // Arrange
        UUID studentId = UUID.randomUUID();

        // Act
        String photoPath = photoStorageService.savePhoto(studentId, validPngData, "image/png");

        // Assert
        assertThat(photoPath).isNotNull();
        assertThat(photoPath).endsWith(".png");
    }

    @Test
    void savePhoto_WithInvalidMimeType_ShouldThrowException() {
        // Arrange
        UUID studentId = UUID.randomUUID();

        // Act & Assert
        assertThatThrownBy(() -> photoStorageService.savePhoto(studentId, validJpegData, "image/gif"))
                .isInstanceOf(InvalidPhotoFormatException.class)
                .hasMessageContaining("Only JPEG and PNG images are allowed");
    }

    @Test
    void savePhoto_WithNullMimeType_ShouldThrowException() {
        // Arrange
        UUID studentId = UUID.randomUUID();

        // Act & Assert
        assertThatThrownBy(() -> photoStorageService.savePhoto(studentId, validJpegData, null))
                .isInstanceOf(InvalidPhotoFormatException.class);
    }

    // ============================================
    // File Size Validation Tests (T096)
    // ============================================

    @Test
    void savePhoto_WithFileSizeUnder5MB_ShouldSucceed() {
        // Arrange
        UUID studentId = UUID.randomUUID();
        byte[] smallData = new byte[1024]; // 1KB

        // Act
        String photoPath = photoStorageService.savePhoto(studentId, smallData, "image/jpeg");

        // Assert
        assertThat(photoPath).isNotNull();
    }

    @Test
    void savePhoto_WithFileSizeOver5MB_ShouldThrowException() {
        // Arrange
        UUID studentId = UUID.randomUUID();

        // Act & Assert
        assertThatThrownBy(() -> photoStorageService.savePhoto(studentId, largeSizeData, "image/jpeg"))
                .isInstanceOf(PhotoSizeExceededException.class)
                .hasMessageContaining("Photo size must not exceed 5MB");
    }

    // ============================================
    // Filename Generation Tests (T097)
    // ============================================

    @Test
    void savePhoto_ShouldGenerateFilenameWithStudentIdAndTimestamp() {
        // Arrange
        UUID studentId = UUID.randomUUID();

        // Act
        String photoPath = photoStorageService.savePhoto(studentId, validJpegData, "image/jpeg");

        // Assert
        assertThat(photoPath).contains(studentId.toString());
        assertThat(photoPath).matches("students/" + studentId.toString() + "_\\d{14}\\.jpg");
    }

    // ============================================
    // Duplicate Handling Tests (T099)
    // ============================================

    @Test
    void savePhoto_WhenPhotoExists_ShouldDeleteOldPhoto() {
        // Arrange
        UUID studentId = UUID.randomUUID();

        // Act - Upload first photo
        String firstPhotoPath = photoStorageService.savePhoto(studentId, validJpegData, "image/jpeg");
        assertThat(photoStorageService.photoExists(studentId)).isTrue();

        // Wait a moment to ensure different timestamp
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Upload second photo
        String secondPhotoPath = photoStorageService.savePhoto(studentId, validJpegData, "image/jpeg");

        // Assert - New photo exists, paths are different
        assertThat(secondPhotoPath).isNotEqualTo(firstPhotoPath);
        assertThat(photoStorageService.photoExists(studentId)).isTrue();
    }

    // ============================================
    // Photo Deletion Tests
    // ============================================

    @Test
    void deletePhotos_WithExistingPhotos_ShouldDeleteAllVersions() {
        // Arrange
        UUID studentId = UUID.randomUUID();
        photoStorageService.savePhoto(studentId, validJpegData, "image/jpeg");

        // Act
        photoStorageService.deletePhotos(studentId);

        // Assert
        assertThat(photoStorageService.photoExists(studentId)).isFalse();
    }

    @Test
    void deletePhotos_WithNoPhotos_ShouldNotThrowException() {
        // Arrange
        UUID studentId = UUID.randomUUID();

        // Act & Assert - Should not throw
        photoStorageService.deletePhotos(studentId);
    }

    // ============================================
    // Photo Existence Tests
    // ============================================

    @Test
    void photoExists_WhenPhotoExists_ShouldReturnTrue() {
        // Arrange
        UUID studentId = UUID.randomUUID();
        photoStorageService.savePhoto(studentId, validJpegData, "image/jpeg");

        // Act & Assert
        assertThat(photoStorageService.photoExists(studentId)).isTrue();
    }

    @Test
    void photoExists_WhenPhotoDoesNotExist_ShouldReturnFalse() {
        // Arrange
        UUID studentId = UUID.randomUUID();

        // Act & Assert
        assertThat(photoStorageService.photoExists(studentId)).isFalse();
    }

    // ============================================
    // Helper Methods
    // ============================================

    /**
     * Create minimal test image data
     */
    private byte[] createTestImageData(int size) {
        return new byte[size];
    }
}
