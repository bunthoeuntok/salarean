-- Add status field to track enrollment lifecycle
ALTER TABLE student_class_enrollments
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
CHECK (status IN ('ACTIVE', 'COMPLETED', 'TRANSFERRED', 'WITHDRAWN'));

-- Add transfer-specific fields
ALTER TABLE student_class_enrollments
ADD COLUMN transfer_date DATE NULL,
ADD COLUMN transfer_reason VARCHAR(500) NULL;

-- Create index for status queries
CREATE INDEX idx_enrollment_status ON student_class_enrollments(status);

-- Create composite index for status + date queries (history filtering)
CREATE INDEX idx_enrollment_status_date
ON student_class_enrollments(status, enrollment_date DESC);

-- Add constraint: transfer fields required when status is TRANSFERRED
ALTER TABLE student_class_enrollments
ADD CONSTRAINT chk_transfer_fields
CHECK (
    (status = 'TRANSFERRED' AND transfer_date IS NOT NULL AND transfer_reason IS NOT NULL)
    OR
    (status != 'TRANSFERRED')
);
