package com.sms.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Schedule entry for a specific day and period")
public class ScheduleEntryDto {

    @Schema(description = "Entry UUID (null for new entries)")
    private UUID id;

    @NotNull(message = "Day of week is required")
    @Min(value = 1, message = "Day of week must be between 1 (Monday) and 6 (Saturday)")
    @Max(value = 6, message = "Day of week must be between 1 (Monday) and 6 (Saturday)")
    @Schema(description = "Day of week (1=Monday, 6=Saturday)", example = "1")
    private Integer dayOfWeek;

    @NotNull(message = "Period number is required")
    @Min(value = 1, message = "Period number must be at least 1")
    @Schema(description = "Period number", example = "1")
    private Integer periodNumber;

    @NotNull(message = "Subject ID is required")
    @Schema(description = "Subject UUID from grade-service")
    private UUID subjectId;

    @Size(max = 50, message = "Room must not exceed 50 characters")
    @Schema(description = "Room location", example = "101")
    private String room;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    @Schema(description = "Additional notes")
    private String notes;
}
