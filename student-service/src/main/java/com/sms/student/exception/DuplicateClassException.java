package com.sms.student.exception;

import com.sms.student.dto.ClassErrorCode;

import lombok.Getter;

/**
 * Exception thrown when attempting to create a class that violates the unique constraint.
 *
 * <p>The unique constraint is defined as:
 * <pre>
 * UNIQUE (name, grade_level, subject, academic_year)
 * </pre>
 * </p>
 *
 * <p>This exception prevents duplicate classes with the same:
 * <ul>
 *   <li>Class name</li>
 *   <li>Grade level (GRADE_1 to GRADE_12)</li>
 *   <li>Subject</li>
 *   <li>Academic year (e.g., "2024-2025")</li>
 * </ul>
 * </p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Getter
public class DuplicateClassException extends RuntimeException {

    private final Enum<?> errorCode;

    /**
     * Constructor with error message.
     *
     * @param message descriptive error message
     */
    public DuplicateClassException(String message) {
        super(message);
        this.errorCode = ClassErrorCode.DUPLICATE_CLASS;
    }

    /**
     * Constructor with error message and cause.
     *
     * @param message descriptive error message
     * @param cause   the underlying cause
     */
    public DuplicateClassException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ClassErrorCode.DUPLICATE_CLASS;
    }
}
