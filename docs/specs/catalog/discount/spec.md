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

## Source parts owned

What this bounded context owns in its source. The source says what each part holds; this spec does not repeat it.

| Unit | Where | What it is for |
| --- | --- | --- |
| createDiscount | [POST /discounts](../../../contracts/catalog.openapi.json) (`/paths/~1discounts/post`) | Create a discount |
| getDiscount | [GET /discounts/{discountId}](../../../contracts/catalog.openapi.json) (`/paths/~1discounts~1{discountId}/get`) | Read a discount |
| Tag `Discount` | [`/tags/2/description`](../../../contracts/catalog.openapi.json) | What the tag groups |
| `Discount`, `Money` schemas | [`/components/schemas`](../../../contracts/catalog.openapi.json) | The discount and an amount as the operations exchange them; `Money` is also used by `setPrice` of `price-list` and `getProductPrice` of `product` |

## Business rules

> **TO FILL BEFORE THIS BOUNDED CONTEXT IS USED.** This section is left empty on purpose: a business rule cannot be deduced from a source or from code. While this banner stands, the bounded context is not fit for design or implementation.

<!-- Keep one banner: "TO FILL" while the section is empty, "TO VALIDATE" while it holds only copied rules. -->

| # | Rule | Trigger | Expected effect | Source |
| --- | --- | --- | --- | --- |

Remove the banner once the section is filled and confirmed.

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

## Applicable area rules and invariants

| Entry | What it constrains here |
| ----- | ----------------------- |
| INV-DATA-001 of `../../invariants.md` | The `minimumOrder` of a discount |
| AR-1 of `../area.md` | Every error answer of its two operations; its list of 404 codes does not name `DISCOUNT_NOT_FOUND`, a reported gap (architect, 2026-10-20) |
| AR-2 of `../area.md` | What it logs (architect, 2026-10-20) |
| AR-3 of `../area.md` | `discountId` in the path of `getDiscount` (architect, 2026-10-20) |
| AR-4 of `../area.md` | The body of `createDiscount` (architect, 2026-10-20) |

AR-5 does not bind `discount` in this version (architect, 2026-10-20).

## Open questions

- What is the order a discount reduces ("Reductions on an order.", `/tags/2/description`; `minimumOrder`)? No bounded context and no contract defines it: who applies a discount to an order?
- Which values of `percent` are accepted: zero, above 100, negative?
- `period` is sent to `createDiscount` but `Discount` does not return it: is it kept?
- May `period.end` come before `period.start`, and what does an absent `end` mean?
- Is `period` (date-time, `start` / `end`) the same notion as the `validity` of a price list (date, `from` / `to`), or another one?
- Must the currency of `minimumOrder` be a known ISO 4217 code, as `price-list`'s BR-4 requires of a price?
- Does a discount apply to some products or price lists? The contract links it to none.
- `Accept-Language` is a parameter of both operations while `Discount` carries no label: does it change anything?
