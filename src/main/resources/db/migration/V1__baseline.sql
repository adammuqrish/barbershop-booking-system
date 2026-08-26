-- Flyway Baseline Migration
-- Creates the full schema so a fresh database (local dev or new prod) works.
-- Previously this file was an empty placeholder that assumed Hibernate's
-- ddl-auto=update had already created the tables - which broke Flyway on any
-- fresh database because Flyway runs BEFORE Hibernate schema generation.
-- Created: 2025-12-23, rewritten 2026-08-25 to be executable.

CREATE TABLE IF NOT EXISTS customers (
    cust_id            BIGSERIAL PRIMARY KEY,
    cust_name          VARCHAR(255),
    cust_email         VARCHAR(255) UNIQUE,
    cust_password      VARCHAR(255),
    cust_phone_number  VARCHAR(255),
    cust_picture       VARCHAR(255),
    cust_loyalty_points INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS staffs (
    staff_id           BIGSERIAL PRIMARY KEY,
    staff_name         VARCHAR(255),
    staff_email        VARCHAR(255) UNIQUE,
    staff_password     VARCHAR(255),
    staff_phone_number VARCHAR(255),
    staff_picture      VARCHAR(255),
    staff_description  VARCHAR(255),
    staff_role         VARCHAR(255),
    admin_id           BIGINT
);

CREATE TABLE IF NOT EXISTS appointments (
    appointment_id     BIGSERIAL PRIMARY KEY,
    cust_id            BIGINT,
    barber_id          BIGINT,
    appointment_date   VARCHAR(255),
    appointment_time   VARCHAR(255),
    payment_status     VARCHAR(255),
    value_loyalty      INTEGER DEFAULT 0,
    cust_type          VARCHAR(255),
    service_status     VARCHAR(255) DEFAULT 'pending',
    cust_book_for      VARCHAR(255),
    customer_name      VARCHAR(255),
    appointment_barber VARCHAR(255),
    payment_method     VARCHAR(255),
    updated_by         BIGINT,
    updated_at         TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payments (
    payment_id      BIGSERIAL PRIMARY KEY,
    payment_date    DATE DEFAULT CURRENT_DATE,
    payment_amount  NUMERIC(10,2),
    payment_method  VARCHAR(255),
    appointment_id  BIGINT
);

CREATE TABLE IF NOT EXISTS online_payments (
    payment_id       BIGINT PRIMARY KEY REFERENCES payments (payment_id),
    bank_name        VARCHAR(255),
    bank_holder_name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS cashes (
    payment_id   BIGINT PRIMARY KEY REFERENCES payments (payment_id),
    cash_receive DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS feedbacks (
    feedback_id    BIGSERIAL PRIMARY KEY,
    comments       VARCHAR(1000),
    rating         INTEGER,
    appointment_id BIGINT
);

CREATE TABLE IF NOT EXISTS bookings (
    id               BIGSERIAL PRIMARY KEY,
    customer_name    VARCHAR(255),
    service_type     VARCHAR(255),
    appointment_time VARCHAR(255)
);
