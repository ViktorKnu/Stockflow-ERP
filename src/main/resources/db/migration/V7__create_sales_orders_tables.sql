CREATE TABLE sales_orders (
    id BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(160) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_sales_orders_status CHECK (status IN ('DRAFT', 'CONFIRMED', 'PAID', 'SHIPPED', 'CANCELLED')),
    CONSTRAINT chk_sales_orders_total_amount_non_negative CHECK (total_amount >= 0)
);

CREATE TABLE sales_order_items (
    id BIGSERIAL PRIMARY KEY,
    sales_order_id BIGINT NOT NULL REFERENCES sales_orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    line_total NUMERIC(19, 2) NOT NULL,
    CONSTRAINT chk_sales_order_items_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_sales_order_items_unit_price_positive CHECK (unit_price > 0),
    CONSTRAINT chk_sales_order_items_line_total_positive CHECK (line_total > 0)
);

CREATE INDEX idx_sales_orders_status ON sales_orders (status);
CREATE INDEX idx_sales_order_items_sales_order_id ON sales_order_items (sales_order_id);
CREATE INDEX idx_sales_order_items_product_id ON sales_order_items (product_id);
