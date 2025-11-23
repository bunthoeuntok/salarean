# Data Model: Student Class Enrollment Management

**Feature**: 004-student-enrollment
**Date**: 2025-11-23
**Purpose**: Entity and DTO specifications for enrollment API implementation

---

## Database Schema

### Existing Tables (No Changes)

**students** table - defined in V1 migration
**classes** table - defined in V1 migration
**student_class_enrollments** table - defined in V3 migration (base structure)

### Schema Enhancement (V6 Migration)

**File**: `V6__add_enrollment_status_field.sql`

```sql
-- Add status field to track enrollment lifecycle
ALTER TABLE student_class_enrollments
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
CHECK (status IN ('ACTIVE', 'COMPLETED', 'TRANSFERRED', 'WITHDRAWN'));

-- Add transfer-specific fields
ALTER TABLE student_class_enrollments
ADD COLUMN transfer_date DATE NULL,
ADD COLUMN transfer_reason VARCHAR(500) NULL;

-- Create index for status queries
CREATE INDEX idx_enrollment_status ON student_class_enrollments(status);

-- Create composite index for status + date queries (history filtering)
CREATE INDEX idx_enrollment_status_date
ON student_class_enrollments(status, enrollment_date DESC);

-- Add constraint: transfer fields required when status is TRANSFERRED
ALTER TABLE student_class_enrollments
ADD CONSTRAINT chk_transfer_fields
CHECK (
    (status = 'TRANSFERRED' AND transfer_date IS NOT NULL AND transfer_reason IS NOT NULL)
    OR
    (status != 'TRANSFERRED')
);
```

**Migration Rationale**:
- Adds `status` field for explicit lifecycle tracking (vs. deriving from `end_date`)
- Adds `transfer_date` and `transfer_reason` to meet FR-013, FR-017 requirements
- Constraint ensures data integrity for transferred enrollments
- Indexes optimize common query patterns (filtering by status, ordering by date)

---

## JPA Entities

### 1. Enrollment Entity (UPDATE EXISTING)

**File**: `student-service/src/main/java/com/sms/student/model/Enrollment.java`

```java
package com.sms.student.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "student_class_enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Class classEntity;  // "class" is Java reserved keyword

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50)
    @Builder.Default
    private EnrollmentReason reason = EnrollmentReason.NEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;  // NEW FIELD

    @Column(name = "transfer_date")
    private LocalDate transferDate;  // NEW FIELD

    @Column(name = "transfer_reason", length = 500)
    private String transferReason;  // NEW FIELD

    @Column(name = "notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Audit fields (to be populated from JWT)
    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;
}
```

**Key Relationships**:
- `@ManyToOne` to Student: Many enrollments per student
- `@ManyToOne` to Class: Many enrollments per class
- `FetchType.LAZY`: Avoid N+1 queries, load on demand

---

### 2. EnrollmentStatus Enum (NEW)

**File**: `student-service/src/main/java/com/sms/student/model/EnrollmentStatus.java`

```java
package com.sms.student.model;

public enum EnrollmentStatus {
    /**
     * Student is currently enrolled in the class
     */
    ACTIVE,

    /**
     * Student completed the class (academic year ended successfully)
     */
    COMPLETED,

    /**
     * Student transferred to another class
     */
    TRANSFERRED,

    /**
     * Student withdrawn from the class (dropped out, expelled, etc.)
     */
    WITHDRAWN
}
```

**Usage**:
- `ACTIVE`: Default status for new enrollments
- `TRANSFERRED`: Set when transfer operation completes (with transfer_date and transfer_reason)
- `COMPLETED`: Set when academic year ends (future automation, not in MVP)
- `WITHDRAWN`: For manual withdrawal operations (not in MVP scope)

---

### 3. EnrollmentReason Enum (EXISTING - Update)

**File**: `student-service/src/main/java/com/sms/student/model/EnrollmentReason.java`

```java
package com.sms.student.model;

public enum EnrollmentReason {
    /**
     * New enrollment (first time joining this class)
     */
    NEW,

    /**
     * Transferred from another class
     */
    TRANSFER,

    /**
     * Promoted from lower grade
     */
    PROMOTION,

    /**
     * Demoted from higher grade
     */
    DEMOTION,

    /**
     * Administrative correction
     */
    CORRECTION
}
```

**Note**: This enum already exists in migration V3. No changes needed.

---

## Data Transfer Objects (DTOs)

### Request DTOs

#### 1. EnrollmentRequest (NEW)

**File**: `student-service/src/main/java/com/sms/student/dto/EnrollmentRequest.java`

**Purpose**: Request body for enrolling a student in a class

```java
package com.sms.student.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequest {

    @NotNull(message = "Class ID is required")
    private UUID classId;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
```

**Validation**:
- `classId`: Required (will validate existence in service layer)
- `notes`: Optional, max 500 characters

**Example JSON**:
```json
{
  "classId": "550e8400-e29b-41d4-a716-446655440000",
  "notes": "Regular enrollment for academic year 2024-2025"
}
```

---

#### 2. TransferRequest (NEW)

**File**: `student-service/src/main/java/com/sms/student/dto/TransferRequest.java`

**Purpose**: Request body for transferring a student to a new class

```java
package com.sms.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotNull(message = "Target class ID is required")
    private UUID targetClassId;

    @NotBlank(message = "Transfer reason is required")
    @Size(max = 500, message = "Transfer reason cannot exceed 500 characters")
    private String reason;
}
```

**Validation**:
- `targetClassId`: Required (will validate existence and capacity in service layer)
- `reason`: Required, max 500 characters (auditable)

**Example JSON**:
```json
{
  "targetClassId": "660e8400-e29b-41d4-a716-446655440001",
  "reason": "Student requested transfer due to scheduling conflict"
}
```

---

### Response DTOs

#### 3. EnrollmentResponse (NEW)

**File**: `student-service/src/main/java/com/sms/student/dto/EnrollmentResponse.java`

**Purpose**: Single enrollment record response

```java
package com.sms.student.dto;

import com.sms.student.model.EnrollmentReason;
import com.sms.student.model.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {

    private UUID id;
    private UUID studentId;
    private UUID classId;
    private String className;        // Denormalized for convenience
    private String schoolName;        // Denormalized for convenience
    private LocalDate enrollmentDate;
    private LocalDate endDate;
    private EnrollmentReason reason;
    private EnrollmentStatus status;
    private LocalDate transferDate;
    private String transferReason;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Example JSON**:
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "studentId": "880e8400-e29b-41d4-a716-446655440003",
  "classId": "550e8400-e29b-41d4-a716-446655440000",
  "className": "Grade 5 - Section A",
  "schoolName": "Phnom Penh Primary School",
  "enrollmentDate": "2024-09-01",
  "endDate": null,
  "reason": "NEW",
  "status": "ACTIVE",
  "transferDate": null,
  "transferReason": null,
  "notes": "Regular enrollment",
  "createdAt": "2024-09-01T08:00:00",
  "updatedAt": "2024-09-01T08:00:00"
}
```

---

#### 4. EnrollmentHistoryResponse (NEW)

**File**: `student-service/src/main/java/com/sms/student/dto/EnrollmentHistoryResponse.java`

**Purpose**: Complete enrollment history for a student

```java
package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentHistoryResponse {

    private List<EnrollmentResponse> enrollments;
    private Integer totalCount;
    private Integer activeCount;      // Count of ACTIVE enrollments
    private Integer completedCount;   // Count of COMPLETED enrollments
    private Integer transferredCount; // Count of TRANSFERRED enrollments
}
```

**Example JSON**:
```json
{
  "enrollments": [
    {
      "id": "770e8400-e29b-41d4-a716-446655440002",
      "studentId": "880e8400-e29b-41d4-a716-446655440003",
      "classId": "550e8400-e29b-41d4-a716-446655440000",
      "className": "Grade 6 - Section A",
      "schoolName": "Phnom Penh Primary School",
      "enrollmentDate": "2025-09-01",
      "endDate": null,
      "reason": "PROMOTION",
      "status": "ACTIVE",
      "transferDate": null,
      "transferReason": null,
      "notes": null,
      "createdAt": "2025-09-01T08:00:00",
      "updatedAt": "2025-09-01T08:00:00"
    },
    {
      "id": "770e8400-e29b-41d4-a716-446655440001",
      "studentId": "880e8400-e29b-41d4-a716-446655440003",
      "classId": "550e8400-e29b-41d4-a716-446655440000",
      "className": "Grade 5 - Section A",
      "schoolName": "Phnom Penh Primary School",
      "enrollmentDate": "2024-09-01",
      "endDate": "2025-06-30",
      "reason": "NEW",
      "status": "COMPLETED",
      "transferDate": null,
      "transferReason": null,
      "notes": "Completed successfully",
      "createdAt": "2024-09-01T08:00:00",
      "updatedAt": "2025-06-30T10:00:00"
    }
  ],
  "totalCount": 2,
  "activeCount": 1,
  "completedCount": 1,
  "transferredCount": 0
}
```

---

## Validation Rules

### Business Validation (Service Layer)

**Enroll Student** (`EnrollmentService.enrollStudent`):
1. Student must exist (throw `StudentNotFoundException` if not found)
2. Class must exist (throw `ClassNotFoundException` if not found)
3. Student must not have active enrollment in the same class (throw `DuplicateEnrollmentException`)
4. Class must have available capacity (throw `ClassCapacityExceededException` if full)

**Transfer Student** (`EnrollmentService.transferStudent`):
1. Student must exist
2. Student must have exactly one active enrollment (throw `EnrollmentNotFoundException` if none)
3. Target class must exist
4. Target class must have available capacity
5. Transfer reason must be provided (validated by DTO `@NotBlank`)
6. Source and target classes must be different (throw `InvalidEnrollmentException`)

**Get Enrollment History** (`EnrollmentService.getEnrollmentHistory`):
1. Student must exist (throw `StudentNotFoundException` if not found)
2. Return empty list if no enrollments (not an error)

---

## Entity Relationships Diagram

```
┌─────────────────┐
│    Student      │
│─────────────────│
│ id (PK)         │
│ student_code    │
│ first_name      │
│ last_name       │
│ ...             │
└────────┬────────┘
         │
         │ 1
         │
         │ N
         │
┌────────▼────────────────────┐
│  Enrollment                 │
│─────────────────────────────│
│ id (PK)                     │
│ student_id (FK) ────────────┤
│ class_id (FK) ──────────┐   │
│ enrollment_date         │   │
│ end_date                │   │
│ reason (ENUM)           │   │
│ status (ENUM) ★NEW      │   │
│ transfer_date ★NEW      │   │
│ transfer_reason ★NEW    │   │
│ notes                   │   │
│ created_at              │   │
│ updated_at              │   │
└─────────────────────────┘   │
                              │
                              │ N
                              │
                              │ 1
                              │
                    ┌─────────▼─────┐
                    │     Class      │
                    │────────────────│
                    │ id (PK)        │
                    │ school_id (FK) │
                    │ grade          │
                    │ section        │
                    │ max_capacity   │
                    │ student_count  │
                    │ version        │
                    │ ...            │
                    └────────────────┘
```

**Key Constraints**:
- One student can have multiple enrollments (historical + current)
- One class can have multiple students enrolled
- Student can only have ONE active enrollment at a time (enforced by unique index)
- Class capacity enforced via optimistic locking (version field)

---

## Data Mapping Strategy

### Entity → DTO Mapping

**Pattern**: Use MapStruct or manual mapping in service layer

**Example** (Manual mapping):

```java
private EnrollmentResponse mapToResponse(Enrollment enrollment) {
    return EnrollmentResponse.builder()
        .id(enrollment.getId())
        .studentId(enrollment.getStudent().getId())
        .classId(enrollment.getClassEntity().getId())
        .className(buildClassName(enrollment.getClassEntity()))
        .schoolName(enrollment.getClassEntity().getSchool().getName())
        .enrollmentDate(enrollment.getEnrollmentDate())
        .endDate(enrollment.getEndDate())
        .reason(enrollment.getReason())
        .status(enrollment.getStatus())
        .transferDate(enrollment.getTransferDate())
        .transferReason(enrollment.getTransferReason())
        .notes(enrollment.getNotes())
        .createdAt(enrollment.getCreatedAt())
        .updatedAt(enrollment.getUpdatedAt())
        .build();
}

private String buildClassName(Class classEntity) {
    return String.format("Grade %d - Section %s",
        classEntity.getGrade(),
        classEntity.getSection());
}
```

**Rationale**:
- Manual mapping gives full control over denormalization
- `className` and `schoolName` provided for UI convenience (avoid extra API calls)
- Can switch to MapStruct later if mapping becomes complex

---

## Summary

**Entities Created/Updated**:
- ✅ `Enrollment` entity (update existing with new fields)
- ✅ `EnrollmentStatus` enum (new)
- ✅ `EnrollmentReason` enum (existing, no changes)

**DTOs Created**:
- ✅ `EnrollmentRequest` (new)
- ✅ `TransferRequest` (new)
- ✅ `EnrollmentResponse` (new)
- ✅ `EnrollmentHistoryResponse` (new)

**Database Changes**:
- ✅ V6 migration adds `status`, `transfer_date`, `transfer_reason` fields
- ✅ New indexes for query optimization
- ✅ Constraint for transfer field integrity

**Validation**:
- ✅ DTO-level validation (Jakarta Bean Validation)
- ✅ Service-level business rules (capacity, duplicates, existence checks)
- ✅ Database-level constraints (foreign keys, check constraints)
