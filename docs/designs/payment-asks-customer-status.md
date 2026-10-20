# `payment` asks `fraud` through `CustomerStatus` — design

> **Spec:** `docs/specs/billing/payment/spec.md` (BR-32, the paragraph on `FraudReview`, Language: Customer under fraud review, `Contracts with neighbours`: `fraud`; the open question of one check per pay or refund); `docs/specs/customer/fraud/spec.md` (BR-3, BR-4, `Contracts with neighbours`: `payment`)
>
> **Contract:** `docs/contracts/billing.openapi.json` 1.3.0, unchanged: `payInvoice` and `refundInvoice` keep their 403; `CustomerStatus`, in process, unchanged
>
> **Plan:** `docs/plans/payment-asks-customer-status.md`

## What moves

`payment` keeps its port `FraudReview` (architect, 2026-10-20). Its only adapter, `ConfiguredFraudReview`, over the list `commerce.fraud-review.customers`, goes; a new outbound adapter answers the port through `fraud`'s `CustomerStatus` (answer G of `docs/plans/fraud-review-and-customer-status.md`). `PayInvoiceUseCase` and `RefundInvoiceUseCase` do not change: they ask `FraudReview` once per pay or refund, after the content and the invoice, before the state (BR-22, the order of a refund), as today.

## `payment` — outbound

- `FraudCustomerStatus implements FraudReview`, a `@Component`: `underReview(customerId)` returns `customerStatus.blocked(customerId)`, the invoice's `customerId`, the same text (BR-32). It is the one class of `payment` that names `fraud`: the contract `CustomerStatus`, from `payment`'s outbound layer, as `InvoicePayments` is reached from it (architect, 2026-10-19).
- `CustomerStatus` cannot answer (an exception of `fraud`'s store): the exception leaves the use case unhandled and the framework answers 500, as a failed record does today (BR-5, BR-28); the check runs before CardPay and before any record, so nothing is charged and nothing recorded (BR-32, answer S). No handler is added; no log names the `customerId` (BR-31, BR-32).
- `FraudReview`'s comment names `CustomerStatus` instead of the list.

## Removed

- `src/main/java/com/commerce/payment/outbound/ConfiguredFraudReview.java`.
- The key `commerce.fraud-review` and its comment in `src/main/resources/application.yaml`: the list is empty in this project (answer H). A start with the key still set is not refused: the key is no longer read.
- `src/main/java/com/commerce/payment/domain/CustomerId.java`: only `ConfiguredFraudReview` uses it, to bound the list's entries by AR-1.
- `src/test/resources/fraud-review/no-list.yaml`, and the three tests of `PayInvoiceTest` on the list read at start-up (`a_missing_fraud_review_list_stops_the_start`, `a_blank_or_overlong_entry_of_the_fraud_review_list_stops_the_start_naming_no_entry`, `reads_an_entry_of_255_characters_counted_as_code_points`): the behaviour they test leaves the spec with the list.

## Tests

- `PaymentApiTest` and `RefundApiTest` drop the property `commerce.fraud-review.customers[0]`. `UNDER_REVIEW` stays the customer under review of their tests; it is placed under review through `fraud`'s `PlaceReviewUseCase` before each test, in the test class, as the risk team would.
- `PayInvoiceTest` and `RefundInvoiceTest` keep their tests of BR-32 (403, no body, nothing recorded, CardPay not called; the order of the answers; the same text; no output naming the customer) and gain:
  - a customer whose review is lifted through `LiftReviewUseCase` pays (200) and is refunded (201);
  - `CustomerStatus` failing (a spy that throws): 500, CardPay not called, nothing recorded, the invoice unchanged, for a pay and for a refund.
- `RefundInvoiceTest`'s spy on `FraudReview` stays.
- `ArchitectureTest`: `CONTRACTS` gains `new Contract("payment/payment", "outbound", "fraud/fraud", Set.of("com.commerce.fraud.application.CustomerStatus"))`, and its comment names `payment/spec.md` and `fraud/spec.md`.

## Questions the design raised, settled

1. The adapter is named `FraudCustomerStatus` and keeps the port `FraudReview` (architect, 2026-10-20).
2. A `CustomerStatus` failure is answered by the framework's 500, with no handler added (architect, 2026-10-20).
3. `CustomerId` of `payment` and the three start-up tests go with the list (TL, 2026-10-20).
4. The tests of `payment` place and lift reviews through `fraud`'s use cases, test code only (TL, 2026-10-20).

Design validated as written (architect, TL, 2026-10-20).

## Acceptance

Checked once, at delivery, through `./mvnw -q clean test`:

- `PayInvoiceTest` and `RefundInvoiceTest`: a customer placed under review through `fraud` gets 403, no body, CardPay not called, nothing recorded; after a lift, 200 and 201; `CustomerStatus` failing: 500, CardPay not called, nothing recorded; the order of the answers and the same text as before; no output names the customer.
- `ArchitectureTest`: `payment` reaches `fraud` only through `CustomerStatus`, from its outbound layer.
- No class of `payment` imports `ConfiguredFraudReview`, `CustomerId` or reads `commerce.fraud-review`.
- The other tests still pass.
