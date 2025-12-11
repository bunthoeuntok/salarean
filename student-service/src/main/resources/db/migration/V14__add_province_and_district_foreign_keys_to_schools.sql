-- V14__add_province_and_district_foreign_keys_to_schools.sql
-- Step 1: Add new columns (nullable initially)
ALTER TABLE schools
ADD COLUMN province_id UUID,
ADD COLUMN district_id UUID;

-- Step 2: Create indexes before FK constraints (improves performance)
CREATE INDEX idx_schools_province ON schools(province_id);
CREATE INDEX idx_schools_district ON schools(district_id);

-- Step 3: Backfill province_id and district_id by matching VARCHAR columns
UPDATE schools s
SET province_id = p.id,
    district_id = d.id
FROM provinces p
JOIN districts d ON d.province_id = p.id
WHERE TRIM(s.province) = p.name
  AND TRIM(s.district) = d.name;

-- Step 4: Add foreign key constraints
ALTER TABLE schools
ADD CONSTRAINT fk_schools_province FOREIGN KEY (province_id) REFERENCES provinces(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_schools_district FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE SET NULL;

-- Step 5: Add unique constraint for school names within districts
ALTER TABLE schools
ADD CONSTRAINT unique_school_per_district UNIQUE (district_id, name);

-- Verification: Check for schools with null foreign keys
DO $$
DECLARE
    null_fk_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO null_fk_count
    FROM schools
    WHERE province IS NOT NULL AND district IS NOT NULL
      AND (province_id IS NULL OR district_id IS NULL);

    IF null_fk_count > 0 THEN
        RAISE WARNING 'Backfill incomplete: % schools have null foreign keys', null_fk_count;
    ELSE
        RAISE NOTICE 'Backfill successful: All schools have valid foreign keys';
    END IF;
END $$;

COMMENT ON COLUMN schools.province_id IS 'Foreign key to provinces table (replaces VARCHAR province column)';
COMMENT ON COLUMN schools.district_id IS 'Foreign key to districts table (replaces VARCHAR district column)';
COMMENT ON CONSTRAINT unique_school_per_district ON schools IS 'Prevents duplicate school names within the same district';
