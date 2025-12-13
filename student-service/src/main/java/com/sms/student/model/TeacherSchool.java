package com.sms.student.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing the one-to-one association between a teacher (user) and their school.
 * Each teacher can be associated with exactly one school.
 */
@Entity
@Table(name = "teacher_school")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherSchool {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * User ID from auth-service (cross-service reference).
     * Unique constraint ensures one-to-one relationship.
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    /**
     * Reference to the School entity in this service.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "principal_name", nullable = false, length = 255)
    private String principalName;

    @Column(name = "principal_gender", nullable = false, length = 1)
    private String principalGender;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
