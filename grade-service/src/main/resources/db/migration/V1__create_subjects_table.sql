-- V1: Create subjects table (reference data for MoEYS curriculum)
-- Feature: grade-service initial schema
-- Purpose: Store standard subjects aligned with Cambodia education curriculum

BEGIN;

-- Subjects table (reference data)
CREATE TABLE IF NOT EXISTS subjects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    name_km VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(500),
    is_core BOOLEAN DEFAULT true,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes
CREATE INDEX idx_subjects_code ON subjects(code);
CREATE INDEX idx_subjects_is_core ON subjects(is_core);

-- Comments
COMMENT ON TABLE subjects IS 'Reference data for MoEYS curriculum subjects';
COMMENT ON COLUMN subjects.name IS 'Subject name in English';
COMMENT ON COLUMN subjects.name_km IS 'Subject name in Khmer';
COMMENT ON COLUMN subjects.code IS 'Unique subject code (e.g., KHM, MATH)';
COMMENT ON COLUMN subjects.is_core IS 'Whether this is a core/compulsory subject';
COMMENT ON COLUMN subjects.display_order IS 'Order for display in reports';

-- Insert standard Cambodian subjects (MoEYS curriculum)
INSERT INTO subjects (name, name_km, code, is_core, display_order) VALUES
    ('Khmer Language', 'ភាសាខ្មែរ', 'KHM', true, 1),
    ('Mathematics', 'គណិតវិទ្យា', 'MATH', true, 2),
    ('Science', 'វិទ្យាសាស្ត្រ', 'SCI', true, 3),
    ('Physics', 'រូបវិទ្យា', 'PHY', true, 4),
    ('Chemistry', 'គីមីវិទ្យា', 'CHEM', true, 5),
    ('Biology', 'ជីវវិទ្យា', 'BIO', true, 6),
    ('History', 'ប្រវត្តិវិទ្យា', 'HIST', true, 7),
    ('Geography', 'ភូមិវិទ្យា', 'GEO', true, 8),
    ('Civics', 'កិច្ចការសង្គម', 'CIV', true, 9),
    ('English', 'ភាសាអង់គ្លេស', 'ENG', true, 10),
    ('Physical Education', 'អប់រំកាយ', 'PE', false, 11),
    ('Art', 'សិល្បៈ', 'ART', false, 12),
    ('Music', 'តន្ត្រី', 'MUS', false, 13),
    ('Information Technology', 'ព័ត៌មានវិទ្យា', 'IT', false, 14),
    ('Morality', 'សីលធម៌', 'MOR', true, 15);

COMMIT;
