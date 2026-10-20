-- product (docs/specs/catalog/product/spec.md): the status of a product, active or retired (BR-9, P-6). Every product
-- already in the store is active, as a new one is. Only the retired state is kept: neither when nor who.
ALTER TABLE product ADD COLUMN status VARCHAR(7) DEFAULT 'ACTIVE' NOT NULL;

ALTER TABLE product ADD CONSTRAINT product_status_known CHECK (status IN ('ACTIVE', 'RETIRED'));
