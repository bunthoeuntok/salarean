-- V9: Create semester_configs table and add SEMESTER_1/SEMESTER_2 assessment types
-- Feature: Configurable semester exam schedule by academic year
-- Purpose: Store which monthly exams belong to each semester and their corresponding months

BEGIN;

-- Add SEMESTER_1 and SEMESTER_2 assessment types (replacing single SEMESTER)
-- First, update existing SEMESTER to SEMESTER_1
UPDATE assessment_types
SET code = 'SEMESTER_1',
    name = 'Semester 1 Exam',
    name_km = 'ប្រឡងឆមាស ១',
    description = 'First semester examination'
WHERE code = 'SEMESTER';

-- Insert SEMESTER_2
INSERT INTO assessment_types (name, name_km, code, category, default_weight, max_score, description, display_order)
VALUES ('Semester 2 Exam', 'ប្រឡងឆមាស ២', 'SEMESTER_2', 'SEMESTER_EXAM', 50.00, 100, 'Second semester examination', 6);

-- Semester configuration table
CREATE TABLE IF NOT EXISTS semester_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID,  -- NULL means default/system config
    academic_year VARCHAR(20) NOT NULL,
    semester_exam_code VARCHAR(30) NOT NULL,  -- References assessment_types.code (SEMESTER_1, SEMESTER_2)
    exam_schedule JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Unique constraint: one config per teacher/academic_year/semester_exam_code
    -- Uses COALESCE to handle NULL teacher_id (default configs)
    CONSTRAINT uk_semester_config UNIQUE (
        COALESCE(teacher_id, '00000000-0000-0000-0000-000000000000'),
        academic_year,
        semester_exam_code
    )
);

-- Indexes for performance
CREATE INDEX idx_semester_config_teacher ON semester_configs(teacher_id) WHERE teacher_id IS NOT NULL;
CREATE INDEX idx_semester_config_academic_year ON semester_configs(academic_year);
CREATE INDEX idx_semester_config_default ON semester_configs(academic_year, semester_exam_code) WHERE teacher_id IS NULL;

-- Comments
COMMENT ON TABLE semester_configs IS 'Semester exam schedule configuration per academic year';
COMMENT ON COLUMN semester_configs.teacher_id IS 'Teacher ID for custom config, NULL for default/system config';
COMMENT ON COLUMN semester_configs.academic_year IS 'Academic year in format YYYY-YYYY (e.g., 2024-2025)';
COMMENT ON COLUMN semester_configs.semester_exam_code IS 'Semester exam assessment type code (SEMESTER_1, SEMESTER_2)';
COMMENT ON COLUMN semester_configs.exam_schedule IS 'JSON array of exam schedule items [{assessmentCode, month, displayOrder}]';

-- Insert default configurations for 2024-2025 academic year
-- Semester 1: November - March (exams in Nov, Dec, Jan, Feb, semester exam in Mar)
INSERT INTO semester_configs (teacher_id, academic_year, semester_exam_code, exam_schedule)
VALUES (
    NULL,  -- Default config
    '2024-2025',
    'SEMESTER_1',
    '[
        {"assessmentCode": "MONTHLY_1", "month": 11, "displayOrder": 1},
        {"assessmentCode": "MONTHLY_2", "month": 12, "displayOrder": 2},
        {"assessmentCode": "MONTHLY_3", "month": 1, "displayOrder": 3},
        {"assessmentCode": "MONTHLY_4", "month": 2, "displayOrder": 4},
        {"assessmentCode": "SEMESTER_1", "month": 3, "displayOrder": 5}
    ]'::jsonb
);

-- Semester 2: April - August (exams in Apr, May, Jun, Jul, semester exam in Aug)
INSERT INTO semester_configs (teacher_id, academic_year, semester_exam_code, exam_schedule)
VALUES (
    NULL,  -- Default config
    '2024-2025',
    'SEMESTER_2',
    '[
        {"assessmentCode": "MONTHLY_1", "month": 4, "displayOrder": 1},
        {"assessmentCode": "MONTHLY_2", "month": 5, "displayOrder": 2},
        {"assessmentCode": "MONTHLY_3", "month": 6, "displayOrder": 3},
        {"assessmentCode": "MONTHLY_4", "month": 7, "displayOrder": 4},
        {"assessmentCode": "SEMESTER_2", "month": 8, "displayOrder": 5}
    ]'::jsonb
);

-- Insert default configurations for 2025-2026 academic year
INSERT INTO semester_configs (teacher_id, academic_year, semester_exam_code, exam_schedule)
VALUES (
    NULL,
    '2025-2026',
    'SEMESTER_1',
    '[
        {"assessmentCode": "MONTHLY_1", "month": 11, "displayOrder": 1},
        {"assessmentCode": "MONTHLY_2", "month": 12, "displayOrder": 2},
        {"assessmentCode": "MONTHLY_3", "month": 1, "displayOrder": 3},
        {"assessmentCode": "MONTHLY_4", "month": 2, "displayOrder": 4},
        {"assessmentCode": "SEMESTER_1", "month": 3, "displayOrder": 5}
    ]'::jsonb
);

INSERT INTO semester_configs (teacher_id, academic_year, semester_exam_code, exam_schedule)
VALUES (
    NULL,
    '2025-2026',
    'SEMESTER_2',
    '[
        {"assessmentCode": "MONTHLY_1", "month": 4, "displayOrder": 1},
        {"assessmentCode": "MONTHLY_2", "month": 5, "displayOrder": 2},
        {"assessmentCode": "MONTHLY_3", "month": 6, "displayOrder": 3},
        {"assessmentCode": "MONTHLY_4", "month": 7, "displayOrder": 4},
        {"assessmentCode": "SEMESTER_2", "month": 8, "displayOrder": 5}
    ]'::jsonb
);

COMMIT;
