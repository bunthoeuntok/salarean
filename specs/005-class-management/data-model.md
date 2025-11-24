# Data Model: Class Management API

**Feature**: 005-class-management (student-service extension)
**Date**: 2025-11-24
**Database**: `student_db` (PostgreSQL 15+ - existing)

## Overview

This document defines the data model for class management functionality within student-service. Classes and enrollment history tables will be added to the existing student_db database alongside student tables.

---

## Entity Relationship Diagram

```
┌─────────────────────┐
│   ClassEntity       │
├─────────────────────┤
│ id (UUID) PK        │
│ name                │
│ gradeLevel          │
│ subject             │
│ academicYear        │
│ capacity            │
│ teacherId           │
│ description         │
│ archived            │
│ createdAt           │
│ updatedAt           │
│ version             │
└─────────────────────┘
         │
         │ 1:N
         │
         v
┌──────────────────────┐
│ EnrollmentHistory    │ (READ-ONLY for class management)
├──────────────────────┤
│ id (UUID) PK         │
│ classId FK           │
│ studentId            │
│ eventType            │
│ eventTimestamp       │
│ sourceClassId        │
│ destinationClassId   │
│ performedBy          │
│ notes                │
└──────────────────────┘
```

**Note**: `studentId`, `teacherId`, and `performedBy` are foreign references to existing student and user tables in student_db.

---

## 1. ClassEntity

**Purpose**: Represents an academic class taught by a teacher.

**JPA Entity**:
```java
package com.sms.student.classmanagement.model;

@Entity
@Table(
    name = "classes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_class_name_grade_subject_year",
            columnNames = {"name", "grade_level", "subject", "academic_year"}
        )
    },
    indexes = {
        @Index(name = "idx_classes_teacher_id", columnList = "teacher_id"),
        @Index(name = "idx_classes_archived", columnList = "archived"),
        @Index(name = "idx_classes_teacher_archived", columnList = "teacher_id, archived")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClassEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_level", nullable = false, length = 20)
    private GradeLevel gradeLevel;

    @Column(nullable = false, length = 50)
    private String subject;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;  // Format: "2024-2025"

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean archived = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;  // Optimistic locking
}
```

**Flyway Migration** (`V5__create_classes_table.sql`):
```sql
CREATE TABLE classes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    grade_level VARCHAR(20) NOT NULL,
    subject VARCHAR(50) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity >= 5 AND capacity <= 60),
    teacher_id UUID NOT NULL,
    description VARCHAR(500),
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uk_class_name_grade_subject_year
        UNIQUE (name, grade_level, subject, academic_year)
);

CREATE INDEX idx_classes_teacher_id ON classes(teacher_id);
CREATE INDEX idx_classes_archived ON classes(archived);
CREATE INDEX idx_classes_teacher_archived ON classes(teacher_id, archived);
```

---

## 2. GradeLevel Enum

**Purpose**: Type-safe enumeration of Cambodia education grades (per clarification #2).

```java
package com.sms.student.classmanagement.model;

@Getter
@RequiredArgsConstructor
public enum GradeLevel {
    // Primary Education (6 years)
    GRADE_1("Grade 1", 1),
    GRADE_2("Grade 2", 2),
    GRADE_3("Grade 3", 3),
    GRADE_4("Grade 4", 4),
    GRADE_5("Grade 5", 5),
    GRADE_6("Grade 6", 6),

    // Lower Secondary Education (3 years)
    GRADE_7("Grade 7", 7),
    GRADE_8("Grade 8", 8),
    GRADE_9("Grade 9", 9),

    // Upper Secondary Education (3 years)
    GRADE_10("Grade 10", 10),
    GRADE_11("Grade 11", 11),
    GRADE_12("Grade 12", 12);

    private final String displayName;
    private final int level;
}
```

---

## 3. EnrollmentHistory Entity (READ-ONLY)

**Purpose**: Chronological record of student enrollment events. **Class management feature only reads this data** (per clarification #5).

**JPA Entity**:
```java
package com.sms.student.classmanagement.model;

@Entity
@Table(
    name = "enrollment_history",
    indexes = {
        @Index(name = "idx_enrollment_class_id", columnList = "class_id"),
        @Index(name = "idx_enrollment_student_id", columnList = "student_id"),
        @Index(name = "idx_enrollment_class_timestamp",
               columnList = "class_id, event_timestamp DESC")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EnrollmentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private EnrollmentEventType eventType;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "source_class_id")
    private UUID sourceClassId;

    @Column(name = "destination_class_id")
    private UUID destinationClassId;

    @Column(name = "performed_by", nullable = false)
    private UUID performedBy;

    @Column(length = 500)
    private String notes;
}
```

**Flyway Migration** (`V6__create_enrollment_history_table.sql`):
```sql
CREATE TABLE enrollment_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id UUID NOT NULL,
    student_id UUID NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    event_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    source_class_id UUID,
    destination_class_id UUID,
    performed_by UUID NOT NULL,
    notes VARCHAR(500),

    CONSTRAINT fk_enrollment_class
        FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
);

CREATE INDEX idx_enrollment_class_id ON enrollment_history(class_id);
CREATE INDEX idx_enrollment_student_id ON enrollment_history(student_id);
CREATE INDEX idx_enrollment_class_timestamp
    ON enrollment_history(class_id, event_timestamp DESC);
```

---

## 4. EnrollmentEventType Enum

```java
public enum EnrollmentEventType {
    ENROLLED,
    TRANSFERRED_OUT,
    WITHDRAWN,
    TRANSFERRED_IN
}
```

---

## 5. Repository Interfaces

**ClassRepository**:
```java
@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, UUID> {
    List<ClassEntity> findByTeacherIdAndArchived(UUID teacherId, Boolean archived);
    List<ClassEntity> findByTeacherId(UUID teacherId);
    boolean existsByNameAndGradeLevelAndSubjectAndAcademicYear(
        String name, GradeLevel gradeLevel, String subject, String academicYear);
    Optional<ClassEntity> findByIdAndTeacherId(UUID classId, UUID teacherId);
}
```

**EnrollmentHistoryRepository** (READ-ONLY):
```java
@Repository
public interface EnrollmentHistoryRepository extends JpaRepository<EnrollmentHistory, UUID> {
    List<EnrollmentHistory> findByClassIdOrderByEventTimestampDesc(UUID classId);
}
```

---

## Summary

| Component | Count | Description |
|-----------|-------|-------------|
| Entities | 2 | ClassEntity, EnrollmentHistory (read-only) |
| Enums | 2 | GradeLevel (12 values), EnrollmentEventType (4 values) |
| Tables | 2 | classes, enrollment_history |
| Indexes | 6 | Optimized for teacher queries and timestamp sorting |
| Unique Constraints | 1 | Prevent duplicate classes per academic year |
| Flyway Migrations | 2 | V5 (classes), V6 (enrollment_history) |

**Data Model Status**: ✅ COMPLETE
