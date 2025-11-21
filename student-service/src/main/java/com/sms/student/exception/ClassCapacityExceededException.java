package com.sms.student.exception;

import com.sms.student.dto.ErrorCode;
import lombok.Getter;

/**
 * Exception thrown when attempting to enroll a student in a class that has reached maximum capacity.
 */
@Getter
public class ClassCapacityExceededException extends RuntimeException {

    private final ErrorCode errorCode;

    public ClassCapacityExceededException(String message) {
        super(message);
        this.errorCode = ErrorCode.CLASS_CAPACITY_EXCEEDED;
    }

    public ClassCapacityExceededException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.CLASS_CAPACITY_EXCEEDED;
    }
}
