# price-list

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

Group prices by list: create a price list, read it, and set the price of a product in it.

## Scope

**In scope:** the price lists and the prices they hold.

**Out of scope:** the products and the reading of a product's price (`getProductPrice`, `product`); the discounts (`createDiscount`, `getDiscount`, out of the decomposition's scope).

## Language

One meaning per word, no synonyms. A word that changes meaning in a neighbour marks the boundary. The words shared by `catalog` (Product, Price list, Price, Day, Currency, Amount, Applies, Blank, Character, Identifier, Minor units, Refused) are in `../area.md`.

| Term | Meaning in this bounded context |
| ---- | ------------------------------- |
| Channel | `SHOP`, `MARKETPLACE` or `B2B`: kept and returned, governs no price (BA, 2026-10-19). |

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

| # | Rule | Trigger | Expected effect | Source |
| --- | --- | --- | --- | --- |
| BR-1 | A price list holds one price per product. | A `setPrice` for a product already priced in that list; two `setPrice` for one product and list at the same moment | The new price replaces the old one: 200, the price set; the list keeps no history. One price per product and list, held by the store's key: the last to commit replaces the other. The last price set replaces the price whatever its `effectiveFrom`: a future price hides the current one, and until it applies, by the day as `product`'s BR-9 reads it, the product has no price in that list | BA (rule, trigger, effect, a future price, by the day), architect (the key), 2026-10-19 |
| BR-2 | The amount of a price is never negative. | A `setPrice` with a negative amount | Refused: 400, nothing set; zero is a price | BA (rule; worded after the area's Price), 2026-10-19 |
| BR-3 | A price is in its price list's currency. | A `setPrice` whose currency is not its list's | Refused: 400, nothing set | BA, 2026-10-19 |
| BR-4 | A currency is a known ISO 4217 code, as `invoice`'s rule on a currency says: a code of the JDK's `Currency` list that does not start with `X`. The list is the JDK's, updated with the JDK; fund codes and the withdrawn codes it still lists are known in this version. | A `createPriceList` or a `setPrice` with a currency the contract's pattern accepts but that is not a known code (`XYZ`) | Refused: 400, nothing created or set; content, checked with the body (BR-10). BR-4 is checked on incoming requests only: a stored code is read as kept, a code a later JDK drops included | BA (the whole rule, incoming only), architect (its own check, content, incoming only), 2026-10-19 |
| BR-5 | A name is not blank. | A `createPriceList` with a blank `name` | Refused: 400, nothing created | BA, 2026-10-19 |
| BR-6 | The channel and the validity of a price list govern no price in this version. | A `createPriceList` | The channel is kept and returned; the validity is accepted, not kept, a validity ending before it starts included; a value the contract's schema refuses is still refused, 400 (`backend-api-contract`) | BA, TL, 2026-10-19 |
| BR-7 | `createPriceList` never answers 409 in this version. | A `createPriceList` | No 409 | BA, 2026-10-19 |
| BR-8 | A price takes effect at its `effectiveFrom`, read in UTC; absent, now. | A `setPrice` | `effectiveFrom` absent is the server's clock in UTC; one with an offset is read in UTC; one without an offset is refused, 400, the schema's date-time requiring one; one in the past is accepted; returned as kept | BA, TL, 2026-10-19 |
| BR-9 | An unknown price list is not found. | A `getPriceList` or a `setPrice` whose `priceListId` is not a UUID or names no list | 404, `PRICE_LIST_NOT_FOUND` | architect, 2026-10-19 |
| BR-12 | A price is set for the product identifier given, whether `product` holds it or not. | A `setPrice` | `price-list` does not read `product`; a `productId` that is not a UUID: 404, `PRICE_LIST_NOT_FOUND`, the only 404 `setPrice` declares | architect, 2026-10-19 |
| BR-10 | A `setPrice` checks its body, then its price list, then its product identifier, then BR-3. | A `setPrice` refused on more than one ground | The body first: a negative amount, an unknown currency (`XYZ`), any value the schema refuses: 400. Then the `priceListId` (BR-9), then the `productId` (BR-12): 404. Then a currency other than the list's (BR-3): 400. The first group refused stops the checks, and the 400 carries one item per field at fault within it (AR-1): a negative amount with `XYZ` gives two items, both content; a negative amount in a currency other than the list's gives one, BR-3 being checked after | architect, 2026-10-19 (`backend-api-contract`; corrected the same day: "a negative amount with `XYZ` gives two items") |
| BR-13 | `Accept-Language` changes nothing. | A `createPriceList`, `getPriceList` or `setPrice` that carries it | Nothing changes in this version | BA, 2026-10-19 |

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
| Callers | inbound | `docs/contracts/catalog.openapi.json` 1.0.0: `createPriceList`, `getPriceList`, `setPrice` (tag `PriceList`), served under `/commerce/v1/catalog` | as the contract says; authentication is out of this version: the contract's `security` is kept as written, nothing of it is built, 401 and 403 are never answered (TL, PO, 2026-10-19) |
| `product` | inbound, in process | `PriceListPrices`, in `price-list`'s application layer | in: a product, a price list, a day; out: the amount, the currency and the `effectiveFrom` of the price that applies (BR-1, and `product`'s BR-9), or none; an unknown price list and no price that day are not told apart: `product` answers both with its one 404, `PRICE_NOT_FOUND` (architect, 2026-10-19; the TL's three codes withdrawn). Called only from `product`'s outbound adapter (architect, 2026-10-19) |

## Applicable area rules and invariants

| Entry | What it constrains here |
| ----- | ----------------------- |
| INV-DATA-001 of `../../invariants.md` | The price `setPrice` sets (architect, 2026-10-19) |
| AR-1 of `../area.md` | Every error answer of its three operations (BA, TL, 2026-10-19) |
| AR-2 of `../area.md` | What it logs: never an amount (TL, 2026-10-19; was BR-11 of this spec) |
| AR-3 of `../area.md` | `priceListId` and `productId` in the path of `setPrice`, `priceListId` of `getPriceList` (TL, 2026-10-19) |
| AR-4 of `../area.md` | The bodies of `createPriceList` and `setPrice` (TL, 2026-10-19) |
| AR-5 of `../area.md` | `effectiveFrom` (TL, 2026-10-19) |

## Open questions


None (2026-10-19).
