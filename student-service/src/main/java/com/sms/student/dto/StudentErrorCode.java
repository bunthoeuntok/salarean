package com.sms.student.dto;

/**
 * Student-service specific error codes.
 *
 * These error codes are specific to student management,
 * parent contacts, class enrollment, and photo uploads.
 *
 * For common error codes shared across all services, see:
 * {@link com.sms.common.dto.ErrorCode}
 */
public enum StudentErrorCode {
    // STUDENT MANAGEMENT
    STUDENT_NOT_FOUND,
    INVALID_STUDENT_DATA,
    DUPLICATE_STUDENT_CODE,
    STUDENT_ALREADY_ENROLLED,

    // PHOTO UPLOAD
    PHOTO_SIZE_EXCEEDED,
    INVALID_PHOTO_FORMAT,
    PHOTO_UPLOAD_FAILED,
    PHOTO_PROCESSING_ERROR,

    // PARENT CONTACTS
    PARENT_CONTACT_REQUIRED,
    PARENT_CONTACT_NOT_FOUND,

    // CLASS MANAGEMENT
    CLASS_NOT_FOUND,
    CLASS_CAPACITY_EXCEEDED,

    // ENROLLMENT MANAGEMENT
    DUPLICATE_ENROLLMENT,
    ENROLLMENT_NOT_FOUND,
    INVALID_ENROLLMENT_STATUS
}
