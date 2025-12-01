package com.sms.student.dto;

import com.sms.student.enums.ClassLevel;
import com.sms.student.enums.ClassStatus;
import com.sms.student.enums.ClassType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing detailed information about an academic class.
 *
 * <p>Includes all class information plus the roster of enrolled students.
 * Used when viewing a specific class's complete profile.</p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassDetailDto {

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
     * Class level (PRIMARY, SECONDARY, or HIGH_SCHOOL).
     */
    private ClassLevel level;

    /**
     * Class type (NORMAL, SCIENCE, or SOCIAL_SCIENCE).
     */
    private ClassType type;

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

    /**
     * List of students currently enrolled in this class.
     */
    private List<StudentRosterItemDto> students;
}
