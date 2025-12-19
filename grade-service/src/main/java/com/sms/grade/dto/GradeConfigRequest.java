package com.sms.grade.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating or updating teacher assessment configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeConfigRequest {

    @NotNull(message = "Class ID is required")
    private UUID classId;

    @NotNull(message = "Subject ID is required")
    private UUID subjectId;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be 1 or 2")
    @Max(value = 2, message = "Semester must be 1 or 2")
    private Integer semester;

    @NotBlank(message = "Academic year is required")
    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "Academic year must be in format YYYY-YYYY")
    private String academicYear;

    @NotNull(message = "Monthly exam count is required")
    @Min(value = 1, message = "Monthly exam count must be between 1 and 6")
    @Max(value = 6, message = "Monthly exam count must be between 1 and 6")
    private Integer monthlyExamCount;

    @NotNull(message = "Monthly weight is required")
    @DecimalMin(value = "0.0", message = "Monthly weight must be at least 0")
    @DecimalMax(value = "100.0", message = "Monthly weight must not exceed 100")
    private BigDecimal monthlyWeight;

    @NotNull(message = "Semester exam weight is required")
    @DecimalMin(value = "0.0", message = "Semester weight must be at least 0")
    @DecimalMax(value = "100.0", message = "Semester weight must not exceed 100")
    private BigDecimal semesterExamWeight;
}
