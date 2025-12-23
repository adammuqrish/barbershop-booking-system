-- Fix payment amount data type from DOUBLE PRECISION to NUMERIC
-- NUMERIC provides exact precision for financial calculations
-- Created: 2025-12-23

ALTER TABLE payments 
ALTER COLUMN payment_amount TYPE NUMERIC(10,2);

-- Also update child tables if they have the column
-- (In JOINED inheritance strategy, the column is only in parent table)
