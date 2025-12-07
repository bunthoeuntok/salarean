package com.sms.student.exception;

/**
 * Exception thrown when a teacher attempts to access a resource they don't own.
 *
 * <p>This exception is used to enforce teacher-based data isolation,
 * ensuring that teachers can only access their own students.</p>
 *
 * <p><b>Common Scenarios:</b></p>
 * <ul>
 *   <li>Teacher A tries to view/update/delete a student owned by Teacher B</li>
 *   <li>API request attempts to access a student without proper ownership validation</li>
 *   <li>Teacher context is missing when accessing protected resources</li>
 * </ul>
 *
 * <p>This exception is handled by GlobalExceptionHandler and returns HTTP 401 Unauthorized
 * with error code UNAUTHORIZED_ACCESS.</p>
 *
 * @see com.sms.student.exception.GlobalExceptionHandler
 * @see com.sms.student.security.TeacherContextHolder
 */
public class UnauthorizedAccessException extends RuntimeException {

    /**
     * Constructs a new UnauthorizedAccessException with the specified detail message.
     *
     * @param message the detail message explaining the authorization failure
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    /**
     * Constructs a new UnauthorizedAccessException with a default message.
     */
    public UnauthorizedAccessException() {
        super("You are not authorized to access this resource");
    }

    /**
     * Constructs a new UnauthorizedAccessException with the specified detail message and cause.
     *
     * @param message the detail message explaining the authorization failure
     * @param cause   the cause of the exception
     */
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
