package com.sms.student.exception;

import com.sms.student.dto.StudentErrorCode;
import lombok.Getter;

@Getter
public class ClassNotFoundException extends RuntimeException {

    private final Enum<?> errorCode;

    public ClassNotFoundException(String message) {
        super(message);
        this.errorCode = StudentErrorCode.CLASS_NOT_FOUND;
    }

    public ClassNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = StudentErrorCode.CLASS_NOT_FOUND;
    }
}
