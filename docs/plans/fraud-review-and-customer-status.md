# Place and lift a fraud review, and `fraud` answers whether a customer is blocked — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-20

## Tasks

- [x] Task 1 — 2026-10-20 — The owners answer the questions this feature depends on
- [x] Task 2 — 2026-10-20 — The specs of `fraud` and `payment` updated with the answers
- [x] Task 3 — 2026-10-20 — Challenge the spec of `fraud` (partition-attack), then design the feature
- [x] Task 4 — 2026-10-20 — Choose the form, layer map and patterns of `fraud`
- [x] Task 5 — 2026-10-20 — Place, lift and read a fraud review in `fraud`
- [x] Task 6 — 2026-10-20 — `CustomerStatus`: is this customer blocked, yes or no
- [x] Task 7 — 2026-10-20 — Close: gate, levels, README, C4

## Goal

Deliver `#3-fraud-review` of `docs/legacy/customer-spec.md` (747a8dc) in the bounded context `fraud` of the area `customer`: a member of the risk team places a customer under fraud review, lifts it and reads its reviews, use cases with no entry point in this version; and `fraud` answers, through `CustomerStatus`, an interface of its application layer, whether a customer is blocked, yes or no (BR-3, BR-4). The durable spec `docs/specs/customer/fraud/spec.md` comes first; the code implements it. `payment/spec.md` moves in this plan (BR-32 asks `CustomerStatus`, validated 2026-10-20); `payment`'s code switches in a plan of its own, once this one is done, one commit per plan (architect): until then `payment`'s code still reads the list `commerce.fraud-review.customers`, a known divergence from its spec.

## Task 1 — The owners answer the questions this feature depends on

- Scope: none of the repository; answers from the PO, the BA, the architect and the TL in the conversation
- Verification: run after Task 2 — `grep -n -A12 "## Open questions" docs/specs/customer/fraud/spec.md`
- Expected: questions 1, 2, 3 and 6 of `fraud` are gone; blockers A to I are deleted from `## Blockers`
- Decisions that bind it: none
- Done 2026-10-20: answers A to I (PO, BA, architect, TL), one line each in `## Decisions`; blockers A to I deleted. Verified after Task 2: `Open questions` of `fraud` holds 4, 5, 7 and the new 8 to 12; 1, 2, 3 and 6 are gone

## Task 2 — The specs of `fraud` and `payment` updated with the answers

- Scope: `docs/specs/customer/fraud/spec.md`, `docs/specs/billing/payment/spec.md` (BR-32's list, the port `FraudReview`, `Contracts with neighbours`, the word "Customer under fraud review"; then answers R and S), `docs/specs/customer/bounded-contexts.md`; through `partition-spec`, each change of `payment` validated by its owner
- Verification: `grep -n "CustomerStatus\|application.yaml\|commerce.fraud-review" docs/specs/customer/fraud/spec.md docs/specs/billing/payment/spec.md`, then `grep -n -A10 "## Open questions" docs/specs/customer/fraud/spec.md`
- Expected: `CustomerStatus` appears in both specs' `Contracts with neighbours`; neither `application.yaml` nor `commerce.fraud-review` is left in `payment/spec.md`; after answers J to W, `fraud`'s open questions are 4, 5, 7 and one on the running service (answer P), and questions 8 to 12 are gone
- Decisions that bind it: 2026-10-20 · `fraud` owns BR-3, `payment` consumes `CustomerStatus`; 2026-10-20 · `CustomerStatus` is an interface of `fraud`'s application layer; 2026-10-20 · `payment` asks `CustomerStatus`, its spec first, then the list and its adapter go (answer G); 2026-10-20 · the list is empty, nothing to carry over (answer H); answers K to W
- Written 2026-10-20: `fraud/spec.md` (Scope; Language: Blocked, Customer, Open; BR-1 to BR-3 triggers and effects; BR-4 to BR-6; the review's UUID; `Contracts with neighbours`; questions 8 to 12) and `payment/spec.md` (the word "Customer under fraud review", BR-32's trigger, the port `FraudReview` answered by `CustomerStatus`, a `fraud` line in `Contracts with neighbours`). Output of the first check: `CustomerStatus` at `fraud` lines 22, 24, 56, 57, 85 and `payment` lines 36, 71, 75, 100; no `application.yaml` nor `commerce.fraud-review` left in `payment/spec.md`, as expected. `payment`'s change validated as written (PO, TL, architect, 2026-10-20). Then answers K to W: `fraud/spec.md` (Language: Lifted, Under fraud review; BR-1, BR-5, BR-6 effects; BR-7 to BR-9; what a review records; question 13; the attack settled), `payment/spec.md` (BR-32: 500 when `CustomerStatus` cannot answer; the open question of answer R)
- Done 2026-10-20. Output of the second check: `CustomerStatus` in `payment` lines 36, 71, 75, 100, 117 and `fraud` lines 22, 24, 58, 59, 90; no `application.yaml` nor `commerce.fraud-review` in `payment/spec.md`; `fraud`'s open questions are 4, 5, 7 and 13, as expected

## Task 3 — Challenge the spec of `fraud` (partition-attack), then design the feature

- Scope: findings written into `docs/specs/customer/fraud/spec.md`; `docs/designs/fraud-review-and-customer-status.md`
- Verification: `test -f docs/designs/fraud-review-and-customer-status.md && grep -c "BR-1\|BR-3\|CustomerStatus" docs/designs/fraud-review-and-customer-status.md`
- Expected: the file exists; the count is at least 3; every finding of the attack is either settled in the spec or moved to `## Blockers`
- Decisions that bind it: the answers of Task 1; answers K to W
- Attack done 2026-10-20: 8 findings under `### Attack — 2026-10-20` of `fraud/spec.md`, all settled by answers P to W
- Done 2026-10-20: `docs/designs/fraud-review-and-customer-status.md` written. Output of the check: `10`, as expected (at least 3). The design's six questions are blockers X to AC of Tasks 4 to 6

## Task 4 — Choose the form, layer map and patterns of `fraud`

- Scope: `docs/partition.yml` (`fraud` hexagon entry), `docs/architecture/patterns-grid.md`; via `partition-hexagon` and `partition-patterns`
- Verification: `grep -n -A16 "id: fraud" docs/partition.yml`
- Expected: `fraud` declares one hexagon with its form, its reason, its layers and its allowed dependencies; `code` stays `src/main/java/com/commerce/fraud/**`
- Decisions that bind it: 2026-10-20 · code path `src/main/java/com/commerce/fraud/**` (TL); 2026-10-20 · `CustomerStatus` is an interface of `fraud`'s application layer; 2026-10-20 · the hexagon of `fraud` is rich: a review moves from open to lifted, and only the risk team lifts it (architect); 2026-10-20 · BR-6 held by the store (answer Q); 2026-10-20 · P-7, P-8 adopted, layer map as `product` (answers X, Y)
- Done 2026-10-20: `docs/partition.yml` lines 85–98 (form rich, reason, four layers, four allowed edges), as expected; P-7 and P-8 in `docs/architecture/patterns-grid.md`

## Task 5 — Place, lift and read a fraud review in `fraud`

- Scope: `src/main/java/com/commerce/fraud/**`, a migration under `src/main/resources/db/migration/` for the reviews, tests under `src/test/java/com/commerce/fraud/`
- Verification: `./mvnw -q test`
- Expected: BUILD SUCCESS; one test class per use case (place, lift, read), covering BR-1, BR-5, BR-6, BR-7 and the log rule of answer V
- Decisions that bind it: 2026-10-20 · identifiers are UUIDs, generated by the service, never by the caller (`.apm/instructions/backend-api-contract-customer.instructions.md`, TL); 2026-10-20 · placing, lifting and reading a review are use cases with no entry point, called by their tests only (answer E); 2026-10-20 · nothing is checked about who places or lifts (answer F); 2026-10-20 · placing a customer already under review changes nothing, one open review at most (answer C); 2026-10-20 · a review names its customer by billing's `customerId`, the same text (answer D); 2026-10-20 · nothing archives (answer I); answers K to O, Q, T, U, V; answers Z, AA, AB, AC
- Done 2026-10-20. Baseline before any change: `./mvnw -q test`, 160 tests in 18 classes, all passing. Tests written first: `./mvnw -q test-compile` failed, `package com.commerce.fraud.application does not exist`, as expected. Written: `fraud/domain` (`Review`, `ReviewStatus`, `CustomerId`, `InvalidCustomerId`), `fraud/application` (`Reviews`, `OpenReviewHeld`, `PlaceReviewUseCase`, `LiftReviewUseCase`, `ReadReviewsUseCase`, `ReviewView`), `fraud/outbound/JdbcReviews`, `V11__create_fraud_review.sql`; tests `FraudTest`, `PlaceReviewTest` (13), `LiftReviewTest` (7), `ReadReviewsTest` (5). Output: BUILD SUCCESS (exit 0), 190 tests in 22 classes, 0 failures, 0 errors, `ArchitectureTest` 4 passing with the `fraud` map

## Task 6 — `CustomerStatus`: is this customer blocked, yes or no

- Scope: `src/main/java/com/commerce/fraud/application/**` and its tests
- Verification: `./mvnw -q test`
- Expected: BUILD SUCCESS; a test answers yes for a customer under review and no once it is lifted, and no for a customer never reviewed
- Decisions that bind it: 2026-10-20 · `CustomerStatus` is an interface of `fraud`'s application layer (architect); 2026-10-20 · `fraud` owns BR-3 (owner); 2026-10-20 · a customer is blocked while a review of it is open, not lifted (answer A); 2026-10-20 · asked with billing's `customerId`, the same text (answer D); 2026-10-20 · `payment`'s code switches in a plan of its own (answer J): no class of `payment` changes here
- Done 2026-10-20: `CustomerStatus` and `CustomerStatusUseCase` in `fraud/application`; `CustomerStatusTest` (5: blocked while open, not once lifted, not when never placed, blocked again after a new place, the same text). Output: in the run of Task 5, `CustomerStatusTest` 5 passing; no class of `payment` changed

## Task 7 — Close: gate, levels, README, C4

- Scope: this plan; `docs/architecture/model.yml` and its views via `partition-c4`; `README.md` via `partition-readme`
- Verification: `partition-gate` from the commit before Task 1 (4f73a84: the decomposition was committed as 5b461f6 and 4f73a84 before this plan's first task) to the working tree
- Expected: SHIP or SHIP WITH CONDITIONS; no statement left only in this plan
- Decisions that bind it: 2026-10-20 · `payment`'s code switches in a plan of its own (answer J)
- Gate 2026-10-20, 4f73a84 → working tree (6 tracked docs, 21 untracked files: 15 of `fraud`'s code and V11, 5 tests, the design; the plan): **SHIP WITH CONDITIONS**, confidence Medium. Evidence: `./mvnw -q test` exit 0, 190 tests in 22 classes, 0 failures, 0 errors (baseline before the change: 160 in 18, all passing); `ArchitectureTest` 4 passing with the `fraud` map; `detect_test_tampering.sh --range 4f73a84`: nothing deleted, skipped or weakened. Diff coverage not measured: no coverage tool in `pom.xml`; by reading, every new public method is called by a test. Findings: [Medium] `docs/specs/billing/payment/spec.md:71` says BR-32 asks `CustomerStatus`, `src/main/java/com/commerce/payment/outbound/ConfiguredFraudReview.java:20` still reads the list: the divergence answer J accepts until `payment`'s plan. [Low] `src/main/java/com/commerce/fraud/application/ReviewView.java:9` carries the domain's `ReviewStatus`: the first inbound adapter that reads it would depend on `domain`, which the layer map does not allow (`ArchitectureTest` would fail then, not now). Deployment: V11 only adds a table, nothing reads it but `fraud`; an earlier version runs against it; rollback by redeploying. Not verified: a place and a lift of one customer crossing (BR-1, answer T) has no test; the store's behaviour under concurrency on another database than H2; `CustomerStatus` from `payment`, which does not call it yet. Condition: `payment`'s plan switches BR-32 to `CustomerStatus` and adds the `payment` → `fraud` contract to `ArchitectureTest` (owner: architect, TL) — blocker AD. The Low finding on `ReviewView` is no condition: nothing reads it outside the tests in this version; it goes to the design of the first inbound adapter of `fraud`
- Levels 2026-10-20: every statement of `## Decisions` is written at its level, read now: answers A to I and K to W in `docs/specs/customer/fraud/spec.md` (Language, BR-1 to BR-9, the paragraph on what a review records, question 13) or `docs/specs/billing/payment/spec.md` (BR-32, the open question of answer R); X in `docs/architecture/patterns-grid.md` (P-7, P-8); Y in `docs/partition.yml`; Z, AA in BR-8, BR-7 of `fraud`; AB, AC and the store's column in the design; the rule on identifiers in `.apm/instructions/backend-api-contract-customer.instructions.md`. `AR-` and `INV-` entries added or changed: none. Candidates rejected: BR-3 (owned by `fraud`, local to it: `fraud` BR-3), BR-9 (local to `fraud`: `fraud` BR-9), "times to the millisecond, in UTC" (local to `fraud`: the paragraph under `Business rules` of `fraud`)
- README 2026-10-20: `README.md` line 44 gains V11, which adds the table of the fraud reviews and changes no existing table (`partition-readme`); the `commerce.fraud-review.customers` paragraph is still true of the running code, kept until `payment`'s plan. C4 2026-10-20: `docs/architecture/model.yml` gains the component `fraud` (context `fraud`) in `commerce-app` and `fraud -> h2: SQL`; no `payment -> fraud` relation, `payment` does not call it yet. `--check`: "10 elements, 8 relations, 7 views, no error"; generation: the seven pages of `docs/architecture/c4.drawio` written, no hand-set position to keep
- Gate again 2026-10-20, 4f73a84 → working tree, the README and C4 writes included (9 tracked files, +104/−46, and the untracked files above): **SHIP WITH CONDITIONS**, confidence Medium, the same findings and condition; `./mvnw -q test` exit 0, 22 classes, none failing. The plan closes once blocker AD is settled
- Done 2026-10-20: blocker AD settled (architect): the plan closes; the gate's condition is carried by `payment`'s plan, as its goal

## Decisions

- 2026-10-20 · Decomposition of `docs/legacy/customer-spec.md` into `customer` and `fraud`, area `customer` (Claire Martin, architect, proposal option 1) · "an account and a review are two root aggregates (C2), each with its own words (C3)" · rejected: one `customer` bounded context under R4
- 2026-10-20 · `fraud` owns "A customer under fraud review can neither pay nor be refunded." (BR-3), `payment` consumes `CustomerStatus` (owner) · a rule one bounded context owns is local to it; it fails the admission test of the register · rejected: an `INV-` entry
- 2026-10-20 · `CustomerStatus` is an interface of `fraud`'s application layer: is this customer blocked, yes or no (architect) · rejected: none stated
- 2026-10-20 · `#5-conventions` is one line of `.apm/instructions/backend-api-contract-customer.instructions.md`, scoped to `src/main/java/com/commerce/fraud/**` (TL) · how code is written · rejected: an area rule
- 2026-10-20 · The rules of `customer` and `fraud` are confirmed as written, with no other rule; each spec keeps its own words, its `Language` written as drafted from the source; no area file (BA) · rejected: an area file for `customer`
- 2026-10-20 · Code path of `fraud`: `src/main/java/com/commerce/fraud/**`; `customer` has no code yet (TL)
- 2026-10-20 · Plans in `docs/plans/`, as `docs/partition.yml` declares (`paths.plans`) · the global instruction names `docs/superpowers/plans/`; `docs/partition.yml` and the project's `CLAUDE.md` (rule 9) name `docs/plans/`
- 2026-10-20 · A customer is blocked while a review of it is open, not lifted (PO, BA) · answer A · rejected: none
- 2026-10-20 · Only a member of the risk team places a review (PO) · answer B
- 2026-10-20 · Placing a customer already under review changes nothing: one open review per customer at most (PO) · answer C · rejected: a second open review, a refusal
- 2026-10-20 · A review names its customer by the `customerId` billing holds, the same text; the review's own identifier is a UUID `fraud` makes (§5) (architect) · answer D · rejected: the account's identifier of `customer`
- 2026-10-20 · No API contract for the risk team in this version: placing, lifting and reading a review are use cases with no entry point, called by their tests only, as the retirement of a product; the HTTP operations come with a later contract (architect) · answer E
- 2026-10-20 · Nothing is checked about who places or lifts a review in this version: the demo runs without a login (PO) · answer F · rejected: a check of the risk team's members
- 2026-10-20 · `payment` asks `CustomerStatus`; its spec moves first, then the list and its adapter go (PO, TL, architect) · answer G · rejected: keeping the list beside `CustomerStatus`
- 2026-10-20 · The list `commerce.fraud-review.customers` is empty in this project: nothing to carry over (PO) · answer H
- 2026-10-20 · Archiving is out of this plan: nothing archives in this version; BR-2 stays, an open question until an operation needs it (PO) · answer I · rejected: an archiving task
- 2026-10-20 · The hexagon of `fraud` is rich: a review moves from open to lifted, and only the risk team lifts it (architect) · rejected: thin
- 2026-10-20 · The change of `payment/spec.md` is validated as written (PO, TL, architect); `payment`'s code switches in a plan of its own, once this plan is done: one commit per plan (architect) · answer J · rejected: Task 7 "`payment` asks `fraud` through `CustomerStatus`" in this plan, removed; Close renumbered Task 7
- 2026-10-20 · A lift names the customer, by its `customerId`: one open review per customer (architect) · answer K · rejected: a lift by the review's identifier
- 2026-10-20 · A lift of a customer with no open review changes nothing (PO) · answer L · rejected: a refusal
- 2026-10-20 · A review is read by the customer's `customerId`: its reviews, each with its identifier, open or lifted, when placed and when lifted (PO, architect) · answer M
- 2026-10-20 · The `customerId` of a place is bounded like billing's AR-1: a blank or longer one is refused, nothing placed (PO, TL) · answer N
- 2026-10-20 · After a lift, a place opens a new review (PO) · answer O · rejected: reopening the lifted review
- 2026-10-20 · Nobody can place or lift in the running service in this version: accepted, an open question until the HTTP operations come (PO) · answer P
- 2026-10-20 · BR-6 holds at every write, held by the store: of two places at the same moment, the first to commit wins, the other changes nothing (architect) · answer Q · rejected: a check in the code only
- 2026-10-20 · One check of `CustomerStatus` per pay or refund is accepted for this version; an open question of `payment` (PO, architect) · answer R
- 2026-10-20 · If `CustomerStatus` cannot answer, `payment` refuses: 500, nothing charged, CardPay not called (architect) · answer S · rejected: letting the customer through
- 2026-10-20 · A place and a lift of one customer crossing: the last to commit wins; accepted (PO) · answer T
- 2026-10-20 · A review stores its identifier, the `customerId`, open or lifted, the time placed and the time lifted, to the millisecond, in UTC (architect, TL) · answer U
- 2026-10-20 · A log or an error of `fraud` names a review's identifier, never its `customerId` (TL) · answer V
- 2026-10-20 · "Under fraud review": a customer with an open review; "Lifted": a review the risk team ended — both enter `fraud`'s `Language` (BA) · answer W
- 2026-10-20 · P-7 (status of a review: no pattern, an enum and one guarded `lift`, P-1 cited) and P-8 (BR-6: a nullable unique column `open_customer_id`) adopted as proposed (architect) · answer X · rejected: State (GoF); a check before the insert, a partial unique index, a lock on a customer row
- 2026-10-20 · The layer map of `fraud`: domain, application, inbound, outbound, as `product`; `inbound` empty in this version (architect) · answer Y
- 2026-10-20 · A read lists a customer's reviews by time placed, oldest first (PO) · answer Z · rejected: no order promised
- 2026-10-20 · BR-7 bounds the place only; lift, read and `CustomerStatus` take the text as given: a text no review names lifts nothing, reads none, answers no (PO, TL) · answer AA
- 2026-10-20 · Place and lift return nothing, as the retirement of a product (architect) · answer AB
- 2026-10-20 · Test classes `PlaceReviewTest`, `LiftReviewTest`, `ReadReviewsTest`, `CustomerStatusTest`; the aggregate tested inside its use case's class; in-memory database, no HTTP (TL) · answer AC
- 2026-10-20 · The design `docs/designs/fraud-review-and-customer-status.md` is validated (architect, PO, TL)
- 2026-10-20 · The closing gate runs from 4f73a84, not 747a8dc: the decomposition (5b461f6, 4f73a84) was committed before this plan's first task · rejected: 747a8dc, which would audit commits outside this plan
- 2026-10-20 · This plan closes now; the gate's condition (`payment`'s code switched to `CustomerStatus`, the `payment` → `fraud` contract in `ArchitectureTest`) is carried by `payment`'s plan, as its goal (architect) · answer AD · rejected: keeping this plan open until `payment`'s plan meets it

## Blockers

<!-- What is blocked · what unblocks it · who owns it. Delete the line when it clears. -->