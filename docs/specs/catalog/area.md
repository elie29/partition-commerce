# catalog

The language and the area rules shared by the bounded contexts of this area. A rule one bounded context owns stays in its spec; a statement that spans areas or layers goes to the invariant register.

## Bounded contexts

| Bounded context | Spec                  |
| --------------- | --------------------- |
| product         | `product/spec.md`     |
| price-list      | `price-list/spec.md`  |
| discount        | `discount/spec.md`    |

## Language

One meaning per word, no synonyms. A word used by one bounded context only stays in its spec.

| Term | Meaning in this area |
| ---- | -------------------- |
| Product | What the shop sells (BA, 2026-10-19). |
| Price list | The prices of products in one currency, for one channel (BA, 2026-10-19). |
| Price | An amount in `Money` and its `effectiveFrom`; its amount is "the amount" (BA, 2026-10-19). |
| Day | A calendar date in UTC (BA, 2026-10-19). |
| Currency | A three-letter ISO 4217 code; one meaning with `billing`'s (BA, architect, 2026-10-19). |
| Amount | A sum of money in one currency, stored in minor units; of a price, its amount; one meaning with `billing`'s (BA, 2026-10-19). |
| Applies | Of a price, on a day: its `effectiveFrom` is on or before the end of that day, in UTC (BA, 2026-10-19). |
| Blank | Empty or only white space; a non-breaking space (U+00A0, U+2007, U+202F) is white space; one meaning with `billing`'s (BA, 2026-10-19). |
| Character | A Unicode code point: an emoji is one character; one meaning with `billing`'s (BA, 2026-10-19). |
| Identifier | A UUID made by the bounded context that creates the resource (BA, 2026-10-19). |
| Minor units | The smallest unit of a currency, counted as a whole number; one meaning with `billing`'s (BA, 2026-10-19). |
| Refused | What a bounded context of `catalog` answers to a request one of its rules does not accept; nothing changes; one meaning with `billing`'s (BA, 2026-10-19). |

## Area rules

| # | Rule | Trigger | Expected effect | Binds | Source |
| --- | --- | --- | --- | --- | --- |
| AR-1 | Every catalog error answer carries the common error envelope. | An answer of the contract with a status of 400, 404, 409 or 500 | `detail`: one sentence naming the refusal, no value sent. `errors`: for a 400, one item per field at fault, `code` `VALIDATION_FAILED`; for a 409, one item, `code` `CONFLICT`; for a 404, one item with the contract's code (`PRODUCT_NOT_FOUND`, `PRICE_LIST_NOT_FOUND`, `PRICE_NOT_FOUND`, `DISCOUNT_NOT_FOUND`), no `field`. `field`: the JSON pointer of a body field, or the name of a header, query or path parameter. `message`: one sentence saying what is wrong with that field, without its value. `errorId`: a new UUID per error, logged once with the `X-Request-Id`, the path and the status. `path`: under `/commerce/v1/catalog`. Without `X-Request-Id`: 400, logged with the `errorId`, the path and the status. 405, 406 and 415 stay as the framework answers them. A 500: the envelope with its `detail` and its `errorId`, `errors` empty. A body that does not parse: 400, one item, no `field`, `code` `VALIDATION_FAILED`. A malformed `X-Request-Id`: 400, its value never logged. The headers are checked first; the first refusal stops the checks | product, price-list, discount | TL, 2026-10-19 (the codes are the contract's examples); 405, 406, 415: architect, 2026-10-19; validated as written: BA, TL, 2026-10-19; 500, body, malformed `X-Request-Id`: TL, 2026-10-19, validated as written: TL, architect, 2026-10-19; headers first: TL, 2026-10-19; binds `discount`: architect, 2026-10-20; `DISCOUNT_NOT_FOUND`: architect, 2026-10-20 |
| AR-2 | A catalog log holds identifiers, the path, the status and the `errorId` only; a 500 adds the class and the stack frames of its exception. | Any log of `product`, `price-list` or `discount` | The `X-Request-Id` is logged with them when it is well formed. Never a label, a reference value, an amount, the `Authorization` header, a refused `X-Request-Id`, or the message of an exception | product, price-list, discount | TL, 2026-10-19; validated as written: TL, architect, 2026-10-19; the `X-Request-Id`: TL, 2026-10-19; the 500: TL, 2026-10-19; binds `discount`: architect, 2026-10-20; the trigger names `discount`: TL, architect, 2026-10-20 |
| AR-3 | An identifier is a UUID in its 8-4-4-4-12 hexadecimal form. | An identifier given in a path or a query | Any other form is not a UUID: a 404 in the path, a 400 in the query (`backend-api-contract`) | product, price-list, discount | TL, 2026-10-19; validated as written: TL, architect, 2026-10-19; binds `discount`: architect, 2026-10-20 |
| AR-4 | A body property the schema does not name, and the `Authorization` header, are ignored. | A request carrying them | Accepted; neither changes the answer | product, price-list, discount | TL, 2026-10-19; validated as written: TL, architect, 2026-10-19; binds `discount`: architect, 2026-10-20 |
| AR-5 | A time is kept to the millisecond, in UTC. | `createdAt`, `effectiveFrom` | Kept and returned to the millisecond, in UTC | product, price-list | TL, 2026-10-19; validated as written: TL, architect, 2026-10-19 |
