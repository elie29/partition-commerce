# Products and price lists of the catalog — design

> **Spec:** `docs/specs/catalog/product/spec.md` (BR-1 to BR-10, `PriceListPrices`); `docs/specs/catalog/price-list/spec.md` (BR-1 to BR-10, BR-12, BR-13, `PriceListPrices`); `docs/specs/catalog/area.md` (AR-1 to AR-5, the words); `docs/specs/invariants.md` (INV-DATA-001)
>
> **Contract:** `docs/contracts/catalog.openapi.json` 1.0.0 (5016f59), tags `Product` and `PriceList`
>
> **Plan:** `docs/plans/catalog-products-and-price-lists.md`

## The generated API

Two executions of `openapi-generator-maven-plugin`, as `billing` has one per tag: `product` (`FILTER=tag:Product`, into `com.commerce.product.inbound.api`) and `price-list` (`FILTER=tag:PriceList`, into `com.commerce.pricelist.inbound.api`), with `billing`'s `configOptions`. Each lists in `modelsToGenerate` the models its operations reach (`Product`, `Price`, `Money`, `PriceList`, and the inline request, page and envelope models the generator names); the names are read from the first generation, in Task 5 and Task 6. The base path `/commerce/v1/catalog` comes from the contract's `servers`; if the generated interfaces do not carry it, it is set for the catalog controllers only, never for `billing`'s (TL: billing unchanged).

As delivered in Tasks 5 and 6 (2026-10-19): the generated interface carries no base path, so each controller carries it as its own type-level `@RequestMapping` (a `PathMatchConfigurer` prefix is keyed by its text: two contexts under one prefix replace each other's). The catalog executions map `UUID` to `String` (`typeMappings`), since `UUID.fromString` accepts `1-1-1-1-1` against AR-3, turn bean validation off (`useBeanValidation=false`), since a generated `@Size` counts UTF-16 units against Character and Spring's binding stops at the first field it cannot convert against AR-1's "one item per field at fault", and list no `modelsToGenerate`, which drops the inline models. In their place, per context: a `HandlerInterceptor` checks the headers, then the query, before any binding; a `RequestBodyAdvice` judges the body on its JSON tree before it is bound (the schema, types included, in inbound; what the use case or the domain judges, from the application; merged by field); the use case checks the path's identifiers.

## `product` — thin

- **inbound** `ProductController` implements the generated `ProductApi`: it maps the request to plain structures and calls one use case per operation. `Location` is the path `/commerce/v1/catalog/products/{productId}` (BR-5).
- **application**, one use case per operation, plain records in and out, no domain layer: `CreateProduct` (BR-1 to BR-5: blank label refused, the reference handed to the store whose unique constraint answers BR-1, `createdAt` from the clock), `GetProduct` (BR-8), `ListProducts` (BR-7), `GetProductPrice` (BR-9, BR-10). Ports: `Products` (the store) and `ProductPrices` (the price of a product in a list on a day, or none).
- **outbound** `JdbcProducts` implements `Products` with `JdbcClient`; a `DuplicateKeyException` on the reference becomes the refusal of BR-1, so of two creations at the same moment the first to commit holds. `PriceListPricesAdapter` implements `ProductPrices` over `price-list`'s `PriceListPrices`: the only class of `product` that imports `price-list` (GoF Adapter, the shape of `InvoicePaymentsAdapter`).
- `getProductPrice`: the product is read first; an unknown product, and "none" from `PriceListPrices`, give the one 404 of BR-9, `PRICE_NOT_FOUND`. The day is `at`, or today in UTC; "applies" is computed by `price-list` (Applies, `../area.md`).
- `listProducts`: `ORDER BY reference`, `references` distinct, empty means no filter, offset `page × size` computed in `long` so a far page answers 200 with no item and the true totals (BR-7).

## `price-list` — rich

- **domain** (no Spring, no JDBC):
  - `Currency`: a known ISO 4217 code under BR-4, the JDK's list without the codes starting with `X`; written for `price-list`, not imported from `invoice` (architect: each spec its own check).
  - `Money`: an amount in minor units (a `long`) and its `Currency`; refuses a negative amount (BR-2), zero allowed.
  - `PriceList`: identifier, name (not blank, BR-5), `Currency`, channel. `price(productId, Money, Instant effectiveFrom)` returns a `Price` in the list's currency or refuses (BR-3); the list holds no price collection: one price per product is held by the store's key (BR-1).
  - `Price`: the product, the `Money`, `effectiveFrom`; `appliesOn(LocalDate day)` is true when `effectiveFrom` is before 00:00 UTC of the next day (BR-9 of `product`, Applies).
- **application**: `CreatePriceList` (BR-4 to BR-7), `GetPriceList` (BR-9), `SetPrice` (BR-1 to BR-4, BR-8 to BR-10, BR-12), and `PriceListPrices`, the interface `product` calls, with `PriceListPricesService`: a product, a list, a day → the amount, the currency and the `effectiveFrom` of the price that applies, or none; an unknown list is none too. Ports: `PriceLists`, `Prices`.
- **`setPrice`'s order (BR-10)**: the body is turned into `Money` first (schema, BR-4, BR-2: 400), then the list is read (404 `PRICE_LIST_NOT_FOUND`, a `priceListId` or a `productId` not a UUID included, BR-12), then `PriceList.price` (BR-3: 400). `effectiveFrom` absent is the clock, in UTC; kept to the millisecond (AR-5).
- **outbound** `JdbcPriceLists`, `JdbcPrices`: the price is written by one `MERGE … KEY (price_list_id, product_id)`, so the last to commit replaces the other and two first prices at the same moment both answer 200 (BR-1; P-5).

## Errors, logs, headers (AR-1 to AR-4)

- Each refusal of a use case is an exception of its application layer carrying the contract's code (`VALIDATION_FAILED`, `CONFLICT`, `PRODUCT_NOT_FOUND`, `PRICE_LIST_NOT_FOUND`, `PRICE_NOT_FOUND`) and, for a 400, the field and its message; the framework's refusals (`HttpMessageNotReadableException`, `MethodArgumentNotValidException`, `HandlerMethodValidationException`, `MissingRequestHeaderException`, `MethodArgumentTypeMismatchException`) are mapped the same way. Where the envelope is built: P-4.
- The envelope: `timestamp`, `path` (the request's, under the base path), a new `errorId`, `detail` (one sentence naming the refusal, no value), `errors` (for a 400, one item per field at fault within the first group refused: headers, then content, then the path's identifiers, then the rules on what is held; one `CONFLICT` item, `field` `/reference`, for the 409; one item with no `field` for a 404; empty for a 500). `message` and `detail` are fixed sentences: no value of the request is ever copied into them.
- One log line per error answer: the `errorId`, the `X-Request-Id` when it is a well-formed UUID, the path, the status (AR-2). No log names a label, a reference value, an amount or the `Authorization` header; the request models' `toString` is never logged.
- An identifier that is not in the 8-4-4-4-12 hexadecimal form is not a UUID (AR-3): the path identifiers are taken as text and checked by that form, not by `UUID.fromString`, which accepts `1-1-1-1-1`. Unknown body properties are ignored (AR-4: the mapper does not fail on them).
- 405, 406, 415 stay the framework's (architect; `backend-api-contract.md`, as amended 2026-10-19).

## Data

`V7__create_catalog.sql`, `catalog`'s own tables, no foreign key to another context's tables:

- `product (product_id UUID PK, reference VARCHAR(10) NOT NULL UNIQUE, label VARCHAR(240) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL)`: the contract's pattern bounds the reference to 10 characters; 120 characters (Character, `../area.md`) are at most 240 UTF-16 units, as V1 sizes a text.
- `price_list (price_list_id UUID PK, name VARCHAR(160) NOT NULL, currency CHAR(3) NOT NULL, channel VARCHAR(16) NOT NULL)`.
- `price (price_list_id UUID NOT NULL REFERENCES price_list, product_id UUID NOT NULL, amount_minor BIGINT NOT NULL, amount_currency CHAR(3) NOT NULL, effective_from TIMESTAMP WITH TIME ZONE NOT NULL, PRIMARY KEY (price_list_id, product_id))`: INV-DATA-001, the database rule's `BIGINT` and column names; no foreign key to `product` (BR-12).

The clock: one `java.time.Clock` in UTC, injected, so a test sets the time; `createdAt` and `effectiveFrom` truncated to the millisecond (AR-5).

## Patterns (`docs/architecture/patterns-grid.md`)

- **P-4: where the error envelope of AR-1 is built.** One `@RestControllerAdvice` per context, `ProductErrors` and `PriceListErrors`, each building the envelope and its log line, as `billing` has one per context; rule 3 kept as written. Lost: one `CatalogErrors` in a package `com.commerce.catalog` (architect, 2026-10-19).
- **P-5: one price per product and list (BR-1).** A primary key on `(price_list_id, product_id)` and one `MERGE`: the last to commit replaces. Lost: an `UPDATE` then an `INSERT`, a version column (architect, 2026-10-19).
- **Ports and adapters** for `Products`, `ProductPrices`, `PriceLists`, `Prices`; **GoF Adapter** for `PriceListPricesAdapter` over `PriceListPrices`. `ArchitectureTest` gains the contract `product/product` → `price-list/price-list` from `outbound`, types `PriceListPrices` and its answer.

## Questions the design raised, settled

- A `page` beyond the integer range: 400, as the schema's type says (TL, PO, 2026-10-19; `product` BR-7).
- A 400 carries one item per field at fault within the group the first refusal stops at; the content is one group (architect, 2026-10-19; AR-1, `price-list` BR-10). So the body is validated whole, every field at fault reported; the headers, the path's identifiers and the rules on what is held are groups of their own, checked in that order around it.

## Acceptance

Checked once, at delivery, through `./mvnw test`:

- `CreateProductTest`: 201, `Location`, `createdAt` to the millisecond; 409 `CONFLICT` `/reference` for a held reference, nothing created; two at once: one 201, one 409; 400 for a blank label (non-breaking space included), a reference off its pattern, both at once (two items), a label of 121 characters, no body, invalid JSON, no `X-Request-Id`, a malformed one; an unknown property and `description`, `ean`, `attributes` accepted and returned by no operation; the envelope's fields; no output names the label, the reference value or the `Authorization` header.
- `GetProductTest`, `ListProductsTest`: 404 `PRODUCT_NOT_FOUND` for an unknown product and for `1-1-1-1-1`; the order by reference, `references` exact, twice once, empty no filter, a page past the last 200 with the true totals; `localized` and `Accept-Language` change nothing.
- `CreatePriceListTest`, `GetPriceListTest`: 201 with the channel; validity accepted, not returned; 400 for a blank name, `XYZ`, `xyz`; 404 `PRICE_LIST_NOT_FOUND`; never 409.
- `SetPriceTest`: 200; a second price replaces the first; two at once, first time and later: both 200, the last kept; 400 for a negative amount (zero accepted), `XYZ`, a currency other than the list's, an `effectiveFrom` without an offset; the order body → list → product → BR-3; a negative amount with `XYZ`: two items; a negative amount in another currency than the list's: one; 404 `PRICE_LIST_NOT_FOUND` for an unknown list, a list or a product id not a UUID; a product `product` does not hold is priced; no output names an amount.
- `GetProductPriceTest`: 200 when `effectiveFrom` is on or before the end of day `at`, today when absent, 23:59:59.999Z included and 00:00Z of the next day excluded; a future price hides the price, 404; 404 `PRICE_NOT_FOUND`, one `detail`, for an unknown product, an unknown list, no price; 400 for a `priceListId` not a UUID and an `at` not a date, before the product.
- `PriceListTest` (domain, no Spring): BR-2, BR-3, BR-4, BR-5, `appliesOn`.
- `ArchitectureTest`: the layer maps of `product` and `price-list`, and the one contract `PriceListPrices`.
- The tests written before this feature still pass.
