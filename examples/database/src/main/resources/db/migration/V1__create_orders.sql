-- V1: Create orders and order_items tables
-- Flyway naming convention: V{version}__{description}.sql

CREATE TABLE orders (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100)   NOT NULL,
    status        VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    total_amount  DECIMAL(12, 2) NOT NULL DEFAULT 0,
    version       INT            NOT NULL DEFAULT 0,
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_customer ON orders (customer_name);

CREATE TABLE order_items (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT         NOT NULL,
    product_name VARCHAR(255)   NOT NULL,
    quantity     INT            NOT NULL,
    unit_price   DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX idx_order_items_order ON order_items (order_id);
