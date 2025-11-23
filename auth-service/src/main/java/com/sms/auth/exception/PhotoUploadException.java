package com.sms.auth.exception;

public class PhotoUploadException extends RuntimeException {

    private final Enum<?> errorCode;

    public PhotoUploadException(Enum<?> errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PhotoUploadException(Enum<?> errorCode) {
        super(errorCode.toString());
        this.errorCode = errorCode;
    }

    public Enum<?> getErrorCode() {
        return errorCode;
    }
}
