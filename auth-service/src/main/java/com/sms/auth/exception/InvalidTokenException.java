package com.sms.auth.exception;

public class InvalidTokenException extends RuntimeException {

    private final Enum<?> errorCode;

    public InvalidTokenException(Enum<?> errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public InvalidTokenException(Enum<?> errorCode) {
        super(errorCode.toString());
        this.errorCode = errorCode;
    }

    public Enum<?> getErrorCode() {
        return errorCode;
    }
}
