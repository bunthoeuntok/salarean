package com.sms.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class TeacherSchoolRequest {

    @NotNull(message = "School ID is required")
    private UUID schoolId;

    @NotBlank(message = "Principal name is required")
    @Size(max = 255, message = "Principal name must not exceed 255 characters")
    private String principalName;

    @NotNull(message = "Principal gender is required")
    @Pattern(regexp = "M|F", message = "Gender must be M or F")
    private String principalGender;
}
