# Discounts of the catalog — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-20

## Tasks

- [x] Task 1 — 2026-10-20 — The spec of `discount` takes the business rules and the answers
- [x] Task 2 — 2026-10-20 — Declare the form and layer map of `discount` (thin)
- [x] Task 3 — 2026-10-20 — Challenge the spec (partition-attack), then design the discounts
- [x] Task 4 — 2026-10-20 — `createDiscount`
- [x] Task 5 — 2026-10-20 — `getDiscount`
- [x] Task 6 — 2026-10-20 — Close: gate, levels, README, C4
- [x] Task 7 — 2026-10-20 — Gate finding 1: the untested guards of `discount`

## Goal

Deliver the two operations of `docs/contracts/catalog.openapi.json` 1.0.0 (5016f59) under `tag:Discount`, `createDiscount` and `getDiscount`, in the bounded context `discount` of the area `catalog`, as `docs/specs/catalog/bounded-contexts.md` decided on 2026-10-20. The durable spec `docs/specs/catalog/discount/spec.md` comes first; the code implements it under `docs/designs/catalog-discounts.md`, AR-1 to AR-4, INV-DATA-001 and the project's rules. The commit before this plan's first task is `ad514d6` (the decomposition, committed by the owner).

## Task 1 — The spec of `discount` takes the business rules and the answers

- Scope: `docs/specs/catalog/discount/spec.md`, `docs/specs/catalog/area.md` (AR-1), `docs/specs/catalog/bounded-contexts.md` (gaps), via `partition-spec`
- Verification: `grep -c 'TO FILL' docs/specs/catalog/discount/spec.md; grep -c '^| BR-' docs/specs/catalog/discount/spec.md`
- Expected: `0` banners; `10` BR rows, each with its trigger, its effect and its source
- Decisions that bind it: every answer of 2026-10-20 in `## Decisions`
- Done 2026-10-20. Output: `TO FILL: 0`, `BR rows: 10`. As expected. AR-1 names `DISCOUNT_NOT_FOUND`; the Language entry Discount; the Callers row; three gaps of `bounded-contexts.md` marked resolved, one added (the currency rule in two specs)

## Task 2 — Declare the form and layer map of `discount` (thin)

- Scope: `docs/partition.yml`, via `partition-hexagon`
- Verification: `grep -n -A1 'form:' docs/partition.yml | tail -2`
- Expected: `discount` declared `form: thin` with the architect's reason, and a layer map `application`, `inbound`, `outbound` with `inbound -> application`, `outbound -> application`
- Decisions that bind it: 2026-10-20 · `discount` thin (Claire Martin, architect)
- Done 2026-10-20. Output: line 118 `form: thin`, line 119 its reason naming Claire Martin; `allowed` lines 124–126 as expected. As expected

## Task 3 — Challenge the spec (partition-attack), then design the discounts

- Scope: `docs/specs/catalog/discount/spec.md` (findings only), `docs/designs/catalog-discounts.md`
- Verification: `grep -n "^## " docs/designs/catalog-discounts.md; grep -n -A3 "## Open questions" docs/specs/catalog/discount/spec.md`
- Expected: the design's sections print; the spec's open questions hold only finding 1, accepted for this version by the PO; the design's two questions answered
- Decisions that bind it: 2026-10-20 · findings 2 to 5 settled (architect, TL, BA); finding 1 accepted for this version (PO)
- 2026-10-20: attack run, five findings written into the spec; 2 to 5 settled and written (BR-5, BR-7), 1 left open, accepted for this version. Design written: `docs/designs/catalog-discounts.md`. Waiting: its validation and its two questions (Blockers)
- Done 2026-10-20. P-4 extended to `discount` in `docs/architecture/patterns-grid.md` (architect); `.apm/instructions/backend-api-contract-catalog.instructions.md` extended to `discount` (TL; `apm install` by the TL once the plan is done); the design's questions marked settled, design validated (architect, TL). Output: the design's seven `##` sections print (lines 9–48); the spec's `Open questions` holds finding 1 only. As expected

## Task 4 — `createDiscount`

- Scope: `pom.xml` (execution `discount`), `src/main/resources/db/migration/V8__create_discount.sql`, `src/main/java/com/commerce/discount/**`, `src/test/java/com/commerce/discount/**`
- Verification: `./mvnw -q clean test`
- Expected: red first (the new tests fail or do not compile before any code); then `CreateDiscountTest` passes with the design's _Acceptance_ cases for it, `ArchitectureTest` passes on `discount`'s layer map, the 150 earlier tests still pass
- Decisions that bind it: 2026-10-20 · `discount` thin, code under `src/main/java/com/commerce/discount/**` (architect, TL); BR-4 written for `discount`, citing `price-list`'s, separate copies on purpose (architect); the body one group (BR-9, TL); a retry creates another discount (TL); `minimumOrder` governs nothing (BA); no authentication, no limit (PO); the design validated as written (architect, TL); P-4: its own `DiscountErrors` (architect); `backend-api-contract-catalog` binds `discount`'s code (TL)
- In progress 2026-10-20. Red first: `DiscountApiTest`, `DiscountClient`, `CreateDiscountTest` (16 tests) written before any code; `./mvnw -q test -Dtest=CreateDiscountTest`: 16 run, 12 failures, 4 errors (404 on `/discounts`, no `discount` table). Next: the generator execution, V8, the code
- Done 2026-10-20. `pom.xml` execution `discount`; `V8__create_discount.sql`; application `CreateDiscountUseCase`, `Discounts`, `DiscountView`, `MinimumOrder`, `KnownCurrency`, `Identifiers`, `Fault`; inbound `DiscountController`, `DiscountRequestCheck`, `DiscountBodyCheck`, `JsonBody`, `DiscountErrors`, `DiscountWebConfiguration`, `RequestRefused`, `BodyUnreadable`; outbound `JdbcDiscounts`. The error model is the generator's `ListProducts400Response`, as in `price-list`. `DiscountErrors.framesOf` stops on a cause chain that loops back (the gate's follow-up of 2026-10-19 on `ProductErrors`, `PriceListErrors`, not carried to those two here). Output: `./mvnw -q clean test` no error printed; surefire 18 classes, 166 tests, 0 failures, 0 errors, 0 skipped (`CreateDiscountTest` 16, `ArchitectureTest` 4). As expected

## Task 5 — `getDiscount`

- Scope: `src/main/java/com/commerce/discount/**`, `src/test/java/com/commerce/discount/**`
- Verification: `./mvnw -q clean test`
- Expected: `GetDiscountTest` passes with the design's _Acceptance_ cases for it, the 500 included; the earlier tests still pass
- Decisions that bind it: 2026-10-20 · `DISCOUNT_NOT_FOUND` for an identifier not a UUID or unknown (architect, AR-1, AR-3); the design validated as written (architect, TL); P-4 (architect)
- Done 2026-10-20. Red first: `GetDiscountTest` (6 tests) written before the code; `./mvnw -q test-compile` exit 1, `cannot find symbol … method find(java.lang.Object)`. Then `Discounts.find`, `GetDiscountUseCase`, `DiscountNotFound`, `JdbcDiscounts.find` (a stored currency read as kept), `DiscountController.getDiscount`, `DISCOUNT_NOT_FOUND` in `DiscountErrors`. Output: `./mvnw -q clean test` no error printed; surefire 19 classes, 172 tests, 0 failures, 0 errors (`GetDiscountTest` 6, `CreateDiscountTest` 16). As expected

## Task 6 — Close: gate, levels, README, C4

- Scope: as `partition-plan` _Close_ says
- Verification: the `partition-gate` verdict, on `ad514d6` to the working tree
- Expected: SHIP or SHIP WITH CONDITIONS on every change of the plan
- Decisions that bind it: none
- Corrected 2026-10-20: the verification named `5d9808f` as the base; the commit before the plan's first task is `ad514d6` (gate finding)
- Gate, 2026-10-20, run by a reviewer that did not write the code (a separate agent), on `ad514d6` to the working tree, untracked files included: **SHIP WITH CONDITIONS**, confidence high. Evidence: `collect_change_context.sh` (sensitive paths `docs/partition.yml`, `pom.xml`; no deletion); `detect_test_tampering.sh` (one hit, the edited P-4 row of `patterns-grid.md`, a false positive); `./mvnw -q clean … jacoco … test … report` build OK, 19 classes, 172 tests, 0 failures, 0 errors, 0 skipped; new `discount` code 197 of 201 lines covered (98.0%), 85 of 94 branches (JaCoCo CSV; `diff_coverage.py` does not see untracked files). No Blocker, no High. Findings, all Low: (1) untested guards: a non-string `minimumOrder/currency` or `period/start`, the loop guard of `framesOf`, the AR-2 `toString`s (`JsonBody.java:76`, `DiscountErrors.java:90`, `DiscountView.java:14`, `MinimumOrder.java:12`) → Task 7; (2) `ISO_OFFSET_DATE_TIME` accepts a date-time without seconds, as `price-list` does; no effect here (BR-2) → Blockers, TL; (3) P-4's "Two contexts answer the same envelope today" now three (`patterns-grid.md:26`), AR-2's trigger names `product` or `price-list` only while its Binds holds `discount` (`area.md:37`, before this plan), the base named here → corrected above, the first two under Blockers (architect; TL, architect); (4) the deployed rule not yet covering `discount` → Blockers (TL, `apm install`). Conditions: README realigned (step 5 of this close), `apm install` (TL). Not verified: a baseline run at `ad514d6`, a probe of the running app, a database other than H2, load, lint (none configured)
- Close, 2026-10-20. Step 1: gate above; finding 1 → Task 7 (done); the rest under `## Blockers`. Steps 2–4, levels: every business rule, word and contract of this plan is in `docs/specs/catalog/discount/spec.md` (written during Tasks 1 and 3, validated by the BA, TL, architect, PO); the shared ones in `docs/specs/catalog/area.md`; P-4's extension in `docs/architecture/patterns-grid.md`; how `discount` checks its requests in `.apm/instructions/backend-api-contract-catalog.instructions.md` (TL). Nothing true for every feature is left only in the design or here. Area entries changed by this plan: AR-1 (binds `discount`, names `DISCOUNT_NOT_FOUND`), AR-2, AR-3, AR-4 (bind `discount`), the bounded-contexts table (`discount`); none added; no word added to the area (Discount stays in `discount`'s spec: one bounded context uses it). Invariant register: none added or changed; candidates: none — INV-DATA-001 already holds the minimum order's amount (`docs/specs/invariants.md`, INV-DATA-001, read 2026-10-20); the currency rule, which spans `catalog` and `billing`, stays a gap of `docs/specs/catalog/bounded-contexts.md` by the architect's decision of 2026-10-20. Step 5: README realigned (`partition-readme`: the tag `Discount`, `createDiscount`, `getDiscount`, `DISCOUNT_NOT_FOUND`, the `discount` spec, `V8`); C4: component `discount` and `discount -> h2: SQL` added to `docs/architecture/model.yml`; `--check`: 12 elements, 13 relations, 7 views, no error; `c4.drawio` regenerated. Step 6: these writes and Task 7 need a second gate run
- Gate, second run, 2026-10-20, by a reviewer that did not write the code (a separate agent), on `ad514d6` to the working tree, untracked files included, every change of the plan and of this close covered: **SHIP WITH CONDITIONS**, confidence high. Evidence: `collect_change_context.sh` (sensitive `pom.xml`, `docs/partition.yml`, `model.yml`; no deletion); `detect_test_tampering.sh` (the P-4 row, a false positive); `./mvnw -q clean … jacoco … test … report` build OK, 19 classes, 173 tests, 0 failures, 0 errors, 0 skipped; new `discount` code 199 of 201 lines (99.0%), 86 of 94 branches; no source outside `discount` changed; README checked against the contract, the controller, `DiscountErrors` and V8; C4 `--check` no error, `c4.drawio` regenerated identical. Findings, all Low: the AR-2 `toString`s of `DiscountView`, `MinimumOrder` untested → Blockers (TL); the rest carried under Blockers. Not verified: Spring's overflow on a cyclic cause (the reviewer's probe needed approval; observed in this plan's Task 7 run), a running-app probe, a database other than H2, load, lint, a rollback over V8. Conditions: `apm install` (TL); the owners' answers under `## Blockers`. The plan closes once they are met
- Close, continued, 2026-10-20. Conditions met: `apm install` run (TL; read: `.claude/rules/backend-api-contract-catalog.md` line 5 lists `src/main/java/com/commerce/discount/**`); P-4's evidence reads three contexts (architect, `docs/architecture/patterns-grid.md`); AR-2's trigger names `discount`, as validated (TL, architect, `docs/specs/catalog/area.md`); the untested `toString`s accepted (TL); the loop guard kept in the three contexts (TL); a date-time without seconds accepted in this version, written as an open question of `docs/specs/catalog/discount/spec.md` and `docs/specs/catalog/price-list/spec.md` (TL). Area entries changed by this step: AR-2 (its trigger). Invariant register: unchanged. README unaffected by this step (no command, option, key or operation changed); C4 unaffected. These writes need a third gate run
- Gate, third run, 2026-10-20, by a reviewer that did not write the code (a separate agent), on `ad514d6` to the working tree, untracked files included, every change of the plan and of its close covered: **SHIP**, confidence high. Evidence: `./mvnw -q clean test` exit 0, 19 classes, 173 tests, 0 failures, 0 errors, 0 skipped; `detect_test_tampering.sh` no hit; the deployed rule's body identical to its `.apm/instructions` source, its hash matching `apm.lock.yaml`; no source newer than the documentation writes. Left, Low, no condition, reported to the owners: (1) P-4's other columns still reason with two contexts ("moves two files", "outside both", "one guard for both") (architect); (2) the loop-guard decision: only `DiscountErrors` holds an identity-set guard; `ProductErrors.java:99` and `PriceListErrors.java:93` test `getCause() == cause`, which never holds, so "kept in the three contexts" describes two different guards (TL). Not verified: a baseline run at `ad514d6`, a running-app probe, a database other than H2, load, lint, a rollback over V8; `collect_change_context.sh` did not scan the untracked `discount` code in this run (it did in the first two)
- Done 2026-10-20. Closed on the third gate's SHIP

## Task 7 — Gate finding 1: the untested guards of `discount`

- Scope: `src/test/java/com/commerce/discount/CreateDiscountTest.java`, `src/test/java/com/commerce/discount/GetDiscountTest.java`
- Verification: `./mvnw -q clean test`
- Expected: `CreateDiscountTest` gains a non-string `minimumOrder/currency` and `period/start` (400, each by its field); `GetDiscountTest`'s forced 500 carries a cause chain that loops back and answers 500 with its log; 174 tests, 0 failures
- Decisions that bind it: AR-1 (one item per field at fault), AR-2 (a 500 logs the class and frames, never the message)
- Done 2026-10-20. `CreateDiscountTest.refuses_a_currency_or_a_period_start_that_is_not_a_string_by_its_field` added; it passed at once, the guards existed (coverage, no behaviour added). The loop-back test was written, then removed: its run gave a 500 without the envelope, Spring's own `ExceptionHandlerMethodResolver.resolveExceptionMapping` (spring-web 7.0.9) overflowing on the cyclic cause before any handler runs (`StackOverflowError`, surefire report of `GetDiscountTest`), so `framesOf`'s loop guard cannot be reached through HTTP (Blockers, TL). Output: `./mvnw -q clean test` no error printed; surefire 19 classes, 173 tests, 0 failures, 0 errors (`CreateDiscountTest` 17, `GetDiscountTest` 6). Not as expected (174): the loop-back test removed, for the reason given

## Decisions

- 2026-10-20 · Scope of the catalog decomposition extended to `tag:Discount`; new bounded context `discount` (createDiscount, getDiscount) · Claire Martin, architect: "a discount is its own aggregate, created and read by its own operations; no price list holds it" · rejected: option 2, the discounts in `price-list` (breaks R1)
- 2026-10-20 · AR-1, AR-2, AR-3, AR-4 bind `discount`; AR-5 does not in this version · architect
- 2026-10-20 · Code of `discount` under `src/main/java/com/commerce/discount/**` · TL
- 2026-10-20 · Nothing of an order: `discount` stores and returns discounts, nothing applies them · architect · `discount` BR-6
- 2026-10-20 · A discount is between 1 and 90 percent, both bounds accepted; outside: 400, nothing created · BA · BR-1
- 2026-10-20 · `period` accepted, not kept, governs nothing, as the validity of a price list; an end before its start accepted · BA · BR-2
- 2026-10-20 · `minimumOrder`: a known ISO 4217 code as in `price-list`; a negative amount 400; zero allowed; kept and returned; governs nothing · BA · BR-3 to BR-5
- 2026-10-20 · A discount names no product and no price list · architect · BR-6
- 2026-10-20 · `Accept-Language` accepted and ignored · TL · BR-10
- 2026-10-20 · AR-1 names `DISCOUNT_NOT_FOUND` · architect
- 2026-10-20 · Overlapping discounts allowed, `createDiscount` never 409 · BA · BR-7
- 2026-10-20 · Anyone creates a discount, the demo runs without a login, no limit on how many · PO · finding 1 stays open until authentication is built or something applies discounts
- 2026-10-20 · 401 and 403 never answered; the catalog's order of checks holds for `discount` · TL · Callers, BR-9
- 2026-10-20 · BR-9 "the body checked as one group" confirmed as worded · TL
- 2026-10-20 · The currency rule kept as a citation of `price-list` BR-4; the amount rules separate copies on purpose; no catalog area rule for amounts in this version; where the rule lives is the gap of `bounded-contexts.md` · architect
- 2026-10-20 · `X-Request-Id` does not make a create idempotent; avoiding a retry is the caller's job, in `discount` as in the rest of the catalog · TL · BR-7
- 2026-10-20 · Language: Discount, "A reduction of 1 to 90 percent, with an optional minimum order." · BA
- 2026-10-20 · `discount` thin: its rules only check inputs, a discount has no state that changes · Claire Martin, architect · rejected: rich
- 2026-10-20 · `ArchitectureTest` reads the layer map from `docs/partition.yml` when it runs: it covers `discount` once the map is written · TL
- 2026-10-20 · Plans in `docs/plans/` (`paths.plans`, the project's rule 9); the global instruction names `docs/superpowers/plans/`: the project's declaration wins
- 2026-10-20 · P-4 extends to `discount`: one error handler per context · architect · rejected: a shared catalog handler (as on 2026-10-19)
- 2026-10-20 · `backend-api-contract-catalog` extends to `src/main/java/com/commerce/discount/**` and the `discount` execution; the TL runs `apm install` once the plan is done · TL
- 2026-10-20 · The design `docs/designs/catalog-discounts.md` validated as written, Acceptance included · architect, TL

- 2026-10-20 · `apm install` run; `.claude/rules/backend-api-contract-catalog.md` covers `src/main/java/com/commerce/discount/**` · TL
- 2026-10-20 · P-4's evidence reads three contexts · architect
- 2026-10-20 · AR-2's trigger: "Any log of `product`, `price-list` or `discount`", validated as written · TL, architect
- 2026-10-20 · The AR-2 `toString`s of `DiscountView`, `MinimumOrder` untested in this version: accepted · TL
- 2026-10-20 · `framesOf`'s loop guard stays as defensive code in `product`, `price-list` and `discount` · TL · rejected: dropping it
- 2026-10-20 · A date-time without seconds is accepted in this version, in `price-list` and `discount` alike; an open question of both specs · TL

## Blockers

