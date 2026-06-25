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
        'LEDGER_TRANSACTION_CREATED'
    )
);
