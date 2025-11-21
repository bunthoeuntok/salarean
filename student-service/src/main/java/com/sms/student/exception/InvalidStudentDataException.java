package com.sms.student.exception;

import com.sms.student.dto.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidStudentDataException extends RuntimeException {

    private final ErrorCode errorCode;

    public InvalidStudentDataException(String message) {
        super(message);
        this.errorCode = ErrorCode.INVALID_STUDENT_DATA;
    }

    public InvalidStudentDataException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.INVALID_STUDENT_DATA;
    }
}
