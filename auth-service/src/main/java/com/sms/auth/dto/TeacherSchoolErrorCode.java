package com.sms.auth.dto;

/**
 * Error codes for teacher-school association operations
 */
public enum TeacherSchoolErrorCode {
    SUCCESS,
    TEACHER_ALREADY_ASSIGNED,
    SCHOOL_NOT_FOUND,
    INVALID_PRINCIPAL_DATA,
    TEACHER_SCHOOL_NOT_FOUND,
    INVALID_INPUT,
    UNAUTHORIZED
}
