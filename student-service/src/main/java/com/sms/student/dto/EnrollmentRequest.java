package com.sms.student.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequest {

    @NotNull(message = "Class ID is required")
    private UUID classId;

    @NotNull(message = "Enrollment date is required")
    private LocalDate enrollmentDate;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
