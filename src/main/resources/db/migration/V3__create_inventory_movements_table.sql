CREATE TABLE inventory_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    type VARCHAR(30) NOT NULL,
    quantity INTEGER NOT NULL,
    reason VARCHAR(500) NOT NULL,
    previous_quantity INTEGER NOT NULL,
    new_quantity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_inventory_movements_type CHECK (type IN ('IN', 'OUT', 'ADJUSTMENT')),
    CONSTRAINT chk_inventory_movements_quantity_non_negative CHECK (quantity >= 0),
    CONSTRAINT chk_inventory_movements_reason_not_blank CHECK (length(trim(reason)) > 0),
    CONSTRAINT chk_inventory_movements_previous_quantity_non_negative CHECK (previous_quantity >= 0),
    CONSTRAINT chk_inventory_movements_new_quantity_non_negative CHECK (new_quantity >= 0)
);

CREATE INDEX idx_inventory_movements_product_id ON inventory_movements (product_id);
CREATE INDEX idx_inventory_movements_created_at ON inventory_movements (created_at);
