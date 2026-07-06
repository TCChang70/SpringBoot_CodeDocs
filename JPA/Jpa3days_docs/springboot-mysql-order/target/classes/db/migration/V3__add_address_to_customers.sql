ALTER TABLE customers
    ADD COLUMN address VARCHAR(255) AFTER phone,
    ADD COLUMN city    VARCHAR(50)  AFTER address;
