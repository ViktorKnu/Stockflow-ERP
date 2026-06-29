CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'MANAGER', 'EMPLOYEE'))
);

CREATE UNIQUE INDEX uk_users_email_lower ON users (LOWER(email));
CREATE INDEX idx_users_role ON users (role);

ALTER TABLE audit_logs
DROP CONSTRAINT chk_audit_logs_action;

ALTER TABLE audit_logs
ADD CONSTRAINT chk_audit_logs_action CHECK (
    action IN (
        'PRODUCT_CREATED',
        'PRODUCT_UPDATED',
        'PRODUCT_DELETED',
        'INVENTORY_MOVEMENT_CREATED',
        'PURCHASE_ORDER_RECEIVED',
        'SALES_ORDER_SHIPPED',
        'LEDGER_TRANSACTION_CREATED',
        'USER_CREATED'
    )
);
