# Data Model: Teacher School Setup

**Feature**: 009-teacher-school-setup
**Date**: 2025-12-10
**Status**: Ready for implementation

## Overview

This document defines the data models, database schemas, and entity relationships for the teacher school setup feature. The feature introduces normalized location data (provinces, districts) and a teacher-school association table with principal metadata.

---

## Service Boundaries

### auth-service (`auth_db` database)
- **Owns**: Teacher-school associations, principal metadata
- **New tables**: `teacher_school`

### student-service (`student_db` database)
- **Owns**: Location hierarchy, schools data
- **New tables**: `provinces`, `districts`
- **Modified tables**: `schools` (add foreign keys to provinces/districts)

---

## Entity Relationship Diagram

```
auth_db:
┌─────────────────┐         ┌──────────────────────┐
│      users      │         │   teacher_school     │
├─────────────────┤         ├──────────────────────┤
│ id (PK, UUID)   │◄────┐   │ id (PK, UUID)        │
│ email           │     └───│ user_id (FK, UNIQUE) │
│ phone_number    │         │ school_id (UUID)*    │
│ password_hash   │         │ principal_name       │
│ name            │         │ principal_gender     │
│ ...             │         │ created_at           │
└─────────────────┘         │ updated_at           │
                            └──────────────────────┘
                                       │
                                       │ *References student_db.schools(id)
                                       │  (application-layer validation)
                                       ▼

student_db:
┌───────────────────┐       ┌────────────────────┐       ┌──────────────────┐
│    provinces      │       │     districts      │       │      schools     │
├───────────────────┤       ├────────────────────┤       ├──────────────────┤
│ id (PK, UUID)     │◄──┐   │ id (PK, UUID)      │◄──┐   │ id (PK, UUID)    │
│ name (UNIQUE)     │   └───│ province_id (FK)   │   └───│ province_id (FK) │
│ name_km           │       │ name               │       │ district_id (FK) │
│ code (UNIQUE)     │       │ name_km            │       │ name             │
│ created_at        │       │ code               │       │ name_km          │
│ updated_at        │       │ created_at         │       │ address          │
└───────────────────┘       │ updated_at         │       │ type             │
                            └────────────────────┘       │ province (old)*  │
                                                         │ district (old)*  │
                                                         │ created_at       │
                                                         │ updated_at       │
                                                         └──────────────────┘
                                                         * VARCHAR columns for
                                                           backward compatibility
```

---

## Table Schemas

### 1. teacher_school (auth_db)

**Purpose**: One-to-one association between teachers (users) and schools with principal metadata.

**Schema**:
```sql
CREATE TABLE teacher_school (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    school_id UUID NOT NULL,  -- References student_service.schools(id) (application-layer)
    principal_name VARCHAR(255) NOT NULL,
    principal_gender VARCHAR(1) NOT NULL CHECK (principal_gender IN ('M', 'F')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_school UNIQUE (user_id)
);

CREATE INDEX idx_teacher_school_user ON teacher_school(user_id);
CREATE INDEX idx_teacher_school_school ON teacher_school(school_id);

-- Trigger for updated_at
CREATE TRIGGER update_teacher_school_updated_at
BEFORE UPDATE ON teacher_school
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
```

**Columns**:
- `id`: Primary key (UUID v4)
- `user_id`: Foreign key to users.id (teacher identifier) - **UNIQUE** enforces one school per teacher
- `school_id`: UUID reference to student_service.schools.id (validated at application layer)
- `principal_name`: Name of the school principal (required for school setup)
- `principal_gender`: Principal's gender ('M' or 'F')
- `created_at`: Record creation timestamp
- `updated_at`: Record last update timestamp (auto-updated by trigger)

**Constraints**:
- `unique_user_school`: Each user can only have one school association
- `CHECK (principal_gender IN ('M', 'F'))`: Gender validation

**Indexes**:
- `idx_teacher_school_user`: Fast lookup by user_id (primary query pattern)
- `idx_teacher_school_school`: Fast lookup by school_id (analytics queries)

**Relationships**:
- `user_id` → `users.id` (ON DELETE CASCADE): When user deleted, association removed
- `school_id` → `student_service.schools.id` (application-layer validation): No database-level foreign key to maintain microservice independence

---

### 2. provinces (student_db)

**Purpose**: Top-level location entity for Cambodia's administrative divisions.

**Schema**:
```sql
CREATE TABLE provinces (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    name_km VARCHAR(100),
    code VARCHAR(10) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_provinces_code ON provinces(code);

-- Trigger for updated_at
CREATE TRIGGER update_provinces_updated_at
BEFORE UPDATE ON provinces
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
```

**Columns**:
- `id`: Primary key (UUID v4)
- `name`: Province name in English (unique)
- `name_km`: Province name in Khmer (optional for MVP)
- `code`: Province code (e.g., "PP" for Phnom Penh) - unique, optional
- `created_at`: Record creation timestamp
- `updated_at`: Record last update timestamp

**Constraints**:
- `UNIQUE (name)`: Province names must be unique
- `UNIQUE (code)`: Province codes must be unique (if provided)

**Indexes**:
- `idx_provinces_code`: Fast lookup by code

**Sample Data**:
```sql
INSERT INTO provinces (name, name_km, code) VALUES
('Phnom Penh', 'ភ្នំពេញ', 'PP'),
('Siem Reap', 'សៀមរាប', 'SR'),
('Battambang', 'បាត់ដំបង', 'BB'),
('Kandal', 'កណ្តាល', 'KD');
-- ... additional provinces
```

---

### 3. districts (student_db)

**Purpose**: Second-level location entity representing districts within provinces.

**Schema**:
```sql
CREATE TABLE districts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    province_id UUID NOT NULL REFERENCES provinces(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    name_km VARCHAR(100),
    code VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_district_per_province UNIQUE (province_id, name)
);

CREATE INDEX idx_districts_province ON districts(province_id);
CREATE INDEX idx_districts_code ON districts(code);

-- Trigger for updated_at
CREATE TRIGGER update_districts_updated_at
BEFORE UPDATE ON districts
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
```

**Columns**:
- `id`: Primary key (UUID v4)
- `province_id`: Foreign key to provinces.id (required)
- `name`: District name in English (unique within province)
- `name_km`: District name in Khmer (optional for MVP)
- `code`: District code (optional)
- `created_at`: Record creation timestamp
- `updated_at`: Record last update timestamp

**Constraints**:
- `unique_district_per_province`: District name must be unique within a province
- `ON DELETE CASCADE`: When province deleted, all districts deleted (unlikely in practice)

**Indexes**:
- `idx_districts_province`: Fast filtering by province_id (primary query pattern)
- `idx_districts_code`: Fast lookup by code

**Sample Data**:
```sql
INSERT INTO districts (province_id, name, name_km, code) VALUES
((SELECT id FROM provinces WHERE code = 'PP'), 'Chamkar Mon', 'ចំការមន', 'CM'),
((SELECT id FROM provinces WHERE code = 'PP'), 'Doun Penh', 'ដូនពេញ', 'DP'),
((SELECT id FROM provinces WHERE code = 'SR'), 'Siem Reap', 'សៀមរាប', 'SR01');
-- ... additional districts
```

---

### 4. schools (student_db) - MODIFIED

**Purpose**: Educational institutions (existing table with new foreign keys added).

**Migration Changes**:
```sql
-- Add new foreign key columns
ALTER TABLE schools
ADD COLUMN province_id UUID REFERENCES provinces(id) ON DELETE SET NULL,
ADD COLUMN district_id UUID REFERENCES districts(id) ON DELETE SET NULL;

-- Create indexes for new foreign keys
CREATE INDEX idx_schools_province ON schools(province_id);
CREATE INDEX idx_schools_district ON schools(district_id);

-- Add unique constraint to prevent duplicate school names in same district
ALTER TABLE schools
ADD CONSTRAINT unique_school_per_district UNIQUE (district_id, name);

-- Note: OLD province/district VARCHAR columns remain for backward compatibility
-- Deprecate in future release after migration verification
```

**Updated Schema** (showing relevant columns):
```sql
-- Existing columns (unchanged)
id UUID PRIMARY KEY
name VARCHAR(255) NOT NULL
name_km VARCHAR(255)
address VARCHAR(500)
type VARCHAR(50) NOT NULL CHECK (type IN ('PRIMARY', 'SECONDARY', 'HIGH_SCHOOL', 'VOCATIONAL'))
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

-- NEW foreign key columns
province_id UUID REFERENCES provinces(id) ON DELETE SET NULL
district_id UUID REFERENCES districts(id) ON DELETE SET NULL

-- OLD VARCHAR columns (deprecated but kept for backward compatibility)
province VARCHAR(100)  -- To be removed in future release
district VARCHAR(100)  -- To be removed in future release
```

**Constraints**:
- `unique_school_per_district`: School name must be unique within a district (prevents duplicate entries)
- `ON DELETE SET NULL`: If province/district deleted, school retains data but foreign keys nulled (rare edge case)

**Indexes**:
- `idx_schools_province`: Fast filtering by province_id
- `idx_schools_district`: Fast filtering by district_id (primary query pattern)

---

## JPA Entity Classes

### TeacherSchool.java (auth-service)

```java
package com.sms.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "teacher_school")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherSchool {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;  // FK to users.id (not mapped as @ManyToOne to avoid circular dependency)

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;  // References student_service.schools.id (application-layer validation)

    @Column(name = "principal_name", nullable = false, length = 255)
    private String principalName;

    @Column(name = "principal_gender", nullable = false, length = 1)
    private String principalGender;  // 'M' or 'F'

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

---

### Province.java (student-service)

```java
package com.sms.student.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "provinces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Province {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "name_km", length = 100)
    private String nameKhmer;

    @Column(unique = true, length = 10)
    private String code;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

---

### District.java (student-service)

```java
package com.sms.student.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "districts",
    uniqueConstraints = @UniqueConstraint(
        name = "unique_district_per_province",
        columnNames = {"province_id", "name"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "province_id", nullable = false)
    private UUID provinceId;  // FK to provinces.id (not mapped as @ManyToOne for simplicity)

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "name_km", length = 100)
    private String nameKhmer;

    @Column(length = 10)
    private String code;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

---

### School.java (student-service) - UPDATED

```java
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
```

---

## DTOs (Data Transfer Objects)

### Auth-Service DTOs

#### TeacherSchoolRequest
```java
package com.sms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class TeacherSchoolRequest {

    @NotNull(message = "School ID is required")
    private UUID schoolId;

    @NotBlank(message = "Principal name is required")
    @Size(max = 255, message = "Principal name must not exceed 255 characters")
    private String principalName;

    @NotNull(message = "Principal gender is required")
    @Pattern(regexp = "M|F", message = "Gender must be M or F")
    private String principalGender;
}
```

#### TeacherSchoolResponse
```java
package com.sms.auth.dto;

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
public class TeacherSchoolResponse {

    private UUID id;
    private UUID userId;
    private UUID schoolId;
    private String schoolName;  // Enriched from student-service
    private String principalName;
    private String principalGender;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

### Student-Service DTOs

#### ProvinceResponse
```java
package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceResponse {

    private UUID id;
    private String name;
    private String nameKhmer;
    private String code;
}
```

#### DistrictResponse
```java
package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistrictResponse {

    private UUID id;
    private UUID provinceId;
    private String name;
    private String nameKhmer;
    private String code;
}
```

#### SchoolRequest
```java
package com.sms.student.dto;

import com.sms.student.enums.SchoolType;
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
public class SchoolRequest {

    @NotBlank(message = "School name is required")
    @Size(max = 255, message = "School name must not exceed 255 characters")
    private String name;

    @Size(max = 255, message = "Khmer name must not exceed 255 characters")
    private String nameKhmer;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @NotNull(message = "Province is required")
    private UUID provinceId;

    @NotNull(message = "District is required")
    private UUID districtId;

    @NotNull(message = "School type is required")
    private SchoolType type;
}
```

#### SchoolResponse
```java
package com.sms.student.dto;

import com.sms.student.enums.SchoolType;
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
public class SchoolResponse {

    private UUID id;
    private String name;
    private String nameKhmer;
    private String address;
    private UUID provinceId;
    private UUID districtId;
    private String provinceName;  // Enriched for display
    private String districtName;  // Enriched for display
    private SchoolType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## Enums

### SchoolType (student-service)

```java
package com.sms.student.enums;

public enum SchoolType {
    PRIMARY,        // Grades 1-6
    SECONDARY,      // Grades 7-9
    HIGH_SCHOOL,    // Grades 10-12
    VOCATIONAL      // Technical/vocational training
}
```

---

## Validation Rules

### Field Validation

| Field | Constraints | Error Message |
|-------|-------------|---------------|
| `school_id` | NOT NULL, UUID | "School ID is required" |
| `principal_name` | NOT BLANK, MAX 255 | "Principal name is required" |
| `principal_gender` | NOT NULL, PATTERN(M\|F) | "Gender must be M or F" |
| `province_id` | NOT NULL, UUID | "Province is required" |
| `district_id` | NOT NULL, UUID | "District is required" |
| `name` | NOT BLANK, MAX 255 | "School name is required" |
| `address` | NOT BLANK, MAX 500 | "Address is required" |
| `type` | NOT NULL, ENUM | "School type is required" |

### Business Logic Validation

| Rule | Validation | Error Code |
|------|------------|------------|
| **Unique school association** | Teacher can only have one school_id in teacher_school table | `TEACHER_ALREADY_ASSIGNED` |
| **Valid school reference** | school_id must exist in student_service.schools table | `SCHOOL_NOT_FOUND` |
| **Valid province reference** | province_id must exist in provinces table | `PROVINCE_NOT_FOUND` |
| **Valid district reference** | district_id must exist in districts table | `DISTRICT_NOT_FOUND` |
| **District belongs to province** | district.province_id must match school.province_id | `INVALID_DISTRICT_FOR_PROVINCE` |
| **Unique school name per district** | (district_id, name) must be unique in schools table | `DUPLICATE_SCHOOL_NAME` |

---

## Migration Scripts

### Migration V5: Create teacher_school table (auth-service)

```sql
-- V5__create_teacher_school_table.sql
CREATE TABLE teacher_school (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    school_id UUID NOT NULL,
    principal_name VARCHAR(255) NOT NULL,
    principal_gender VARCHAR(1) NOT NULL CHECK (principal_gender IN ('M', 'F')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_school UNIQUE (user_id)
);

CREATE INDEX idx_teacher_school_user ON teacher_school(user_id);
CREATE INDEX idx_teacher_school_school ON teacher_school(school_id);

-- Reuse existing trigger function for updated_at
CREATE TRIGGER update_teacher_school_updated_at
BEFORE UPDATE ON teacher_school
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE teacher_school IS 'One-to-one association between teachers and schools with principal metadata';
COMMENT ON COLUMN teacher_school.school_id IS 'References student_service.schools.id (validated at application layer)';
COMMENT ON COLUMN teacher_school.principal_name IS 'Name of the school principal';
COMMENT ON COLUMN teacher_school.principal_gender IS 'Principal gender: M (Male) or F (Female)';
```

---

### Migration V12: Create provinces and districts tables (student-service)

```sql
-- V12__create_provinces_and_districts_tables.sql
-- Step 1: Create provinces table
CREATE TABLE provinces (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    name_km VARCHAR(100),
    code VARCHAR(10) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_provinces_code ON provinces(code);

CREATE TRIGGER update_provinces_updated_at
BEFORE UPDATE ON provinces
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE provinces IS 'Cambodia provinces (top-level administrative division)';
COMMENT ON COLUMN provinces.name_km IS 'Province name in Khmer';
COMMENT ON COLUMN provinces.code IS 'Province code (e.g., PP for Phnom Penh)';

-- Step 2: Create districts table
CREATE TABLE districts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    province_id UUID NOT NULL REFERENCES provinces(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    name_km VARCHAR(100),
    code VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_district_per_province UNIQUE (province_id, name)
);

CREATE INDEX idx_districts_province ON districts(province_id);
CREATE INDEX idx_districts_code ON districts(code);

CREATE TRIGGER update_districts_updated_at
BEFORE UPDATE ON districts
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE districts IS 'Cambodia districts (second-level administrative division)';
COMMENT ON COLUMN districts.province_id IS 'Foreign key to provinces table';
COMMENT ON COLUMN districts.name_km IS 'District name in Khmer';
```

---

### Migration V13: Populate provinces and districts from existing data (student-service)

```sql
-- V13__populate_provinces_and_districts_from_existing_data.sql
-- Extract unique provinces from schools.province and insert into provinces table
INSERT INTO provinces (name)
SELECT DISTINCT TRIM(province)
FROM schools
WHERE province IS NOT NULL AND TRIM(province) != ''
ORDER BY province;

-- Extract unique districts from schools.district and insert into districts table
INSERT INTO districts (province_id, name)
SELECT DISTINCT
    p.id AS province_id,
    TRIM(s.district) AS name
FROM schools s
JOIN provinces p ON TRIM(s.province) = p.name
WHERE s.district IS NOT NULL AND TRIM(s.district) != ''
ORDER BY p.name, s.district;

-- Verification query (should return 0 for successful migration)
DO $$
DECLARE
    unmapped_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO unmapped_count
    FROM schools s
    WHERE s.province IS NOT NULL AND s.district IS NOT NULL
      AND NOT EXISTS (
          SELECT 1 FROM provinces p
          JOIN districts d ON d.province_id = p.id
          WHERE TRIM(s.province) = p.name AND TRIM(s.district) = d.name
      );

    IF unmapped_count > 0 THEN
        RAISE WARNING 'Migration incomplete: % schools have unmapped province/district combinations', unmapped_count;
    ELSE
        RAISE NOTICE 'Migration successful: All schools have mapped provinces and districts';
    END IF;
END $$;
```

---

### Migration V14: Add foreign keys to schools table (student-service)

```sql
-- V14__add_province_and_district_foreign_keys_to_schools.sql
-- Step 1: Add new columns (nullable initially)
ALTER TABLE schools
ADD COLUMN province_id UUID,
ADD COLUMN district_id UUID;

-- Step 2: Create indexes before FK constraints (improves performance)
CREATE INDEX idx_schools_province ON schools(province_id);
CREATE INDEX idx_schools_district ON schools(district_id);

-- Step 3: Backfill province_id and district_id by matching VARCHAR columns
UPDATE schools s
SET province_id = p.id,
    district_id = d.id
FROM provinces p
JOIN districts d ON d.province_id = p.id
WHERE TRIM(s.province) = p.name
  AND TRIM(s.district) = d.name;

-- Step 4: Add foreign key constraints
ALTER TABLE schools
ADD CONSTRAINT fk_schools_province FOREIGN KEY (province_id) REFERENCES provinces(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_schools_district FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE SET NULL;

-- Step 5: Add unique constraint for school names within districts
ALTER TABLE schools
ADD CONSTRAINT unique_school_per_district UNIQUE (district_id, name);

-- Verification: Check for schools with null foreign keys
DO $$
DECLARE
    null_fk_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO null_fk_count
    FROM schools
    WHERE province IS NOT NULL AND district IS NOT NULL
      AND (province_id IS NULL OR district_id IS NULL);

    IF null_fk_count > 0 THEN
        RAISE WARNING 'Backfill incomplete: % schools have null foreign keys', null_fk_count;
    ELSE
        RAISE NOTICE 'Backfill successful: All schools have valid foreign keys';
    END IF;
END $$;

COMMENT ON COLUMN schools.province_id IS 'Foreign key to provinces table (replaces VARCHAR province column)';
COMMENT ON COLUMN schools.district_id IS 'Foreign key to districts table (replaces VARCHAR district column)';
COMMENT ON CONSTRAINT unique_school_per_district ON schools IS 'Prevents duplicate school names within the same district';
```

---

## Data Integrity Rules

### Referential Integrity

1. **teacher_school.user_id → users.id**
   - ON DELETE CASCADE: When user deleted, teacher-school association removed
   - UNIQUE constraint ensures one school per teacher

2. **districts.province_id → provinces.id**
   - ON DELETE CASCADE: When province deleted, all districts removed (rare scenario)

3. **schools.province_id → provinces.id**
   - ON DELETE SET NULL: If province deleted, school retains data but FK nulled

4. **schools.district_id → districts.id**
   - ON DELETE SET NULL: If district deleted, school retains data but FK nulled

5. **teacher_school.school_id → student_service.schools.id**
   - **Application-layer validation only** (no database FK across services)
   - Validated via HTTP call to student-service before creating association

### Unique Constraints

1. **teacher_school.user_id**: One school per teacher
2. **provinces.name**: Province names must be unique
3. **provinces.code**: Province codes must be unique
4. **districts (province_id, name)**: District name unique within province
5. **schools (district_id, name)**: School name unique within district

---

## Query Patterns

### Common Queries

#### 1. Get all provinces
```sql
SELECT id, name, name_km, code
FROM provinces
ORDER BY name;
```

#### 2. Get districts for a province
```sql
SELECT id, province_id, name, name_km, code
FROM districts
WHERE province_id = ?
ORDER BY name;
```

#### 3. Get schools for a district
```sql
SELECT s.id, s.name, s.name_km, s.address, s.type,
       s.province_id, s.district_id,
       p.name AS province_name,
       d.name AS district_name
FROM schools s
JOIN provinces p ON s.province_id = p.id
JOIN districts d ON s.district_id = d.id
WHERE s.district_id = ?
ORDER BY s.name;
```

#### 4. Get teacher's school association
```sql
SELECT ts.id, ts.user_id, ts.school_id, ts.principal_name, ts.principal_gender,
       ts.created_at, ts.updated_at
FROM teacher_school ts
WHERE ts.user_id = ?;
```

#### 5. Create teacher-school association
```sql
INSERT INTO teacher_school (user_id, school_id, principal_name, principal_gender)
VALUES (?, ?, ?, ?)
ON CONFLICT (user_id) DO UPDATE
SET school_id = EXCLUDED.school_id,
    principal_name = EXCLUDED.principal_name,
    principal_gender = EXCLUDED.principal_gender,
    updated_at = CURRENT_TIMESTAMP;
```

---

## Performance Considerations

### Indexes

| Table | Index | Purpose | Query Pattern |
|-------|-------|---------|---------------|
| `teacher_school` | `idx_teacher_school_user` | Find teacher's school | `WHERE user_id = ?` |
| `teacher_school` | `idx_teacher_school_school` | Analytics by school | `WHERE school_id = ?` |
| `provinces` | `idx_provinces_code` | Lookup by code | `WHERE code = ?` |
| `districts` | `idx_districts_province` | Filter by province | `WHERE province_id = ?` |
| `districts` | `idx_districts_code` | Lookup by code | `WHERE code = ?` |
| `schools` | `idx_schools_province` | Filter by province | `WHERE province_id = ?` |
| `schools` | `idx_schools_district` | Filter by district | `WHERE district_id = ?` |

### Query Optimization

- **Provinces query**: No pagination needed (~25 provinces in Cambodia)
- **Districts query**: No pagination needed (~200 districts total, ~10 per province on average)
- **Schools query**: No pagination initially (~1000 schools total, ~10-50 per district on average)
- **Teacher-school lookup**: Single-row query with indexed user_id (sub-millisecond)

### Caching Strategy (Future Enhancement)

- Provinces list: Rarely changes → Cache for 24 hours (not implemented in MVP)
- Districts per province: Rarely changes → Cache for 24 hours (not implemented in MVP)
- Schools per district: Changes frequently (new additions) → No caching in MVP

---

## Testing Data

### Sample Provinces
```sql
INSERT INTO provinces (name, name_km, code) VALUES
('Phnom Penh', 'ភ្នំពេញ', 'PP'),
('Siem Reap', 'សៀមរាប', 'SR'),
('Battambang', 'បាត់ដំបង', 'BB'),
('Kandal', 'កណ្តាល', 'KD');
```

### Sample Districts
```sql
INSERT INTO districts (province_id, name, name_km, code)
SELECT
    (SELECT id FROM provinces WHERE code = 'PP'),
    'Chamkar Mon', 'ចំការមន', 'CM'
UNION ALL SELECT
    (SELECT id FROM provinces WHERE code = 'PP'),
    'Doun Penh', 'ដូនពេញ', 'DP'
UNION ALL SELECT
    (SELECT id FROM provinces WHERE code = 'SR'),
    'Siem Reap', 'សៀមរាប', 'SR01';
```

### Sample Teacher-School Association
```sql
INSERT INTO teacher_school (user_id, school_id, principal_name, principal_gender)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000',  -- Example user UUID
    '660e8400-e29b-41d4-a716-446655440001',  -- Example school UUID
    'Mr. Sok Sambath',
    'M'
);
```

---

## Summary

This data model introduces:
- **1 new table** in auth-service: `teacher_school` (teacher-school associations with principal metadata)
- **2 new tables** in student-service: `provinces`, `districts` (normalized location hierarchy)
- **Modified table** in student-service: `schools` (added province_id and district_id foreign keys)
- **Cross-service reference**: teacher_school.school_id validated at application layer (maintains microservice independence)
- **Backward compatibility**: Old VARCHAR columns (province, district) retained temporarily

All tables follow SMS architecture standards:
- UUID primary keys
- Timestamp audit fields (created_at, updated_at)
- Proper indexes for query performance
- Unique constraints for data integrity
- Flyway migration scripts for version control
