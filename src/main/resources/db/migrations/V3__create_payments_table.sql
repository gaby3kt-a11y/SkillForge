-- V3__create_payments_table.sql
-- Postgres-oriented payments schema + indexes.

CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    course_id VARCHAR(36) NOT NULL REFERENCES courses(id),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    payment_method VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(100) UNIQUE,
    stripe_payment_intent_id VARCHAR(100),
    paypal_order_id VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_date TIMESTAMP,
    refund_date TIMESTAMP,
    refund_reason TEXT,
    refund_amount DECIMAL(10,2),
    invoice_url VARCHAR(500),
    receipt_url VARCHAR(500),
    platform_fee DECIMAL(10,2),
    instructor_earnings DECIMAL(10,2),
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    coupon_code VARCHAR(50),
    payment_description VARCHAR(500),
    billing_address VARCHAR(500),
    billing_city VARCHAR(100),
    billing_country VARCHAR(2),
    billing_postal_code VARCHAR(20),
    failure_reason TEXT,
    failure_code VARCHAR(50),
    retry_count INT NOT NULL DEFAULT 0,
    metadata TEXT,
    webhook_received BOOLEAN NOT NULL DEFAULT FALSE,
    webhook_processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payment_user ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_course ON payments(course_id);
CREATE INDEX IF NOT EXISTS idx_payment_transaction ON payments(transaction_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payment_date ON payments(payment_date);
CREATE INDEX IF NOT EXISTS idx_payment_user_status ON payments(user_id, status);
CREATE INDEX IF NOT EXISTS idx_payment_course_status ON payments(course_id, status);
CREATE INDEX IF NOT EXISTS idx_payment_created ON payments(created_at);

CREATE INDEX IF NOT EXISTS idx_payment_completed ON payments(payment_date) WHERE status = 'COMPLETED';
CREATE INDEX IF NOT EXISTS idx_payment_refunded ON payments(refund_date) WHERE status = 'REFUNDED';