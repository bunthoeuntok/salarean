package com.sms.auth.exception;

import com.sms.auth.dto.ErrorCode;

public class PhotoUploadException extends RuntimeException {

    private final ErrorCode errorCode;

    public PhotoUploadException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PhotoUploadException(ErrorCode errorCode) {
        super(errorCode.toString());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
