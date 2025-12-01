-- V9: Add level and type columns to classes table

-- Add level column (enum: PRIMARY, SECONDARY, HIGH_SCHOOL)
ALTER TABLE classes ADD COLUMN level VARCHAR(20);
UPDATE classes SET level = 'PRIMARY' WHERE level IS NULL;
ALTER TABLE classes ALTER COLUMN level SET NOT NULL;
ALTER TABLE classes ALTER COLUMN level SET DEFAULT 'PRIMARY';
ALTER TABLE classes ADD CONSTRAINT classes_level_check
    CHECK (level IN ('PRIMARY', 'SECONDARY', 'HIGH_SCHOOL'));

-- Add type column (enum: NORMAL, SCIENCE, SOCIAL_SCIENCE)
ALTER TABLE classes ADD COLUMN type VARCHAR(20);
UPDATE classes SET type = 'NORMAL' WHERE type IS NULL;
ALTER TABLE classes ALTER COLUMN type SET NOT NULL;
ALTER TABLE classes ALTER COLUMN type SET DEFAULT 'NORMAL';
ALTER TABLE classes ADD CONSTRAINT classes_type_check
    CHECK (type IN ('NORMAL', 'SCIENCE', 'SOCIAL_SCIENCE'));

-- Create index on level for filtering
CREATE INDEX idx_classes_level ON classes(level);

-- Create index on type for filtering
CREATE INDEX idx_classes_type ON classes(type);

-- Create composite index for level and type combination filtering
CREATE INDEX idx_classes_level_type ON classes(level, type);
