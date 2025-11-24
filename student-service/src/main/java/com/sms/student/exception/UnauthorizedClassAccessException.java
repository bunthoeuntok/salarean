package com.sms.student.exception;

import com.sms.student.dto.ClassErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a teacher attempts to access a class they don't own.
 *
 * <p>This exception enforces the authorization rule that teachers can only:
 * <ul>
 *   <li>View their own classes</li>
 *   <li>Update their own classes</li>
 *   <li>Delete/Archive their own classes</li>
 * </ul>
 * </p>
 *
 * <p>Attempting to access another teacher's class will result in this exception.</p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Getter
public class UnauthorizedClassAccessException extends RuntimeException {

    private final Enum<?> errorCode;

    /**
     * Constructor with error message.
     *
     * @param message descriptive error message
     */
    public UnauthorizedClassAccessException(String message) {
        super(message);
        this.errorCode = ClassErrorCode.UNAUTHORIZED_CLASS_ACCESS;
    }

    /**
     * Constructor with error message and cause.
     *
     * @param message descriptive error message
     * @param cause   the underlying cause
     */
    public UnauthorizedClassAccessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ClassErrorCode.UNAUTHORIZED_CLASS_ACCESS;
    }
}
