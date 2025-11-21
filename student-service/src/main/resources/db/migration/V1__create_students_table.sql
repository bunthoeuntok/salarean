-- V1: Create schools, classes, and students tables

-- Schools table (reference data for classes)
CREATE TABLE IF NOT EXISTS schools (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    name_km VARCHAR(255),
    address VARCHAR(500),
    province VARCHAR(100),
    district VARCHAR(100),
    type VARCHAR(50) NOT NULL CHECK (type IN ('PRIMARY', 'SECONDARY', 'HIGH_SCHOOL', 'VOCATIONAL')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Classes table
CREATE TABLE IF NOT EXISTS classes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id UUID NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    teacher_id UUID NOT NULL,
    grade INTEGER NOT NULL CHECK (grade BETWEEN 1 AND 12),
    section VARCHAR(10) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    max_capacity INTEGER,
    student_count INTEGER DEFAULT 0 NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    version BIGINT DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT unique_class_section UNIQUE (school_id, grade, section, academic_year)
);

CREATE INDEX idx_classes_teacher ON classes(teacher_id);
CREATE INDEX idx_classes_school ON classes(school_id);
CREATE INDEX idx_classes_status ON classes(status);

-- Students table (core entity)
CREATE TABLE IF NOT EXISTS students (
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
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    deletion_reason VARCHAR(500),
    deleted_at TIMESTAMP,
    deleted_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_students_code ON students(student_code);
CREATE INDEX idx_students_status ON students(status);
CREATE INDEX idx_students_deleted_at ON students(deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX idx_students_enrollment_date ON students(enrollment_date);
