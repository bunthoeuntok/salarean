package com.sms.auth.exception;

import com.sms.common.dto.ErrorCode;

public class InvalidTokenException extends RuntimeException {

    private final ErrorCode errorCode;

    public InvalidTokenException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public InvalidTokenException(ErrorCode errorCode) {
        super(errorCode.toString());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
