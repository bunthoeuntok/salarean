-- V19: Create class schedules and schedule entries tables
-- Stores weekly timetable configuration for each class

CREATE TABLE class_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id UUID NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    time_slot_template_id UUID REFERENCES time_slot_templates(id),
    custom_slots JSONB,  -- Override template if customized
    academic_year VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT class_schedules_unique_class_year UNIQUE (class_id, academic_year)
);

CREATE TABLE schedule_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_schedule_id UUID NOT NULL REFERENCES class_schedules(id) ON DELETE CASCADE,
    day_of_week INTEGER NOT NULL,  -- 1=Monday, 2=Tuesday, ..., 6=Saturday
    period_number INTEGER NOT NULL,
    subject_id UUID NOT NULL,
    room VARCHAR(50),
    notes VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT schedule_entries_day_check CHECK (day_of_week BETWEEN 1 AND 6),
    CONSTRAINT schedule_entries_unique_slot UNIQUE (class_schedule_id, day_of_week, period_number)
);

-- Indexes for efficient queries
CREATE INDEX idx_class_schedules_class_id ON class_schedules(class_id);
CREATE INDEX idx_class_schedules_academic_year ON class_schedules(academic_year);
CREATE INDEX idx_schedule_entries_class_schedule_id ON schedule_entries(class_schedule_id);
CREATE INDEX idx_schedule_entries_day_period ON schedule_entries(day_of_week, period_number);
CREATE INDEX idx_schedule_entries_subject_id ON schedule_entries(subject_id);

-- Comments for documentation
COMMENT ON TABLE class_schedules IS 'Weekly schedule configuration for each class';
COMMENT ON COLUMN class_schedules.custom_slots IS 'Custom time slots, overrides template if set';
COMMENT ON TABLE schedule_entries IS 'Individual schedule entries for each day and period';
COMMENT ON COLUMN schedule_entries.day_of_week IS '1=Monday, 2=Tuesday, 3=Wednesday, 4=Thursday, 5=Friday, 6=Saturday';
COMMENT ON COLUMN schedule_entries.subject_id IS 'References subject in grade-service';
