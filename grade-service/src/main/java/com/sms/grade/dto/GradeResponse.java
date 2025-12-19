package com.sms.grade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for a single grade entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeResponse {

    private UUID id;
    private UUID studentId;
    private UUID classId;
    private UUID subjectId;
    private String subjectName;
    private String subjectNameKhmer;
    private UUID assessmentTypeId;
    private String assessmentTypeName;
    private String assessmentTypeCode;
    private BigDecimal score;
    private BigDecimal maxScore;
    private BigDecimal percentage;
    private String letterGrade;
    private Integer semester;
    private String academicYear;
    private String comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
