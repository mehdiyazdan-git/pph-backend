ALTER TABLE contracts
DROP
COLUMN advance_payment;

ALTER TABLE contracts
DROP
COLUMN insurance_deposit;

ALTER TABLE contracts
DROP
COLUMN performance_bond;

ALTER TABLE contracts
    ADD advance_payment DOUBLE PRECISION;

ALTER TABLE contracts
    ADD insurance_deposit DOUBLE PRECISION;

ALTER TABLE contracts
    ADD performance_bond DOUBLE PRECISION;