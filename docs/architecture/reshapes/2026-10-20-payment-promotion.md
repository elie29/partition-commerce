# Reshape — promotion: `payment`, from a component of `commerce-app` to a container `payment-app`

> **Decided on:** 2026-10-16, by the architect and the product owner, from workshop of 2026-10-16 (its result is not saved under `docs/sources/`); the answers to the preparation, 2026-10-20 (architect, PO, TL). Carried out by `docs/plans/payment-promotion.md`
>
> **From:** component `payment` of the container `commerce-app` (`docs/architecture/model.yml`) **To:** a container `payment-app` of the system `commerce`, with its own `application.yaml`, H2 database and Flyway migrations (`parent-pom.xml`; TL, 2026-10-20)
>
> **Raised by:** a workshop, 2026-10-16: CardPay's volumes and its release rhythm differ from the rest. Not a budget record: `docs/budget/2026-10-20.md` finds `payment` at 23 %

## Why here

A promotion is not a boundary: `payment` keeps its bounded context, its spec and its language. It changes kind and parent in C4, and what crosses to `invoice` and `fraud` leaves the process.

The domain events the specs and contracts name, in their words:

| Event | Owner | Where named |
| --- | --- | --- |
| An invoice paid (`ISSUED` → `PAID`) | `invoice` | BR-23 of `invoice`; `recordPayment` of `InvoicePayments` and of `billing-internal` 1.0.0 |
| A payment recorded: time, charge reference, amount paid | `payment` | BR-5, BR-24 |
| A card declined | CardPay, answered to `payment` | BR-5, Language "Declined" |
| A payment cancelled; the invoice issued again | `payment`, then `invoice` | REQ-5, BR-6 (not built); `recordPaymentCancelled` |
| A refund requested, sent, settled | `payment` | BR-25, BR-28; Language "Refund" |
| A review placed, lifted; a customer blocked | `fraud` | BR-3, BR-4 of `fraud`; `CustomerStatus` |

Words that carry two meanings across the line (unchanged by the promotion, which keeps both sides as they are):

| Term | Meaning on this side (`payment`) | Meaning on the other side |
| ---- | -------------------- | ------------------------- |
| Cancelled | of a payment: reversed within 24 hours (`payment/spec.md`, Language) | of an invoice: voided before any payment (`invoice/spec.md`, Language) |

No word changes meaning inside `payment`.

## Challenge

Proposal: no workshop result to challenge.

## Integration

What crosses today, in process, and what `billing-internal` 1.0.0 (frozen) makes of it:

| Crosses the boundary | Direction | Form (event, call, file) | Contract |
| -------------------- | --------- | ------------------------ | -------- |
| Read an invoice as `getInvoice` shows it | `payment-app` → `commerce-app` (`invoice`) | HTTP call, `GET /internal/invoices/{invoiceId}` | `billing-internal.openapi.json` 1.0.0, `getInvoiceForPayment` |
| The customer of an invoice | `payment-app` → `commerce-app` (`invoice`) | HTTP call, `GET /internal/invoices/{invoiceId}/customer` | 1.0.0, `getInvoiceCustomer` |
| Record a payment | `payment-app` → `commerce-app` (`invoice`) | HTTP call, `POST /internal/invoices/{invoiceId}/payment` | 1.0.0, `recordPayment`; `invoice` decides, `payment` stores after (answer 2) |
| Record a payment cancelled (REQ-5, not built) | `payment-app` → `commerce-app` (`invoice`) | HTTP call | 1.0.0, `recordPaymentCancelled` |
| Is a customer blocked | `payment-app` → `commerce-app` (`fraud`) | HTTP call, `GET /internal/customers/{customerId}/status` | 1.0.0, `getCustomerStatus` |
| `payInvoice`, `refundInvoice` (tags `Payment`, `Refund`) | caller → `payment-app` | HTTP, on `payment-app`'s own port | `billing.openapi.json` 1.3.0, unchanged (answer 4) |

## Rules

Rules and `Persistence` lines both sides need. `Persistence`: none declared for this project (`payment/spec.md`).

| Rule | Was in | Goes to |
| ---- | ------ | ------- |
| BR-5 (pay recorded with the invoice paid, in one transaction) and BR-23 of `invoice` (conditional record "in the caller's transaction") | `payment`, `invoice` | stays where it is, rewritten: `invoice` decides, `payment` stores once it answered (answer 2) |
| BR-32 of `payment` / BR-3, BR-4 of `fraud` | `payment` consumes, `fraud` owns | stays; "`CustomerStatus` cannot answer: 500" now also covers the network (how, not what) |
| AR-1 (bounded text) | `billing/area.md` | stays in the area file: both apply it, one area |
| INV-DATA-001 (amounts in minor units) | `invariants.md` | stays in the register |

## Preconditions

- [x] Every caller reaches it through an interface: `payment` has no in-process caller (no class outside `com/commerce/payment/` imports it; search below); its callers come over HTTP through `billing.openapi.json`. Its own dependencies go through its ports `PayableInvoices` and `FraudReview`
- [x] One adapter implements each port: `payment/outbound/InvoicePaymentsAdapter.java:20` (over `InvoicePayments`), `payment/outbound/FraudCustomerStatus.java:13` (over `CustomerStatus`); to be replaced by HTTP adapters speaking `billing-internal` 1.0.0
- [x] The contract is written and frozen: `docs/contracts/billing-internal.openapi.json` 1.0.0, frozen (`d32a5f8`); "in the caller's transaction" of `InvoicePayments.recordPayment` (`invoice/application/InvoicePayments.java:24-25`) is dropped by answer 2, not carried into it
- [x] For a fission, the new bounded context has its spec: not a fission
- [ ] Each rule both sides need has one home: yes; BR-5 and BR-23 of `invoice` rewritten for two transactions by Task 1 of the plan (answer 2)
- [x] For a merge, the person who carries the hand-written sections over: not a merge
- [x] For a removal, nothing calls it any more: not a removal

The search: `grep -rln "com.commerce.payment" src --include=*.java | grep -v "/payment/"` — no output.

### The price of the move: dependencies that do not go through an interface

| Where | What | Why it blocks |
| --- | --- | --- |
| `payment/application/RecordPayment.java:24-28` | `@Transactional` holds `invoices.recordPaid` and `payments.add` in one transaction | the invoice's write happens in `commerce-app`'s database: one local transaction can no longer hold both (BR-5, BR-23) |
| `invoice/application/InvoicePayments.java:24-25`; `invoice/application/InvoicePaymentsService.java:35` | "in the caller's transaction" | the frozen HTTP contract has no caller's transaction |
| `src/main/resources/db/migration/V3`–`V6` | one Flyway history writes the invoice table and the `payment` and `refund` tables | `parent-pom.xml` gives each app its own H2 and migrations: the payment and refund tables, and their rows in `./.local/h2/commerce`, have to move |
| `src/main/resources/application.yaml` | one datasource, `./.local/h2/commerce` | `payment-app` needs its own |
| `pom.xml:112-125` | `payment`'s API is generated from `billing.openapi.json` in the one module | moves to `payment-app`'s module |
| `src/test/java/com/commerce/payment/PayInvoiceTest.java:16-18`, `RefundInvoiceTest.java:13-15` | the tests of `payment` import `fraud`'s `CustomerStatus`, `PlaceReviewUseCase`, `LiftReviewUseCase` | they cannot reach `fraud` in-process from `payment-app` |
| `src/test/java/com/commerce/ArchitectureTest.java:125-130` | the in-process contracts `payment` → `invoice`, `payment` → `fraud` | become HTTP contracts |

## Answers of 2026-10-20

1. Raised and decided: workshop of 2026-10-16 (architect, PO), CardPay's volumes and release rhythm.
2. BR-5 and BR-23 (the single transaction, cleared): `invoice` decides; of two `recordPayment`, or a pay and a cancel, the first it records holds. `payment` stores its payment only once `invoice` answered recorded; a `409` stores nothing. A payment `invoice` recorded that `payment` then failed to store stays open until REQ-5, with the charge a lost record leaves. BR-5 and BR-23 change through `partition-spec` before the code (architect).
3. No data is kept: `payment-app`'s migrations create `payment` and `refund` from `V1`; a new migration of `commerce-app` drops them; no merged migration is edited (TL).
4. Each app serves its own operations of `billing.openapi.json`, unchanged: `commerce-app` the tag `Invoice`, `payment-app` the tags `Payment` and `Refund`, each on its own port; no gateway (architect).
5. `getInvoiceCustomer`: an identifier that names no invoice gets 404, no body, as `getInvoice` (architect). 1.0.0 does not declare it and stays frozen: the code enforces it.
6. Unreachable or slower than 2 seconds is a failure: BR-32's 500 and BR-5's 500, nothing stored, CardPay not called when the call comes before the charge (architect, TL).
7. `payment-app`'s tests answer `CustomerStatus` through a fake of its HTTP client; `commerce-app`'s keep placing and lifting reviews through `fraud`'s use cases (TL).
8. `parent-pom.xml` lands beside `pom.xml`, which it replaces during the move (TL).

## Proof that it was a move

Run on 2026-10-20 against `HEAD` (`d32a5f8`), the main code only, before any commit of the move (Task 8 of `docs/plans/payment-promotion.md`).

- Verification: `git diff --cached -M --name-status -- commerce-app/src/main src/main` (the move of `src/` staged by `git mv`); `git diff --name-status -- commerce-app/src/main` and `git status --short --untracked-files=all commerce-app/src/main`; for each file of `payment-app/src/main/java/com/commerce/payment/`, `git show HEAD:src/main/java/com/commerce/payment/<file> | diff -q - payment-app/src/main/java/com/commerce/payment/<file>` (moved with `mv`: Git sees the rename once staged)
- Expected: `payment`'s use cases, domain and inbound adapters, and every main file of `invoice`, `fraud`, `product`, moved as they were; changed or added only the wiring (the outbound adapters of `payment` and the inbound adapters of `commerce-app` over the frozen contract, the applications, their configuration, their migrations) and javadoc; one file outside the wiring, known beforehand: `RecordPayment`, changed by BR-5, not by a dependency
- Real output:
  - `commerce-app/src/main`: every file `R100` from `src/main`; then `M` `fraud/application/CustomerStatus.java` and `invoice/application/InvoicePayments.java`, javadoc lines only; `D` the 42 files of `payment`; `??` `fraud/inbound/CustomerStatusController.java`, `invoice/inbound/InvoicePaymentsController.java`, `invoice/inbound/InvoicePaymentsErrors.java`, `db/migration/V12__payment_leaves_for_payment_app.sql`
  - `payment-app/src/main/java/com/commerce/payment`: identical to `HEAD`, `application` 20 of 23 (all but `PayableInvoices`, `Payments`, `RecordPayment`), `domain` 7 of 7, `inbound` 5 of 5, `outbound` `FakeCardPay`, `FakeCardPayRefunds`, `JdbcRefunds`; `PayableInvoices`, `Payments`, `JdbcPayments`: javadoc lines only; `RecordPayment`: javadoc and `@Transactional` removed (BR-5); `InvoicePaymentsAdapter`, `FraudCustomerStatus`: rewritten over the generated client, same names, same ports; new: `BillingInternalClient`, `com.commerce.PaymentApplication`, `com.commerce.JsonConfiguration` (copy), `application.yaml`, `V1__create_payment.sql`, `V2__create_refund.sql`
- As expected: no caller of a port changed (`PayInvoiceUseCase`, `RefundInvoiceUseCase` identical); every dependency went through an interface

## After

- [x] C4 model updated and regenerated: `payment` a container of `commerce` (port 8081); `Payment H2` its database; `payment -> invoicing` and `payment -> fraud` over HTTP, `billing-internal` 1.0.0; the customer pays and the accountant refunds at `payment-app` (2026-10-20)
- [x] `docs/partition.yml`: `payment`'s `code` and layer globs under `payment-app/`, the others under `commerce-app/`; no bounded context added or removed (2026-10-20)
- [x] Budget measured again: `docs/budget/2026-10-20.md`, `payment` 7 416 → 7 786 (24 %), no gauge near its threshold; the promotion was not made to bring a gauge down
