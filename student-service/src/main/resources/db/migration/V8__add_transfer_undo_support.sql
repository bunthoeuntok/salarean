-- =====================================================================
-- Migration: V8__add_transfer_undo_support
-- Description: Add columns to enrollment_history table to support
--              batch student transfer and undo functionality
-- Author: System Generated
-- Date: 2025-12-04
-- =====================================================================

-- Add new columns to enrollment_history table
ALTER TABLE enrollment_history
ADD COLUMN transfer_id UUID,
ADD COLUMN undo_of_transfer_id UUID,
ADD COLUMN performed_by_user_id UUID;

-- Backfill performed_by_user_id with 'system' UUID for existing records
-- This ensures NOT NULL constraint can be applied without breaking existing data
UPDATE enrollment_history
SET performed_by_user_id = '00000000-0000-0000-0000-000000000000'
WHERE performed_by_user_id IS NULL;

-- Make performed_by_user_id NOT NULL after backfill
ALTER TABLE enrollment_history
ALTER COLUMN performed_by_user_id SET NOT NULL;

-- Create indexes for efficient transfer and undo queries
-- Partial index on transfer_id (only rows with non-null transfer_id)
CREATE INDEX idx_enrollment_history_transfer_id
ON enrollment_history(transfer_id)
WHERE transfer_id IS NOT NULL;

-- Partial index on undo_of_transfer_id (only rows with non-null undo_of_transfer_id)
CREATE INDEX idx_enrollment_history_undo_of_transfer_id
ON enrollment_history(undo_of_transfer_id)
WHERE undo_of_transfer_id IS NOT NULL;

-- Composite index for conflict detection during undo
-- (checking if student has been transferred again after a specific timestamp)
CREATE INDEX idx_enrollment_history_performed_at
ON enrollment_history(performed_at DESC, student_id);

-- Add comments for documentation
COMMENT ON COLUMN enrollment_history.transfer_id IS 'Groups all students transferred together in a batch operation';
COMMENT ON COLUMN enrollment_history.undo_of_transfer_id IS 'References the transfer_id being undone';
COMMENT ON COLUMN enrollment_history.performed_by_user_id IS 'User who performed the enrollment action (required for undo authorization)';
