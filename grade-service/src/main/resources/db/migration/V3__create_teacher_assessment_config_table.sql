-- V3: Create teacher_assessment_config table
-- Feature: Teacher customization of assessment schedule
-- Purpose: Allow teachers to customize number of monthly exams per class/semester

BEGIN;

-- Teacher assessment configuration table
CREATE TABLE IF NOT EXISTS teacher_assessment_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID NOT NULL,
    class_id UUID NOT NULL,
    subject_id UUID NOT NULL REFERENCES subjects(id),
    semester INTEGER NOT NULL CHECK (semester IN (1, 2)),
    academic_year VARCHAR(20) NOT NULL,
    monthly_exam_count INTEGER NOT NULL DEFAULT 4 CHECK (monthly_exam_count BETWEEN 1 AND 6),
    monthly_weight DECIMAL(5,2) NOT NULL DEFAULT 50.00 CHECK (monthly_weight >= 0 AND monthly_weight <= 100),
    semester_weight DECIMAL(5,2) NOT NULL DEFAULT 50.00 CHECK (semester_weight >= 0 AND semester_weight <= 100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Ensure unique config per teacher/class/subject/semester/year
    CONSTRAINT unique_config_per_class_subject_semester
        UNIQUE (teacher_id, class_id, subject_id, semester, academic_year),

    -- Ensure weights sum to 100
    CONSTRAINT weights_sum_to_100
        CHECK (monthly_weight + semester_weight = 100)
);

-- Indexes for performance
CREATE INDEX idx_config_teacher ON teacher_assessment_config(teacher_id);
CREATE INDEX idx_config_class ON teacher_assessment_config(class_id);
CREATE INDEX idx_config_subject ON teacher_assessment_config(subject_id);
CREATE INDEX idx_config_lookup ON teacher_assessment_config(teacher_id, class_id, semester, academic_year);

-- Comments
COMMENT ON TABLE teacher_assessment_config IS 'Teacher customization for assessment schedule per class/semester';
COMMENT ON COLUMN teacher_assessment_config.monthly_exam_count IS 'Number of monthly exams (1-6, default 4)';
COMMENT ON COLUMN teacher_assessment_config.monthly_weight IS 'Total weight for all monthly exams (default 50%)';
COMMENT ON COLUMN teacher_assessment_config.semester_weight IS 'Weight for semester exam (default 50%)';

COMMIT;
