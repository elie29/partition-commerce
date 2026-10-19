# Fission of `invoice`: `payment` comes out — execution plan

> **Status:** in progress
>
> **Resume at:** Task 1 — write the verification and the expected result of Task 1, then generate `payInvoice` into `com.commerce.payment.inbound.api`
>
> **Last updated:** 2026-10-19 · uncommitted

## Tasks

- [ ] Task 1 — `payInvoice` generated into `com.commerce.payment.inbound.api`
- [ ] Task 2 — The pay moves to `com.commerce.payment`, reaching `invoice` through `InvoicePayments` only
- [ ] Task 3 — V4: `payment`'s own table; the invoice row's payment columns go with the move
- [ ] Task 4 — `invoice` loses the pay: `Invoice.pay`, the charge reference, `Invoices.recordPayment`
- [ ] Task 5 — Architecture test: `payment` reaches `invoice` through `InvoicePayments` only
- [ ] Task 6 — Proof that it was a move
- [ ] Task 7 — After: `docs/partition.yml` hexagon reasons, C4, budget, the record
- [ ] Task 8 — Close: gate, levels, README

## Goal

Carry out the fission recorded in `docs/architecture/reshapes/2026-10-19-invoice-fission.md`: the pay (REQ-4) moves from `com.commerce.invoice` to `com.commerce.payment`, behind the frozen `InvoicePayments`, with `payment`'s own table, as the specs written by `docs/plans/invoice-fission-preconditions.md` say. REQ-5 is not built here.

## Task 1 — `payInvoice` generated into `com.commerce.payment.inbound.api`

- Scope: `pom.xml`, `src/main/java/com/commerce/payment/inbound/`
- Verification: to write when the task starts
- Expected: to write when the task starts
- Decisions that bind it: one interface per tag (TL); `payment` answers the contract's `Invoice` schema (architect)

## Task 2 — The pay moves to `com.commerce.payment`, reaching `invoice` through `InvoicePayments` only

- Scope: `src/main/java/com/commerce/payment/**`, `src/test/java/com/commerce/payment/**`
- Verification: to write when the task starts
- Expected: to write when the task starts
- Decisions that bind it: every write of the invoice's state stays in `invoice` (architect); the record of `payment` and `recordPayment` in one transaction, the first to commit holds both (architect); gate condition C1: the pay calls `InvoicePayments.recordPayment` only in the same change as Task 4 (see Task 4)

## Task 3 — V4: `payment`'s own table; the invoice row's payment columns go with the move

- Scope: `src/main/resources/db/migration/V4__*.sql`, `payment`'s outbound adapter
- Verification: to write when the task starts
- Expected: to write when the task starts
- Decisions that bind it: the time and the charge reference go to `payment`'s own table; `payment` stores the amount charged; the time of a payment is the moment the record of `payment` commits (architect)

## Task 4 — `invoice` loses the pay: `Invoice.pay`, the charge reference, `Invoices.recordPayment`

- Scope: `src/main/java/com/commerce/invoice/**`, `src/test/java/com/commerce/invoice/InvoicePaymentsTest.java`
- Verification: to write when the task starts
- Expected: to write when the task starts
- Decisions that bind it: as Task 2
- Gate condition C1 (preconditions, 2026-10-19): `InvoicePayments.recordPayment` gets its production caller (Task 2) only together with `Invoice` dropping the charge reference on `PAID` (this task): until then a `PAID` row with no charge reference is unreadable (`Invoice.java:33-35`)
- Gate condition C2 (preconditions, 2026-10-19): a test covers `InvoicePaymentsService.java:35`, the refusal when a simultaneous pay or cancel commits between the read and the conditional update
- Carried from the preconditions (Task 3): put back in `records_a_payment_changing_the_status_only_in_the_callers_transaction` the second `recordPayment` refused with `InvoiceNotIssued`, dropped while `Invoice` required a charge reference on `PAID`

## Task 5 — Architecture test: `payment` reaches `invoice` through `InvoicePayments` only

- Scope: `src/test/java/com/commerce/ArchitectureTest.java`
- Verification: to write when the task starts
- Expected: to write when the task starts
- Decisions that bind it: from now on the bounded contexts know each other through their contracts only

## Task 6 — Proof that it was a move

- Scope: none written; the record's `Proof that it was a move`
- Verification: `git diff --stat <commit before Task 1> -- . ':!src/main/java/com/commerce/payment' ':!src/test/java/com/commerce/payment'`
- Expected: to write before it runs: only the wiring
- Decisions that bind it: none

## Task 7 — After: `docs/partition.yml` hexagon reasons, C4, budget, the record

- Gate condition C3 (preconditions, 2026-10-19): `invoice`'s form reason still cites BR-3 to BR-6; rewrite it through `partition-hexagon`
- Scope: `docs/partition.yml` (the reason of `invoice`'s hexagon), `docs/architecture/model.yml` and its views (`partition-c4`), `docs/budget/` (`partition-budget`), the record's `After`
- Verification: to write when the task starts
- Expected: to write when the task starts
- Decisions that bind it: none

## Task 8 — Close: gate, levels, README

- Scope: as `partition-plan` _Close_ says
- Verification: `partition-gate` on the changes of this plan
- Expected: SHIP or SHIP WITH CONDITIONS
- Decisions that bind it: none

## Decisions

- 2026-10-19 · The design of the move: a port of `payment` with its own types, an outbound adapter of `payment` that calls `InvoicePayments`; one generator run per tag; own copies of shared helpers · architect, TL
- 2026-10-19 · The ArchUnit exception: `InvoicePayments` and the types of its signature, from `payment`'s outbound adapter only, declared in the test from `Contracts with neighbours` of both specs; the layer map gives way for `InvoicePayments` only, and the test says so · architect
- 2026-10-19 · A key of its own; no foreign key across contexts; the data already stored is copied by the migration; two writes in one transaction held by a separate `@Transactional` bean · architect
- 2026-10-19 · A time taken in the transaction just before its commit is the time of that commit · architect
- 2026-10-19 · No version older than V4 or V5 runs after them; no rollback to it · TL
- 2026-10-19 · Types of `invoice` the paying code still imports move with `payment` or are written again on its side for AR-1 and AR-2 · architect
- 2026-10-19 · The TL's look at the running application, for the gate: issue `201`; pay `200`, `PAID`; pay again `409`; cancel the paid invoice `409`; pay `nope` `404`; pay with a blank token `400`; pay a cancelled invoice `409`; no card token in the log · TL
- 2026-10-19 · The decisions of the fission are in `docs/plans/invoice-fission-preconditions.md` (`## Decisions`) and in the record; each task above copies those that bind it · this plan waits for the preconditions

## Blockers

None.
