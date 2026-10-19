# Bounded contexts — catalog

> **Source:** `docs/contracts/catalog.openapi.json`
>
> **Source version:** 1.0.0
>
> **Provenance:** 2026-10-19 · 5016f59abcc032d499cd375b285e213b3b90cc56
>
> **Scope:** tag:Product; tag:PriceList
>
> **Units covered:** 7 / 9
>
> **Decided:** 2026-10-19 · [architect] · proposal: option 1

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
| product | listProducts, createProduct, getProduct, getProductPrice | C2, C3, C4 (hint: C1, tag `Product`) | The terminal resource is `products`; `getProductPrice` (`…/products/{productId}/price`) is a sub-resource of `products`, its root aggregate (C2, R3). Its vocabulary (reference, label, attributes, translations) stands apart from the price lists'; a price list knows a product only by its `productId`. No named third party (C5 no). |
| price-list | createPriceList, getPriceList, setPrice | C2, C3, C4 (hint: C1, tag `PriceList`) | The terminal resource is `price-lists`; `setPrice` (`…/price-lists/{priceListId}/prices/{productId}`) is a sub-resource of `price-lists`. Its vocabulary (name, currency, channel, validity, price, effectiveFrom) stands apart from the products'. No named third party (C5 no). Against R4: it shares `Price` and `Money` with `getProductPrice` of `product`; weak sharing, it does not prevail. |

## Decisions not to split

None.

## Alternatives rejected

| Option | Bounded contexts | Why not chosen | Decided by | Date |
| --- | --- | --- | --- | --- |
| 2 | `product` (listProducts, createProduct, getProduct), `price-list` (createPriceList, getPriceList, setPrice, getProductPrice) | "`getProductPrice` is read under `/products`, its root aggregate (C2, R3)" | [architect] | 2026-10-19 |
| 3 | `catalog` (all seven operations), under R4 | "one bounded context would hold two root aggregates" | [architect] | 2026-10-19 |

## Kept against the criteria

None.

## Gaps detected

- "Every amount is stored in minor units, with its currency." ([`/info/description`](../../contracts/catalog.openapi.json)) is a rule of the contract, the one `docs/specs/billing/area.md` holds as AR-2 ([BA], 2026-10-19): it spans two areas, goes to no area file, and is quoted as a candidate under `Applicable area rules and invariants` of `product` (getProductPrice) and `price-list` (setPrice). Candidate for the admission test of the invariant register. Nothing moved in `billing`. Resolved on 2026-10-19 ([architect]): passes the admission test, written as INV-DATA-001 of `docs/specs/invariants.md` (category `DATA`, the register's only category for now); `product` and `price-list` name it. AR-2 of `billing` removed, its place kept by a note in `docs/specs/billing/area.md`; `invoice`, `payment` and `refund` name INV-DATA-001 instead ([architect], 2026-10-19). The `BIGINT` of AR-2 moves to the database rule: "An amount column is a `BIGINT` named `<what>_minor`, its currency column `<what>_currency`." ([TL], 2026-10-19, written in `.apm/instructions/backend-database.instructions.md`, deployed by `apm install`). "Three letters" and "never a decimal" stay with the words Currency and Minor units of `billing`'s Language ([architect], 2026-10-19). Migrations V4 and V6 are not edited ([TL], 2026-10-19); the comments and the error messages that cite AR-2 in `src/main/java/com/commerce/payment/domain/Money.java` and `src/main/java/com/commerce/refund/domain/Money.java` name INV-DATA-001 instead, with the catalog work ([TL], 2026-10-19): pending.
- No invariant register existed at `docs/specs/invariants.md`: created on 2026-10-19 with INV-DATA-001.
- Coupling outside the scope: `Money` is used by `setPrice`, `getProductPrice` and by `createDiscount`, `getDiscount` (out of scope). Extend the scope to `tag:Discount` or not?
- `ean` of `createProduct` is deprecated with "Use attributes.gtin." and no reason: pointed to, not a decision.
- None of the contract's field or parameter descriptions is a business rule: they say what the API does when a value is absent, or what a parameter means ([BA], 2026-10-19). They are pointed to, not copied.
- The common error envelope (`.claude/rules/backend-api-contract.md`), written inline in all nine operations, is a cross-cutting convention: left out of the coupling matrix.
- No orphan schema.

## Coverage

| Unit | Grouping | Bounded context |
| --- | --- | --- |
| listProducts | Product | product |
| createProduct | Product | product |
| getProduct | Product | product |
| getProductPrice | Product | product |
| createPriceList | PriceList | price-list |
| getPriceList | PriceList | price-list |
| setPrice | PriceList | price-list |
| createDiscount | Discount | out of scope |
| getDiscount | Discount | out of scope |

## History

| Date | Version | Provenance | Nature | Detail |
| ---- | ------- | ---------- | ------ | ------ |
