-- V7: Add grade_levels to subjects table
-- Feature: Subject grade level filtering
-- Purpose: Track which grade levels each subject is available for

BEGIN;

-- Add grade_levels column as integer array
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS grade_levels INTEGER[];

COMMENT ON COLUMN subjects.grade_levels IS 'Array of grade levels this subject is available for (1-12)';

-- Update existing subjects with default grade levels
-- Core subjects typically available for all grades
UPDATE subjects SET grade_levels = ARRAY[1,2,3,4,5,6,7,8,9,10,11,12] WHERE is_core = true;

-- Non-core subjects (PE, Art, Music, IT) also for all grades
UPDATE subjects SET grade_levels = ARRAY[1,2,3,4,5,6,7,8,9,10,11,12] WHERE is_core = false;

COMMIT;
