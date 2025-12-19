package com.sms.grade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a student's grades summary for a semester or year.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentGradesSummary {

    private UUID studentId;
    private UUID classId;
    private Integer semester;
    private String academicYear;

    private List<SubjectGrades> subjectGrades;

    private BigDecimal overallMonthlyAverage;
    private BigDecimal overallSemesterAverage;
    private String overallLetterGrade;
    private Integer classRank;
    private Integer totalStudents;

    /**
     * Grades for a single subject.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectGrades {
        private UUID subjectId;
        private String subjectName;
        private String subjectNameKhmer;
        private boolean isCore;

        private List<GradeEntry> monthlyExams;
        private GradeEntry semesterExam;

        private BigDecimal monthlyAverage;
        private BigDecimal semesterAverage;
        private String letterGrade;
        private Integer subjectRank;
    }

    /**
     * Individual grade entry.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeEntry {
        private UUID gradeId;
        private String assessmentName;
        private String assessmentCode;
        private BigDecimal score;
        private BigDecimal maxScore;
        private BigDecimal percentage;
    }
}
