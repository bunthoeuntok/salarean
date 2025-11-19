package com.sms.student.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "classes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(nullable = false)
    private Integer grade;

    @Column(nullable = false)
    private String section;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "student_count")
    private Integer studentCount = 0;

    @Enumerated(EnumType.STRING)
    private ClassStatus status = ClassStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ClassStatus {
        ACTIVE, ARCHIVED
    }
}
