# Retire a product — design

> **Spec:** `docs/specs/catalog/product/spec.md` (BR-1, BR-3, BR-5, BR-8, BR-9; Language: Retired, Active, Catalog manager); `docs/specs/catalog/area.md` (AR-2)
>
> **Contract:** `docs/contracts/catalog.openapi.json` 1.1.0, unchanged: no operation, `Product` unchanged
>
> **Plan:** `docs/plans/retire-product.md`

## `product` — rich (Claire Martin, architect, 2026-10-20)

The hexagon takes the layer map of `docs/partition.yml`: `domain` is new, `application -> domain` and `outbound -> domain` are allowed. `ArchitectureTest` reads the map from that file and checks the domain against it: the domain imports nothing but itself and `java..`, and nothing from Spring (`backend-architecture`).

## Task 4 — the move, no behaviour added or changed

- **domain** `Product`: a record `(productId, reference, label, createdAt)`, with the data `ProductView` carries today. Its `toString` names only the identifier (AR-2), as `ProductView` and `PriceList` do. `Blank` moves to `domain` as it is, still the one definition of Blank in `product`.
- **application** `Products` (the port) takes and returns `Product`: `add(Product)`, `find(UUID) → Optional<Product>`. `ProductPage` keeps its `ProductView` items, which `JdbcProducts` builds with `ProductView.of` (Claire Martin, architect, 2026-10-20). `CreateProductUseCase` builds the `Product`. `contentFaults` still judges BR-3 through `Blank`, and the 400 is unchanged. `ProductView` stays the application's answer to `inbound`, made from a `Product` by `ProductView.of(Product)`. The use cases return `ProductView` and `ProductPage`, so `inbound` does not change.
- **outbound** `JdbcProducts` reads and writes `Product`. Its SQL is unchanged.
- No migration, and no test changes except imports.

## Task 5 — the retirement (BR-9)

- **domain**
  - `ProductStatus`: an enum, `ACTIVE` and `RETIRED` (Language: Active, Retired).
  - `Product` takes a `status` field.
  - `Product.create(productId, reference, label, createdAt)` makes an `ACTIVE` product ("a new product is active").
  - `retire()` returns the product `RETIRED`, and returns the same product when it is already retired ("nothing changes").
  - `isRetired()`.
  - No method makes a product `ACTIVE` except `create`. There is no status setter and no `reactivate` ("never reactivated").
- **application** `RetireProductUseCase.retire(UUID productId)`:
  - It reads the product through `Products.find`. When there is none, it throws `ProductNotFound` and nothing changes (BR-9, "as BR-8 does").
  - It calls `retire()`. When the product was active, it writes the new status through `Products.changeStatus(Product)`. When the product was already retired, it writes nothing.
  - It returns nothing: no reader sees the retirement, and `ProductView` does not carry the status.
  - It is a Spring `@Service` like the other three use cases. No controller, job, command line or message reaches it: only its tests do (BR-9). It checks nothing about who calls it (Catalog manager, BR-9).
- **outbound** `JdbcProducts`:
  - `changeStatus` runs `UPDATE product SET status = :status WHERE product_id = :id`.
  - `add` writes `status`, and the mapper reads it.
  - `find` and `page` filter nothing on `status`: `getProduct` and `listProducts` answer as before (BR-9).
- **Reads unchanged:** `ProductView`, `ProductController` and the generated API do not change.
- **Concurrency:** two retirements of one product at the same moment both write `RETIRED`, and the product ends retired, which is BR-9's effect. Nothing else writes `status`, so no conditional update is needed.
- **The reference** is not touched: the unique constraint of V7 still holds it, so a `createProduct` with a retired product's reference gets 409 (BR-1, BR-9).
- **Logs (AR-2):** the use case logs nothing. A failure of the store reaches the caller as it does today.

## Data

`V9__add_product_status.sql`:

- `ALTER TABLE product ADD COLUMN status VARCHAR(7) DEFAULT 'ACTIVE' NOT NULL`, with `CONSTRAINT product_status_known CHECK (status IN ('ACTIVE', 'RETIRED'))`. The default makes every product already in the store active (BR-9).
- Nothing is kept for when or who (BR-9: "the retired state only").

## Patterns (`docs/architecture/patterns-grid.md`)

- **Proposed P-6**, `product` / status of a product: no pattern. It is an enum held by the `Product` aggregate, with one guarded method (`retire`) and no status setter, as P-1 and P-2 do. Candidate that lost: State (GoF), which would mean two classes, one of whose `retire` bodies does nothing, for a single move. It would be ruled again when a second move enters the spec.
- **Ports and adapters**: `Products` gains `changeStatus`.

## Questions the design raised, settled

1. **P-6** as proposed above. It records the same ruling as P-1, citing P-1, and the question is not reopened (Claire Martin, architect, 2026-10-20).
2. **The input of `RetireProductUseCase`** is a `UUID`, not a text checked by the 8-4-4-4-12 form as `getProduct` takes one. Nothing crosses a trust boundary in this version (BR-9: only its tests reach it). When an operation comes with a later contract, its inbound adapter checks the form (AR-3) (Claire Martin, architect, 2026-10-20).
3. **Test classes:** the aggregate is tested inside `RetireProductTest`, the test class of the use case (`backend-testing`: one test class per use case), on the database and with no HTTP (TL, 2026-10-20).

Design validated as written (architect, TL, 2026-10-20).

## Acceptance

Checked once, at delivery, through `./mvnw -q clean test`:

- **Task 4:** every test that existed before Task 4 passes, with the same count before and after. `ArchitectureTest` passes with `product/domain` present.
- `RetireProductTest`:
  - A product created through `createProduct` is active.
  - Retiring it makes it retired, read back from the store.
  - Retiring it again leaves it retired and writes nothing (the port is spied).
  - An unknown identifier throws `ProductNotFound`, and no row changes.
  - After the retirement, a `createProduct` with its reference gets 409.
  - `getProduct` and `listProducts` answer for a retired product exactly as they did before its retirement.
  - A product inserted before V9 (a row without `status`) reads as active.
  - `Product` has no public method other than `create` that returns an `ACTIVE` product, and no setter.
  - No output names the label or the reference.
- `ArchitectureTest`: the layer map of `product`, its domain included.
- The tests written before this feature still pass.
