-- refund (REQ-6): its own tables, with no foreign key to another context's tables.
-- A refund records its amount, in minor units with its currency (AR-2), the charge reference of the payment it gives
-- back, bounded by AR-1 (510 UTF-16 units, as V1 sizes a text), its status and the time it was requested.
CREATE TABLE refund (
    refund_id        UUID                     NOT NULL PRIMARY KEY,
    invoice_id       UUID                     NOT NULL,
    amount_minor     BIGINT                   NOT NULL,
    amount_currency  CHAR(3)                  NOT NULL,
    charge_reference VARCHAR(510)             NOT NULL,
    status           VARCHAR(16)              NOT NULL,
    requested_at     TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX refund_of_invoice ON refund (invoice_id);

-- BR-25, P-3: the cap of the refunds of an invoice, its amount paid, and the running total of the refunds not refused,
-- REQUESTED, SENT or SETTLED. A refund is counted by one conditional update of this row.
CREATE TABLE refund_cap (
    invoice_id           UUID    NOT NULL PRIMARY KEY,
    amount_paid_minor    BIGINT  NOT NULL,
    amount_paid_currency CHAR(3) NOT NULL,
    refunded_minor       BIGINT  NOT NULL,
    refunded_currency    CHAR(3) NOT NULL
);
