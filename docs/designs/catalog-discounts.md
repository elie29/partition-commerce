# Discounts of the catalog — design

> **Spec:** `docs/specs/catalog/discount/spec.md` (BR-1 to BR-10, Callers); `docs/specs/catalog/area.md` (AR-1 to AR-4, the words); `docs/specs/invariants.md` (INV-DATA-001)
>
> **Contract:** `docs/contracts/catalog.openapi.json` 1.0.0 (5016f59), tag `Discount`
>
> **Plan:** `docs/plans/catalog-discounts.md`

## The generated API

One more execution of `openapi-generator-maven-plugin`, `discount` (`FILTER=tag:Discount`, into `com.commerce.discount.inbound.api`), with the options of the catalog's two executions (`backend-api-contract-catalog`): `UUID` mapped to `String`, bean validation off, no `modelsToGenerate`. As in `product` and `price-list`: a `HandlerInterceptor` checks the headers before any binding; a `RequestBodyAdvice` judges the body on its JSON tree before it is bound (the schema, types included, in inbound; what the use case judges, from the application; merged by field); the use case checks the path's identifier. The controller carries `/commerce/v1/catalog` as its own type-level `@RequestMapping`.

## `discount` — thin

- **inbound** `DiscountController` implements the generated `DiscountApi` and calls one use case per operation. `DiscountRequestCheck` (headers: `X-Request-Id` present and a UUID, AR-1), `DiscountBodyCheck` with its `JsonBody` (the body of `createDiscount`: the schema, types included; `period` judged by the schema only and dropped, BR-2; unknown properties ignored, AR-4), `DiscountErrors` (the envelope, P-4), `DiscountWebConfiguration`. `createDiscount` answers 201 with the `Discount`, no `Location`: the contract declares none.
- **application**, plain records, no domain layer:
  - `CreateDiscountUseCase`: `contentFaults(percent, minimumOrder)` returns the faults it judges beyond the schema, merged with the schema's by field into one 400 (BR-9): `percent` outside 1–90 (BR-1), a negative `minimumOrder/amountMinor` (BR-3), a `minimumOrder/currency` that is not a known code (BR-4); `create` makes the identifier (Identifier, `../area.md`), keeps `percent` and `minimumOrder` (BR-5), and never refuses a discount like another (BR-7).
  - `GetDiscountUseCase`: the `discountId` checked by the 8-4-4-4-12 form (AR-3), then read; not a UUID or unknown: `DiscountNotFound` (BR-8).
  - `KnownCurrency`: BR-4 as `price-list`'s BR-4 states it, written for `discount`, not imported (each spec its own check, architect, 2026-10-19): a code of the JDK's `Currency` list that does not start with `X`; incoming only, a stored code is read as kept.
  - Port `Discounts` (add, find by identifier); records `DiscountView`, `MinimumOrder` (amount in minor units, a `long`, and the currency code), `Fault`, `Identifiers`.
- **outbound** `JdbcDiscounts` implements `Discounts` with `JdbcClient`.
- Nothing reads a neighbour (BR-6): no contract to `product` or `price-list`.

## Errors, logs, headers (AR-1 to AR-4)

As `product` and `price-list` (design `catalog-products-and-price-lists.md`, _Errors, logs, headers_): one `@RestControllerAdvice`, `DiscountErrors`, builds the envelope and its log line; `VALIDATION_FAILED` per field at fault, `DISCOUNT_NOT_FOUND` one item with no `field`, a 500 with `errors` empty and the class and frames of its exception logged, never its message (AR-2). No log names `percent` or an amount. 401, 403 and 409 are never answered (Callers, BR-7). `Accept-Language` is ignored (BR-10).

## Data

`V8__create_discount.sql`, `discount`'s own table, no foreign key:

- `discount (discount_id UUID PK, percent INTEGER NOT NULL, minimum_order_minor BIGINT NULL, minimum_order_currency CHAR(3) NULL, CHECK both null or both set)`: INV-DATA-001 and the database rule's names (`<what>_minor`, `<what>_currency`); `period` has no column (BR-2).

No clock: a discount keeps no time.

## Patterns (`docs/architecture/patterns-grid.md`)

- **P-4** applied to `discount`: its own `DiscountErrors`, rule 3 kept; P-4 extended to `discount` (architect, 2026-10-20).
- **Ports and adapters** for `Discounts`. `ArchitectureTest` reads the layer map of `discount` from `docs/partition.yml` (TL, 2026-10-20).

## Questions the design raised, settled

1. P-4 extends to `discount`, one error handler per context (architect, 2026-10-20).
2. `backend-api-contract-catalog` extends its `applyTo` to `src/main/java/com/commerce/discount/**` and its first line to the `discount` execution; `apm install` run by the TL once the plan is done (TL, 2026-10-20).

Design validated as written, Acceptance included (architect, TL, 2026-10-20).

## Acceptance

Checked once, at delivery, through `./mvnw -q clean test`:

- `CreateDiscountTest`: 201 with `discountId`, `percent`, `minimumOrder` as sent, and without `minimumOrder` when none is sent; `percent` 1 and 90 accepted, 0 and 91 refused (400, nothing created); a negative amount refused, zero accepted; `XYZ` refused; a negative amount with `XYZ` and `percent` 0: three items; `period` accepted and returned by no operation, an end before its start included, a `period` without `start` 400; a `null` `minimumOrder` 400; two identical discounts both 201; no body, invalid JSON, no `X-Request-Id`, a malformed one: 400; an unknown property and `Accept-Language` accepted; never 401, 403, 409; no output names an amount or the percent.
- `GetDiscountTest`: 200 as created; 404 `DISCOUNT_NOT_FOUND` for an unknown identifier and for `1-1-1-1-1`; a 500 forced by the store: the envelope, `errors` empty, its log the class and frames, not the message.
- `ArchitectureTest`: the layer map of `discount`.
- The tests written before this feature still pass.
