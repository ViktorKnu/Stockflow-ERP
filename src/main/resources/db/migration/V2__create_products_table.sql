CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    sku VARCHAR(80) NOT NULL,
    description VARCHAR(1000),
    category VARCHAR(120) NOT NULL,
    quantity INTEGER NOT NULL,
    minimum_stock INTEGER NOT NULL,
    price NUMERIC(19, 2) NOT NULL,
    supplier_id BIGINT REFERENCES suppliers(id),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT chk_products_name_not_blank CHECK (length(trim(name)) > 0),
    CONSTRAINT chk_products_sku_not_blank CHECK (length(trim(sku)) > 0),
    CONSTRAINT chk_products_category_not_blank CHECK (length(trim(category)) > 0),
    CONSTRAINT chk_products_quantity_non_negative CHECK (quantity >= 0),
    CONSTRAINT chk_products_minimum_stock_non_negative CHECK (minimum_stock >= 0),
    CONSTRAINT chk_products_price_positive CHECK (price > 0)
);

CREATE INDEX idx_products_name ON products (lower(name));
CREATE INDEX idx_products_category ON products (lower(category));
CREATE INDEX idx_products_supplier_id ON products (supplier_id);
