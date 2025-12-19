package com.sms.grade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for teacher assessment configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssessmentConfigResponse {

    private UUID id;
    private UUID classId;
    private UUID subjectId;
    private String subjectName;
    private String subjectNameKhmer;
    private Integer semester;
    private String academicYear;
    private Integer monthlyExamCount;
    private BigDecimal monthlyWeight;
    private BigDecimal semesterExamWeight;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
