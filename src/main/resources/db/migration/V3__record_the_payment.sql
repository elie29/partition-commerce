-- BR-3, BR-5: a payment records the time of the payment and CardPay's charge reference, never the card token;
-- both NULL while the invoice is not paid. The charge reference is bounded as a label is (BR-12): 510 UTF-16 units,
-- as V1 sizes a text.
ALTER TABLE invoice ADD COLUMN paid_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE invoice ADD COLUMN charge_reference VARCHAR(510);
