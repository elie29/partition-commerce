# Move of `getProductPrice` from `product` to `price-list` — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-20

## Tasks

- [x] Task 1 — 2026-10-20 — The derived records follow the move
- [x] Task 2 — 2026-10-20 — The hand-written sections follow the move (by a person)
- [x] Task 3 — 2026-10-20 — The code of `getProductPrice` moves to `price-list`
- [x] Task 4 — 2026-10-20 — Prove it was a move
- [x] Task 5 — 2026-10-20 — Afterwards: C4 model, budget
- [x] Task 6 — 2026-10-20 — Close: gate, levels, README

## Goal

Carry out the move of `getProductPrice` from `product` to `price-list`, validated by name in the delta of `docs/contracts/catalog.openapi.json` 1.0.0 → 1.1.0 (Claire Martin, architect, 2026-10-20, proposal: option 1; `docs/specs/catalog/bounded-contexts.md`), as `docs/architecture/reshapes/2026-10-20-get-product-price-move.md` records it. The specs move first, then the code, in this plan. The commit before this plan's first task is `e6f4d22` (the delta, committed by the owner). The build is red since `260ac41`: `ProductController.java:[67,5] method does not override or implement a method from a supertype` (`./mvnw -q clean test`, 2026-10-20), the generated `ProductApi` no longer carrying `getProductPrice`.

## Task 1 — The derived records follow the move

- Scope: `docs/specs/catalog/bounded-contexts.md` (`Bounded contexts retained`, `Alternatives rejected`, `Gaps detected`, `Coverage`, `History`); `Source parts owned`, `Source version` and `Provenance` of `docs/specs/catalog/product/spec.md`, `price-list/spec.md`, `discount/spec.md`
- Verification: `grep -n "getProductPrice" docs/specs/catalog/*/spec.md | grep "\[GET"; grep -n "^| getProductPrice" docs/specs/catalog/bounded-contexts.md; grep -n "Source version" docs/specs/catalog/*/spec.md`
- Expected: one `Source parts owned` row for `getProductPrice`, in `price-list/spec.md`; its `Coverage` row reads `PriceList | price-list`; the three specs read `1.1.0`
- Decisions that bind it: 2026-10-20 · the move, option 1 (Claire Martin, architect)
- Done 2026-10-20 · uncommitted. Output: `price-list/spec.md:43:| getProductPrice | [GET /price-lists/{priceListId}/product-prices/{productId}]…`; `84:| getProductPrice | PriceList | price-list |`; `Source version: 1.1.0` in `product`, `price-list`, `discount`. As expected. Option 2 of 2026-10-19 left `Alternatives rejected` for `History` (overturned); the R4 sentence of `price-list` on `getProductPrice` of `product` dropped; the gap of the move marks the derived records done

## Task 2 — The hand-written sections follow the move (by a person)

- Scope: `docs/specs/catalog/product/spec.md` and `docs/specs/catalog/price-list/spec.md` (`Business objective`, `Scope`, `Business rules`, `Contracts with neighbours`, `Applicable area rules and invariants`); `docs/specs/catalog/area.md` (words one bounded context alone uses after the move). Carried by a person, through `partition-spec`; the assistant writes no hand-written line
- Verification: `grep -n "getProductPrice\|PriceListPrices" docs/specs/catalog/product/spec.md; grep -n "getProductPrice\|PriceListPrices\|PRICE_NOT_FOUND" docs/specs/catalog/price-list/spec.md`
- Expected: `product/spec.md` names neither `getProductPrice` nor `PriceListPrices`; `price-list/spec.md` holds the rules of `getProductPrice` (once BR-9 and BR-10 of `product`) with `PRICE_NOT_FOUND`, and no `PriceListPrices`
- Decisions that bind it: 2026-10-20 · the product check goes, no integration ([architect]); AR-3 answers a `priceListId` that is not a UUID in the path with a 404
- What to carry, read on 2026-10-20:
  - `product/spec.md`: BR-9 and BR-10 leave for `price-list`; `Business objective` and `Scope` ("read the price of a product in a price list", "`getProductPrice`"); `Contracts with neighbours`, the `Callers` line (1.0.0, `getProductPrice`) and the `price-list` line (`PriceListPrices`); `Applicable area rules and invariants`, the INV-DATA-001 line (the amount `getProductPrice` answers), AR-1 ("its four operations"), AR-2 ("the amount read from `price-list`"), AR-3 (`priceListId` in the query of `getProductPrice`)
  - `price-list/spec.md`: BR-9 and BR-10 of `product` arrive; the product check goes ([architect], 2026-10-20); `priceListId` is in the path (AR-3: 404); their numbers must not collide with its BR-9, BR-10; BR-1 ("by the day as `product`'s BR-9 reads it"); `Business objective`, `Scope` (`Out of scope`: "`getProductPrice`, `product`"); `Contracts with neighbours`, the `Callers` line (1.0.0, three operations) and the `product` line (`PriceListPrices`); `Applicable area rules and invariants`: INV-DATA-001, AR-1 ("three operations"), AR-3 (the path of `getProductPrice`, the `at` query)
  - `discount/spec.md`: `Out of scope` ("the reading of a product's price (`product`)")
  - `area.md`: Price, Day and Applies are words `price-list` alone uses after the move (`discount` defines none of them, `product` loses them with BR-9): each comes down to `price-list`'s `Language` and leaves the area file, or stays, the owner's call
- Done 2026-10-20 · uncommitted, the wording drafted by the assistant and approved as drafted by the architect. Output: `product/spec.md:87:| AR-3 of ../area.md | productId in the path of getProduct (TL, 2026-10-19; getProductPrice gone: architect, 2026-10-20) |`; `price-list/spec.md` lines 22 (scope), 46 (`Source parts owned`), 66 (BR-14), 67 (BR-15), 90 (`Callers`, 1.1.0), 96 (INV-DATA-001), 99 (AR-3); no `PriceListPrices`. Not quite as expected: `product/spec.md` still names `getProductPrice` once, in the approved attribution of its AR-3 line ("`getProductPrice` gone"), a trace, no rule nor contract on it. `product` loses its INV-DATA-001 line (architect); `Amount` of `area.md` kept as written, "a price" an ordinary word there (architect)

## Task 3 — The code of `getProductPrice` moves to `price-list`

- Scope: `src/main/java/com/commerce/pricelist/**` (a use case over `Prices`, `PriceListController.getProductPrice`, the `at` query check in `PriceListRequestCheck`, the `PRICE_NOT_FOUND` answer in `PriceListErrors`); out of `src/main/java/com/commerce/product/**`: `GetProductPriceUseCase`, `ProductPrices`, `ProductPriceView`, `PriceNotFound`, `outbound/PriceListPricesAdapter`, the `getProductPrice` branch of `ProductRequestCheck`, the `PriceNotFound` handler of `ProductErrors`; out of `pricelist/application`: `PriceListPrices`, `PriceListPrice`, `PriceListPricesService`; `src/test/java/com/commerce/product/GetProductPriceTest.java` → `src/test/java/com/commerce/pricelist/`, at the 1.1.0 path; the `product` → `price-list` contract of `src/test/java/com/commerce/ArchitectureTest.java`
- Verification: `./mvnw -q clean test`
- Expected: exit 0, no test failure; `grep -rn "pricelist" src/main/java/com/commerce/product` prints nothing
- Decisions that bind it: 2026-10-20 · the product check goes, no integration ([architect]); a `priceListId` or `productId` not a UUID: 404 `PRICE_NOT_FOUND`; only `at` is a 400 content check, before the identifiers (BR-15); `PriceListPrices`, its implementation and its `ArchitectureTest` exception go; no period with both paths ([architect]); waits for Task 2
- Done 2026-10-20 · uncommitted. Output: `exit=0`; surefire, 19 suites, none with an error or a failure; `com.commerce.pricelist.GetProductPriceTest` tests="8" errors="0" failures="0"; `com.commerce.ArchitectureTest` tests="4" errors="0" failures="0"; `grep -rn "pricelist" src/main/java/com/commerce/product` prints nothing. As expected. The ERROR lines of the log are the tests that make the store fail on purpose. `Price.appliesOn` cites Applies in `price-list/spec.md`; `PriceListController.toPrice` is shared by `setPrice` and `getProductPrice`

## Task 4 — Prove it was a move

- Scope: none written; the record's `## Proof that it was a move`
- Verification: `git diff --stat e6f4d22 -- src`
- Expected: only files of `src/main/java/com/commerce/product/`, `src/main/java/com/commerce/pricelist/`, their tests and `src/test/java/com/commerce/ArchitectureTest.java`; no other caller changed
- Decisions that bind it: none
- Done 2026-10-20 · uncommitted. Output: 19 files, 73 insertions, 433 deletions, all under `src/main/java/com/commerce/pricelist/`, `src/main/java/com/commerce/product/`, `src/test/java/com/commerce/pricelist/`, `src/test/java/com/commerce/product/` and `src/test/java/com/commerce/ArchitectureTest.java`; `git status` adds three untracked files, `pricelist/application/GetProductPriceUseCase.java`, `pricelist/application/PriceNotFound.java`, `src/test/java/com/commerce/pricelist/GetProductPriceTest.java`. As expected: no other caller changed

## Task 5 — Afterwards: C4 model, budget

- Scope: `docs/architecture/model.yml` (the relation `product -> price-list: PriceListPrices, in process` goes) and its views, through `partition-c4`; the record of the day under `docs/budget/`, through `partition-budget`
- Verification: `grep -n "product -> price-list" docs/architecture/model.yml; ls docs/budget/`
- Expected: no relation printed; a budget record dated 2026-10-20 present
- Decisions that bind it: 2026-10-20 · no integration after the move ([architect])
- Done 2026-10-20 · uncommitted. Output: `grep exit=1` (no relation); `2026-10-19.md 2026-10-20.md`. As expected. `c4-drawio.mjs --check`: "12 elements, 12 relations, 7 views, no error"; the views regenerated, seven pages. `budget.mjs --check`: exit 0, `product` 5 394 (17 %), `price-list` 6 457 (20 %), none over 32 000; the record of the day names nobody: who held it, the three observed signs and a decision are the people's

## Task 6 — Close: gate, levels, README

- Scope: as `partition-plan` says, _Close_
- Verification: the `partition-gate` verdict, on `e6f4d22` to the working tree
- Expected: SHIP or SHIP WITH CONDITIONS
- Decisions that bind it: none
- Gate 1, 2026-10-20, on `e6f4d22` to the working tree, untracked files read by hand (the collector's patch leaves them out): **SHIP WITH CONDITIONS**, confidence Medium. Evidence: `./mvnw -q clean test` exit 0, 19 suites, 173 tests, 0 failed, 0 errors, 0 skipped; `pricelist.GetProductPriceTest` 8/8 carries the eight scenarios of the deleted `product.GetProductPriceTest`; `ArchitectureTest` 4/4 now forbids any `product` → `price-list` import; `detect_test_tampering.sh`: one test class deleted, replaced, no assertion loosened, nothing skipped. Not verified: diff coverage (no coverage tool in `pom.xml`); a base-to-change comparison (the base `e6f4d22` does not compile, red since `260ac41`); callers outside the repository (none, the architect says). Findings: [Medium] `README.md:35` documents the removed `GET /products/{productId}/price?priceListId=…`; [Low] `getProductPrice` answers `productId` as the stored UUID, lowercase (`PriceListController.toPrice`), where `product` echoed the path as sent, as `setPrice` already does; the spec is silent on its case. Condition: the README realigned (`partition-readme`, closing step 5)
- Closing step 2–4, 2026-10-20, each statement read now where it lives: BR-14, BR-15 → `docs/specs/catalog/price-list/spec.md` lines 66, 67, in place; Price, Day, Applies → its `Language`, in place, gone from `docs/specs/catalog/area.md`; no integration between `product` and `price-list` → neither spec's `Contracts with neighbours` names the other, in place, held by `ArchitectureTest`; no period with both paths → a decision of this delivery, stays in this plan, the README says the old path is no longer served. `AR-` entries changed: none (three words left `area.md`, validated by the architect). `INV-` entries: none added nor changed; no candidate
- Closing step 5, 2026-10-20: `README.md` realigned through `partition-readme`: the line of `GET …/products/{productId}/price?priceListId=…` replaced, after `setPrice`, by `GET /commerce/v1/catalog/price-lists/{priceListId}/product-prices/{productId}?at=…`, its 404 and 400 as BR-14, BR-15 say, the break called out. C4 model: done in Task 5
- Gate 2, 2026-10-20, on gate 1's target plus `README.md`: **SHIP WITH CONDITIONS**, confidence Medium. The README line checked against `PriceListController`, `PriceListRequestCheck`, `GetProductPriceUseCase` and BR-14, BR-15: it matches; gate 1's condition met. No build input changed since gate 1's `./mvnw -q clean test` (exit 0, 173 tests). Not verified: an empty `at=` (no test reaches it; read as today, by Spring's binding of an empty value, `PriceListRequestCheck.isDateOrAbsent`) and the `productId` answered lowercase for an uppercase path (gate 1, Low); the probe `scratchpad/probe.sh` was not run, its run needing the owner's approval. Condition: the owner runs the probe, or accepts both unverified
- Expected for the probe, written before any run: `setPrice 200`; an empty `at=` with an uppercase `productId`: 200, its `productId` lowercase; the old path: 404
- Probe, run by the TL on the running app (port 18080), 2026-10-20. Output: `setPrice 200`; `{"productId":"abcdef01-2345-6789-abcd-ef0123456789","price":{"amountMinor":1999,"currency":"EUR"},"effectiveFrom":"2026-10-20T07:31:50.091Z"} 200`; old path `404`. As expected. The lowercase `productId` accepted (architect). Gate 2's condition met
- Closing step 3, 2026-10-20: the lowercase `productId` (architect) is a statement of `price-list` its spec does not hold (`setPrice` and `getProductPrice` alike). Drafted as BR-16, shown to the architect, not yet written. Once written, a third gate covers it before the plan closes
- Closing step 3, 2026-10-20: BR-16 approved as worded (architect) and written in `docs/specs/catalog/price-list/spec.md`. `AR-` entries touched: none. `INV-` entries touched: none; no candidate: BR-16 binds `price-list` alone, `product` and `discount` answering only the identifiers they create
- Gate 3, 2026-10-20, on gate 2's target plus the BR-16 line: **SHIP**, confidence Medium. Output: `grep -c "^| BR-16 "` → `1`; BR-16 held by `Identifiers.parse` on the way in (`SetPriceUseCase.java:60`, `GetProductPriceUseCase.java:32`) and `UUID.toString()` on the way out (`PriceListController.java:81`), both exercised by the TL's probe; no build input changed since gate 1's `./mvnw -q clean test` (exit 0, 173 tests). Not verified: diff coverage (no coverage tool); BR-16 by an automated test (the probe alone). The plan closes on gate 3, which covers every change of the plan
- README unaffected by BR-16: it writes down what both operations already answered
- Done 2026-10-20

## Decisions

- 2026-10-20 · `getProductPrice` moves from `product` to `price-list` (proposal: option 1) · since 1.1.0 its root aggregate is `price-lists` (C2, R3) · rejected: option 2, `product` keeps it, kept against C2/R3, no reason stated (Claire Martin, architect)
- 2026-10-20 · "The product check goes: an unknown product gets, as the contract says, `404` `PRICE_NOT_FOUND`, no price for it in that list. The integration after the move: none; `price-list` answers from its own data, `product` no longer calls `PriceListPrices`." · `price-list` does not read `product` (its BR-12) · rejected: `price-list` reading `product` through a contract ([architect])
- 2026-10-20 · BR-9 and BR-10 of `product` become BR-14 and BR-15 of `price-list`, the next free numbers; BR-14 without the product check; a `priceListId` or a `productId` that is not a UUID gets `404` `PRICE_NOT_FOUND`, the only code the contract gives the operation; BR-15 keeps only the `at` the schema refuses as a `400` content check; `product` keeps no rule on prices · the rules follow their operation · rejected: keeping the numbers, a 400 for a malformed `priceListId` ([architect])
- 2026-10-20 · `PriceListPrices` goes, with its implementation and its exception in `ArchitectureTest`; no period with both paths · no client calls the old path outside this repository · rejected: serving both paths for a while ([architect])
- 2026-10-20 · the objective, scope, `Contracts with neighbours` and area-rule lines of `product`, `price-list` and `discount` are rewritten to fit the move, as Task 2 lists them; `price-list`'s BR-1 cites BR-14 · they would be false after the move ([architect])
- 2026-10-20 · Price, Day and Applies leave `area.md` for `price-list`'s `Language` · only `price-list` uses them after the move ([architect])
- 2026-10-20 · the wording is drafted by the assistant through `partition-spec` and shown to the architect before it is written · the architect's instruction, in place of a person carrying the lines ([architect])
- 2026-10-20 · the drafted wording approved as drafted; `Amount` of `area.md` kept as written, "a price" being an ordinary word there; `product`'s INV-DATA-001 line removed · `product` stores and returns no amount after the move ([architect])
- 2026-10-20 · `getProductPrice` answers `productId` in lowercase, as stored, for an uppercase path · as `setPrice` already does · rejected: echoing the path as sent, as `product` did ([architect])
- 2026-10-20 · BR-16 of `price-list`, "A product identifier is answered as stored: lowercase.", approved as worded · the decision above, durable, for `setPrice` and `getProductPrice` ([architect])

## Blockers

<!-- What is blocked · what unblocks it · who owns it. Delete the line when it clears. -->

