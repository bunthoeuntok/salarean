package com.sms.student.enums;

/**
 * Status of a student's enrollment in a class.
 *
 * <p>Tracks the lifecycle of a student-class enrollment relationship.</p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
public enum EnrollmentStatus {
    /**
     * Student is currently enrolled and attending this class.
     */
    ACTIVE,

    /**
     * Student has successfully completed this grade level.
     * (Mapped from 'COMPLETED' in database for frontend display as 'Graduated')
     */
    COMPLETED,

    /**
     * Student moved to another class or school.
     */
    TRANSFERRED,

    /**
     * Student dropped out or left the school.
     */
    WITHDRAWN
}
