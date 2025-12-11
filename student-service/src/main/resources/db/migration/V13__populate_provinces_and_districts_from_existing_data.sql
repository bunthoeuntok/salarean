-- V13__populate_provinces_and_districts_from_existing_data.sql
-- Extract unique provinces from schools.province and insert into provinces table
INSERT INTO provinces (name)
SELECT DISTINCT TRIM(province)
FROM schools
WHERE province IS NOT NULL AND TRIM(province) != ''
ORDER BY TRIM(province);

-- Extract unique districts from schools.district and insert into districts table
INSERT INTO districts (province_id, name)
SELECT DISTINCT
    p.id AS province_id,
    TRIM(s.district) AS name
FROM schools s
JOIN provinces p ON TRIM(s.province) = p.name
WHERE s.district IS NOT NULL AND TRIM(s.district) != ''
ORDER BY p.id, TRIM(s.district);

-- Verification query (should return 0 for successful migration)
DO $$
DECLARE
    unmapped_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO unmapped_count
    FROM schools s
    WHERE s.province IS NOT NULL AND s.district IS NOT NULL
      AND NOT EXISTS (
          SELECT 1 FROM provinces p
          JOIN districts d ON d.province_id = p.id
          WHERE TRIM(s.province) = p.name AND TRIM(s.district) = d.name
      );

    IF unmapped_count > 0 THEN
        RAISE WARNING 'Migration incomplete: % schools have unmapped province/district combinations', unmapped_count;
    ELSE
        RAISE NOTICE 'Migration successful: All schools have mapped provinces and districts';
    END IF;
END $$;
