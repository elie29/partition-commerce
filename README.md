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
- `POST /invoices/{invoiceId}/cancel` cancels an invoice: `200` with the invoice, cancelled; `404` when the identifier names no invoice; `409` when it is already cancelled.

A correction is a cancel, then a new `POST /invoices` for the same order. Since this version, an order whose invoices are all cancelled can be invoiced again.

A refusal carries no body. A JSON value of the wrong type is refused, never coerced. The rules are in `docs/specs/billing/invoice/spec.md`.
