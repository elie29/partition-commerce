# Products and price lists of the catalog — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-19

## Tasks

- [x] Task 1 — 2026-10-19 — The owners answer the questions this feature depends on
- [x] Task 2 — 2026-10-19 — The specs of `product` and `price-list` take the business rules and the answers
- [x] Task 3 — 2026-10-19 — Declare the forms and layer maps of `product` (thin) and `price-list` (rich)
- [x] Task 4 — 2026-10-19 — Challenge the specs (partition-attack), then design the products and the price lists
- [x] Task 5 — 2026-10-19 — Products: `createProduct`, `getProduct`, `listProducts`
- [x] Task 6 — 2026-10-19 — Price lists: `createPriceList`, `getPriceList`
- [x] Task 7 — 2026-10-19 — Prices: `setPrice`
- [x] Task 8 — 2026-10-19 — The price of a product: `getProductPrice`, through the contract of `price-list`
- [x] Task 9 — 2026-10-19 — The two `Money.java` of `billing` name INV-DATA-001 instead of AR-2
- [x] Task 10 — 2026-10-19 — Close: gate, levels, README, C4
- [x] Task 11 — 2026-10-19 — Gate condition: two first prices at the same moment both answer 200 (BR-1, P-5)
- [x] Task 12 — 2026-10-19 — Gate condition: a 500 of each context, tested, and what it logs as the TL rules
- [x] Task 13 — 2026-10-19 — Gate finding 4: a stored currency code is read as kept (BR-4, incoming only)

## Goal

Deliver the seven operations of `docs/contracts/catalog.openapi.json` 1.0.0 (5016f59) in scope `tag:Product; tag:PriceList`, in the bounded contexts `product` (`listProducts`, `createProduct`, `getProduct`, `getProductPrice`) and `price-list` (`createPriceList`, `getPriceList`, `setPrice`) of the area `catalog`, as `docs/specs/catalog/bounded-contexts.md` decided. The durable specs `docs/specs/catalog/product/spec.md` and `docs/specs/catalog/price-list/spec.md` come first; the code implements them, under INV-DATA-001 and the project's rules.

## Task 1 — The owners answer the questions this feature depends on

- Scope: none of the repository; answers from the BA, the PO, the architect and the TL in the conversation
- Verification: run after Task 2 — `grep -n -A12 "## Open questions" docs/specs/catalog/product/spec.md docs/specs/catalog/price-list/spec.md`
- Expected: none of the questions under `## Blockers` is left open in either spec
- Decisions that bind it: none
- Answers received 2026-10-19 (BA, PO, architect, TL), one line each in `## Decisions`. Two narrower questions remain, in the specs' `Open questions` and under `## Blockers`
- Done 2026-10-19. Output of the verification: none of the four questions of the first `## Blockers` is left open in either spec; one is left, the conflict between the architect's "one 404" and the TL's two codes, carried by Task 4, and the attack's findings of Task 4 are under the same heading

## Task 2 — The specs of `product` and `price-list` take the business rules and the answers

- Scope: `docs/specs/catalog/product/spec.md`, `docs/specs/catalog/price-list/spec.md`, via `partition-spec`
- Verification: `grep -n "TO FILL\|BR-" docs/specs/catalog/product/spec.md docs/specs/catalog/price-list/spec.md`
- Expected: no "TO FILL" banner in either spec; `product` holds one BR row (the reference unique), `price-list` two (one price per product, a price never negative), each with its trigger, its effect and its source
- Decisions that bind it: 2026-10-19 · the three rules of the BA, and every answer of 2026-10-19 in `## Decisions`
- In progress 2026-10-19: both specs written from the answers (Language, Business rules, Contracts with neighbours, Open questions). Output: "TO FILL" `0` in both; BR rows `10` in `product`, `11` in `price-list` — not as expected (1 and 2): the expected result was written before the answers, which brought more rules than the three of the BA. Left: `docs/specs/catalog/area.md` (words and AR-1, to be shown and validated as written), the shared words pointed to from both specs, the two open questions
- Done 2026-10-19. `docs/specs/catalog/area.md` written as shown and validated (BA, TL): five words, AR-1; both specs point to it (Language, Applicable area rules); `price-list` BR-12 (the architect: a price is set for the identifier given); `product` BR-8 carries `PRODUCT_NOT_FOUND` (TL). Verification above: no banner, BR rows 10 and 11, not as expected for the reason given

## Task 3 — Declare the forms and layer maps of `product` (thin) and `price-list` (rich)

- Scope: `docs/partition.yml`, via `partition-hexagon`
- Verification: `grep -n -A3 "form:" docs/partition.yml`
- Expected: `product` declared `form: thin` and `price-list` `form: rich`, each with the architect's reason and a layer map with its `allowed` list
- Decisions that bind it: 2026-10-19 · the forms of the architect; a form is chosen from the `Business rules` of the spec, so this task follows Task 2; `product` stays thin, BR-1 held by a unique constraint (architect)
- Done 2026-10-19. Output: line 85 `form: thin` with its reason, line 100 `form: rich` with its reason; both with a layer map and `allowed`, the layer names those of the project's other hexagons. As expected

## Task 4 — Challenge the specs (partition-attack), then design the products and the price lists

- Scope: `docs/specs/catalog/product/spec.md`, `docs/specs/catalog/price-list/spec.md` (findings only), `docs/designs/catalog-products-and-price-lists.md`
- Verification: `grep -n "^## " docs/designs/catalog-products-and-price-lists.md`
- Expected: the design exists; every finding of the attack is settled by its owner or carried as a blocker
- In progress 2026-10-19: attack run by a reader that did not write the specs (a separate agent): 24 findings. Dropped as already settled: `getProductPrice`'s code for an unknown product (TL), `Location` a path (TL). Folded: the check order into the one-404 question; B2B reading and missing words into their kin. Carried to the design, not a spec gap: two first-time `setPrice` at once must both end in 200, the last replacing. Written: 12 entries in `product`'s `Open questions`, 9 in `price-list`'s; one, the billing edit, under `## Blockers` only
- 2026-10-19: answers received. Written into the specs: `product` BR-1 (`field` `/reference`), BR-2 (a rule), BR-5 (`createdAt` wherever `Product` is), BR-7 (no bounds, empty filter), BR-8 (`getProduct` only), BR-9 (by the day, one 404 `PRICE_NOT_FOUND`), BR-10 (content first), INV-DATA-001 row; `price-list` BR-1 (by the day), BR-2 (the amount), BR-4 (the whole rule, content), BR-8 (no offset refused), BR-9 and BR-12 (`PRICE_LIST_NOT_FOUND`), BR-10 (body, list, product, BR-3), `PriceListPrices` row. Waiting: the entries that bind both, for `../area.md`, shown 2026-10-19
- 2026-10-19: `../area.md` written as validated (TL, architect): seven words, AR-1 (500, body, malformed `X-Request-Id`, headers first), AR-2 (with the `X-Request-Id` when well formed), AR-3 to AR-5; `price-list` BR-11 removed for AR-2, BR-13 (`Accept-Language`); `product` BR-7 (the shift accepted); `.apm/instructions/backend-api-contract.instructions.md` amended as proposed (`apm install` to run). Design written: `docs/designs/catalog-products-and-price-lists.md`. Verification: its eight `##` sections print; one finding left open (one item or one per field), carried under `## Blockers`. Left: the rulings on P-4 and P-5, the design's two questions
- Done 2026-10-19. P-4 and P-5 ruled and written in `docs/architecture/patterns-grid.md` and the design; the one-item question and the `page` range settled and written (`price-list` BR-10, `product` BR-3, BR-7); both specs' `Open questions` read "None"
- Decisions that bind it: 2026-10-19 · `price-list` does not read `product`: a price is set for the identifier given (architect); `PriceListPrices` the only contract between `product` and `price-list` (architect); the error answers (TL, catalog AR-1 once validated); the order of refusals; 405, 406, 415 as the framework answers; base path `/commerce/v1/catalog`; no authentication

## Task 5 — Products: `createProduct`, `getProduct`, `listProducts`

- Scope: `src/main/java/com/commerce/product/**`, `src/test/java/com/commerce/product/**`, a new Flyway migration
- Verification: `./mvnw -q test`
- Expected: `CreateProductTest`, `GetProductTest`, `ListProductsTest` pass with the cases of the design's _Acceptance_ for them; the 80 earlier tests still pass; `ArchitectureTest` passes on `product`'s layer map
- Decisions that bind it: 2026-10-19 · `product` thin; the code under `src/main/java/com/commerce/product/**`; P-4: its own `ProductErrors`; AR-1 to AR-5; one item per field at fault within the first group refused (architect); a `page` beyond the integer range: 400 (TL, PO); base path `/commerce/v1/catalog`, billing unchanged (TL)
- Done 2026-10-19. Red first: the 25 new tests run before any code, 22 failures and 3 errors. Then `pom.xml` (execution `product`), `V7__create_catalog.sql` (the three tables), `com.commerce.ClockConfiguration`, `product`'s application, inbound and outbound. Output: `./mvnw -q test` exit 0; surefire 105 tests, 0 failures, 0 errors (`CreateProductTest` 14, `GetProductTest` 4, `ListProductsTest` 7, the 80 earlier, `ArchitectureTest` 4 included). As expected
- Reworked 2026-10-19, found during Task 6: the run above compiled against inline models left in `target/` by a first, unfiltered generation; `modelsToGenerate` drops the inline models (bodies, page, envelope), so a clean build would not have compiled. `modelsToGenerate` removed from both catalog executions. Verified again: `./mvnw -q clean test` exit 0, 12 classes, 105 tests, 0 failures, 0 errors
- Reworked again 2026-10-19, with Task 6: a value of the wrong JSON type failed at binding, so its field and the other faults of the content were lost (AR-1). `ProductBodyCheck` becomes a `RequestBodyAdvice` judging the JSON tree before binding, with its `JsonBody`; one test added (`{"reference":123,"label":" "}`: two items, and a non-integer dimension). The base path moves to a type-level `@RequestMapping` on the controller. Verified with Task 6's run below

## Task 6 — Price lists: `createPriceList`, `getPriceList`

- Scope: `src/main/java/com/commerce/pricelist/**`, `src/test/java/com/commerce/pricelist/**`, a new Flyway migration
- Verification: `./mvnw -q test`
- Expected: `CreatePriceListTest`, `GetPriceListTest` (through the API) and `PriceListTest` (the domain, no Spring) pass with the cases of the design's _Acceptance_ for them; the 105 earlier tests still pass; `ArchitectureTest` passes on `price-list`'s layer map, its domain importing nothing outside it
- Decisions that bind it: 2026-10-19 · `price-list` rich; the code under `src/main/java/com/commerce/pricelist/**`; P-4: its own `PriceListErrors`; the checks as `product`'s (Task 5 decision); BR-4 the whole rule
- Done 2026-10-19. Red first: `./mvnw -q test-compile` exit 1, `package com.commerce.pricelist.domain does not exist` (12 lines). Then the generator execution `price-list`, the domain (`Currency`, `Money`, `Channel`, `PriceList`, `Price`, `InvalidPriceList`), the application (`CreatePriceListUseCase`, `GetPriceListUseCase`, `PriceListView`, `PriceLists`, `Identifiers`, `Fault`, `PriceListNotFound`), inbound (`PriceListController`, `PriceListRequestCheck`, `PriceListBodyCheck`, `JsonBody`, `PriceListErrors`, `PriceListWebConfiguration`), outbound `JdbcPriceLists`; the tables were created by V7 in Task 5, no new migration. A first run: 404 on every `/price-lists` path (two `addPathPrefix` with one prefix: the second replaced the first), fixed by a type-level `@RequestMapping` per controller. Output: `./mvnw -q clean test` exit 0; 15 classes, 126 tests, 0 failures, 0 errors (`CreatePriceListTest` 11, `GetPriceListTest` 3, `PriceListTest` 6, `CreateProductTest` 15, `ArchitectureTest` 4 included). As expected

## Task 7 — Prices: `setPrice`

- Scope: `src/main/java/com/commerce/pricelist/**`, `src/test/java/com/commerce/pricelist/**`; no new migration: V7 created `price` in Task 5
- Verification: `./mvnw -q clean test`
- Expected: `SetPriceTest` passes with the design's _Acceptance_ cases for it; the 126 earlier tests still pass
- Decisions that bind it: 2026-10-19 · one price per product, a price never negative (BA); `price-list` rich; P-5: one `MERGE`, the last to commit replaces; BR-10: body, then list, then product, then BR-3, one item per field at fault in the first group (architect); BR-12: the product is not read; `effectiveFrom` absent is now, UTC, to the millisecond (AR-5)
- Done 2026-10-19. Red first: `SetPriceTest` 11 tests, 11 failures. Then `Prices`, `PriceView`, `PriceRefused`, `SetPriceUseCase` (application), `JdbcPrices` (one `MERGE … KEY`), `setPrice`'s body in `PriceListBodyCheck`, `PriceListController.setPrice`, the BR-3 answer in `PriceListErrors`. Output: `./mvnw -q clean test` exit 0; 16 classes, 137 tests, 0 failures, 0 errors (`SetPriceTest` 11). As expected

## Task 8 — The price of a product: `getProductPrice`, through the contract of `price-list`

- Scope: `src/main/java/com/commerce/product/**`, `src/test/java/com/commerce/product/**`, the contract's side in `src/main/java/com/commerce/pricelist/application/**`, `src/test/java/com/commerce/ArchitectureTest.java` (the contract)
- Verification: `./mvnw -q clean test`
- Expected: `GetProductPriceTest` passes with the design's _Acceptance_ cases for it; `ArchitectureTest` passes with the contract `product/product` → `price-list/price-list` from `outbound`; the 137 earlier tests still pass
- Decisions that bind it: 2026-10-19 · `getProductPrice` belongs to `product` (architect, option 1); it reads `price-list` through `PriceListPrices` only, from its outbound adapter; one 404 `PRICE_NOT_FOUND`, one `detail`, for no product, no list, no price that applies (architect); a price applies by the day, UTC, `at` absent is today (BA); content first: `priceListId`, `at` (TL)
- Done 2026-10-19. Red first: `GetProductPriceTest` 8 tests, 6 failures (the query and header checks already built in Task 5 passed). Then `PriceListPrices`, `PriceListPrice`, `PriceListPricesService` (`price-list` application), `ProductPrices`, `ProductPriceView`, `PriceNotFound`, `GetProductPriceUseCase` (`product` application), `PriceListPricesAdapter` (`product` outbound), `getProductPrice` in the controller and `PRICE_NOT_FOUND` in `ProductErrors`, the contract in `ArchitectureTest`. Output: `./mvnw -q clean test` exit 0; 17 classes, 145 tests, 0 failures, 0 errors (`GetProductPriceTest` 8, `ArchitectureTest` 4). As expected

## Task 9 — The two `Money.java` of `billing` name INV-DATA-001 instead of AR-2

- Scope: `src/main/java/com/commerce/payment/domain/Money.java`, `src/main/java/com/commerce/refund/domain/Money.java` (comment and error message); not the migrations V4 and V6
- Verification: `grep -rn "AR-2" src/main/java; ./mvnw -q test`
- Expected: no `AR-2` left under `src/main/java`; the tests pass
- Decisions that bind it: 2026-10-19 · their comments and error messages name INV-DATA-001, with the catalog work; the two migrations stay unedited (TL); kept in this plan, gated with this work (architect, TL)
- Done 2026-10-19. Output: `grep -rn "AR-2" src/main/java` prints nothing (exit 1); `./mvnw -q test` exit 0, surefire 80 tests, 0 failures, 0 errors, across 9 classes. As expected

## Task 10 — Close: gate, levels, README, C4

- Scope: as `partition-plan` _Close_ says
- Verification: the `partition-gate` verdict
- Expected: SHIP or SHIP WITH CONDITIONS on every change of the plan
- Decisions that bind it: none
- Gate, 2026-10-19, run by a reviewer that did not write the code (a separate agent), on `0b23b98` (the commit before the plan's first task; the owner committed the decomposition, the register and the AR-2 removal there) to the working tree, untracked files included: **SHIP WITH CONDITIONS**, confidence medium. Evidence: `collect_change_context.sh` and `detect_test_tampering.sh` (no tampering); `./mvnw -q clean … jacoco … test … report` exit 0, 17 classes, 145 tests, 0 failures, 0 errors, 0 skipped; new catalog code 579 of 606 executable lines covered (JaCoCo, counted from `jacoco.csv`, `diff_coverage.py` not run); `diff` of the API rule's source and deployed copy differ at line 10. Findings: (1) Medium, plausible: two first `setPrice` at once may give one 500 under H2's `MERGE … KEY` (`JdbcPrices.java:30`), the 5-round test too weak, the probe blocked → Task 11; (2) Medium: a catalog 500 logs no cause (`ProductErrors.java:72-74`, `PriceListErrors.java:66-68`), AR-2 allows nothing more, untested → Task 12, after the TL; (3) Low: `.claude/rules/backend-api-contract.md` still holds the old wording → Blockers (TL, `apm install`); (4) Low: stored currency codes re-checked on read, a JDK dropping a code makes those lists answer 500 (`JdbcPriceLists.java:39`, `JdbcPrices.java:49`) → `price-list`'s `Open questions`; (5) Low: README not realigned → this task's step 5. Not verified: red-first at the base, contract consumers outside the repo, load, a database other than H2, the generated code
- Corrected 2026-10-19: `Resume at` named `5016f59` as the base; the commit before the plan's first task is `0b23b98`
- Close, 2026-10-19. Step 1: gate above; condition (1) met by Task 11; (2) Task 12, waiting for the TL; (3), (4) under `## Blockers`. Step 2–4, levels: business rules, words and contracts in `docs/specs/catalog/product/spec.md`, `docs/specs/catalog/price-list/spec.md` (written during Tasks 1–4); AR-1 to AR-5 and seven words added to `docs/specs/catalog/area.md` (validated, TL, architect; the first five words and AR-1 by the BA and the TL); P-4, P-5 in `docs/architecture/patterns-grid.md`; the `BIGINT` and the API rule's wording in `.apm/instructions/` (TL). Invariant register: no entry added or changed by this plan (INV-DATA-001 predates it, `0b23b98`); no candidate: nothing of this plan spans two areas. Left at no level: how the catalog code checks its requests (generator options, interceptor, body advice, base path per controller), found only in the design and here: proposed for a rule file scoped to the catalog code, waiting for the TL (Blockers). Step 5: README realigned (`partition-readme`: the Build line, the catalog API, the envelope, `X-Request-Id`, V7); C4: `product` and `price-list` added to `docs/architecture/model.yml` with `product -> h2`, `price-list -> h2`, `product -> price-list: PriceListPrices`; `--check`: 11 elements, 12 relations, 7 views, no error; `c4.drawio` regenerated. No caller of the catalog drawn: no source names it (Blockers). Step 6: these writes need a second gate run, after Task 12
- Close, continued, 2026-10-19. Levels of what Tasks 12 and 13 established: AR-2 amended in `docs/specs/catalog/area.md` (validated, TL, architect); BR-4 amended in `docs/specs/catalog/price-list/spec.md` (BA, architect); how the catalog checks its requests in `.apm/instructions/backend-api-contract-catalog.instructions.md`, deployed (TL). Area entries added by this plan: AR-1 to AR-5 and twelve words; changed: AR-1 (500, body, malformed `X-Request-Id`, headers first), AR-2 (the 500). Invariant register: none added or changed; no candidate. README: realigned in step 5; unaffected by Tasks 12 and 13 (no command, option, key or operation changed). C4: unaffected since step 5.
- Gate, second run, 2026-10-19, by the same reviewer that did not write the code, on `0b23b98` to the working tree, untracked files included, every change of the plan and of this close covered: **SHIP**, confidence medium. Evidence: `collect_change_context.sh` and `detect_test_tampering.sh` (no tampering); `./mvnw -q clean … jacoco … test … report` exit 0, 17 classes, 150 tests, 0 failures, 0 errors, 0 skipped; new catalog code 609 of 636 executable lines covered (JaCoCo, counted by hand), both 500 handlers fully covered; each rule's source and deployed copy differ only in front matter. Findings 2 to 5 of the first run confirmed resolved. Left, Low, no condition: (1) the race of two first prices not proven absent: 1000 concurrent calls passed, the H2 probe `…/scratchpad/gate3/probe/MergeRace.java` was denied; (2) new: `framesOf` loops forever on a cause chain that loops back (A caused by B caused by A), `ProductErrors.java:99`, `PriceListErrors.java:93`: not fixed here, a change after this verdict would fall outside it; proposed to the owner as a follow-up. Not verified: the H2 probe, `diff_coverage.py`, red-first at the base, `c4.drawio` opened, consumers outside the repo, load, a database other than H2, 405/406/415 and failures that are not a `RuntimeException`

## Task 11 — Gate condition: two first prices at the same moment both answer 200 (BR-1, P-5)

- Scope: `src/test/java/com/commerce/pricelist/SetPriceTest.java`; `src/main/java/com/commerce/pricelist/outbound/JdbcPrices.java` if the test fails
- Verification: `./mvnw -q clean test`
- Expected: a test of 500 rounds of two concurrent first `SetPriceUseCase.set` passes, every call succeeding and one price kept per round; the 145 earlier tests still pass
- Decisions that bind it: 2026-10-19 · P-5: the last to commit replaces, two first prices at once both 200 (architect)
- Done 2026-10-19. Test added: `SetPriceTest.of_two_first_prices_set_at_the_same_moment_none_fails_over_many_rounds`, 500 rounds of two concurrent first `SetPriceUseCase.set` on H2 2.4. Output: `SetPriceTest` 12 tests, 0 failures, 0 errors; then `./mvnw -q clean test` exit 0, 17 classes, 146 tests, 0 failures. As expected: the gate's finding 1 did not reproduce in 500 rounds, so `JdbcPrices` is unchanged (no retry added without a failure to justify it). Not proven: a race that 500 rounds on one machine miss

## Task 12 — Gate condition: a 500 of each context, tested, and what it logs as the TL rules

- Scope: `src/test/java/com/commerce/product/**`, `src/test/java/com/commerce/pricelist/**`; `ProductErrors`, `PriceListErrors` if the TL's ruling moves AR-2
- Verification: `./mvnw -q clean test`
- Expected: `GetProductTest` and `GetPriceListTest` each gain a test that forces a 500 (the store failing with a message): the answer is 500 with the envelope, `errors` empty, an `errorId`; the log holds that `errorId`, the path, the status, the exception's class and its frames, and not its message; the 148 earlier tests still pass
- Decisions that bind it: AR-1 (a 500: the envelope, `detail`, `errorId`, `errors` empty); AR-2 as amended 2026-10-19 (a 500 adds the class and the stack frames of its exception, never its message; TL, validated by the TL and the architect)
- Done 2026-10-19. AR-2 written first. Red: `GetProductTest` 5 tests, 1 failure; `GetPriceListTest` 5 tests, 1 failure (the log held no class nor frame). Then `ProductErrors` and `PriceListErrors` log a 500 at ERROR with the class and the frames of the exception and its causes, no message. Output: `./mvnw -q clean test` exit 0; 17 classes, 150 tests, 0 failures, 0 errors. As expected

## Task 13 — Gate finding 4: a stored currency code is read as kept (BR-4, incoming only)

- Scope: `src/main/java/com/commerce/pricelist/domain/Currency.java`, `src/main/java/com/commerce/pricelist/outbound/JdbcPriceLists.java`, `src/main/java/com/commerce/pricelist/outbound/JdbcPrices.java`, `src/test/java/com/commerce/pricelist/PriceListTest.java`, `src/test/java/com/commerce/pricelist/GetPriceListTest.java`
- Verification: `./mvnw -q clean test`
- Expected: `PriceListTest` (a code the JDK does not list is refused when incoming, read when stored) and `GetPriceListTest` (a list stored in a code the JDK does not list reads 200 with that code) pass; the 146 earlier tests still pass
- Decisions that bind it: 2026-10-19 · BR-4 is checked on incoming requests only; a stored code is read as kept (BA, architect); `price-list` BR-4 written first
- Done 2026-10-19. `price-list` BR-4 amended and its open question closed first. Red: `./mvnw -q test-compile` exit 1, `cannot find symbol … method stored(java.lang.String)`. Then `Currency` a final class with `of` (incoming, BR-4) and `stored` (as kept); `JdbcPriceLists` and `JdbcPrices` read with `stored`. Output: `./mvnw -q clean test` exit 0; 17 classes, 148 tests, 0 failures, 0 errors (`PriceListTest` 7, `GetPriceListTest` 4). As expected

## Decisions

- 2026-10-19 · Decomposition, register and AR-2: recorded in `docs/specs/catalog/bounded-contexts.md` (option 1, INV-DATA-001, AR-2 removed, the `BIGINT` in the database rule) · architect, BA, TL
- 2026-10-19 · Business rules the contract does not state: `product` "A product reference is unique."; `price-list` "A price list holds one price per product." and "A price is never negative." · BA · not yet in the specs: their triggers and effects are not given (Blockers)
- 2026-10-19 · Forms: `product` thin, "its only rule checks an input: there is no state to protect"; `price-list` rich, "one price per product holds at every write" · architect · written in `docs/partition.yml` once the rules are in the specs (Task 3)
- 2026-10-19 · The comments and error messages of the two `Money.java` of `billing` name INV-DATA-001 with the catalog work; the migrations V4 and V6 stay unedited · TL · Task 9
- 2026-10-19 · A reference held by another product: 409, nothing created, compared exactly; two at once: a unique constraint in the store, the first to commit holds; `product` stays thin · BA, architect · `product` BR-1
- 2026-10-19 · A `setPrice` for a product already priced replaces the price, 200, no history; two at once: the store's key, the last to commit replaces; a future price hides the current one, intended · BA, architect · `price-list` BR-1 · rejected: dated prices over time
- 2026-10-19 · A negative amount: 400, nothing set; zero is a price; a currency other than the list's: 400; an unknown code (`XYZ`): 400, as `invoice`'s rule on a currency says, `price-list` its own check · BA, architect · `price-list` BR-2 to BR-4
- 2026-10-19 · "A price is never negative" and `invoice` BR-8 carry two meanings of price: each rule stays in its own spec · BA
- 2026-10-19 · Currency has one meaning with `billing`'s: a three-letter ISO 4217 code; not an invariant: each area keeps it in its Language · BA, architect · rejected: a register entry
- 2026-10-19 · INV-DATA-001 speaks of how any amount is stored; what an amount is stays in each area's Language · architect
- 2026-10-19 · `getProductPrice`: the price applies when its `effectiveFrom` is on or before the end of day `at`, UTC; `at` absent is today UTC; a day ends before 00:00 UTC of the next day; a past `at` is answered against the price held now; otherwise 404 · BA · `product` BR-9
- 2026-10-19 · An unknown product or price list: 404, as the contract says · architect · `product` BR-8, `price-list` BR-9; `setPrice`'s unknown product left open (Blockers)
- 2026-10-19 · Channel kept and returned, governs no price; validity accepted, not kept, one ending before it starts included · BA · `price-list` BR-6
- 2026-10-19 · `description`, `ean`, `attributes`, translations included: accepted, not kept; a value the schema refuses still gets 400 · BA, TL · `product` BR-4
- 2026-10-19 · `localized` and `Accept-Language` change nothing; `listProducts` by reference ascending, `references` exact, duplicates once, a page past the last 200 with no item and true totals · BA · `product` BR-6, BR-7
- 2026-10-19 · `createProduct` conflicts on a held reference only; `createPriceList` never answers 409 · BA · `product` BR-2, `price-list` BR-7
- 2026-10-19 · `product` reads prices through `PriceListPrices`, in `price-list`'s application layer: the amount, currency and `effectiveFrom` of the price that applies on a day, or none; only `product`'s outbound adapter calls it; an unknown price list and no price that day give one 404, one `detail` · architect · Tasks 4, 8
- 2026-10-19 · Error answers: the envelope on every catalog error; `detail` one sentence naming the refusal, no value; `errors` per field for 400 (`VALIDATION_FAILED`), one `CONFLICT` item for 409, one item with the contract's code and no `field` for 404; `field` a JSON pointer for a body field, the name for a header, query or path parameter; `message` one sentence without the value; `errorId` a new UUID, logged once with `X-Request-Id`, path and status; no `X-Request-Id`: 400, logged with `errorId`, path, status · TL · catalog AR-1, to be shown and validated as written (Task 2)
- 2026-10-19 · The order of refusals is the project's API rule: content, then the path's identifiers, then the rules on what is held; `getProductPrice` the product, the price list, then the day; a `priceListId` the schema refuses in the query is content, 400 · architect, TL · `product` BR-10, `price-list` BR-10
- 2026-10-19 · No authentication: the contract's `security` kept as written, nothing built, 401 and 403 never answered · TL, PO · Contracts with neighbours of both specs. The TL's "already answered … as in billing" is recorded nowhere in the repository: this line is its first record
- 2026-10-19 · 405, 406, 415 stay as the framework answers them, without the envelope · architect · Tasks 5 to 8
- 2026-10-19 · Base path `/commerce/v1/catalog` (the contract's `servers`), carried by `Location` (a path) and the envelope's `path`; billing unchanged · TL · Tasks 5 to 8
- 2026-10-19 · "Now" is the server's clock in UTC; an `effectiveFrom` with an offset is read in UTC; `createdAt` kept and returned, server clock UTC · TL, BA · `product` BR-5, `price-list` BR-8
- 2026-10-19 · `price-list` logs its identifiers only; an error names the field, never the amount · TL · `price-list` BR-11
- 2026-10-19 · A blank `name` or `label`: 400, nothing created · BA · `product` BR-3, `price-list` BR-5
- 2026-10-19 · Words: Product, Reference, Label, Price list, Channel, Price, Day as the BA gave them · BA · Reference and Label in `product`, Channel in `price-list`; the shared words to `catalog/area.md` once shown and validated as written (Task 2)
- 2026-10-19 · `docs/specs/catalog/area.md` validated as shown: five words, AR-1 · BA, TL
- 2026-10-19 · `price-list` does not read `product`: a price is set for the product identifier given, whether `product` holds it or not · architect · `price-list` BR-12 · rejected: a contract from `product` to `price-list`
- 2026-10-19 · `PRODUCT_NOT_FOUND` for an unknown product, `PRICE_LIST_NOT_FOUND` for an unknown price list, `PRICE_NOT_FOUND` when the list has no price for the product that day · TL · the first written in `product` BR-8; the other two conflict with the architect's "one 404, one detail" (Blockers)
- 2026-10-19 · `getProductPrice`: one 404, `PRICE_NOT_FOUND`, one `detail` "No such product, or no price for it in that price list"; `PriceListPrices` does not tell them apart; `PRICE_LIST_NOT_FOUND` for `getPriceList` and `setPrice`, `PRODUCT_NOT_FOUND` for `getProduct` · architect · the TL's three codes for `getProductPrice` withdrawn (TL)
- 2026-10-19 · A price applies by the day: on day `at` when its `effectiveFrom` is on or before the end of that day, UTC; `price-list` BR-1 reads the same · BA · rejected: by the instant
- 2026-10-19 · BR-4 takes `invoice`'s whole rule on a currency, the JDK's list included; an unknown currency is content, 400, checked with the body; the first refusal stops the checks · BA, architect
- 2026-10-19 · `setPrice`: the price list first, `PRICE_LIST_NOT_FOUND` for a `priceListId` not a UUID or unknown, and for a `productId` not a UUID · architect
- 2026-10-19 · A 500 carries the envelope, `detail` and `errorId`, `errors` empty; a 409 item names `/reference`; a body that does not parse: 400, one item, no `field`, `VALIDATION_FAILED` · TL · 405, 406, 415 stay the framework's: "the project's rule covers the error answers the contract declares" · architect
- 2026-10-19 · A malformed `X-Request-Id`: 400, its value never logged; not a UUID: not the 8-4-4-4-12 hexadecimal form; an unnamed body property: ignored, accepted; `Authorization`: ignored, never logged; both contexts log their identifiers, the path, the status and the `errorId` only · TL
- 2026-10-19 · Blank, Character, Refused, Amount, Minor units carry `billing`'s meanings, one meaning across the two areas; Identifier: a UUID made by the context that creates the resource; Applies: by the day; the area's Price, Price list, Product hold, the specs follow its wording; `product` BR-2 is a rule · BA
- 2026-10-19 · No maximum on `page` or `references`; `references=` empty is no filter · PO · `createdAt` where `Product` carries it · BA · no offset on `effectiveFrom`: 400; times kept to the millisecond, UTC · TL
- 2026-10-19 · No authentication's consequences all accepted for this version · PO
- 2026-10-19 · The billing edit stays in this plan, gated with this work (Task 9); INV-DATA-001 stays named in `product`: it answers an amount read from `price-list` · architect, TL
- 2026-10-19 · Area entries validated as shown · TL, architect · the seven words were the BA's meanings; two adapted by the assistant (Amount, Refused), validated by the TL and the architect, not by the BA
- 2026-10-19 · The `X-Request-Id` is logged with the `errorId`, the path and the status when well formed; AR-2 names it · TL
- 2026-10-19 · A missing `X-Request-Id` and a blank `label`: one item; the headers come first, the first refusal stops the checks · TL
- 2026-10-19 · A product created between two pages may shift them: accepted · PO · `Accept-Language` changes nothing on `price-list` either · BA
- 2026-10-19 · `backend-api-contract`: "An error response the contract declares, when it has a body, carries the common error envelope." · TL · rejected: the envelope on 405, 406, 415
- 2026-10-19 · A 400 carries one item per body field at fault within the group the first refusal stops at; the content is one group: a negative amount with `XYZ` gives two items, a negative amount in another currency than the list's one · architect · corrects the architect's earlier "one item"
- 2026-10-19 · P-4: one error handler per context · architect · rejected: a shared `com.commerce.catalog` package
- 2026-10-19 · P-5: a key on `(price_list_id, product_id)` and one `MERGE` · architect · rejected: `UPDATE` then `INSERT`, a version column
- 2026-10-19 · A `page` beyond the integer range: 400, as the schema's type says · TL, PO
- 2026-10-19 · The catalog generator executions map `UUID` to `String` and turn bean validation off; a `HandlerInterceptor` per context checks the headers then the query before binding, the controller the body whole, the use case the path's identifier · assistant, within the design · reason: `UUID.fromString` accepts `1-1-1-1-1` (AR-3), a generated `@Size` counts UTF-16 units (Character), Spring's binding stops at the first field (AR-1) · rejected: a global strict `String`→`UUID` converter (it would reach `billing`)
- 2026-10-19 · A body is judged on its JSON tree by a `RequestBodyAdvice` per context, before binding; the base path is a type-level `@RequestMapping` per controller; no `modelsToGenerate` in the catalog executions · assistant, within the design · reason: AR-1 (a wrong type loses the other faults at binding), two prefixes keyed by one text replace each other, `modelsToGenerate` drops inline models · rejected: one shared prefix configuration (it would sit outside both contexts, against P-4)
- 2026-10-19 · A catalog 500 logs the exception's class and its stack frames, never its message, with the `errorId`, the path and the status · TL · AR-2 of `docs/specs/catalog/area.md` to be amended first, shown and validated as written (Blockers), then Task 12
- 2026-10-19 · `apm install` run; the API rule deployed · TL
- 2026-10-19 · The rule file of how the catalog checks its requests, scoped to `product` and `pricelist` code, with the four lines proposed · TL · written as `.apm/instructions/backend-api-contract-catalog.instructions.md`
- 2026-10-19 · BR-4 is checked on incoming requests only; a stored code is read as kept · BA, architect · `price-list` BR-4, Task 13 · rejected: re-checking stored codes on read
- 2026-10-19 · No caller of the catalog is drawn in the C4 model in this version: the contract names none · PO, architect
- 2026-10-19 · AR-2 as shown: "A catalog log holds identifiers, the path, the status and the `errorId` only; a 500 adds the class and the stack frames of its exception", never its message · TL, architect · written in `docs/specs/catalog/area.md`
- 2026-10-19 · `apm install` run: `.claude/rules/backend-api-contract-catalog.md` deployed (read: the same five lines, its two paths) · TL

## Blockers

