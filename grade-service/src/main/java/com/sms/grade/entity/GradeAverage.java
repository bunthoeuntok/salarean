package com.sms.grade.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "grade_averages", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "subject_id", "academic_year", "semester"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeAverage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Enumerated(EnumType.STRING)
    private Grade.Semester semester;

    @Column(name = "monthly_average", precision = 5, scale = 2)
    private BigDecimal monthlyAverage;

    @Column(name = "semester_exam_score", precision = 5, scale = 2)
    private BigDecimal semesterExamScore;

    @Column(name = "overall_average", precision = 5, scale = 2)
    private BigDecimal overallAverage;

    @Column(name = "letter_grade", length = 2)
    private String letterGrade;

    private Integer rank;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static String calculateLetterGrade(BigDecimal percentage) {
        if (percentage == null) return "F";
        double value = percentage.doubleValue();

        if (value >= 85) return "A";
        if (value >= 70) return "B";
        if (value >= 55) return "C";
        if (value >= 40) return "D";
        if (value >= 25) return "E";
        return "F";
    }
}
