package com.sms.student.exception;

import com.sms.student.dto.StudentErrorCode;
import lombok.Getter;

@Getter
public class SchoolNotFoundException extends RuntimeException {

    private final Enum<?> errorCode;

    public SchoolNotFoundException(String message) {
        super(message);
        this.errorCode = StudentErrorCode.SCHOOL_NOT_FOUND;
    }

    public SchoolNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = StudentErrorCode.SCHOOL_NOT_FOUND;
    }
}
