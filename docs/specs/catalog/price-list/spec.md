# price-list

> **Source:** `docs/contracts/catalog.openapi.json`
>
> **Source version:** 1.0.0
>
> **Provenance:** 2026-10-19 · 5016f59abcc032d499cd375b285e213b3b90cc56
>
> **Scope:** tag:Product; tag:PriceList
>
> **Decomposition:** see `../bounded-contexts.md`

<!-- The header above is filled by partition-shape, or partition-reshape for a fission. -->
<!-- The empty ">" lines keep each header line on its own line when the Markdown is reformatted. -->

## Business objective

Group prices by list: create a price list, read it, and set the price of a product in it.

## Scope

**In scope:** the price lists and the prices they hold.

**Out of scope:** the products and the reading of a product's price (`getProductPrice`, `product`); the discounts (`createDiscount`, `getDiscount`, out of the decomposition's scope).

## Language

One meaning per word, no synonyms. A word that changes meaning in a neighbour marks the boundary.

| Term | Meaning in this bounded context |
| ---- | ------------------------------- |

## Source parts owned

What this bounded context owns in its source. The source says what each part holds; this spec does not repeat it.

| Unit | Where | What it is for |
| --- | --- | --- |
| createPriceList | [POST /price-lists](../../../contracts/catalog.openapi.json) (`/paths/~1price-lists/post`) | Create a price list |
| getPriceList | [GET /price-lists/{priceListId}](../../../contracts/catalog.openapi.json) (`/paths/~1price-lists~1{priceListId}/get`) | Read a price list |
| setPrice | [PUT /price-lists/{priceListId}/prices/{productId}](../../../contracts/catalog.openapi.json) (`/paths/~1price-lists~1{priceListId}~1prices~1{productId}/put`) | Set the price of a product in a price list |
| Tag `PriceList` | [`/tags/1/description`](../../../contracts/catalog.openapi.json) | What the tag groups |
| `PriceList`, `Price`, `Money` schemas | [`/components/schemas`](../../../contracts/catalog.openapi.json) | The price list, a price and an amount as the operations exchange them; `Price` and `Money` are also used by `getProductPrice` of `product`, `Money` by the discounts |

## Business rules

> **TO FILL BEFORE THIS BOUNDED CONTEXT IS USED.** This section is left empty on purpose: a business rule cannot be deduced from a source or from code. While this banner stands, the bounded context is not fit for design or implementation.

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
| INV-DATA-001 of `../../invariants.md` | The price `setPrice` sets (architect, 2026-10-19) |

## Open questions

- What does `createPriceList` refuse with `409 CONFLICT`: which state of the price lists does the request conflict with? (2026-10-19)
- Must the product named in the path of `setPrice` exist? The contract documents a `404` only for an unknown price list. (2026-10-19)
- May the currency of a price set by `setPrice` differ from the currency of its price list? (2026-10-19)
- The `PriceList` response does not hold the `validity` that `createPriceList` accepts: is it kept, and where is it read? (2026-10-19)
