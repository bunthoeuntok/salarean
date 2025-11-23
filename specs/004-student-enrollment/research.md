# Research: Student Class Enrollment Management

**Feature**: 004-student-enrollment
**Date**: 2025-11-23
**Purpose**: Technical research and decision documentation for enrollment API implementation

---

## Database Schema Analysis

### Existing Schema Review

**student_class_enrollments** table (created in V3 migration):

```sql
CREATE TABLE IF NOT EXISTS student_class_enrollments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    class_id UUID NOT NULL REFERENCES classes(id) ON DELETE RESTRICT,
    enrollment_date DATE NOT NULL,
    end_date DATE NULL,
    reason VARCHAR(50) NOT NULL DEFAULT 'NEW' CHECK (
        reason IN ('NEW', 'TRANSFER', 'PROMOTION', 'DEMOTION', 'CORRECTION')
    ),
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CHECK (end_date IS NULL OR end_date >= enrollment_date)
);
```

**Key Observations**:
- ✅ Core structure already exists
- ✅ Foreign keys to students and classes tables
- ✅ Audit timestamps (created_at, updated_at)
- ✅ Unique constraint for active enrollments: `idx_enrollment_student_active`
- ❌ **Missing**: No explicit `status` field for lifecycle tracking

**Gap Identified**: The current schema uses `reason` to explain WHY an enrollment was created, but lacks a `status` field to track the CURRENT STATE of the enrollment (active, transferred, withdrawn, completed).

### Schema Enhancement Decision

**Decision**: Add `status` column via new Flyway migration V6

**Rationale**:
- **Separation of Concerns**:
  - `reason` = WHY was this enrollment created? (NEW, TRANSFER, PROMOTION)
  - `status` = WHAT is the current state? (ACTIVE, TRANSFERRED, WITHDRAWN, COMPLETED)
- **Spec Alignment**: Feature spec explicitly requires status tracking (FR-002, FR-004)
- **Query Efficiency**: Status-based queries (e.g., "get all active enrollments") are clearer than derived logic

**Migration Plan**:

```sql
-- V6__add_enrollment_status_field.sql

ALTER TABLE student_class_enrollments
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
CHECK (status IN ('ACTIVE', 'COMPLETED', 'TRANSFERRED', 'WITHDRAWN'));

-- Create index for status queries
CREATE INDEX idx_enrollment_status ON student_class_enrollments(status);

-- Create index for combined status + date queries
CREATE INDEX idx_enrollment_status_date ON student_class_enrollments(status, enrollment_date DESC);
```

**Backward Compatibility**:
- Default value `'ACTIVE'` ensures existing rows get valid status
- Existing `end_date` logic still works (enrollment is active when `end_date IS NULL`)
- No breaking changes to existing code

---

## JPA Entity Design Pattern

### Existing Entity Pattern Analysis

Reviewed existing Student and Class entities in student-service:

**Common Patterns Found**:
1. **Primary Keys**: UUID with `@GeneratedValue(strategy = GenerationType.UUID)`
2. **Lombok**: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
3. **Audit Fields**: `@CreationTimestamp`, `@UpdateTimestamp` for timestamps
4. **Table Naming**: snake_case with `@Table(name = "table_name")`
5. **Enums**: Java enums with `@Enumerated(EnumType.STRING)` for database storage

**Decision**: Enrollment entity will follow the same pattern for consistency.

### Enrollment Entity Design

```java
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
    private Class classEntity;  // "class" is Java keyword

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private EnrollmentReason reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EnrollmentStatus status;  // NEW FIELD

    @Column(name = "notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

---

## Transaction Management

### Transfer Operation Atomicity

**Requirement**: FR-018 states transfer operation MUST be atomic - both marking old enrollment and creating new enrollment must succeed or fail together.

**Research Findings**:
- Spring Boot's `@Transactional` annotation provides declarative transaction management
- Transactions automatically rollback on unchecked exceptions (RuntimeException)
- Database constraints (unique indexes, foreign keys) are enforced within transaction

**Decision**: Use `@Transactional` on service method

**Pattern**:

```java
@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    @Transactional
    public EnrollmentResponse transferStudent(UUID studentId, TransferRequest request) {
        // Step 1: Find and validate active enrollment
        Enrollment currentEnrollment = findActiveEnrollment(studentId);

        // Step 2: Validate target class capacity
        Class targetClass = validateTargetClassCapacity(request.getTargetClassId());

        // Step 3: Mark current enrollment as TRANSFERRED (within transaction)
        currentEnrollment.setStatus(EnrollmentStatus.TRANSFERRED);
        currentEnrollment.setEndDate(LocalDate.now());
        currentEnrollment.setNotes(request.getReason());
        enrollmentRepository.save(currentEnrollment);

        // Step 4: Create new enrollment (within same transaction)
        Enrollment newEnrollment = Enrollment.builder()
            .student(currentEnrollment.getStudent())
            .classEntity(targetClass)
            .enrollmentDate(LocalDate.now())
            .reason(EnrollmentReason.TRANSFER)
            .status(EnrollmentStatus.ACTIVE)
            .notes(request.getReason())
            .build();
        enrollmentRepository.save(newEnrollment);

        // Step 5: Update class student counts (within same transaction)
        updateClassCounts(currentEnrollment.getClassEntity(), targetClass);

        // If any step fails, entire transaction rolls back
        return mapToResponse(newEnrollment);
    }
}
```

**Rollback Scenarios**:
- Target class not found → RuntimeException → Rollback
- Capacity exceeded → RuntimeException → Rollback
- Database constraint violation → SQLException → Rollback
- Any unchecked exception → Rollback

---

## Capacity Validation & Race Condition Prevention

### Problem Statement

**Scenario**: Two administrators simultaneously try to enroll different students in a class with 1 remaining spot.

Without proper locking:
1. Admin A checks capacity: 29/30 (1 slot available) ✅
2. Admin B checks capacity: 29/30 (1 slot available) ✅
3. Admin A enrolls student → capacity becomes 30/30
4. Admin B enrolls student → capacity becomes 31/30 ❌ **VIOLATION**

### Research: Concurrency Control Options

**Option 1: Pessimistic Locking (`SELECT FOR UPDATE`)**
- Locks row until transaction commits
- Guarantees no other transaction can modify
- Can cause deadlocks under high contention

**Option 2: Optimistic Locking (Version Field)**
- No locking, uses version counter
- Transaction fails if version changed during read → write
- Better performance, no deadlocks

**Option 3: Database Constraints**
- Create CHECK constraint: `student_count <= max_capacity`
- Database enforces constraint, transaction fails if violated
- Most reliable, but less user-friendly error messages

### Decision: Hybrid Approach

**Primary**: Optimistic locking with version field (already exists in classes table)

**Fallback**: Database constraint for safety net

**Implementation**:

```java
@Entity
@Table(name = "classes")
public class Class {
    // ... other fields

    @Version
    @Column(name = "version", nullable = false)
    private Long version;  // Already exists in schema

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "student_count", nullable = false)
    private Integer studentCount = 0;
}

// Service layer
@Transactional
public void enrollStudent(UUID studentId, UUID classId) {
    Class classEntity = classRepository.findById(classId)
        .orElseThrow(() -> new ClassNotFoundException(classId));

    // Check capacity (optimistic - may change before commit)
    if (classEntity.getStudentCount() >= classEntity.getMaxCapacity()) {
        throw new ClassCapacityExceededException();
    }

    // Increment student count (version field auto-increments)
    classEntity.setStudentCount(classEntity.getStudentCount() + 1);
    classRepository.save(classEntity);  // Fails if version changed

    // Create enrollment
    // ... (if version check fails, entire transaction rolls back)
}
```

**Rationale**:
- Optimistic locking handles 99% of cases efficiently
- Version mismatch throws `OptimisticLockException` → clear error to retry
- No deadlocks, good performance under normal load

---

## API Response Patterns

### Existing Pattern Review

**Current student-service patterns**:

```java
// ApiResponse<T> wrapper (from sms-common)
public class ApiResponse<T> {
    private Enum<?> errorCode;
    private T data;
}

// StudentErrorCode enum
public enum StudentErrorCode {
    STUDENT_NOT_FOUND,
    INVALID_STUDENT_DATA,
    DUPLICATE_STUDENT_CODE,
    // ... existing codes
}
```

**Decision**: Add enrollment-specific error codes to existing `StudentErrorCode` enum

**New Error Codes**:

```java
public enum StudentErrorCode {
    // Existing codes...

    // Enrollment-specific codes (NEW)
    DUPLICATE_ENROLLMENT,
    ENROLLMENT_NOT_FOUND,
    INVALID_ENROLLMENT_STATUS,
    TRANSFER_REASON_REQUIRED
}
```

**Rationale**: Enrollment is part of student domain, so error codes belong in `StudentErrorCode` rather than creating separate enum.

---

## Performance Considerations

### Enrollment History Query Optimization

**Requirement**: SC-001 states "Administrators can retrieve a student's complete enrollment history in under 2 seconds"

**Challenge**: History query may involve:
- Hundreds of enrollments per student (over multiple years)
- JOIN to classes table for class details
- JOIN to schools table for school name
- Potential N+1 query problem

**Decision**: Use JOIN FETCH to load related entities in single query

**Query Pattern**:

```java
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    @Query("""
        SELECT e FROM Enrollment e
        JOIN FETCH e.classEntity c
        JOIN FETCH c.school
        WHERE e.student.id = :studentId
        ORDER BY e.enrollmentDate DESC, e.createdAt DESC
    """)
    List<Enrollment> findEnrollmentHistoryByStudentId(@Param("studentId") UUID studentId);
}
```

**Rationale**:
- Single query fetches enrollment + class + school in one round trip
- `JOIN FETCH` prevents N+1 query problem
- Existing indexes on `student_id` and `enrollment_date` optimize query
- ORDER BY DESC shows most recent enrollments first (per spec FR-003)

**Performance Testing Plan**:
- Load test with 1000 enrollments per student
- Measure query time (target: <2 seconds per SC-001)
- If needed, add pagination (not in MVP scope per spec)

---

## Alternatives Considered

### Status Field vs. Derived Logic

**Alternative**: Don't add `status` field, derive status from `end_date`:
- `end_date IS NULL` → ACTIVE
- `end_date IS NOT NULL AND reason = 'TRANSFER'` → TRANSFERRED
- etc.

**Rejected Because**:
- Complex query logic for every status check
- Doesn't support COMPLETED vs TRANSFERRED (both have end_date)
- Harder to maintain as status types grow
- Explicit status field is clearer and more performant

### Separate Transfer Table

**Alternative**: Create separate `enrollment_transfers` table to track transfer history

**Rejected Because**:
- YAGNI - current requirements don't need separate table
- Increases complexity without clear benefit
- Transfer history can be derived from enrollment records (old + new)
- Can refactor later if needed

---

## Summary of Technical Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **Status Field** | Add `status` column via V6 migration | Separation of concerns, clearer queries, spec alignment |
| **Entity Pattern** | Follow existing UUID + Lombok pattern | Consistency with existing codebase |
| **Transactions** | `@Transactional` on service methods | Spring's declarative management, automatic rollback |
| **Concurrency Control** | Optimistic locking (version field) | Good performance, no deadlocks, already exists |
| **Error Codes** | Add to `StudentErrorCode` enum | Enrollment is part of student domain |
| **Query Optimization** | JOIN FETCH for history queries | Prevent N+1, single round trip, index support |
| **API Pattern** | `ApiResponse<T>` wrapper | Follows established project convention |

---

**Research Complete**: All technical unknowns resolved. Ready for Phase 1 (Design & Contracts).
