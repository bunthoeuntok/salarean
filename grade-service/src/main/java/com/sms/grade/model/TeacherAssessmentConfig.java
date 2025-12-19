package com.sms.grade.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing teacher's customized assessment configuration.
 * Allows teachers to customize the number of monthly exams and weights per class/semester.
 */
@Entity
@Table(name = "teacher_assessment_config",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"teacher_id", "class_id", "subject_id", "semester", "academic_year"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherAssessmentConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private Integer semester;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "monthly_exam_count", nullable = false)
    @Builder.Default
    private Integer monthlyExamCount = 4;

    @Column(name = "monthly_weight", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal monthlyWeight = BigDecimal.valueOf(50.00);

    @Column(name = "semester_weight", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal semesterWeight = BigDecimal.valueOf(50.00);

    /**
     * Alias for semesterWeight to match DTO naming.
     */
    public BigDecimal getSemesterExamWeight() {
        return semesterWeight;
    }

    /**
     * Alias setter for semesterWeight.
     */
    public void setSemesterExamWeight(BigDecimal weight) {
        this.semesterWeight = weight;
    }

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Calculate the weight per individual monthly exam.
     */
    public BigDecimal getWeightPerMonthlyExam() {
        if (monthlyExamCount == null || monthlyExamCount == 0) {
            return BigDecimal.ZERO;
        }
        return monthlyWeight.divide(BigDecimal.valueOf(monthlyExamCount), 2, java.math.RoundingMode.HALF_UP);
    }
}
