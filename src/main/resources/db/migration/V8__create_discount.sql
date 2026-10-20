-- discount (docs/specs/catalog/discount/spec.md): its own table, with no foreign key to another context's tables; a
-- discount names no product, no price list and no order (BR-6).

-- A percent of 1 to 90 (BR-1), checked before the write. The minimum order is optional, its amount in minor units with
-- its currency (INV-DATA-001): both set or both absent. The period is accepted, not kept (BR-2).
CREATE TABLE discount (
    discount_id            UUID    NOT NULL PRIMARY KEY,
    percent                INTEGER NOT NULL,
    minimum_order_minor    BIGINT,
    minimum_order_currency CHAR(3),
    CONSTRAINT discount_minimum_order_whole
        CHECK ((minimum_order_minor IS NULL) = (minimum_order_currency IS NULL))
);
