# Merge: `product` absorbs `price-list` — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-20

## Tasks

- [x] Task 1 — 2026-10-20 — The specs: banners, `product`'s derived sections, `bounded-contexts.md`
- [x] Task 2 — 2026-10-20 — The design of the code merge and `product`'s reason, validated by the architect and the TL
- [x] Task 3 — 2026-10-20 — `price-list`'s code moves into `product`'s layers
- [x] Task 4 — 2026-10-20 — `docs/partition.yml` and the architecture test: `price-list` keeps its id and area only
- [x] Task 5 — 2026-10-20 — Proof that it was a move
- [x] Task 6 — 2026-10-20 — After: C4, budget, the record
- [x] Task 7 — 2026-10-20 — Close: gate, levels, README, carry-over report

## Goal

Carry out the merge recorded in `docs/architecture/reshapes/2026-10-20-price-list-into-product.md` (workshop `docs/sources/workshop-price-list-into-product.md`, validated by name by the architect on 2026-10-20): `product` absorbs `price-list`. The code of `price-list` comes behind the interfaces of `product`; there is no contract between them to remove; the HTTP contract `catalog.openapi.json` 1.1.0 is unchanged; no behaviour changes (BR-9 of `product`, BR-12 and BR-14 of `price-list` carried over as they are). The specs move as `partition-reshape` `references/specs.md` says for a merge: no business rule is copied by the assistant; Léa Petit (BA) carries the hand-written sections over and renumbers the rules.

## Task 1 — The specs: banners, `product`'s derived sections, `bounded-contexts.md`

- Scope: `docs/specs/catalog/product/spec.md` (banner "absorbed `price-list`"; `Source parts owned` gains createPriceList, getPriceList, setPrice, getProductPrice, the tag `PriceList` and the `PriceList`, `Price`, `Money` schemas; `Persistence` unchanged: neither declares schema documents), `docs/specs/catalog/price-list/spec.md` (banner "merged into `product`", frozen), `docs/specs/catalog/bounded-contexts.md` (finding 1: `Kept against the criteria`, `Decisions not to split`; finding 4: `product`'s retained line on the seven units, `price-list`'s line out, the overturned lines to `History` word for word, `Coverage` → `product`, the `Decided` line, a `History` line `merge`). Not touched: the hand-written sections of either spec, the area file (AR-5 comes down, `price-list` leaves the `Binds` of AR-1 to AR-4, by a person), `docs/partition.yml` (Tasks 2 and 4)
- Verification: `grep -n "Status:\*\* absorbed\|Status:\*\* merged" docs/specs/catalog/product/spec.md docs/specs/catalog/price-list/spec.md; grep -n "createPriceList\|getPriceList\|setPrice\|getProductPrice" docs/specs/catalog/product/spec.md; grep -n "price-list" docs/specs/catalog/bounded-contexts.md`
- Expected: one banner in each spec; four `Source parts owned` rows of the price-list's units in `product`'s spec; in `bounded-contexts.md`: `product` retained on the seven units, no `price-list` line under `Bounded contexts retained`, one `Decisions not to split` line "merge of 2026-10-20 into `product`", one `Kept against the criteria` line on C2, C3, C4, the four units → `product` in `Coverage`, a `History` line `merge` quoting the overturned lines
- Decisions that bind it: 2026-10-20 · finding 1 (b): kept against C2, C3, C4, and `price-list` to `Decisions not to split` (architect); finding 4 as proposed (architect); no business rule copied by the assistant (`references/specs.md`)
- Result: `product/spec.md:16` banner "absorbed `price-list`", `:49-52` the four units, the tag `PriceList` and the `PriceList`, `Price`, `Money` schemas; `price-list/spec.md:16` banner "merged into `product`"; `bounded-contexts.md:13` `Decided` names the workshop, `:36` `product` on the seven units, no `price-list` line retained, `:43` `Decisions not to split` "Merge of 2026-10-20 into `product`" (no rule calls it: kept against C2, C3, C4), `:47` option 3 out of `Alternatives rejected`, to `History`, `:58` `Kept against the criteria` C2, C3, C4, `:88-91` the four units → `product`, `:102` `History` `merge` quoting the overturned lines word for word — as expected
- [x] 2026-10-20

## Task 2 — The design of the code merge and `product`'s reason, validated by the architect and the TL

- Scope: none written in code; the design below, proposed 2026-10-20, to be validated or changed:
  - D1. One hexagon `product`: `price-list`'s classes move into `com.commerce.product.{domain,application,inbound,outbound}` under their names (rule 3 of the project: one package per bounded context); no name collides: `product` has no `Money`, `Currency`, `Channel`, `Price*`
  - D2. The copies collapse to `product`'s: `Fault`, `Identifiers`, `BodyUnreadable`, `RequestRefused` (they differ by their package and a comment naming the context); `JsonBody`: one class, the union of both (`product`'s `array`, `int32`; `price-list`'s `oneOf`, `int64`, `date`, `dateTime`, `text`). Alternative: `price-list`'s `JsonBody` kept under another name
  - D3. The inbound side stays one set per tag: `ProductController` and `PriceListController`, each with its `*Errors` advice (`assignableTypes`), `*RequestCheck`, `*BodyCheck`, `*WebConfiguration`: interface segregation, one generated API interface each; base paths unchanged. Alternative: one controller and one advice for the seven operations
  - D4. Domain and ports unchanged: `Price`, `PriceList`, `Money`, `Currency`, `Channel`, `InvalidPriceList`; ports `Products`, `PriceLists`, `Prices` with their `Jdbc*` adapters. No data moves, no migration, no foreign key from `price` to `product` (BR-12, finding 2)
  - D5. `pom.xml`: one generator execution `product`, `FILTER=tag:Product|PriceList`, `com.commerce.product.inbound.api`; the execution `price-list` goes. Checked in Task 3: the generated inline models do not collide
  - D6. Tests: the seven files of `src/test/java/com/commerce/pricelist/` move to `com.commerce.product`, their assertions unchanged; `PriceListApiTest` and `PriceListClient` move as they are (A) or fold into `ProductApiTest` and `ProductClient` (B) — to choose
  - D7. The comments of the moved code that cite `price-list`'s rule numbers (BR-n, which collide with `product`'s) stay as they are in this move; realigned once Léa Petit has renumbered, after the carry-over. Alternative: prefix them now ("`price-list` BR-n")
  - R. `product`'s reason in `docs/partition.yml`, form rich, proposed: "a product can be retired and is never reactivated (BR-9); the unique reference (BR-1) stays held by the store's unique constraint (Claire Martin, architect, 2026-10-20); one price per product and list holds at every write (BR-1 of `price-list`, until renumbered) (architect, 2026-10-19); absorbed `price-list` (Claire Martin, architect, 2026-10-20)"
- Verification: the architect's and the TL's answer, written in `## Decisions`
- Expected: each of D1 to D7 and R validated or changed, by name
- Decisions that bind it: the merge stays a move, no behaviour changes (architect, finding 2)
- Result (architect, TL, 2026-10-20): D1, D2 (one combined `JsonBody`), D3 (one inbound set per tag), D4, D5 as proposed; D6 option A; D7 the comments stay until Léa Petit renumbers; R as proposed; the wording of the `Decisions not to split` line kept (architect) — as expected
- [x] 2026-10-20

## Task 3 — `price-list`'s code moves into `product`'s layers

- Scope: `src/main/java/com/commerce/product/**`, `src/main/java/com/commerce/pricelist/**` (emptied), `src/test/java/com/commerce/product/**`, `src/test/java/com/commerce/pricelist/**` (emptied), `pom.xml`, as Task 2 decides. Not touched: migrations, `application.yaml`, `catalog.openapi.json`, `discount`, `invoice`, `payment`
- Verification: `./mvnw clean verify`; `find src -path '*com/commerce/pricelist*'`; `grep -rn "com\.commerce\.pricelist" src pom.xml`
- Baseline, 2026-10-20 at `271e685`: `./mvnw clean verify` → `Tests run: 183, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`; in `com.commerce.pricelist`: `SetPriceTest` 12, `PriceListTest` 7, `CreatePriceListTest` 11, `GetPriceListTest` 5, `GetProductPriceTest` 8
- Expected (written 2026-10-20, before the run): `BUILD SUCCESS`, `Tests run: 183, Failures: 0`; the five classes above in `com.commerce.product` with the same counts and their assertions unchanged; generated `com.commerce.product.inbound.api` holds `ProductApi` and `PriceListApi`; the find lists nothing; the grep finds nothing
- Done: 24 classes and the 7 test files moved with `git mv` (domain 6, application 11, inbound 5, outbound 2); 5 deleted, `price-list`'s copies of `Fault`, `Identifiers`, `BodyUnreadable`, `RequestRefused`, `JsonBody` (D2); `product`'s `JsonBody` gains `oneOf`, `int64`, `date`, `dateTime`, `text`, copied as they were; `com.commerce.pricelist` → `com.commerce.product` in the moved files, the generated package included; `pom.xml` one execution `product`, `FILTER=tag:Product|PriceList`, the execution `price-list` gone
- Result: `Tests run: 183, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`; in `com.commerce.product`: `SetPriceTest` 12, `PriceListTest` 7, `CreatePriceListTest` 11, `GetPriceListTest` 5, `GetProductPriceTest` 8, as at `271e685`; the 13 `ERROR` log lines of the run, as many as the baseline's (the tests that simulate a failure); generated `com.commerce.product.inbound.api`: `ApiUtil`, `PriceListApi`, `ProductApi`, the models without collision; the find lists nothing; the grep finds nothing (exit 1); the diff of the tests against `271e685`, the package and imports aside: one line, `GetPriceListTest`'s expected stack frame `at com.commerce.pricelist.application.GetPriceListUseCase` → `at com.commerce.product.…`, the class's own package — as expected, but for that line, which names the moved class, no assertion weakened
- [x] 2026-10-20
- Decisions that bind it (architect, TL, 2026-10-20): D1 one hexagon `product`, `price-list`'s classes under their names in its four layers; D2 `Fault`, `Identifiers`, `BodyUnreadable`, `RequestRefused` collapse to `product`'s, one `JsonBody` with the union of both; D3 one inbound set per tag, base paths unchanged; D4 domain, ports and `Jdbc*` adapters unchanged, no data moves, no migration, no foreign key; D5 one generator execution `product`, `FILTER=tag:Product|PriceList`, the inline models checked for collisions; D6 option A: the seven test files move to `com.commerce.product`, `PriceListApiTest` and `PriceListClient` as they are, assertions unchanged; D7 the comments citing `price-list`'s BR-n stay as they are

## Task 4 — `docs/partition.yml` and the architecture test: `price-list` keeps its id and area only

- Decisions that bind it too: R, `product`'s reason, form rich: "a product can be retired and is never reactivated (BR-9); the unique reference (BR-1) stays held by the store's unique constraint (Claire Martin, architect, 2026-10-20); one price per product and list holds at every write (BR-1 of `price-list`, until renumbered) (architect, 2026-10-19); absorbed `price-list` (Claire Martin, architect, 2026-10-20)" (architect, 2026-10-20)
- Scope: `docs/partition.yml` (`price-list` keeps `id` and `area`, loses `code` and `hexagons`, while `docs/specs/catalog/price-list/` exists; `product`'s reason as Task 2 decides), `src/test/java/com/commerce/ArchitectureTest.java` (its comment no longer names two catalog contexts that call none another, if Task 3 leaves it untrue)
- Verification: `./mvnw clean verify -Dtest=ArchitectureTest`, then `./mvnw clean verify`
- Expected (written 2026-10-20, before the run): `ArchitectureTest` `Tests run: 4, Failures: 0`, the hexagon `product` now holding the moved classes in its four layers; then `Tests run: 183, Failures: 0`, `BUILD SUCCESS`; `partition.yml`: `price-list` with `id` and `area` only, `product`'s reason R
- Done: `partition.yml` `price-list` keeps `id: price-list`, `area: catalog`, loses `code` and `hexagons`; `product`'s reason R as validated; `ArchitectureTest.java:120` the comment adds "`price-list` merged into `product` on 2026-10-20: no contract stood between them" (no `Contract` to remove: none was declared between them)
- Result: `ArchitectureTest` `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`; `./mvnw clean verify` `Tests run: 183, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` — as expected
- [x] 2026-10-20
- Decisions that bind it: the absorbed context stays declared with its area as long as its spec folder exists (`partition-reshape`)

## Task 5 — Proof that it was a move

- Scope: none written; the record's `Proof that it was a move`
- Verification: `git status --short` outside `src/{main,test}/java/com/commerce/{product,pricelist}`, against the commit before the merge; `git diff --quiet` on the callers and the contracts
- Expected (written 2026-10-20, before the run): outside `src/{main,test}/java/com/commerce/{product,pricelist}`, only
  - the wiring: `pom.xml` (D5), `src/test/java/com/commerce/ArchitectureTest.java` (its comment), `docs/partition.yml`;
  - the documents of the reshape: `docs/plans/price-list-into-product.md`, `docs/architecture/reshapes/2026-10-20-price-list-into-product.md` and `docs/sources/workshop-price-list-into-product.md` (new), `docs/specs/catalog/product/spec.md`, `docs/specs/catalog/price-list/spec.md`, `docs/specs/catalog/bounded-contexts.md`;
  - unchanged against `271e685`: the callers and the neighbours, `src/{main,test}/java/com/commerce/{discount,invoice,payment}`, `docs/contracts/catalog.openapi.json`, `src/main/resources` (no migration, `application.yaml`); inside `product`, the product's own code but `JsonBody` (D2): its use cases, ports, domain, `Product*` inbound classes, `Fault`, `Identifiers`, `BodyUnreadable`, `RequestRefused`, `JdbcProducts`, and its six test files
- Decisions that bind it: none
- Result: HEAD `271e685`; `git status --short` outside the merged code: `M docs/partition.yml`, `M docs/specs/catalog/bounded-contexts.md`, `M docs/specs/catalog/price-list/spec.md`, `M docs/specs/catalog/product/spec.md`, `M pom.xml`, `M src/test/java/com/commerce/ArchitectureTest.java`, `?? docs/architecture/reshapes/2026-10-20-price-list-into-product.md`, `?? docs/plans/price-list-into-product.md`, `?? docs/sources/workshop-price-list-into-product.md`; `git diff --quiet 271e685 --` the neighbours and the contracts: `exit=0`; the product's own 22 classes and 6 test files: `exit=0` — as expected: no caller changed
- [x] 2026-10-20

## Task 6 — After: C4, budget, the record

- Scope: `docs/architecture/model.yml` and its views (`partition-c4`), `docs/budget/` (`partition-budget`), the record's `After`
- Verification: `node .claude/skills/partition-c4/scripts/c4-drawio.mjs --check` then without `--check`; `node .claude/skills/partition-budget/scripts/budget.mjs`
- Done before the run: `model.yml` loses the component `price-list` and the relation `price-list -> h2: SQL`; `product -> h2: SQL` stays; no dynamic view names `price-list`
- Expected (written 2026-10-20, before the run): `--check` no error, 11 elements (12 less `price-list`), 8 relations (9 less 1), 7 views; `c4.drawio` regenerated, seven pages; budget: `product` higher than its 5 756 of 2026-10-20 by the four unit rows, the tag and schema rows and the banner only (the hand-written sections of `price-list` not yet carried over), well under 32 000; `price-list` still measured while its spec exists, its scoped rules down (its code path left `partition.yml`)
- Decisions that bind it: none
- Result: `model.yml: 10 elements, 8 relations, 7 views, no error`; `c4.drawio` seven pages regenerated, every element placed automatically (no hand-set position kept); budget `product` 6 090 (19 % ok, spec 1 914 → 2 248), `price-list` 6 019 (19 % ok, spec 2 682 → 2 747, scoped rules 570 → 0), plan read on resume 714 of 4 702; record `docs/budget/2026-10-20.md` replaced, the earlier figures quoted in it; the record's `After` ticked — as expected but for the element count: 10, not 11 — there were 11 before, the prediction miscounted; the model is right
- [x] 2026-10-20

## Task 7 — Close: gate, levels, README, carry-over report

- Scope: as `partition-plan` _Close_ says; and, as `references/specs.md` _At closing_ says: the hand-written sections of `price-list` to carry into `product` with the two meanings of "Product" and of "not found" and the rules renumbered (Léa Petit, BA), AR-5 to bring down from `area.md` into `product` and `price-list` out of the `Binds` of AR-1 to AR-4 (by a person), then the folder `docs/specs/catalog/price-list/` to delete by hand and `price-list`'s entry out of `docs/partition.yml`
- Verification: `partition-gate` on the changes of this plan
- Expected: SHIP or SHIP WITH CONDITIONS
- Decisions that bind it: finding 3: Léa Petit states the two meanings in the merged Language and renumbers the rules at the carry-over (BA, 2026-10-20)
- **Gate run 1, 2026-10-20 — SHIP WITH CONDITIONS, confidence High.** Target: `271e685` to the working tree, 46 files (31 renames, 5 deletions, `JsonBody`, `pom.xml`, `ArchitectureTest.java`, the documents of the reshape); two bounded contexts merged, no contract between them; the HTTP contract `catalog.openapi.json` 1.1.0 and the neighbours unchanged (Task 5)
  - Evidence: `./mvnw clean org.jacoco:jacoco-maven-plugin:prepare-agent verify org.jacoco:jacoco-maven-plugin:report` → `Tests run: 183, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`, the same 183 as at `271e685` (baseline run this session, Task 3); `diff_coverage.py --range 271e685` → `32/35` changed lines executed (91.4 %); the 32 files it lists "not measured" are the renamed ones, whose changed lines are the package and imports: all 24 moved classes are in `jacoco.xml`, executed; `detect_test_tampering.sh --range 271e685` → nothing: no test deleted, skipped or weakened; the moved tests differ by their package, their imports and `GetPriceListTest`'s expected stack frame, which names the moved class (Task 3)
  - Read: the 5 deleted copies differ from `product`'s by their package and a comment only; `JsonBody`'s added methods are `price-list`'s, unchanged; no simple name collides in `com.commerce.product`; `PriceListErrors` and `ProductErrors` each scoped to their controller (`assignableTypes`), each `*WebConfiguration` to its own paths: no answer changes; no migration, `application.yaml` names no `pricelist` logger
  - Findings: [Low] `product/inbound/JsonBody.java:97,135-136` uncovered: `int64` absent and `text` not a string, branches copied unchanged from `price-list`'s `JsonBody`. [Low] the loggers of the moved classes are now `com.commerce.product.*`, no longer `com.commerce.pricelist.*`: a log filter outside the repository keyed on that name would miss them. [Low] the hand-written sections of `product`'s spec (`Business objective`, `Scope`: "Out of scope: the price lists") and `area.md` (AR-5, the `Binds` of AR-1 to AR-4) still describe two contexts: the merge's banners say so, the carry-over by Léa Petit settles it — reported at closing, not a condition. [Low] `.claude/rules/backend-api-contract-catalog.md` stale (see `## Blockers`)
  - Skipped: migrations (none), performance (the same queries, the same beans), frontend, async (none)
  - Not verified: the look at the running application — no manual probe was run in this session; the price-list tests run through a real HTTP server
  - Deployment: no schema change, no configuration change; rollback by redeploying `271e685`'s artifact is safe
  - Condition: G1 — the TL's look on this working tree: `POST /price-lists` `201`; `PUT …/prices/{productId}` `200`; `GET …/product-prices/{productId}` `200`, then `404` `PRICE_NOT_FOUND` for an unknown product; `GET /price-lists/{id}` unknown `404` `PRICE_LIST_NOT_FOUND`; `POST /products` `201`, `GET /products/{id}` `200`; a request without `X-Request-Id` `400`; the log holds no amount, label or reference value
- Levels (2026-10-20): finding 1 and finding 4 are in `bounded-contexts.md` (Task 1); finding 2 (no behaviour change) needs no level: BR-9, BR-12, BR-14 stand as written; finding 3 (two meanings, renumbering) goes to `product`'s spec by the BA at the carry-over; D1 to D7 and R stay in `## Decisions`, R written in `docs/partition.yml`. Area file: no entry added or changed by the assistant; AR-5 to come down by a person. Invariant register: no entry, no candidate
- README unaffected: API, configuration and commands unchanged; its line 42 still points to `docs/specs/catalog/price-list/spec.md`, true until the carry-over — realign then. C4: done in Task 6
- G1 met (TL, 2026-10-20), the look on the running app (port 18080, stopped after): `POST /products` `201`; `GET /products/{id}` `200`; `POST /price-lists` `201`; `PUT …/prices/{productId}` `200`; `GET …/product-prices/{productId}` `200`; `GET …/product-prices/unknown` `404` `PRICE_NOT_FOUND`; `GET /price-lists/unknown` `404` `PRICE_LIST_NOT_FOUND`; `GET /products/{id}` without `X-Request-Id` `400`; the log holds no amount, label or reference value — as expected
- The stale rule (TL, 2026-10-20): its source `.apm/instructions/backend-api-contract-catalog.instructions.md` changed as the TL asked: `applyTo` without `src/main/java/com/commerce/pricelist/**`; the first rule names the executions "`product`, for the tags `Product` and `PriceList`, and `discount`" (the executions: TL, 2026-10-20). The deployed `.claude/rules/backend-api-contract-catalog.md` is regenerated by the TL's `apm install`, after this plan; its `description` still names `price-list`, not asked
- **Gate run 2, 2026-10-20 — SHIP, confidence High.** Target: `271e685` to the working tree, 47 files: run 1's and the rule source. Evidence: `./mvnw clean verify` → `Tests run: 183, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`; `detect_test_tampering.sh --range 271e685` → nothing; the only file added since run 1 is a Markdown instruction, read by no build step. G1 met; run 1's Low findings stand, none a condition. The verdict covers every change of the plan; the plan closes
- Carry-over report (`references/specs.md`, _At closing_): (1) the hand-written sections of `docs/specs/catalog/price-list/spec.md` into `docs/specs/catalog/product/spec.md`, the two meanings of "Product" and of "not found" stated, the rules renumbered, then the banner of `product`'s spec removed — Léa Petit (BA); (2) AR-5 down from `docs/specs/catalog/area.md` into `product`'s `Business rules`, `price-list` out of the `Binds` of AR-1 to AR-4 — a person; (3) the folder `docs/specs/catalog/price-list/` deleted by hand, then `price-list` out of `docs/partition.yml`, README line 42 realigned; (4) the comments of the moved code citing `price-list`'s BR-n realigned after the renumbering (D7)

## Decisions

- 2026-10-20 · Merge: `product` absorbs `price-list`; no contract between them to remove; `catalog.openapi.json` 1.1.0 unchanged · workshop (Claire Martin, Paul Durand, Léa Petit), validated by name by the architect · rejected: `price-list` kept apart
- 2026-10-20 · Finding 1 (b): the merge is kept against C2, C3 and C4 in `Kept against the criteria`, with the workshop's reason; `price-list` goes to `Decisions not to split`, "merge of 2026-10-20 into `product`" · architect · rejected: (a) R4 prevailing
- 2026-10-20 · Finding 2: the merge stays a move; BR-9, BR-12, BR-14 carried over unchanged; a price that checks its product is a new behaviour, later · architect
- 2026-10-20 · Finding 3: Léa Petit states the two meanings of "Product" and of "not found" in the merged Language and renumbers the rules at the carry-over · BA
- 2026-10-20 · Finding 4: `bounded-contexts.md`: the overturned lines to `History`, word for word; `Coverage` → `product`; the `Decided` line names the workshop · architect
- 2026-10-20 · Léa Petit (BA) carries the hand-written sections of `price-list` into `product` · workshop
- 2026-10-20 · The `Decisions not to split` line says no rule calls the merge, kept against C2, C3, C4 · architect
- 2026-10-20 · D1, D3, D4, D5 as written in Task 2 · architect, TL · rejected for D3: one controller and one advice for the seven operations
- 2026-10-20 · D2: one `JsonBody`, the union of both · architect, TL · rejected: `price-list`'s kept under another name
- 2026-10-20 · D6 option A: `PriceListApiTest` and `PriceListClient` move as they are · architect, TL · rejected: folding them into `ProductApiTest` and `ProductClient`
- 2026-10-20 · D7: the comments citing `price-list`'s BR-n stay until Léa Petit renumbers · architect, TL · rejected: prefixing them now
- 2026-10-20 · R: `product`'s reason in `docs/partition.yml` as written in Task 2, form rich · architect
- 2026-10-20 · G1 · The TL's look on the running app: every answer as expected, nothing to hide in the log · TL
- 2026-10-20 · The catalog rule's source: `pricelist/**` out of `applyTo`, the executions named `product` (tags `Product`, `PriceList`) and `discount`; `apm install` by the TL after the plan · TL

## Blockers

None.
