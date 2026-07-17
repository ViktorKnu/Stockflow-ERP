INSERT INTO suppliers (name, email, phone, address, created_at, updated_at)
SELECT 'Nordic Components AS', 'orders@nordic-components.demo', '+47 22 11 33 44',
       'Industriveien 12, 0661 Oslo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM suppliers WHERE lower(email) = 'orders@nordic-components.demo'
);

INSERT INTO suppliers (name, email, phone, address, created_at, updated_at)
SELECT 'Oslo Office Supply', 'sales@oslo-office.demo', '+47 22 55 77 99',
       'Kontorveien 8, 0484 Oslo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM suppliers WHERE lower(email) = 'sales@oslo-office.demo'
);

INSERT INTO products (
    name, sku, description, category, quantity, minimum_stock, price,
    supplier_id, version, created_at, updated_at
)
SELECT 'StockFlow Pro Laptop', 'DEMO-LAPTOP-001',
       'Bærbar PC for lager- og administrasjonsarbeid', 'Elektronikk',
       12, 5, 12999.00, s.id, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM suppliers s
WHERE lower(s.email) = 'orders@nordic-components.demo'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO products (
    name, sku, description, category, quantity, minimum_stock, price,
    supplier_id, version, created_at, updated_at
)
SELECT 'Håndholdt strekkodeskanner', 'DEMO-SCANNER-001',
       'Trådløs skanner for varemottak og plukk', 'Elektronikk',
       4, 6, 2499.00, s.id, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM suppliers s
WHERE lower(s.email) = 'orders@nordic-components.demo'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO products (
    name, sku, description, category, quantity, minimum_stock, price,
    supplier_id, version, created_at, updated_at
)
SELECT 'Fraktetiketter 100 x 150 mm', 'DEMO-LABEL-001',
       'Rull med 500 termiske fraktetiketter', 'Emballasje',
       120, 25, 149.90, s.id, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM suppliers s
WHERE lower(s.email) = 'sales@oslo-office.demo'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO inventory_movements (
    product_id, type, quantity, reason, previous_quantity, new_quantity, created_at
)
SELECT p.id, 'IN', p.quantity, 'Demo initial stock', 0, p.quantity, CURRENT_TIMESTAMP
FROM products p
WHERE p.sku LIKE 'DEMO-%'
  AND NOT EXISTS (
      SELECT 1
      FROM inventory_movements m
      WHERE m.product_id = p.id
        AND m.reason = 'Demo initial stock'
  );

INSERT INTO purchase_orders (supplier_id, status, total_amount, created_at, updated_at)
SELECT s.id, 'ORDERED', 12495.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM suppliers s
WHERE lower(s.email) = 'orders@nordic-components.demo'
  AND NOT EXISTS (
      SELECT 1
      FROM purchase_orders po
      WHERE po.supplier_id = s.id
        AND po.status = 'ORDERED'
        AND po.total_amount = 12495.00
  );

INSERT INTO purchase_order_items (
    purchase_order_id, product_id, quantity, unit_price, line_total
)
SELECT po.id, p.id, 5, 2499.00, 12495.00
FROM purchase_orders po
JOIN suppliers s ON s.id = po.supplier_id
JOIN products p ON p.sku = 'DEMO-SCANNER-001'
WHERE lower(s.email) = 'orders@nordic-components.demo'
  AND po.status = 'ORDERED'
  AND po.total_amount = 12495.00
  AND NOT EXISTS (
      SELECT 1
      FROM purchase_order_items poi
      WHERE poi.purchase_order_id = po.id
        AND poi.product_id = p.id
  );

INSERT INTO sales_orders (
    customer_name, customer_email, status, total_amount, created_at, updated_at
)
SELECT 'Fjordhandel AS', 'warehouse@fjordhandel.demo', 'CONFIRMED', 25998.00,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM sales_orders
    WHERE lower(customer_email) = 'warehouse@fjordhandel.demo'
      AND status = 'CONFIRMED'
      AND total_amount = 25998.00
);

INSERT INTO sales_order_items (
    sales_order_id, product_id, quantity, unit_price, line_total
)
SELECT so.id, p.id, 2, 12999.00, 25998.00
FROM sales_orders so
JOIN products p ON p.sku = 'DEMO-LAPTOP-001'
WHERE lower(so.customer_email) = 'warehouse@fjordhandel.demo'
  AND so.status = 'CONFIRMED'
  AND so.total_amount = 25998.00
  AND NOT EXISTS (
      SELECT 1
      FROM sales_order_items soi
      WHERE soi.sales_order_id = so.id
        AND soi.product_id = p.id
  );
