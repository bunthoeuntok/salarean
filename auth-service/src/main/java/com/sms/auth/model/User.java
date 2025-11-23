package com.sms.auth.model;

import com.sms.common.validation.KhmerPhone;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @KhmerPhone
    @Column(name = "phone_number", unique = true, nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 255)
    private String name;

    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    @Column(name = "profile_photo_uploaded_at")
    private LocalDateTime profilePhotoUploadedAt;

    @Builder.Default
    @Column(name = "preferred_language", length = 2)
    private String preferredLanguage = "en"; // 'en' or 'km'

    @Builder.Default
    @Column(name = "account_status", length = 20)
    private String accountStatus = "active"; // 'active', 'inactive', 'locked'

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
