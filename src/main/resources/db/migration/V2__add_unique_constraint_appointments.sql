-- Add unique constraint to prevent double-booking
-- This ensures one barber cannot have two appointments at the same date and time
-- Created: 2025-12-23

ALTER TABLE appointments 
ADD CONSTRAINT unique_barber_slot 
UNIQUE (barber_id, appointment_date, appointment_time);
