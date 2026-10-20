-- catalog (docs/specs/catalog/): the tables of product and price-list, with no foreign key to another context's tables.

-- product: a reference unique (BR-1), held by the store; 10 characters, as the contract's pattern bounds it. A label of
-- 120 characters (Character: a code point) is at most 240 UTF-16 units, as V1 sizes a text. Times in UTC (AR-5).
CREATE TABLE product (
    product_id UUID                     NOT NULL PRIMARY KEY,
    reference  VARCHAR(10)              NOT NULL,
    label      VARCHAR(240)             NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT product_reference_unique UNIQUE (reference)
);

-- price-list: a name of 80 characters, at most 160 UTF-16 units; its currency (BR-4) and its channel. The validity is
-- accepted, not kept (BR-6).
CREATE TABLE price_list (
    price_list_id UUID         NOT NULL PRIMARY KEY,
    name          VARCHAR(160) NOT NULL,
    currency      CHAR(3)      NOT NULL,
    channel       VARCHAR(16)  NOT NULL
);

-- One price per product and list (BR-1, P-5): the key holds it, one MERGE replaces. The amount in minor units with its
-- currency (INV-DATA-001). No foreign key to product: a price is set for the identifier given (BR-12).
CREATE TABLE price (
    price_list_id   UUID                     NOT NULL REFERENCES price_list (price_list_id),
    product_id      UUID                     NOT NULL,
    amount_minor    BIGINT                   NOT NULL,
    amount_currency CHAR(3)                  NOT NULL,
    effective_from  TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (price_list_id, product_id)
);
