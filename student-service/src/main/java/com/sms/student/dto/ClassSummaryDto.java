package com.sms.student.dto;

import com.sms.student.enums.ClassLevel;
import com.sms.student.enums.ClassShift;
import com.sms.student.enums.ClassStatus;
import com.sms.student.enums.ClassType;
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
     * Display name for the class (e.g., "Grade 10 - Section A").
     */
    private String name;

    /**
     * Grade level as string (e.g., "10", "11", "12").
     */
    private String grade;

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
     * Class shift (MORNING, AFTERNOON, or FULLDAY).
     */
    private ClassShift shift;

    /**
     * Class status (ACTIVE, INACTIVE, or COMPLETED).
     */
    private ClassStatus status;

    /**
     * UUID of the teacher assigned to this class.
     */
    private UUID teacherId;

    /**
     * Name of the teacher assigned to this class.
     */
    private String teacherName;

    /**
     * Timestamp when the class was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the class was last updated.
     */
    private LocalDateTime updatedAt;
}
