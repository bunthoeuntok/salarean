package com.sms.student.dto;

/**
 * Error codes for school and location-related operations
 */
public enum SchoolErrorCode {
    SUCCESS,
    PROVINCE_NOT_FOUND,
    DISTRICT_NOT_FOUND,
    SCHOOL_NOT_FOUND,
    DUPLICATE_SCHOOL_NAME,
    INVALID_DISTRICT_FOR_PROVINCE,
    INVALID_INPUT,
    UNAUTHORIZED
}
