# Refund a paid invoice, and the fraud review on payments and refunds — design

> **Spec:** `docs/specs/billing/refund/spec.md` (BR-25 to BR-29, BR-31, AR-1 to AR-3); `docs/specs/billing/payment/spec.md` (BR-22, BR-30, AR-3, `InvoicePayments`, `PaymentRefunds`); `docs/specs/billing/area.md` (AR-3)
>
> **Contract:** `docs/contracts/billing.openapi.json` 1.3.0 (`refundInvoice`; `payInvoice` 403)
>
> **Plan:** `docs/plans/refund-and-fraud-review.md`

## The fraud review (AR-3), in each context

Each of `payment` and `refund` declares an application port `FraudReview` with one question, `underReview(customerId)`, and an outbound adapter `ConfiguredFraudReview` that reads `commerce.fraud-review.customers` once, at start-up, through Spring's `Binder` (architect, TL):

- the key absent: the start stops;
- an entry blank or longer than 255 characters (AR-1): the start stops, with a message that names the rule and the key, never the entry;
- an entry matches a `customerId` exactly, case and white space included;
- `application.yaml` declares the key with an empty list, so the service starts with nobody under review.

AR-1 is checked by the domain's own definition of a bounded text in each context (`payment`'s `Text`, `refund`'s `Text`), reached through a domain value `CustomerId`. No log, no error and no message names a customer (TL).

## `InvoicePayments` gains the customer

`InvoiceView` stays the reader's view, without the customer, since `getInvoice` uses it too. `InvoicePayments` gains one method, `Optional<String> customerOf(invoiceId)`, empty when the identifier names no invoice (architect: `InvoicePayments` gains the `customerId`). A customer never changes once the invoice is issued, so reading it apart from `find` cannot disagree with it. `payment`'s port `PayableInvoices` gains the same question; the pay's answer, the contract's `Invoice`, still shows no customer. Rejected: a new view type returned by `find` and `recordPayment` (every caller and test of the contract moves for one field).

## Pay, with the fraud review

`PayInvoiceUseCase.pay`: the content (400, BR-22), the invoice (404, BR-21), then `FraudReview` on the invoice's customer: under review → `PayForbidden`, 403, no body, CardPay not called, nothing recorded; then the state (409, BR-20), the charge, the record, as before.

## `PaymentRefunds`, in `payment`'s application layer

`PaymentRefunds.find(invoiceId)` → `Optional<RefundableInvoice>`, empty when the identifier names no invoice. `RefundableInvoice(invoiceId, customerId, payment)`, with `payment` a `PaidPayment(chargeReference, amountMinor, currency)` when the invoice's status is `PAID`, and empty otherwise. Its one implementation, `PaymentRefundsService`, reads the invoice and its customer through the port `PayableInvoices` (over `InvoicePayments`), and the payment through `Payments.findOf(invoiceId)` (`JdbcPayments`, the latest row of the invoice). `refund` reaches it from its outbound adapter only.

## Refund an invoice

`RefundInvoiceUseCase.refund(invoiceId, amountMinor, currency)`, in the order of the spec:

1. The content: an amount ≤ 0, or a body the contract refuses → 400 (BR-26).
2. The invoice: the identifier is not a canonical UUID, or `PaymentRefunds` finds nothing → 404 (BR-27).
3. The fraud review: the invoice's customer under review → 403, nothing recorded, CardPay not called (AR-3).
4. The payment: none (the invoice not `PAID`, a cancelled payment included) → 409 (BR-25, BR-29); a currency other than the amount paid's → 400; an amount greater than what remains → 409 (BR-25). An invoice with no payment has no currency to compare, so its answer is 409.
5. Record `REQUESTED` in one transaction (below), send to CardPay with the charge reference and the amount, then record `SENT` (BR-28) and answer 201 with the contract's `Refund`, status `SENT`.

Step 5's failure paths (the send not accepted; `SENT` not recorded after CardPay sent) are open questions of the spec: open question 2 and attack finding 1. They wait for the architect, with no interim behaviour.

### How the cap holds (BR-25)

A table `refund_cap` holds one row per refunded invoice: the amount paid (copied from `PaymentRefunds` when the first refund opens the row) and the sum of the refunds recorded so far. A refund is recorded by one conditional update, `refunded_minor = refunded_minor + :amount WHERE invoice_id = :id AND refunded_minor + :amount <= amount_paid_minor`, then the insert of the refund row, in the same transaction. Of two refunds at the same moment, the second update waits for the first to commit and re-checks the sum: the first to commit wins, the other updates no row → 409 (architect). The row is opened with an insert that ignores a duplicate key, in its own transaction, before the conditional update.

Rejected: one `INSERT … SELECT … WHERE (SELECT SUM(…)) + :amount <= :paid` (under READ COMMITTED, two such inserts both see the old sum and both pass); `SELECT … FOR UPDATE` on `payment`'s row (it would reach into another context's table).

### The domain of `refund` (rich, architect)

- `Refund(id, invoiceId, Money amount, chargeReference, RefundStatus status)`; `Refund.request(invoiceId, amount, AmountPaid)` refuses a currency other than the amount paid's; `refund.sent()` moves `REQUESTED` → `SENT` and refuses any other move.
- `RefundStatus { REQUESTED, SENT, SETTLED }`; nothing moves a refund to `SETTLED` in this version (open question).
- `Money` (AR-2, a three-letter code), `RefundAmount` (positive, BR-26), `AmountPaid`, `CustomerId` and `Text` (AR-1), `InvalidRefund`.
- `Refund.toString()` shows its identifiers and its status only (BR-31).

### Ports and adapters of `refund`

| Port (application) | Adapter (outbound) |
| --- | --- |
| `RefundableInvoices` | `PaymentRefundsAdapter`, over `payment`'s `PaymentRefunds` (the only class of `refund` that knows `payment`) |
| `Refunds` (open the cap, record a refund under the cap, record it sent) | `JdbcRefunds` |
| `CardPayRefunds` (`send(chargeReference, amount)`) | `FakeCardPayRefunds`: sends every refund (TL) |
| `FraudReview` | `ConfiguredFraudReview` |

The inbound adapter `RefundInvoiceController` implements the generated `RefundApi` (tag `Refund`); `RefundErrors` answers each refusal with a status and no body.

## As delivered

- **`RefundView`.** The use case answers an application `RefundView`, not the domain `Refund`: the layer map lets `inbound` reach `application` only.
- **No `RefundAmount` nor `AmountPaid`.** `Money.ofRefund` refuses an amount that is not positive (BR-26); the amount paid is a `Money`.
- **`refund_cap.refunded_currency`.** Added for `.claude/rules/backend-database.md`, which pairs every `_minor` column with its `_currency` column; the conditional update checks it.
- **The 500.** It keeps Spring's default error body (timestamp, status, path), as the pay's does; it holds no charge reference, no amount, no customer.
- **The start-up failures** are tested once, in `PayInvoiceTest`: both adapters read the same key with the same code, and the start fails at whichever adapter Spring creates first.

## Data

Migration `V6__create_refund.sql`, `refund`'s own tables, with no foreign key to another context's tables:

- `refund (refund_id UUID PK, invoice_id UUID NOT NULL, amount_minor BIGINT NOT NULL, amount_currency CHAR(3) NOT NULL, charge_reference VARCHAR(510) NOT NULL, status VARCHAR(16) NOT NULL, requested_at TIMESTAMP WITH TIME ZONE NOT NULL)`: AR-1 and AR-2.
- `refund_cap (invoice_id UUID PK, amount_paid_minor BIGINT NOT NULL, amount_paid_currency CHAR(3) NOT NULL, refunded_minor BIGINT NOT NULL)`.

## Patterns

- **P-2 (new): the status of a refund.** No pattern: an enum held by the `Refund` aggregate, one guarded method per move (`sent()`), as P-1 rules for the invoice. Rejected: State (one move today).
- **P-3 (new): the cap of the refunds.** A running total, guarded by a conditional update (optimistic, row-level), as BR-23's conditional change of status is. Rejected: summing at insert, and a lock on another context's table (above).
- **Ports and adapters** (the hexagon's own form) for `FraudReview`, `CardPayRefunds`, `Refunds`, `RefundableInvoices`; **GoF Adapter** for `PaymentRefundsAdapter` over `PaymentRefunds`, the same shape as `InvoicePaymentsAdapter`.
- No shared kernel: each context keeps its own `FraudReview`, `Money` and `Text` (architect: each reads the list).

## Acceptance

Checked once, at delivery, through `./mvnw test`:

- `FraudReviewTest` (each context): a listed customer is under review, an unlisted one is not, a differently cased one is not; the start stops when the key is absent, or when an entry is blank or has 256 characters, and the message names no entry.
- `PayInvoiceTest`: a pay of an invoice whose customer is under review: 403, no body, CardPay not called, read back `ISSUED`; 400 and 404 are answered before it; 403 is answered before a 409 on a `PAID` invoice; the customer appears in no output.
- `RefundInvoiceTest`: a refund of part of a paid invoice: 201, `SENT`, the amount, the invoice; CardPay asked once with the charge reference and the amount; the invoice read back `PAID`; refunds up to the amount paid accepted, one minor unit more refused 409; 409 for an `ISSUED` and a `CANCELLED` invoice; 404 for an unknown identifier and for one that is not a UUID; 400 for zero, a negative amount, a currency other than the amount paid's, no body, invalid JSON, no `amount`; the order 400 → 404 → 403 → 409; 403 for a customer under review, nothing recorded, CardPay not called; two refunds at the same moment that together exceed the amount paid: one 201, one 409; a payment copied by V4 is refunded; no output names the charge reference, the amount, or a customer under review.
- `ArchitectureTest`: the layer map of `refund`, and its one contract `PaymentRefunds`.
- The tests written before this feature still pass.
