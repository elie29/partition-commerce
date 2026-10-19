# product

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

Describe what the shop sells: create a product, read it, list the products, and read the price of a product in a price list.

## Scope

**In scope:** the products, and the reading of a product's price in a price list (`getProductPrice`).

**Out of scope:** the price lists and the setting of a price (`price-list`); the discounts (`createDiscount`, `getDiscount`, out of the decomposition's scope).

## Language

One meaning per word, no synonyms. A word that changes meaning in a neighbour marks the boundary.

| Term | Meaning in this bounded context |
| ---- | ------------------------------- |

## Source parts owned

What this bounded context owns in its source. The source says what each part holds; this spec does not repeat it.

| Unit | Where | What it is for |
| --- | --- | --- |
| listProducts | [GET /products](../../../contracts/catalog.openapi.json) (`/paths/~1products/get`) | List the products, one page at a time |
| createProduct | [POST /products](../../../contracts/catalog.openapi.json) (`/paths/~1products/post`) | Create a product |
| getProduct | [GET /products/{productId}](../../../contracts/catalog.openapi.json) (`/paths/~1products~1{productId}/get`) | Read a product |
| getProductPrice | [GET /products/{productId}/price](../../../contracts/catalog.openapi.json) (`/paths/~1products~1{productId}~1price/get`) | Read the price of a product in a price list |
| Tag `Product` | [`/tags/0/description`](../../../contracts/catalog.openapi.json) | What the tag groups |
| `Product` schema | [`/components/schemas/Product`](../../../contracts/catalog.openapi.json) | The product as the operations return it |

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
| INV-DATA-001 of `../../invariants.md` | The price `getProductPrice` returns (architect, 2026-10-19) |

## Open questions

- The `Product` response holds neither the `description`, the `attributes` nor the `ean` that `createProduct` accepts: are they kept, and where are they read? (2026-10-19)
- What does `createProduct` refuse with `409 CONFLICT`: which state of the products does the request conflict with? (2026-10-19)
- `getProductPrice` reads a price held by a price list: through which contract of `price-list` is it read? (2026-10-19)
