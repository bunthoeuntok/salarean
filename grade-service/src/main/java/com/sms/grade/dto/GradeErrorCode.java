package com.sms.grade.dto;

/**
 * Error codes for grade-service operations.
 * These codes are used for client-side i18n lookup.
 */
public enum GradeErrorCode {
    // Success
    SUCCESS,

    // Grade Errors
    GRADE_NOT_FOUND,
    DUPLICATE_GRADE_ENTRY,
    SCORE_OUT_OF_RANGE,
    INVALID_SCORE,
    GRADE_ALREADY_EXISTS,

    // Subject Errors
    SUBJECT_NOT_FOUND,
    INVALID_SUBJECT_FOR_GRADE_LEVEL,

    // Assessment Type Errors
    ASSESSMENT_TYPE_NOT_FOUND,
    INVALID_ASSESSMENT_TYPE,

    // Configuration Errors
    CONFIG_NOT_FOUND,
    INVALID_CONFIG,
    MONTHLY_EXAM_COUNT_OUT_OF_RANGE,
    WEIGHTS_MUST_SUM_TO_100,

    // Calculation Errors
    INSUFFICIENT_GRADES_FOR_CALCULATION,
    CALCULATION_ERROR,
    MISSING_MONTHLY_EXAMS,
    MISSING_SEMESTER_EXAM,

    // Validation Errors
    INVALID_SEMESTER,
    INVALID_ACADEMIC_YEAR,
    INVALID_STUDENT_ID,
    INVALID_CLASS_ID,
    STUDENT_NOT_IN_CLASS,

    // Authorization Errors
    UNAUTHORIZED,
    ACCESS_DENIED,

    // General Errors
    VALIDATION_ERROR,
    INTERNAL_ERROR
}
