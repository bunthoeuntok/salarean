package com.sms.grade.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for bulk grade entry (multiple students, same assessment).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkGradeRequest {

    @NotNull(message = "Class ID is required")
    private UUID classId;

    @NotNull(message = "Subject ID is required")
    private UUID subjectId;

    @NotNull(message = "Assessment type ID is required")
    private UUID assessmentTypeId;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be 1 or 2")
    @Max(value = 2, message = "Semester must be 1 or 2")
    private Integer semester;

    @NotBlank(message = "Academic year is required")
    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "Academic year must be in format YYYY-YYYY")
    private String academicYear;

    @NotEmpty(message = "At least one student grade is required")
    @Valid
    private List<StudentGradeEntry> grades;

    /**
     * Individual student grade entry within a bulk request.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentGradeEntry {

        @NotNull(message = "Student ID is required")
        private UUID studentId;

        @NotNull(message = "Score is required")
        @DecimalMin(value = "0.0", message = "Score must be at least 0")
        @DecimalMax(value = "100.0", message = "Score must not exceed 100")
        private BigDecimal score;

        private String comments;
    }
}
