package com.sms.student.dto;

import com.sms.student.enums.ClassStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a summary view of an academic class.
 *
 * <p>Used in list views where full class details are not required.
 * Includes current enrollment count for capacity monitoring.</p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSummaryDto {

    /**
     * Unique identifier for the class.
     */
    private UUID id;

    /**
     * School UUID that this class belongs to.
     */
    private UUID schoolId;

    /**
     * UUID of the teacher assigned to this class.
     */
    private UUID teacherId;

    /**
     * Grade level (1-12).
     */
    private Integer grade;

    /**
     * Section identifier (e.g., "A", "B", "1").
     */
    private String section;

    /**
     * Academic year in format YYYY-YYYY (e.g., "2024-2025").
     */
    private String academicYear;

    /**
     * Maximum number of students allowed.
     */
    private Integer maxCapacity;

    /**
     * Current number of enrolled students.
     */
    private Integer studentCount;

    /**
     * Class status (ACTIVE or ARCHIVED).
     */
    private ClassStatus status;

    /**
     * Timestamp when the class was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the class was last updated.
     */
    private LocalDateTime updatedAt;
}
