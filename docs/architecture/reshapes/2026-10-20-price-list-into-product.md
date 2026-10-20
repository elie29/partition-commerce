# Reshape — merge: `product` absorbs `price-list`

> **Decided on:** 2026-10-20, by Claire Martin (architect), Paul Durand (product owner), Léa Petit (business analyst), from workshop: `docs/sources/workshop-price-list-into-product.md`; validated by name to be carried out on 2026-10-20 (architect)
>
> **From:** `product` (listProducts, createProduct, getProduct) and `price-list` (createPriceList, getPriceList, setPrice, getProductPrice), both in area `catalog` **To:** `product` (the seven), area `catalog`
>
> **Raised by:** the workshop of 2026-10-20; no budget record raised it (`docs/budget/2026-10-20.md` proposes no workshop)

## Why here

"A price is never managed apart from its product; every change of one is a change of the other" (workshop). No contract was left between the two since the move of `getProductPrice` (`docs/architecture/reshapes/2026-10-20-get-product-price-move.md`): neither calls the other, no foreign key joins their tables. The contract 1.1.0 is unchanged: `products` and `price-lists` stay two root aggregates, kept together against C2, C3 and C4 (finding 1).

| Term | Meaning on this side (`product`) | Meaning on the other side (`price-list`) |
| ---- | -------------------------------- | ---------------------------------------- |
| Product | an entity: a reference, a label, a status (`product/spec.md`, Language) | an identifier given, not checked: "A price is set for the product identifier given, whether `product` holds it or not" (`price-list` BR-12) |
| Not found (a product) | 404 `PRODUCT_NOT_FOUND` (`product` BR-8) | an unknown product: 404 `PRICE_NOT_FOUND` (`price-list` BR-14) |
| Retired | the final state of a product (`product` BR-9) | not known: "`price-list` and `discount` are not reached" (`product` BR-9) |

## Challenge

| Finding | Improvement proposed | Outcome |
| --- | --- | --- |
| 1. **R1 still holds for both, and the merge puts two root aggregates in one bounded context (C2).** The result: "a price is never managed apart from its product". The contract 1.1.0, unchanged, serves the prices under `/price-lists/{priceListId}/…`; `price-list` was retained on C2, C3, C4 (`docs/specs/catalog/bounded-contexts.md`, _Bounded contexts retained_); R6 does not call this merge. Option 3 was rejected on 2026-10-19: "one bounded context would hold two root aggregates" (_Alternatives rejected_). R4 rests on C6: `Product` and `Price` share `productId` only, a sharing the file calls weak for `Money` ("weak sharing, it does not prevail"). | Either (a) R4 prevails over C2, C3, C4: a line in `Decisions not to split`; or (b) the merge kept against C2 in `Kept against the criteria`, with the point, the criterion, Claire Martin, 2026-10-20 and the workshop's reason. Recommended: (b). | (b): kept against C2, C3 and C4 in `Kept against the criteria`, with the workshop's reason; and, as every merge, `price-list` goes to `Decisions not to split`, "merge of 2026-10-20 into `product`". The result taken as it is (Claire Martin, architect, 2026-10-20) |
| 2. **"Every change of one is a change of the other" does not match the specs nor the history.** `product` BR-9: "`price-list` and `discount` are not reached"; `price-list` BR-12, BR-14: "`price-list` does not read `product`". The retirement (`90f98b5`, `5709dae`) changed `product` alone; `a7ba35b` the spec of `price-list` alone; 3 of the 7 catalog commits touched both. | The result's text stays as validated; the merge stays a move: BR-9, BR-12, BR-14 carried over unchanged. A price that checks its product (unknown, retired) is a new behaviour, through `partition-spec`, after. | Taken as proposed (architect, 2026-10-20) |
| 3. **"Product" and "not found" would carry two meanings in one bounded context, and the rule numbers collide.** See _Why here_; both specs number from BR-1. | Léa Petit states the two meanings in the merged Language (the contract's codes unchanged) and gives the rules unique numbers in the merged spec. | Taken as proposed (Léa Petit, BA, 2026-10-20) |
| 4. **`bounded-contexts.md` keeps the decisions this merge overturns.** The _Bounded contexts retained_ line of `price-list`, the _Alternatives rejected_ line of option 3 ("one bounded context would hold two root aggregates"), `Coverage` of the four units of `price-list`, the `Decided` line. | The overturned lines to `History`, dated, word for word; `Coverage` → `product`; the `Decided` line names this workshop. | Taken as proposed (architect, 2026-10-20) |

## Integration

| Crosses the boundary | Direction | Form (event, call, file) | Contract |
| -------------------- | --------- | ------------------------ | -------- |
| Nothing | — | — | none between `product` and `price-list` since the move of `getProductPrice` (`PriceListPrices` removed, 2026-10-20); `ArchitectureTest.java:118`: "The catalog's contexts call none another" |
| `listProducts`, `createProduct`, `getProduct` (tag `Product`); `createPriceList`, `getPriceList`, `setPrice`, `getProductPrice` (tag `PriceList`) | caller → `product` | HTTP | `docs/contracts/catalog.openapi.json` 1.1.0: unchanged |

Other consumers: none in the code — `grep -rn "com.commerce.pricelist" src` finds nothing outside `src/{main,test}/java/com/commerce/pricelist`. `discount` shares the `Money` schema of the contract, not code.

## Rules

| Rule | Was in | Goes to |
| ---- | ------ | ------- |
| BR-1 to BR-16 (no BR-11) | `price-list` | `product`, renumbered unique in the merged spec, carried by Léa Petit (BA); BR-9 of `product`, BR-12 and BR-14 of `price-list` unchanged (finding 2) |
| BR-1 to BR-9 | `product` | `product` |
| AR-5 (a time to the millisecond, in UTC), binds `product` and `price-list` only | `docs/specs/catalog/area.md` | `product`'s spec; leaves the area file, by a person |
| AR-1 to AR-4, bind `discount` too | `docs/specs/catalog/area.md` | stay; `price-list` leaves their `Binds`, by a person |
| Area words Product, Price list | `docs/specs/catalog/area.md` | stay: `discount` uses them (its BR-2, BR-6) |
| INV-DATA-001 | `price-list` (_Applicable area rules and invariants_) | `product` |
| "A currency is a known ISO 4217 code" (`price-list` BR-4, `discount` BR-4) | `price-list` | `product`; the gap of `bounded-contexts.md` stays open with `discount` |
| `Persistence` | both specs: no schema documents declared | unchanged |

## Questions left open by the workshop

- The reason of the form of `product` in `docs/partition.yml` once it holds the price lists: for the architect (`docs/plans/price-list-into-product.md`, Task 2).

## Preconditions

- [x] Every caller reaches it through an interface, or a merged half through its contract: both halves are reached by HTTP only, through `docs/contracts/catalog.openapi.json` 1.1.0; no in-process caller (`ArchitectureTest.java:118`), 2026-10-20
- [x] One adapter implements that interface: not a fission, a move or a promotion
- [x] The contract is written and frozen: not a fission, a move or a promotion
- [x] For a fission, the new bounded context has its durable spec and its own language: not a fission
- [x] Each rule both sides need has one home: the owner's spec, the area file or the register: AR-5 became BR-26 of `product` and left the area file (its note, `area.md`); the rules of `price-list` are BR-10 to BR-25 of `product`; Blank and Character came down into `product`'s Language — carried over by Léa Petit (BA), 2026-10-20
- [x] For a merge, the person who carries the hand-written sections over: Léa Petit (business analyst), 2026-10-20
- [x] For a removal, nothing calls it any more: not a removal

## Proof that it was a move

Carried out by `docs/plans/price-list-into-product.md` (Tasks 3 to 5).

- Verification: `git status --short -- . ':!src/main/java/com/commerce/product' ':!src/test/java/com/commerce/product' ':!src/main/java/com/commerce/pricelist' ':!src/test/java/com/commerce/pricelist'` against `271e685`, the commit before the merge; then `git diff --quiet 271e685 --` the neighbours, the contracts and the product's own code
- Expected: outside the merged code, only the wiring (`pom.xml`, `ArchitectureTest.java`, `docs/partition.yml`) and the documents of the reshape; unchanged: `discount`, `invoice` and `payment`, code and tests, `catalog.openapi.json`, `src/main/resources`, and inside `product` its own code but `JsonBody` (D2)
- Real output (2026-10-20): `M docs/partition.yml`, `M docs/specs/catalog/bounded-contexts.md`, `M docs/specs/catalog/price-list/spec.md`, `M docs/specs/catalog/product/spec.md`, `M pom.xml`, `M src/test/java/com/commerce/ArchitectureTest.java`, `??` the record, the plan, the workshop result; `git diff --quiet`: `exit=0` for the neighbours and the contracts, `exit=0` for the product's own 22 classes and 6 test files. No caller changed. `./mvnw clean verify`: `Tests run: 183, Failures: 0`, as at `271e685`

## After

- [x] C4 model updated and regenerated: the component `price-list` and its relation to `h2` gone; `c4.drawio` seven pages, 2026-10-20
- [x] `docs/partition.yml` kept `price-list`, with its area only, while `docs/specs/catalog/price-list/` existed; the folder deleted by a person and the entry taken out, 2026-10-20 (`docs/plans/price-list-into-product-after-carry-over.md`)
- [x] Budget measured again: `docs/budget/2026-10-20.md` — `product` 6 090 (19 %), `price-list` 6 019 (19 %) while its spec exists; the merge was raised by a workshop, not a gauge
