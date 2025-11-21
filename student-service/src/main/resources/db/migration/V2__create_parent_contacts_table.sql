-- V2: Create parent_contacts table

CREATE TABLE IF NOT EXISTS parent_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    relationship VARCHAR(50) NOT NULL CHECK (
        relationship IN ('MOTHER', 'FATHER', 'GUARDIAN', 'OTHER')
    ),
    is_primary BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_parent_contacts_student ON parent_contacts(student_id);
CREATE INDEX idx_parent_contacts_primary ON parent_contacts(student_id, is_primary) WHERE is_primary = TRUE;
