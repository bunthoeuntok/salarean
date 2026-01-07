-- V18: Create time slot templates table with default templates
-- Stores predefined and custom time slot configurations for class schedules

CREATE TABLE time_slot_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID,  -- NULL for system defaults
    name VARCHAR(100) NOT NULL,
    name_km VARCHAR(100),  -- Khmer name
    shift VARCHAR(20) NOT NULL,
    slots JSONB NOT NULL,  -- [{periodNumber, startTime, endTime, label, labelKm, isBreak}]
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT time_slot_templates_shift_check CHECK (shift IN ('MORNING', 'AFTERNOON', 'FULLDAY'))
);

-- Index for quick lookup by teacher and shift
CREATE INDEX idx_time_slot_templates_teacher_id ON time_slot_templates(teacher_id);
CREATE INDEX idx_time_slot_templates_shift ON time_slot_templates(shift);

-- Insert default MORNING template (5 periods)
INSERT INTO time_slot_templates (id, teacher_id, name, name_km, shift, slots, is_default) VALUES (
    'a0000000-0000-0000-0000-000000000001',
    NULL,
    'Morning Standard',
    'វេនព្រឹកស្តង់ដារ',
    'MORNING',
    '[
        {"periodNumber": 1, "startTime": "07:00", "endTime": "07:45", "label": "Period 1", "labelKm": "មុខវិជ្ជាទី១", "isBreak": false},
        {"periodNumber": 2, "startTime": "07:45", "endTime": "08:30", "label": "Period 2", "labelKm": "មុខវិជ្ជាទី២", "isBreak": false},
        {"periodNumber": 3, "startTime": "08:30", "endTime": "09:15", "label": "Period 3", "labelKm": "មុខវិជ្ជាទី៣", "isBreak": false},
        {"periodNumber": null, "startTime": "09:15", "endTime": "09:30", "label": "Break", "labelKm": "សម្រាក", "isBreak": true},
        {"periodNumber": 4, "startTime": "09:30", "endTime": "10:15", "label": "Period 4", "labelKm": "មុខវិជ្ជាទី៤", "isBreak": false},
        {"periodNumber": 5, "startTime": "10:15", "endTime": "11:00", "label": "Period 5", "labelKm": "មុខវិជ្ជាទី៥", "isBreak": false}
    ]'::jsonb,
    true
);

-- Insert default AFTERNOON template (5 periods)
INSERT INTO time_slot_templates (id, teacher_id, name, name_km, shift, slots, is_default) VALUES (
    'a0000000-0000-0000-0000-000000000002',
    NULL,
    'Afternoon Standard',
    'វេនរសៀលស្តង់ដារ',
    'AFTERNOON',
    '[
        {"periodNumber": 1, "startTime": "13:00", "endTime": "13:45", "label": "Period 1", "labelKm": "មុខវិជ្ជាទី១", "isBreak": false},
        {"periodNumber": 2, "startTime": "13:45", "endTime": "14:30", "label": "Period 2", "labelKm": "មុខវិជ្ជាទី២", "isBreak": false},
        {"periodNumber": 3, "startTime": "14:30", "endTime": "15:15", "label": "Period 3", "labelKm": "មុខវិជ្ជាទី៣", "isBreak": false},
        {"periodNumber": null, "startTime": "15:15", "endTime": "15:30", "label": "Break", "labelKm": "សម្រាក", "isBreak": true},
        {"periodNumber": 4, "startTime": "15:30", "endTime": "16:15", "label": "Period 4", "labelKm": "មុខវិជ្ជាទី៤", "isBreak": false},
        {"periodNumber": 5, "startTime": "16:15", "endTime": "17:00", "label": "Period 5", "labelKm": "មុខវិជ្ជាទី៥", "isBreak": false}
    ]'::jsonb,
    true
);

-- Insert default FULLDAY template (10 periods)
INSERT INTO time_slot_templates (id, teacher_id, name, name_km, shift, slots, is_default) VALUES (
    'a0000000-0000-0000-0000-000000000003',
    NULL,
    'Full Day Standard',
    'ពេញថ្ងៃស្តង់ដារ',
    'FULLDAY',
    '[
        {"periodNumber": 1, "startTime": "07:00", "endTime": "07:45", "label": "Period 1", "labelKm": "មុខវិជ្ជាទី១", "isBreak": false},
        {"periodNumber": 2, "startTime": "07:45", "endTime": "08:30", "label": "Period 2", "labelKm": "មុខវិជ្ជាទី២", "isBreak": false},
        {"periodNumber": 3, "startTime": "08:30", "endTime": "09:15", "label": "Period 3", "labelKm": "មុខវិជ្ជាទី៣", "isBreak": false},
        {"periodNumber": null, "startTime": "09:15", "endTime": "09:30", "label": "Morning Break", "labelKm": "សម្រាកព្រឹក", "isBreak": true},
        {"periodNumber": 4, "startTime": "09:30", "endTime": "10:15", "label": "Period 4", "labelKm": "មុខវិជ្ជាទី៤", "isBreak": false},
        {"periodNumber": 5, "startTime": "10:15", "endTime": "11:00", "label": "Period 5", "labelKm": "មុខវិជ្ជាទី៥", "isBreak": false},
        {"periodNumber": null, "startTime": "11:00", "endTime": "13:00", "label": "Lunch Break", "labelKm": "សម្រាកអាហារថ្ងៃត្រង់", "isBreak": true},
        {"periodNumber": 6, "startTime": "13:00", "endTime": "13:45", "label": "Period 6", "labelKm": "មុខវិជ្ជាទី៦", "isBreak": false},
        {"periodNumber": 7, "startTime": "13:45", "endTime": "14:30", "label": "Period 7", "labelKm": "មុខវិជ្ជាទី៧", "isBreak": false},
        {"periodNumber": 8, "startTime": "14:30", "endTime": "15:15", "label": "Period 8", "labelKm": "មុខវិជ្ជាទី៨", "isBreak": false},
        {"periodNumber": null, "startTime": "15:15", "endTime": "15:30", "label": "Afternoon Break", "labelKm": "សម្រាករសៀល", "isBreak": true},
        {"periodNumber": 9, "startTime": "15:30", "endTime": "16:15", "label": "Period 9", "labelKm": "មុខវិជ្ជាទី៩", "isBreak": false},
        {"periodNumber": 10, "startTime": "16:15", "endTime": "17:00", "label": "Period 10", "labelKm": "មុខវិជ្ជាទី១០", "isBreak": false}
    ]'::jsonb,
    true
);

COMMENT ON TABLE time_slot_templates IS 'Stores time slot configurations for class schedules';
COMMENT ON COLUMN time_slot_templates.teacher_id IS 'NULL for system defaults, teacher UUID for custom templates';
COMMENT ON COLUMN time_slot_templates.slots IS 'JSON array of time slots with period info';
