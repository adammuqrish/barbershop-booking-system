-- Add audit tracking fields to appointments table
-- Tracks who updated the appointment and when
-- Created: 2025-12-23

ALTER TABLE appointments 
ADD COLUMN updated_by BIGINT,
ADD COLUMN updated_at TIMESTAMP;

-- Add comment for documentation
COMMENT ON COLUMN appointments.updated_by IS 'Staff ID who last updated this appointment';
COMMENT ON COLUMN appointments.updated_at IS 'Timestamp of last update';
