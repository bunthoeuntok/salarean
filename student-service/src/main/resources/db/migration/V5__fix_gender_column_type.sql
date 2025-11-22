-- V5: Fix gender column type to CHAR(1) for JPA compatibility
ALTER TABLE students ALTER COLUMN gender TYPE CHAR(1) USING gender::CHAR(1);
