package com.sms.grade.enums;

/**
 * Types of grade averages that can be calculated.
 */
public enum AverageType {
    MONTHLY_AVERAGE,      // Average of monthly exams for a subject
    SEMESTER_AVERAGE,     // Semester average for a subject (monthly + semester exam)
    SUBJECT_ANNUAL,       // Annual average for a subject
    OVERALL_SEMESTER,     // Overall average across all subjects for a semester
    OVERALL_ANNUAL        // Overall annual average
}
