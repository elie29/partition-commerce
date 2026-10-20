# partition-commerce

Spring Boot service that sells products from price lists and bills them.

## Prerequisites

- Java 21

## Build

- `./mvnw clean install` builds and runs every test.
- The HTTP interfaces are generated from `docs/contracts/billing.openapi.json` and `docs/contracts/catalog.openapi.json` at each build, under `target/generated-sources/openapi`.

## API

The billing API, as `docs/contracts/billing.openapi.json` declares it:

- `POST /invoices` issues the invoice of an order: `201` with the invoice, `409` when the order already has an invoice that is not cancelled, `400` when the request is refused.
- `GET /invoices/{invoiceId}` reads an invoice back: `200`, or `404` when the identifier names no invoice.
- `POST /invoices/{invoiceId}/cancel` cancels an invoice: `200` with the invoice, cancelled; `404` when the identifier names no invoice; `409` when it is paid or already cancelled.
- `POST /invoices/{invoiceId}/pay` pays an invoice by card, with a body `{"cardToken": "..."}`: `200` with the invoice, `PAID`; `402` when CardPay declines the card, the invoice staying `ISSUED`; `404` when the identifier names no invoice; `403` when the customer of the invoice is under fraud review; `409` when it is paid or cancelled; `400` when the request is refused, checked first.
- `POST /invoices/{invoiceId}/refunds` refunds all or part of a paid invoice to the card, with a body `{"amount": {"amountMinor": 500, "currency": "EUR"}}`: `201` with the refund, `SENT`, the invoice staying `PAID`; `400` when the request is refused (an amount that is not positive, checked first) or its currency is not the one paid; `404` when the identifier names no invoice; `403` when the customer of the invoice is under fraud review; `409` when the invoice is not paid, or the refund exceeds what remains of the amount paid. Two requests are two refunds: the refunds of an invoice never exceed its amount paid.

A correction is a cancel, then a new `POST /invoices` for the same order. An order whose invoices are all cancelled can be invoiced again; a paid invoice can be neither cancelled nor corrected in this version.

CardPay is not reachable in this version: the service charges cards and sends refunds through a fake that accepts every card and every refund. There is no URL and no credentials. No operation reads a refund back, and a refund is never shown `SETTLED` in this version.

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

Every catalog request carries an `X-Request-Id`, a UUID: missing or malformed, the request is refused with `400`, checked before anything else. There is no authentication in this version: `401` and `403` are never answered. A catalog error answer carries the common error envelope `{timestamp, path, errorId, detail, errors}`, each item of `errors` being `{field, code, message}`, with the contract's codes (`VALIDATION_FAILED`, `CONFLICT`, `PRODUCT_NOT_FOUND`, `PRICE_LIST_NOT_FOUND`, `PRICE_NOT_FOUND`); no value sent is copied into it, and the `errorId` is logged with the `X-Request-Id`, the path and the status. `405`, `406` and `415` are answered by the framework. The rules are in `docs/specs/catalog/product/spec.md` and `docs/specs/catalog/area.md`.

The database migration `V3` records the payment. Once an invoice is paid, an earlier version of the service can no longer read it: there is no rollback, the project fixes forward. The migrations `V4` and `V5` move the payment to a table of its own, copying the payments already recorded, and drop its columns from the invoice: no version earlier than `V5` runs after them. The migration `V6` adds the tables of the refunds; it changes no existing table. The migration `V7` adds the tables of the catalog (products, price lists, prices); it changes no existing table. The migration `V8` adds the table of the discounts; it changes no existing table. The migration `V9` adds the status of a product to the table of the products, active or retired; every product already stored is active. An earlier version of the service still runs against it. The migration `V10` drops the table of the discounts, with every discount stored; it changes no other table.

## Configuration

- `commerce.fraud-review.customers` in `src/main/resources/application.yaml`: the customers under fraud review, each an invoice's `customerId`, matched exactly. Such a customer can neither pay nor be refunded (`403`). The risk team edits the list; it is read at start-up, so an edit counts at the next restart. The key is required: a missing list, or an entry blank or longer than 255 characters, stops the start. It ships empty: `customers: []`.
