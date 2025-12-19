package com.sms.grade.dto;

import com.sms.grade.enums.AverageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for grade calculation results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationResult {

    private UUID studentId;
    private UUID classId;
    private UUID subjectId;
    private String subjectName;
    private Integer semester;
    private String academicYear;
    private AverageType averageType;

    private BigDecimal calculatedScore;
    private String letterGrade;
    private Integer classRank;
    private Integer totalStudents;

    private CalculationDetails details;

    /**
     * Breakdown of how the calculation was performed.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalculationDetails {
        private List<GradeComponent> monthlyExams;
        private GradeComponent semesterExam;
        private BigDecimal monthlyWeight;
        private BigDecimal semesterWeight;
        private BigDecimal monthlyAverage;
        private BigDecimal weightedMonthly;
        private BigDecimal weightedSemester;
        private String formula;
    }

    /**
     * Individual grade component used in calculation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeComponent {
        private String name;
        private BigDecimal score;
        private BigDecimal maxScore;
        private BigDecimal percentage;
    }
}
