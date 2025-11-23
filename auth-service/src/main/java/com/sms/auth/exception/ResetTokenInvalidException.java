package com.sms.auth.exception;

public class ResetTokenInvalidException extends RuntimeException {

    private final Enum<?> errorCode;

    public ResetTokenInvalidException(Enum<?> errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ResetTokenInvalidException(Enum<?> errorCode) {
        super(errorCode.toString());
        this.errorCode = errorCode;
    }

    public Enum<?> getErrorCode() {
        return errorCode;
    }
}
