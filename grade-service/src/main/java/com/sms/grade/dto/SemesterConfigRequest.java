package com.sms.grade.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating or updating semester configuration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemesterConfigRequest {

    @NotBlank(message = "Academic year is required")
    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "Academic year must be in format YYYY-YYYY")
    private String academicYear;

    /**
     * Semester exam assessment type code (e.g., SEMESTER_1, SEMESTER_2).
     * Must reference an existing assessment_type with category = 'SEMESTER_EXAM'.
     */
    @NotBlank(message = "Semester exam code is required")
    @Size(max = 30, message = "Semester exam code must not exceed 30 characters")
    private String semesterExamCode;

    @NotNull(message = "Exam schedule is required")
    @NotEmpty(message = "Exam schedule must have at least one item")
    @Size(max = 10, message = "Exam schedule cannot have more than 10 items")
    @Valid
    private List<ExamScheduleItemDto> examSchedule;
}
