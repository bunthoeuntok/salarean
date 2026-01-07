package com.sms.grade.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing semester configuration for exam schedule.
 * Stores which assessment types (monthly exams) belong to each semester
 * and their corresponding months.
 *
 * If teacher_id is NULL, this is the default/system configuration.
 * Teachers can override with their own configuration.
 */
@Entity
@Table(name = "semester_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemesterConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Teacher ID for custom configuration.
     * NULL means this is the default/system configuration for the academic year.
     */
    @Column(name = "teacher_id")
    private UUID teacherId;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    /**
     * Semester exam assessment type code (e.g., SEMESTER_1, SEMESTER_2).
     * References assessment_types.code where category = 'SEMESTER_EXAM'.
     */
    @Column(name = "semester_exam_code", nullable = false, length = 30)
    private String semesterExamCode;

    /**
     * Exam schedule stored as JSON array.
     * Example: [{"assessmentCode": "MONTHLY_1", "title": "November", "displayOrder": 1}, ...]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "exam_schedule", columnDefinition = "jsonb", nullable = false)
    private List<ExamScheduleItem> examSchedule;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if this is a default/system configuration (no teacher assigned).
     */
    public boolean isDefaultConfig() {
        return teacherId == null;
    }

    /**
     * Get the number of monthly exams configured.
     */
    public int getMonthlyExamCount() {
        if (examSchedule == null) return 0;
        return (int) examSchedule.stream()
                .filter(item -> item.getAssessmentCode().startsWith("MONTHLY_"))
                .count();
    }

    /**
     * Inner class representing a single exam schedule item.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExamScheduleItem {
        /**
         * Assessment type code (e.g., MONTHLY_1, MONTHLY_2, SEMESTER)
         */
        private String assessmentCode;

        /**
         * Display title for this exam (e.g., "November", "December", or custom text).
         */
        private String title;

        /**
         * Display order for UI.
         */
        private Integer displayOrder;
    }
}
