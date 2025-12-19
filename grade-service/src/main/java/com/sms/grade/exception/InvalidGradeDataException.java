package com.sms.grade.exception;

public class InvalidGradeDataException extends RuntimeException {
    public InvalidGradeDataException(String message) {
        super(message);
    }
}
