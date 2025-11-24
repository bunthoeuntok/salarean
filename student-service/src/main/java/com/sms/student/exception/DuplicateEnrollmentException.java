package com.sms.student.exception;

import com.sms.student.dto.StudentErrorCode;
import lombok.Getter;

@Getter
public class DuplicateEnrollmentException extends RuntimeException {

    private final Enum<?> errorCode;

    public DuplicateEnrollmentException(String message) {
        super(message);
        this.errorCode = StudentErrorCode.DUPLICATE_ENROLLMENT;
    }

    public DuplicateEnrollmentException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = StudentErrorCode.DUPLICATE_ENROLLMENT;
    }
}
