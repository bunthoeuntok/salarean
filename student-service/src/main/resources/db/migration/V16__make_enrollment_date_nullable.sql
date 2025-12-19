-- V16: Make enrollment_date nullable in students table
-- This allows creating students without immediate class enrollment

ALTER TABLE students ALTER COLUMN enrollment_date DROP NOT NULL;
