ALTER TABLE payment
DROP
COLUMN payment_amount;

ALTER TABLE payment
    ADD payment_amount BIGINT;

ALTER TABLE my_table
ALTER
COLUMN total_amount TYPE DECIMAL USING (total_amount::DECIMAL);