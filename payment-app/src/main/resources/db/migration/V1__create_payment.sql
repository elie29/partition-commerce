-- payment-app's own database (promotion of payment, 2026-10-20): the table as commerce-app's V4 left it; no row is
-- copied, no instance holds data to keep (TL). A payment records the amount charged, in minor units with its
-- currency, the charge reference, bounded by AR-1 (510 UTF-16 units), and its time, never the card token (BR-24).
CREATE TABLE payment (
    payment_id       UUID                     NOT NULL PRIMARY KEY,
    invoice_id       UUID                     NOT NULL,
    amount_minor     BIGINT                   NOT NULL,
    amount_currency  CHAR(3)                  NOT NULL,
    charge_reference VARCHAR(510)             NOT NULL,
    paid_at          TIMESTAMP WITH TIME ZONE NOT NULL
);
