package com.sms.grade.model;

import com.sms.grade.enums.AverageType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a pre-calculated grade average.
 * Used for performance optimization and ranking calculations.
 */
@Entity
@Table(name = "grade_averages",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"student_id", "class_id", "subject_id", "semester", "academic_year", "average_type"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeAverage {

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
    @JoinColumn(name = "subject_id")
    private Subject subject;  // Nullable for overall averages

    @Column
    private Integer semester;  // Nullable for annual averages

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "average_type", nullable = false, length = 30)
    private AverageType averageType;

    @Column(name = "average_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal averageScore;

    @Column(name = "letter_grade", nullable = false, length = 2)
    private String letterGrade;

    @Column(name = "class_rank")
    private Integer classRank;

    @Column(name = "subject_rank")
    private Integer subjectRank;

    @Column(name = "total_students")
    private Integer totalStudents;

    @Column(name = "calculated_at", nullable = false)
    @Builder.Default
    private LocalDateTime calculatedAt = LocalDateTime.now();

    /**
     * Calculate and set letter grade based on average score.
     */
    public void calculateLetterGrade() {
        if (averageScore == null) {
            this.letterGrade = "F";
            return;
        }

        if (averageScore.compareTo(BigDecimal.valueOf(85)) >= 0) {
            this.letterGrade = "A";
        } else if (averageScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            this.letterGrade = "B";
        } else if (averageScore.compareTo(BigDecimal.valueOf(55)) >= 0) {
            this.letterGrade = "C";
        } else if (averageScore.compareTo(BigDecimal.valueOf(40)) >= 0) {
            this.letterGrade = "D";
        } else if (averageScore.compareTo(BigDecimal.valueOf(25)) >= 0) {
            this.letterGrade = "E";
        } else {
            this.letterGrade = "F";
        }
    }

    /**
     * Get rank display string (e.g., "12/35").
     */
    public String getRankDisplay() {
        if (classRank == null || totalStudents == null) {
            return "-";
        }
        return classRank + "/" + totalStudents;
    }
}
