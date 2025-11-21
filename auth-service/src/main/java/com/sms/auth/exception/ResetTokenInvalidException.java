package com.sms.auth.exception;

import com.sms.auth.dto.ErrorCode;

public class ResetTokenInvalidException extends RuntimeException {

    private final ErrorCode errorCode;

    public ResetTokenInvalidException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ResetTokenInvalidException(ErrorCode errorCode) {
        super(errorCode.toString());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
