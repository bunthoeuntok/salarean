package com.sms.grade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a class's grades summary for a semester.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassGradesSummary {

    private UUID classId;
    private Integer semester;
    private String academicYear;
    private int totalStudents;

    private List<StudentSummary> students;
    private List<SubjectStatistics> subjectStatistics;
    private ClassStatistics classStatistics;

    /**
     * Summary for a single student in the class.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentSummary {
        private UUID studentId;
        private String studentName;
        private BigDecimal semesterAverage;
        private String letterGrade;
        private Integer classRank;
        private List<SubjectScore> subjectScores;
    }

    /**
     * Score for a single subject.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectScore {
        private UUID subjectId;
        private String subjectName;
        private BigDecimal monthlyAverage;
        private BigDecimal semesterExamScore;
        private BigDecimal semesterAverage;
        private String letterGrade;
    }

    /**
     * Statistics for a single subject across the class.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectStatistics {
        private UUID subjectId;
        private String subjectName;
        private BigDecimal classAverage;
        private BigDecimal highestScore;
        private BigDecimal lowestScore;
        private int passCount;
        private int failCount;
        private BigDecimal passRate;
    }

    /**
     * Overall class statistics.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassStatistics {
        private BigDecimal classAverage;
        private BigDecimal highestAverage;
        private BigDecimal lowestAverage;
        private int gradeACount;
        private int gradeBCount;
        private int gradeCCount;
        private int gradeDCount;
        private int gradeECount;
        private int gradeFCount;
        private BigDecimal overallPassRate;
    }
}
