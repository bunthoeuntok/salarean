package com.sms.grade.exception;

public class DuplicateGradeException extends RuntimeException {
    public DuplicateGradeException(String message) {
        super(message);
    }
}
