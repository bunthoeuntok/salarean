package com.sms.student.model;

public enum EnrollmentStatus {
    /**
     * Student is currently enrolled in the class
     */
    ACTIVE,

    /**
     * Student completed the class (academic year ended successfully)
     */
    COMPLETED,

    /**
     * Student transferred to another class
     */
    TRANSFERRED,

    /**
     * Student withdrawn from the class (dropped out, expelled, etc.)
     */
    WITHDRAWN
}
