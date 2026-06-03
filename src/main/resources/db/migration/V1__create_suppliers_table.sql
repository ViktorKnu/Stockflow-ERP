CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    address VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_suppliers_email_not_blank CHECK (length(trim(email)) > 0),
    CONSTRAINT chk_suppliers_name_not_blank CHECK (length(trim(name)) > 0)
);
