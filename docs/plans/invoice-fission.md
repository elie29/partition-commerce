# Fission of `invoice`: `payment` comes out — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-19

## Tasks

- [x] Task 1 — 2026-10-19 — `payInvoice` generated into `com.commerce.payment.inbound.api`
- [x] Task 2 — 2026-10-19 — The pay moves to `com.commerce.payment`, reaching `invoice` through `InvoicePayments` only
- [x] Task 3 — 2026-10-19 — V4: `payment`'s own table; the invoice row's payment columns go with the move
- [x] Task 4 — 2026-10-19 — `invoice` loses the pay: `Invoice.pay`, the charge reference, `Invoices.recordPayment`
- [x] Task 5 — 2026-10-19 — Architecture test: `payment` reaches `invoice` through `InvoicePayments` only
- [x] Task 6 — 2026-10-19 — Proof that it was a move
- [x] Task 7 — 2026-10-19 — After: `docs/partition.yml` hexagon reasons, C4, budget, the record
- [x] Task 8 — 2026-10-19 — Close: gate, levels, README

## Goal

Carry out the fission recorded in `docs/architecture/reshapes/2026-10-19-invoice-fission.md`: the pay (REQ-4) moves from `com.commerce.invoice` to `com.commerce.payment`, behind the frozen `InvoicePayments`, with `payment`'s own table, as the specs written by `docs/plans/invoice-fission-preconditions.md` say. REQ-5 is not built here.

## Task 1 — `payInvoice` generated into `com.commerce.payment.inbound.api`

- Scope: `pom.xml`: a second execution of `openapi-generator-maven-plugin`, `payment`, keeping only the operations of the tag `Payment` (`openapiNormalizer` `FILTER=tag:Payment`) and the models they use (`modelsToGenerate`), into `com.commerce.payment.inbound.api` and `.model`. The execution of `invoice` is filtered to its tag in Task 2, when the pay's controller leaves it
- Verification: `./mvnw clean verify; ls target/generated-sources/openapi/src/main/java/com/commerce/payment/inbound/api/ target/generated-sources/openapi/src/main/java/com/commerce/payment/inbound/api/model/`
- Expected: `Tests run: 55, Failures: 0`, `BUILD SUCCESS`; `api/`: `ApiUtil.java`, `PaymentApi.java`, `model`, no `InvoiceApi.java`; `model/`: `Invoice.java`, `Line.java`, `Money.java`, `PayInvoice.java`, no `IssueInvoice.java`
- Decisions that bind it: one interface per tag (TL); `payment` answers the contract's `Invoice` schema (architect)
- Result: `Tests run: 55, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`; `api/`: `ApiUtil.java PaymentApi.java model`; `model/`: `Invoice.java Line.java Money.java PayInvoice.java` — as expected
- [x] 2026-10-19

## Task 2 — The pay moves to `com.commerce.payment`, reaching `invoice` through `InvoicePayments` only

- Carried out with Tasks 3, 4 and 5 as one change: C1 ties the pay's switch to `recordPayment` to Task 4, and `ArchitectureTest.no_hexagon_imports_from_another` fails as soon as `payment` imports `invoice` until Task 5 declares the exception. Each task keeps its own result
- Design (2026-10-19), from the decisions below:
  - `payment.domain`: `Payment` (a key of its own, the invoice's identifier, the amount charged, the charge reference bounded by AR-1: a reference out of bounds is a defect, 500, BR-5); own `Money` (AR-2: minor units, a three-letter code), own `CardToken` and `Text` (AR-1), `InvalidPayment`. No status yet: "made → cancelled" comes with REQ-5
  - `payment.application`: `PayInvoiceUseCase` (moved; order 400 → 404 → 409 → CardPay → record); `RecordPayment`, the separate `@Transactional` bean holding the two writes, the invoice recorded paid first, the payment inserted last so its time is taken just before the commit; ports `CardPay` and `Charge` (moved), `Payments`, `PayableInvoices` with its own types `InvoiceToPay`, `InvoiceLine` and its refusal `InvoiceNoLongerIssued`; own `InvoiceIds`; `PaymentContentRefused` (400), `InvoiceNotFound` (404), `PayRefused` (409), `CardDeclined` (402)
  - `payment.outbound`: `InvoicePaymentsAdapter` (GoF Adapter, the only class of `payment` that imports `invoice`: `InvoicePayments`, `InvoiceView`, `LineData`, `InvoiceNotIssued`); `JdbcPayments`; `FakeCardPay` (moved)
  - `payment.inbound`: `PayInvoiceController` implements `com.commerce.payment.inbound.api.PaymentApi`; `PaymentModels` (own copy of the mapping); `PaymentErrors`, scoped to it
  - Tests: `PayInvoiceTest` moves to `src/test/java/com/commerce/payment/` with its own `PaymentApiTest` and `PaymentClient` copies; its spy on `invoice`'s `Invoices` becomes a spy on `payment`'s `Payments` (the record failing); its assertions unchanged
- Scope: `src/main/java/com/commerce/payment/**`, `src/test/java/com/commerce/payment/**`
- Verification: `./mvnw clean verify`, then `grep -rln "com.commerce.invoice" src/main/java/com/commerce/payment`
- Expected: `BUILD SUCCESS`, no test failing, `PayInvoiceTest` 14 tests in `com.commerce.payment`; the grep lists `outbound/InvoicePaymentsAdapter.java` only
- Result: `Tests run: 58, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` (first run), `Tests run: 14 … in com.commerce.payment.PayInvoiceTest`; grep: `src/main/java/com/commerce/payment/outbound/InvoicePaymentsAdapter.java` only — as expected
- [x] 2026-10-19
- Decisions that bind it: every write of the invoice's state stays in `invoice` (architect); the record of `payment` and `recordPayment` in one transaction, the first to commit holds both (architect); gate condition C1: the pay calls `InvoicePayments.recordPayment` only in the same change as Task 4 (see Task 4)

## Task 3 — V4: `payment`'s own table; the invoice row's payment columns go with the move

- Scope: `src/main/resources/db/migration/V4__move_the_payment_to_its_own_table.sql`: table `payment` (`payment_id` UUID key, `invoice_id` with no foreign key, `amount_minor` BIGINT, `amount_currency` CHAR(3), `charge_reference`, `paid_at` not null), the payments already recorded copied from the `PAID` invoices (amount: the sum of their lines); `JdbcPayments`
- Verification: `./mvnw clean verify`; then a migration test on a database at V3 holding a paid invoice: `PaymentMigrationTest`
- Expected: `BUILD SUCCESS`; the test finds the paid invoice's charge reference, time and amount in `payment`
- Result: `Tests run: 1, Failures: 0 … in com.commerce.payment.PaymentMigrationTest` (V3 with a paid invoice of 1250 + 199 EUR, `ref-1`, 2026-10-19T10:00Z → `payment` 1449 EUR, `ref-1`, the same instant; the invoice row keeps `STATUS` and loses `CHARGE_REFERENCE`, `PAID_AT`) — as expected. The test lives in `payment` and tests no use case (`.claude/rules/backend-testing.md`: one class per use case; like the architecture test, it sits outside)
- [x] 2026-10-19
- Decisions that bind it: the time and the charge reference go to `payment`'s own table; `payment` stores the amount charged; the time of a payment is the moment the record of `payment` commits (architect)

## Task 4 — `invoice` loses the pay: `Invoice.pay`, the charge reference, `Invoices.recordPayment`

- Scope: `src/main/java/com/commerce/invoice/**`, `src/test/java/com/commerce/invoice/**`, `pom.xml` (the execution `billing` filtered to the tag `Invoice`), `V5__the_invoice_row_loses_its_payment.sql` (drops `charge_reference`, `paid_at`). `Invoice` loses `pay`, `requirePayable` and its charge reference, and gains `recordPayment()`, guarded on `ISSUED` (P-1: every change of status goes through the aggregate; `InvoicePaymentsService` of the preconditions bypassed it); `Invoices.recordPayment`, `CardToken`, `CardPay`, `Charge`, `CardDeclined`, `PayRefused`, `PayInvoiceUseCase`, `FakeCardPay`, `PayInvoiceController` leave `invoice`
- Verification: `./mvnw clean verify`; `grep -rn "chargeReference\|CardPay\|CardToken\|PayInvoice" src/main/java/com/commerce/invoice`; `ls target/generated-sources/openapi/src/main/java/com/commerce/invoice/inbound/api/`
- Expected: `BUILD SUCCESS`; the grep finds nothing; `ApiUtil.java InvoiceApi.java model`, no `PaymentApi.java`; `InvoicePaymentsTest` with its second record refused back, and a test of `InvoicePaymentsService.java`'s refusal after a lost conditional update (C2)
- Result: `BUILD SUCCESS`; grep: nothing; `ApiUtil.java InvoiceApi.java model`; `Tests run: 7, Failures: 0 … in com.commerce.invoice.InvoicePaymentsTest` (the second record refused back; `refuses_to_record_a_payment_when_a_simultaneous_change_commits_first`, C2) — as expected. C1 met: the pay calls `recordPayment` in the same change as `Invoice` dropping its charge reference
- [x] 2026-10-19
- Decisions that bind it: as Task 2
- Gate condition C1 (preconditions, 2026-10-19): `InvoicePayments.recordPayment` gets its production caller (Task 2) only together with `Invoice` dropping the charge reference on `PAID` (this task): until then a `PAID` row with no charge reference is unreadable (`Invoice.java:33-35`)
- Gate condition C2 (preconditions, 2026-10-19): a test covers `InvoicePaymentsService.java:35`, the refusal when a simultaneous pay or cancel commits between the read and the conditional update
- Carried from the preconditions (Task 3): put back in `records_a_payment_changing_the_status_only_in_the_callers_transaction` the second `recordPayment` refused with `InvoiceNotIssued`, dropped while `Invoice` required a charge reference on `PAID`

## Task 5 — Architecture test: `payment` reaches `invoice` through `InvoicePayments` only

- Scope: `src/test/java/com/commerce/ArchitectureTest.java`: a declared contract, `payment` → `invoice` from `payment`'s outbound layer, types `InvoicePayments`, `InvoiceView`, `LineData`, `InvoiceNotIssued`, quoted from `Contracts with neighbours` of both specs; `no_hexagon_imports_from_another` lets that through and nothing else; a new rule: no class of `payment` outside its outbound layer depends on `invoice`
- Verification: `./mvnw clean verify -Dtest=ArchitectureTest`; then the same with a temporary import of `com.commerce.invoice.domain.Invoice` in `PayInvoiceUseCase`, removed after
- Expected: green; then red on `no_hexagon_imports_from_another` naming `PayInvoiceUseCase`
- Result: `Tests run: 4, Failures: 0 … in com.commerce.ArchitectureTest`; with a temporary class literal `com.commerce.invoice.domain.Invoice.class` in `PayInvoiceUseCase` (an import alone leaves no trace in bytecode): `Failures: 2`, `no_hexagon_imports_from_another` and `only_the_layer_a_contract_names_reaches_the_other_hexagon`, each "`PayInvoiceUseCase.<clinit>()` references class object `com.commerce.invoice.domain.Invoice` in (PayInvoiceUseCase.java:19)" — as expected. Probe removed (`grep -c PROBE`: `0`); `./mvnw clean verify`: `Tests run: 58, Failures: 0`, `BUILD SUCCESS`
- [x] 2026-10-19
- Decisions that bind it: from now on the bounded contexts know each other through their contracts only

## Task 6 — Proof that it was a move

- Scope: none written; the record's `Proof that it was a move`
- Verification: `git status --short -- . ':!src/main/java/com/commerce/payment' ':!src/test/java/com/commerce/payment'` against HEAD = 4b3a86f (the commit before Task 1; shows untracked and deleted files too), then `git diff --quiet 4b3a86f -- <the callers and the contract>`
- Expected (written 2026-10-19, before the run): outside `payment`, only
  - the wiring: `pom.xml` (one generator run per tag), `src/test/java/com/commerce/ArchitectureTest.java` (the declared contract), `docs/plans/invoice-fission.md`;
  - what `invoice` loses (Task 4): deleted `application/CardPay.java`, `Charge.java`, `CardDeclined.java`, `PayRefused.java`, `PayInvoiceUseCase.java`, `domain/CardToken.java`, `outbound/FakeCardPay.java`, `inbound/PayInvoiceController.java`, `src/test/.../invoice/PayInvoiceTest.java`; modified `domain/Invoice.java`, `InvoiceNotPayable.java`, `InvoiceStatus.java`, `Text.java`, `application/Invoices.java`, `InvoiceContentRefused.java`, `InvoicePaymentsService.java` (the adapter of the frozen contract), `inbound/InvoiceErrors.java`, `outbound/JdbcInvoices.java`, `src/test/.../invoice/InvoicePaymentsTest.java` (C1, C2);
  - the migrations V4, V5 (new).
  - Unchanged, the callers and the contract: `InvoicePayments.java`, `InvoiceView.java`, `LineData.java`, `InvoiceNotIssued.java`, `InvoiceController.java`, `IssueInvoiceUseCase.java`, `GetInvoiceUseCase.java`, `CancelInvoiceUseCase.java`, `docs/contracts/billing.openapi.json`, `application.yaml`, `CancelInvoiceTest.java`, `GetInvoiceTest.java`, `IssueInvoiceTest.java`, `InvoiceClient.java`
- Result: HEAD `4b3a86f`; `git status --short` outside `payment`: `M docs/plans/invoice-fission.md`, `M pom.xml`, 8 `D` and 9 `M` under `src/main/java/com/commerce/invoice/` exactly as listed, `M ArchitectureTest.java`, `M InvoicePaymentsTest.java`, `D PayInvoiceTest.java`, `?? V4__…`, `?? V5__…`; `git diff --quiet 4b3a86f -- <the 14 callers and the contract>`: `exit=0` — as expected: no caller changed
- [x] 2026-10-19
- Decisions that bind it: none

## Task 7 — After: `docs/partition.yml` hexagon reasons, C4, budget, the record

- Gate condition C3 (preconditions, 2026-10-19): `invoice`'s form reason still cites BR-3 to BR-6; rewrite it through `partition-hexagon`
- Scope: `docs/partition.yml` (the reason of `invoice`'s hexagon), `docs/architecture/model.yml` and its views (`partition-c4`), `docs/budget/` (`partition-budget`), the record's `After`
- Verification: `grep -n "reason:" docs/partition.yml`; `node .claude/skills/partition-c4/scripts/c4-drawio.mjs --check` then without `--check`; `node .claude/skills/partition-budget/scripts/budget.mjs`
- Expected: not written before the commands ran — a slip of this task, recorded as such; the results below are reported as they came, not checked against a prediction
- Done: `invoice`'s reason cites BR-3, BR-4, BR-18, BR-23 instead of "BR-3 to BR-6" (rule references only; C3); `model.yml` gains the component `payment` (context `payment`, name "Payment", proposed by the assistant), the relations `payment -> invoicing: InvoicePayments, in process`, `payment -> h2`, `payment -> cardpay` (which leaves `invoicing`), and the dynamic view `pay-invoice` through `payment`; the budget record of the day replaced (`docs/budget/2026-10-19.md`), the architect's decision "nothing" in it
- Result: line 32 `invoice` reason as above, line 50 `payment`'s; `model.yml: 8 elements, 6 relations, 6 views, no error`; `c4.drawio` six pages regenerated (`payment` placed automatically, no hand-set position kept); budget `invoice` 5 881 (18 % ok), `payment` 5 089 (16 % ok), every session 1 786, the plan 395 read on resume
- [x] 2026-10-19
- Decisions that bind it: none

## Task 8 — Close: gate, levels, README

- Scope: as `partition-plan` _Close_ says
- Verification: `partition-gate` on the changes of this plan
- Expected: SHIP or SHIP WITH CONDITIONS
- Decisions that bind it: none
- **Gate run 1, 2026-10-19 — SHIP WITH CONDITIONS, confidence Medium.** Target: `4b3a86f` to the working tree; touches two bounded contexts and the contract between them (`InvoicePayments`, unchanged). HTTP contract unchanged; callers of `invoice` unchanged (Task 6)
  - Evidence: `./mvnw clean …jacoco prepare-agent verify …report` → `Tests run: 58, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`; `diff_coverage.py --range 4b3a86f` → `16/16` tracked changed lines; `payment`'s hand-written classes from `jacoco.xml`: all lines covered but `payment/domain/Money.java:8` (a malformed currency, unreachable: the currency comes from an invoice `invoice` validated) and `CardToken.java:15` (`toString`, hidden, called by nothing); `detect_test_tampering.sh` → the "removed" assertions are the moved `PayInvoiceTest`: diffed against `4b3a86f`, only package, imports, annotation, client and the spy (`Invoices.recordPayment` → `Payments.add`) differ, 45 assertions before and after; the "loosened" hit is the new stub of the C2 test. `./mvnw spring-boot:run` (port 18080, in-memory) → migrations 1 to 5 applied, `Started CommerceApplication in 2.139 seconds`
  - Found and fixed during the gate: the moved `PayInvoiceTest` had lost its U+00A0 blank token (`payRequest(" ")` was written as a plain space); restored, the diff then shows only the import; suite rerun: `Tests run: 58, Failures: 0`
  - Findings: [Medium] `src/test/java/com/commerce/payment/PaymentMigrationTest.java` breaks `.claude/rules/backend-testing.md` ("one test class per use case. The single architecture test class tests no use case and sits outside this rule"): a second class that tests no use case. [Low] V5 drops `charge_reference` and `paid_at`: one-way, accepted (TL, 2026-10-19); the local file database migrates on the next run. [Low] P-1 (`docs/architecture/patterns-grid.md`) still names BR-5 and BR-6 among `invoice`'s moves; the ruling holds (its reopening condition is not met) but its text cites rules now `payment`'s
  - Skipped: performance (one extra insert per pay, no new query in a loop), frontend, async (none)
  - Not verified: the look at the running application — `curl` not permitted in this session; the TL's look of 2026-10-19 predates this code and does not cover it. No run of the suite at `4b3a86f` to compare (it passed at its commit: 55 tests, preconditions plan)
  - Conditions: G1 — the TL's look on this working tree (`## Blockers`). G2 — the owner rules on `PaymentMigrationTest` against the testing rule (`## Blockers`). G3 — the README realigned (lines 27 and 29: where the rules are; V4 and V5): this task, `partition-readme`
- G3 done 2026-10-19 (`partition-readme`): README line 27 names the three files that hold the rules (`invoice`, `payment`, the area file); line 29 adds V4 and V5 and that no version earlier than V5 runs after them. API, build and prerequisites unchanged, verified against `pom.xml` and the contract
- Levels: BR-23 (`invoice`), BR-24 and the pay's rules (`payment`), AR-1 and AR-2 (area file) were written by the preconditions plan and the code implements them as written; the contract exception lives in `Contracts with neighbours` of both specs and in `ArchitectureTest`; the design choices (`RecordPayment`, the port's own types, one generator run per tag, the time taken just before the commit) stay in `## Decisions`, no level of their own. Area file: no entry added or changed. Invariant register: no entry, no candidate — nothing spans two areas or two layers. P-1's text citing BR-5 and BR-6: reported (finding), not edited — the grid is the architect's
- C4: done in Task 7. Second gate run: due, since the README was written by the close; held after G1 and G2, so it covers them too
- G1 met: the TL's look on this working tree, every answer as expected (`## Decisions`). G2 met in its source: `.apm/instructions/backend-testing.instructions.md` line 6 admits one test class per migration that moves data (`partition-rules`); `apm install` was not approved in this session. P-1: the architect's reading added to the ruling's reason in `docs/architecture/patterns-grid.md`
- **Gate run 2, 2026-10-19 — SHIP WITH CONDITIONS, confidence High.** Target: `4b3a86f` to the working tree, run 1's change plus `README.md`, `.apm/instructions/backend-testing.instructions.md`, `docs/architecture/patterns-grid.md` and this plan; no code changed since run 1
  - Evidence: `./mvnw clean verify` → `Tests run: 58, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`; the TL's look on this working tree (G1); README lines 27 and 29 checked against the specs' paths and the migrations V4, V5 as they are
  - Findings: run 1's Medium (`PaymentMigrationTest` against the testing rule) settled by the rule's source; [Low] the deployed `.claude/rules/backend-testing.md` still holds the old line (`grep -c "one test class per migration"`: `0`, source `1`) until `apm install`
  - Not verified: the suite at `4b3a86f` (it passed at its commit, 55 tests)
  - Condition: G4 — `apm install`, then the grep gives `1` in `.claude/rules/backend-testing.md`
- G4 met: `apm install` run by the TL, 2026-10-19; `grep -c "one test class per migration" .claude/rules/backend-testing.md` → `1` — as expected. Every condition of both gate runs met; the plan closes
- [x] 2026-10-19

## Decisions

- 2026-10-19 · The design of the move: a port of `payment` with its own types, an outbound adapter of `payment` that calls `InvoicePayments`; one generator run per tag; own copies of shared helpers · architect, TL
- 2026-10-19 · The ArchUnit exception: `InvoicePayments` and the types of its signature, from `payment`'s outbound adapter only, declared in the test from `Contracts with neighbours` of both specs; the layer map gives way for `InvoicePayments` only, and the test says so · architect
- 2026-10-19 · A key of its own; no foreign key across contexts; the data already stored is copied by the migration; two writes in one transaction held by a separate `@Transactional` bean · architect
- 2026-10-19 · A time taken in the transaction just before its commit is the time of that commit · architect
- 2026-10-19 · No version older than V4 or V5 runs after them; no rollback to it · TL
- 2026-10-19 · Types of `invoice` the paying code still imports move with `payment` or are written again on its side for AR-1 and AR-2 · architect
- 2026-10-19 · The TL's look at the running application, for the gate: issue `201`; pay `200`, `PAID`; pay again `409`; cancel the paid invoice `409`; pay `nope` `404`; pay with a blank token `400`; pay a cancelled invoice `409`; no card token in the log · TL
- 2026-10-19 · Tasks 2 to 5 carried out as one change: C1 ties Task 2 to Task 4, and the architecture test fails between Task 2 and Task 5 · the plan's own reading of C1 · rejected: four separate green builds, not reachable
- 2026-10-19 · `Invoice.recordPayment()`, guarded on `ISSUED`, carries the status change for `InvoicePaymentsService`: P-1 says every change of status goes through the aggregate, which the precondition adapter bypassed; P-1 holds, its reopening condition is not met (one "cancelled" in `invoice`, "unpaid again" is `ISSUED`) · rejected: re-ruling P-1
- 2026-10-19 · G1 · The TL's look on this working tree (`./mvnw spring-boot:run`): issue `201`; pay `200`, `PAID`; pay again `409`; cancel the paid invoice `409`; pay `nope` `404`; pay with a blank token `400`; cancel `200`, then pay the cancelled invoice `409`; the log holds no card token — as expected · TL
- 2026-10-19 · G2 · `PaymentMigrationTest` stays; the testing rule admits, beside the architecture test, one test class per migration that moves data · TL · rejected: removing the test
- 2026-10-19 · P-1 holds; its reference to BR-5 and BR-6 now reads `payment`'s rules · architect
- 2026-10-19 · The decisions of the fission are in `docs/plans/invoice-fission-preconditions.md` (`## Decisions`) and in the record; each task above copies those that bind it · this plan waits for the preconditions

## Blockers

None.
