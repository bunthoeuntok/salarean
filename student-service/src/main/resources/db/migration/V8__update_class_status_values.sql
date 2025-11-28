-- V8: Update class status constraint to match frontend expectations

-- First, update any existing ARCHIVED records to COMPLETED
UPDATE classes SET status = 'COMPLETED' WHERE status = 'ARCHIVED';

-- Drop the existing constraint and add new one with updated values
ALTER TABLE classes DROP CONSTRAINT IF EXISTS classes_status_check;
ALTER TABLE classes ADD CONSTRAINT classes_status_check
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'COMPLETED'));
