package com.sms.grade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for semester configuration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemesterConfigResponse {

    private UUID id;

    /**
     * Teacher ID if this is a custom configuration.
     * Null if this is the default/system configuration.
     */
    private UUID teacherId;

    private String academicYear;

    /**
     * Semester exam assessment type code (e.g., SEMESTER_1, SEMESTER_2).
     */
    private String semesterExamCode;

    private List<ExamScheduleItemDto> examSchedule;

    /**
     * Number of monthly exams in this semester.
     */
    private Integer monthlyExamCount;

    /**
     * Whether this is the default system configuration.
     */
    private Boolean isDefault;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
