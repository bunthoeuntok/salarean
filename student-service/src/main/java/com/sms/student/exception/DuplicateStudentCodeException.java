package com.sms.student.exception;

import com.sms.student.dto.ErrorCode;
import lombok.Getter;

@Getter
public class DuplicateStudentCodeException extends RuntimeException {

    private final ErrorCode errorCode;

    public DuplicateStudentCodeException(String message) {
        super(message);
        this.errorCode = ErrorCode.DUPLICATE_STUDENT_CODE;
    }

    public DuplicateStudentCodeException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.DUPLICATE_STUDENT_CODE;
    }
}
