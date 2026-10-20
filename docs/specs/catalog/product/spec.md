# product

> **Source:** `docs/contracts/catalog.openapi.json`
>
> **Source version:** 1.0.0
>
> **Provenance:** 2026-10-19 · 5016f59abcc032d499cd375b285e213b3b90cc56
>
> **Scope:** tag:Product; tag:PriceList; tag:Discount
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

One meaning per word, no synonyms. A word that changes meaning in a neighbour marks the boundary. The words shared by `catalog` (Product, Price list, Price, Day, Currency, Amount, Applies, Blank, Character, Identifier, Minor units, Refused) are in `../area.md`.

| Term | Meaning in this bounded context |
| ---- | ------------------------------- |
| Reference | The code a product is known by, unique (BR-1) (BA, 2026-10-19). |
| Label | The name of a product as given at creation (BA, 2026-10-19). |

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

| # | Rule | Trigger | Expected effect | Source |
| --- | --- | --- | --- | --- |
| BR-1 | A product reference is unique. | A `createProduct` whose reference another product holds, references compared exactly as written; two `createProduct` with one reference at the same moment | Refused: 409, nothing created; its `errors` item names `/reference` as its `field`. A unique constraint on the reference in the store holds the rule: the first to commit holds, the other gets 409 | BA (rule, trigger, effect), architect (the constraint), TL (the `field`), 2026-10-19 |
| BR-2 | `createProduct` answers 409 for a reference already held, and for nothing else. | A `createProduct` refused on any other ground | Not a 409 | BA, 2026-10-19 |
| BR-3 | A label is not blank. | A `createProduct` with a blank `label` | Refused: 400, nothing created; with a `reference` off its pattern too, two items, both content (AR-1) | BA, architect (two items), 2026-10-19 |
| BR-4 | `description`, `ean` and `attributes`, translations included, are accepted and not kept. | A `createProduct` that carries them | No operation returns them; a value the contract's schema refuses is still refused, 400 (`backend-api-contract`) | BA, TL, 2026-10-19 |
| BR-5 | A product is created at the server's clock, in UTC. | A `createProduct` accepted | 201 with the product; `createdAt` kept and returned wherever the contract's `Product` carries it (`createProduct`, `getProduct`, `listProducts`); `Location` is the path `/commerce/v1/catalog/products/{productId}` | BA (`createdAt`), TL (`Location`), 2026-10-19 |
| BR-6 | The label is the one given at creation, on every operation. | `localized` or `Accept-Language` given | They change nothing in this version | BA, 2026-10-19 |
| BR-7 | A page of `listProducts` is ordered by reference, ascending. | A `listProducts` | Products by reference, ascending; `references` compared exactly, as a reference is: an unknown reference matches nothing, a reference given twice counts once; `references=` empty is no filter; a page past the last: 200, no item, the totals true; no maximum on `page` nor on the number of `references` in this version, but a `page` beyond the integer range is refused, 400, as the schema's type says; a product created between two pages may shift them, an item seen twice | BA, PO (bounds, empty filter, the shift accepted), TL and PO (the integer range), 2026-10-19 |
| BR-8 | An unknown product is not found. | A `getProduct` whose product does not exist, or whose `productId` is not a UUID | 404, `PRODUCT_NOT_FOUND` | architect, 2026-10-19 |
| BR-9 | The price of a product in a price list on a day is the price the list holds now, when its `effectiveFrom` is on or before the end of that day, in UTC: a price applies by the day. | A `getProductPrice`; `at` absent is today; a day ends before 00:00 UTC of the next day; an `at` in the past is answered against the price the list holds now | 200 with the price that applies. No such product, no such price list, or no price that applies: one 404, `PRICE_NOT_FOUND`, one `detail`, "No such product, or no price for it in that price list"; `PriceListPrices` does not tell them apart | BA (rule, by the day, `at`, today, end of day), architect (one 404, its code), 2026-10-19 |
| BR-10 | `getProductPrice` checks its content first. | A `getProductPrice` refused on more than one ground | A `priceListId` or an `at` the schema refuses is content: 400, before the product; then any 404 of BR-9. The first refusal stops the checks | architect, TL, 2026-10-19 |

## Decisions

The decisions the source states, copied with their reason. A decision taken while building lives in the plan that took it; what it established is written in the sections above.

| Decision | Reason | Source | Date |
| -------- | ------ | ------ | ---- |

## Persistence

No schema documents are declared for this project.

## External dependencies

None.

## Contracts with neighbours

| Neighbour | Direction | Contract | What crosses |
| --------- | --------- | -------- | ------------ |
| Callers | inbound | `docs/contracts/catalog.openapi.json` 1.0.0: `listProducts`, `createProduct`, `getProduct`, `getProductPrice` (tag `Product`), served under `/commerce/v1/catalog` | as the contract says; authentication is out of this version: the contract's `security` is kept as written, nothing of it is built, 401 and 403 are never answered (TL, PO, 2026-10-19) |
| `price-list` | outbound, in process | `PriceListPrices`, in `price-list`'s application layer | in: a product, a price list, a day; out: the amount, the currency and the `effectiveFrom` of the price that applies, or none; an unknown price list and no price that day are not told apart. Called only from `product`'s outbound adapter, which uses nothing else of `price-list` (architect, 2026-10-19) |

## Applicable area rules and invariants

| Entry | What it constrains here |
| ----- | ----------------------- |
| INV-DATA-001 of `../../invariants.md` | The amount `getProductPrice` answers, read from `price-list` (architect, 2026-10-19) |
| AR-1 of `../area.md` | Every error answer of its four operations (BA, TL, 2026-10-19) |
| AR-2 of `../area.md` | What it logs: never a label, a reference value or the amount read from `price-list` (TL, 2026-10-19) |
| AR-3 of `../area.md` | `productId` in the path, `priceListId` in the query of `getProductPrice` (TL, 2026-10-19) |
| AR-4 of `../area.md` | The body of `createProduct` (TL, 2026-10-19) |
| AR-5 of `../area.md` | `createdAt` (TL, 2026-10-19) |

## Open questions

None (2026-10-19).

