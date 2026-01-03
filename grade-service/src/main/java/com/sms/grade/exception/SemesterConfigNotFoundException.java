package com.sms.grade.exception;

/**
 * Exception thrown when a semester configuration is not found.
 */
public class SemesterConfigNotFoundException extends RuntimeException {

    public SemesterConfigNotFoundException(String academicYear, String semesterExamCode) {
        super(String.format("Semester config not found for academic year %s semester exam code %s",
                academicYear, semesterExamCode));
    }

    public SemesterConfigNotFoundException(String message) {
        super(message);
    }
}
