package com.sms.student.exception;

import com.sms.student.dto.ClassErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a requested class is not found in the system.
 *
 * <p>This exception is thrown in the following scenarios:
 * <ul>
 *   <li>Class ID does not exist in database</li>
 *   <li>Class exists but has been archived/soft-deleted</li>
 *   <li>Class exists but is not owned by the requesting teacher</li>
 * </ul>
 * </p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Getter
public class ClassNotFoundException extends RuntimeException {

    private final Enum<?> errorCode;

    /**
     * Constructor with error message.
     *
     * @param message descriptive error message
     */
    public ClassNotFoundException(String message) {
        super(message);
        this.errorCode = ClassErrorCode.CLASS_NOT_FOUND;
    }

    /**
     * Constructor with error message and cause.
     *
     * @param message descriptive error message
     * @param cause   the underlying cause
     */
    public ClassNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ClassErrorCode.CLASS_NOT_FOUND;
    }
}
