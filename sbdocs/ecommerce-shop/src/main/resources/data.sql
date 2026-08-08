-- src/main/resources/data.sql
-- 3C 商品分類
INSERT INTO ecommerce_db.categories (id, name) VALUES (1, '手機');
INSERT INTO ecommerce_db.categories (id, name) VALUES (2, '筆記型電腦');
INSERT INTO ecommerce_db.categories (id, name) VALUES (3, '耳機與音訊');
INSERT INTO ecommerce_db.categories (id, name) VALUES (4, '相機');

-- 3C 商品（category_id 對應上方 categories.id）
INSERT INTO ecommerce_db.products (name, brand, price, stock, category_id) VALUES
  ('iPhone 15 Pro',       'Apple',   39900.0, 20, 1),
  ('Samsung Galaxy S24',  'Samsung', 28900.0, 15, 1),
  ('MacBook Pro 14',      'Apple',   59900.0,  8, 2),
  ('ASUS ROG Zephyrus',   'ASUS',    42900.0, 10, 2),
  ('Sony WH-1000XM5',     'Sony',    10900.0, 50, 3),
  ('AirPods Pro 2',       'Apple',    7990.0, 30, 3),
  ('Canon EOS R8',        'Canon',   42900.0,  6, 4);

-- 訂單與訂單明細（order_items 的 order_id 對應 orders.id）
INSERT INTO ecommerce_db.orders (order_no, customer_name, order_date, total_amount) VALUES
  ('ORD-20260701001', 'Alice', '2026-07-01 10:00:00', 47890.0),
  ('ORD-20260710002', 'Bob',   '2026-07-10 14:30:00', 59900.0);

INSERT INTO ecommerce_db.order_items (product_id, product_name, price, quantity, order_id) VALUES
  (1, 'iPhone 15 Pro',      39900.0, 1, 1),
  (6, 'AirPods Pro 2',       7990.0, 1, 1),
  (3, 'MacBook Pro 14',     59900.0, 1, 2);