package com.sms.student.dto;

import com.sms.student.enums.ClassShift;
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
@Schema(description = "Time slot template response")
public class TimeSlotTemplateResponse {

    @Schema(description = "Template UUID")
    private UUID id;

    @Schema(description = "Teacher UUID (null for system defaults)")
    private UUID teacherId;

    @Schema(description = "Template name in English", example = "Morning Standard")
    private String name;

    @Schema(description = "Template name in Khmer", example = "វេនព្រឹកស្តង់ដារ")
    private String nameKm;

    @Schema(description = "Shift type", example = "MORNING")
    private ClassShift shift;

    @Schema(description = "List of time slots")
    private List<TimeSlotDto> slots;

    @Schema(description = "Whether this is a system default template")
    private Boolean isDefault;

    @Schema(description = "Number of periods (excluding breaks)")
    private Integer periodCount;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
