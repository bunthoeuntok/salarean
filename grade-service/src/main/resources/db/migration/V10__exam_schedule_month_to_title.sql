-- V10: Change exam_schedule JSON structure from month (number) to title (string)
-- Purpose: Use assessment type's Khmer name as title instead of month number
-- Before: {"assessmentCode": "MONTHLY_1", "month": 11, "displayOrder": 1}
-- After:  {"assessmentCode": "MONTHLY_1", "title": "ប្រឡងប្រចាំខែ ១", "displayOrder": 1}

BEGIN;

-- Update all existing semester_configs records to use title (name_km from assessment_types) instead of month
UPDATE semester_configs sc
SET exam_schedule = (
    SELECT jsonb_agg(
        jsonb_build_object(
            'assessmentCode', item->>'assessmentCode',
            'title', COALESCE(at.name_km, item->>'assessmentCode'),
            'displayOrder', (item->>'displayOrder')::INTEGER
        )
        ORDER BY (item->>'displayOrder')::INTEGER
    )
    FROM jsonb_array_elements(sc.exam_schedule) AS item
    LEFT JOIN assessment_types at ON at.code = item->>'assessmentCode'
);

-- Update comment
COMMENT ON COLUMN semester_configs.exam_schedule IS 'JSON array of exam schedule items [{assessmentCode, title (from assessment_types.name_km), displayOrder}]';

COMMIT;
