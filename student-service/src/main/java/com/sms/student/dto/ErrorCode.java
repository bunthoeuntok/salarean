package com.sms.student.dto;

public enum ErrorCode {
    SUCCESS,

    // Student-specific errors
    STUDENT_NOT_FOUND,
    INVALID_STUDENT_DATA,
    DUPLICATE_STUDENT_CODE,
    STUDENT_ALREADY_ENROLLED,

    // Photo errors
    PHOTO_SIZE_EXCEEDED,
    INVALID_PHOTO_FORMAT,
    PHOTO_UPLOAD_FAILED,

    // Parent contact errors
    INVALID_PHONE_FORMAT,
    PARENT_CONTACT_REQUIRED,

    // Class errors
    CLASS_NOT_FOUND,
    CLASS_CAPACITY_EXCEEDED,

    // Generic
    VALIDATION_ERROR,
    UNAUTHORIZED,
    INTERNAL_ERROR
}
