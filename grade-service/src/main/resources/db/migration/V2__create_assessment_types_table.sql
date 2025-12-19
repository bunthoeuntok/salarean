-- V2: Create assessment_types table
-- Feature: grade-service assessment configuration
-- Purpose: Define types of assessments (monthly exams, semester exams)

BEGIN;

-- Assessment types table (reference data)
CREATE TABLE IF NOT EXISTS assessment_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    name_km VARCHAR(100) NOT NULL,
    code VARCHAR(30) NOT NULL UNIQUE,
    category VARCHAR(30) NOT NULL CHECK (category IN ('MONTHLY_EXAM', 'SEMESTER_EXAM')),
    default_weight DECIMAL(5,2) NOT NULL CHECK (default_weight >= 0 AND default_weight <= 100),
    max_score DECIMAL(5,2) DEFAULT 100,
    description VARCHAR(500),
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes
CREATE INDEX idx_assessment_types_category ON assessment_types(category);
CREATE INDEX idx_assessment_types_code ON assessment_types(code);

-- Comments
COMMENT ON TABLE assessment_types IS 'Types of assessments for grading (monthly exams, semester exams)';
COMMENT ON COLUMN assessment_types.category IS 'MONTHLY_EXAM or SEMESTER_EXAM';
COMMENT ON COLUMN assessment_types.default_weight IS 'Default weight percentage for grade calculation';

-- Insert standard assessment types
-- Default: 4 monthly exams (12.5% each = 50%) + 1 semester exam (50%)
INSERT INTO assessment_types (name, name_km, code, category, default_weight, max_score, description, display_order) VALUES
    ('Monthly Exam 1', 'ប្រឡងប្រចាំខែ ១', 'MONTHLY_1', 'MONTHLY_EXAM', 12.50, 100, 'First monthly examination', 1),
    ('Monthly Exam 2', 'ប្រឡងប្រចាំខែ ២', 'MONTHLY_2', 'MONTHLY_EXAM', 12.50, 100, 'Second monthly examination', 2),
    ('Monthly Exam 3', 'ប្រឡងប្រចាំខែ ៣', 'MONTHLY_3', 'MONTHLY_EXAM', 12.50, 100, 'Third monthly examination', 3),
    ('Monthly Exam 4', 'ប្រឡងប្រចាំខែ ៤', 'MONTHLY_4', 'MONTHLY_EXAM', 12.50, 100, 'Fourth monthly examination', 4),
    ('Semester Exam', 'ប្រឡងឆមាស', 'SEMESTER', 'SEMESTER_EXAM', 50.00, 100, 'End of semester examination', 5);

COMMIT;
