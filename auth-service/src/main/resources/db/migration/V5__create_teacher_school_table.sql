-- V5__create_teacher_school_table.sql
CREATE TABLE teacher_school (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    school_id UUID NOT NULL,
    principal_name VARCHAR(255) NOT NULL,
    principal_gender VARCHAR(1) NOT NULL CHECK (principal_gender IN ('M', 'F')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_school UNIQUE (user_id)
);

CREATE INDEX idx_teacher_school_user ON teacher_school(user_id);
CREATE INDEX idx_teacher_school_school ON teacher_school(school_id);

-- Reuse existing trigger function for updated_at
CREATE TRIGGER update_teacher_school_updated_at
BEFORE UPDATE ON teacher_school
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE teacher_school IS 'One-to-one association between teachers and schools with principal metadata';
COMMENT ON COLUMN teacher_school.school_id IS 'References student_service.schools.id (validated at application layer)';
COMMENT ON COLUMN teacher_school.principal_name IS 'Name of the school principal';
COMMENT ON COLUMN teacher_school.principal_gender IS 'Principal gender: M (Male) or F (Female)';
