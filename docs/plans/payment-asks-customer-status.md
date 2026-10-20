# `payment` asks `fraud` through `CustomerStatus` — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-20

## Tasks

- [x] Task 1 — 2026-10-20 — The design validated
- [x] Task 2 — 2026-10-20 — The tests of `payment` on `CustomerStatus`, written first
- [x] Task 3 — 2026-10-20 — `payment` asks `CustomerStatus`; the list, its adapter and `CustomerId` removed; the contract in `ArchitectureTest`
- [x] Task 4 — 2026-10-20 — Close: gate, levels, README, C4

## Goal

Carry the condition the gate of `docs/plans/fraud-review-and-customer-status.md` left (architect, 2026-10-20): `payment`'s code asks `fraud` through `CustomerStatus` for BR-32, as `docs/specs/billing/payment/spec.md` already says (validated 2026-10-20), and `ArchitectureTest` declares the `payment` → `fraud` contract. The list `commerce.fraud-review.customers` and its adapter go. No spec moves in this plan: both specs already say it; the code follows. Works in `payment`; `fraud` is read through `CustomerStatus` only.

## Task 1 — The design validated

- Scope: `docs/designs/payment-asks-customer-status.md`
- Verification: `grep -n "Questions the design raised, settled" docs/designs/payment-asks-customer-status.md`
- Expected: one line found; blockers A to D deleted
- Decisions that bind it: 2026-10-20 · `payment` keeps its port `FraudReview` (architect); 2026-10-20 · `payment`'s code switches in a plan of its own, one commit per plan (answer J)
- No attack in this plan: the change of `payment/spec.md` was attacked on 2026-10-20 with `fraud`'s (findings 3 and 4, settled as answers R and S)
- Done 2026-10-20: answers A to D written into the design. Output of the check: `35:## Questions the design raised, settled`, as expected; blockers A to D deleted

## Task 2 — The tests of `payment` on `CustomerStatus`, written first

- Scope: `src/test/java/com/commerce/payment/**`, `src/test/java/com/commerce/ArchitectureTest.java`, `src/test/resources/fraud-review/no-list.yaml`
- Verification: `./mvnw -q test`
- Expected: failures, and only in the new tests: the customer placed through `fraud` is not refused (the list does not know it), `CustomerStatus` failing is not reached, the contract of `ArchitectureTest` is not yet used
- Decisions that bind it: 2026-10-20 · the three start-up tests and `no-list.yaml` go with the list (answer C); 2026-10-20 · the tests place and lift reviews through `fraud`'s use cases (answer D)
- Done 2026-10-20: `PaymentApiTest`, `RefundApiTest` without the list property; `PayInvoiceTest` places `UNDER_REVIEW` before each test, loses the three start-up tests, gains `pays_once_the_review_of_its_customer_is_lifted` and `answers_500_without_calling_cardpay_or_recording_when_customer_status_cannot_answer`; `RefundInvoiceTest` places `UNDER_REVIEW`, renames `reads_the_list_of_application_yaml_…` to `asks_fraud_…`, gains `refuses_a_customer_placed_under_review_after_its_pay_and_refunds_it_once_the_review_is_lifted` and `answers_500_without_sending_or_recording_when_customer_status_cannot_answer`; `ArchitectureTest` gains the `payment` → `fraud` contract; `src/test/resources/fraud-review/no-list.yaml` deleted. Expected written first: 9 failures, 5 in `PayInvoiceTest`, 4 in `RefundInvoiceTest`, all on the fraud review. Output: exit 1; `PayInvoiceTest` 20 run, 5 failures; `RefundInvoiceTest` 16 run, 4 failures; exactly the 9 tests predicted; every other class passing

## Task 3 — `payment` asks `CustomerStatus`; the list, its adapter and `CustomerId` removed; the contract in `ArchitectureTest`

- Scope: `src/main/java/com/commerce/payment/outbound/**`, `src/main/java/com/commerce/payment/application/FraudReview.java`, `src/main/java/com/commerce/payment/domain/CustomerId.java`, `src/main/resources/application.yaml`
- Verification: `./mvnw -q test`, then `grep -rn "commerce.fraud-review\|ConfiguredFraudReview" src`
- Expected: BUILD SUCCESS, every test passing; the grep finds nothing
- Decisions that bind it: 2026-10-20 · if `CustomerStatus` cannot answer, `payment` refuses: 500, nothing charged, CardPay not called (answer S); 2026-10-20 · the list is empty, nothing to carry over (answer H); 2026-10-20 · `FraudCustomerStatus`, the port `FraudReview` kept (answer A); 2026-10-20 · the framework's 500, no handler (answer B); 2026-10-20 · `CustomerId` of `payment` goes (answer C)
- Done 2026-10-20: `payment/outbound/FraudCustomerStatus` written; `FraudReview`'s comment names `CustomerStatus`; `ConfiguredFraudReview.java` and `payment/domain/CustomerId.java` deleted; the key `commerce.fraud-review` and its comment out of `application.yaml`. Output: `./mvnw -q test` exit 0, 191 tests in 22 classes (190 − 3 removed + 4 new), 0 failures, 0 errors; `ArchitectureTest` 4 passing with the new contract; `grep -rn "commerce.fraud-review\|ConfiguredFraudReview" src` found nothing, as expected

## Task 4 — Close: gate, levels, README, C4

- Scope: this plan; `README.md` via `partition-readme` (the `Configuration` section); `docs/architecture/model.yml` and its views via `partition-c4` (`payment -> fraud`)
- Verification: `partition-gate` from 39ec10b to the working tree
- Expected: SHIP or SHIP WITH CONDITIONS; no statement left only in this plan
- Decisions that bind it: none
- README 2026-10-20 (`partition-readme`, from 8ab274a, its last commit): the `## Configuration` section, whose one entry was the list, removed; a paragraph under the billing API says a customer is under fraud review while its review is open, the pay and the refund ask `fraud` and answer 500 when it cannot answer, no operation places or lifts a review, and the key is no longer read. Probe of that last claim: `./mvnw -q test -Dtest=CommerceApplicationTests "-DargLine=-Dcommerce.fraud-review.customers[0]=someone"`, expected a start, output exit 0, 1 test passing
- C4 2026-10-20: `payment -> fraud: CustomerStatus, in process` in `docs/architecture/model.yml`; the dynamic views `pay-invoice` and `refund-invoice` gain `fraud` and the steps of BR-32 (the ask, the 500, the 403), AR-3 renamed BR-32. `--check`: "10 elements, 9 relations, 7 views, no error"; the seven pages of `docs/architecture/c4.drawio` regenerated, no hand-set position to keep
- Gate 2026-10-20, 39ec10b → working tree, the README and C4 writes included (13 tracked files, +343/−301, and 3 untracked: `FraudCustomerStatus.java`, the design, this plan): **SHIP**, confidence Medium. One contract between two contexts, `payment` → `fraud` through `CustomerStatus`, declared in both specs and in `ArchitectureTest`. Evidence: `./mvnw -q test` exit 0, 191 tests in 22 classes, 0 failures, 0 errors, after the last change of code; the red run of Task 2 failed exactly the 9 tests predicted; `ArchitectureTest` 4 passing with the new contract; `detect_test_tampering.sh --range 39ec10b`: three tests and `no-list.yaml` removed, the list's start-up behaviour that left the spec with the list (answer C), nothing skipped; the "loosened" leads are new `any()` matchers of new tests. Diff coverage not measured: no coverage tool in `pom.xml`; by reading, `FraudCustomerStatus.underReview` runs on every pay and refund past the invoice check. No findings. Deployment: no migration; a deployment that set the list loses it silently, which the README says (the list is empty in this project, answer H); no customer can be blocked in a running service until the HTTP operations of `fraud` come (`fraud` question 13, accepted, PO); rollback by redeploying 39ec10b, which reads the list again. Not verified: a real failure of the store behind `CustomerStatus` (the tests make a spy throw); the one-check gap between the ask and the record (accepted, an open question of `payment`)
- Levels 2026-10-20: answers A to D are design choices, in `docs/designs/payment-asks-customer-status.md`; BR-32, the 500 and the contract were already in both specs and need no change. `AR-` and `INV-` entries added or changed: none; no candidate
- Done 2026-10-20

## Decisions

- 2026-10-20 · This plan carries the condition of the gate of `docs/plans/fraud-review-and-customer-status.md`, as its goal (architect) · answer AD of that plan
- 2026-10-20 · Plans in `docs/plans/`, as `docs/partition.yml` declares · the global instruction names `docs/superpowers/plans/`

- 2026-10-20 · The adapter is `FraudCustomerStatus`; the port `FraudReview` is kept (architect) · answer A · rejected: `payment`'s use cases calling `CustomerStatus` directly
- 2026-10-20 · A `CustomerStatus` failure is answered by the framework's 500, no handler added (architect) · answer B · rejected: a handler of its own
- 2026-10-20 · `CustomerId` of `payment` and the three start-up tests of `PayInvoiceTest` go with the list (TL) · answer C
- 2026-10-20 · The tests of `payment` place and lift reviews through `fraud`'s use cases, test code only (TL) · answer D · rejected: only a spy on `FraudReview`
- 2026-10-20 · The design `docs/designs/payment-asks-customer-status.md` is validated (architect, TL)
- 2026-10-20 · The README and C4 writes are made before the gate, so one gate run covers every change of the plan · rejected: a gate, then the writes, then a second gate on docs alone

## Blockers

<!-- What is blocked · what unblocks it · who owns it. Delete the line when it clears. -->
