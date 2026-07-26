ALTER TABLE sales_orders
DROP CONSTRAINT chk_sales_orders_status;

ALTER TABLE sales_orders
ADD CONSTRAINT chk_sales_orders_status CHECK (
    status IN ('DRAFT', 'CONFIRMED', 'PAID', 'SHIPPED', 'REFUNDED', 'CANCELLED')
);

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
        'SALES_ORDER_REFUNDED',
        'LEDGER_TRANSACTION_CREATED',
        'USER_CREATED',
        'USER_BOOTSTRAPPED'
    )
);

ALTER TABLE ledger_transactions
DROP CONSTRAINT chk_ledger_transactions_amount_positive;

ALTER TABLE ledger_transactions
ADD CONSTRAINT chk_ledger_transactions_amount CHECK (
    (type = 'ADJUSTMENT' AND amount <> 0)
    OR (type <> 'ADJUSTMENT' AND amount > 0)
);
