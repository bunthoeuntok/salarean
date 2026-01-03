package com.sms.grade.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
     * Month number (1-12) when this exam occurs.
     */
    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    /**
     * Display order for UI.
     */
    @Min(value = 1, message = "Display order must be positive")
    private Integer displayOrder;
}
