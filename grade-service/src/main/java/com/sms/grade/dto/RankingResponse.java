package com.sms.grade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for class rankings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingResponse {

    private UUID classId;
    private Integer semester;
    private String academicYear;
    private int totalStudents;
    private UUID subjectId;
    private String subjectName;

    private List<StudentRanking> rankings;

    /**
     * Individual student ranking entry.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentRanking {
        private Integer rank;
        private UUID studentId;
        private String studentName;
        private BigDecimal averageScore;
        private String letterGrade;
        private BigDecimal previousAverage;
        private Integer previousRank;
        private Integer rankChange;
    }
}
