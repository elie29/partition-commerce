# product

> **Source:** `docs/contracts/catalog.openapi.json`
>
> **Source version:** 2.0.0
>
> **Provenance:** 2026-10-20 · 1e1a06661e4397e05abbb1a87ec7b06e2ad1812d
>
> **Scope:** tag:Product; tag:PriceList
>
> **Decomposition:** see `../bounded-contexts.md`

<!-- The header above is filled by partition-shape, or partition-reshape for a fission. -->
<!-- The empty ">" lines keep each header line on its own line when the Markdown is reformatted. -->

## Business objective

Describe what the shop sells and its prices: create a product, read it, list the products, retire it; group prices by list: create a price list, read it, set the price of a product in it, and read the price of a product in it on a day.

## Scope

**In scope:** the products; the price lists, the prices they hold, and the reading of a product's price in a price list.

**Out of scope:** the discounts, moved to another system with catalog 2.0.0 (BA, 2026-10-20).

## Language

One meaning per word, no synonyms. A word that changes meaning in a neighbour marks the boundary. The words of the catalog area came down here when its area file was deleted (BA, 2026-10-20).

| Term | Meaning in this bounded context |
| ---- | ------------------------------- |
| Product | What the shop sells (BA, 2026-10-19; came down from the area file: BA, 2026-10-20). |
| Price list | The prices of products in one currency, for one channel (BA, 2026-10-19; came down from the area file: BA, 2026-10-20). |
| Currency | A three-letter ISO 4217 code; one meaning with `billing`'s (BA, architect, 2026-10-19; came down from the area file: BA, 2026-10-20). |
| Amount | A sum of money in one currency, stored in minor units; of a price, its amount; one meaning with `billing`'s (BA, 2026-10-19; came down from the area file: BA, 2026-10-20). |
| Identifier | A UUID made by the bounded context that creates the resource (BA, 2026-10-19; came down from the area file: BA, 2026-10-20). |
| Minor units | The smallest unit of a currency, counted as a whole number; one meaning with `billing`'s (BA, 2026-10-19; came down from the area file: BA, 2026-10-20). |
| Refused | What `product` answers to a request one of its rules does not accept; nothing changes; one meaning with `billing`'s (BA, 2026-10-19; came down from the area file: BA, 2026-10-20). |
| Reference | The code a product is known by, unique (BR-1) (BA, 2026-10-19). |
| Label | The name of a product as given at creation (BA, 2026-10-19). |
| Retired | The state of a product the catalog manager has retired; final: a retired product is never reactivated (BA, PO, 2026-10-20). |
| Active | A product not retired (BA, 2026-10-20). |
| Catalog manager | The person who maintains the catalog (PO, 2026-10-20). |
| Price | An amount in `Money` and its `effectiveFrom`; its amount is "the amount" (BA, 2026-10-19; from `price-list`: BA, 2026-10-20). |
| Day | A calendar date in UTC (BA, 2026-10-19; from `price-list`: BA, 2026-10-20). |
| Applies | Of a price, on a day: its `effectiveFrom` is on or before the end of that day, in UTC (BA, 2026-10-19; from `price-list`: BA, 2026-10-20). |
| Channel | `SHOP`, `MARKETPLACE` or `B2B`: kept and returned, governs no price (BA, 2026-10-19; from `price-list`: BA, 2026-10-20). |
| Blank | Empty or only white space; a non-breaking space (U+00A0, U+2007, U+202F) is white space; one meaning with `billing`'s (BA, 2026-10-19; came down from the area file: BA, 2026-10-20). |
| Character | A Unicode code point: an emoji is one character; one meaning with `billing`'s (BA, 2026-10-19; came down from the area file: BA, 2026-10-20). |
| Product identifier | The `productId` a price names: not checked against the products, a price may name a product that does not exist (BR-21); not the Product, which the shop sells (BA, 2026-10-20). |
| Not found | Of a product read: `PRODUCT_NOT_FOUND` (BR-8); of a price list: `PRICE_LIST_NOT_FOUND` (BR-18); of a price: `PRICE_NOT_FOUND`, an unknown product included (BR-23). The codes are the contract's (BA, 2026-10-20). |

## Source parts owned

What this bounded context owns in its source. The source says what each part holds; this spec does not repeat it.

| Unit | Where | What it is for |
| --- | --- | --- |
| listProducts | [GET /products](../../../contracts/catalog.openapi.json) (`/paths/~1products/get`) | List the products, one page at a time |
| createProduct | [POST /products](../../../contracts/catalog.openapi.json) (`/paths/~1products/post`) | Create a product |
| getProduct | [GET /products/{productId}](../../../contracts/catalog.openapi.json) (`/paths/~1products~1{productId}/get`) | Read a product |
| createPriceList | [POST /price-lists](../../../contracts/catalog.openapi.json) (`/paths/~1price-lists/post`) | Create a price list |
| getPriceList | [GET /price-lists/{priceListId}](../../../contracts/catalog.openapi.json) (`/paths/~1price-lists~1{priceListId}/get`) | Read a price list |
| setPrice | [PUT /price-lists/{priceListId}/prices/{productId}](../../../contracts/catalog.openapi.json) (`/paths/~1price-lists~1{priceListId}~1prices~1{productId}/put`) | Set the price of a product in a price list |
| getProductPrice | [GET /price-lists/{priceListId}/product-prices/{productId}](../../../contracts/catalog.openapi.json) (`/paths/~1price-lists~1{priceListId}~1product-prices~1{productId}/get`) | Read the price of a product in a price list |
| Tag `Product` | [`/tags/0/description`](../../../contracts/catalog.openapi.json) | What the tag groups |
| Tag `PriceList` | [`/tags/1/description`](../../../contracts/catalog.openapi.json) | What the tag groups |
| `Product` schema | [`/components/schemas/Product`](../../../contracts/catalog.openapi.json) | The product as the operations return it |
| `PriceList`, `Price`, `Money` schemas | [`/components/schemas`](../../../contracts/catalog.openapi.json) | The price list, a price and an amount as the operations exchange them |

## Business rules

| # | Rule | Trigger | Expected effect | Source |
| --- | --- | --- | --- | --- |
| BR-1 | A product reference is unique. | A `createProduct` whose reference another product holds, references compared exactly as written; two `createProduct` with one reference at the same moment | Refused: 409, nothing created; its `errors` item names `/reference` as its `field`. A unique constraint on the reference in the store holds the rule: the first to commit holds, the other gets 409 | BA (rule, trigger, effect), architect (the constraint), TL (the `field`), 2026-10-19 |
| BR-2 | `createProduct` answers 409 for a reference already held, and for nothing else. | A `createProduct` refused on any other ground | Not a 409 | BA, 2026-10-19 |
| BR-3 | A label is not blank. | A `createProduct` with a blank `label` | Refused: 400, nothing created; with a `reference` off its pattern too, two items, both content (BR-27) | BA, architect (two items), 2026-10-19 |
| BR-4 | `description`, `ean` and `attributes`, translations included, are accepted and not kept. | A `createProduct` that carries them | No operation returns them; a value the contract's schema refuses is still refused, 400 (`backend-api-contract`) | BA, TL, 2026-10-19 |
| BR-5 | A product is created at the server's clock, in UTC. | A `createProduct` accepted | 201 with the product; `createdAt` kept and returned wherever the contract's `Product` carries it (`createProduct`, `getProduct`, `listProducts`); `Location` is the path `/commerce/v1/catalog/products/{productId}` | BA (`createdAt`), TL (`Location`), 2026-10-19 |
| BR-6 | The label is the one given at creation, on every operation. | `localized` or `Accept-Language` given | They change nothing in this version | BA, 2026-10-19 |
| BR-7 | A page of `listProducts` is ordered by reference, ascending. | A `listProducts` | Products by reference, ascending; `references` compared exactly, as a reference is: an unknown reference matches nothing, a reference given twice counts once; `references=` empty is no filter; a page past the last: 200, no item, the totals true; no maximum on `page` nor on the number of `references` in this version, but a `page` beyond the integer range is refused, 400, as the schema's type says; a product created between two pages may shift them, an item seen twice | BA, PO (bounds, empty filter, the shift accepted), TL and PO (the integer range), 2026-10-19 |
| BR-8 | An unknown product is not found. | A `getProduct` whose product does not exist, or whose `productId` is not a UUID | 404, `PRODUCT_NOT_FOUND` | architect, 2026-10-19 |
| BR-9 | A product can be retired; a retired product is never reactivated. | The catalog manager retires a product, through the retirement use case of the domain; in this version only its tests reach it: no operation of the contract, no job, no command line, no message; nothing is checked about who retires a product | A new product is active, and every product already in the store is active; the product is retired and stays retired; the retirement keeps the retired state only, neither when nor who; retiring a retired product leaves it retired, nothing changes; retiring a product that does not exist is refused: the use case reports the product not found, as BR-8 does for `getProduct`, nothing changes; no use case reactivates a product; it keeps its reference, which stays taken (BR-1); `getProduct` and `listProducts` answer as before, `Product` unchanged; its prices are not reached | BA (rule, already retired, unknown product, reference, reads, active, the retired state only), PO (the catalog manager, nothing checked), architect (use case, no operation, not visible, neighbours), architect and PO (reached by its tests only), 2026-10-20 |
| BR-10 | A price list holds one price per product. | A `setPrice` for a product already priced in that list; two `setPrice` for one product and list at the same moment | The new price replaces the old one: 200, the price set; the list keeps no history. One price per product and list, held by the store's key: the last to commit replaces the other. The last price set replaces the price whatever its `effectiveFrom`: a future price hides the current one, and until it applies, by the day as BR-23 reads it, the product has no price in that list | BA (rule, trigger, effect, a future price, by the day), architect (the key), 2026-10-19; was BR-1 of `price-list`, carried by the BA on 2026-10-20 |
| BR-11 | The amount of a price is never negative. | A `setPrice` with a negative amount | Refused: 400, nothing set; zero is a price | BA (rule; worded after the area's Price), 2026-10-19; was BR-2 of `price-list`, carried by the BA on 2026-10-20 |
| BR-12 | A price is in its price list's currency. | A `setPrice` whose currency is not its list's | Refused: 400, nothing set | BA, 2026-10-19; was BR-3 of `price-list`, carried by the BA on 2026-10-20 |
| BR-13 | A currency is a known ISO 4217 code, as `invoice`'s rule on a currency says: a code of the JDK's `Currency` list that does not start with `X`. The list is the JDK's, updated with the JDK; fund codes and the withdrawn codes it still lists are known in this version. | A `createPriceList` or a `setPrice` with a currency the contract's pattern accepts but that is not a known code (`XYZ`) | Refused: 400, nothing created or set; content, checked with the body (BR-19). BR-13 is checked on incoming requests only: a stored code is read as kept, a code a later JDK drops included | BA (the whole rule, incoming only), architect (its own check, content, incoming only), 2026-10-19; was BR-4 of `price-list`, carried by the BA on 2026-10-20 |
| BR-14 | A name is not blank. | A `createPriceList` with a blank `name` | Refused: 400, nothing created | BA, 2026-10-19; was BR-5 of `price-list`, carried by the BA on 2026-10-20 |
| BR-15 | The channel and the validity of a price list govern no price in this version. | A `createPriceList` | The channel is kept and returned; the validity is accepted, not kept, a validity ending before it starts included; a value the contract's schema refuses is still refused, 400 (`backend-api-contract`) | BA, TL, 2026-10-19; was BR-6 of `price-list`, carried by the BA on 2026-10-20 |
| BR-16 | `createPriceList` never answers 409 in this version. | A `createPriceList` | No 409 | BA, 2026-10-19; was BR-7 of `price-list`, carried by the BA on 2026-10-20 |
| BR-17 | A price takes effect at its `effectiveFrom`, read in UTC; absent, now. | A `setPrice` | `effectiveFrom` absent is the server's clock in UTC; one with an offset is read in UTC; one without an offset is refused, 400, the schema's date-time requiring one; one in the past is accepted; returned as kept | BA, TL, 2026-10-19; was BR-8 of `price-list`, carried by the BA on 2026-10-20 |
| BR-18 | An unknown price list is not found. | A `getPriceList` or a `setPrice` whose `priceListId` is not a UUID or names no list | 404, `PRICE_LIST_NOT_FOUND` | architect, 2026-10-19; was BR-9 of `price-list`, carried by the BA on 2026-10-20 |
| BR-19 | A `setPrice` checks its body, then its price list, then its product identifier, then BR-12. | A `setPrice` refused on more than one ground | The body first: a negative amount, an unknown currency (`XYZ`), any value the schema refuses: 400. Then the `priceListId` (BR-18), then the `productId` (BR-21): 404. Then a currency other than the list's (BR-12): 400. The first group refused stops the checks, and the 400 carries one item per field at fault within it (BR-27): a negative amount with `XYZ` gives two items, both content; a negative amount in a currency other than the list's gives one, BR-12 being checked after | architect, 2026-10-19 (`backend-api-contract`; corrected the same day: "a negative amount with `XYZ` gives two items"); was BR-10 of `price-list`, carried by the BA on 2026-10-20 |
| BR-21 | A price is set for the product identifier given, whether a product holds it or not. | A `setPrice` | a price does not read the products; a `productId` that is not a UUID: 404, `PRICE_LIST_NOT_FOUND`, the only 404 `setPrice` declares | architect, 2026-10-19; was BR-12 of `price-list`, carried by the BA on 2026-10-20 |
| BR-22 | `Accept-Language` changes nothing. | A `createPriceList`, `getPriceList` or `setPrice` that carries it | Nothing changes in this version | BA, 2026-10-19; was BR-13 of `price-list`, carried by the BA on 2026-10-20 |
| BR-23 | The price of a product in a price list on a day is the price the list holds now, when its `effectiveFrom` is on or before the end of that day, in UTC: a price applies by the day. | A `getProductPrice`; `at` absent is today; a day ends before 00:00 UTC of the next day; an `at` in the past is answered against the price the list holds now | 200 with the price that applies. No such price list, no price for the product in it, an unknown product included, no price that applies, or a `priceListId` or a `productId` that is not a UUID (BR-29): one 404, `PRICE_NOT_FOUND`, the only 404 the contract gives `getProductPrice`, one `detail`, "No such product, or no price for it" (catalog 2.0.0: BA, 2026-10-20); a price does not read the products (BR-21) | BA (rule, by the day, `at`, today, end of day), architect (one 404, its code), 2026-10-19; was BR-9 of `product`, then BR-14 of `price-list`, without its product check, the identifiers' 404: architect, 2026-10-20; carried by the BA on 2026-10-20 |
| BR-24 | `getProductPrice` checks its content first. | A `getProductPrice` refused on more than one ground | An `at` the schema refuses is content: 400, before the identifiers; then any 404 of BR-23. The first refusal stops the checks | architect, TL, 2026-10-19; was BR-10 of `product`, then BR-15 of `price-list`, `priceListId` in the path since 1.1.0: architect, 2026-10-20; carried by the BA on 2026-10-20 |
| BR-25 | A product identifier is answered as stored: lowercase. | A `setPrice` or a `getProductPrice` whose `productId` carries uppercase letters | The price is set or found whatever the case; the `productId` of the answer is in lowercase | architect, 2026-10-20; was BR-16 of `price-list`, carried by the BA on 2026-10-20 |
| BR-26 | A time is kept to the millisecond, in UTC. | `createdAt`, `effectiveFrom` | Kept and returned to the millisecond, in UTC | TL, 2026-10-19; validated as written: TL, architect, 2026-10-19; was AR-5 of the catalog area file, carried by the BA on 2026-10-20 |
| BR-27 | Every catalog error answer carries the common error envelope. | An answer of the contract with a status of 400, 404, 409 or 500 | `detail`: one sentence naming the refusal, no value sent. `errors`: for a 400, one item per field at fault, `code` `VALIDATION_FAILED`; for a 409, one item, `code` `CONFLICT`; for a 404, one item with the contract's code (`PRODUCT_NOT_FOUND`, `PRICE_LIST_NOT_FOUND`, `PRICE_NOT_FOUND`), no `field`. `field`: the JSON pointer of a body field, or the name of a header, query or path parameter. `message`: one sentence saying what is wrong with that field, without its value. `errorId`: a new UUID per error, logged once with the `X-Request-Id`, the path and the status. `path`: under `/commerce/v1/catalog`. Without `X-Request-Id`: 400, logged with the `errorId`, the path and the status. 405, 406 and 415 stay as the framework answers them. A 500: the envelope with its `detail` and its `errorId`, `errors` empty. A body that does not parse: 400, one item, no `field`, `code` `VALIDATION_FAILED`. A malformed `X-Request-Id`: 400, its value never logged. The headers are checked first; the first refusal stops the checks | TL, 2026-10-19 (the codes are the contract's examples); 405, 406, 415: architect, 2026-10-19; validated as written: BA, TL, 2026-10-19; 500, body, malformed `X-Request-Id`: TL, 2026-10-19, validated as written: TL, architect, 2026-10-19; headers first: TL, 2026-10-19; binds `discount`: architect, 2026-10-20; `DISCOUNT_NOT_FOUND`: architect, 2026-10-20; `price-list` merged into `product`: 2026-10-20; was AR-1 of the catalog area file, carried by the BA on 2026-10-20 |
| BR-28 | A catalog log holds identifiers, the path, the status and the `errorId` only; a 500 adds the class and the stack frames of its exception. | Any log of `product` | The `X-Request-Id` is logged with them when it is well formed. Never a label, a reference value, an amount, the `Authorization` header, a refused `X-Request-Id`, or the message of an exception | TL, 2026-10-19; validated as written: TL, architect, 2026-10-19; the `X-Request-Id`: TL, 2026-10-19; the 500: TL, 2026-10-19; binds `discount`: architect, 2026-10-20; the trigger names `discount`: TL, architect, 2026-10-20; `price-list` merged into `product`: 2026-10-20; was AR-2 of the catalog area file, carried by the BA on 2026-10-20 |
| BR-29 | An identifier is a UUID in its 8-4-4-4-12 hexadecimal form. | An identifier given in a path or a query | Any other form is not a UUID: a 404 in the path, a 400 in the query (`backend-api-contract`) | TL, 2026-10-19; validated as written: TL, architect, 2026-10-19; binds `discount`: architect, 2026-10-20; `price-list` merged into `product`: 2026-10-20; was AR-3 of the catalog area file, carried by the BA on 2026-10-20 |
| BR-30 | A body property the schema does not name, and the `Authorization` header, are ignored. | A request carrying them | Accepted; neither changes the answer | TL, 2026-10-19; validated as written: TL, architect, 2026-10-19; binds `discount`: architect, 2026-10-20; `price-list` merged into `product`: 2026-10-20; was AR-4 of the catalog area file, carried by the BA on 2026-10-20 |

BR-20 is not used: `price-list` had no BR-11, which became AR-2 of the catalog area file on 2026-10-19, now BR-28 (BA, 2026-10-20).

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
| Callers | inbound | `docs/contracts/catalog.openapi.json` 2.0.0: `listProducts`, `createProduct`, `getProduct` (tag `Product`), `createPriceList`, `getPriceList`, `setPrice`, `getProductPrice` (tag `PriceList`), served under `/commerce/v1/catalog` | as the contract says; authentication is out of this version: the contract's `security` is kept as written, nothing of it is built, 401 and 403 are never answered (TL, PO, 2026-10-19) |

## Applicable area rules and invariants

| Entry | What it constrains here |
| ----- | ----------------------- |
| INV-DATA-001 of `../../invariants.md` | The price `setPrice` sets and the amount `getProductPrice` answers (architect, 2026-10-19; `getProductPrice`: architect, 2026-10-20) |

## Open questions

1. **A date-time without seconds.** An `effectiveFrom` of `setPrice` as `2026-01-01T00:00Z` is accepted, which RFC 3339 refuses (found by the gate of `docs/plans/catalog-discounts.md`). Accepted in this version, in the prices; is it refused in a later version? (TL, 2026-10-20) (carried from `price-list`: BA, 2026-10-20)

