package com.sms.student.exception;

import com.sms.student.dto.StudentErrorCode;
import lombok.Getter;

@Getter
public class EnrollmentNotFoundException extends RuntimeException {

    private final Enum<?> errorCode;

    public EnrollmentNotFoundException(String message) {
        super(message);
        this.errorCode = StudentErrorCode.ENROLLMENT_NOT_FOUND;
    }

    public EnrollmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = StudentErrorCode.ENROLLMENT_NOT_FOUND;
    }
}
