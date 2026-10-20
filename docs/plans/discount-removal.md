# Removal: `discount` — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-20

## Tasks

- [x] Task 1 — 2026-10-20 — The specs: `discount` frozen, `bounded-contexts.md`, the reshape record
- [x] Task 2 — 2026-10-20 — The owner rules on the code and on the `discount` table
- [x] Task 3 — 2026-10-20 — The code of `discount` goes, as ruled
- [x] Task 4 — 2026-10-20 — `docs/partition.yml` and the architecture test: `discount` keeps its id and area only
- [x] Task 5 — 2026-10-20 — Proof that it was a removal: nothing else changed
- [x] Task 6 — 2026-10-20 — After: C4, budget, the record
- [x] Task 7 — 2026-10-20 — Close: gate, levels, README, carry-over report

## Goal

Carry out the removal recorded in `docs/architecture/reshapes/2026-10-20-discount-removal.md` (proposal option 1 of the delta catalog 1.1.0 → 2.0.0, validated by name by Claire Martin, architect, 2026-10-20): `discount` owns no unit of `docs/contracts/catalog.openapi.json` 2.0.0 any more and goes. The specs move as `partition-reshape` `references/specs.md` says for a removal: the spec is frozen first, its folder is kept, nothing written by hand is copied by the assistant; the code is deleted only after the owner rules on it, in a task of its own.

## Task 1 — The specs: `discount` frozen, `bounded-contexts.md`, the reshape record

- Scope: `docs/specs/catalog/discount/spec.md` (banner "removed", frozen), `docs/specs/catalog/bounded-contexts.md` (the `discount` line leaves `Bounded contexts retained`, carried word for word into a `History` line `removal`; the 2.0.0 gap notes that the specs carry it), `docs/architecture/reshapes/2026-10-20-discount-removal.md` (created). Not touched: the hand-written sections of either spec, `area.md`, `docs/partition.yml`, the code
- Verification: `grep -n "Status:\*\* removed" docs/specs/catalog/discount/spec.md; grep -n "^| product \|^| discount \|| removal |\|| createDiscount\|| getDiscount" docs/specs/catalog/bounded-contexts.md`
- Expected: one banner in `discount/spec.md`; in `bounded-contexts.md`, a `product` line, no `discount` line in `Bounded contexts retained`, no `createDiscount` nor `getDiscount` in `Coverage`, one `History` line `removal`
- Decisions that bind it: 2026-10-20 · removal of `discount`, proposal option 1 (Claire Martin, architect); no business rule copied by the assistant (`references/specs.md`)
- Result: `discount/spec.md:16` banner "removed on 2026-10-20"; `bounded-contexts.md:36` the `product` line only, `:107` the `History` line `removal`; no `discount` retained line, no discount unit in `Coverage` — as expected
- [x] 2026-10-20

## Task 2 — The owner rules on the code and on the `discount` table

- Scope: none written in code; the questions below, to be answered by name:
  - Q1. Delete the code of `discount`: `src/main/java/com/commerce/discount/**` (18 classes; 17 was a miscount, corrected 2026-10-20), `src/test/java/com/commerce/discount/**` (`CreateDiscountTest`, `GetDiscountTest`, `DiscountApiTest`, `DiscountClient`), and the generator execution `discount` of `pom.xml:158-167` (`FILTER=tag:Discount`, which 2.0.0 no longer declares)?
  - Q2. The table `discount` made by `V8__create_discount.sql` (V8 is not edited): (a) a new migration `V10` drops it, its rows lost; or (b) the table stays, unread by any code?
  - Q3. Who archives or reassigns the hand-written sections of `discount/spec.md` (BR-1 to BR-10 included), and brings AR-1 to AR-4 and the area words of `catalog/area.md`, now binding `product` alone, down to `product/spec.md`? Until then the folder `discount/` and its `docs/partition.yml` entry stay
- Verification: the owner's answer, written in `## Decisions`
- Expected: Q1, Q2, Q3 each answered by name
- Decisions that bind it: 2026-10-20 · the code is deleted only after the owner rules on it, in a task of its own (`partition-reshape`)
- Result (2026-10-20): Q1 delete it (PO); Q2 (a), a new migration drops the `discount` table, `V8` not edited (architect); Q3 Léa Petit does the hand moves after this plan (BA); P-4 of `patterns-grid.md` raised, not rewritten (architect) — as expected
- [x] 2026-10-20

## Task 3 — The code of `discount` goes, as ruled

- Scope: as ruled in Task 2: `src/main/java/com/commerce/discount/**`, `src/test/java/com/commerce/discount/**`, `pom.xml`, and `src/main/resources/db/migration/` only if Q2 is (a). Not touched: `product`, `invoice`, `payment`, `V1` to `V9`, `catalog.openapi.json`
- Verification: `./mvnw clean verify` (baseline taken first, at the commit before this task); `find src -path '*com/commerce/discount*'`; `grep -rn "com\.commerce\.discount" src pom.xml`
- Baseline: at `HEAD` (`771e40e`) `./mvnw clean verify` → `BUILD FAILURE`, `cannot find symbol` `DiscountApi`, `CreateDiscountRequest`, `Discount` in `discount/inbound/DiscountController.java`, `DiscountBodyCheck.java`: contract 2.0.0 no longer generates them, the break this removal absorbs. The green baseline is the last recorded run, `Tests run: 183, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` (`docs/plans/price-list-into-product.md:111`); no commit touched `src` nor `pom.xml` since `f339eaa`. Building `f339eaa` apart was not approved in this session
- Expected: `BUILD SUCCESS`, `Tests run: 160, Failures: 0, Errors: 0, Skipped: 0` (183 less the 23 `@Test` of `CreateDiscountTest` 17 and `GetDiscountTest` 6, none parameterized); Flyway applies `V10` on H2; the find lists nothing; the grep finds nothing
- Decisions that bind it (2026-10-20): Q1 the code of `discount`, its tests and the generator execution `discount` of `pom.xml` are deleted (PO); Q2 (a) a new migration `V10` drops the table `discount`, `V8` not edited (architect)
- Done: `src/main/java/com/commerce/discount/` (18 classes) and `src/test/java/com/commerce/discount/` (4 files) deleted; the execution `discount` of `pom.xml` removed; `src/main/resources/db/migration/V10__drop_discount.sql` added (`DROP TABLE discount;`)
- Result: `Tests run: 160, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`; Flyway "Migrating schema "PUBLIC" to version "10 - drop discount""; the find lists nothing; the grep finds nothing (`exit=1`). The `ERROR` lines of the log are the tests that simulate "the database is down" — as expected
- [x] 2026-10-20

## Task 4 — `docs/partition.yml` and the architecture test: `discount` keeps its id and area only

- Scope: `docs/partition.yml` (the `discount` entry keeps `id` and `area` while `docs/specs/catalog/discount/` exists), `src/test/java/com/commerce/ArchitectureTest.java` only if it fails on an entry with no hexagon
- Verification: `grep -n -A3 "id: discount" docs/partition.yml; ./mvnw clean verify`
- Expected: the entry reads `id: discount`, `area: catalog`, nothing else (no `code`, no `hexagons`); `ArchitectureTest` unchanged, its `readHexagons` skips an entry with no `hexagons` (`ArchitectureTest.java:144`); `Tests run: 160, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`
- Decisions that bind it: the removed bounded context stays declared, with its area, as long as its spec folder exists (`partition-reshape`)
- Result: `docs/partition.yml:79-80` `id: discount`, `area: catalog`, nothing else; `ArchitectureTest` unchanged, `Tests run: 4, Failures: 0`; `Tests run: 160, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` — as expected
- [x] 2026-10-20

## Task 5 — Proof that it was a removal: nothing else changed

- Scope: none written; the record's `## Proof that it was a move`
- Verification: `git status --short -- . ':!src/main/java/com/commerce/discount' ':!src/test/java/com/commerce/discount'` against `771e40e`, the commit before Task 1; then `git diff --quiet 771e40e -- src/main/java src/test/java/com/commerce/ArchitectureTest.java docs/contracts src/main/resources/db/migration/V8__create_discount.sql; echo exit=$?`
- Expected: outside the removed code, only the wiring (`M pom.xml`, `M docs/partition.yml`, `?? V10__drop_discount.sql`) and the documents of the reshape (`M bounded-contexts.md`, `M discount/spec.md`, `??` the record, `??` the plan); `exit=0`: no class of `product`, `invoice`, `payment` changed, nor `ArchitectureTest`, the contracts, `V8`
- Decisions that bind it: none
- Result (2026-10-20): `M docs/partition.yml`, `M docs/specs/catalog/bounded-contexts.md`, `M docs/specs/catalog/discount/spec.md`, `M pom.xml`, `??` the record, the plan, `V10__drop_discount.sql`; `git diff --stat 771e40e` on `src/main/java` (but `discount`), `ArchitectureTest.java`, `docs/contracts`, `V8`: empty, no change (the `--quiet … echo exit=$?` form was refused by the session's sandbox; `--stat` used instead). No caller changed — as expected
- [x] 2026-10-20

## Task 6 — After: C4, budget, the record

- Scope: `docs/architecture/model.yml`, `docs/architecture/c4.drawio` (`partition-c4`), `docs/architecture/patterns-grid.md` P-4 (reported to the architect, not rewritten), `docs/budget/` (`partition-budget`), the record's `## After`
- Verification: `node .claude/skills/partition-c4/scripts/c4-drawio.mjs --check`; `node .claude/skills/partition-c4/scripts/c4-drawio.mjs`; `grep -c -i discount docs/architecture/c4.drawio docs/architecture/model.yml`; `node .claude/skills/partition-budget/scripts/budget.mjs` (the commands were not written here before they were run; the expected result below was)
- Expected: the component `discount` and its relation `discount -> h2` gone; a budget record of the day
- Decisions that bind it: 2026-10-20 · P-4 of `patterns-grid.md` is raised to the architect, not rewritten (architect)
- Result: `model.yml: 9 elements, 7 relations, 7 views, no error`; seven pages regenerated; `discount` 0 times in `model.yml` and in `c4.drawio`; `docs/budget/2026-10-20.md` replaced: `discount` 5 788 → 5 190 (16 %), `product` 8 183 → 8 169 (26 %), no gauge over its threshold; the record's `## After` filled; P-4 raised in the closing report — as expected
- [x] 2026-10-20

## Task 7 — Close: gate, levels, README, carry-over report

- Scope: `partition-gate` on the plan's changes; `README.md` (`partition-readme`: the discount endpoints, `DISCOUNT_NOT_FOUND`, migration `V8`); the carry-over report of `references/specs.md`
- Verification: the gate's verdict
- Expected: SHIP or SHIP WITH CONDITIONS
- Decisions that bind it: 2026-10-20 · Léa Petit (BA) does the hand moves (`discount`'s hand-written sections, AR-1 to AR-4 and the area words down to `product`) after this plan: the carry-over report names her (BA)
- README (`partition-readme`, before the gate so that one verdict covers every change): the tags `Product` and `PriceList` only; the two discount endpoints replaced by one line, "Since version 2.0.0 of the contract, the discounts are gone"; `DISCOUNT_NOT_FOUND` out of the error codes; the rules in `product/spec.md` and `area.md`; `V10` "drops the table of the discounts, with every discount stored; it changes no other table"
- Gate (`partition-gate`, 2026-10-20), target `771e40e` → working tree, 30 files +39/−1268, plus 3 untracked (`V10`, the record, the plan). **SHIP WITH CONDITIONS**, confidence Medium: the code side is fully executed, the callers of `/discounts` outside the repository and the deploy topology are not.
  - Evidence: `./mvnw clean verify` → `Tests run: 160, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`, `ArchitectureTest` 4 passed, Flyway applies `V10` (Task 4 run; the changes after it are documents only). At `771e40e` the build fails to compile (`DiscountApi` no longer generated by 2.0.0); last green run 183 (`price-list-into-product.md:111`): 183 − 160 = the 23 tests of `discount`, deleted with the feature. `detect_test_tampering.sh`: 4 test files deleted, all under `com/commerce/discount`, no assertion loosened, no skip, no threshold changed. Blast radius: no reference to `com.commerce.discount`, `DISCOUNT_NOT_FOUND` nor `/discounts` left in `src` nor `docs/contracts`; `ArchitectureTest` skips an entry with no `hexagons`. Diff coverage: not run, the change deletes code; the one added executable line, `DROP TABLE discount;`, runs in every Spring test (Flyway log)
  - Findings: [Low] AR-1 to AR-4 of `docs/specs/catalog/area.md` still bind `discount`, AR-1 still lists `DISCOUNT_NOT_FOUND`, `product/spec.md` still names `discount` (Out of scope, BR-9, open question 1): text only, no behaviour; the hand moves of Léa Petit (BA). [Low] P-4 of `docs/architecture/patterns-grid.md:26` still names `discount`: raised to the architect
  - Deployment: `V10` is destructive and one-way, every discount stored is lost (ruled, Q2 (a), architect). The database is the H2 file `./.local/h2/commerce` (`application.yaml:7`), one process: no N−1 window in the repository's set-up. Redeploying the previous artifact after `V10` starts (a later migration is ignored by Flyway by default — not verified here) but its discount endpoints find no table
  - Not verified: callers of `POST /discounts` and `GET /discounts/{discountId}` outside this repository; the deploy topology (no deployment files in the repository); a rollback to the previous artifact
  - Conditions: in `## Blockers`
- Levels (close, step 2 to 4): the statements of `## Decisions` are about this removal (the code, `V10`, who does the hand moves) and live in the record and in `bounded-contexts.md`; no `AR-` nor `INV-` entry added or changed; candidate rejected: none. AR-1 to AR-4 and the area words coming down to `product` is the hand move of Léa Petit, not written here
- Conditions met (2026-10-20): 1. no client calls `/discounts` outside this repository (PO); 2. no stored discount is wanted, no backup (architect). No file changed by them: the verdict covers every change of the plan
- Carry-over report: Léa Petit (BA) archives or reassigns the hand-written sections of `docs/specs/catalog/discount/spec.md` (BR-1 to BR-10 included), brings AR-1 to AR-4 and the words of `docs/specs/catalog/area.md` down to `product/spec.md` (the area file deleted if left empty), then deletes `docs/specs/catalog/discount/` and takes its entry out of `docs/partition.yml`. P-4 of `docs/architecture/patterns-grid.md` raised to the architect
- [x] 2026-10-20

## Decisions

- 2026-10-20 · Removal of `discount`, proposal option 1 of the delta catalog 1.1.0 → 2.0.0 · its units were removed by the contract, R1 cannot hold with none · rejected: option 2, `discount` kept with no unit, against R1 (Claire Martin, architect)
- 2026-10-20 · Carried out through `partition-reshape`, with no new workshop nor challenge · the decision was validated by name through `partition-shape` (owner)
- 2026-10-20 · Q1: the code of `discount`, its tests and its generator execution in `pom.xml` are deleted · 2.0.0 no longer declares its operations (PO)
- 2026-10-20 · Q2: a new migration `V10` drops the table `discount`; `V8` is not edited · migrations fix forward · rejected: (b) the table kept, unread (architect)
- 2026-10-20 · Q3: Léa Petit does the hand moves after this plan · a person carries what is written by hand (BA)
- 2026-10-20 · P-4 of `patterns-grid.md` is raised, not rewritten · it is the architect's grid (architect)

- 2026-10-20 · Gate condition 1 met: no client calls `/discounts` outside this repository · the callers of 2.0.0 were the only unverified consumers (PO)
- 2026-10-20 · Gate condition 2 met: no stored discount is wanted, no backup before `V10` · `V10` drops them for good · rejected: a backup of `./.local/h2/commerce` (architect)

## Blockers

<!-- What is blocked · what unblocks it · who owns it. Delete the line when it clears. -->
