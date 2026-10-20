# discount

> **Source:** `docs/contracts/catalog.openapi.json`
>
> **Source version:** 1.0.0
>
> **Provenance:** 2026-10-20 · 5016f59abcc032d499cd375b285e213b3b90cc56
>
> **Scope:** tag:Product; tag:PriceList; tag:Discount
>
> **Decomposition:** see `../bounded-contexts.md`

<!-- The header above is filled by partition-shape, or partition-reshape for a fission. -->
<!-- The empty ">" lines keep each header line on its own line when the Markdown is reformatted. -->

## Business objective

Grant reductions on an order: create a discount and read it.

## Scope

**In scope:** the discounts.

**Out of scope:** the products and the reading of a product's price (`product`); the price lists and the setting of a price (`price-list`); the order a discount reduces, which no operation of the contract handles.

## Language

One meaning per word, no synonyms. A word that changes meaning in a neighbour marks the boundary. The words shared by `catalog` are in `../area.md`.

| Term | Meaning in this bounded context |
| ---- | ------------------------------- |
| Discount | A reduction of 1 to 90 percent, with an optional minimum order (BA, 2026-10-20). |

## Source parts owned

What this bounded context owns in its source. The source says what each part holds; this spec does not repeat it.

| Unit | Where | What it is for |
| --- | --- | --- |
| createDiscount | [POST /discounts](../../../contracts/catalog.openapi.json) (`/paths/~1discounts/post`) | Create a discount |
| getDiscount | [GET /discounts/{discountId}](../../../contracts/catalog.openapi.json) (`/paths/~1discounts~1{discountId}/get`) | Read a discount |
| Tag `Discount` | [`/tags/2/description`](../../../contracts/catalog.openapi.json) | What the tag groups |
| `Discount`, `Money` schemas | [`/components/schemas`](../../../contracts/catalog.openapi.json) | The discount and an amount as the operations exchange them; `Money` is also used by `setPrice` of `price-list` and `getProductPrice` of `product` |

## Business rules

| # | Rule | Trigger | Expected effect | Source |
| --- | --- | --- | --- | --- |
| BR-1 | A discount is between 1 and 90 percent, both bounds included. | A `createDiscount` with a `percent` below 1 or above 90 | Refused: 400, nothing created | BA, 2026-10-20 |
| BR-2 | The period of a discount governs nothing in this version, as the validity of a price list. | A `createDiscount` | `period` is accepted, not kept, not returned; an end before its start is accepted; a value the contract's schema refuses is still refused, 400 (`backend-api-contract`) | BA, 2026-10-20 |
| BR-3 | The amount of a minimum order is never negative. | A `createDiscount` whose `minimumOrder` amount is negative | Refused: 400, nothing created; zero is a minimum order | BA, 2026-10-20 |
| BR-4 | A currency is a known ISO 4217 code, as `price-list`'s BR-4 says. | A `createDiscount` whose `minimumOrder` currency the contract's pattern accepts but is not a known code (`XYZ`) | Refused: 400, nothing created; content, checked with the body (BR-9); incoming requests only | BA, 2026-10-20 |
| BR-5 | A discount keeps its `minimumOrder`. | A `createDiscount` that sends one | Returned as kept by `createDiscount` and `getDiscount`; it governs nothing in this version | BA, 2026-10-20 |
| BR-6 | A discount names no product, no price list and no order; nothing applies it in this version. | Any operation | `discount` stores and returns discounts; it reads no neighbour | architect, 2026-10-20 |
| BR-7 | Two discounts may overlap. | A `createDiscount` like an existing discount | Created; `createDiscount` never answers 409 in this version. A retried `createDiscount` creates another discount: `X-Request-Id` does not make a create idempotent; avoiding a retry is the caller's job | BA, 2026-10-20; the retry: TL, 2026-10-20 |
| BR-8 | An unknown discount is not found. | A `getDiscount` whose `discountId` is not a UUID or names no discount | 404, `DISCOUNT_NOT_FOUND` | architect, 2026-10-20 (AR-1, AR-3) |
| BR-9 | A `createDiscount` checks its body as one group. | A `createDiscount` refused on more than one ground | `percent` (BR-1), the currency (BR-4), the amount (BR-3) and any value the schema refuses are all content: one 400, one item per field at fault (AR-1) | TL, 2026-10-20 (the catalog's order of checks, `backend-api-contract`) |
| BR-10 | `Accept-Language` changes nothing. | A `createDiscount` or `getDiscount` that carries it | Accepted and ignored | TL, 2026-10-20 |

## Decisions

The decisions the source states, copied with their reason. A decision taken while building lives in the plan that took it; what it established is written in the sections above.

| Decision | Reason | Source | Date |
| -------- | ------ | ------ | ---- |

## Persistence

No schema documents are declared for this project.

## External dependencies

None.

## Contracts with neighbours

<What this bounded context shows to the others, and what it consumes from them. A neighbour is known by this and nothing else. An API contract given for this bounded context once it exists is recorded here, never as a second source of its decomposition.>

| Neighbour | Direction | Contract | What crosses |
| --------- | --------- | -------- | ------------ |
| Callers | inbound | `docs/contracts/catalog.openapi.json` 1.0.0: `createDiscount`, `getDiscount` (tag `Discount`), served under `/commerce/v1/catalog` | as the contract says; authentication is out of this version: the contract's `security` is kept as written, nothing of it is built, 401 and 403 are never answered (TL, 2026-10-20); anyone creates a discount, the demo runs without a login (PO, 2026-10-20) |

## Applicable area rules and invariants

| Entry | What it constrains here |
| ----- | ----------------------- |
| INV-DATA-001 of `../../invariants.md` | The `minimumOrder` of a discount |
| AR-1 of `../area.md` | Every error answer of its two operations (architect, 2026-10-20) |
| AR-2 of `../area.md` | What it logs (architect, 2026-10-20) |
| AR-3 of `../area.md` | `discountId` in the path of `getDiscount` (architect, 2026-10-20) |
| AR-4 of `../area.md` | The body of `createDiscount` (architect, 2026-10-20) |

AR-5 does not bind `discount` in this version (architect, 2026-10-20).

## Open questions

Findings of the attack of 2026-10-20 (`partition-attack`). Findings 2 to 5 were settled on 2026-10-20 and left this section: BR-4 keeps its citation and the amount rules stay separate copies, under the gap recorded in `../bounded-contexts.md` (architect); a retry is the caller's to avoid (TL, BR-7); `minimumOrder` governs nothing (BA, BR-5).

1. **Anonymous discounts that outlive the demo.** Scenario: without a token, a caller sends `createDiscount` with `percent` 90, a thousand times; each is created (BR-7), with no record of who created it. Impact: today none, as nothing applies a discount (BR-6); the day something does, every discount created anonymously during the demo applies, and their number is unbounded. Missing: what becomes of the discounts created without a login once authentication is built or something applies them. Accepted for this version, no limit on how many are created; stays open until authentication is built or something applies discounts (PO, 2026-10-20).
2. **A date-time without seconds.** `period/start` or `period/end` as `2026-01-01T00:00Z` is accepted, which RFC 3339 refuses; no effect, the period is not kept (BR-2). Accepted in this version, in `price-list` and `discount` alike; is it refused once a period is kept? (TL, 2026-10-20)

Attacks with nothing to report: **the session** (no status, no permission, no selection that can change under a caller: a discount is created and read, never changed); **the contract** (the spec shows nothing the contract does not declare: 401 and 403 are declared and never answered, as in the rest of `catalog`; a `null` `minimumOrder` or `period` is a value the OpenAPI 3.1 schema refuses: 400, `backend-api-contract`); **the data** (no amount in a log, AR-2; no value in an error, AR-1; the period accepted and not kept is BR-2).
