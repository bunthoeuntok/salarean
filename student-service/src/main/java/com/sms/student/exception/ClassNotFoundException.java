package com.sms.student.exception;

import com.sms.student.dto.ErrorCode;
import lombok.Getter;

@Getter
public class ClassNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;

    public ClassNotFoundException(String message) {
        super(message);
        this.errorCode = ErrorCode.CLASS_NOT_FOUND;
    }

    public ClassNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.CLASS_NOT_FOUND;
    }
}
