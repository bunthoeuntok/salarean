package com.sms.student.model;

import com.sms.student.enums.SchoolType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "schools",
    uniqueConstraints = @UniqueConstraint(
        name = "unique_school_per_district",
        columnNames = {"district_id", "name"}
    )
)
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

    // NEW: Foreign key to provinces table
    @Column(name = "province_id")
    private UUID provinceId;

    // NEW: Foreign key to districts table
    @Column(name = "district_id")
    private UUID districtId;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private SchoolType type;

    // OLD: Deprecated VARCHAR columns (kept for backward compatibility)
    @Deprecated
    @Column(length = 100)
    private String province;

    @Deprecated
    @Column(length = 100)
    private String district;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
