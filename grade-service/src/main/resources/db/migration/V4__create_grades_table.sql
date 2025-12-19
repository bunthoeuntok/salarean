-- V4: Create grades table
-- Feature: Student grade storage
-- Purpose: Store actual student scores with teacher-based isolation

BEGIN;

-- Grades table (actual student scores)
CREATE TABLE IF NOT EXISTS grades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID NOT NULL,
    student_id UUID NOT NULL,
    class_id UUID NOT NULL,
    subject_id UUID NOT NULL REFERENCES subjects(id),
    assessment_type_id UUID NOT NULL REFERENCES assessment_types(id),
    semester INTEGER NOT NULL CHECK (semester IN (1, 2)),
    academic_year VARCHAR(20) NOT NULL,
    score DECIMAL(5,2) NOT NULL CHECK (score >= 0 AND score <= 100),
    max_score DECIMAL(5,2) NOT NULL DEFAULT 100,
    notes VARCHAR(500),
    entered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    entered_by UUID NOT NULL,
    updated_by UUID,

    -- Prevent duplicate grade entries for same student/subject/assessment
    CONSTRAINT unique_grade_entry
        UNIQUE (student_id, class_id, subject_id, assessment_type_id, semester, academic_year)
);

-- Indexes for performance
CREATE INDEX idx_grades_teacher ON grades(teacher_id);
CREATE INDEX idx_grades_student ON grades(student_id);
CREATE INDEX idx_grades_class ON grades(class_id);
CREATE INDEX idx_grades_subject ON grades(subject_id);
CREATE INDEX idx_grades_assessment ON grades(assessment_type_id);
CREATE INDEX idx_grades_semester ON grades(semester, academic_year);
CREATE INDEX idx_grades_lookup ON grades(teacher_id, class_id, subject_id, semester, academic_year);
CREATE INDEX idx_grades_student_subject ON grades(student_id, subject_id, academic_year);
CREATE INDEX idx_grades_class_assessment ON grades(class_id, assessment_type_id, semester, academic_year);

-- Comments
COMMENT ON TABLE grades IS 'Student grade records with teacher-based data isolation';
COMMENT ON COLUMN grades.teacher_id IS 'Teacher who owns this grade record (for data isolation)';
COMMENT ON COLUMN grades.score IS 'Actual score achieved (0-100)';
COMMENT ON COLUMN grades.entered_by IS 'User who entered the grade';
COMMENT ON COLUMN grades.updated_by IS 'User who last updated the grade';

COMMIT;
