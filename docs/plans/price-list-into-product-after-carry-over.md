# After the carry-over of `price-list` into `product` — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-20

## Tasks

- [x] Task 1 — 2026-10-20 — `price-list` out of `docs/partition.yml`; the reshape record completed
- [x] Task 2 — 2026-10-20 — The moved code and `product`'s reason cite the renumbered rules (D7)
- [x] Task 3 — 2026-10-20 — Budget measured again
- [x] Task 4 — 2026-10-20 — Close: gate, levels, README

## Goal

Finish the merge recorded in `docs/architecture/reshapes/2026-10-20-price-list-into-product.md` (plan `docs/plans/price-list-into-product.md`, done; the merge committed as `ccb64ba`) now that Léa Petit (BA) has carried the hand-written sections of `price-list` into `docs/specs/catalog/product/spec.md` — its rules renumbered BR-10 to BR-25, AR-5 brought down as BR-26, Blank and Character brought down into its Language, `price-list` taken out of the area file — the owner has realigned the README, and a person has deleted `docs/specs/catalog/price-list/`.

## Task 1 — `price-list` out of `docs/partition.yml`; the reshape record completed

- Scope: `docs/partition.yml` (the entry `price-list` goes: its spec folder is deleted), `docs/architecture/reshapes/2026-10-20-price-list-into-product.md` (the precondition on rules ticked: AR-5 → BR-26 of `product`, the rules renumbered BR-10 to BR-25; `After`: the entry gone)
- Verification: `ls docs/specs/catalog/; grep -n "id: price-list" docs/partition.yml; node .claude/skills/partition-budget/scripts/budget.mjs --check; ./mvnw clean verify -Dtest=ArchitectureTest`
- Expected: `area.md bounded-contexts.md discount product`; the grep finds nothing; the budget lists no `price-list` and passes its schema check; `Tests run: 4, Failures: 0`, `BUILD SUCCESS`
- Decisions that bind it: the absorbed context leaves `docs/partition.yml` once a person has deleted its spec folder (`partition-reshape`)
- Result: `ls` → `area.md bounded-contexts.md discount product`; grep: nothing (exit 1); `budget.mjs --check` exit `0`, four bounded contexts, no `price-list` (`product` 8 148, 25 % ok, spec 4 367; `discount` 5 788, the area file 1 216 → 1 152); `ArchitectureTest` `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`; the record's precondition on rules and its `After` line ticked — as expected
- [x] 2026-10-20

## Task 2 — The moved code and `product`'s reason cite the renumbered rules (D7)

- Scope, to be confirmed by the architect and the TL: the comments citing `price-list`'s BR-n in the 19 moved classes of `src/main/java/com/commerce/product/**`, `JsonBody.java:117` and the 5 moved test files; the 7 exception messages that carry a number (`PriceListNotFound.java:10` BR-9, `PriceNotFound.java:10` BR-14, `PriceRefused.java:14` BR-3, `Currency.java:29` BR-4, `Money.java:8` BR-2, `PriceList.java:17` BR-5, `PriceList.java:33` BR-3) — no answer carries them (each `detail` is a fixed sentence, `PriceListErrors`) and no test asserts them; `product`'s reason in `docs/partition.yml` ("BR-1 of `price-list`, until renumbered" → BR-10). Not touched: AR-5 in the code, which resolves through the note `area.md:32`, as AR-3 of `billing` did
- Mapping, from the `Source` column of `product/spec.md`: BR-1 → 10, 2 → 11, 3 → 12, 4 → 13, 5 → 14, 6 → 15, 7 → 16, 8 → 17, 9 → 18, 12 → 21, 13 → 22, 14 → 23, 15 → 24, 16 → 25; BR-10 → 19 by its content only: BR-19 does not say "was BR-10 of `price-list`"
- Answer (architect, TL, 2026-10-20): points 1 to 5 as proposed — the comments, the 7 messages, the reason, AR-5 untouched; the mapping 1→10 … 10→19, 12→21 … 16→25
- Verification: `grep -rn "BR-[0-9]*" <the scope>` read line by line against the mapping; `grep -n "reason:" docs/partition.yml`; `./mvnw clean verify`
- Expected (written 2026-10-20, before the run): every BR-n of the scope names `product`'s number for the rule it meant; no `BR-1` to `BR-9` left that meant a rule of `price-list`; `product`'s reason "BR-10"; `Tests run: 183, Failures: 0`, `BUILD SUCCESS`
- Decisions that bind it: D7, the comments stay until Léa Petit renumbers (architect, TL, 2026-10-20); the scope and the mapping above (architect, TL, 2026-10-20)
- Done: 25 files, +52/−52, each line a renumbering: 19 main classes and `JsonBody.java:117`, the 5 test files' comments, the 7 messages (`PriceListNotFound` BR-18, `PriceNotFound` BR-23, `PriceRefused` BR-12, `Currency` BR-13, `Money` BR-11, `PriceList` BR-14 and BR-12); where a line named "the spec of `price-list`", it names "the spec of `product`", the spec that now holds the rule; `product`'s reason "(BR-10)". Not touched: AR-5 (`SetPriceUseCase.java:51`, `SetPriceTest.java:32`), resolving through its note
- Result: the added lines cite BR-10 to BR-19 and BR-21 to BR-24, each the mapping's image of the number it replaced (BR-25, once BR-16, was cited nowhere); `grep "BR-[1-9]\b"` on the moved files: nothing (exit 1); the reason reads "one price per product and list holds at every write (BR-10)"; `./mvnw clean verify` → `Tests run: 183, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` — as expected
- [x] 2026-10-20

## Task 3 — Budget measured again

- Scope: `docs/budget/2026-10-20.md` (`partition-budget`: a later record the same day replaces it)
- Verification: `node .claude/skills/partition-budget/scripts/budget.mjs`
- Expected (written 2026-10-20, before the run): four bounded contexts, no `price-list`; `product` 8 148 _Always_ (25 % ok) and `discount` 5 788, as Task 1's `--check` measured (Task 2 touched no spec, area file or rule); `invoice` 6 347, `payment` 7 416; the plan in progress read on resume well under its whole file
- Decisions that bind it: none
- Result: four bounded contexts, no `price-list`; `product` 8 183 (26 % ok, spec 4 402), `discount` 5 788, `invoice` 6 347, `payment` 7 416; the plan read on resume 276 of 1 673; `docs/budget/2026-10-20.md` replaced, the earlier figures of the day quoted in it — as expected but for `product`: 8 183, not 8 148 — the BA's corrections to `product/spec.md` (BR-19's trace, the BR-20 line) came after Task 1's measure, +35
- [x] 2026-10-20

## Task 4 — Close: gate, levels, README

- Scope: as `partition-plan` _Close_ says; the README realigned by the owner (line 42: no `price-list/spec.md`), checked against the specs
- Verification: `partition-gate` on the changes of this plan, from `ccb64ba`
- Expected: SHIP or SHIP WITH CONDITIONS
- Decisions that bind it: none
- **Gate run 1, 2026-10-20 — SHIP WITH CONDITIONS, confidence High.** Target: `ccb64ba` (the merge, committed by the owner) to the working tree, 32 files: the carry-over (`product/spec.md`, `area.md`, `README.md`, `price-list/spec.md` deleted, by Léa Petit and a person), `docs/partition.yml`, the renumbered comments and 7 messages (Task 2), the reshape record, the budget record, this plan; no behaviour, no migration, no configuration
  - Evidence: `./mvnw clean verify` → `Tests run: 183, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` (run after Task 2; nothing under `src` changed since); `detect_test_tampering.sh --range ccb64ba` → its one hit, "test file deleted", is `docs/specs/catalog/price-list/spec.md`, a spec matched by its name, not a test; the 7 messages reach no answer (fixed `detail`s) and no test asserts them (Task 2)
  - Read against the code: `product`'s carried-over spec, BR-10 to BR-26, the rules of `price-list` and AR-5 as they were, renumbered; BR-19 traced, BR-20 explained (BA); the README's line 42 names `product/spec.md` and `discount/spec.md`
  - Findings: [Medium] `docs/specs/catalog/discount/spec.md:52` BR-4: "A currency is a known ISO 4217 code, as `price-list`'s BR-4 says": a business rule of `discount` cites a spec deleted, the rule now BR-13 of `product`; hand-written. [Low] `discount/spec.md:24` (`Out of scope`, hand-written) and `:43` (`Source parts owned`, derived: "`Money` is also used by `setPrice` and `getProductPrice` of `price-list`") name `price-list`. [Low] `src/main/java/com/commerce/product/domain/Price.java:20`: "Applies (`docs/specs/catalog/price-list/spec.md`)", a path deleted; the word is in `product`'s Language. Not a finding: `docs/designs/catalog-products-and-price-lists.md:3`, a design of a closed feature, dated as written
  - Skipped: every runtime class — no behaviour changed
  - Not verified: none beyond run 1 of the merge plan (G1 by the TL on `ccb64ba`'s code)
  - Conditions: C1 — `discount/spec.md:52` BR-4 and `:24` realigned by a person (BA), its line `:43` by the assistant once C1's owner agrees, through `partition-spec`; C2 — `Price.java:20` names `product`'s Language, by the assistant once confirmed (TL)
- C1 met (2026-10-20): the BA realigned `discount/spec.md:52` (BR-4 cites BR-13 of `product`, "was BR-4 of `price-list`") and `:24` (`Out of scope` names `product` for the products, the price lists and the prices); with her agreement the assistant rewrote `:43`: "`Money` is also used by `setPrice` and `getProductPrice` of `product`"
- C2 met (2026-10-20): the TL confirmed; `Price.java:20` reads "Applies (`docs/specs/catalog/product/spec.md`)"
- **Gate run 2, 2026-10-20 — SHIP, confidence High.** Target: `ccb64ba` to the working tree, 34 files, +115/−205: run 1's and C1, C2. Evidence: `./mvnw clean verify` → `Tests run: 183, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`; `detect_test_tampering.sh --range ccb64ba` → its one hit is still the deleted spec, matched by its name; no assertion removed, skipped or loosened; `grep -rn "price-list/spec.md\|of \`price-list\`" docs/specs src README.md`, the carry-over's traces aside: only `bounded-contexts.md:58,70,75,77`, the `Kept against the criteria` line and resolved `Gaps detected`, dated records of the decomposition. Run 1's findings resolved by C1 and C2; none left. The verdict covers every change of the plan; the plan closes
- Levels (2026-10-20): the renumbering is held by `product/spec.md` (each rule's `Source`: "was BR-n of `price-list`"; BR-20 explained), and AR-5 resolves through its note `area.md:32`; `discount`'s BR-4 cites BR-13 of `product` (BA). No `AR-` or `INV-` entry added or changed; no candidate. The gap of `bounded-contexts.md:69` ("a known ISO 4217 code" in two specs and two areas) stays as the architect left it, a citation
- README: realigned by the owner in this change (line 42: `product/spec.md`, `discount/spec.md`), checked against the specs as they are; nothing for `partition-readme` to add: API, configuration and commands unchanged. C4: unchanged since the merge plan
- [x] 2026-10-20

## Decisions

- 2026-10-20 · The follow-up of the merge in a plan of its own: `docs/plans/price-list-into-product.md` is done and is never resumed · `partition-plan`
- 2026-10-20 · The moved code's comments, its 7 numbered exception messages and `product`'s reason cite the renumbered rules; AR-5 stays, resolving through its note · architect, TL
- 2026-10-20 · The mapping 1→10 … 10→19, 12→21 … 16→25 · architect, TL
- 2026-10-20 · `product/spec.md`: BR-19 traces "was BR-10 of `price-list`"; the table in numerical order; BR-20 not used, a line says why · BA
- 2026-10-20 · C1: `discount`'s BR-4 cites BR-13 of `product`; its `Out of scope` names `product`; its `Money` row names `setPrice` and `getProductPrice` of `product`, written by the assistant · BA
- 2026-10-20 · C2: `Price.java:20` points "Applies" to `product`'s Language · TL

## Blockers

None.
