package com.sms.student.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing enrollment history records.
 * Tracks all enrollment changes including transfers and undos.
 */
@Entity
@Table(name = "enrollment_history", indexes = {
    @Index(name = "idx_enrollment_history_transfer_id", columnList = "transfer_id"),
    @Index(name = "idx_enrollment_history_undo_of_transfer_id", columnList = "undo_of_transfer_id"),
    @Index(name = "idx_enrollment_history_performed_at", columnList = "performed_at DESC, student_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private UUID classId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EnrollmentAction action;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime performedAt;

    @Column(nullable = false)
    private UUID performedByUserId;

    @Column
    private UUID transferId;

    @Column
    private UUID undoOfTransferId;

    @Column(columnDefinition = "JSONB")
    private String metadata; // Stores additional context as JSON

    /**
     * Enrollment action types
     */
    public enum EnrollmentAction {
        /** Student enrolled in a class */
        ENROLLED,

        /** Student transferred between classes */
        TRANSFERRED,

        /** Student withdrawn from a class */
        WITHDRAWN,

        /** Transfer operation was undone */
        UNDO
    }
}
