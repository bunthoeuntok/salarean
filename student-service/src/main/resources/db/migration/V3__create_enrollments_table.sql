-- V3: Create student_class_enrollments table

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

CREATE INDEX idx_enrollment_student ON student_class_enrollments(student_id);
CREATE INDEX idx_enrollment_class ON student_class_enrollments(class_id);
CREATE INDEX idx_enrollment_dates ON student_class_enrollments(enrollment_date, end_date);
CREATE INDEX idx_enrollment_current ON student_class_enrollments(student_id, end_date) WHERE end_date IS NULL;
CREATE UNIQUE INDEX idx_enrollment_student_active ON student_class_enrollments(student_id) WHERE end_date IS NULL;
