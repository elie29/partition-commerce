# After the carry-over of `refund` into `payment` — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-20

## Tasks

- [x] Task 1 — 2026-10-20 — `refund` out of `docs/partition.yml`; the reshape record completed
- [x] Task 2 — 2026-10-20 — The code, the configuration and the C4 model cite BR-32 of `payment` instead of billing's AR-3
- [x] Task 3 — 2026-10-20 — Budget measured again
- [x] Task 4 — 2026-10-20 — Close: gate, levels, README

## Goal

Finish the merge recorded in `docs/architecture/reshapes/2026-10-20-refund-into-payment.md` (plan `docs/plans/refund-into-payment.md`, done) now that Léa Petit (BA) has carried the hand-written sections of `refund` into `docs/specs/billing/payment/spec.md`, brought AR-3 down as its BR-32, taken `refund` out of AR-1 and the area file, realigned the README, and a person has deleted `docs/specs/billing/refund/`.

## Task 1 — `refund` out of `docs/partition.yml`; the reshape record completed

- Scope: `docs/partition.yml` (the entry `refund` goes: its spec folder is deleted), `docs/architecture/reshapes/2026-10-20-refund-into-payment.md` (the precondition on rules ticked: AR-3 → BR-32 of `payment`, DEC-1 once, "Amount paid"; `After`: the entry gone)
- Verification: `ls docs/specs/billing/; grep -n "id: refund" docs/partition.yml; node .claude/skills/partition-budget/scripts/budget.mjs --check; ./mvnw clean verify -Dtest=ArchitectureTest`
- Expected: `area.md bounded-contexts.md invoice payment`; the grep finds nothing; the budget lists no `refund` and passes its schema check; `Tests run: 4, Failures: 0`, `BUILD SUCCESS`
- Decisions that bind it: the absorbed context leaves `docs/partition.yml` once a person has deleted its spec folder (`partition-reshape`)
- Result: `ls` → `area.md bounded-contexts.md invoice payment`; grep: nothing; `budget.mjs --check` exit `0`, five bounded contexts, no `refund` (`payment` 7 416, 23 % ok; `invoice` 6 347, the area file shorter by AR-3); `ArchitectureTest` `Tests run: 4, Failures: 0`, `BUILD SUCCESS`; the record's precondition on rules and its `After` line ticked — as expected
- [x] 2026-10-20

## Task 2 — The code, the configuration and the C4 model cite BR-32 of `payment` instead of billing's AR-3

- Scope, to be confirmed by the owner: `src/main/java/com/commerce/payment/**` (comments; the messages of `PayForbidden`, `RefundForbidden`, `ConfiguredFraudReview`, `CustomerId`), `src/test/java/com/commerce/payment/PayInvoiceTest.java:322,332` (they assert those messages), the comments of `RefundInvoiceTest`, `RefundApiTest`, `PaymentApiTest`, `src/test/resources/fraud-review/no-list.yaml`, `src/main/resources/application.yaml:19` (comment), `docs/architecture/model.yml:119,142`; `src/main/java/com/commerce/invoice/application/InvoicePayments.java:17` and `src/test/java/com/commerce/invoice/InvoicePaymentsTest.java:29` are `invoice`'s, the first in the frozen contract. Not touched: the catalog's AR-3 (another area, another rule)
- Answer (architect, TL, 2026-10-20): no rename. The note left in `docs/specs/billing/area.md` keeps AR-3 resolving to BR-32 of `payment`, as the note on AR-2 did at its removal; an identifier stays permanent. The code, the messages, the tests and the C4 steps are unchanged; `InvoicePayments` and its test untouched: the contract is frozen
- Verification: `grep -n "^AR-3" docs/specs/billing/area.md`; `git status --short -- src docs/architecture/model.yml`
- Expected: line 32 "AR-3 — moved on 2026-10-20 (BA): BR-32 of `payment/spec.md` …"; nothing under `src` or in `model.yml`
- Result: `32:AR-3 — moved on 2026-10-20 (BA): BR-32 of `payment/spec.md`, the one bounded context it binds since `refund` merged into `payment`.`; `git status` shows nothing under `src` nor `model.yml` — as expected
- [x] 2026-10-20
- Decisions that bind it: an identifier stays permanent; a removed area rule resolves through its note (architect, TL, 2026-10-20)

## Task 3 — Budget measured again

- Scope: `docs/budget/2026-10-20.md` (`partition-budget`: a second record the same day replaces it)
- Verification: `node .claude/skills/partition-budget/scripts/budget.mjs`
- Expected (written 2026-10-20, before the run): five bounded contexts, no `refund`; `payment` 7 416 _Always_ (23 % ok), as Task 1's `--check` measured; `invoice` 6 347, the area file shorter by AR-3; the others as in the record (product 5 756, price-list 6 524, discount 5 849); the plan in progress `refund-into-payment-after-carry-over` read on resume well under its whole file
- Result: five bounded contexts, no `refund`; `payment` 7 416 (23 % ok, spec 4 533), `invoice` 6 347 (area file 743 → 564), product 5 756, price-list 6 524, discount 5 849; the plan read on resume 296 of 1 353; `docs/budget/2026-10-20.md` replaced, the earlier figures of the day quoted in it — as expected
- [x] 2026-10-20
- Decisions that bind it: none

## Task 4 — Close: gate, levels, README

- Scope: as `partition-plan` _Close_ says
- Verification: `partition-gate` on the changes of this plan
- Expected: SHIP or SHIP WITH CONDITIONS
- Decisions that bind it: none
- **Gate run 1, 2026-10-20 — SHIP, confidence High.** Target: `2668b00` (the merge, committed by the owner) to the working tree, 7 files, +55/−142, plus this plan: documents only — the carry-over (`payment/spec.md`, `area.md`, `README.md`, `refund/spec.md` deleted, by Léa Petit and a person), `docs/partition.yml` (the entry `refund`), the reshape record, the budget record; no code, no test, no migration, no configuration
  - Evidence: `./mvnw clean verify` → `Tests run: 183, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` (`ArchitectureTest` reads the changed `docs/partition.yml`); `detect_test_tampering.sh --range 2668b00` → its one hit, "test file deleted", is `docs/specs/billing/refund/spec.md`, a spec matched by its name, not a test; `git status --short -- src docs/architecture/model.yml` → nothing. The code is `2668b00`'s, whose look on the running app the TL made (G1 of `docs/plans/refund-into-payment.md`)
  - Read against the code: `payment`'s carried-over spec — BR-25 to BR-29, BR-31, BR-32 and the order 400 → 404 → 403 → 400/409 — as `RefundInvoiceUseCase` does; one port `FraudReview` for pays and refunds, two ports to CardPay (D3, D5); `Contracts with neighbours` without `PaymentRefunds`; the AR-3 cited by the code, its messages and `PayInvoiceTest.java:322,332` resolves through the note `area.md:32` (Task 2); the README's line 28 names `payment/spec.md` (pay, refund)
  - Findings: none
  - Skipped: every class but the specs' — no code changed
  - Not verified: none beyond run 1 of the merge plan
- Levels (2026-10-20): the decisions of Task 2 — an identifier stays permanent, a removed area rule resolves through its note — rejected as a new entry: held already by `docs/specs/billing/area.md`, its notes on AR-2 (line 34) and AR-3 (line 32). No `AR-` or `INV-` entry added or changed; no other candidate
- README: realigned by the owner in this change (line 28: `payment/spec.md` for pay and refund), checked against the specs as they are; nothing for `partition-readme` to add: API, configuration and commands unchanged. C4: unchanged, as Task 2 decided
- [x] 2026-10-20

## Decisions

- 2026-10-20 · The follow-up of the merge in a plan of its own: `docs/plans/refund-into-payment.md` is done and is never resumed · `partition-plan`

- 2026-10-20 · AR-3 is not renamed in the code, the messages, the tests or the C4 steps: the note in `area.md` keeps it resolving to BR-32 of `payment`; an identifier stays permanent · architect, TL · rejected: renaming to BR-32
- 2026-10-20 · `InvoicePayments` and `InvoicePaymentsTest` untouched: the contract is frozen · architect, TL

## Blockers

None.
