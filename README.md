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
- `POST /invoices/{invoiceId}/pay` pays an invoice by card, with a body `{"cardToken": "..."}`: `200` with the invoice, `PAID`; `402` when CardPay declines the card, the invoice staying `ISSUED`; `404` when the identifier names no invoice; `409` when it is paid or cancelled; `400` when the request is refused, checked first.

A correction is a cancel, then a new `POST /invoices` for the same order. An order whose invoices are all cancelled can be invoiced again; a paid invoice can be neither cancelled nor corrected in this version.

CardPay is not reachable in this version: the service charges cards through a fake that accepts every card. There is nothing to configure, no URL and no credentials.

A refusal carries no body. A JSON value of the wrong type is refused, never coerced; a request with no content type gets `415`. The rules are in `docs/specs/billing/invoice/spec.md`.

The database migration `V3` records the payment. Once an invoice is paid, an earlier version of the service can no longer read it: there is no rollback, the project fixes forward.
