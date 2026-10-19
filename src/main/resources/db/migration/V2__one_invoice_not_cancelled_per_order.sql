-- BR-1: an order has at most one invoice that is not cancelled, held by the database for simultaneous issues too.
-- H2 has no partial unique index: the key is on a column that holds the order while the invoice is not cancelled,
-- and NULL once it is; NULLs never collide, so a cancelled invoice frees its order.
ALTER TABLE invoice DROP CONSTRAINT uk_invoice_order;

ALTER TABLE invoice ADD COLUMN open_order_id VARCHAR(510)
    GENERATED ALWAYS AS (CASE WHEN status = 'CANCELLED' THEN NULL ELSE order_id END);

ALTER TABLE invoice ADD CONSTRAINT uk_invoice_open_order UNIQUE (open_order_id);
