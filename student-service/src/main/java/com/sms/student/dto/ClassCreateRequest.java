package com.sms.student.dto;

import com.sms.student.enums.ClassLevel;
import com.sms.student.enums.ClassShift;
import com.sms.student.enums.ClassType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a new class.
 *
 * <p>Validates required fields and business constraints for class creation.</p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new class")
public class ClassCreateRequest {

    /**
     * School ID (optional - auto-determined from teacher's school association).
     * If not provided, the school will be looked up from the authenticated teacher's school association.
     */
    @Schema(
        description = "UUID of the school this class belongs to (optional - auto-determined from teacher's school)",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID schoolId;

    /**
     * Grade level (required, 1-12).
     * Follows Cambodia's education system: Primary (1-6), Lower Secondary (7-9), Upper Secondary (10-12).
     */
    @NotNull(message = "Grade is required")
    @Min(value = 1, message = "Grade must be between 1 and 12")
    @Max(value = 12, message = "Grade must be between 1 and 12")
    @Schema(
        description = "Grade level (1-12). Cambodia education system: Primary (1-6), Lower Secondary (7-9), Upper Secondary (10-12)",
        example = "7",
        minimum = "1",
        maximum = "12"
    )
    private Integer grade;

    /**
     * Section identifier (required, max 10 characters).
     * Examples: "A", "B", "Science", etc.
     */
    @NotBlank(message = "Section is required")
    @Size(max = 10, message = "Section must not exceed 10 characters")
    @Schema(
        description = "Section identifier (e.g., 'A', 'B', 'Science')",
        example = "A",
        maxLength = 10
    )
    private String section;

    /**
     * Academic year (required).
     * Format: "YYYY-YYYY" where second year equals first year plus one.
     * Example: "2024-2025"
     */
    @NotBlank(message = "Academic year is required")
    @Pattern(
        regexp = "^(\\d{4})-(\\d{4})$",
        message = "Academic year must be in format 'YYYY-YYYY'"
    )
    @Schema(
        description = "Academic year in format 'YYYY-YYYY' where second year = first year + 1",
        example = "2024-2025",
        pattern = "^(\\d{4})-(\\d{4})$"
    )
    private String academicYear;

    /**
     * Maximum student capacity (optional).
     * If not specified, class has unlimited capacity.
     * If specified, must be positive.
     */
    @Min(value = 1, message = "Maximum capacity must be at least 1 if specified")
    @Schema(
        description = "Maximum number of students allowed in this class (optional, unlimited if not specified)",
        example = "40",
        minimum = "1"
    )
    private Integer maxCapacity;

    /**
     * Class level (required).
     * Defines the education level: PRIMARY (1-6), SECONDARY (7-9), HIGH_SCHOOL (10-12).
     */
    @NotNull(message = "Level is required")
    @Schema(
        description = "Class level: PRIMARY (grades 1-6), SECONDARY (grades 7-9), HIGH_SCHOOL (grades 10-12)",
        example = "SECONDARY"
    )
    private ClassLevel level;

    /**
     * Class type (required).
     * Defines the specialization: NORMAL, SCIENCE, or SOCIAL_SCIENCE.
     */
    @NotNull(message = "Type is required")
    @Schema(
        description = "Class type: NORMAL, SCIENCE, or SOCIAL_SCIENCE",
        example = "NORMAL"
    )
    private ClassType type;

    /**
     * Class shift (optional, defaults to MORNING).
     * Defines the time of day: MORNING, AFTERNOON, or FULLDAY.
     */
    @Schema(
        description = "Class shift: MORNING, AFTERNOON, or FULLDAY",
        example = "MORNING"
    )
    private ClassShift shift;
}
