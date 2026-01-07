package com.sms.grade.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for exam schedule item within a semester configuration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamScheduleItemDto {

    /**
     * Assessment type code (e.g., MONTHLY_1, MONTHLY_2, SEMESTER)
     */
    @NotBlank(message = "Assessment code is required")
    private String assessmentCode;

    /**
     * Display title for this exam (e.g., "November", "December", or custom text).
     */
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    /**
     * Display order for UI.
     */
    @Min(value = 1, message = "Display order must be positive")
    private Integer displayOrder;
}
