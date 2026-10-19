# Refund a paid invoice, and the fraud review on payments and refunds — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-19

## Tasks

- [x] Task 1 — 2026-10-19 — The owners answer the questions this feature depends on
- [x] Task 2 — 2026-10-19 — The specs of `refund` and `payment` updated with the answers and with `billing.openapi.json` 1.3.0
- [x] Task 3 — 2026-10-19 — Challenge the specs (partition-attack), then design the refund and the fraud review
- [x] Task 4 — 2026-10-19 — Choose the form, layer map and patterns of `refund`; check `payment` against the design
- [x] Task 5 — 2026-10-19 — The fraud review list read from `commerce.fraud-review.customers` (AR-3), behind a port
- [x] Task 6 — 2026-10-19 — A pay refused under AR-3 (`payInvoice` 403)
- [x] Task 7 — 2026-10-19 — Refund a paid invoice (`refundInvoice` 201, 404, 409 under BR-25), CardPay behind an outbound port
- [x] Task 8 — 2026-10-19 — A refund refused under AR-3 (`refundInvoice` 403)
- [x] Task 9 — 2026-10-19 — Close: gate, levels, README, C4

## Goal

Deliver REQ-6 of `docs/sources/billing.md` 1.1 in the bounded context `refund` (the accountant refunds all or part of a paid invoice, sent to the card through CardPay, BR-25), and AR-3 of `docs/specs/billing/area.md` in `payment` and `refund` (a customer under fraud review can neither pay nor be refunded), as `docs/contracts/billing.openapi.json` 1.3.0 (ad4a01b) words them: `refundInvoice` 201/403/404/409 and `payInvoice` 403. The durable specs `docs/specs/billing/refund/spec.md` and `docs/specs/billing/payment/spec.md` come first; the code implements them.

## Task 1 — The owners answer the questions this feature depends on

- Scope: none of the repository; answers from the PO, the BA and the architect in the conversation
- Verification: run after Task 2 — `grep -n -A20 "## Open questions" docs/specs/billing/refund/spec.md docs/specs/billing/payment/spec.md`
- Expected: none of the six questions under `## Blockers` is left open in either spec
- Decisions that bind it: none
- Answers received 2026-10-19 (PO, BA, architect, TL), one line each in `## Decisions`. Verified at the end of Task 2: the six are gone from both specs' `Open questions`; three narrower ones remain in `refund` (settlement, a failed send, how `PaymentRefunds` handles an unpaid invoice)

## Task 2 — The specs of `refund` and `payment` updated with the answers and with `billing.openapi.json` 1.3.0

- Scope: `docs/specs/billing/refund/spec.md`, `docs/specs/billing/payment/spec.md`, `docs/specs/billing/invoice/spec.md` (the `InvoicePayments` row gains `customerId`), `docs/specs/billing/bounded-contexts.md`, `docs/specs/billing/area.md` (only through `partition-spec`, after the owner validates), via `partition-shape` (contract version change) and `partition-spec`
- Verification: `grep -n "1.3.0\|refundInvoice\|403" docs/specs/billing/refund/spec.md docs/specs/billing/payment/spec.md`
- Expected: both contract rows cite 1.3.0; `refund` cites `refundInvoice`; the 403 of AR-3 appears in both specs; no 1.2.0 row is left in `payment`
- Decisions that bind it: every decision of 2026-10-19 in `## Decisions`
- Done 2026-10-19. Output: `payment` lines 57, 61, 83 and `refund` lines 54, 56, 78 carry 1.3.0, `refundInvoice` and 403; `grep -c 1.2.0` on `payment` gives 0. Written: `refund` Language (Amount paid, Refund, What remains), BR-25 trigger and effect, BR-26 to BR-29, the order of answers, AR-3's 403, CardPay port, contract rows (`refundInvoice`, `PaymentRefunds`); `payment` BR-22 order with 403, BR-30, AR-3's 403, `InvoicePayments` with `customerId`, `PaymentRefunds` inbound row; `invoice` `InvoicePayments` row with `customerId`, contract 1.3.0; `bounded-contexts.md` three open lines resolved. `area.md` untouched.

## Task 3 — Challenge the specs (partition-attack), then design the refund and the fraud review

- Scope: findings written into the two specs; `docs/designs/refund-and-fraud-review.md`
- Verification: `test -f docs/designs/refund-and-fraud-review.md && grep -c "AR-3\|BR-25" docs/designs/refund-and-fraud-review.md`
- Expected: the file exists; the count is at least 2; any finding of the attack is either settled in the spec or moved to `## Blockers`
- Decisions that bind it: [architect] for each remaining design choice, take the recommended option; a rename outside the feature waits
- Done 2026-10-19. First, the answers B and C and the attack answers were written into `refund/spec.md` (header and `Source parts owned` with DEC-1, `PaymentRefunds` for any invoice, the cap under concurrency, BR-31, the list read at start-up, anyone may refund) and `payment/spec.md` (`PaymentRefunds` row, the list, no log names a customer, a cancel is not refused by AR-3). Attack: 2 findings written under `### Attack — 2026-10-19` in `refund/spec.md` → Blockers D and E. Design: `docs/designs/refund-and-fraud-review.md`. Output of the check: `5`.

## Task 4 — Choose the form, layer map and patterns of `refund`; check `payment` against the design

- Scope: `docs/partition.yml` (`refund` hexagon entry), via `partition-hexagon` and `partition-patterns`
- Verification: `grep -n -A20 "id: refund" docs/partition.yml`
- Expected: `refund` declares one hexagon with its form, its reason, its layers and its allowed dependencies
- Decisions that bind it: [architect, 2026-10-19] the hexagon of `refund` is rich: a refund is bounded by what was paid, and it moves from requested to sent to settled
- Done 2026-10-19. `docs/partition.yml` declares the `refund` hexagon (lines 65–78, as the check printed). `docs/architecture/patterns-grid.md` gains P-2 (a refund's status: no pattern, as P-1) and P-3 (the cap: a running total behind a conditional update). `payment` against the design: the same layer map, a new port `FraudReview` and its outbound adapter, and `PaymentRefunds` in its application layer.

## Task 5 — The fraud review list read from `commerce.fraud-review.customers` (AR-3), behind a port

- Scope: the `payment` and `refund` packages only (each context reads the list through its own port; no shared package unless Task 3 decides otherwise), `src/main/resources/application.yaml`, tests
- Verification: `./mvnw -q test`
- Expected: BUILD SUCCESS; tests show a configured customer is under review and an unlisted customer is not
- Decisions that bind it: AR-3 trigger (PO, 2026-10-19): the list lives in `application.yaml` under `commerce.fraud-review.customers` until the risk team has a system; [TL] read at start-up, exact match, a blank or overlong entry stops the start; [architect] one `FraudReview` port per context, a missing list stops the start; [TL] no log names a customer under review; [TL] testing rule: one test class per use case, so the list is tested through `PayInvoiceTest` and `RefundInvoiceTest`, not a class of its own
- 2026-10-19, `payment` side done: `payment.domain.CustomerId`, `payment.application.FraudReview`, `payment.outbound.ConfiguredFraudReview` (bean `paymentFraudReview`), `application.yaml` gains `commerce.fraud-review.customers: []`, `src/test/resources/fraud-review/no-list.yaml`. Its tests are in `PayInvoiceTest` (see Task 6). `refund` side written but not yet tested: `refund.domain.{Text, InvalidRefund, CustomerId}`, `refund.application.FraudReview`, `refund.outbound.ConfiguredFraudReview` (bean `refundFraudReview`). Its tests come with `RefundInvoiceTest` in Task 8, so this task stays open until then.
- Done 2026-10-19. `refund` side tested in `RefundInvoiceTest`: the listed customer gets 403, with exact match. The start-up failures are tested once, in `PayInvoiceTest`: both adapters read the same key with the same code, and a failed start fails at whichever adapter Spring creates first, so neither can be singled out through the application.

## Task 6 — A pay refused under AR-3 (`payInvoice` 403)

- Scope: `src/main/java/com/commerce/payment/**`, its tests
- Verification: `./mvnw -q test`
- Expected: BUILD SUCCESS; a pay for a customer under review answers 403 and nothing changes (no charge sent to CardPay, the invoice stays issued); the order of checks follows the answer to Blocker 4
- Decisions that bind it: [architect] pay order: content 400, invoice 404, fraud review 403, state 409; [architect] `InvoicePayments` gains the customer; [TL] no log names a customer under review
- Done 2026-10-19. `InvoicePayments.customerOf(invoiceId)` (+ `InvoicePaymentsService`), `PayableInvoices.customerOf` (+ `InvoicePaymentsAdapter`), `PayForbidden` → 403 in `PaymentErrors`, the check in `PayInvoiceUseCase` after the 404 and before the 409. Design changed and recorded: `customerOf` instead of a new view type (see `## Decisions`). Tests: `PayInvoiceTest` +7 (403 with CardPay not called and the invoice read back `ISSUED`; exact match; 400/404 before 403, 403 before 409; no output names the customer; a missing list, a blank, NBSP or 256-character entry stops the start with a message naming no entry; a 255-code-point entry is read), `InvoicePaymentsTest` +1 (`customerOf`). Output of `./mvnw -q test`: exit 0; surefire: CancelInvoiceTest 8, CommerceApplicationTests 1, ArchitectureTest 4, GetInvoiceTest 3, IssueInvoiceTest 20, InvoicePaymentsTest 8, PaymentMigrationTest 1, PayInvoiceTest 21, with 0 failures and 0 errors.

## Task 7 — Refund a paid invoice (`refundInvoice` 201, 404, 409 under BR-25), CardPay behind an outbound port

- Scope: `src/main/java/com/commerce/refund/**`, its tests
- Verification: `./mvnw -q test`
- Expected: BUILD SUCCESS; 201 with a `Refund` for a refund within what remains of the amount paid; 409 when the invoice is not paid or the refund exceeds what remains; 404 for an unknown invoice
- Decisions that bind it: [architect] refund order: content 400, invoice 404, fraud review 403, then the payment: currency 400, state and what remains 409; [architect] two refunds at once: the first to commit wins, the other gets 409 (P-3); [architect] `REQUESTED` before CardPay is called; if `SENT` fails after the send: 500, it stays `REQUESTED` and counts; [TL] logs: identifiers and status only; [architect] `PaymentRefunds` answers for any invoice
- Done 2026-10-19.
  - `payment`: `PaymentRefunds`, `RefundableInvoice`, `PaidPayment`, `PaymentRefundsService`, and `Payments.findOf` with its `JdbcPayments` implementation.
  - `refund` domain: `Money` (with `ofRefund`, BR-26), `Refund`, `RefundStatus`.
  - `refund` application: `RefundInvoiceUseCase`, `RefundView`, `InvoiceToRefund`, ports `RefundableInvoices`, `Refunds`, `CardPayRefunds`, exceptions, `InvoiceIds`.
  - `refund` outbound: `PaymentRefundsAdapter`, `JdbcRefunds`, `FakeCardPayRefunds`. Inbound: `RefundInvoiceController`, `RefundErrors`.
  - `V6__create_refund.sql` (`refund`, `refund_cap` with `refunded_currency` per `.claude/rules/backend-database.md`), a `refund` generator execution in `pom.xml`, the `refund → payment` contract in `ArchitectureTest`.
  - Differs from the design, recorded in `## Decisions`: the use case returns `RefundView`, since the inbound layer may not reach the domain; `Money.ofRefund` stands in for the designed `RefundAmount`/`AmountPaid`.
  - First run: 1 failure, in `no_output_…`. The test expected no body on the 500, but Spring answers its default error body (timestamp, status, path), as on the pay's 500. The test was corrected to check refusal bodies for emptiness and the 500 body for no sensitive value.
  - Second run: `./mvnw -q test`, exit 0. Surefire: ArchitectureTest 4, InvoicePaymentsTest 8, CancelInvoiceTest 8, RefundInvoiceTest 14, CommerceApplicationTests 1, GetInvoiceTest 3, PayInvoiceTest 21, PaymentMigrationTest 1, IssueInvoiceTest 20, with 0 failures and 0 errors.

## Task 8 — A refund refused under AR-3 (`refundInvoice` 403)

- Scope: `src/main/java/com/commerce/refund/**`, its tests
- Verification: `./mvnw -q test`
- Expected: BUILD SUCCESS; a refund for a customer under review answers 403 and nothing is sent to CardPay
- Decisions that bind it: [architect] 403 after 400 and 404, before the payment's 400 and 409; [TL] no log names a customer under review
- Done 2026-10-19, in the same use case as Task 7 and checked by the same run. In `RefundInvoiceTest`: a 403 on a paid invoice whose customer comes under review (spied `FraudReview`) for any amount or currency, nothing recorded, CardPay not called; the list from configuration on an issued invoice (403 before 409; 400 and 404 before 403); exact match; no output names the customer.

## Task 9 — Close: gate, levels, README, C4

- Scope: as `partition-plan` (Close) says
- Verification: the `partition-gate` verdict
- Expected: SHIP or SHIP WITH CONDITIONS, with every condition turned into a task or a blocker
- Decisions that bind it: none

### Gate run 1 — 2026-10-19 — ad4a01b → working tree

**Verdict: SHIP WITH CONDITIONS.** Confidence: Medium. The full suite passes before and after the change, and every rule of the specs maps to a test. But diff coverage could not be measured (no coverage tool in `pom.xml`), the author of the change ran the audit, and only H2 was exercised.

**What changed.** The change spans three bounded contexts and two in-process contracts:
- `invoice`: `InvoicePayments.customerOf`.
- `payment`: the AR-3 check in pay, a `FraudReview` port and adapter, the new contract `PaymentRefunds`, `Payments.findOf`.
- `refund`: a new hexagon with `refundInvoice`, V6 and `refund_cap`.
- Shared: the new required config key `commerce.fraud-review.customers`, and contract 1.3.0 (`payInvoice` 403, `refundInvoice`).

Blast radius: each interface that gained a method has one implementation, updated (`InvoicePaymentsService`, `InvoicePaymentsAdapter`, `JdbcPayments`). No consumer outside the repository.

**Evidence.**

| Check | Command | Result |
| --- | --- | --- |
| Baseline, before the code changes | `./mvnw -q test` | exit 0 |
| Build and full suite, after | `./mvnw -q test` | exit 0; 80 tests, 0 failed, 0 errors, 0 skipped (surefire) |
| Test tampering | `detect_test_tampering.sh --range ad4a01b` | none; the two "loosened" hits are new `verify(…, never())` lines |
| Architecture | `ArchitectureTest` | 4 passed, `refund` layer map and `refund → payment` contract included |
| Diff coverage | — | not run: no coverage tool configured |

**Findings.**
- [Medium] **An unpaid invoice with another currency answers 409, not 400** (`src/main/java/com/commerce/refund/application/RefundInvoiceUseCase.java:56`). The spec's order reads "then the payment: its currency (400), its state and what remains (409)". An invoice with no payment has no currency to compare, so the code answers 409, but the spec does not say so: a behaviour the spec does not describe. Fix: the architect confirms, and BR-25 states it.
- [Medium] **A refund's amount at DEBUG/TRACE.** The generated `RefundInvoice` and `Refund` print the amount, and Spring's web layer logs the bodies it reads at DEBUG/TRACE, against BR-31. Same exposure as the pay's card token, accepted there by the TL. Fix or acceptance: the TL (Blocker F).
- [Low] **Two extra reads per pay** (`PayInvoiceUseCase.java:54`, `PaymentRefundsService.java:27`). `customerOf` loads the invoice aggregate a second time. Not in a hot path at this scale; noted, no condition.

Classes skipped: frontend (none); async/jobs (none; CardPay is a synchronous fake).

**Deployment.**
- V6 is additive (two new tables and an index), with no backfill and no lock on existing tables.
- N−1: the previous artifact ignores the new tables. Flyway's default `ignoreMigrationPatterns` (`*:future`) lets it start against V6.
- Rollback: redeploy the previous artifact. Refunds recorded meanwhile stay in their tables, and the fake sends nothing outside.
- The new key is declared in the packaged `application.yaml` (empty list), so an external configuration without it still starts.
- Observability: one INFO line per refund (identifiers and status); a 500 logs its stack trace without values.

**Not verified.**
- Diff coverage.
- PostgreSQL behaviour of the conditional update and the `DuplicateKeyException` path of `openCap` (H2 only; the concurrent test passes on H2).
- The start-up failures of `refund`'s adapter on its own (they are covered through whichever adapter Spring creates first).
- Load and performance.
- A real CardPay.

**Conditions.**
- [x] The architect confirms that an invoice not paid answers 409 whatever the currency, and BR-25 of `refund/spec.md` says so (Blocker G) — 2026-10-19.
- [x] The TL rules on a refund's amount at DEBUG/TRACE (Blocker F): accepted, open question — 2026-10-19.

### Close steps 2–5 — 2026-10-19

**Levels.** Every statement the work established, and where it lives now:
- In `refund/spec.md`: Language (Amount paid, Refund, What remains), BR-25 to BR-29 and BR-31, the order of answers, the CardPay port, `PaymentRefunds`, DEC-1 in `Source parts owned`.
- In `payment/spec.md`: BR-22's order with 403, BR-30, `InvoicePayments` with the customer, `PaymentRefunds`.
- In `invoice/spec.md`: `InvoicePayments` with the customer.
- In `area.md`: AR-3 (trigger, list, effect), validated by the architect.
- In `docs/architecture/patterns-grid.md`: P-2, P-3.
- Only in this plan, and true of this feature only: the 500's default body, the bean names, `customerOf` instead of a view type, `RefundView`, where the tests live.

**Register and area file.** `AR-` entries: 1 changed (AR-3), 0 added. `INV-` entries: 0. Rejected candidates:
- "No log names a customer under fraud review", as an `INV-`: held by AR-3 of `docs/specs/billing/area.md`, read now. It spans `payment` and `refund`, one area.
- "The refunds of an invoice never exceed its amount paid", as an `INV-`: held by BR-25 of `docs/specs/billing/refund/spec.md`. It belongs to one bounded context.
- "Amounts in minor units with a currency column": held by AR-2 of `area.md` and `.claude/rules/backend-database.md`.

**Rule-file candidates**, shown to the owner and not written:
1. "A value that holds a customer, a charge reference or a refund's amount hides it in `toString`" (backend, `payment` and `refund`).
2. "A Spring bean whose class name repeats across bounded contexts is named after its context."

**README.** Realigned with `partition-readme`:
- `## API` gains `POST /invoices/{invoiceId}/refunds`, the pay's `403`, the fake's refunds, no read of a refund, and the refund spec among the rule files.
- The migrations paragraph gains V6.
- New `## Configuration` section for `commerce.fraud-review.customers`.

**C4.**
- `docs/architecture/model.yml` gains the component `refund` (context `refund`) and relations `refund -> payment` (`PaymentRefunds`), `refund -> h2`, `refund -> cardpay`.
- The accountant now "issues, cancels, refunds".
- `pay-invoice` gains the `customerOf` and 403 steps, and there is a new dynamic view `refund-invoice`.
- `--check`: "9 elements, 9 relations, 7 views, no error". Generation wrote 7 pages to `docs/architecture/c4.drawio`; 0 elements had a hand-set position.

Step 6, the second gate run covering these writes, waits for Blockers F and G, since meeting them writes the spec again.

### Gate run 2 — 2026-10-19 — ad4a01b → working tree, the close's writes included

**Verdict: SHIP.** Confidence: Medium. No code changed since run 1. Since then: the two conditions are met (BR-25 says 409 for an unpaid invoice whatever the currency; the amount at DEBUG/TRACE is an accepted open question), the README and the C4 model are realigned, and the plan carries the levels. Every refusal and effect of the specs maps to a test that runs it. Line-level diff coverage is still unmeasured.

| Check | Command | Result |
| --- | --- | --- |
| Build and full suite | `./mvnw -q test` | exit 0; 80 tests, 0 failed, 0 errors, 0 skipped (surefire: ArchitectureTest 4, GetInvoiceTest 3, CancelInvoiceTest 8, CommerceApplicationTests 1, InvoicePaymentsTest 8, PaymentMigrationTest 1, IssueInvoiceTest 20, RefundInvoiceTest 14, PayInvoiceTest 21) |
| C4 model | `node .claude/skills/partition-c4/scripts/c4-drawio.mjs --check` | "9 elements, 9 relations, 7 views, no error" |
| Test tampering | `detect_test_tampering.sh --range ad4a01b` | no test removed, no assertion removed, nothing skipped |
| README against the code | read: each endpoint, status, key and migration it names | as the code and the contract say |
| Diff coverage | — | not run: no coverage tool configured |

**Findings.** One Low, carried from run 1: two extra reads per pay, through `customerOf`; no condition.

**Not verified**, as in run 1:
- line-level diff coverage;
- PostgreSQL behaviour of the conditional update and of `openCap`'s duplicate path;
- `refund`'s start-up failure on its own;
- load;
- a real CardPay.

README realigned; C4 updated.

## Decisions

- 2026-10-19 · Plans go in `docs/plans/`, as `docs/partition.yml` (`paths.plans`) and rule 9 of the project's `CLAUDE.md` say · the user-level `CLAUDE.md` names `docs/superpowers/plans/`, which does not exist here; the project's own setting wins, and the mismatch is flagged to the owner.
- 2026-10-19 · One plan covers both the refund (REQ-6) and the fraud review (AR-3 in `payment` and `refund`) · the user asked for both together, and AR-3 is the one rule the two share · rejected: two separate plans.
- 2026-10-19 · [PO] The amount paid of BR-25 is the amount charged, held by `payment`; every refund not refused counts against it, whether requested, sent or settled · answers Blocker 1.
- 2026-10-19 · [BA] One term: "amount paid" · no synonym for the amount charged within `refund`.
- 2026-10-19 · [architect] The currency of a refund is the currency of the amount charged; the payments copied by V4 are found like any other · answers Blocker 1.
- 2026-10-19 · [TL] A CardPay port sends a refund (the charge reference, the amount), and the refund becomes `SENT`; its only adapter is a fake that accepts every refund · answers Blocker 2.
- 2026-10-19 · [TL] Settlement comes from CardPay's notifications: left as an open question in the spec; nothing moves a refund to `SETTLED` in this version · answers Blocker 2.
- 2026-10-19 · [PO] DEC-1 (CardPay) covers refunds too · answers Blocker 2.
- 2026-10-19 · [architect] A refund is recorded `REQUESTED` before CardPay is called, and counts against the amount paid from then on; what follows a failed send stays an open question until CardPay is reachable, since the fake always sends · answers Blocker 2.
- 2026-10-19 · [architect] `refund` reads the payment of a paid invoice through `PaymentRefunds`, a contract of `payment`: its charge reference, the amount charged and the invoice's customer · answers Blocker 3.
- 2026-10-19 · [architect] `InvoicePayments` gains the invoice's customer (`customerId`), agreed by `invoice` and `payment` · answers Blocker 3.
- 2026-10-19 · [architect] Refund checks run in this order: the content of the request (400), the invoice (404, a non-UUID included), the fraud review (403), then the payment: its currency (400), its state and what remains (409) · answers Blocker 6.
- 2026-10-19 · [architect] Pay checks run in this order: the content (400), the invoice (404), the fraud review (403), then its state (409) · answers Blocker 4.
- 2026-10-19 · [PO] After a refund the invoice stays `PAID`; a refunded invoice cannot be cancelled (REQ-5), and a cancelled payment cannot be refunded · answers Blocker 5.
- 2026-10-19 · [PO] The risk team puts a customer under fraud review and lifts it by editing the list in `application.yaml`; there is no API for it in this version.
- 2026-10-19 · [PO] A refund of zero, of a negative amount, or in another currency is refused: 400 with no body, as for issuing.
- 2026-10-19 · [architect] The one text `refund` stores, the charge reference, is bounded by AR-1.
- 2026-10-19 · [architect] The hexagon of `refund` is rich: a refund is bounded by what was paid, and it moves from requested to sent to settled · binds Task 4.
- 2026-10-19 · The 403 of AR-3 and the "risk team edits the list" note are written in each spec (`payment`, `refund`), not in AR-3 of `area.md` · nothing goes into the area file until the owner validates it, and each context's answer comes from its own operation in contract 1.3.0 · rejected for now: filling AR-3's expected effect (can be proposed to the owner at the close).
- 2026-10-19 · New rules are numbered BR-26 to BR-29 (`refund`) and BR-30 (`payment`), after BR-25, the highest number in use across `docs/specs`.
- 2026-10-19 · [PO, BA, architect] The spec changes of Task 2 are validated as written · clears Blocker A.
- 2026-10-19 · [architect] `PaymentRefunds` answers for any invoice: whether it exists, the invoice's customer, and its payment when it is paid (charge reference, amount charged); `payment` reads the invoice and its customer through `InvoicePayments` · clears Blocker B; binds Tasks 6, 7.
- 2026-10-19 · [architect] DEC-1 is shared by `payment` and `refund`: `refund` points to it in `Source parts owned`, as `payment` does, beside REQ-6 · clears Blocker C.
- 2026-10-19 · [PO] What was inferred and written in Task 2 is confirmed: a refusal records nothing; a refund is `REQUESTED` before it is sent.
- 2026-10-19 · [architect] Two refunds of one invoice at the same moment are recorded under one conditional check of the sum against the amount paid: the first to commit wins, the other gets 409 · binds Task 7.
- 2026-10-19 · [PO] Anyone holding the identifier may refund; accepted in this version, since there is no login.
- 2026-10-19 · [TL] No log ever names a customer under fraud review. A refund writes only its identifiers and its status to a log or an error: never the charge reference, never the amount · binds Tasks 5 to 8.
- 2026-10-19 · [PO] A request sent again after its answer was lost makes two refunds; there is no idempotency key in this version, and the cap bounds them.
- 2026-10-19 · [PO] That a 403 tells anyone holding an identifier the customer is under review is accepted in this version: contract 1.3.0 answers 403.
- 2026-10-19 · [PO] A customer under fraud review may still cancel: AR-3 names paying and being refunded only; cancelling stays as REQ-5 says.
- 2026-10-19 · [TL] The fraud review list is read at start-up, so an edit counts from the next restart (accepted). Entries match `customerId` exactly; a blank or overlong entry (AR-1) stops the start. `payment` and `refund` each read it · binds Task 5.
- 2026-10-19 · [PO] No operation reads a refund, and `SETTLED` is never shown; accepted in this version.
- 2026-10-19 · [architect] Each context reads the list through its own `FraudReview` port, with an adapter that reads the list, so the source can change once the risk team has a system; a missing list stops the start · binds Task 5; rejected: one shared component.
- 2026-10-19 · [architect] The area-file proposals ("under fraud review" defined; DEC-1 covering refunds) are approved. These entries had not been shown in their written form, so they are shown at the close for a validation of the exact text before anything is written to `area.md` (`partition-spec`).
- 2026-10-19 · [architect] For the remaining design choices and pattern rulings (what `PaymentRefunds` answers, how the cap holds, a rename), take the recommended option of each; a rename outside the feature waits · binds Tasks 3, 4.
- 2026-10-19 · Recommended option taken for the cap (P-3): a running total per invoice in `refund_cap`, behind one conditional update · rejected: a sum checked by `INSERT … SELECT … WHERE` (both refunds pass under READ COMMITTED), a lock on `payment`'s row (reaches into another context's table).
- 2026-10-19 · Recommended option taken for a refund's status (P-2): an enum and guarded methods on the aggregate, as P-1 · rejected: State (one move built).
- 2026-10-19 · `InvoicePayments` gains `customerOf(invoiceId)` rather than a new view type returned by `find` and `recordPayment` · the customer never changes after issue, and `InvoiceView` and its callers stay as they are · rejected: `InvoicePaymentView` (every caller and test of the contract moves for one field). The design was updated.
- 2026-10-19 · The fraud review list is tested through the use-case test classes (`PayInvoiceTest`, later `RefundInvoiceTest`), each starting the real application for the start-up failures · `.claude/rules/backend-testing.md` exempts only the architecture test and data-moving migration tests from "one test class per use case" · rejected: a `FraudReviewTest` of its own.
- 2026-10-19 · [architect] If CardPay sent the refund but recording `SENT` fails, or the process stops before the send: 500, as for a pay; the refund stays `REQUESTED` and keeps counting against the cap in this version; an open question until CardPay is reachable · clears Blocker D; binds Task 7.
- 2026-10-19 · [architect] AR-3 validated in `area.md` exactly as shown; no area entry for DEC-1 · clears Blocker E. The two specs now point to AR-3 instead of repeating the list's rules.
- 2026-10-19 · `RefundInvoiceUseCase` returns an application `RefundView`, not the domain `Refund` · the layer map lets `inbound` reach `application` only · as `payment` returns `InvoiceToPay`.
- 2026-10-19 · `refund_cap` has a `refunded_currency` column · `.claude/rules/backend-database.md` pairs every `_minor` column with its `_currency` column.
- 2026-10-19 · A refund's 500 keeps Spring's default error body (timestamp, status, path), as the pay's 500 does · "no body" in the spec covers refusals; the 500 is the architect's "as for a pay".
- 2026-10-19 · [TL] A refund's amount at DEBUG/TRACE is accepted in this version, as for the card token: the demo never runs `org.springframework.web` at DEBUG or TRACE; it stays an open question · clears Blocker F.
- 2026-10-19 · [architect] An unpaid invoice answers 409 whatever the currency: it has no payment to compare a currency against; BR-25 says so · clears Blocker G.
- 2026-10-19 · [TL] The two rule-file candidates (hiding sensitive values in `toString`; beans named after their context) are not written now.
- 2026-10-19 · The adapters are named `paymentFraudReview` and `refundFraudReview` · two classes named `ConfiguredFraudReview` would otherwise clash on Spring's default bean name.

## Blockers

Blockers 1 to 6 were answered on 2026-10-19 (see `## Decisions`).

Blockers A, B and C were cleared on 2026-10-19 (see `## Decisions`).

Blockers D and E were cleared on 2026-10-19 (see `## Decisions`).

Blockers F and G were cleared on 2026-10-19 (see `## Decisions`).

Settled by contract 1.3.0 (ad4a01b), to be carried into the specs in Task 2 without new wording: `refundInvoice` exists (closes "REQ-6 has no operation"); a refusal under AR-3 answers 403 in both `payInvoice` and `refundInvoice`; a refusal under BR-25 or for an unpaid invoice answers 409.
