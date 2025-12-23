-- Flyway Baseline Migration
-- This migration documents the current database schema state
-- Created: 2025-12-23

-- Note: This is a baseline migration to allow Flyway to track schema from current state
-- The actual tables already exist (created by JPA hibernate.ddl-auto=update)
-- We use baseline-on-migrate=true in application.properties to skip this migration

-- Tables that already exist:
-- - staffs
-- - customers  
-- - appointments
-- - payments
-- - online_payments (inherits from payments)
-- - cash_payments (inherits from payments)
-- - feedbacks
-- - bookings

-- This file serves as documentation only
