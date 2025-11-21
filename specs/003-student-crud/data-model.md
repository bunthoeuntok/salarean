# Data Model: Student CRUD Operations

**Feature**: 003-student-crud
**Date**: 2025-11-22
**Database**: student_db (PostgreSQL 15+)

---

## Overview

This document defines the complete data model for the Student CRUD Operations feature, including entity definitions, relationships, constraints, indexes, and state transitions. The schema is implemented via Flyway migrations and maps directly to JPA entities in the `student-service`.

---

## Entity Relationship Diagram

```
┌─────────────┐
│   School    │
└──────┬──────┘
       │ 1:N
       │
┌──────▼──────┐         ┌────────────────────────┐
│    Class    │◄────N:M──►│ StudentClassEnrollment │
└──────┬──────┘         └───────────┬────────────┘
       │                            │
       │ references                 │ N:1
       │                            │
       │                  ┌─────────▼────────┐
       │                  │     Student      │
       │                  └─────────┬────────┘
       │                            │ 1:N
       │                            │
       │                  ┌─────────▼──────────┐
       │                  │   ParentContact     │
       │                  └────────────────────┘
       │
       └──references───► teacher_id (from auth-service)
```

---

## Core Entities

### 1. School

**Purpose**: Reference entity for school information (read-only for student-service)

**Table**: `schools`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique school identifier |
| `name` | VARCHAR(255) | NOT NULL | School name (English) |
| `name_km` | VARCHAR(255) | | School name (Khmer) |
| `address` | VARCHAR(500) | | Physical address |
| `province` | VARCHAR(100) | | Province name |
| `district` | VARCHAR(100) | | District name |
| `type` | VARCHAR(50) | NOT NULL | School type (PRIMARY, SECONDARY, etc.) |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**JPA Entity**:
```java
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

public enum SchoolType {
    PRIMARY,
    SECONDARY,
    HIGH_SCHOOL,
    VOCATIONAL
}
```

---

### 2. Class

**Purpose**: Represents a classroom/grade section that students are enrolled in

**Table**: `classes`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique class identifier |
| `school_id` | UUID | NOT NULL, FOREIGN KEY (schools.id) | Reference to school |
| `teacher_id` | UUID | NOT NULL | Teacher user ID (from auth-service) |
| `grade` | INTEGER | NOT NULL, CHECK (grade BETWEEN 1 AND 12) | Grade level (1-12) |
| `section` | VARCHAR(10) | NOT NULL | Section identifier (A, B, C, etc.) |
| `academic_year` | VARCHAR(20) | NOT NULL | Academic year (e.g., "2024-2025") |
| `max_capacity` | INTEGER | | Maximum student capacity (null = unlimited) |
| `student_count` | INTEGER | DEFAULT 0 | Current enrollment count |
| `status` | VARCHAR(20) | DEFAULT 'ACTIVE' | Class status (ACTIVE, ARCHIVED) |
| `version` | BIGINT | NOT NULL, DEFAULT 0 | Optimistic locking version |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**Constraints**:
- `UNIQUE(school_id, grade, section, academic_year)`: No duplicate class sections per year

**Indexes**:
```sql
CREATE INDEX idx_classes_teacher ON classes(teacher_id);
CREATE INDEX idx_classes_school ON classes(school_id);
```

**JPA Entity**:
```java
@Entity
@Table(name = "classes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"school_id", "grade", "section", "academic_year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Class {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(nullable = false)
    @Min(1)
    @Max(12)
    private Integer grade;

    @Column(nullable = false, length = 10)
    private String section;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "student_count", nullable = false)
    private Integer studentCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClassStatus status = ClassStatus.ACTIVE;

    @Version  // Optimistic locking
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Business logic
    public boolean hasCapacity() {
        return maxCapacity == null || studentCount < maxCapacity;
    }

    public void incrementEnrollment() {
        studentCount++;
    }

    public void decrementEnrollment() {
        if (studentCount > 0) {
            studentCount--;
        }
    }
}

public enum ClassStatus {
    ACTIVE,
    ARCHIVED
}
```

---

### 3. Student

**Purpose**: Core entity representing a student's demographic and enrollment information

**Table**: `students`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique student identifier |
| `student_code` | VARCHAR(50) | UNIQUE, NOT NULL | Human-readable student code (e.g., "2025-S-00001") |
| `first_name` | VARCHAR(100) | NOT NULL | First name (English) |
| `last_name` | VARCHAR(100) | NOT NULL | Last name (English) |
| `first_name_km` | VARCHAR(100) | | First name (Khmer) |
| `last_name_km` | VARCHAR(100) | | Last name (Khmer) |
| `date_of_birth` | DATE | NOT NULL | Date of birth |
| `gender` | VARCHAR(1) | NOT NULL, CHECK (gender IN ('M', 'F')) | Gender (M=Male, F=Female) |
| `photo_url` | VARCHAR(500) | | Photo file path or URL |
| `parent_name` | VARCHAR(255) | | Primary parent/guardian name (deprecated: use parent_contacts table) |
| `parent_phone` | VARCHAR(20) | | Primary parent phone (deprecated: use parent_contacts table) |
| `emergency_contact` | VARCHAR(20) | | Emergency contact number |
| `address` | VARCHAR(500) | | Home address |
| `enrollment_date` | DATE | NOT NULL | Initial enrollment date |
| `status` | VARCHAR(20) | DEFAULT 'ACTIVE' | Student status (ACTIVE, INACTIVE) |
| `deletion_reason` | VARCHAR(500) | | Soft delete reason (GRADUATED, TRANSFERRED, WITHDREW) |
| `deleted_at` | TIMESTAMP | | Soft delete timestamp |
| `deleted_by` | UUID | | User who soft-deleted (from auth-service) |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update timestamp |
| `created_by` | UUID | | User who created record |
| `updated_by` | UUID | | User who last updated record |

**Indexes**:
```sql
CREATE INDEX idx_students_code ON students(student_code);
CREATE INDEX idx_students_status ON students(status);
CREATE INDEX idx_students_deleted_at ON students(deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX idx_students_fulltext ON students USING GIN (
    to_tsvector('simple', COALESCE(first_name, '') || ' ' || COALESCE(last_name, '') || ' ' ||
                           COALESCE(first_name_km, '') || ' ' || COALESCE(last_name_km, ''))
);
```

**JPA Entity**:
```java
@Entity
@Table(name = "students")
@Where(clause = "status = 'ACTIVE'")  // Default filter for active students
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_code", unique = true, nullable = false, length = 50)
    private String studentCode;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "first_name_km", length = 100)
    private String firstNameKhmer;

    @Column(name = "last_name_km", length = 100)
    private String lastNameKhmer;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(length = 500)
    private String address;

    @Column(name = "emergency_contact", length = 20)
    private String emergencyContact;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentStatus status = StudentStatus.ACTIVE;

    @Column(name = "deletion_reason", length = 500)
    private String deletionReason;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    // Relationships
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParentContact> parentContacts = new ArrayList<>();

    // Convenience methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getFullNameKhmer() {
        if (firstNameKhmer != null && lastNameKhmer != null) {
            return firstNameKhmer + " " + lastNameKhmer;
        }
        return null;
    }

    public Integer getAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public boolean isActive() {
        return status == StudentStatus.ACTIVE;
    }
}

public enum StudentStatus {
    ACTIVE,
    INACTIVE
}

public enum Gender {
    M,  // Male
    F   // Female
}
```

---

### 4. ParentContact

**Purpose**: Stores parent/guardian contact information for students

**Table**: `parent_contacts` (not explicitly defined in provided schema, inferred from spec)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique contact identifier |
| `student_id` | UUID | NOT NULL, FOREIGN KEY (students.id) ON DELETE CASCADE | Reference to student |
| `full_name` | VARCHAR(255) | NOT NULL | Parent/guardian full name |
| `phone_number` | VARCHAR(20) | NOT NULL | Contact phone number |
| `relationship` | VARCHAR(50) | NOT NULL | Relationship type (MOTHER, FATHER, GUARDIAN, OTHER) |
| `is_primary` | BOOLEAN | DEFAULT FALSE | Primary contact flag |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**Indexes**:
```sql
CREATE INDEX idx_parent_contacts_student ON parent_contacts(student_id);
```

**JPA Entity**:
```java
@Entity
@Table(name = "parent_contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentContact {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "phone_number", nullable = false, length = 20)
    @Pattern(regexp = "^\\+855\\d{8,9}$", message = "Invalid Cambodia phone format")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Relationship relationship;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

public enum Relationship {
    MOTHER,
    FATHER,
    GUARDIAN,
    OTHER
}
```

---

### 5. StudentClassEnrollment

**Purpose**: Junction table tracking complete enrollment history for students across classes

**Table**: `student_class_enrollments`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique enrollment identifier |
| `student_id` | UUID | NOT NULL, FOREIGN KEY (students.id) ON DELETE CASCADE | Reference to student |
| `class_id` | UUID | NOT NULL, FOREIGN KEY (classes.id) ON DELETE RESTRICT | Reference to class |
| `enrollment_date` | DATE | NOT NULL | Enrollment start date |
| `end_date` | DATE | CHECK (end_date IS NULL OR end_date >= enrollment_date) | Enrollment end date (null = currently enrolled) |
| `reason` | VARCHAR(50) | NOT NULL, DEFAULT 'NEW', CHECK (...) | Enrollment reason (NEW, TRANSFER, PROMOTION, etc.) |
| `notes` | VARCHAR(500) | | Additional notes |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**Constraints**:
- `UNIQUE INDEX idx_enrollment_student_active ON student_class_enrollments(student_id) WHERE end_date IS NULL`: Ensures student enrolled in only one class at a time

**Indexes**:
```sql
CREATE INDEX idx_enrollment_student ON student_class_enrollments(student_id);
CREATE INDEX idx_enrollment_class ON student_class_enrollments(class_id);
CREATE INDEX idx_enrollment_dates ON student_class_enrollments(enrollment_date, end_date);
CREATE INDEX idx_enrollment_current ON student_class_enrollments(student_id, end_date) WHERE end_date IS NULL;
CREATE UNIQUE INDEX idx_enrollment_student_active ON student_class_enrollments(student_id) WHERE end_date IS NULL;
```

**JPA Entity**:
```java
@Entity
@Table(name = "student_class_enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentClassEnrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EnrollmentReason reason = EnrollmentReason.NEW;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Business logic
    public boolean isCurrent() {
        return endDate == null;
    }

    public void endEnrollment(LocalDate endDate, String notes) {
        this.endDate = endDate;
        this.notes = notes;
    }
}

public enum EnrollmentReason {
    NEW,         // First-time enrollment
    TRANSFER,    // Transferred from another class/school
    PROMOTION,   // Promoted to next grade
    DEMOTION,    // Repeated grade
    CORRECTION   // Administrative correction
}
```

---

## Database Views

### v_current_class_roster

**Purpose**: Convenient view for querying current class composition (active students only)

```sql
CREATE VIEW v_current_class_roster AS
SELECT
    c.id as class_id,
    c.grade,
    c.section,
    c.academic_year,
    s.id as student_id,
    s.student_code,
    s.first_name,
    s.last_name,
    sce.enrollment_date
FROM student_class_enrollments sce
JOIN students s ON sce.student_id = s.id
JOIN classes c ON sce.class_id = c.id
WHERE sce.end_date IS NULL
  AND s.status = 'ACTIVE'
ORDER BY c.grade, c.section, s.last_name;
```

**Usage**:
```java
@Query(value = "SELECT * FROM v_current_class_roster WHERE class_id = :classId", nativeQuery = true)
List<ClassRosterView> getCurrentClassRoster(@Param("classId") UUID classId);
```

---

## Utility Functions

### get_student_current_class

**Purpose**: Get the current class ID for a student

```sql
CREATE OR REPLACE FUNCTION get_student_current_class(p_student_id UUID)
RETURNS UUID AS $$
    SELECT class_id FROM student_class_enrollments
    WHERE student_id = p_student_id AND end_date IS NULL
    LIMIT 1;
$$ LANGUAGE sql STABLE;
```

**Usage**:
```java
@Query(value = "SELECT get_student_current_class(:studentId)", nativeQuery = true)
UUID getCurrentClassId(@Param("studentId") UUID studentId);
```

---

## State Transitions

### Student Status

```
        ┌──────────────┐
        │   ACTIVE     │◄─────┐
        └──────┬───────┘      │
               │              │ restore()
               │ softDelete() │
               │              │
        ┌──────▼───────┐      │
        │   INACTIVE   │──────┘
        └──────────────┘
```

**Transitions**:
- **ACTIVE → INACTIVE**: `softDelete(reason, userId)` - Sets status, deleted_at, deleted_by, deletion_reason
- **INACTIVE → ACTIVE**: `restore()` - Clears soft delete fields, restores to active

### Enrollment Lifecycle

```
┌─────────┐    endEnrollment()    ┌──────────────┐
│ CURRENT │───────────────────────►│    ENDED     │
│         │   (set end_date)       │ (historical) │
└─────────┘                        └──────────────┘
```

**Transitions**:
- **CURRENT → ENDED**: `endEnrollment(endDate, notes)` - Sets end_date, making enrollment historical
- **NEW enrollment**: Create new record with `end_date = null`, ends previous enrollment automatically

---

## Validation Rules

### Student

1. **Student Code**: Unique, format validated (e.g., "YYYY-S-NNNNN")
2. **Date of Birth**: Must be in past, reasonable age range (5-25 years for enrollment)
3. **Gender**: Must be 'M' or 'F'
4. **Photo URL**: Valid file path or URL format
5. **Phone Numbers**: Cambodia format (+855 XX XXX XXX)
6. **At least one parent contact**: Business rule enforced at service layer

### ParentContact

1. **Phone Number**: Regex validation `^\\+855\\d{8,9}$`
2. **Primary Contact**: Only one primary per student (enforced at service layer)

### Class

1. **Grade**: Between 1 and 12
2. **Capacity**: If set, enrollment count cannot exceed max_capacity
3. **Unique Section**: Combination of (school_id, grade, section, academic_year) must be unique

### StudentClassEnrollment

1. **Active Enrollment**: Student can only be enrolled in one class at a time (end_date IS NULL)
2. **Date Consistency**: end_date must be >= enrollment_date
3. **No Past Enrollment**: enrollment_date cannot be in future

---

## Migration Scripts

### V1__create_students_table.sql

```sql
CREATE TABLE students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_code VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    first_name_km VARCHAR(100),
    last_name_km VARCHAR(100),
    date_of_birth DATE NOT NULL,
    gender VARCHAR(1) NOT NULL CHECK (gender IN ('M', 'F')),
    photo_url VARCHAR(500),
    address VARCHAR(500),
    emergency_contact VARCHAR(20),
    enrollment_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    deletion_reason VARCHAR(500),
    deleted_at TIMESTAMP,
    deleted_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_students_code ON students(student_code);
CREATE INDEX idx_students_status ON students(status);
CREATE INDEX idx_students_deleted_at ON students(deleted_at) WHERE deleted_at IS NOT NULL;
```

### V2__create_parent_contacts_table.sql

```sql
CREATE TABLE parent_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    relationship VARCHAR(50) NOT NULL CHECK (
        relationship IN ('MOTHER', 'FATHER', 'GUARDIAN', 'OTHER')
    ),
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_parent_contacts_student ON parent_contacts(student_id);
```

### V3__create_enrollments_table.sql

```sql
CREATE TABLE student_class_enrollments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    class_id UUID NOT NULL REFERENCES classes(id) ON DELETE RESTRICT,
    enrollment_date DATE NOT NULL,
    end_date DATE NULL,
    reason VARCHAR(50) NOT NULL DEFAULT 'NEW' CHECK (
        reason IN ('NEW', 'TRANSFER', 'PROMOTION', 'DEMOTION', 'CORRECTION')
    ),
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (end_date IS NULL OR end_date >= enrollment_date)
);

CREATE INDEX idx_enrollment_student ON student_class_enrollments(student_id);
CREATE INDEX idx_enrollment_class ON student_class_enrollments(class_id);
CREATE INDEX idx_enrollment_dates ON student_class_enrollments(enrollment_date, end_date);
CREATE INDEX idx_enrollment_current ON student_class_enrollments(student_id, end_date) WHERE end_date IS NULL;
CREATE UNIQUE INDEX idx_enrollment_student_active ON student_class_enrollments(student_id) WHERE end_date IS NULL;
```

### V4__create_views_and_functions.sql

```sql
-- Current class roster view
CREATE VIEW v_current_class_roster AS
SELECT
    c.id as class_id, c.grade, c.section, c.academic_year,
    s.id as student_id, s.student_code, s.first_name, s.last_name,
    sce.enrollment_date
FROM student_class_enrollments sce
JOIN students s ON sce.student_id = s.id
JOIN classes c ON sce.class_id = c.id
WHERE sce.end_date IS NULL AND s.status = 'ACTIVE'
ORDER BY c.grade, c.section, s.last_name;

-- Utility function
CREATE OR REPLACE FUNCTION get_student_current_class(p_student_id UUID)
RETURNS UUID AS $$
    SELECT class_id FROM student_class_enrollments
    WHERE student_id = p_student_id AND end_date IS NULL
    LIMIT 1;
$$ LANGUAGE sql STABLE;

-- Full-text search index
CREATE INDEX idx_students_fulltext ON students USING GIN (
    to_tsvector('simple', COALESCE(first_name, '') || ' ' || COALESCE(last_name, '') || ' ' ||
                           COALESCE(first_name_km, '') || ' ' || COALESCE(last_name_km, ''))
);
```

---

## Data Dictionary

### students

| Column | Business Meaning | Example Value |
|--------|------------------|---------------|
| student_code | Unique identifier for human reference | "2025-S-00042" |
| first_name | Given name (English) | "Sok" |
| last_name | Family name (English) | "Sara" |
| first_name_km | Given name (Khmer script) | "សុខ" |
| last_name_km | Family name (Khmer script) | "សារ៉ា" |
| date_of_birth | Birth date for age calculation | 2010-03-15 |
| gender | Biological sex | 'F' |
| photo_url | Path to student photo | "/uploads/students/abc123_1700000001.jpg" |
| enrollment_date | First enrollment date in school | 2023-09-01 |
| status | Current activity status | "ACTIVE" |
| deletion_reason | Why student was soft-deleted | "GRADUATED" |

### parent_contacts

| Column | Business Meaning | Example Value |
|--------|------------------|---------------|
| student_id | Link to student | UUID |
| full_name | Parent/guardian full name | "Mrs. Sok Channary" |
| phone_number | Contact phone | "+855 12 345 678" |
| relationship | Type of guardian | "MOTHER" |
| is_primary | Primary contact for communication | true |

### student_class_enrollments

| Column | Business Meaning | Example Value |
|--------|------------------|---------------|
| student_id | Enrolled student | UUID |
| class_id | Assigned class | UUID |
| enrollment_date | Start date of enrollment | 2024-09-01 |
| end_date | End date (null = current) | null or 2025-06-30 |
| reason | Why enrolled/transferred | "PROMOTION" |

---

## Performance Considerations

### Indexes

1. **Primary Keys**: All tables use UUID primary keys for distributed ID generation
2. **Foreign Keys**: Indexed automatically for join performance
3. **Status Fields**: Indexed for filtering active vs. inactive records
4. **Full-Text Search**: GIN index on concatenated name fields for bilingual search
5. **Partial Indexes**: `WHERE end_date IS NULL` for current enrollment queries
6. **Unique Constraints**: Enforce business rules at database level

### Query Optimization

1. **Pagination**: Always use LIMIT/OFFSET for list endpoints
2. **Lazy Loading**: Parent contacts loaded on-demand to avoid N+1 queries
3. **Soft Delete Filter**: JPA `@Where` clause automatically excludes inactive students
4. **View Usage**: `v_current_class_roster` pre-joins tables for roster queries

---

## Security Considerations

### Row-Level Security (Future Enhancement)

```sql
-- Example: Teachers can only see students in their assigned classes
CREATE POLICY teacher_class_students ON students
FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM student_class_enrollments sce
        JOIN classes c ON sce.class_id = c.id
        WHERE sce.student_id = students.id
          AND c.teacher_id = current_user_id()
          AND sce.end_date IS NULL
    )
);
```

### Audit Fields

- `created_by`, `updated_by`: Track who modified data
- `created_at`, `updated_at`: Track when modifications occurred
- `deleted_by`, `deleted_at`: Track who soft-deleted and when

---

## Next Steps

Data model complete. Ready for:
1. **API Contract Design**: Define OpenAPI endpoints mapping to these entities
2. **Repository Implementation**: Create Spring Data JPA repositories
3. **Service Layer**: Implement business logic leveraging this schema

**Status**: ✅ Data Model Complete
