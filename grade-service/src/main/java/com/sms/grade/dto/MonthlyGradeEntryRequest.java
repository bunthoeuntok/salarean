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
 * Request DTO for entering monthly exam grades for a class.
 * Allows entering all 4 monthly exams for multiple students at once.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyGradeEntryRequest {

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

    @NotEmpty(message = "At least one student entry is required")
    @Valid
    private List<StudentMonthlyGrades> studentGrades;

    /**
     * Monthly grades for a single student.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentMonthlyGrades {

        @NotNull(message = "Student ID is required")
        private UUID studentId;

        @DecimalMin(value = "0.0", message = "Score must be at least 0")
        @DecimalMax(value = "100.0", message = "Score must not exceed 100")
        private BigDecimal exam1Score;

        @DecimalMin(value = "0.0", message = "Score must be at least 0")
        @DecimalMax(value = "100.0", message = "Score must not exceed 100")
        private BigDecimal exam2Score;

        @DecimalMin(value = "0.0", message = "Score must be at least 0")
        @DecimalMax(value = "100.0", message = "Score must not exceed 100")
        private BigDecimal exam3Score;

        @DecimalMin(value = "0.0", message = "Score must be at least 0")
        @DecimalMax(value = "100.0", message = "Score must not exceed 100")
        private BigDecimal exam4Score;

        private String comments;
    }
}
