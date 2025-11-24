package com.sms.student.dto;

import com.sms.student.enums.EnrollmentReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing an enrollment history event.
 *
 * <p>Tracks student enrollment events including initial enrollments,
 * transfers, and withdrawals from classes.</p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentHistoryDto {

    /**
     * Unique identifier for the enrollment record.
     */
    private UUID id;

    /**
     * UUID of the class.
     */
    private UUID classId;

    /**
     * UUID of the student.
     */
    private UUID studentId;

    /**
     * Student's full name (Latin).
     */
    private String studentName;

    /**
     * Student's full name in Khmer script.
     */
    private String studentNameKhmer;

    /**
     * Student code/ID number.
     */
    private String studentCode;

    /**
     * Reason for enrollment (NEW, TRANSFER, PROMOTION, DEMOTION, CORRECTION).
     */
    private EnrollmentReason reason;

    /**
     * Date when the student enrolled in this class.
     */
    private LocalDate enrollmentDate;

    /**
     * Date when the enrollment ended (null if current).
     */
    private LocalDate endDate;

    /**
     * Whether this is a current (active) enrollment.
     */
    private Boolean isCurrent;

    /**
     * Optional notes about the enrollment.
     */
    private String notes;

    /**
     * Timestamp when the record was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the record was last updated.
     */
    private LocalDateTime updatedAt;
}
