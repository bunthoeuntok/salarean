package com.sms.grade.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a grade is entered.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeEnteredEvent {

    private UUID gradeId;
    private UUID teacherId;
    private UUID studentId;
    private UUID classId;
    private UUID subjectId;
    private String subjectName;
    private String assessmentType;
    private String assessmentCode;
    private BigDecimal score;
    private BigDecimal maxScore;
    private BigDecimal percentage;
    private String letterGrade;
    private Integer semester;
    private String academicYear;
    private LocalDateTime enteredAt;
    private EventType eventType;

    public enum EventType {
        CREATED,
        UPDATED,
        DELETED
    }
}
