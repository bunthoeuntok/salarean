package com.sms.student.dto;

import com.sms.student.enums.EnrollmentReason;
import com.sms.student.model.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {

    private UUID id;
    private UUID studentId;
    private UUID classId;
    private String className;        // Denormalized for convenience
    private String schoolName;        // Denormalized for convenience
    private LocalDate enrollmentDate;
    private LocalDate endDate;
    private EnrollmentReason reason;
    private EnrollmentStatus status;
    private LocalDate transferDate;
    private String transferReason;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
