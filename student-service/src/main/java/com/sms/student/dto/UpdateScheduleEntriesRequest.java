package com.sms.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update schedule entries")
public class UpdateScheduleEntriesRequest {

    @NotEmpty(message = "At least one entry is required")
    @Valid
    @Schema(description = "List of schedule entries to update or create")
    private List<ScheduleEntryDto> entries;

    @Schema(description = "Whether to clear all existing entries before adding new ones", example = "false")
    @Builder.Default
    private Boolean clearExisting = false;
}
