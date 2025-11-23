package com.sms.auth.exception;

public class ResetTokenExpiredException extends RuntimeException {

    private final Enum<?> errorCode;

    public ResetTokenExpiredException(Enum<?> errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ResetTokenExpiredException(Enum<?> errorCode) {
        super(errorCode.toString());
        this.errorCode = errorCode;
    }

    public Enum<?> getErrorCode() {
        return errorCode;
    }
}
