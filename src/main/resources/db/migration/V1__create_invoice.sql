-- A text of the spec holds at most 255 characters, counted as Unicode code points (BR-11, BR-12);
-- H2 counts UTF-16 units, so a column holds 510 to take 255 code points outside the Basic Multilingual Plane.
CREATE TABLE invoice (
    invoice_id  UUID         NOT NULL PRIMARY KEY,
    order_id    VARCHAR(510) NOT NULL,
    customer_id VARCHAR(510) NOT NULL,
    status      VARCHAR(16)  NOT NULL,
    CONSTRAINT uk_invoice_order UNIQUE (order_id)
);

CREATE TABLE invoice_line (
    invoice_id     UUID         NOT NULL REFERENCES invoice (invoice_id),
    line_no        INTEGER      NOT NULL,
    label          VARCHAR(510) NOT NULL,
    price_minor    BIGINT       NOT NULL,
    price_currency CHAR(3)      NOT NULL,
    PRIMARY KEY (invoice_id, line_no)
);
