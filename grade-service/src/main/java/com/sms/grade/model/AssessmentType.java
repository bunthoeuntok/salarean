package com.sms.grade.model;

import com.sms.grade.enums.AssessmentCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a type of assessment (monthly exam, semester exam).
 * Reference data for grade configuration.
 */
@Entity
@Table(name = "assessment_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "name_km", nullable = false, length = 100)
    private String nameKhmer;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AssessmentCategory category;

    @Column(name = "default_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal defaultWeight;

    @Column(name = "max_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal maxScore = BigDecimal.valueOf(100);

    @Column(length = 500)
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if this is a monthly exam type.
     */
    public boolean isMonthlyExam() {
        return category == AssessmentCategory.MONTHLY_EXAM;
    }

    /**
     * Check if this is a semester exam type.
     */
    public boolean isSemesterExam() {
        return category == AssessmentCategory.SEMESTER_EXAM;
    }
}
