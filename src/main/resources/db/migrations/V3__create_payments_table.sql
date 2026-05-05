-- V2__create_payments_table.sql
CREATE TABLE IF NOT EXISTS payments (
                                        id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
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
    tax_amount DECIMAL(10,2) DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    coupon_code VARCHAR(50),
    payment_description VARCHAR(500),
    billing_address VARCHAR(500),
    billing_city VARCHAR(100),
    billing_country VARCHAR(2),
    billing_postal_code VARCHAR(20),
    failure_reason TEXT,
    failure_code VARCHAR(50),
    retry_count INT DEFAULT 0,
    metadata TEXT,
    webhook_received BOOLEAN DEFAULT FALSE,
    webhook_processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id),

    -- Composite indexes for common queries
    INDEX idx_payment_user (user_id),
    INDEX idx_payment_course (course_id),
    INDEX idx_payment_transaction (transaction_id),
    INDEX idx_payment_status (status),
    INDEX idx_payment_date (payment_date),
    INDEX idx_payment_user_status (user_id, status),
    INDEX idx_payment_course_status (course_id, status),
    INDEX idx_payment_created (created_at)
    );

-- Create partial index for completed payments
CREATE INDEX idx_payment_completed ON payments(payment_date) WHERE status = 'COMPLETED';

-- Create partial index for refunded payments
CREATE INDEX idx_payment_refunded ON payments(refund_date) WHERE status = 'REFUNDED';