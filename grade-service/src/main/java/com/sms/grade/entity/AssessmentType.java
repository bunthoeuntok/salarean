package com.sms.grade.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "assessment_types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "name_km", nullable = false)
    private String nameKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentCategory category;

    @Column(name = "grade_level")
    private Integer gradeLevel;

    @Column(name = "display_order")
    private Integer displayOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum AssessmentCategory {
        MONTHLY, SEMESTER, ANNUAL
    }
}
