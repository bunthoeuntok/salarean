# Data Model: Teacher-Based Student Data Isolation

**Feature**: 001-tenant-student-isolation
**Date**: 2025-12-07
**Phase**: 1 (Design & Contracts)

## Overview

This document specifies the data model changes required to implement teacher-based isolation for student records. The core change is adding a `teacher_id` column to the `students` table to establish ownership.

---

## Entity: Student (Modified)

### Current Schema (Before Changes)

```sql
CREATE TABLE students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_code VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    first_name_km VARCHAR(100),
    last_name_km VARCHAR(100),
    date_of_birth DATE NOT NULL,
    gender CHAR(1) NOT NULL,
    photo_url VARCHAR(500),
    address VARCHAR(500),
    emergency_contact VARCHAR(20),
    enrollment_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    -- Soft delete fields
    deletion_reason VARCHAR(500),
    deleted_at TIMESTAMP,
    deleted_by UUID,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);
```

### New Schema (After Changes)

```sql
CREATE TABLE students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_code VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    first_name_km VARCHAR(100),
    last_name_km VARCHAR(100),
    date_of_birth DATE NOT NULL,
    gender CHAR(1) NOT NULL,
    photo_url VARCHAR(500),
    address VARCHAR(500),
    emergency_contact VARCHAR(20),
    enrollment_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    -- NEW: Teacher ownership field
    teacher_id UUID,  -- Nullable initially for backward compatibility

    -- Soft delete fields
    deletion_reason VARCHAR(500),
    deleted_at TIMESTAMP,
    deleted_by UUID,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- NEW: Foreign key constraint (optional - can be added post-migration)
    -- CONSTRAINT fk_students_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id)
);

-- NEW: Index for performance
CREATE INDEX idx_students_teacher_id ON students(teacher_id);
```

### Field Specifications

| Field | Type | Nullable | Default | Description | Changes |
|-------|------|----------|---------|-------------|---------|
| teacher_id | UUID | Yes | NULL | References the teacher who created/owns this student | **NEW** |

**Nullability Decision**:
- **Initially nullable**: Allows migration without breaking existing data
- **Application enforces NOT NULL**: New students created via API will always have teacher_id set
- **Future enhancement**: Can add NOT NULL constraint after backfilling existing students

**Foreign Key Decision**:
- **Not enforced initially**: Student-service doesn't have direct access to teachers table (in auth-service)
- **Referential integrity**: Enforced at application layer via JWT validation
- **Future enhancement**: Could add foreign key if teacher data is replicated to student-service

**Index Decision**:
- **Index on teacher_id**: Required for performant queries filtering by teacher
- **Composite index**: Not needed - most queries will filter by teacher_id alone or with id

---

## Entity: Teacher (Reference Only)

**Note**: Teacher entity exists in `auth-service`, not `student-service`. Included here for reference.

### Schema (Reference)

```sql
-- In auth-service database (auth_db)
CREATE TABLE teachers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    preferred_language VARCHAR(10) NOT NULL DEFAULT 'en',

    -- Account status
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    account_enabled BOOLEAN NOT NULL DEFAULT TRUE,  -- Soft delete flag
    failed_login_attempts INT NOT NULL DEFAULT 0,
    last_failed_login TIMESTAMP,
    locked_until TIMESTAMP,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);
```

**Relationship**:
- `students.teacher_id` → `teachers.id` (logical foreign key, not enforced at DB level)
- Cross-service relationship (student-service → auth-service)

---

## Data Access Patterns

### Read Operations

**Pattern**: All read queries MUST include teacher_id filter

```java
// Repository method
@Query("SELECT s FROM Student s WHERE s.teacherId = :teacherId")
List<Student> findAllByTeacherId(@Param("teacherId") UUID teacherId);

@Query("SELECT s FROM Student s WHERE s.id = :id AND s.teacherId = :teacherId")
Optional<Student> findByIdAndTeacherId(@Param("id") UUID id, @Param("teacherId") UUID teacherId);

@Query("SELECT s FROM Student s WHERE s.teacherId = :teacherId AND s.studentCode = :code")
Optional<Student> findByStudentCodeAndTeacherId(@Param("code") String code, @Param("teacherId") UUID teacherId);
```

**Performance Characteristics**:
- Index on `teacher_id` ensures O(log n) lookup
- Expected query time: < 100ms for teacher with 1000+ students
- Cache hit ratio: ~80% after warm-up (based on typical access patterns)

### Write Operations

**Pattern**: All write operations MUST validate teacher ownership before modification

```java
// Service layer
public StudentResponse updateStudent(UUID studentId, StudentRequest request) {
    UUID teacherId = TeacherContextHolder.getTeacherId();

    Student student = studentRepository.findByIdAndTeacherId(studentId, teacherId)
        .orElseThrow(() -> new UnauthorizedAccessException("Student not found or access denied"));

    // Update fields
    student.setFirstName(request.getFirstName());
    // ... other updates
    student.setUpdatedBy(teacherId);

    return toResponse(studentRepository.save(student));
}
```

**Validation Rules**:
1. **Create**: Auto-assign `teacher_id` from JWT context
2. **Update**: Verify `student.teacher_id == authenticated teacher_id`
3. **Delete**: Verify `student.teacher_id == authenticated teacher_id`
4. **Read**: Filter by `teacher_id` in query

### Cache Patterns

**Cache Key Structure**:
```
students:{teacherId}:all              → List<StudentResponse>
students:{teacherId}:{studentId}      → StudentResponse
```

**Cache Operations**:
```java
// Cache read (automatic via @Cacheable)
@Cacheable(value = "students", key = "#teacherId + ':all'")
public List<StudentResponse> getAllStudents(UUID teacherId) { ... }

// Cache write (automatic via @CachePut)
@CachePut(value = "students", key = "#result.teacherId + ':' + #result.id")
public StudentResponse createStudent(StudentRequest request) { ... }

// Cache eviction (automatic on update/delete)
@CacheEvict(value = "students", key = "#teacherId + ':' + #studentId")
public void deleteStudent(UUID studentId, UUID teacherId) { ... }

// Manual cache reload (user-initiated)
@CacheEvict(value = "students", key = "#teacherId + ':all'")
public void evictTeacherCache(UUID teacherId) { ... }
```

---

## Migration Script

### V6__add_teacher_id_to_students.sql

```sql
-- Migration: Add teacher_id column to students table
-- Date: 2025-12-07
-- Feature: 001-tenant-student-isolation

BEGIN;

-- Step 1: Add teacher_id column (nullable for backward compatibility)
ALTER TABLE students
ADD COLUMN teacher_id UUID;

-- Step 2: Create index for performance
CREATE INDEX idx_students_teacher_id ON students(teacher_id);

-- Step 3: Backfill strategy (choose based on deployment needs)
-- Option A: Leave NULL (requires manual assignment by admins)
-- No action needed - teacher_id remains NULL for existing students

-- Option B: Assign to first teacher (if teachers exist)
-- UPDATE students
-- SET teacher_id = (SELECT id FROM teachers LIMIT 1)
-- WHERE teacher_id IS NULL;

-- Option C: Assign based on created_by field (if populated)
-- UPDATE students
-- SET teacher_id = created_by
-- WHERE teacher_id IS NULL AND created_by IS NOT NULL;

-- Step 4: Add comment for documentation
COMMENT ON COLUMN students.teacher_id IS 'References the teacher who owns/created this student. Used for teacher-based data isolation.';

-- Step 5: (Optional) Add NOT NULL constraint after backfill is complete
-- This should be done in a separate migration after verifying all students have teacher_id
-- ALTER TABLE students ALTER COLUMN teacher_id SET NOT NULL;

-- Step 6: (Optional) Add foreign key constraint if teacher data is replicated
-- This is NOT recommended for cross-service references
-- ALTER TABLE students
-- ADD CONSTRAINT fk_students_teacher
-- FOREIGN KEY (teacher_id) REFERENCES teachers(id)
-- ON DELETE SET NULL;

COMMIT;
```

**Migration Strategy**:
1. **Deploy migration**: Adds nullable column + index
2. **Deploy application code**: Enforces teacher_id on new students
3. **Backfill existing data**: Manual or automated assignment of teacher_id
4. **Optional: Add NOT NULL**: After all students have teacher_id assigned

---

## Validation Rules

### Entity-Level Validation

**Student Entity**:
```java
@Entity
@Table(name = "students")
public class Student {

    @Column(name = "teacher_id")
    private UUID teacherId;  // No @NotNull - enforced at service layer

    // Validation: teacher_id must be present for active students
    @AssertTrue(message = "Active students must have a teacher assigned")
    private boolean isTeacherAssigned() {
        return status == StudentStatus.INACTIVE || teacherId != null;
    }
}
```

### Service-Level Validation

**StudentService**:
```java
@Service
public class StudentService implements IStudentService {

    @Override
    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        UUID teacherId = TeacherContextHolder.getTeacherId();

        Student student = Student.builder()
            .teacherId(teacherId)  // Auto-assign from context
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            // ... other fields
            .createdBy(teacherId)
            .status(StudentStatus.ACTIVE)
            .build();

        return toResponse(studentRepository.save(student));
    }

    @Override
    public StudentResponse getStudent(UUID studentId) {
        UUID teacherId = TeacherContextHolder.getTeacherId();

        Student student = studentRepository.findByIdAndTeacherId(studentId, teacherId)
            .orElseThrow(() -> new UnauthorizedAccessException(
                "Student not found or you don't have permission to access it"
            ));

        return toResponse(student);
    }
}
```

---

## Data Integrity Constraints

### Application-Level Constraints

1. **Teacher Ownership Immutability**:
   - Once assigned, `teacher_id` CANNOT be changed (no student transfers in this feature)
   - Transfer functionality is out of scope (separate administrative feature)

2. **Orphaned Student Prevention**:
   - New students MUST have teacher_id assigned from JWT context
   - Service layer enforces this rule (database constraint is optional)

3. **Soft Delete Preservation**:
   - When teacher account is deactivated, student records remain
   - Teacher `account_enabled = false` does NOT delete students
   - Students can be accessed by reactivating teacher account

### Database-Level Constraints

1. **Index for Performance**:
   - `idx_students_teacher_id` ensures fast teacher-scoped queries
   - Supports query pattern: `WHERE teacher_id = ?`

2. **Optional Foreign Key**:
   - NOT enforced initially (cross-service reference)
   - Can be added if teacher data is replicated to student-service

---

## Performance Considerations

### Query Performance

| Operation | Without Cache | With Cache (Hit) | Target |
|-----------|--------------|------------------|--------|
| List all students (teacher) | ~500ms | ~50ms | <2s (SC-001) |
| Get single student | ~100ms | ~10ms | <200ms |
| Create student | ~200ms | N/A | <500ms |
| Update student | ~200ms | N/A | <500ms |

**Assumptions**:
- Teacher has ~500 students (average)
- Database on SSD storage
- Redis cache hit ratio ~80% after warm-up

### Index Effectiveness

```sql
-- Query plan for list operation
EXPLAIN ANALYZE
SELECT * FROM students WHERE teacher_id = 'uuid-value';

-- Expected plan:
-- Index Scan using idx_students_teacher_id on students (cost=0.43..8.45 rows=1 width=...)
-- Index Cond: (teacher_id = 'uuid-value')
```

**Index Selectivity**:
- Assuming 100 teachers, 50,000 total students
- Average rows per teacher: 500
- Index selectivity: 1/100 = 1% (excellent)

---

## State Transitions

### Student Ownership Lifecycle

```
[New Student Request]
        ↓
[Extract teacher_id from JWT]
        ↓
[Create Student with teacher_id]
        ↓
[Student.status = ACTIVE, teacher_id = {teacher}]
        ↓
[Teacher performs CRUD operations]
        ↓
[Student remains owned by original teacher]
        ↓
(Optional) [Teacher deactivated] → [Student accessible to admins only]
```

**State Invariants**:
1. Active students MUST have non-null teacher_id
2. teacher_id CANNOT be changed after creation (immutable)
3. Soft-deleted students retain teacher_id for audit trail

---

## Summary

| Aspect | Decision | Impact |
|--------|----------|--------|
| **New Field** | `teacher_id UUID` | Establishes ownership |
| **Nullability** | Initially nullable, enforced by app | Backward compatible migration |
| **Index** | `idx_students_teacher_id` | Fast teacher-scoped queries |
| **Foreign Key** | Not enforced (cross-service) | Flexibility over referential integrity |
| **Validation** | Service layer enforcement | Security + business logic coupling |
| **Caching** | Redis with teacher-scoped keys | Performance + data isolation |

**Next Steps**: API contract specification (Phase 1 continued)
