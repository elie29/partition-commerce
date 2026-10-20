-- fraud (docs/specs/customer/fraud/spec.md): the reviews of the risk team, the table #4-data of the source names, with
-- no foreign key to another context's table. A customer_id is the text billing holds, kept as it is: 255 characters
-- (BR-7; a character is a code point) are at most 510 UTF-16 units, as V1 sizes a text. Times to the millisecond, in
-- UTC (architect, TL, 2026-10-20).
-- open_customer_id is the customer_id while the review is open, empty once lifted: its unique constraint holds one open
-- review per customer at every write (BR-6, P-8); H2 has no partial index.
CREATE TABLE fraud_review (
    review_id        UUID                        NOT NULL PRIMARY KEY,
    customer_id      VARCHAR(510)                NOT NULL,
    status           VARCHAR(6)                  NOT NULL,
    placed_at        TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    lifted_at        TIMESTAMP(3) WITH TIME ZONE,
    open_customer_id VARCHAR(510),
    CONSTRAINT fraud_review_status_known CHECK (status IN ('OPEN', 'LIFTED')),
    CONSTRAINT fraud_review_one_open_per_customer UNIQUE (open_customer_id),
    CONSTRAINT fraud_review_open_is_its_customer CHECK (
        (status = 'OPEN' AND open_customer_id = customer_id AND lifted_at IS NULL)
        OR (status = 'LIFTED' AND open_customer_id IS NULL AND lifted_at IS NOT NULL))
);

-- The read of a customer's reviews (BR-8).
CREATE INDEX fraud_review_customer ON fraud_review (customer_id);
