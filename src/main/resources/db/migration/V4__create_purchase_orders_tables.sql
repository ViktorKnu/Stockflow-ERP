CREATE TABLE purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL REFERENCES suppliers(id),
    status VARCHAR(30) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_purchase_orders_status CHECK (status IN ('DRAFT', 'ORDERED', 'RECEIVED', 'CANCELLED')),
    CONSTRAINT chk_purchase_orders_total_amount_non_negative CHECK (total_amount >= 0)
);

CREATE TABLE purchase_order_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    line_total NUMERIC(19, 2) NOT NULL,
    CONSTRAINT chk_purchase_order_items_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_purchase_order_items_unit_price_positive CHECK (unit_price > 0),
    CONSTRAINT chk_purchase_order_items_line_total_positive CHECK (line_total > 0)
);

CREATE INDEX idx_purchase_orders_supplier_id ON purchase_orders (supplier_id);
CREATE INDEX idx_purchase_orders_status ON purchase_orders (status);
CREATE INDEX idx_purchase_order_items_purchase_order_id ON purchase_order_items (purchase_order_id);
CREATE INDEX idx_purchase_order_items_product_id ON purchase_order_items (product_id);
