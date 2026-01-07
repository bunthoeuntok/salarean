package com.sms.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Schedule entry response")
public class ScheduleEntryResponse {

    @Schema(description = "Entry UUID")
    private UUID id;

    @Schema(description = "Day of week (1=Monday, 6=Saturday)", example = "1")
    private Integer dayOfWeek;

    @Schema(description = "Period number", example = "1")
    private Integer periodNumber;

    @Schema(description = "Subject UUID from grade-service")
    private UUID subjectId;

    @Schema(description = "Room location", example = "101")
    private String room;

    @Schema(description = "Additional notes")
    private String notes;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
