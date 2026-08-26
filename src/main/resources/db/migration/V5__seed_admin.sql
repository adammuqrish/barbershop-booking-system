-- Seed default ADMIN credential (industry practice: single source via Flyway, idempotent)
-- Email: admin@hugibarbershop.com
-- Password: Admin@123  (BCrypt, cost 10)
-- Created: 2026-05-13 — requested before next admin-UI phase

INSERT INTO staffs (staff_name, staff_email, staff_password, staff_phone_number, staff_role, staff_description, admin_id)
VALUES (
  'System Admin',
  'admin@hugibarbershop.com',
  '$2a$10$JawnsJr71dqsdkw2mRHHn.eiuyYQTZqstEOvCj7vuAxrzrhCE/9WK',
  '0120000000',
  'ADMIN',
  'Default system administrator — change password after first login.',
  NULL
)
ON CONFLICT (staff_email) DO NOTHING;
