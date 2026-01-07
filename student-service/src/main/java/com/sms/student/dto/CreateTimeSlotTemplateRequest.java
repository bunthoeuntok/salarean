package com.sms.student.dto;

import com.sms.student.enums.ClassShift;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a custom time slot template")
public class CreateTimeSlotTemplateRequest {

    @NotBlank(message = "Template name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Schema(description = "Template name in English", example = "Custom Morning")
    private String name;

    @Size(max = 100, message = "Khmer name must not exceed 100 characters")
    @Schema(description = "Template name in Khmer", example = "វេនព្រឹកផ្ទាល់ខ្លួន")
    private String nameKm;

    @NotNull(message = "Shift is required")
    @Schema(description = "Shift type", example = "MORNING")
    private ClassShift shift;

    @NotEmpty(message = "At least one time slot is required")
    @Valid
    @Schema(description = "List of time slots")
    private List<TimeSlotDto> slots;
}
