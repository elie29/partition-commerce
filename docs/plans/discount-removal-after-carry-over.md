# After the hand moves of the removal of `discount` — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-20

## Tasks

- [x] Task 1 — 2026-10-20 — `discount` out of `docs/partition.yml`; the reshape record and the gaps completed
- [x] Task 2 — 2026-10-20 — The catalog code cites the renumbered rules and `product`'s spec
- [x] Task 3 — 2026-10-20 — Budget measured again
- [x] Task 4 — 2026-10-20 — BR-23's `detail` in the code takes the 2.0.0 wording
- [x] Task 5 — 2026-10-20 — The catalog rule's source realigned through `partition-rules`
- [x] Task 6 — 2026-10-20 — Close: gate, levels, README

## Goal

Finish the removal recorded in `docs/architecture/reshapes/2026-10-20-discount-removal.md` (plan `docs/plans/discount-removal.md`, done; committed as `28acef3`) now that Léa Petit (BA) has done the hand moves, in the working tree: AR-1 to AR-4 of the catalog area file brought down as BR-27 to BR-30 of `docs/specs/catalog/product/spec.md` (AR-5 was already BR-26), the area words brought down into its Language, `docs/specs/catalog/area.md` deleted, and `docs/specs/catalog/discount/` deleted.

## Task 1 — `discount` out of `docs/partition.yml`; the reshape record and the gaps completed

- Scope: `docs/partition.yml` (the entry `discount` goes: its spec folder is deleted); `docs/architecture/reshapes/2026-10-20-discount-removal.md` (`Rules`: where AR-1 to AR-4 and the words went; `After`: the entry gone); `docs/specs/catalog/bounded-contexts.md` (`Gaps detected`: the gaps the hand moves and `28acef3` resolve marked "resolved on 2026-10-20 (version 2.0.0)", never erased). Not touched: `product/spec.md` (the BA's), the code
- Verification: `ls docs/specs/catalog/; grep -n "id: discount" docs/partition.yml; node .claude/skills/partition-budget/scripts/budget.mjs --check; ./mvnw clean verify -Dtest=ArchitectureTest`
- Expected: `bounded-contexts.md product`; the grep finds nothing (exit 1); the budget lists three bounded contexts, no `discount`, no area file of `catalog`, and passes its schema check (exit 0); `Tests run: 4, Failures: 0`, `BUILD SUCCESS`
- Decisions that bind it: the removed context leaves `docs/partition.yml` once a person has deleted its spec folder (`partition-reshape`)
- Done: the entry `discount` out of `docs/partition.yml`; the record's `Rules` (BR-1 to BR-10 gone with the folder; AR-1 to AR-4 → BR-27 to BR-30; the words → `product`'s Language) and `After`; in `bounded-contexts.md`, four gaps marked resolved or partly resolved — the code and the folder, the code / spec divergence (P-4 still open with the architect), the hand-written lines (still untrue in `product/spec.md`: `Out of scope`, BR-9, the `Callers` line at 1.1.0, open question 1), the currency rule (held by `product` BR-13 alone)
- Result: `ls` → `bounded-contexts.md product`; grep: nothing (exit 1); `budget.mjs --check` exit 0, three bounded contexts, no `discount`, "Area catalog: no area file" (`product` 7 925, 25 % ok, spec 5 296, area file none); `ArchitectureTest` `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` — as expected
- [x] 2026-10-20

## Task 2 — The catalog code cites the renumbered rules and `product`'s spec

- Scope, to be confirmed by the architect and the TL: the comments of `src/main/java/com/commerce/product/**`, `src/test/java/com/commerce/product/**`, `ClockConfiguration.java:7` and `pom.xml:143,147` (the `product` generator execution) that cite AR-1 to AR-5 of the deleted catalog area file or `docs/specs/catalog/area.md` — 51 lines in 35 files of `product` plus those three; the 2 exception messages that carry a number (`BodyUnreadable.java:7` "AR-1: the body cannot be read", `RequestRefused.java:12` "AR-1: the request is refused"). Not touched: `V7__create_catalog.sql` (a merged migration is never edited, `backend-database`); the AR-n of `invoice` and `payment`, which cite `billing`'s area file, still there
- Mapping, from the `Source` column of `product/spec.md`: AR-1 → BR-27, AR-2 → BR-28, AR-3 → BR-29, AR-4 → BR-30, AR-5 → BR-26; `docs/specs/catalog/area.md` (Blank, Character) → `docs/specs/catalog/product/spec.md`, its Language
- Also, by the architect's decision: P-4 of `docs/architecture/patterns-grid.md:26` drops `discount`; and in `bounded-contexts.md`, the gaps the BA's lines and P-4 resolve are marked resolved
- Answer (architect, TL, 2026-10-20): as proposed — the mapping, the area-file references to `product/spec.md`, the two messages; `V7` and `billing`'s AR-n untouched
- Verification: `grep -rn "AR-[1-5]\b\|catalog/area.md" src/main/java/com/commerce/product src/test/java/com/commerce/product src/main/java/com/commerce/ClockConfiguration.java pom.xml`, then the diff read line by line against the mapping; `grep -n discount docs/architecture/patterns-grid.md`; `./mvnw clean verify`
- Expected (written 2026-10-20, before the run): the grep finds nothing (exit 1); every changed line only renumbers AR-n to its BR-n by the mapping or names `docs/specs/catalog/product/spec.md` for the area file; P-4 names no `discount`; `Tests run: 160, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`
- Decisions that bind it (2026-10-20): the mapping AR-1 → BR-27, AR-2 → BR-28, AR-3 → BR-29, AR-4 → BR-30, AR-5 → BR-26, `catalog/area.md` → `product/spec.md`, the two messages, `V7` and `billing`'s AR-n untouched (architect, TL); P-4 drops `discount` (architect)
- Done: 50 lines in 37 files (28 main classes of `product`, 7 test files, `ClockConfiguration.java:7`, `pom.xml:143,147`), each a renumbering by the mapping; "AR-1 to AR-3" → "BR-27 to BR-29", "AR-1 to AR-4" → "BR-27 to BR-30", "AR-1 to AR-5" → "BR-26 to BR-30" (AR-5 is BR-26); `GetProductPriceTest.java:28` loses "of the area", false now; `ClockConfiguration.java:7` names `docs/specs/catalog/product/spec.md`, BR-26 for "`docs/specs/catalog/`, AR-5"; the messages "BR-27: the body cannot be read", "BR-27: the request is refused". P-4: `discount` out of its contexts; two dated traces keep it ("`discount` dropped, removed on 2026-10-20", "`discount` dropped with its removal"), as the grid keeps `price-list`. `bounded-contexts.md`: the gaps of the BA's lines, BR-23 and P-4 marked resolved
- Result: the grep finds nothing (exit 1); the added lines cite BR-26 ×4, BR-27 ×21, BR-28 ×14, BR-29 ×11, BR-30 ×3, `catalog/product/spec.md` ×6, no AR-n; `Tests run: 160, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` — as expected, but for P-4: "names no `discount`" was expected; it keeps two dated traces of it
- [x] 2026-10-20

## Task 3 — Budget measured again

- Scope: `docs/budget/2026-10-20.md` (`partition-budget`: a later record the same day replaces it)
- Verification: `node .claude/skills/partition-budget/scripts/budget.mjs`
- Expected (written 2026-10-20, before the run; Task 2 touches code only, which the gauges do not count): three bounded contexts, no `discount`; `product` 7 925 (25 % ok, spec 5 296, no area file), `invoice` 6 347, `payment` 7 416, as Task 1's `--check` measured; the plan in progress read on resume well under its whole file
- Decisions that bind it: none
- Result: three bounded contexts, no `discount`; `product` 7 925 (25 % ok, spec 5 296, area file none), `invoice` 6 347, `payment` 7 416; the plan read on resume 642 of 1 394; `docs/budget/2026-10-20.md` replaced, the earlier figures of the day quoted in it — as expected
- [x] 2026-10-20

## Task 4 — BR-23's `detail` in the code takes the 2.0.0 wording

- Scope: `src/main/java/com/commerce/product/inbound/PriceListErrors.java:69` (the `detail` answered), `src/test/java/com/commerce/product/GetProductPriceTest.java:34` (`NOT_FOUND_DETAIL`), `src/main/java/com/commerce/product/application/PriceNotFound.java:10` (the message). Nothing else: the `errors` item's message ("No price applies for this product in this price list.") is not BR-23's `detail`
- Verification: `grep -rn "in that price list" src`; `./mvnw clean verify`
- Expected (written 2026-10-20, before the edit): the grep finds nothing (exit 1); `PriceListErrors` answers "No such product, or no price for it.", the test asserts it; `Tests run: 160, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`
- Decisions that bind it: 2026-10-20 · the three lines take the 2.0.0 wording "No such product, or no price for it", the contract's own 404 description (PO, TL)
- Result: the grep finds nothing (exit 1); `PriceListErrors.java:69` "No such product, or no price for it.", `GetProductPriceTest.java:34` the same, `PriceNotFound.java:10` "BR-23: no such product, or no price for it"; `GetProductPriceTest` 8 passed; `Tests run: 160, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` — as expected
- [x] 2026-10-20

## Task 5 — The catalog rule's source realigned through `partition-rules`

- Scope: `.apm/instructions/backend-api-contract-catalog.instructions.md` only (its deployed copy `.claude/rules/backend-api-contract-catalog.md` is rewritten by the TL's `apm install`, after the plan): `src/main/java/com/commerce/discount/**` out of `applyTo`, `discount` out of the executions (and of the `description`), AR-3 → BR-29 and AR-1 → BR-27 of `product`
- Verification: the TL's validation of the lines shown, then `git diff --stat -- .apm/instructions/` and `grep -n "discount\|AR-[0-9]\|applyTo\|description" .apm/instructions/backend-api-contract-catalog.instructions.md`
- Answer (TL, 2026-10-20): the lines validated; the singular kept; the `description` "Backend API contract of catalog: how product checks its requests"; `apm install` by the TL once the plan is done
- Expected (written 2026-10-20, before the edit): one file changed, 4 lines (`description`, `applyTo`, the generator line, the `RequestBodyAdvice` line); the grep shows `applyTo: 'src/main/java/com/commerce/product/**'`, the new `description`, no `discount`, no AR-n
- Decisions that bind it: 2026-10-20 · the rule changes through its source with `partition-rules`; the TL sees the lines first and runs `apm install` once the plan is done (TL); the singular, the new `description` (TL)
- Result: `1 file changed, 4 insertions(+), 4 deletions(-)`; the grep shows the new `description` (line 2) and `applyTo: 'src/main/java/com/commerce/product/**'` (line 3) only: no `discount`, no AR-n — as expected. The deployed copy `.claude/rules/backend-api-contract-catalog.md` keeps the old text until the TL's `apm install`
- [x] 2026-10-20

## Task 6 — Close: gate, levels, README

- Scope: `partition-gate` from `28acef3` to the working tree; `README.md:42` ("The rules are in `docs/specs/catalog/product/spec.md` and `docs/specs/catalog/area.md`": the area file is deleted) with `partition-readme`
- Verification: the gate's verdict
- Expected: SHIP or SHIP WITH CONDITIONS
- Decisions that bind it: none
- Done: `README.md:42` "The rules are in `docs/specs/catalog/product/spec.md`." (`partition-readme`); the README carried no other reference to the area file nor the 404 wording
- Found before the gate: BR-23 of `product/spec.md` now quotes the 2.0.0 `detail` "No such product, or no price for it" (BA); the code still answers "No such product, or no price for it in that price list." (`PriceListErrors.java:69`), asserts it (`GetProductPriceTest.java:34`, `NOT_FOUND_DETAIL`) and carries it in a message (`PriceNotFound.java:10`): a business rule the code breaks, a Blocker for the gate
- Found too: the rule file `.claude/rules/backend-api-contract-catalog.md` still names the generator execution `discount` (gone, `28acef3`) and cites AR-3 and AR-1 (now BR-29, BR-27 of `product`); a rule file changes through `partition-rules`, by the TL: reported, not changed. Both settled: Task 4 and Task 5
- Gate (`partition-gate`, 2026-10-20), target `28acef3` → working tree, 47 files +99/−233, plus the plan (untracked); one bounded context, `product`, no contract between two. **SHIP**, confidence High: every code change is a comment, an exception message or the 404 `detail` of BR-23, all executed by the full suite.
  - Evidence: `./mvnw clean verify` after the last code change (Task 4) → `Tests run: 160, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`, `GetProductPriceTest` 8 passed, asserting the new `detail`; Task 5 changed a rule source only. `detect_test_tampering.sh`: no test deleted, skipped or loosened, no assertion removed (its "deleted" hits are `area.md` and `discount/spec.md`, documents). Blast radius: the exception messages of `BodyUnreadable`, `RequestRefused`, `PriceNotFound` are never answered nor logged (the handlers answer fixed sentences and `Fault.message()`; BR-28 logs no message); the `detail` of `PriceListErrors.java:69` reaches the callers of `getProductPrice`, as contract 2.0.0's 404 description says (PO, TL). The "auth / permission" heuristic matched the words of P-4 and of a message, no logic. No migration, no configuration key, no dependency changed (`pom.xml`: two comments). Diff coverage: not run; the one changed answer is asserted by `GetProductPriceTest`
  - Spec: BR-23 and the code agree on "No such product, or no price for it"; BR-26 to BR-30 cited where AR-5, AR-1 to AR-4 were
  - Findings: [Low] P-4 of `docs/architecture/patterns-grid.md:26` still cites "AR-1 (`docs/specs/catalog/area.md`)" and AR-2, the file deleted (now BR-27, BR-28 of `product`): a dangling reference in the grid, the architect's to settle; the dated notes of `discount` stay by their decision
  - Deployment: nothing to migrate; callers of `getProductPrice` see the 404 `detail` "No such product, or no price for it." The deployed rule copy `.claude/rules/backend-api-contract-catalog.md` keeps the old text until the TL's `apm install`
  - Not verified: callers of `getProductPrice` outside this repository that would read the `detail` text
- Levels (close, steps 2 to 4): BR-23's wording is in `product/spec.md` (BA); the catalog rule in its source (TL); the citations are code. No `AR-` nor `INV-` entry added or changed; candidate rejected: none
- README: `README.md:42` realigned before the gate; the verdict covers it
- Carry-over report: nothing left to carry; the deployed rule waits for the TL's `apm install`; P-4's dangling AR-1, AR-2 to the architect
- [x] 2026-10-20

## Decisions

- 2026-10-20 · The hand moves of the removal are done by Léa Petit (BA): AR-1 to AR-4 → BR-27 to BR-30 of `product`, the area words into its Language, `catalog/area.md` and `catalog/discount/` deleted · reported by the owner, read in the working tree

- 2026-10-20 · Task 2 as proposed: AR-1 → BR-27, AR-2 → BR-28, AR-3 → BR-29, AR-4 → BR-30, AR-5 → BR-26, `catalog/area.md` → `product/spec.md`, the two exception messages renumbered; `V7` and `billing`'s AR-n untouched · the rules live in `product/spec.md` now; a merged migration is never edited (architect, TL)
- 2026-10-20 · P-4 of `patterns-grid.md` drops `discount` · the context is gone (architect)
- 2026-10-20 · `product/spec.md` realigned by the BA: `Out of scope` "the discounts, moved to another system with catalog 2.0.0", BR-9 "its prices are not reached", the `Callers` line at 2.0.0, open question 1 without `discount`, BR-23 quotes "No such product, or no price for it" · the hand-written lines 2.0.0 left untrue (BA)

- 2026-10-20 · BR-23's `detail` in the code takes the 2.0.0 wording "No such product, or no price for it", in this plan · it is the contract's own 404 description (PO, TL)
- 2026-10-20 · `.apm/instructions/backend-api-contract-catalog.instructions.md` through `partition-rules`: `discount` out of `applyTo` and of the executions, AR-3 → BR-29, AR-1 → BR-27 of `product`; lines shown first; `apm install` by the TL once the plan is done (TL)
- 2026-10-20 · `product`'s `Out of scope` "moved to another system" stands · the architect said the discounts move to another system, out of this project (BA)
- 2026-10-20 · P-4 keeps its two dated notes of `discount` · history of the grid (architect)
- 2026-10-20 · The lines of the catalog rule's source validated; the singular kept (one execution left); its `description` "Backend API contract of catalog: how product checks its requests" · `price-list` and `discount` are gone · rejected: the plural, the old `description` (TL)

## Blockers

<!-- What is blocked · what unblocks it · who owns it. Delete the line when it clears. -->
