package com.sms.student.dto;

/**
 * Error codes specific to class management operations in student-service.
 *
 * <p>These error codes follow the same conventions as {@link com.sms.common.dto.ErrorCode}
 * but are specific to the class management feature.</p>
 *
 * <p>Frontend Responsibility:
 * <ul>
 *   <li>Map error codes to localized messages (English/Khmer)</li>
 *   <li>Display appropriate error messages to users</li>
 * </ul>
 * </p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
public enum ClassErrorCode {

    // ============================================
    // CLASS NOT FOUND ERRORS
    // ============================================

    /**
     * The requested class was not found in the system.
     */
    CLASS_NOT_FOUND,

    /**
     * Teacher attempted to access a class they don't own.
     */
    CLASS_NOT_OWNED_BY_TEACHER,


    // ============================================
    // DUPLICATE CLASS ERRORS
    // ============================================

    /**
     * A class with the same name, grade, subject, and academic year already exists.
     * Violates unique constraint: uk_class_name_grade_subject_year
     */
    DUPLICATE_CLASS,


    // ============================================
    // VALIDATION ERRORS
    // ============================================

    /**
     * Class capacity must be between 5 and 60 students.
     */
    INVALID_CLASS_CAPACITY,

    /**
     * Academic year format must be YYYY-YYYY (e.g., "2024-2025").
     */
    INVALID_ACADEMIC_YEAR_FORMAT,

    /**
     * Academic year must be consecutive (second year = first year + 1).
     */
    INVALID_ACADEMIC_YEAR_SEQUENCE,

    /**
     * Invalid grade level for Cambodia education system (must be GRADE_1 to GRADE_12).
     */
    INVALID_GRADE_LEVEL,


    // ============================================
    // AUTHORIZATION ERRORS
    // ============================================

    /**
     * Teacher is not authorized to access or modify this class.
     */
    UNAUTHORIZED_CLASS_ACCESS,


    // ============================================
    // ENROLLMENT HISTORY ERRORS
    // ============================================

    /**
     * No enrollment history found for the requested class.
     */
    ENROLLMENT_HISTORY_NOT_FOUND
}
