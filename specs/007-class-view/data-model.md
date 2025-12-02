# Data Model: Class Detail View

**Feature**: 007-class-view
**Date**: 2025-12-02
**Purpose**: Define data structures for class detail view and student enrollment display

## Overview

This feature primarily consumes existing data models from the student-service. No new database tables are required. The data model defines TypeScript interfaces for frontend consumption and DTO classes for API responses.

---

## Frontend Type Definitions

### ClassDetailResponse

Represents comprehensive information about a class displayed in the page header.

```typescript
interface ClassDetailResponse {
  id: string                    // UUID
  className: string             // e.g., "Mathematics Grade 10A"
  classCode: string             // e.g., "MATH-10A-2024"
  gradeLevel: GradeLevel        // Enum: KINDERGARTEN, PRIMARY_1..12, SECONDARY_1..12
  academicYear: string          // e.g., "2024-2025"
  teacherName: string | null    // Assigned teacher's full name (null if unassigned)
  capacity: number              // Maximum students allowed
  currentEnrollment: number     // Current number of enrolled students
}

enum GradeLevel {
  KINDERGARTEN = 'KINDERGARTEN',
  PRIMARY_1 = 'PRIMARY_1',
  PRIMARY_2 = 'PRIMARY_2',
  PRIMARY_3 = 'PRIMARY_3',
  PRIMARY_4 = 'PRIMARY_4',
  PRIMARY_5 = 'PRIMARY_5',
  PRIMARY_6 = 'PRIMARY_6',
  SECONDARY_7 = 'SECONDARY_7',
  SECONDARY_8 = 'SECONDARY_8',
  SECONDARY_9 = 'SECONDARY_9',
  SECONDARY_10 = 'SECONDARY_10',
  SECONDARY_11 = 'SECONDARY_11',
  SECONDARY_12 = 'SECONDARY_12',
}
```

**Source**: `/api/classes/{id}` (existing endpoint, may need class-service enhancement)

**Usage**: Displayed in class header section above tabs

---

### StudentEnrollmentListResponse

Response containing all students enrolled in a class, with optional status filtering support.

```typescript
interface StudentEnrollmentListResponse {
  students: StudentEnrollmentItem[]  // Array of enrollment items (all students in class)
  totalCount: number                  // Total number of students returned
}
```

**Source**: `/api/classes/{id}/students` (new endpoint in student-service)

**Usage**: Powers student list table (all students displayed in scrollable list, no pagination)

---

### StudentEnrollmentItem

Individual student entry in the class roster with enrollment metadata.

```typescript
interface StudentEnrollmentItem {
  studentId: string             // UUID - Student entity ID
  studentName: string           // Full name (e.g., "Sok Pisey")
  studentCode: string           // Unique code (e.g., "STU-2024-0001")
  photoUrl: string | null       // Profile photo URL (null if no photo uploaded)
  enrollmentDate: string        // ISO 8601 date (e.g., "2024-09-01")
  enrollmentStatus: EnrollmentStatus  // ACTIVE | TRANSFERRED | GRADUATED | WITHDRAWN
}

enum EnrollmentStatus {
  ACTIVE = 'ACTIVE',           // Currently enrolled and attending
  TRANSFERRED = 'TRANSFERRED',  // Moved to another class/school
  GRADUATED = 'GRADUATED',      // Completed this grade level
  WITHDRAWN = 'WITHDRAWN',      // Dropped out or left school
}
```

**Source**: JOIN of `students` and `student_class_enrollments` tables

**Display fields**:
- Photo (avatar or initials if null)
- Student Name
- Student Code
- Enrollment Date (formatted: "September 1, 2024")
- Status badge (color-coded: green/ACTIVE, blue/TRANSFERRED, purple/GRADUATED, gray/WITHDRAWN)

---

### StudentFilters

Client-side state for search and filter controls.

```typescript
interface StudentFilters {
  search: string                // Search term (name or code) - client-side filtering
  status: EnrollmentStatus | null  // Filter by status (null = all) - server-side filtering
  sort: string                  // Sort expression (e.g., "studentName,asc")
}
```

**Default values**:
```typescript
const defaultFilters: StudentFilters = {
  search: '',
  status: null,
  sort: 'studentName,asc'  // Alphabetical by name (clarification decision)
}
```

**Usage**: Managed by TanStack Query hook (`useClassStudents`). Search term applied client-side using TanStack Table's globalFilter for real-time filtering (300ms debounce). Status filter applied server-side.

---

## Backend Entity Schema (Existing)

These entities already exist in `student_db` database. No schema changes required.

### Student (Table: students)

```sql
CREATE TABLE students (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_code VARCHAR(20) UNIQUE NOT NULL,
  full_name VARCHAR(255) NOT NULL,
  date_of_birth DATE NOT NULL,
  gender VARCHAR(10) NOT NULL,
  photo_url VARCHAR(500) NULL,
  phone_number VARCHAR(20) NULL,
  email VARCHAR(255) NULL,
  address TEXT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_students_student_code ON students(student_code);
CREATE INDEX idx_students_full_name ON students(full_name);
```

**Relevant fields for this feature**:
- `id`: Student identifier (mapped to `studentId`)
- `student_code`: Display in table (mapped to `studentCode`)
- `full_name`: Display in table (mapped to `studentName`)
- `photo_url`: Avatar image (mapped to `photoUrl`)

---

### StudentClassEnrollment (Table: student_class_enrollments)

```sql
CREATE TABLE student_class_enrollments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
  class_id UUID NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  enrollment_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  enrollment_reason TEXT NULL,
  transfer_reason TEXT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_student FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT fk_class FOREIGN KEY (class_id) REFERENCES classes(id),
  CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'TRANSFERRED', 'GRADUATED', 'WITHDRAWN'))
);

CREATE INDEX idx_enrollments_class_id ON student_class_enrollments(class_id);
CREATE INDEX idx_enrollments_student_id ON student_class_enrollments(student_id);
CREATE INDEX idx_enrollments_status ON student_class_enrollments(status);
CREATE INDEX idx_enrollments_class_status ON student_class_enrollments(class_id, status);
```

**Relevant fields for this feature**:
- `class_id`: Filter enrollments by class (query parameter)
- `enrollment_date`: Display in table
- `status`: Display badge, filter dropdown

**Query optimization**:
- Composite index `(class_id, status)` for filtered queries
- Index on `class_id` alone for unfiltered queries

---

## Backend DTOs (New)

These DTOs are created in `student-service` for this feature.

### StudentEnrollmentListResponse.java

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
public class StudentEnrollmentListResponse {
    private List<StudentEnrollmentItem> students;
    private int totalCount;
}
```

### StudentEnrollmentItem.java

```java
package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentEnrollmentItem {
    private String studentId;       // UUID as string
    private String studentName;     // Student's full name
    private String studentCode;     // Student's unique code
    private String photoUrl;        // Nullable
    private LocalDate enrollmentDate;
    private String enrollmentStatus; // ACTIVE | TRANSFERRED | GRADUATED | WITHDRAWN
}
```

---

## State Transitions

### EnrollmentStatus Lifecycle

```
ACTIVE (initial state)
  ↓
  ├── TRANSFERRED (moved to different class/school)
  ├── GRADUATED (completed grade level)
  └── WITHDRAWN (left school)
```

**Rules**:
- **Initial state**: All new enrollments start as `ACTIVE`
- **Terminal states**: TRANSFERRED, GRADUATED, WITHDRAWN (no transitions out)
- **Business logic**: Once a student leaves ACTIVE status, they cannot return to ACTIVE in the same class (must create new enrollment)

**Status meanings**:
- **ACTIVE**: Student is currently enrolled and attending this class
- **TRANSFERRED**: Student moved to another class in same school OR transferred to different school
- **GRADUATED**: Student successfully completed this grade level and moved up
- **WITHDRAWN**: Student dropped out, moved away, or otherwise left the school

---

## Relationships

### Entity Relationship Diagram

```
┌─────────────┐         ┌──────────────────────────┐         ┌─────────────┐
│  Student    │         │ StudentClassEnrollment   │         │   Class     │
├─────────────┤         ├──────────────────────────┤         ├─────────────┤
│ id (PK)     │◄───────┤ student_id (FK)          │         │ id (PK)     │
│ student_code│         │ class_id (FK)            ├────────►│ class_code  │
│ full_name   │         │ enrollment_date          │         │ class_name  │
│ photo_url   │         │ status                   │         │ grade_level │
│ ...         │         │ ...                      │         │ ...         │
└─────────────┘         └──────────────────────────┘         └─────────────┘
     1                           N                                  1
```

**Key relationships**:
- One student can have multiple enrollments (historical + current)
- One class can have multiple student enrollments
- For this feature, we filter by `class_id` to get all enrollments for a specific class

---

## Validation Rules

### Frontend Validation (Zod)

```typescript
import { z } from 'zod'

const studentFiltersSchema = z.object({
  search: z.string().max(100).optional().default(''),
  status: z.enum(['ACTIVE', 'TRANSFERRED', 'GRADUATED', 'WITHDRAWN']).nullable().optional(),
  sort: z.string().regex(/^[a-zA-Z]+,(asc|desc)$/).optional().default('studentName,asc'),
})
```

### Backend Validation (Jakarta Bean Validation)

```java
// In ClassController.java
@GetMapping("/{classId}/students")
public ApiResponse<StudentEnrollmentListResponse> getStudentsByClass(
    @PathVariable UUID classId,
    @RequestParam(required = false) @Pattern(regexp = "ACTIVE|TRANSFERRED|GRADUATED|WITHDRAWN") String status,
    @RequestParam(defaultValue = "studentName,asc") String sort
) {
    // Implementation
}
```

---

## Performance Considerations

### Database Queries

**Primary query** (for `/api/classes/{id}/students`):
```sql
SELECT
  s.id, s.student_code, s.full_name, s.photo_url,
  e.enrollment_date, e.status
FROM student_class_enrollments e
JOIN students s ON e.student_id = s.id
WHERE e.class_id = ?
  AND (? IS NULL OR e.status = ?)               -- Status filter (server-side)
ORDER BY s.full_name ASC;                        -- Default sort
```

**Note**: Search filtering (by name/code) is performed client-side using TanStack Table's `globalFilter` for instant real-time filtering with 300ms debounce. This avoids unnecessary API calls for typical class sizes (10-100 students).

**Expected performance**:
- **Without filters**: <50ms (indexed on `class_id`)
- **With status filter**: <60ms (composite index `class_id, status`)

### Caching Strategy

**TanStack Query configuration**:
```typescript
const { data, isLoading } = useQuery({
  queryKey: ['class-students', classId, filters],
  queryFn: () => getClassStudents(classId, filters),
  staleTime: 5 * 60 * 1000,  // 5 minutes
  gcTime: 10 * 60 * 1000,     // 10 minutes (formerly cacheTime)
})
```

**Rationale**:
- Student enrollment changes infrequently (not real-time requirement)
- 5-minute cache reduces API calls for repeated views
- Cache invalidated on enrollment mutations (if implemented in future)

---

## Summary

**No database schema changes required** - feature uses existing tables with proper indexes.

**New DTOs created**:
- `StudentEnrollmentListResponse` (list wrapper with totalCount)
- `StudentEnrollmentItem` (enrollment data for display)

**TypeScript interfaces mirror DTOs** for type safety across frontend/backend boundary.

**Performance optimized** with composite indexes and TanStack Query caching. Search filtering performed client-side for instant real-time feedback.
