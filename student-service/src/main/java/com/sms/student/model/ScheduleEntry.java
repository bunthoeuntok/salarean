package com.sms.student.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "schedule_entries", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"class_schedule_id", "day_of_week", "period_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_schedule_id", nullable = false)
    private ClassSchedule classSchedule;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;  // 1=Monday, 6=Saturday

    @Column(name = "period_number", nullable = false)
    private Integer periodNumber;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(length = 50)
    private String room;

    @Column(length = 255)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
