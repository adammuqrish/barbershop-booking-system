-- Add audit tracking fields to appointments table
-- Tracks who updated the appointment and when
-- Created: 2025-12-23
--
-- Made idempotent: on databases created by the rewritten V1 baseline the
-- columns already exist, so guard each statement.

ALTER TABLE appointments ADD COLUMN IF NOT EXISTS updated_by BIGINT;
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

COMMENT ON COLUMN appointments.updated_by IS 'Staff ID who last updated this appointment';
COMMENT ON COLUMN appointments.updated_at IS 'Timestamp of last update';
