package com.sms.student.exception;

import com.sms.student.dto.StudentErrorCode;
import lombok.Getter;

/**
 * Exception thrown when attempting to enroll a student in a class that has reached maximum capacity.
 */
@Getter
public class ClassCapacityExceededException extends RuntimeException {

    private final Enum<?> errorCode;

    public ClassCapacityExceededException(String message) {
        super(message);
        this.errorCode = StudentErrorCode.CLASS_CAPACITY_EXCEEDED;
    }

    public ClassCapacityExceededException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = StudentErrorCode.CLASS_CAPACITY_EXCEEDED;
    }
}
