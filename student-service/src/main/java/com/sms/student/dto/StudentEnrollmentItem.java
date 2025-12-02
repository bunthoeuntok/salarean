package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO representing a student enrollment item for class roster display.
 *
 * <p>Contains essential student information and enrollment status
 * for displaying in the class detail view student list.</p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentEnrollmentItem {

    /**
     * Student's unique identifier (UUID as string).
     */
    private String studentId;

    /**
     * Student's full name (e.g., "Sok Pisey").
     */
    private String studentName;

    /**
     * Student's unique code (e.g., "STU-2024-0001").
     */
    private String studentCode;

    /**
     * Student's profile photo URL (null if not uploaded).
     */
    private String photoUrl;

    /**
     * Date when the student enrolled in this class.
     */
    private LocalDate enrollmentDate;

    /**
     * Enrollment status: ACTIVE, COMPLETED, TRANSFERRED, or WITHDRAWN.
     * Maps to frontend display as: Active, Graduated, Transferred, Withdrawn.
     */
    private String enrollmentStatus;
}
