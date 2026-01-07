package com.sms.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Time slot definition for class schedules")
public class TimeSlotDto {

    @Schema(description = "Period number (null for breaks)", example = "1")
    private Integer periodNumber;

    @NotBlank(message = "Start time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Start time must be in HH:mm format")
    @Schema(description = "Start time in HH:mm format", example = "07:00")
    private String startTime;

    @NotBlank(message = "End time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "End time must be in HH:mm format")
    @Schema(description = "End time in HH:mm format", example = "07:45")
    private String endTime;

    @Schema(description = "Label in English", example = "Period 1")
    private String label;

    @Schema(description = "Label in Khmer", example = "មុខវិជ្ជាទី១")
    private String labelKm;

    @Schema(description = "Whether this slot is a break", example = "false")
    @Builder.Default
    private Boolean isBreak = false;
}
