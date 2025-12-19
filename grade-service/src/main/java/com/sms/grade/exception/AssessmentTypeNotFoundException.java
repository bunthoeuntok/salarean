package com.sms.grade.exception;

import java.util.UUID;

public class AssessmentTypeNotFoundException extends RuntimeException {
    public AssessmentTypeNotFoundException(String message) {
        super(message);
    }

    public AssessmentTypeNotFoundException(UUID assessmentTypeId) {
        super("Assessment type not found with ID: " + assessmentTypeId);
    }
}
