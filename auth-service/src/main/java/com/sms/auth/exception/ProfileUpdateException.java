package com.sms.auth.exception;

public class ProfileUpdateException extends RuntimeException {

    private final Enum<?> errorCode;

    public ProfileUpdateException(Enum<?> errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ProfileUpdateException(Enum<?> errorCode) {
        super(errorCode.toString());
        this.errorCode = errorCode;
    }

    public Enum<?> getErrorCode() {
        return errorCode;
    }
}
