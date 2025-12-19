package com.sms.grade.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an individual student grade record.
 * Includes teacher-based data isolation via teacher_id field.
 */
@Entity
@Table(name = "grades",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"student_id", "class_id", "subject_id", "assessment_type_id", "semester", "academic_year"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_type_id", nullable = false)
    private AssessmentType assessmentType;

    @Column(nullable = false)
    private Integer semester;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "max_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal maxScore = BigDecimal.valueOf(100);

    @Column(length = 500)
    private String notes;

    @Column(length = 500)
    private String comments;

    @CreationTimestamp
    @Column(name = "entered_at", nullable = false, updatable = false)
    private LocalDateTime enteredAt;

    /**
     * Alias for enteredAt to match DTO naming.
     */
    public LocalDateTime getCreatedAt() {
        return enteredAt;
    }

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "entered_by", nullable = false)
    private UUID enteredBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    /**
     * Calculate percentage score.
     */
    public BigDecimal getPercentage() {
        if (maxScore == null || maxScore.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return score.multiply(BigDecimal.valueOf(100))
                .divide(maxScore, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Get letter grade based on MoEYS standard.
     */
    public String getLetterGrade() {
        BigDecimal pct = getPercentage();
        if (pct.compareTo(BigDecimal.valueOf(85)) >= 0) return "A";
        if (pct.compareTo(BigDecimal.valueOf(70)) >= 0) return "B";
        if (pct.compareTo(BigDecimal.valueOf(55)) >= 0) return "C";
        if (pct.compareTo(BigDecimal.valueOf(40)) >= 0) return "D";
        if (pct.compareTo(BigDecimal.valueOf(25)) >= 0) return "E";
        return "F";
    }
}
