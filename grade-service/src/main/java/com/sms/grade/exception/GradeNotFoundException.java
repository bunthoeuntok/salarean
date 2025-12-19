package com.sms.grade.exception;

import java.util.UUID;

public class GradeNotFoundException extends RuntimeException {
    public GradeNotFoundException(String message) {
        super(message);
    }

    public GradeNotFoundException(UUID gradeId) {
        super("Grade not found with ID: " + gradeId);
    }
}
