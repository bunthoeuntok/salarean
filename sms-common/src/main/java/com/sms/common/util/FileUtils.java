package com.sms.common.util;

import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * File utilities for Salarean SMS.
 * Focus on security (anti-spoofing, path traversal prevention) and validation.
 *
 * Security Features:
 * - Apache Tika for MIME type detection (prevents file extension spoofing)
 * - Filename sanitization (prevents path traversal attacks)
 * - Safe path building (ensures files stay within allowed directories)
 */
public final class FileUtils {

    private FileUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ============================================
    // CONSTANTS
    // ============================================

    // File size limits
    /** Maximum photo size: 5MB */
    public static final long MAX_PHOTO_SIZE_BYTES = 5 * 1024 * 1024;

    /** Maximum document size: 10MB */
    public static final long MAX_DOCUMENT_SIZE_BYTES = 10 * 1024 * 1024;

    // Allowed MIME types (detected by Apache Tika)
    /** Allowed image MIME types */
    public static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/webp"
    );

    /** Allowed document MIME types */
    public static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // .xlsx
    );

    // File extensions
    /** Allowed image file extensions */
    public static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    /** Allowed document file extensions */
    public static final Set<String> DOCUMENT_EXTENSIONS = Set.of("pdf", "doc", "docx", "xls", "xlsx");

    // Apache Tika for MIME type detection (security)
    private static final Tika TIKA = new Tika();

    // Date formatter for filename timestamps
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss");


    // ============================================
    // FILE SIZE VALIDATION
    // ============================================

    /**
     * Validate file size against maximum allowed
     *
     * @param fileSize File size in bytes
     * @param maxSize Maximum allowed size in bytes
     * @return true if valid (size > 0 and <= maxSize)
     */
    public static boolean isValidFileSize(long fileSize, long maxSize) {
        return fileSize > 0 && fileSize <= maxSize;
    }

    /**
     * Validate photo file size (max 5MB)
     *
     * @param fileSize File size in bytes
     * @return true if valid
     */
    public static boolean isValidPhotoSize(long fileSize) {
        return isValidFileSize(fileSize, MAX_PHOTO_SIZE_BYTES);
    }

    /**
     * Validate photo file size from MultipartFile
     *
     * @param file Uploaded file
     * @return true if valid
     */
    public static boolean isValidPhotoSize(MultipartFile file) {
        return file != null && isValidPhotoSize(file.getSize());
    }

    /**
     * Validate document file size (max 10MB)
     *
     * @param fileSize File size in bytes
     * @return true if valid
     */
    public static boolean isValidDocumentSize(long fileSize) {
        return isValidFileSize(fileSize, MAX_DOCUMENT_SIZE_BYTES);
    }

    /**
     * Check if file is empty
     *
     * @param file Uploaded file
     * @return true if file is null or empty
     */
    public static boolean isEmpty(MultipartFile file) {
        return file == null || file.isEmpty() || file.getSize() == 0;
    }


    // ============================================
    // MIME TYPE VALIDATION (Security - Anti-spoofing)
    // ============================================

    /**
     * Detect actual MIME type using Apache Tika
     *
     * SECURITY: This detects the actual file content, not just the extension.
     * Prevents spoofing attacks where .exe is renamed to .jpg
     *
     * @param fileData File bytes
     * @return Detected MIME type or null on error
     */
    public static String detectMimeType(byte[] fileData) {
        if (fileData == null || fileData.length == 0) {
            return null;
        }
        try {
            return TIKA.detect(fileData);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Detect actual MIME type from MultipartFile
     *
     * @param file Uploaded file
     * @return Detected MIME type or null on error
     */
    public static String detectMimeType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            return TIKA.detect(file.getBytes());
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Validate MIME type against allowed types
     *
     * @param fileData File bytes
     * @param allowedTypes Set of allowed MIME types
     * @return true if MIME type is in allowed set
     */
    public static boolean isValidMimeType(byte[] fileData, Set<String> allowedTypes) {
        String detectedMimeType = detectMimeType(fileData);
        return detectedMimeType != null && allowedTypes.contains(detectedMimeType.toLowerCase());
    }

    /**
     * Validate MIME type for MultipartFile
     *
     * @param file Uploaded file
     * @param allowedTypes Set of allowed MIME types
     * @return true if MIME type is in allowed set
     */
    public static boolean isValidMimeType(MultipartFile file, Set<String> allowedTypes) {
        try {
            return file != null && isValidMimeType(file.getBytes(), allowedTypes);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Validate photo MIME type (JPEG, PNG, WebP only)
     *
     * @param file Uploaded file
     * @return true if valid photo format
     */
    public static boolean isValidPhotoMimeType(MultipartFile file) {
        return isValidMimeType(file, ALLOWED_IMAGE_TYPES);
    }

    /**
     * Validate photo MIME type from byte array
     *
     * @param fileData File bytes
     * @return true if valid photo format
     */
    public static boolean isValidPhotoMimeType(byte[] fileData) {
        return isValidMimeType(fileData, ALLOWED_IMAGE_TYPES);
    }

    /**
     * Validate document MIME type (PDF, Word, Excel)
     *
     * @param file Uploaded file
     * @return true if valid document format
     */
    public static boolean isValidDocumentMimeType(MultipartFile file) {
        return isValidMimeType(file, ALLOWED_DOCUMENT_TYPES);
    }

    /**
     * Verify file extension matches actual content
     *
     * SECURITY: Prevents spoofing attacks where file extension doesn't match content.
     * Example: File named "photo.jpg" but contains PNG data → returns false
     *
     * @param filename Original filename with extension
     * @param detectedMimeType MIME type detected by Apache Tika
     * @return true if extension matches content
     */
    public static boolean isExtensionMatchingContent(String filename, String detectedMimeType) {
        if (filename == null || detectedMimeType == null) {
            return false;
        }

        String extension = getFileExtension(filename).toLowerCase();
        String mimeType = detectedMimeType.toLowerCase();

        // JPEG validation
        if ((extension.equals("jpg") || extension.equals("jpeg")) && mimeType.equals("image/jpeg")) {
            return true;
        }

        // PNG validation
        if (extension.equals("png") && mimeType.equals("image/png")) {
            return true;
        }

        // WebP validation
        if (extension.equals("webp") && mimeType.equals("image/webp")) {
            return true;
        }

        // PDF validation
        if (extension.equals("pdf") && mimeType.equals("application/pdf")) {
            return true;
        }

        // Word document validation
        if (extension.equals("doc") && mimeType.equals("application/msword")) {
            return true;
        }

        if (extension.equals("docx") &&
            mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return true;
        }

        return false;
    }


    // ============================================
    // FILENAME OPERATIONS
    // ============================================

    /**
     * Extract file extension from filename (without dot)
     *
     * @param filename File name with extension
     * @return Extension without dot (e.g., "jpg", "png") or empty string
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }

        return "";
    }

    /**
     * Get file extension from MIME type (with dot)
     *
     * @param mimeType MIME type (e.g., "image/jpeg")
     * @return Extension with dot (e.g., ".jpg") or ".bin" if unknown
     */
    public static String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null) {
            return ".jpg"; // Default for images
        }

        String lowerCaseMimeType = mimeType.toLowerCase();

        if (lowerCaseMimeType.equals("image/jpeg") || lowerCaseMimeType.equals("image/jpg")) {
            return ".jpg";
        } else if (lowerCaseMimeType.equals("image/png")) {
            return ".png";
        } else if (lowerCaseMimeType.equals("image/webp")) {
            return ".webp";
        } else if (lowerCaseMimeType.equals("application/pdf")) {
            return ".pdf";
        } else if (lowerCaseMimeType.equals("application/msword")) {
            return ".doc";
        } else if (lowerCaseMimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return ".docx";
        } else if (lowerCaseMimeType.equals("application/vnd.ms-excel")) {
            return ".xls";
        } else if (lowerCaseMimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            return ".xlsx";
        } else {
            return ".bin"; // Unknown type
        }
    }

    /**
     * Generate unique filename with entity ID and timestamp
     *
     * Pattern: {entityId}_{timestamp}.{extension}
     * Example: 550e8400-e29b-41d4-a716-446655440000_20250123143000.jpg
     *
     * @param entityId Entity UUID (e.g., student ID, user ID)
     * @param extension File extension (with or without dot)
     * @return Generated filename
     */
    public static String generateFilename(UUID entityId, String extension) {
        if (entityId == null) {
            throw new IllegalArgumentException("Entity ID cannot be null");
        }

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);

        // Ensure extension starts with dot
        if (extension != null && !extension.isEmpty() && !extension.startsWith(".")) {
            extension = "." + extension;
        } else if (extension == null || extension.isEmpty()) {
            extension = "";
        }

        return String.format("%s_%s%s", entityId, timestamp, extension);
    }

    /**
     * Sanitize filename - remove dangerous characters
     *
     * SECURITY: Prevents path traversal attacks (../, ../../, etc.)
     *
     * Operations:
     * - Remove path separators (/, \, null bytes)
     * - Remove leading/trailing dots
     * - Replace spaces with underscores
     * - Remove special characters (keep only alphanumeric, dots, dashes, underscores)
     * - Limit length to 255 characters
     *
     * @param filename Original filename
     * @return Sanitized filename safe for filesystem
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        // Remove path separators and null bytes (security)
        filename = filename.replaceAll("[/\\\\\\0]", "");

        // Remove leading/trailing whitespace and dots
        filename = filename.trim().replaceAll("^\\.+", "");

        // Replace spaces with underscores
        filename = filename.replaceAll("\\s+", "_");

        // Remove special characters except dots, dashes, underscores
        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "");

        // Limit length to 255 characters (filesystem limit)
        if (filename.length() > 255) {
            String extension = getFileExtension(filename);
            int extensionLength = extension.isEmpty() ? 0 : extension.length() + 1; // +1 for dot
            int maxNameLength = 255 - extensionLength;

            if (extensionLength > 0) {
                String nameWithoutExt = filename.substring(0, filename.lastIndexOf('.'));
                filename = nameWithoutExt.substring(0, Math.min(nameWithoutExt.length(), maxNameLength))
                         + "." + extension;
            } else {
                filename = filename.substring(0, 255);
            }
        }

        return filename;
    }


    // ============================================
    // FILE SIZE FORMATTING
    // ============================================

    /**
     * Format file size in human-readable format
     *
     * Examples:
     * - 500 → "500 B"
     * - 1536 → "1.5 KB"
     * - 1048576 → "1.0 MB"
     *
     * @param bytes File size in bytes
     * @return Formatted string (e.g., "1.5 MB")
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";

        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }


    // ============================================
    // DIRECTORY OPERATIONS
    // ============================================

    /**
     * Create directory if it doesn't exist
     *
     * @param dirPath Directory path
     * @return true if directory exists or was created successfully
     */
    public static boolean createDirectoryIfNotExists(Path dirPath) {
        try {
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Check if directory exists
     *
     * @param dirPath Directory path
     * @return true if exists and is a directory
     */
    public static boolean directoryExists(Path dirPath) {
        return Files.exists(dirPath) && Files.isDirectory(dirPath);
    }


    // ============================================
    // FILE OPERATIONS
    // ============================================

    /**
     * Save uploaded file to destination
     *
     * Creates parent directories if needed.
     * Replaces existing file if present.
     *
     * @param file MultipartFile to save
     * @param destination Destination path
     * @return true if saved successfully
     */
    public static boolean saveFile(MultipartFile file, Path destination) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        try {
            // Create parent directory if needed
            if (destination.getParent() != null) {
                Files.createDirectories(destination.getParent());
            }

            // Save file
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Delete file if it exists
     *
     * @param filePath Path to file
     * @return true if file was deleted or didn't exist
     */
    public static boolean deleteFile(Path filePath) {
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Delete files matching a pattern in a directory
     *
     * Pattern supports * wildcard.
     * Example: "student_*" matches "student_123.jpg", "student_456.png"
     *
     * @param directory Directory to search
     * @param filenamePattern Pattern to match (e.g., "student_*", "profile.*")
     * @return Number of files deleted
     */
    public static int deleteFilesMatching(Path directory, String filenamePattern) {
        if (!Files.exists(directory)) {
            return 0;
        }

        int deleted = 0;
        try {
            deleted = (int) Files.list(directory)
                    .filter(path -> matchesPattern(path.getFileName().toString(), filenamePattern))
                    .filter(FileUtils::deleteFile)
                    .count();
        } catch (IOException e) {
            // Return count of successfully deleted files
        }

        return deleted;
    }

    /**
     * Check if file exists
     *
     * @param filePath Path to file
     * @return true if exists and is a regular file
     */
    public static boolean fileExists(Path filePath) {
        return Files.exists(filePath) && Files.isRegularFile(filePath);
    }


    // ============================================
    // PATH OPERATIONS (Security)
    // ============================================

    /**
     * Build safe file path (prevents path traversal attacks)
     *
     * SECURITY: Ensures the resolved path is within the base directory.
     * Throws SecurityException if path traversal is detected.
     *
     * Example:
     * - buildSafePath("/app/uploads", "students", "photo.jpg") → OK
     * - buildSafePath("/app/uploads", "../etc", "passwd") → SecurityException
     *
     * @param baseDir Base directory (must be absolute path)
     * @param pathParts Path parts to append
     * @return Safe resolved path
     * @throws SecurityException if path traversal attempt detected
     */
    public static Path buildSafePath(Path baseDir, String... pathParts) {
        if (baseDir == null) {
            throw new IllegalArgumentException("Base directory cannot be null");
        }

        Path resolved = baseDir;
        for (String part : pathParts) {
            if (part == null || part.isEmpty()) {
                continue;
            }

            // Sanitize each part
            String sanitized = sanitizeFilename(part);
            resolved = resolved.resolve(sanitized);
        }

        // Ensure resolved path is within base directory (security check)
        if (!resolved.normalize().startsWith(baseDir.normalize())) {
            throw new SecurityException("Path traversal attempt detected: " + resolved);
        }

        return resolved;
    }


    // ============================================
    // PRIVATE HELPERS
    // ============================================

    /**
     * Simple pattern matching (supports * wildcard)
     *
     * Example: "student_*" matches "student_123.jpg"
     */
    private static boolean matchesPattern(String filename, String pattern) {
        if (pattern.contains("*")) {
            String regex = pattern.replace("*", ".*");
            return filename.matches(regex);
        }
        return filename.equals(pattern);
    }
}
