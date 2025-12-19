package com.sms.grade.dto.event;

import com.sms.grade.enums.AverageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a grade average is calculated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AverageCalculatedEvent {

    private UUID studentId;
    private UUID classId;
    private UUID subjectId;
    private String subjectName;
    private AverageType averageType;
    private BigDecimal averageScore;
    private String letterGrade;
    private Integer classRank;
    private Integer totalStudents;
    private Integer semester;
    private String academicYear;
    private LocalDateTime calculatedAt;
}
