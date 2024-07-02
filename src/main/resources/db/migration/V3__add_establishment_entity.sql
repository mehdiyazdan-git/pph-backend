CREATE TABLE establishment
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    claims      DOUBLE PRECISION,
    customer_id BIGINT,
    CONSTRAINT pk_establishment PRIMARY KEY (id)
);

ALTER TABLE establishment
    ADD CONSTRAINT FK_ESTABLISHMENT_ON_CUSTOMER FOREIGN KEY (customer_id) REFERENCES customer (id);