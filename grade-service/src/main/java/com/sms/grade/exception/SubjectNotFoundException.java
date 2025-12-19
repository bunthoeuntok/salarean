package com.sms.grade.exception;

import java.util.UUID;

public class SubjectNotFoundException extends RuntimeException {
    public SubjectNotFoundException(String message) {
        super(message);
    }

    public SubjectNotFoundException(UUID subjectId) {
        super("Subject not found with ID: " + subjectId);
    }
}
