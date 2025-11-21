-- V4: Create views and utility functions

-- Current class roster view (for efficient class roster queries)
CREATE OR REPLACE VIEW v_current_class_roster AS
SELECT
    c.id as class_id,
    c.grade,
    c.section,
    c.academic_year,
    s.id as student_id,
    s.student_code,
    s.first_name,
    s.last_name,
    s.first_name_km,
    s.last_name_km,
    sce.enrollment_date
FROM student_class_enrollments sce
JOIN students s ON sce.student_id = s.id
JOIN classes c ON sce.class_id = c.id
WHERE sce.end_date IS NULL
  AND s.status = 'ACTIVE'
ORDER BY c.grade, c.section, s.last_name;

-- Utility function: Get student's current class
CREATE OR REPLACE FUNCTION get_student_current_class(p_student_id UUID)
RETURNS UUID AS $$
    SELECT class_id
    FROM student_class_enrollments
    WHERE student_id = p_student_id
      AND end_date IS NULL
    LIMIT 1;
$$ LANGUAGE sql STABLE;

-- Full-text search index for bilingual student search
CREATE INDEX IF NOT EXISTS idx_students_fulltext ON students USING GIN (
    to_tsvector('simple',
        COALESCE(first_name, '') || ' ' ||
        COALESCE(last_name, '') || ' ' ||
        COALESCE(first_name_km, '') || ' ' ||
        COALESCE(last_name_km, '') || ' ' ||
        COALESCE(student_code, '')
    )
);

-- Update timestamp trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply update trigger to all tables
CREATE TRIGGER update_schools_updated_at BEFORE UPDATE ON schools
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_classes_updated_at BEFORE UPDATE ON classes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_students_updated_at BEFORE UPDATE ON students
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_parent_contacts_updated_at BEFORE UPDATE ON parent_contacts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_enrollments_updated_at BEFORE UPDATE ON student_class_enrollments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
