package com.sms.auth.exception;

public class UserNotFoundException extends RuntimeException {

    private final Enum<?> errorCode;

    public UserNotFoundException(Enum<?> errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public UserNotFoundException(Enum<?> errorCode) {
        super(errorCode.toString());
        this.errorCode = errorCode;
    }

    public Enum<?> getErrorCode() {
        return errorCode;
    }
}
