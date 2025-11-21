package com.sms.auth.exception;

import com.sms.auth.dto.ErrorCode;

public class ResetTokenExpiredException extends RuntimeException {

    private final ErrorCode errorCode;

    public ResetTokenExpiredException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ResetTokenExpiredException(ErrorCode errorCode) {
        super(errorCode.toString());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
