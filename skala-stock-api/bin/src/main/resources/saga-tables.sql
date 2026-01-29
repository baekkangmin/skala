-- Order Table
DROP TABLE IF EXISTS saga_inventory;
DROP TABLE IF EXISTS saga_billing;
DROP TABLE IF EXISTS saga_order;

CREATE TABLE saga_order (
    order_id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    order_status ENUM('CREATED', 'CONFIRMED', 'CANCELED') NOT NULL,
    created_date DATETIME NOT NULL,
    updated_date DATETIME,

    id_insert VARCHAR(64) NULL,
    dt_insert DATETIME NULL,
    id_update VARCHAR(64) NULL,
    dt_update DATETIME NULL,

    PRIMARY KEY (order_id)
);

-- BillingRecord Table
CREATE TABLE saga_billing (
    billing_id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    order_description VARCHAR(255) NOT NULL,
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    billing_status ENUM('PENDING', 'COMPLETED', 'REFUNDED') NOT NULL,
    created_date DATETIME NOT NULL,
    updated_date DATETIME,

    id_insert VARCHAR(64) NULL,
    dt_insert DATETIME NULL,
    id_update VARCHAR(64) NULL,
    dt_update DATETIME NULL,

    PRIMARY KEY (billing_id),
    FOREIGN KEY (order_id) REFERENCES saga_order(order_id)
);

-- InventoryRecord Table
CREATE TABLE saga_inventory (
    inventory_id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    order_id BIGINT NOT NULL,
    order_description VARCHAR(255) NOT NULL,
    inventory_status ENUM('RESERVED', 'ALLOCATED', 'RELEASED') NOT NULL,
    reserved_date DATETIME NOT NULL,
    released_date DATETIME,

    id_insert VARCHAR(64) NULL,
    dt_insert DATETIME NULL,
    id_update VARCHAR(64) NULL,
    dt_update DATETIME NULL,

    PRIMARY KEY (inventory_id),
    FOREIGN KEY (order_id) REFERENCES saga_order(order_id)
);
