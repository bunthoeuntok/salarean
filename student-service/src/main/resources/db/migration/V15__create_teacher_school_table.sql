-- V15__create_teacher_school_table.sql
-- Creates teacher_school table for one-to-one association between teachers and schools

CREATE TABLE teacher_school (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    school_id UUID NOT NULL REFERENCES schools(id) ON DELETE RESTRICT,
    principal_name VARCHAR(255) NOT NULL,
    principal_gender VARCHAR(1) NOT NULL CHECK (principal_gender IN ('M', 'F')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_school UNIQUE (user_id)
);

-- Index for fast lookup by user_id
CREATE INDEX idx_teacher_school_user ON teacher_school(user_id);

-- Index for queries by school_id
CREATE INDEX idx_teacher_school_school ON teacher_school(school_id);

-- Trigger to auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_teacher_school_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_teacher_school_updated_at
BEFORE UPDATE ON teacher_school
FOR EACH ROW
EXECUTE FUNCTION update_teacher_school_updated_at();

COMMENT ON TABLE teacher_school IS 'One-to-one association between teachers and schools with principal metadata';
COMMENT ON COLUMN teacher_school.user_id IS 'References auth_service.users.id (cross-service reference, validated at JWT level)';
COMMENT ON COLUMN teacher_school.school_id IS 'Foreign key to schools table';
COMMENT ON COLUMN teacher_school.principal_name IS 'Name of the school principal';
COMMENT ON COLUMN teacher_school.principal_gender IS 'Principal gender: M (Male) or F (Female)';
