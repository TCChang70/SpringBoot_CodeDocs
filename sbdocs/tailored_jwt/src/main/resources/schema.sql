-- src/main/resources/schema.sql
-- SQLite 建表腳本（需搭配 spring.jpa.hibernate.ddl-auto=none）
-- 說明：Hibernate 7.4 的 SQLite 社群 Dialect 在某些關聯實體上會產生「無型別 id」
--       （不是 INTEGER PRIMARY KEY），導致 SQLite 不會自動給主鍵編號。
--       因此改用手動 schema.sql，明確宣告 INTEGER PRIMARY KEY AUTOINCREMENT。

DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    username   varchar(255) NOT NULL UNIQUE,
    password   varchar(255) NOT NULL,
    role       varchar(255) NOT NULL,
    created_at timestamp
);

CREATE TABLE categories (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name varchar(255) NOT NULL UNIQUE
);

CREATE TABLE products (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        varchar(255) NOT NULL,
    brand       varchar(255) NOT NULL,
    price       float NOT NULL,
    stock       integer,
    category_id bigint,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE TABLE orders (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    order_no      varchar(255) NOT NULL UNIQUE,
    customer_name varchar(255) NOT NULL,
    order_date    timestamp,
    total_amount  float
);

CREATE TABLE order_items (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id   bigint,
    product_name varchar(255) NOT NULL,
    price        float NOT NULL,
    quantity     integer NOT NULL,
    order_id     bigint,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id)
);
