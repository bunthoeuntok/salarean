package com.sms.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Class schedule response")
public class ClassScheduleResponse {

    @Schema(description = "Schedule UUID")
    private UUID id;

    @Schema(description = "Class UUID")
    private UUID classId;

    @Schema(description = "Time slot template UUID")
    private UUID timeSlotTemplateId;

    @Schema(description = "Time slot template details")
    private TimeSlotTemplateResponse template;

    @Schema(description = "Custom time slots (overrides template if set)")
    private List<TimeSlotDto> customSlots;

    @Schema(description = "Effective time slots (custom or from template)")
    private List<TimeSlotDto> effectiveSlots;

    @Schema(description = "Academic year", example = "2024-2025")
    private String academicYear;

    @Schema(description = "Whether schedule is active")
    private Boolean isActive;

    @Schema(description = "Schedule entries by day and period")
    private List<ScheduleEntryResponse> entries;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
