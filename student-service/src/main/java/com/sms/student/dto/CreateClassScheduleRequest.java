package com.sms.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a class schedule")
public class CreateClassScheduleRequest {

    @NotNull(message = "Class ID is required")
    @Schema(description = "Class UUID")
    private UUID classId;

    @Schema(description = "Time slot template UUID (use default for class shift if not provided)")
    private UUID timeSlotTemplateId;

    @Valid
    @Schema(description = "Custom time slots (optional, overrides template)")
    private List<TimeSlotDto> customSlots;

    @NotBlank(message = "Academic year is required")
    @Pattern(regexp = "^(\\d{4})-(\\d{4})$", message = "Academic year must be in format 'YYYY-YYYY'")
    @Schema(description = "Academic year", example = "2024-2025")
    private String academicYear;
}
