CREATE TABLE ledger_transactions (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(30) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    description VARCHAR(500) NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    source_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_ledger_transactions_type CHECK (type IN ('REVENUE', 'EXPENSE', 'REFUND', 'ADJUSTMENT')),
    CONSTRAINT chk_ledger_transactions_source_type CHECK (source_type IN ('PURCHASE_ORDER', 'SALES_ORDER', 'MANUAL')),
    CONSTRAINT chk_ledger_transactions_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_ledger_transactions_type ON ledger_transactions (type);
CREATE INDEX idx_ledger_transactions_source ON ledger_transactions (source_type, source_id);
CREATE INDEX idx_ledger_transactions_created_at ON ledger_transactions (created_at);
