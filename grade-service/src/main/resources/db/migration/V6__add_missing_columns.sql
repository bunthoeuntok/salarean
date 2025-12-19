-- V6: Add missing columns
-- Feature: Additional grade fields
-- Purpose: Add comments column to grades and subject_rank to grade_averages

BEGIN;

-- Add comments column to grades table
ALTER TABLE grades ADD COLUMN IF NOT EXISTS comments VARCHAR(500);

COMMENT ON COLUMN grades.comments IS 'Additional comments for the grade';

-- Add subject_rank column to grade_averages table
ALTER TABLE grade_averages ADD COLUMN IF NOT EXISTS subject_rank INTEGER;

COMMENT ON COLUMN grade_averages.subject_rank IS 'Student rank in class for this subject';

COMMIT;
