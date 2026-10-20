# Bounded contexts — catalog

> **Source:** `docs/contracts/catalog.openapi.json`
>
> **Source version:** 1.1.0
>
> **Provenance:** 2026-10-20 · 260ac4127d89f0b65d7d1d377f5c429f136716f7
>
> **Scope:** tag:Product; tag:PriceList; tag:Discount
>
> **Units covered:** 9 / 9
>
> **Decided:** 2026-10-20 · Claire Martin (architect), Paul Durand (PO), Léa Petit (BA) · workshop: `docs/sources/workshop-price-list-into-product.md` (merge: `product` absorbs `price-list`)

The **Scope** line fixes the units analysed; the others appear in the `Coverage` table with the mention "out of scope". Change it only when a scope extension has been validated.

The **Provenance** line is the anchor of every later update: it fixes the exact state of the source this decomposition comes from. Never delete it; change it only together with the content it dates.

## Splitting criteria applied

The six criteria, evaluated operation by operation over the operations in scope:

- **C1 — Functional grouping** (hint, outside the threshold): the operation carries a tag of its own.
- **C2 — Root aggregate of its own** (mandatory): the terminal resource of the path differs from that of the others; a sub-resource belongs to its parent aggregate.
- **C3 — Business language of its own**: the vocabulary of the operation stands alone.
- **C4 — Life-cycle autonomy**: the operation can be used, versioned and have its model evolve without coordination with the others.
- **C5 — Distinct external dependencies**: the operation relies on named third-party systems different from those of the others.
- **C6 — Shared data** (argues for grouping): the operation shares `components` schemas or inline duplicated structures with others, the common error envelope excluded.

Decision rules: **R1** a bounded context needs C2 and one of C3–C5; **R2** C1 never counts in the threshold; **R3** an operation failing R1 joins its nearest candidate; **R4** C6 never creates a bounded context and argues against a separation; **R5** a single operation neither justifies nor forbids a bounded context; **R6** two bounded contexts that no longer satisfy R1 against each other call for a merge.

## Bounded contexts retained

| Bounded context | Units | Criteria satisfied | Justification |
| --- | --- | --- | --- |
| product | listProducts, createProduct, getProduct, createPriceList, getPriceList, setPrice, getProductPrice | C2, C3, C4 (hint: C1, tags `Product`, `PriceList`) | The terminal resource is `products`. Its vocabulary (reference, label, attributes, translations) stands apart from the price lists'; a price list knows a product only by its `productId`. No named third party (C5 no). Absorbed `price-list` (createPriceList, getPriceList, setPrice, getProductPrice), 2026-10-20: two root aggregates, `products` and `price-lists`, kept together against C2, C3 and C4, see `Kept against the criteria` and `Decisions not to split`. |
| discount | createDiscount, getDiscount | C2, C3, C4 (hint: C1, tag `Discount`) | The terminal resource is `discounts`, not a sub-resource. Its vocabulary (percent, minimumOrder, period, order) stands apart from the products' and the price lists'. A `Discount` names no `productId` and no `priceListId`: it evolves without them. No named third party (C5 no). Against R4: it shares only the value `Money` with `setPrice` and `getProductPrice`; weak sharing, it does not prevail. "A discount is its own aggregate, created and read by its own operations; no price list holds it." (Claire Martin, architect, 2026-10-20) |

## Decisions not to split

| Units kept together | Criteria | Rule and reason | Decided by | Date |
| --- | --- | --- | --- | --- |
| listProducts, createProduct, getProduct / createPriceList, getPriceList, setPrice, getProductPrice | the price list's units against the products': C2 (the terminal resource `price-lists`; `setPrice`, `getProductPrice` its sub-resources), C3 (name, currency, channel, validity, price, effectiveFrom), C4; R1 still holds, the source is unchanged (1.1.0) | No rule calls it, neither R6 (R1 holds) nor R4 (`Product` and `Price` share `productId` only): kept against C2, C3 and C4, see `Kept against the criteria`. Merge of 2026-10-20 into `product`: "a price is never managed apart from its product; every change of one is a change of the other" | Claire Martin (architect), Paul Durand (PO), Léa Petit (BA); workshop `docs/sources/workshop-price-list-into-product.md` | 2026-10-20 |

## Alternatives rejected

Option 3 of 2026-10-19, rejected that day, was taken by the merge of 2026-10-20: see `History`.

| Option | Bounded contexts | Why not chosen | Decided by | Date |
| --- | --- | --- | --- | --- |
| 2 (scope extension) | `product` unchanged, `price-list` (createPriceList, getPriceList, setPrice, createDiscount, getDiscount); breaks R1 on createDiscount, getDiscount | "a discount is its own aggregate, created and read by its own operations; no price list holds it" | Claire Martin, architect | 2026-10-20 |
| 2 (delta 1.0.0 → 1.1.0) | `product` keeps `getProductPrice` beside listProducts, createProduct, getProduct; `price-list` unchanged; breaks C2/R3 on getProductPrice, whose root aggregate is now `price-lists` | No reason stated: option 1 validated by name | Claire Martin, architect | 2026-10-20 |

## Kept against the criteria

| Point | Criterion | Decided by | Date | Reason |
| --- | --- | --- | --- | --- |
| `product` holds the units of `price-list` (createPriceList, getPriceList, setPrice, getProductPrice) beside its own: two root aggregates, `products` and `price-lists` | C2, C3, C4 (R1): the price list's units satisfy them against the products' (the _Bounded contexts retained_ line of `price-list`, overturned 2026-10-20, see `History`) | Claire Martin (architect) | 2026-10-20 | "a price is never managed apart from its product; every change of one is a change of the other" (workshop `docs/sources/workshop-price-list-into-product.md`) |

## Gaps detected

- "Every amount is stored in minor units, with its currency." ([`/info/description`](../../contracts/catalog.openapi.json)) is a rule of the contract, the one `docs/specs/billing/area.md` holds as AR-2 ([BA], 2026-10-19): it spans two areas, goes to no area file, and is quoted as a candidate under `Applicable area rules and invariants` of `product` (getProductPrice) and `price-list` (setPrice). Candidate for the admission test of the invariant register. Nothing moved in `billing`. Resolved on 2026-10-19 ([architect]): passes the admission test, written as INV-DATA-001 of `docs/specs/invariants.md` (category `DATA`, the register's only category for now); `product` and `price-list` name it. AR-2 of `billing` removed, its place kept by a note in `docs/specs/billing/area.md`; `invoice`, `payment` and `refund` name INV-DATA-001 instead ([architect], 2026-10-19). The `BIGINT` of AR-2 moves to the database rule: "An amount column is a `BIGINT` named `<what>_minor`, its currency column `<what>_currency`." ([TL], 2026-10-19, written in `.apm/instructions/backend-database.instructions.md`, deployed by `apm install`). "Three letters" and "never a decimal" stay with the words Currency and Minor units of `billing`'s Language ([architect], 2026-10-19). Migrations V4 and V6 are not edited ([TL], 2026-10-19); the comments and the error messages that cite AR-2 in `src/main/java/com/commerce/payment/domain/Money.java` and `src/main/java/com/commerce/refund/domain/Money.java` name INV-DATA-001 instead, with the catalog work ([TL], 2026-10-19): pending.
- No invariant register existed at `docs/specs/invariants.md`: created on 2026-10-19 with INV-DATA-001.
- Coupling outside the scope: `Money` is used by `setPrice`, `getProductPrice` and by `createDiscount`, `getDiscount` (out of scope). Extend the scope to `tag:Discount` or not? Resolved on 2026-10-20 (version 1.0.0): scope extended to `tag:Discount`, validated by name.
- `ean` of `createProduct` is deprecated with "Use attributes.gtin." and no reason: pointed to, not a decision.
- None of the contract's field or parameter descriptions is a business rule: they say what the API does when a value is absent, or what a parameter means ([BA], 2026-10-19). They are pointed to, not copied.
- The common error envelope (`.claude/rules/backend-api-contract.md`), written inline in all nine operations, is a cross-cutting convention: left out of the coupling matrix.
- No orphan schema.
- AR-1 of `area.md` binds `discount` (architect, 2026-10-20), but its list of 404 codes does not name `DISCOUNT_NOT_FOUND`, the code of `getDiscount`'s 404: kept as a reported gap for now (architect, 2026-10-20). Resolved on 2026-10-20 (version 1.0.0): AR-1 names `DISCOUNT_NOT_FOUND` (architect).
- The "Out of scope" lines of `product/spec.md` and `price-list/spec.md` say the discounts are "out of the decomposition's scope": no longer true since 2026-10-20; hand-written, reported, not rewritten.
- "Order" (`/tags/2/description`, `minimumOrder` of `createDiscount` and `Discount`) is defined by no bounded context of `catalog` and by no contract: open question of `discount`. Resolved on 2026-10-20 (version 1.0.0): nothing of an order in this version; `discount` stores and returns discounts, nothing applies them (architect; `discount` BR-6).
- Two time windows: `validity {from, to}` (date) of `createPriceList` and `period {start, end}` (date-time) of `createDiscount`; whether they are one notion is an open question of `discount`. Resolved on 2026-10-20 (version 1.0.0): `period` governs nothing and is not kept, as the validity of a price list (BA; `discount` BR-2).
- "A currency is a known ISO 4217 code" is held by `price-list` BR-4 and `discount` BR-4, and cites `invoice`'s rule in `billing`: a rule in two specs and two areas, with no owner and in no area file. Owner bounded context, area file, or admission test of the invariant register? Kept as a citation for now (architect, 2026-10-20).
- Version 1.1.0 moves `getProductPrice` from `GET /products/{productId}/price` to [`GET /price-lists/{priceListId}/product-prices/{productId}`](../../contracts/catalog.openapi.json), turns `priceListId` from a required query parameter into a required path parameter, and retags it from `Product` to `PriceList`: breaking for its callers. Its root aggregate is now `price-lists` (C2), so the justification of `product` in `Bounded contexts retained` no longer holds for it. Its move to `price-list` is validated by name (Claire Martin, architect, 2026-10-20, proposal: option 1) and is carried out by `partition-reshape`, in the specs and in the code: pending. Until then `Bounded contexts retained`, `Coverage` and the `Source parts owned` of `product`, `price-list` and `discount` still put it in `product`. On 2026-10-20 they put it in `price-list` (`docs/plans/get-product-price-move.md`, Task 1); the hand-written sections and the code: pending. Resolved on 2026-10-20 (version 1.1.0): the hand-written sections and the code moved (`docs/plans/get-product-price-move.md`, Tasks 2 and 3; `../../architecture/reshapes/2026-10-20-get-product-price-move.md`).
- After the move: "The product check goes: an unknown product gets, as the contract says, `404` `PRICE_NOT_FOUND`, no price for it in that list. The integration after the move: none; `price-list` answers from its own data, `product` no longer calls `PriceListPrices`." ([architect], 2026-10-20). BR-9 and BR-10 of `product`, which `getProductPrice` alone carries, move with it, carried by a person; not rewritten. Resolved on 2026-10-20 (version 1.1.0): they are BR-14 and BR-15 of `price-list` (architect).
- With `priceListId` in the path, AR-3 answers an identifier that is not a UUID with a 404, no longer a 400: BR-10 of `product` ("A `priceListId` … the schema refuses is content: 400") and its AR-3 line ("`priceListId` in the query of `getProductPrice`") no longer match 1.1.0. Reported, not rewritten. Resolved on 2026-10-20 (version 1.1.0): an identifier that is not a UUID gets 404 `PRICE_NOT_FOUND`, only `at` stays a 400 (`price-list` BR-14, BR-15, architect).
- Hand-written lines left untrue by 1.1.0 and the validated move, reported, not rewritten: `Business objective`, `Scope`, the `Callers` and `price-list` lines of `Contracts with neighbours` and the INV-DATA-001 line of `product/spec.md`; the `Out of scope` line, BR-1 ("by the day as `product`'s BR-9 reads it"), BR-12 ("`price-list` does not read `product`", still true) and the `product` line of `Contracts with neighbours` of `price-list/spec.md`. Resolved on 2026-10-20 (version 1.1.0): rewritten to fit the move, as approved by the architect; `discount`'s `Out of scope` too.
- Code / spec divergence: `getProductPrice` is implemented in `product` at the 1.0.0 path, with `priceListId` in the query (`src/main/java/com/commerce/product/inbound/ProductController.java:68`, `application/GetProductPriceUseCase.java`, `inbound/ProductRequestCheck.java:35`, `outbound/PriceListPricesAdapter.java`; `src/test/java/com/commerce/product/GetProductPriceTest.java`; the `product` → `price-list` contract of `src/test/java/com/commerce/ArchitectureTest.java:132`): the break is not absorbed. Resolved on 2026-10-20 (version 1.1.0): `getProductPrice` is served by `price-list` at the 1.1.0 path, `PriceListPrices` and its adapter removed.
- Delta 1.0.0 → 1.1.0: no schema touched, no coupling outside the scope, no orphan schema.

## Coverage

| Unit | Grouping | Bounded context |
| --- | --- | --- |
| listProducts | Product | product |
| createProduct | Product | product |
| getProduct | Product | product |
| getProductPrice | PriceList | product |
| createPriceList | PriceList | product |
| getPriceList | PriceList | product |
| setPrice | PriceList | product |
| createDiscount | Discount | discount |
| getDiscount | Discount | discount |

## History

| Date | Version | Provenance | Nature | Detail |
| ---- | ------- | ---------- | ------ | ------ |
| 2026-10-20 | 1.0.0 | 5016f59 | scope extension | `tag:Discount` added to the scope; createDiscount, getDiscount form the new bounded context `discount` (proposal, option 1 · Claire Martin, architect); the decomposition of 2026-10-19 (proposal, option 1 · [architect]) stands for `product` and `price-list` |
| 2026-10-20 | 1.0.0 → 1.1.0 | 260ac41 | breaking | `getProductPrice` moves to `GET /price-lists/{priceListId}/product-prices/{productId}`, `priceListId` from the query to the path, tag `Product` → `PriceList`; its root aggregate becomes `price-lists` (C2): move from `product` to `price-list` validated (proposal, option 1 · Claire Martin, architect), to be carried out by `partition-reshape` |
| 2026-10-20 | 1.1.0 | 260ac41 | move | `getProductPrice` moves from `product` to `price-list` (`../../architecture/reshapes/2026-10-20-get-product-price-move.md`). It overturns option 2 of `Alternatives rejected` of 2026-10-19, `product` (listProducts, createProduct, getProduct), `price-list` (createPriceList, getPriceList, setPrice, getProductPrice), rejected then because "`getProductPrice` is read under `/products`, its root aggregate (C2, R3)" ([architect]); the line leaves that table |
| 2026-10-20 | 1.1.0 | 260ac41 | merge | `price-list` (createPriceList, getPriceList, setPrice, getProductPrice) absorbed by `product`, both in `catalog`; workshop `docs/sources/workshop-price-list-into-product.md`, record `docs/architecture/reshapes/2026-10-20-price-list-into-product.md`; criteria lost: none, R1 still holds; kept against C2, C3 and C4 (`Kept against the criteria`, `Decisions not to split`); no contract between the two to remove. Overturned the _Bounded contexts retained_ line "price-list — createPriceList, getPriceList, setPrice, getProductPrice — C2, C3, C4 (hint: C1, tag `PriceList`) — The terminal resource is `price-lists`; `setPrice` (`…/price-lists/{priceListId}/prices/{productId}`) and, since 1.1.0, `getProductPrice` (`…/price-lists/{priceListId}/product-prices/{productId}`) are sub-resources of `price-lists` (C2, R3; moved from `product`, Claire Martin, architect, 2026-10-20). Its vocabulary (name, currency, channel, validity, price, effectiveFrom) stands apart from the products'. No named third party (C5 no)."; the _Alternatives rejected_ line "3 — `catalog` (all seven operations), under R4 — "one bounded context would hold two root aggregates" — [architect] — 2026-10-19"; the `product` line of _Bounded contexts retained_ on listProducts, createProduct, getProduct; _Coverage_ "getProductPrice — PriceList — price-list", "createPriceList — PriceList — price-list", "getPriceList — PriceList — price-list", "setPrice — PriceList — price-list". Replaced `Decided` line: "2026-10-20 · Claire Martin, architect · proposal: option 1 (delta 1.0.0 → 1.1.0: `getProductPrice` moves from `product` to `price-list`)" |
