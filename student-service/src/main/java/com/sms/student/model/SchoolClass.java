package com.sms.student.model;

import com.sms.student.enums.ClassLevel;
import com.sms.student.enums.ClassShift;
import com.sms.student.enums.ClassStatus;
import com.sms.student.enums.ClassType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "classes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"school_id", "grade", "section", "academic_year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(nullable = false)
    @Min(1)
    @Max(12)
    private Integer grade;

    @Column(nullable = false, length = 10)
    private String section;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "student_count", nullable = false)
    @Builder.Default
    private Integer studentCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ClassLevel level = ClassLevel.PRIMARY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ClassType type = ClassType.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ClassShift shift = ClassShift.MORNING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ClassStatus status = ClassStatus.ACTIVE;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Business logic methods
    public boolean hasCapacity() {
        return maxCapacity == null || studentCount < maxCapacity;
    }

    public void incrementEnrollment() {
        studentCount++;
    }

    public void decrementEnrollment() {
        if (studentCount > 0) {
            studentCount--;
        }
    }

    public boolean isActive() {
        return status == ClassStatus.ACTIVE;
    }
}
