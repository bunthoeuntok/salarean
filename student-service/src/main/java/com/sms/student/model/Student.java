package com.sms.student.model;

import com.sms.student.enums.Gender;
import com.sms.student.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "students")
@Where(clause = "status = 'ACTIVE'")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_code", unique = true, nullable = false, length = 50)
    private String studentCode;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "first_name_km", length = 100)
    private String firstNameKhmer;

    @Column(name = "last_name_km", length = 100)
    private String lastNameKhmer;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(length = 500)
    private String address;

    @Column(name = "emergency_contact", length = 20)
    private String emergencyContact;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StudentStatus status = StudentStatus.ACTIVE;

    // Soft delete fields
    @Column(name = "deletion_reason", length = 500)
    private String deletionReason;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    // Teacher-based isolation field
    @Column(name = "teacher_id")
    private UUID teacherId;

    // Relationships
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ParentContact> parentContacts = new ArrayList<>();

    // Convenience methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getFullNameKhmer() {
        if (firstNameKhmer != null && lastNameKhmer != null) {
            return firstNameKhmer + " " + lastNameKhmer;
        }
        return null;
    }

    public Integer getAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public boolean isActive() {
        return status == StudentStatus.ACTIVE;
    }

    // Helper methods for managing parent contacts
    public void addParentContact(ParentContact contact) {
        parentContacts.add(contact);
        contact.setStudent(this);
    }

    public void removeParentContact(ParentContact contact) {
        parentContacts.remove(contact);
        contact.setStudent(null);
    }
}
