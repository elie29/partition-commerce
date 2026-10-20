-- The promotion of payment (2026-10-20): its tables live in payment-app's own database, created there from V1; no
-- row is copied, no instance holds data to keep (TL). They leave commerce-app's.
DROP TABLE refund_cap;

DROP TABLE refund;

DROP TABLE payment;
