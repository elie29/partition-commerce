# partition-commerce

Spring Boot service that sells products from price lists and bills them.

## Prerequisites

- Java 21

## Build

- `./mvnw clean install` builds and runs every test.
- The HTTP interfaces are generated from `docs/contracts/billing.openapi.json` at each build, under `target/generated-sources/openapi`.

## API

The billing API, as `docs/contracts/billing.openapi.json` declares it:

- `POST /invoices` issues the invoice of an order: `201` with the invoice, `409` when the order already has an invoice that is not cancelled, `400` when the request is refused.
- `GET /invoices/{invoiceId}` reads an invoice back: `200`, or `404` when the identifier names no invoice.
- `POST /invoices/{invoiceId}/cancel` cancels an invoice: `200` with the invoice, cancelled; `404` when the identifier names no invoice; `409` when it is paid or already cancelled.
- `POST /invoices/{invoiceId}/pay` pays an invoice by card, with a body `{"cardToken": "..."}`: `200` with the invoice, `PAID`; `402` when CardPay declines the card, the invoice staying `ISSUED`; `404` when the identifier names no invoice; `403` when the customer of the invoice is under fraud review; `409` when it is paid or cancelled; `400` when the request is refused, checked first.
- `POST /invoices/{invoiceId}/refunds` refunds all or part of a paid invoice to the card, with a body `{"amount": {"amountMinor": 500, "currency": "EUR"}}`: `201` with the refund, `SENT`, the invoice staying `PAID`; `400` when the request is refused (an amount that is not positive, checked first) or its currency is not the one paid; `404` when the identifier names no invoice; `403` when the customer of the invoice is under fraud review; `409` when the invoice is not paid, or the refund exceeds what remains of the amount paid. Two requests are two refunds: the refunds of an invoice never exceed its amount paid.

A correction is a cancel, then a new `POST /invoices` for the same order. An order whose invoices are all cancelled can be invoiced again; a paid invoice can be neither cancelled nor corrected in this version.

CardPay is not reachable in this version: the service charges cards and sends refunds through a fake that accepts every card and every refund. There is no URL and no credentials. No operation reads a refund back, and a refund is never shown `SETTLED` in this version.

A refusal carries no body. A JSON value of the wrong type is refused, never coerced; a request with no content type gets `415`. The rules are in `docs/specs/billing/invoice/spec.md` (issue, read, cancel), `docs/specs/billing/payment/spec.md` (pay), `docs/specs/billing/refund/spec.md` (refund) and, for those they share, `docs/specs/billing/area.md`.
The database migration `V3` records the payment. Once an invoice is paid, an earlier version of the service can no longer read it: there is no rollback, the project fixes forward. The migrations `V4` and `V5` move the payment to a table of its own, copying the payments already recorded, and drop its columns from the invoice: no version earlier than `V5` runs after them. The migration `V6` adds the tables of the refunds; it changes no existing table.

## Configuration

- `commerce.fraud-review.customers` in `src/main/resources/application.yaml`: the customers under fraud review, each an invoice's `customerId`, matched exactly. Such a customer can neither pay nor be refunded (`403`). The risk team edits the list; it is read at start-up, so an edit counts at the next restart. The key is required: a missing list, or an entry blank or longer than 255 characters, stops the start. It ships empty: `customers: []`.
