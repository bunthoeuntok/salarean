package com.sms.student.model;

import com.sms.student.enums.ClassShift;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "time_slot_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "teacher_id")
    private UUID teacherId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "name_km", length = 100)
    private String nameKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClassShift shift;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<TimeSlot> slots;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
