-- The fission of invoice (2026-10-19): a payment is payment's, in a table of its own, with a key of its own and no
-- foreign key to invoice's tables. It records the amount charged, in minor units with its currency (AR-2), the charge
-- reference, bounded by AR-1 (510 UTF-16 units, as V1 sizes a text), and its time, never the card token (BR-24).
CREATE TABLE payment (
    payment_id       UUID                     NOT NULL PRIMARY KEY,
    invoice_id       UUID                     NOT NULL,
    amount_minor     BIGINT                   NOT NULL,
    amount_currency  CHAR(3)                  NOT NULL,
    charge_reference VARCHAR(510)             NOT NULL,
    paid_at          TIMESTAMP WITH TIME ZONE NOT NULL
);

-- The payments already recorded on the invoice row are copied: the amount charged is the whole amount of the invoice,
-- the sum of its lines in their one currency (BR-2, BR-5, BR-7).
INSERT INTO payment (payment_id, invoice_id, amount_minor, amount_currency, charge_reference, paid_at)
SELECT RANDOM_UUID(), i.invoice_id, SUM(l.price_minor), MIN(l.price_currency), i.charge_reference, i.paid_at
FROM invoice i
JOIN invoice_line l ON l.invoice_id = i.invoice_id
WHERE i.status = 'PAID'
GROUP BY i.invoice_id, i.charge_reference, i.paid_at;
