package com.sms.grade.exception;

public class InsufficientGradesException extends RuntimeException {
    public InsufficientGradesException(String message) {
        super(message);
    }
}
