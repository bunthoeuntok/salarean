-- V6__drop_teacher_school_table.sql
-- Drop teacher_school table (moved to student-service)

-- Drop trigger first
DROP TRIGGER IF EXISTS update_teacher_school_updated_at ON teacher_school;

-- Drop table
DROP TABLE IF EXISTS teacher_school;

COMMENT ON SCHEMA public IS 'Teacher-school association moved to student-service as of V6';
