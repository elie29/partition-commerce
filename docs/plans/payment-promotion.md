# Promotion of `payment` to the container `payment-app` — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-20

## Tasks

- [x] Task 1 — 2026-10-20 — The specs first: BR-5, BR-27, BR-32 of `payment`, BR-23 of `invoice`, the contracts with neighbours over `billing-internal` 1.0.0
- [x] Task 2 — 2026-10-20 — The Maven layout: `parent-pom.xml` becomes `pom.xml`, `src/` moves under `commerce-app/`, an empty `payment-app` module
- [x] Task 3 — 2026-10-20 — The rule scopes follow `src/` under `commerce-app/` (`partition-rules`)
- [x] Task 4 — 2026-10-20 — `commerce-app` serves `billing-internal` 1.0.0: `invoice`'s `InvoicePayments` and `fraud`'s `CustomerStatus` over HTTP
- [x] Task 5 — 2026-10-20 — `payment` moves to `payment-app`: HTTP adapters of `PayableInvoices` and `FraudReview`, the record after `invoice`'s answer, its own database and migrations from `V1`, port 8081
- [x] Task 6 — 2026-10-20 — `commerce-app` loses `payment`: a new migration drops the `payment` and `refund` tables; its API keeps the tag `Invoice`
- [x] Task 7 — 2026-10-20 — The tests: `payment-app` fakes the HTTP client of `CustomerStatus`; the architecture test of each app
- [x] Task 8 — 2026-10-20 — Proof that it was a move
- [x] Task 9 — 2026-10-20 — After: C4, `docs/partition.yml`, budget, the record
- [x] Task 10 — 2026-10-20 — Close: gate, levels, README
- [x] Task 11 — 2026-10-20 — A failed call to `commerce-app` names no customer in a log or an error (gate run 1)

## Goal

Carry out the promotion recorded in `docs/architecture/reshapes/2026-10-20-payment-promotion.md`, decided at the workshop of 2026-10-16 (architect, PO): `payment` leaves the container `commerce-app` for a container of its own, `payment-app`, with its own database, and reaches `invoice` and `fraud` through `docs/contracts/billing-internal.openapi.json` 1.0.0, frozen. `billing.openapi.json` 1.3.0 does not change. The bounded context, its spec and its language stay; its rules change only where the specs are changed first (Task 1).

## Task 1 — The specs first: BR-5, BR-27, BR-32 of `payment`, BR-23 of `invoice`, the contracts with neighbours over `billing-internal` 1.0.0

- Scope: `docs/specs/billing/payment/spec.md` (BR-5, BR-27, BR-32, Contracts with neighbours, Open questions), `docs/specs/billing/invoice/spec.md` (BR-23, Contracts with neighbours), `docs/specs/customer/fraud/spec.md` (Contracts with neighbours), through `partition-spec`
- Verification: `grep -n "^| BR-5 \|^| BR-32 \|billing-internal" docs/specs/billing/payment/spec.md; grep -n "^| BR-23 \|billing-internal" docs/specs/billing/invoice/spec.md; grep -n "billing-internal" docs/specs/customer/fraud/spec.md`
- Expected: BR-5 no longer says "in one transaction": `invoice` decides, the first it records holds, `payment` stores only once `invoice` answered recorded, a 409 stores nothing, a payment recorded by `invoice` and not stored by `payment` is the open question until REQ-5; BR-5 and BR-32 name the failure "unreachable or slower than 2 seconds: 500, nothing stored, CardPay not called when before the charge"; BR-23 no longer says "in the caller's transaction"; the three specs name `billing-internal` 1.0.0, over HTTP, in their contracts with neighbours; `invoice`'s line says an identifier that names no invoice gets 404 on `getInvoiceCustomer`
- Decisions that bind it: 2026-10-20 · the record of a pay (architect); the failure of a call (architect, TL); `getInvoiceCustomer` 404 (architect); a refund's read of the invoice failing (architect)
- Written 2026-10-20: `payment` BR-5 (effect and source), BR-27 (the read of the invoice failing), BR-32 (2 seconds), both contract lines, the open question on the charge kept nowhere; `invoice` BR-23 (effect and source), its `payment` contract line (404); `fraud` its `payment` contract line. No other line of `docs/specs` says "in process", "one transaction" or "caller's transaction" but BR-23's source note
- Result: the greps show BR-5 and BR-32 at `payment/spec.md:58,71`, `billing-internal` at `:99,100`; BR-23 at `invoice/spec.md:76`, `billing-internal` at `:99`; `billing-internal` at `fraud/spec.md:90` — as expected. Wording validated by the architect, 2026-10-20; BR-27's line is the architect's answer of 2026-10-20 as given
- [x] 2026-10-20

## Task 2 — The Maven layout: `parent-pom.xml` becomes `pom.xml`, `src/` moves under `commerce-app/`, an empty `payment-app` module

- Scope: `pom.xml` (the content of `parent-pom.xml`, as the TL wrote it); `parent-pom.xml` (goes: it became `pom.xml`); `commerce-app/pom.xml` (the current `pom.xml`, its parent the root, artifact `commerce-app`, the contracts read at `${maven.multiModuleProjectDirectory}/docs/contracts/`); `src/` → `commerce-app/src/` (`git mv`); `payment-app/pom.xml` (a jar module with no source, filled by Task 5); `commerce-app/src/test/java/com/commerce/ArchitectureTest.java` (reads `../docs/partition.yml`; a glob's source root is whatever ends in `src/main/java/`); `docs/partition.yml` (every `code` and `layers` glob prefixed with `commerce-app/`)
- Verification: `./mvnw -B clean verify 2>&1 | grep -E "Reactor Summary|SUCCESS|FAILURE|Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+$"`, then `git status --short | grep -v "^R " | head`
- Expected: the reactor builds `commerce`, `commerce-app`, `payment-app`, each `SUCCESS`; `Tests run: 191, Failures: 0, Errors: 0, Skipped: 0` as before the move (baseline of 2026-10-20: 191, `BUILD SUCCESS`); every file of `src/` shows as a rename, the only other changes are the files of the scope
- Decisions that bind it: 2026-10-20 · the layout (TL); `docs/` at the root, read by both modules (TL); `payment-app` an empty module until Task 5
- Result: `Tests run: 191, Failures: 0, Errors: 0, Skipped: 0`; reactor `commerce` SUCCESS, `commerce-app` SUCCESS, `payment-app` SUCCESS; `BUILD SUCCESS`. `git status`: renames only under `src/` → `commerce-app/src/`; `RM` `pom.xml` → `commerce-app/pom.xml` and `ArchitectureTest.java` (renamed and edited); `M docs/partition.yml`; new `pom.xml`, `payment-app/`; the Task 1 specs, the plan and the record — as expected
- [x] 2026-10-20

## Task 3 — The rule scopes follow `src/` under `commerce-app/`

- Scope: `.apm/instructions/*.instructions.md` (`applyTo`), the source; `.claude/rules/*.md` (`paths`) redeployed by `apm install`, never edited by hand. The rules of every bounded context (architecture, security, testing, database, configuration, api-contract) scoped to `commerce-app/…` and `payment-app/…`; those of one bounded context (`-invoice`, `-catalog`, `-customer`) to `commerce-app/…` only. The text of no rule changes
- Verification: `apm install`; then `grep -hE "^  - \"" .claude/rules/*.md | sort -u`; then `ls -d commerce-app/src/main/java/com/commerce/{invoice/domain,product,fraud} commerce-app/src/main/resources/db/migration commerce-app/src/test/java commerce-app/src/main/resources`
- Expected: `apm install` succeeds; no deployed glob starts with `src/`; every glob starts with `commerce-app/`, `payment-app/` or `docs/contracts/`; the `commerce-app` paths all exist (the `payment-app` ones match nothing until Task 5, by design)
- Decisions that bind it: 2026-10-20 · the layout (TL)
- Done 2026-10-20: the ten `applyTo` of `.apm/instructions/` rewritten as the scope says; `apm install` run by the TL
- Result: the deployed globs are `commerce-app/…` (10), `payment-app/…` (8) and `docs/contracts/**`; none starts with `src/`; every `commerce-app` path checked exists but `commerce-app/src/test/resources/**`, which matches nothing, as before the move (`src/test/resources` did not exist); `.claude/rules/` and `apm.lock.yaml` modified by `apm install` — as expected
- [x] 2026-10-20

## Task 4 — `commerce-app` serves `billing-internal` 1.0.0

- Design (2026-10-20):
  - `commerce-app/pom.xml`: two executions of the generator on `billing-internal.openapi.json`: `invoice-internal`, `FILTER=tag:InvoicePayments`, models `Invoice`, `InvoiceCustomer`, `Line`, `Money`, into `com.commerce.invoice.inbound.internal.api`; `fraud`, `FILTER=tag:CustomerStatus`, model `CustomerStatus`, into `com.commerce.fraud.inbound.api`
  - `invoice.inbound.InvoicePaymentsController` implements `InvoicePaymentsApi` (GoF Adapter, HTTP over the port `InvoicePayments`): the identifier read by `GetInvoiceUseCase` (404 when not a UUID or no invoice, as `getInvoice`), then `InvoicePayments.customerOf` / `recordPayment`. `recordPaymentCancelled` not overridden: the generated default answers 501 until REQ-5. `InvoicePaymentsErrors`, scoped to it: `InvoiceNotFound` 404, `InvoiceNotIssued` 409, no body
  - `fraud.inbound.CustomerStatusController` implements `CustomerStatusApi` over `CustomerStatus`: `{blocked}`; a failure of the store reaches the framework, 500
  - Javadoc of `InvoicePayments` and `CustomerStatus`: no more "in process", no more "in the caller's transaction" (Task 1)
  - Tests: `InvoicePaymentsTest` gains the HTTP operations; `CustomerStatusTest` the HTTP read, on a server (`@FraudApiTest`, as `@InvoiceApiTest`)
- Scope: `commerce-app/pom.xml`; `commerce-app/src/main/java/com/commerce/invoice/inbound/InvoicePaymentsController.java`, `InvoicePaymentsErrors.java`; `commerce-app/src/main/java/com/commerce/fraud/inbound/CustomerStatusController.java`; `invoice/application/InvoicePayments.java`, `fraud/application/CustomerStatus.java` (javadoc); `commerce-app/src/test/java/com/commerce/invoice/InvoicePaymentsTest.java`, `InvoiceClient.java`; `commerce-app/src/test/java/com/commerce/fraud/CustomerStatusTest.java`, `FraudApiTest.java`
- Verification: `./mvnw -B clean verify 2>&1 | grep -E "BUILD|Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+$|in com.commerce.(invoice.InvoicePaymentsTest|fraud.CustomerStatusTest)"`
- Expected: `BUILD SUCCESS`; `InvoicePaymentsTest` 13 tests (8 + 5 HTTP), `CustomerStatusTest` 7 (5 + 2 HTTP); total 198, `Failures: 0, Errors: 0`
- Decisions that bind it: 2026-10-20 · `getInvoiceCustomer` 404 (architect); the contract frozen, the how open (contract 1.0.0); the ports (architect, TL); `recordPayment` and `recordPaymentCancelled` read the identifier first (project rule); `recordPaymentCancelled` 501 until REQ-5
- Result: `Tests run: 13 … in com.commerce.invoice.InvoicePaymentsTest`, `Tests run: 7 … in com.commerce.fraud.CustomerStatusTest`, `Tests run: 198, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` — as expected. `InvoicePaymentsTest.records_a_payment_changing_the_status_only_in_the_callers_transaction` still passes (Spring joins a surrounding transaction) but tests what Task 1 took out of the contract: revisited in Task 7
- [x] 2026-10-20

## Task 5 — `payment` moves to `payment-app`

- Design (2026-10-20):
  - `payment-app/pom.xml`: the dependencies of `commerce-app` but Lombok (unused by `payment`); the generator: `payment` (server, tags `Payment`, `Refund` of `billing.openapi.json`, moved from `commerce-app`, which keeps its own copy until Task 6) and `billing-internal` (client, `spring` with library `spring-http-interface`: Spring `@HttpExchange` interfaces `InvoicePaymentsApi`, `CustomerStatusApi` in `com.commerce.payment.outbound.internal.api`, no supporting files, no new dependency)
  - `git mv` of `commerce-app/src/main/java/com/commerce/payment/**` and of `commerce-app/src/test/java/com/commerce/payment/**` but `PaymentMigrationTest`, to `payment-app/`; `com.commerce.PaymentApplication`; `com.commerce.JsonConfiguration` copied
  - `payment.outbound`: `InvoicePaymentsAdapter` and `FraudCustomerStatus` keep their names and ports and speak the generated client instead of the in-process contracts: a 404 is an empty `find`/`customerOf`, a 409 of `recordPayment` is `InvoiceNoLongerIssued`; any other answer, a refused connection or 2 seconds without an answer reaches the use case as an exception, 500 (BR-5, BR-27, BR-32). `BillingInternalClient` (configuration): one `RestClient` at `commerce.billing-internal.base-url`, connect and read timeouts of 2 seconds, `HttpServiceProxyFactory` for both interfaces
  - `RecordPayment`: no transaction across the two writes; `invoice` records first, the payment is stored once it answered (BR-5, Task 1)
  - `payment-app/src/main/resources`: `application.yaml` (name `payment`, port 8081, `./.local/h2/payment`, `commerce.billing-internal.base-url: http://localhost:8080`); `db/migration/V1__create_payment.sql`, `V2__create_refund.sql`: the tables as `commerce-app`'s V4 and V6 left them, no data copied
  - Tests: `CommerceAppStub` (JDK `HttpServer`, a random port, set as `commerce.billing-internal.base-url`) holds the invoices and the blocked customers and answers the five operations of 1.0.0; a test issues, cancels and reads an invoice, places and lifts a review through it, and makes an operation slow (> 2 s), failing (500) or unreachable. `PayInvoiceTest`, `RefundInvoiceTest` keep their cases on the stub; the case "the record fails" now expects the invoice `PAID` (BR-5, Task 1); added: `invoice` slow on each read and on the record, `CustomerStatus` slow, `commerce-app` unreachable
  - `commerce-app` tests that reached a paid invoice through `/pay` (`IssueInvoiceTest:138`, `CancelInvoiceTest:80,99`, `InvoicePaymentsTest:111`) reach it through `POST /internal/invoices/{id}/payment`
  - `docs/partition.yml`: `payment`'s globs to `payment-app/`
- Scope: `payment-app/**`; `commerce-app/src/main/java/com/commerce/payment/**` (leaves), `commerce-app/src/test/java/com/commerce/payment/**` but `PaymentMigrationTest` (leaves); the four `commerce-app` tests above and `InvoiceClient.java`; `docs/partition.yml`
- Verification: `./mvnw -B clean verify 2>&1 | grep -E "Reactor Summary|SUCCESS|FAILURE|Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+$|in com.commerce.payment"`, then `grep -rln "com.commerce.invoice\|com.commerce.fraud" payment-app/src`
- Expected: `BUILD SUCCESS`, each module SUCCESS, no failure, no error; in `payment-app`: `PayInvoiceTest` 20 + the new failure cases, `RefundInvoiceTest` its count + its new failure cases; in `commerce-app`: 198 less the moved payment tests, `PaymentMigrationTest` 1 still passing; the grep finds nothing
- Result: `commerce-app` `Tests run: 162, Failures: 0, Errors: 0, Skipped: 0` (198 − 20 − 16 moved), `PaymentMigrationTest` 1 passing; `payment-app` `Tests run: 40, Failures: 0, Errors: 0, Skipped: 0`: `PayInvoiceTest` 23 (20 + slow reads, slow record, unreachable), `RefundInvoiceTest` 17 (16 − the V4 case + slow reads, unreachable); reactor `commerce`, `commerce-app`, `payment-app` SUCCESS, `BUILD SUCCESS`; the grep prints nothing — as expected
- Files moved with `mv`, not `git mv` (the sandbox refused `git mv` from the repository root): the owner's `git add -A` lets Git detect the renames, which Task 8 relies on
- [x] 2026-10-20
- Decisions that bind it: 2026-10-20 · the record of a pay (architect); the failure of a call, a refund's read included (architect, TL); the migrations (TL); each app its own operations and port (architect); the ports and the base URL (architect, TL)

## Task 6 — `commerce-app` loses `payment`

- Scope: `commerce-app/src/main/resources/db/migration/V12__payment_leaves_for_payment_app.sql` (drops `payment`, `refund`, `refund_cap`; V1 to V11 untouched); `commerce-app/src/test/java/com/commerce/payment/PaymentMigrationTest.java` (goes); `commerce-app/pom.xml` (the execution `payment` of the generator goes: the tags `Payment`, `Refund` are `payment-app`'s)
- Verification: `./mvnw -B clean verify 2>&1 | grep -E "Reactor Summary|SUCCESS|FAILURE|Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+$"`; then `git diff --stat -- commerce-app/src/main/resources/db/migration/V1__create_invoice.sql commerce-app/src/main/resources/db/migration/V11__create_fraud_review.sql; git status --short commerce-app/src/main/resources/db/migration`; then `grep -rn "com.commerce.payment\|payment.inbound" commerce-app/src commerce-app/pom.xml`
- Expected: `commerce-app` `Tests run: 161` (162 − `PaymentMigrationTest`), `payment-app` 40, no failure, `BUILD SUCCESS`; the migrations show only `?? V12__payment_leaves_for_payment_app.sql`; the grep finds `ArchitectureTest` only (its contracts of `payment`, Task 7)
- Result: `commerce-app` `Tests run: 161, Failures: 0, Errors: 0, Skipped: 0`, `payment-app` `Tests run: 40, Failures: 0, Errors: 0, Skipped: 0`, reactor all SUCCESS, `BUILD SUCCESS` — as expected. `git status` limited to the folder shows V1–V11 as `A` (the rename seen from its new path) and `?? V12…`; `git diff --cached -M --name-status`: V1–V11 `R100`, no merged migration edited. The grep prints nothing: differs from the expected, `ArchitectureTest` names `payment` by its hexagon id `payment/payment` and `com.commerce.invoice…` types, not by `com.commerce.payment` — its contracts of `payment` are Task 7's all the same
- [x] 2026-10-20
- Decisions that bind it: 2026-10-20 · the migrations (TL); each app its own operations (architect)

## Task 7 — The tests

- Done in Task 5: `payment-app`'s tests on `CommerceAppStub`; `commerce-app`'s keep placing and lifting reviews through `fraud`'s use cases (unchanged)
- Design (2026-10-20): `commerce-app`'s `ArchitectureTest`: `CONTRACTS` empty, no contract in process is left (both went over HTTP, Task 1); its javadoc says so. `payment-app/src/test/java/com/commerce/ArchitectureTest.java`: a copy (no shared module, TL), `CONTRACTS` empty: `payment` reaches `commerce-app` only through the generated client, in its outbound layer, which the layer map already confines. `InvoicePaymentsTest`: the case "in the caller's transaction" becomes "changing the status only", without the surrounding transaction, the class javadoc loses "joins the caller's transaction"
- Scope: `commerce-app/src/test/java/com/commerce/ArchitectureTest.java`, `payment-app/src/test/java/com/commerce/ArchitectureTest.java`, `commerce-app/src/test/java/com/commerce/invoice/InvoicePaymentsTest.java`
- Verification: `./mvnw -B clean verify 2>&1 | grep -E "Reactor Summary|SUCCESS|FAILURE|Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+$|in com.commerce.ArchitectureTest"`; then, to show the payment-app test is not empty, a probe: an import of `com.commerce.invoice.application.InvoiceView` added to a class of `payment.application` must make `payment-app`'s `ArchitectureTest` fail — run with the probe in place, then removed
- Expected: `commerce-app` 161, `payment-app` 44 (40 + 4 of `ArchitectureTest`), `BUILD SUCCESS`; with the probe: it does not compile, since `invoice` is not on `payment-app`'s classpath — the strongest form of the rule; so the probe used is a class `payment.application` importing `payment.outbound`, which the layer map forbids: `ArchitectureTest` fails, then passes once removed
- Result: `commerce-app` `Tests run: 161, Failures: 0, Errors: 0, Skipped: 0` (its `ArchitectureTest` 4), `payment-app` `Tests run: 44, Failures: 0, Errors: 0, Skipped: 0` (its `ArchitectureTest` 4), `BUILD SUCCESS`. Probe `payment.application.ArchitectureProbe` with a field of `outbound.internal.api.CustomerStatusApi`: `Tests run: 4, Failures: 1 … Field <…ArchitectureProbe.forbidden> has type <…CustomerStatusApi>`, `BUILD FAILURE`; probe deleted: `Tests run: 4, Failures: 0`, `BUILD SUCCESS` — as expected
- [x] 2026-10-20
- Decisions that bind it: 2026-10-20 · the tests (TL)

## Task 8 — Proof that it was a move

- Scope: the record, `## Proof that it was a move`; nothing of the code. The real index is not touched: a scratch copy of it takes `git add -A` so that Git detects the renames of the files moved with `mv`
- Verification: `git diff --cached -M --name-status HEAD` on the scratch index, the main code only (`src/main`, `commerce-app/src/main`, `payment-app/src/main`)
- Expected: every file of `payment`'s use cases, domain and inbound adapters, and every main file of `invoice`, `fraud`, `product`, `R100`, moved as they were; changed or added only the wiring: the outbound adapters of `payment` that speak the frozen contract (`InvoicePaymentsAdapter`, `FraudCustomerStatus`, `BillingInternalClient`, new), the inbound adapters of `commerce-app` that serve it (`InvoicePaymentsController`, `InvoicePaymentsErrors`, `CustomerStatusController`, new), `PaymentApplication`, `JsonConfiguration` (copy), `application.yaml`, the migrations (`payment-app` V1, V2; `commerce-app` V12); and javadoc only in `InvoicePayments`, `CustomerStatus`, `PayableInvoices`, `Payments`, `JdbcPayments`. One file outside the wiring, known beforehand: `RecordPayment`, changed by the rule (BR-5 of Task 1), not by a dependency
- The scratch index could not be written (the sandbox refused the copy of `.git/worktrees/s17/index`, `git archive` and a script): verified instead file by file with `git show HEAD:<path> | diff -q - <moved path>`, plus `git diff --cached -M` for the `git mv` of Task 2
- Result: as expected, written in the record, `## Proof that it was a move`: `commerce-app/src/main` all `R100` from `src/main` plus javadoc-only `CustomerStatus`, `InvoicePayments`, the three new inbound adapters and V12; `payment-app`: 35 files identical to `HEAD` (`PayInvoiceUseCase`, `RefundInvoiceUseCase` among them), javadoc-only `PayableInvoices`, `Payments`, `JdbcPayments`, `RecordPayment` as foreseen, the two outbound adapters rewritten over the generated client, the new wiring files. No caller of a port changed
- [x] 2026-10-20
- Decisions that bind it: none

## Task 9 — After

- Scope: `docs/architecture/model.yml` and its views (`partition-c4`), `docs/partition.yml`, `docs/budget/` (`partition-budget`), the record
- Verification: `node .claude/skills/partition-c4/scripts/c4-drawio.mjs --check`, then without `--check`; the budget record of `partition-budget`
- Expected: the model valid; `payment` a container of `commerce` (port 8081), `Payment H2` its database, `customer -> payment: pays`, `accountant -> payment: refunds`, `accountant -> commerce-app: issues, cancels`, `payment -> invoicing` and `payment -> fraud` over HTTP, `billing-internal` 1.0.0; the two dynamic views over HTTP, the payment stored in `Payment H2` after `invoice`'s answer; `docs/partition.yml` already carries `payment`'s globs under `payment-app/` (Task 5), nothing else to declare (a promotion creates no bounded context); the budget measured
- C4 result (2026-10-20): `--check`: `docs/architecture/model.yml: 11 elements, 10 relations, 7 views, no error`; generation: 7 pages, "C4 2 - Containers" 8 elements, "C4 3 - Components of Commerce app" 6, "Dynamic - Pay an invoice by card" 7, "Dynamic - Refund a paid invoice" 6; 0 hand-set positions kept on every page (none had been set) — as expected. `h2`'s description becomes "The database of Commerce app", the only label of the architect's drawing changed: it was "The database", no longer true alone
- Budget result: `docs/budget/2026-10-20.md` replaced (one record per day): `payment` 7 786 (24 %), `invoice` 6 465 (20 %), `product` 7 981 (25 %), `customer` 2 873 (9 %), `fraud` 4 806 (15 %), all ok; this plan read on resume 765 of 7 388. The record's `## After` ticked — as expected
- [x] 2026-10-20
- Decisions that bind it: none

## Task 10 — Close

- Scope: `partition-gate` on the plan's changes, the levels, the README (`partition-readme`): what users run changes (two jars, ports 8080 and 8081, `commerce.billing-internal.base-url`)
- Verification: the verdict of `partition-gate`
- Expected: SHIP or SHIP WITH CONDITIONS
- Decisions that bind it: none
- Gate run 1 (2026-10-20), target: working tree against `d32a5f8`, untracked `payment-app/` included by hand (the collector diffs tracked files only). **Verdict: HOLD**, confidence Medium: build and all tests run, no diff coverage, no end-to-end run of the two apps
  - Evidence: `./mvnw -B clean verify` → `commerce-app` 161, `payment-app` 44, 0 failures, `BUILD SUCCESS` (Task 7); baseline at `d32a5f8` 191 passed (Task 2); 191 → 205 accounted for (36 moved, 2 removed with reason, 18 added); `detect_test_tampering.sh --range d32a5f8`: nothing skipped, loosened or regenerated, removed assertions are the moved files and the two removals of Task 5 and Task 6; the Task 8 proof
  - [Blocker] BR-32 and BR-31 of `payment` broken: a failed `getCustomerStatus` logs the customer — `payment-app/src/main/java/com/commerce/payment/outbound/FraudCustomerStatus.java:24`. Scenario: `commerce-app` slow or unreachable while a customer under review pays or is refunded; Spring's `ResourceAccessException` carries the URL and the dispatcher logs it at ERROR. Evidence: `PayInvoiceTest#answers_500_without_calling_cardpay_when_a_read_before_the_charge_takes_more_than_2_seconds` prints `I/O error on GET request for "http://localhost:41027/internal/customers/customer-1/status": Request cancelled`. Fix: Task 11
  - [Blocker] The internal API is open to every caller of `commerce-app`: `POST /internal/invoices/{id}/payment` records an invoice paid with no charge; `GET /internal/customers/{id}/status` tells anyone who is under review — `commerce-app/…/invoice/inbound/InvoicePaymentsController.java`, `…/fraud/inbound/CustomerStatusController.java`, on 8080 beside the public API. Scenario: anyone holding an invoice identifier marks it `PAID` without paying (BR-5: an invoice is paid once its amount is paid in full). Evidence: `InvoicePaymentsTest#records_a_payment_over_http_then_refuses_it_once_the_invoice_is_paid` gets 200 with no credential. Before the promotion these calls existed only in process. Fix: how `billing-internal` is reached only by `payment-app` — a person's decision (blocker)
  - [Medium] Deploy order: a `payment-app` started against a `commerce-app` without `/internal` gets 404 and answers every pay and refund 404 (no charge, safe but wrong); `commerce-app` with V12 first, then `payment-app`
  - [Medium] V12 drops `payment`, `refund`, `refund_cap`: one-way; the previous artifact cannot be redeployed on a migrated database (no data kept, TL)
  - [Low] `payment-app` copies `commerce-app`'s H2 console, enabled, user `sa` with no password: the pattern already in place, now on 8081 too
  - Not verified: the two jars running together (starting them needs the owner's approval); the JDK client against Tomcat (tests use the JDK `HttpServer` stub); diff coverage (no coverage tool in the build); load and the 2-second limit under load; callers of `/invoices/{id}/pay` and `/refunds` at 8080 outside the repository, which now get 404
  - Skipped classes: frontend (none); performance (one more HTTP call per pay, three per refund, bounded at 2 s each: not measured)
- Gate run 2 (2026-10-20), same target, Task 11 and the two open questions included. **Verdict: SHIP WITH CONDITIONS**, confidence Medium (as run 1: no diff coverage, no end-to-end run, the latter accepted by the TL)
  - Evidence: `./mvnw -B clean verify` → `commerce-app` `Tests run: 161, Failures: 0, Errors: 0, Skipped: 0`, `payment-app` `Tests run: 46, Failures: 0, Errors: 0, Skipped: 0`, reactor all SUCCESS, `BUILD SUCCESS`; Task 11's two tests fail without the fix and pass with it
  - Run 1's Blockers: the customer in the log, fixed (Task 11); the open internal API, accepted for this version by the architect and written as an open question in both specs — no longer a Blocker of this version, a risk the owner holds
  - Medium and Low findings of run 1 stand; deploy order and V12 one-way are deployment facts, not defects
  - Condition: [x] the README says the two apps, their ports, `commerce.billing-internal.base-url`, and that `commerce-app` runs first — step 5 of the closing, `partition-readme` (2026-10-20)
- Levels (closing steps 2–4, 2026-10-20). Each statement of `## Decisions`, and where it now lives:
  - In a spec, written during the work: the record of a pay (BR-5 of `payment`, BR-23 of `invoice`); the 2 seconds (BR-5, BR-27, BR-32 of `payment`); the contracts over `billing-internal` (Contracts with neighbours of `payment`, `invoice`, `fraud`); the open internal API (Open questions of `payment` and `invoice`). Written at the closing: on every operation of `invoice`'s contract line the 404, and `recordPaymentCancelled` 501 until REQ-5 (were in the plan only)
  - In a rule file (how code is written), written at the closing in `.apm/instructions/`: the generated client (`backend-api-contract`), no shared module, what both apps need copied (`backend-architecture`), one architecture test class per app (`backend-testing`, its line "the single architecture test class" rewritten), the stub of `commerce-app` (new `backend-testing-payment`, scoped `payment-app/src/test/java/**`)
  - In the README: the two apps, the ports, `commerce.billing-internal.base-url`, the start order, V12 and `payment-app`'s V1, V2
  - In the plan only, by nature: the layout and the `mv` of the files, the empty `payment-app` module of Task 2, the test removed in Task 5, the tests of `commerce-app` on `/internal`, the end-to-end run accepted unverified
  - `AR-` and `INV-` entries added or changed: zero. Candidates rejected: "unreachable or slower than 2 seconds is a failure" binds `payment` alone (BR-5, BR-27, BR-32 of `docs/specs/billing/payment/spec.md`), no area rule; "every amount in minor units" in `payment-app`'s V1, V2: already INV-DATA-001 of `docs/specs/invariants.md`
- README: realigned with `partition-readme` (Build, Run (new), the billing API by app, the internal API, V12)
- Gate run 3 (2026-10-20), the writes of steps 3–5 (specs, rule sources, README; no code since run 2). **Verdict: SHIP WITH CONDITIONS**, confidence Medium, as run 2. Evidence: `c4-drawio.mjs --check` → `11 elements, 10 relations, 7 views, no error`; `budget.mjs --check` exit 0, `invoice` 6 580 (21 %), `payment` 7 891 (25 %), the others unchanged, no rule without a scope; the two jars the README names exist under `*/target/`; the code is the one gate run 2 built (161 + 46, `BUILD SUCCESS`). Condition: [x] `apm install`, so that `.claude/rules/` carries the four rule lines and `backend-testing-payment` — run by the TL; checked: `.claude/rules/backend-testing-payment.md` scoped `payment-app/src/test/java/**`, the new line in `backend-api-contract.md`, `backend-architecture.md`, `backend-testing.md`; `budget.mjs --check` exit 0, every session 2 056 (no rule without a scope), `payment` 7 980 (25 %), its scoped rules 314 → 403 (2026-10-20).
- [x] 2026-10-20 `docs/budget/2026-10-20.md` stays at its figures of Task 9 (`payment` 7 786): the open question of the closing adds 105, recorded here

## Task 11 — A failed call to `commerce-app` names no customer in a log or an error

- From gate run 1 (Blocker, BR-31, BR-32)
- Scope: `payment-app/src/main/java/com/commerce/payment/outbound/FraudCustomerStatus.java` and a new `CustomerStatusUnavailable` beside it (a `RestClientException` becomes it: its message names the kind of failure, no URL, no cause kept); `PayInvoiceTest`, `RefundInvoiceTest` (`getCustomerStatus` slow, then failing, for the customer under review: 500 each, and the output does not contain it)
- Verification: `./mvnw -B -pl payment-app clean verify 2>&1 | grep -E "BUILD|Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+$|in com.commerce.payment.(PayInvoiceTest|RefundInvoiceTest)"`; first with the two new tests and without the fix, then with the fix
- Expected: without the fix, the two new tests fail (the output contains `customer-under-review`); with it, `PayInvoiceTest` 24, `RefundInvoiceTest` 18, `payment-app` 46, 0 failures, `BUILD SUCCESS`
- Decisions that bind it: BR-31, BR-32 of `payment`
- Result: without the fix, `PayInvoiceTest` `Tests run: 24, Failures: 1` (`no_output_names_a_customer_under_fraud_review_when_customer_status_fails:344`), `RefundInvoiceTest` `Tests run: 18, Failures: 1` (`…:285`); with it, `Tests run: 24, Failures: 0`, `Tests run: 18, Failures: 0`, `payment-app` `Tests run: 46, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` — as expected. `InvoicePaymentsAdapter`'s failures keep their URL: it names an invoice identifier only, which BR-31 allows
- [x] 2026-10-20

## Decisions

- 2026-10-20 · The promotion: `payment` from a component of `commerce-app` to a container of its own, `payment-app` (architect, PO, workshop of 2026-10-16) · CardPay's volumes and its release rhythm differ from the rest
- 2026-10-20 · The record of a pay (architect): `invoice` decides; of two `recordPayment`, or a pay and a cancel, the first it records holds; `payment` stores its payment only once `invoice` answered recorded; a 409 stores nothing; a payment `invoice` recorded that `payment` then failed to store stays open until REQ-5, with the charge a lost record leaves; BR-5 and BR-23 change through `partition-spec` before the code · two databases cannot hold one transaction · rejected: one transaction across the two apps
- 2026-10-20 · The migrations (TL): the payment rows are not copied, no instance holds data to keep; `payment-app`'s migrations create `payment` and `refund` from `V1`; a new migration of `commerce-app` drops them; no merged migration is edited · rejected: copying the rows
- 2026-10-20 · Each app serves its own operations of `billing.openapi.json`, unchanged: `commerce-app` the tag `Invoice`, `payment-app` the tags `Payment` and `Refund`, each on its own port; no gateway in this version (architect)
- 2026-10-20 · `getInvoiceCustomer`, an identifier that names no invoice: 404, no body, as `getInvoice` (architect) · not declared by 1.0.0, which stays frozen: enforced by the code, as the project does for what a contract does not declare
- 2026-10-20 · The failure of a call to `commerce-app`: unreachable or slower than 2 seconds is a failure, BR-32's 500 and BR-5's 500, nothing stored, CardPay not called when the call comes before the charge (architect, TL)
- 2026-10-20 · The tests (TL): `payment-app`'s tests answer `CustomerStatus` through a fake of its HTTP client; `commerce-app`'s tests keep placing and lifting reviews through `fraud`'s use cases
- 2026-10-20 · The layout (TL): `parent-pom.xml`, with modules `commerce-app` and `payment-app`, replaces the current `pom.xml` during the move
- 2026-10-20 · The spec changes of Task 1 validated as written (architect)
- 2026-10-20 · A refund whose read of the invoice is unreachable or slower than 2 seconds answers 500, nothing stored, CardPay not called (architect) · written into BR-27 of `payment`, the rule on the refund's invoice
- 2026-10-20 · `src/` moves under `commerce-app/`; `docs/` stays at the root, read by both modules from the repository root (TL)
- 2026-10-20 · Ports: `commerce-app` keeps 8080, `payment-app` takes 8081; `payment-app` finds `commerce-app` through `commerce.billing-internal.base-url` in its `application.yaml`, `http://localhost:8080` by default (architect, TL)
- 2026-10-20 · `payment-app` enters the reactor in Task 2 as an empty jar module, so that `parent-pom.xml` lands as the TL wrote it; Task 5 fills it · rejected: listing only `commerce-app` until Task 5 (would edit the TL's file)
- 2026-10-20 · The rule scopes (`applyTo`, `paths`) name `src/…`, which the move empties: they move with it, Task 3, through `partition-rules` · without it, no rule loads for the moved code
- 2026-10-20 · `ArchitectureTest` and `docs/partition.yml` change in Task 2, not Task 9: the test reads the globs of `docs/partition.yml` and lets a rule that matches no class pass, so stale globs would pass in silence

- 2026-10-20 · `recordPayment` and `recordPaymentCancelled` over HTTP read the identifier of their path first: not a UUID or no invoice, 404, no body · `.claude/rules/backend-api-contract.md`: "the identifiers of its path (404, one that is not a UUID included)" before the rules on what is held · rejected: 409 for an unknown invoice, as the in-process `recordPayment` throws `InvoiceNotIssued`
- 2026-10-20 · `recordPaymentCancelled` over HTTP answers the generated default, 501, until REQ-5 builds it · the in-process contract throws `UnsupportedOperationException` until then; 1.0.0 says "built with REQ-5" · rejected: a 500 · to be confirmed by the architect
- 2026-10-20 · The HTTP adapter of `invoice` reads the invoice through `GetInvoiceUseCase` · the 404 of `getInvoice`, the same identifier reading · rejected: making `InvoiceIds` public

- 2026-10-20 · `recordPaymentCancelled` keeps the generated 501 until REQ-5 (architect) · confirms the decision above
- 2026-10-20 · `payment-app`'s tests fake `commerce-app` entirely, the invoice operations and the customer status, with one stub (TL)
- 2026-10-20 · The stub is an HTTP server the real client calls, on the JDK's `com.sun.net.httpserver.HttpServer`, answering as `billing-internal` 1.0.0 says; it tests the 2-second limit, the 404 and the 409; no new dependency (TL) · rejected: a fake bean of the client port
- 2026-10-20 · `payment-app` calls `billing-internal` 1.0.0 through a client generated from the contract, as the server side is (TL) · rejected: a hand-written `RestClient` adapter
- 2026-10-20 · `JsonConfiguration` is copied into `payment-app`; no shared module in this version (TL)
- 2026-10-20 · `PaymentMigrationTest` stays in `commerce-app` and goes in Task 6, with the migration that drops the payment tables (TL)

- 2026-10-20 · `RefundInvoiceTest.refunds_a_payment_copied_by_v4` is removed · it tested rows `commerce-app`'s V4 copied; `payment-app` copies no data and has no V4 (TL, the migrations) · rejected: keeping it against a payment row written by hand, which would test nothing V4 does
- 2026-10-20 · The stub also answers `commerce-app`'s `POST /invoices`, `GET /invoices/{id}`, `POST /invoices/{id}/cancel`, as a test needs to hold an invoice · `commerce-app` faked entirely (TL); the tests keep their shape
- 2026-10-20 · The client is generated as Spring `@HttpExchange` interfaces (`spring`, library `spring-http-interface`) over a `RestClient`, connect and read timeouts of 2 seconds · generated from the contract (TL), Spring's own Jackson, no new dependency · rejected: the `java` generator with `restclient`, which brings its own `ApiClient` and Jackson 2

- 2026-10-20 · The internal API stays open, with no restriction, in this version (architect) · a demo project, not intended for production; the public API has no login either · it stays an open question in the specs of `payment` and `invoice` until the apps are deployed anywhere, when the internal API gets its own port or a network rule · rejected for now: a port of its own, a network rule, a credential
- 2026-10-20 · The end-to-end run of the two jars is not in this plan and is accepted unverified (TL) · the stub-server tests cover the contract on both sides

- 2026-10-20 · The four statements of the TL that say how code is written go to rule files at the closing, in the TL's words · `partition-plan`, closing step 3: how code is written goes to a rule file scoped to the code it binds · `backend-testing`'s "the single architecture test class" rewritten "the architecture test class of each app", which the TL's copy implies

## Blockers

<!-- none -->
