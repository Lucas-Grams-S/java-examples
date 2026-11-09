CREATE TABLE IF NOT EXISTS products (id BIGINT PRIMARY KEY, name VARCHAR(80) NOT NULL, price_cents BIGINT NOT NULL, stock INT NOT NULL);
CREATE TABLE IF NOT EXISTS reservations (order_id BIGINT PRIMARY KEY, product_id BIGINT NOT NULL, quantity INT NOT NULL);
MERGE INTO products KEY(id) VALUES (1, 'Teclado', 15000, 100);
MERGE INTO products KEY(id) VALUES (2, 'Mouse', 5000, 100);
MERGE INTO products KEY(id) VALUES (3, 'Monitor', 90000, 5);
