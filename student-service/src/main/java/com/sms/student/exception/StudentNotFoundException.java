package com.sms.student.exception;

import com.sms.student.dto.ErrorCode;
import lombok.Getter;

@Getter
public class StudentNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;

    public StudentNotFoundException(String message) {
        super(message);
        this.errorCode = ErrorCode.STUDENT_NOT_FOUND;
    }

    public StudentNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.STUDENT_NOT_FOUND;
    }
}
