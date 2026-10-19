-- The fission of invoice (2026-10-19): the time and the charge reference of a payment are payment's, copied by V4.
-- No version older than V4 runs after this migration, and none is rolled back to (TL, 2026-10-19).
ALTER TABLE invoice DROP COLUMN charge_reference;

ALTER TABLE invoice DROP COLUMN paid_at;
