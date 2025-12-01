package com.sms.student.dto;

import com.sms.student.enums.ClassLevel;
import com.sms.student.enums.ClassType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing class.
 *
 * <p>All fields are optional - only provided fields will be updated.
 * Validates business constraints when fields are provided.</p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing class (all fields optional)")
public class ClassUpdateRequest {

    /**
     * Grade level (optional, 1-12 if provided).
     * Follows Cambodia's education system: Primary (1-6), Lower Secondary (7-9), Upper Secondary (10-12).
     */
    @Min(value = 1, message = "Grade must be between 1 and 12")
    @Max(value = 12, message = "Grade must be between 1 and 12")
    @Schema(
        description = "Grade level (1-12). Only provide if changing grade",
        example = "8",
        minimum = "1",
        maximum = "12"
    )
    private Integer grade;

    /**
     * Section identifier (optional, max 10 characters if provided).
     * Examples: "A", "B", "Science", etc.
     */
    @Size(max = 10, message = "Section must not exceed 10 characters")
    @Schema(
        description = "Section identifier. Only provide if changing section",
        example = "B",
        maxLength = 10
    )
    private String section;

    /**
     * Academic year (optional).
     * Format: "YYYY-YYYY" where second year equals first year plus one.
     * Example: "2024-2025"
     */
    @Pattern(
        regexp = "^(\\d{4})-(\\d{4})$",
        message = "Academic year must be in format 'YYYY-YYYY'"
    )
    @Schema(
        description = "Academic year in format 'YYYY-YYYY'. Only provide if changing academic year",
        example = "2025-2026",
        pattern = "^(\\d{4})-(\\d{4})$"
    )
    private String academicYear;

    /**
     * Maximum student capacity (optional).
     * If provided, must be positive.
     * Set to null to remove capacity limit.
     */
    @Min(value = 1, message = "Maximum capacity must be at least 1 if specified")
    @Schema(
        description = "Maximum number of students. Only provide if changing capacity",
        example = "45",
        minimum = "1"
    )
    private Integer maxCapacity;

    /**
     * Class level (optional).
     * Defines the education level: PRIMARY (1-6), SECONDARY (7-9), HIGH_SCHOOL (10-12).
     */
    @Schema(
        description = "Class level: PRIMARY (grades 1-6), SECONDARY (grades 7-9), HIGH_SCHOOL (grades 10-12). Only provide if changing level",
        example = "HIGH_SCHOOL"
    )
    private ClassLevel level;

    /**
     * Class type (optional).
     * Defines the specialization: NORMAL, SCIENCE, or SOCIAL_SCIENCE.
     */
    @Schema(
        description = "Class type: NORMAL, SCIENCE, or SOCIAL_SCIENCE. Only provide if changing type",
        example = "SCIENCE"
    )
    private ClassType type;
}
