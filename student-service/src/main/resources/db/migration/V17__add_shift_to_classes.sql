-- V17: Add shift column to classes table
-- Supports MORNING, AFTERNOON, and FULLDAY shifts for class scheduling

ALTER TABLE classes ADD COLUMN shift VARCHAR(20) NOT NULL DEFAULT 'MORNING';

-- Add check constraint for valid shift values
ALTER TABLE classes ADD CONSTRAINT classes_shift_check
    CHECK (shift IN ('MORNING', 'AFTERNOON', 'FULLDAY'));

-- Add comment for documentation
COMMENT ON COLUMN classes.shift IS 'Class shift schedule: MORNING, AFTERNOON, or FULLDAY';
