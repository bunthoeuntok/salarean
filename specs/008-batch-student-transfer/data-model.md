# Data Model: Batch Student Transfer with Undo

**Date**: 2025-12-04
**Feature**: 008-batch-student-transfer

## Overview

This document defines the data model for batch student transfer functionality with undo capability. The design extends existing Student Service entities while maintaining backward compatibility.

---

## 1. Database Schema Changes

### 1.1 Enrollment History Table Extension

**Table**: `enrollment_history` (existing table in `student_db` database)

**New Columns**:
```sql
-- Add columns for transfer tracking
ALTER TABLE enrollment_history
ADD COLUMN transfer_id UUID,
ADD COLUMN undo_of_transfer_id UUID,
ADD COLUMN performed_by_user_id UUID NOT NULL DEFAULT 'system';

-- Add index for efficient transfer lookups
CREATE INDEX idx_enrollment_history_transfer_id
ON enrollment_history(transfer_id)
WHERE transfer_id IS NOT NULL;

-- Add index for undo lookups
CREATE INDEX idx_enrollment_history_undo_of_transfer_id
ON enrollment_history(undo_of_transfer_id)
WHERE undo_of_transfer_id IS NOT NULL;
```

**Updated Schema**:
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| id | UUID | NOT NULL | gen_random_uuid() | Primary key |
| student_id | UUID | NOT NULL | - | Reference to students.id |
| class_id | UUID | NOT NULL | - | Reference to classes.id |
| action | VARCHAR(50) | NOT NULL | - | ENROLLED, TRANSFERRED, WITHDRAWN, UNDO |
| reason | TEXT | NULL | - | Human-readable reason for action |
| performed_at | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | When action occurred |
| performed_by_user_id | UUID | NOT NULL | 'system' | User who performed the action |
| transfer_id | UUID | NULL | - | Groups related transfer events |
| undo_of_transfer_id | UUID | NULL | - | References transfer being undone |
| metadata | JSONB | NULL | - | Additional context (old class, new class, etc.) |

**Rationale**:
- Reuses existing audit trail infrastructure
- `transfer_id` links all students in a batch transfer
- `undo_of_transfer_id` tracks which transfer was reversed
- `performed_by_user_id` enables permission checks for undo
- Backward compatible (new columns are nullable or have defaults)

---

## 2. Backend Entities (JPA)

### 2.1 Transfer Request DTO

**File**: `student-service/src/main/java/com/sms/student/dto/BatchTransferRequest.java`

```java
package com.sms.student.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTransferRequest {

    @NotNull(message = "Destination class ID is required")
    private UUID destinationClassId;

    @NotNull(message = "Student IDs are required")
    @Size(min = 1, max = 100, message = "Must transfer between 1 and 100 students")
    private List<UUID> studentIds;
}
```

### 2.2 Transfer Response DTO

**File**: `student-service/src/main/java/com/sms/student/dto/BatchTransferResponse.java`

```java
package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTransferResponse {

    private UUID transferId;
    private UUID sourceClassId;
    private UUID destinationClassId;
    private Integer successfulTransfers;
    private List<FailedTransfer> failedTransfers;
    private LocalDateTime transferredAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedTransfer {
        private UUID studentId;
        private String studentName;
        private String reason; // Error code (e.g., "ALREADY_ENROLLED")
    }
}
```

### 2.3 Undo Response DTO

**File**: `student-service/src/main/java/com/sms/student/dto/UndoTransferResponse.java`

```java
package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UndoTransferResponse {

    private UUID transferId;
    private Integer undoneStudents;
    private UUID sourceClassId;
    private LocalDateTime undoneAt;
}
```

### 2.4 Enrollment History Entity Extension

**File**: `student-service/src/main/java/com/sms/student/model/EnrollmentHistory.java`

```java
package com.sms.student.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "enrollment_history", indexes = {
    @Index(name = "idx_enrollment_history_transfer_id", columnList = "transfer_id"),
    @Index(name = "idx_enrollment_history_undo_of_transfer_id", columnList = "undo_of_transfer_id")
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

    public enum EnrollmentAction {
        ENROLLED,
        TRANSFERRED,
        WITHDRAWN,
        UNDO
    }
}
```

**Rationale**:
- Extends existing entity without breaking changes
- Uses UUID for transfer correlation
- JSONB metadata field for extensibility
- Indexed for efficient transfer/undo queries

---

## 3. Frontend TypeScript Types

### 3.1 Transfer Request Type

**File**: `frontend/src/types/transfer.ts`

```typescript
export interface BatchTransferRequest {
  destinationClassId: string;
  studentIds: string[];
}

export interface BatchTransferResponse {
  transferId: string;
  sourceClassId: string;
  destinationClassId: string;
  successfulTransfers: number;
  failedTransfers: FailedTransfer[];
  transferredAt: string; // ISO 8601 timestamp
}

export interface FailedTransfer {
  studentId: string;
  studentName: string;
  reason: string; // Error code
}

export interface UndoTransferResponse {
  transferId: string;
  undoneStudents: number;
  sourceClassId: string;
  undoneAt: string; // ISO 8601 timestamp
}
```

### 3.2 Undo State Type

**File**: `frontend/src/store/undo-store.ts`

```typescript
export interface UndoState {
  transferId: string;
  sourceClassId: string;
  sourceClassName: string;
  destinationClassId: string;
  destinationClassName: string;
  studentIds: string[];
  studentNames: string[]; // For display in toast
  performedBy: string; // Current user ID
  transferredAt: number; // Unix timestamp (ms)
  expiresAt: number; // transferredAt + 5 minutes
}

export interface UndoStore {
  undoState: UndoState | null;
  setUndoState: (state: UndoState) => void;
  clearUndoState: () => void;
  isUndoAvailable: () => boolean;
}
```

### 3.3 Student Selection Type

**File**: `frontend/src/features/classes/types.ts`

```typescript
export interface StudentRow {
  id: string;
  studentCode: string;
  name: string;
  enrollmentDate: string;
  enrollmentStatus: 'ACTIVE' | 'TRANSFERRED' | 'WITHDRAWN';
  profilePhotoUrl?: string;
}

export interface TransferDialogState {
  open: boolean;
  selectedStudents: StudentRow[];
  destinationClassId: string | null;
  isTransferring: boolean;
  error: string | null;
}
```

---

## 4. Data Flow Diagrams

### 4.1 Batch Transfer Flow

```
┌─────────────┐
│   User      │
│  Selects    │
│  Students   │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  Frontend       │
│  - Validates    │
│  - Opens Dialog │
└────────┬────────┘
         │
         │ POST /api/classes/{id}/students/batch-transfer
         │ { destinationClassId, studentIds }
         ▼
┌────────────────────────┐
│  Student Service       │
│  1. Validate all rules │
│  2. Generate transferId│
│  3. Update enrollments │
│  4. Create history     │
└────────┬───────────────┘
         │
         │ Returns BatchTransferResponse
         ▼
┌─────────────────────────┐
│  Frontend               │
│  1. Invalidate cache    │
│  2. Store undo state    │
│  3. Show toast with btn │
│  4. Start 5min countdown│
└─────────────────────────┘
```

### 4.2 Undo Transfer Flow

```
┌─────────────┐
│   User      │
│  Clicks     │
│  Undo Btn   │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  Frontend       │
│  - Get undoState│
│  - Disable btn  │
└────────┬────────┘
         │
         │ POST /api/transfers/{transferId}/undo
         ▼
┌────────────────────────┐
│  Student Service       │
│  1. Validate timestamp │
│  2. Check conflicts    │
│  3. Check permissions  │
│  4. Reverse enrollments│
│  5. Create undo history│
└────────┬───────────────┘
         │
         │ Returns UndoTransferResponse
         ▼
┌─────────────────────────┐
│  Frontend               │
│  1. Invalidate cache    │
│  2. Clear undo state    │
│  3. Hide toast          │
│  4. Show confirmation   │
└─────────────────────────┘
```

---

## 5. Validation Rules

### 5.1 Transfer Validation

**Pre-Transfer Checks**:
1. All `studentIds` must exist in the database
2. All students must be currently enrolled in `sourceClassId` with `ACTIVE` status
3. `destinationClassId` must exist and be an `ACTIVE` class
4. Source and destination classes must have the same `gradeLevel`
5. Destination class must have capacity: `capacity >= currentEnrollment + transferCount`
6. No student can already be enrolled in `destinationClassId`
7. User must have `TRANSFER_STUDENTS` permission for source class

**Error Codes**:
- `STUDENT_NOT_FOUND` - One or more student IDs do not exist
- `STUDENT_NOT_ENROLLED` - Student is not enrolled in source class
- `CLASS_NOT_FOUND` - Destination class does not exist
- `CLASS_INACTIVE` - Destination class is not active
- `GRADE_MISMATCH` - Source and destination classes have different grade levels
- `CAPACITY_EXCEEDED` - Destination class cannot accommodate transfer
- `ALREADY_ENROLLED` - Student is already enrolled in destination class
- `UNAUTHORIZED` - User does not have transfer permission

### 5.2 Undo Validation

**Pre-Undo Checks**:
1. `transferId` must exist in enrollment_history
2. Transfer must have occurred within last 5 minutes (server time)
3. Current user ID must match `performedByUserId` from transfer
4. No transferred student has a new enrollment record after the transfer timestamp
5. Original source class must still exist and be accessible

**Error Codes**:
- `TRANSFER_NOT_FOUND` - Transfer ID does not exist
- `UNDO_EXPIRED` - More than 5 minutes have elapsed
- `UNDO_UNAUTHORIZED` - Current user did not perform the transfer
- `UNDO_CONFLICT` - One or more students have been transferred again
- `SOURCE_CLASS_NOT_FOUND` - Original source class no longer exists

---

## 6. State Transitions

### 6.1 Student Enrollment Status

```
┌─────────────┐
│   ACTIVE    │ ◄─────────────────┐
│ (in class)  │                   │
└──────┬──────┘                   │
       │                          │
       │ Transfer                 │ Undo
       ▼                          │
┌─────────────┐                   │
│TRANSFERRED  │                   │
│ (left class)│───────────────────┘
└─────────────┘
       │
       │ Enroll in new class
       ▼
┌─────────────┐
│   ACTIVE    │
│(in new class)│
└─────────────┘
```

### 6.2 Undo State Lifecycle

```
NULL (no transfer)
       │
       │ Transfer completes
       ▼
ACTIVE (undo available)
       │
       ├─> EXPIRED (5 min passed) ──> NULL
       │
       ├─> CONFLICT (student moved) ──> NULL
       │
       └─> UNDONE (user clicked undo) ──> NULL
```

---

## 7. Indexing Strategy

**Existing Indexes** (assumed):
- `student_class_enrollments.student_id` (for student lookup)
- `student_class_enrollments.class_id` (for class roster)
- `student_class_enrollments.status` (for active enrollment filter)

**New Indexes**:
```sql
-- For finding all transfers by transferId
CREATE INDEX idx_enrollment_history_transfer_id
ON enrollment_history(transfer_id)
WHERE transfer_id IS NOT NULL;

-- For finding undo records
CREATE INDEX idx_enrollment_history_undo_of_transfer_id
ON enrollment_history(undo_of_transfer_id)
WHERE undo_of_transfer_id IS NOT NULL;

-- For checking recent transfers (undo conflict detection)
CREATE INDEX idx_enrollment_history_performed_at
ON enrollment_history(performed_at DESC, student_id);
```

**Rationale**:
- Partial indexes reduce index size (only rows with non-null transfer_id)
- Composite index on (performed_at, student_id) enables efficient conflict detection
- Descending order on performed_at optimizes recent transfer queries

---

## 8. Data Migration Plan

**Migration File**: `student-service/src/main/resources/db/migration/V8__add_transfer_undo_support.sql`

```sql
-- Add new columns to enrollment_history
ALTER TABLE enrollment_history
ADD COLUMN transfer_id UUID,
ADD COLUMN undo_of_transfer_id UUID,
ADD COLUMN performed_by_user_id UUID;

-- Backfill performed_by_user_id with 'system' UUID for existing records
UPDATE enrollment_history
SET performed_by_user_id = '00000000-0000-0000-0000-000000000000'
WHERE performed_by_user_id IS NULL;

-- Make performed_by_user_id NOT NULL after backfill
ALTER TABLE enrollment_history
ALTER COLUMN performed_by_user_id SET NOT NULL;

-- Create indexes
CREATE INDEX idx_enrollment_history_transfer_id
ON enrollment_history(transfer_id)
WHERE transfer_id IS NOT NULL;

CREATE INDEX idx_enrollment_history_undo_of_transfer_id
ON enrollment_history(undo_of_transfer_id)
WHERE undo_of_transfer_id IS NOT NULL;

CREATE INDEX idx_enrollment_history_performed_at
ON enrollment_history(performed_at DESC, student_id);
```

**Rollback Script** (if needed):
```sql
-- Drop indexes
DROP INDEX IF EXISTS idx_enrollment_history_transfer_id;
DROP INDEX IF EXISTS idx_enrollment_history_undo_of_transfer_id;
DROP INDEX IF EXISTS idx_enrollment_history_performed_at;

-- Remove columns
ALTER TABLE enrollment_history
DROP COLUMN transfer_id,
DROP COLUMN undo_of_transfer_id,
DROP COLUMN performed_by_user_id;
```

---

## Summary

**Database Changes**:
- 3 new columns in `enrollment_history` table
- 3 new indexes for transfer and undo lookups
- Backward compatible (nullable columns, defaults)

**Backend DTOs**:
- `BatchTransferRequest` (validation: 1-100 students)
- `BatchTransferResponse` (includes partial failure details)
- `UndoTransferResponse`
- `EnrollmentHistory` entity extension

**Frontend Types**:
- Transfer request/response interfaces
- Undo state store interface
- Student selection types

**Validation**:
- 7 pre-transfer checks with specific error codes
- 5 pre-undo checks with conflict detection

All data model design complete. Ready for API contract definition.
