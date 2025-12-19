-- V5: Create grade_averages table
-- Feature: Cached grade calculations
-- Purpose: Store pre-calculated averages for performance

BEGIN;

-- Grade averages table (cached calculations)
CREATE TABLE IF NOT EXISTS grade_averages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID NOT NULL,
    student_id UUID NOT NULL,
    class_id UUID NOT NULL,
    subject_id UUID REFERENCES subjects(id),  -- NULL for overall average
    semester INTEGER CHECK (semester IN (1, 2)),  -- NULL for annual average
    academic_year VARCHAR(20) NOT NULL,
    average_type VARCHAR(30) NOT NULL CHECK (average_type IN (
        'MONTHLY_AVERAGE',      -- Average of monthly exams for a subject
        'SEMESTER_AVERAGE',     -- Semester average for a subject (monthly + semester exam)
        'SUBJECT_ANNUAL',       -- Annual average for a subject
        'OVERALL_SEMESTER',     -- Overall average across all subjects for a semester
        'OVERALL_ANNUAL'        -- Overall annual average
    )),
    average_score DECIMAL(5,2) NOT NULL CHECK (average_score >= 0 AND average_score <= 100),
    letter_grade VARCHAR(2) NOT NULL,  -- A, B, C, D, E, F
    class_rank INTEGER,
    total_students INTEGER,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Ensure unique average per type/student/subject/period
    CONSTRAINT unique_average_entry
        UNIQUE (student_id, class_id, subject_id, semester, academic_year, average_type)
);

-- Indexes for performance
CREATE INDEX idx_averages_teacher ON grade_averages(teacher_id);
CREATE INDEX idx_averages_student ON grade_averages(student_id);
CREATE INDEX idx_averages_class ON grade_averages(class_id);
CREATE INDEX idx_averages_subject ON grade_averages(subject_id);
CREATE INDEX idx_averages_type ON grade_averages(average_type);
CREATE INDEX idx_averages_rankings ON grade_averages(class_id, semester, academic_year, average_type, average_score DESC);
CREATE INDEX idx_averages_student_year ON grade_averages(student_id, academic_year);

-- Comments
COMMENT ON TABLE grade_averages IS 'Pre-calculated grade averages for performance optimization';
COMMENT ON COLUMN grade_averages.average_type IS 'Type of average calculation';
COMMENT ON COLUMN grade_averages.letter_grade IS 'MoEYS letter grade (A=85-100, B=70-84, C=55-69, D=40-54, E=25-39, F=0-24)';
COMMENT ON COLUMN grade_averages.class_rank IS 'Student rank in class for this average type';

-- Function to calculate letter grade based on MoEYS standard
CREATE OR REPLACE FUNCTION get_letter_grade(score DECIMAL)
RETURNS VARCHAR(2) AS $$
BEGIN
    RETURN CASE
        WHEN score >= 85 THEN 'A'
        WHEN score >= 70 THEN 'B'
        WHEN score >= 55 THEN 'C'
        WHEN score >= 40 THEN 'D'
        WHEN score >= 25 THEN 'E'
        ELSE 'F'
    END;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION get_letter_grade IS 'Convert numeric score to MoEYS letter grade';

COMMIT;
