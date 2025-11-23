package com.sms.auth.exception;

import com.sms.common.dto.ErrorCode;

public class ProfileUpdateException extends RuntimeException {

    private final ErrorCode errorCode;

    public ProfileUpdateException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ProfileUpdateException(ErrorCode errorCode) {
        super(errorCode.toString());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
