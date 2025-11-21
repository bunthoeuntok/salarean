package com.sms.student.entity;

import com.sms.student.enums.SchoolType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "schools")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "name_km", length = 255)
    private String nameKhmer;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String province;

    @Column(length = 100)
    private String district;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private SchoolType type;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
