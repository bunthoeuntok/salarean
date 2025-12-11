-- V12__create_provinces_and_districts_tables.sql
-- Step 1: Create provinces table
CREATE TABLE provinces (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    name_km VARCHAR(100),
    code VARCHAR(10) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_provinces_code ON provinces(code);

CREATE TRIGGER update_provinces_updated_at
BEFORE UPDATE ON provinces
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE provinces IS 'Cambodia provinces (top-level administrative division)';
COMMENT ON COLUMN provinces.name_km IS 'Province name in Khmer';
COMMENT ON COLUMN provinces.code IS 'Province code (e.g., PP for Phnom Penh)';

-- Step 2: Create districts table
CREATE TABLE districts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    province_id UUID NOT NULL REFERENCES provinces(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    name_km VARCHAR(100),
    code VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_district_per_province UNIQUE (province_id, name)
);

CREATE INDEX idx_districts_province ON districts(province_id);
CREATE INDEX idx_districts_code ON districts(code);

CREATE TRIGGER update_districts_updated_at
BEFORE UPDATE ON districts
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE districts IS 'Cambodia districts (second-level administrative division)';
COMMENT ON COLUMN districts.province_id IS 'Foreign key to provinces table';
COMMENT ON COLUMN districts.name_km IS 'District name in Khmer';
