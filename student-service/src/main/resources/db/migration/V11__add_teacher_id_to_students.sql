-- Migration: Add teacher_id column to students table
-- Feature: 001-tenant-student-isolation
-- Date: 2025-12-07
-- Purpose: Enable teacher-based data isolation where each student belongs to one teacher

BEGIN;

-- Step 1: Add teacher_id column (nullable for backward compatibility)
ALTER TABLE students
ADD COLUMN teacher_id UUID;

-- Step 2: Create index for performance (teacher-scoped queries)
CREATE INDEX idx_students_teacher_id ON students(teacher_id);

-- Step 3: Add column comment for documentation
COMMENT ON COLUMN students.teacher_id IS 'References the teacher who owns/created this student. Used for teacher-based data isolation.';

-- Step 4: Backfill strategy
-- Option: Leave NULL for existing students (requires manual assignment by admins)
-- No action needed - teacher_id remains NULL for existing students
-- New students created via API will automatically have teacher_id assigned

-- Step 5: (Future) Add NOT NULL constraint after backfill is complete
-- This should be done in a separate migration after verifying all students have teacher_id
-- ALTER TABLE students ALTER COLUMN teacher_id SET NOT NULL;

-- Step 6: (Optional) Add foreign key constraint if teacher data is replicated
-- This is NOT recommended for cross-service references
-- The teacher_id references teachers table in auth-service database
-- Referential integrity will be enforced at application layer via JWT validation

COMMIT;
