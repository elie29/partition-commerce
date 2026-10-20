# partition-commerce

Spring Boot services that sell products from price lists and bill them: two applications, `commerce-app` and `payment-app`.

## Prerequisites

- Java 21

## Build

- `./mvnw clean install`, from the root, builds both modules, `commerce-app` and `payment-app`, and runs every test.
- The HTTP interfaces are generated from the contracts of `docs/contracts/` at each build, under `<module>/target/generated-sources/openapi`: `commerce-app` from `billing.openapi.json` (tag `Invoice`), `catalog.openapi.json` and `billing-internal.openapi.json` (the side it serves); `payment-app` from `billing.openapi.json` (tags `Payment` and `Refund`) and `billing-internal.openapi.json` (its client of `commerce-app`).

## Run

Start `commerce-app` first, then `payment-app`:

- `java -jar commerce-app/target/commerce-app-0.0.1-SNAPSHOT.jar` serves the invoices, the catalog and the internal API on port `8080`, with its database in `./.local/h2/commerce`.
- `java -jar payment-app/target/payment-app-0.0.1-SNAPSHOT.jar` serves the pay and the refunds on port `8081`, with its own database in `./.local/h2/payment`. It finds `commerce-app` through `commerce.billing-internal.base-url` (`http://localhost:8080` by default); a `payment-app` started against no `commerce-app`, or one without the internal API, answers every pay and refund `404` or `500` and charges nothing.

## API

The billing API, as `docs/contracts/billing.openapi.json` declares it. The first three operations are served by `commerce-app` (`8080`), the pay and the refunds by `payment-app` (`8081`); `commerce-app` no longer serves the pay or the refunds:

- `POST /invoices` issues the invoice of an order: `201` with the invoice, `409` when the order already has an invoice that is not cancelled, `400` when the request is refused.
- `GET /invoices/{invoiceId}` reads an invoice back: `200`, or `404` when the identifier names no invoice.
- `POST /invoices/{invoiceId}/cancel` cancels an invoice: `200` with the invoice, cancelled; `404` when the identifier names no invoice; `409` when it is paid or already cancelled.
- `POST /invoices/{invoiceId}/pay` pays an invoice by card, with a body `{"cardToken": "..."}`: `200` with the invoice, `PAID`; `402` when CardPay declines the card, the invoice staying `ISSUED`; `404` when the identifier names no invoice; `403` when the customer of the invoice is under fraud review; `409` when it is paid or cancelled; `400` when the request is refused, checked first.
- `POST /invoices/{invoiceId}/refunds` refunds all or part of a paid invoice to the card, with a body `{"amount": {"amountMinor": 500, "currency": "EUR"}}`: `201` with the refund, `SENT`, the invoice staying `PAID`; `400` when the request is refused (an amount that is not positive, checked first) or its currency is not the one paid; `404` when the identifier names no invoice; `403` when the customer of the invoice is under fraud review; `409` when the invoice is not paid, or the refund exceeds what remains of the amount paid. Two requests are two refunds: the refunds of an invoice never exceed its amount paid.

A correction is a cancel, then a new `POST /invoices` for the same order. An order whose invoices are all cancelled can be invoiced again; a paid invoice can be neither cancelled nor corrected in this version.

CardPay is not reachable in this version: the service charges cards and sends refunds through a fake that accepts every card and every refund. There is no URL and no credentials. No operation reads a refund back, and a refund is never shown `SETTLED` in this version.

A customer is under fraud review while the risk team's review of it is open: the pay and the refund ask `fraud` (`docs/specs/customer/fraud/spec.md`), and answer `500`, charging and recording nothing, when it cannot answer.

`payment-app` reaches `commerce-app` over HTTP, as `docs/contracts/billing-internal.openapi.json` declares it: `GET /internal/invoices/{invoiceId}`, `GET /internal/invoices/{invoiceId}/customer`, `POST /internal/invoices/{invoiceId}/payment` and `GET /internal/customers/{customerId}/status`; `POST /internal/invoices/{invoiceId}/payment-cancellation` answers `501` until the cancel of a payment is built. `commerce-app` unreachable or slower than 2 seconds is a failure: `500`, nothing recorded, CardPay not called when it comes before the charge. `commerce-app` records the invoice paid first, then `payment-app` stores the payment; a payment whose store fails after the invoice is recorded paid answers `500` and stays open until the cancel of a payment is built. The internal API has no restriction in this version, a demo not intended for production: anyone reaching port `8080` can record an invoice paid or ask whether a customer is under review. No operation places or lifts a review in this version, so no customer is under review in a running service. The list `commerce.fraud-review.customers` of `application.yaml` is no longer read: a configuration that still sets it starts, and the key changes nothing.

A refusal of the billing API carries no body. A JSON value of the wrong type is refused, never coerced; a request with no content type gets `415`. The rules are in `docs/specs/billing/invoice/spec.md` (issue, read, cancel), `docs/specs/billing/payment/spec.md` (pay, refund) and, for those they share, `docs/specs/billing/area.md`.

The catalog API, as `docs/contracts/catalog.openapi.json` declares it for the tags `Product` and `PriceList`, under the base path `/commerce/v1/catalog`:

- `POST /commerce/v1/catalog/products` creates a product, with a body `{"reference": "BOK-000123", "label": "..."}`: `201` with the product and its `Location`; `409` when another product holds the reference; `400` when the request is refused. `description`, `ean` and `attributes` are accepted and not kept.
- `GET /commerce/v1/catalog/products` lists the products by reference, ascending, one page at a time (`page`, `size` up to 100), filtered by `references` when given: `200`. `localized` and `Accept-Language` change nothing in this version.
- `GET /commerce/v1/catalog/products/{productId}` reads a product: `200`, or `404` when the identifier names no product.
- `POST /commerce/v1/catalog/price-lists` creates a price list, with a body `{"name": "...", "currency": "EUR", "channel": "SHOP"}`: `201` with the list; `400` when the request is refused (a blank name, a currency that is not a known ISO 4217 code, a channel other than `SHOP`, `MARKETPLACE`, `B2B`). The channel governs no price; a `validity` is accepted and not kept.
- `GET /commerce/v1/catalog/price-lists/{priceListId}` reads a price list: `200`, or `404` when the identifier names no list.
- `PUT /commerce/v1/catalog/price-lists/{priceListId}/prices/{productId}` sets the price of a product in a list, with a body `{"price": {"amountMinor": 1999, "currency": "EUR"}, "effectiveFrom": "2026-10-20T00:00:00Z"}`: `200` with the price. A price list holds one price per product: a new price replaces the old one, whatever its `effectiveFrom`, and keeps no history. `effectiveFrom` absent is now; it needs its offset. `400` when the body is refused (a negative amount, a currency that is not a known code, checked first) or its currency is not the list's; `404` when the list does not exist or an identifier is not a UUID. The product is not read: a price is set for the identifier given.
- `GET /commerce/v1/catalog/price-lists/{priceListId}/product-prices/{productId}?at=2026-10-19` reads the price of a product in a price list on a day: `200` when the list holds a price whose `effectiveFrom` is on or before the end of that day, in UTC (`at` absent is today); `404` when there is no such price list, no price for the product in it, no price that applies, or an identifier is not a UUID; `400` when `at` is not a date, checked first. The product is not read. Since version 1.1.0 of the contract, this replaces `GET /commerce/v1/catalog/products/{productId}/price?priceListId=...`, which is no longer served.

Since version 2.0.0 of the contract, the discounts are gone: `POST /commerce/v1/catalog/discounts` and `GET /commerce/v1/catalog/discounts/{discountId}` are no longer served, and the discounts stored are dropped (`V10`).

Every catalog request carries an `X-Request-Id`, a UUID: missing or malformed, the request is refused with `400`, checked before anything else. There is no authentication in this version: `401` and `403` are never answered. A catalog error answer carries the common error envelope `{timestamp, path, errorId, detail, errors}`, each item of `errors` being `{field, code, message}`, with the contract's codes (`VALIDATION_FAILED`, `CONFLICT`, `PRODUCT_NOT_FOUND`, `PRICE_LIST_NOT_FOUND`, `PRICE_NOT_FOUND`); no value sent is copied into it, and the `errorId` is logged with the `X-Request-Id`, the path and the status. `405`, `406` and `415` are answered by the framework. The rules are in `docs/specs/catalog/product/spec.md`.

The database migration `V3` records the payment. Once an invoice is paid, an earlier version of the service can no longer read it: there is no rollback, the project fixes forward. The migrations `V4` and `V5` move the payment to a table of its own, copying the payments already recorded, and drop its columns from the invoice: no version earlier than `V5` runs after them. The migration `V6` adds the tables of the refunds; it changes no existing table. The migration `V7` adds the tables of the catalog (products, price lists, prices); it changes no existing table. The migration `V8` adds the table of the discounts; it changes no existing table. The migration `V9` adds the status of a product to the table of the products, active or retired; every product already stored is active. An earlier version of the service still runs against it. The migration `V10` drops the table of the discounts, with every discount stored; it changes no other table. The migration `V11` adds the table of the fraud reviews; it changes no existing table, and an earlier version of the service still runs against it. The migration `V12` of `commerce-app` drops the tables of the payments and the refunds, with every row stored: they live in `payment-app`'s database, created by its own migrations `V1` (payments) and `V2` (refunds), and no row is copied. There is no rollback: no version earlier than `V12` runs after it.