# refund

> **Source:** `docs/sources/billing.md`
>
> **Source version:** 1.1
>
> **Provenance:** 2026-10-19 · 972aeb0000d5498ea82996110ca39366fadffb37
>
> **Scope:** REQ-6 of the whole source
>
> **Decomposition:** see `../bounded-contexts.md`

<!-- The header above is filled by partition-shape, or partition-reshape for a fission. -->
<!-- The empty ">" lines keep each header line on its own line when the Markdown is reformatted. -->

## Business objective

Give back to the customer's card all or part of what was paid for an invoice, at the accountant's request.

## Scope

**In scope:** refunding all or part of a paid invoice (REQ-6).

**Out of scope:** the invoice, its issue, its read and its cancel: `invoice`. Paying an invoice and cancelling a payment (REQ-4, REQ-5): `payment`. Sending the refund to the card and settling it: handed to CardPay (REQ-6).

## Language

One meaning per word, no synonyms. A word that changes meaning in a neighbour marks the boundary. The words shared by `billing` (Amount, Blank, Character, Currency, Minor units, Payment, Refused) are in `../area.md`.

| Term | Meaning in this bounded context |
| ---- | ------------------------------- |

## Source parts owned

What this bounded context owns in its source. The source says what each part holds; this spec does not repeat it.

| Unit | Where | What it is for |
| --- | --- | --- |
| REQ-6 | [REQ-6](../../../sources/billing.md#req-6--refund-a-paid-invoice) | The accountant refunds all or part of a paid invoice |

## Business rules

| # | Rule | Trigger | Expected effect | Source |
| --- | --- | --- | --- | --- |
| BR-25 | The refunds of an invoice never exceed its amount paid. | — | — | from source: REQ-6; confirmed: owner, 2026-10-19 |

The rule on fraud review REQ-6 states is AR-3 of `../area.md`, shared with `payment`.

## Decisions

The decisions the source states, copied with their reason. A decision taken while building lives in the plan that took it; what it established is written in the sections above.

| Decision | Reason | Source | Date |
| -------- | ------ | ------ | ---- |

## Persistence

No schema documents are declared for this project.

## External dependencies

- **CardPay** — a refund is sent to the card through CardPay, then settled by CardPay, days later, on its own schedule (REQ-6).

## Contracts with neighbours

| Neighbour | Direction | Contract | What crosses |
| --------- | --------- | -------- | ------------ |

## Applicable area rules and invariants

| Entry | What it constrains here |
| ----- | ----------------------- |
| AR-1 | The charge reference it stores (architect, 2026-10-19) |
| AR-2 | The amount of a refund, stored (architect, 2026-10-19) |
| AR-3 | A refund of an invoice whose customer is under fraud review |

## Open questions

- What is the "amount paid" of an invoice (BR-25)? Neither REQ-6 nor `../area.md` defines it: is it `payment`'s amount charged (its BR-24)? (1.1, 2026-10-19). Owner: PO, BA.
- What does a refund refused by BR-25 or by AR-3 answer? (1.1, 2026-10-19). Owner: PO, architect.
- What happens to a refund CardPay does not settle, and what does `refund` record between sent and settled? (1.1, 2026-10-19). Owner: PO, architect.
- REQ-6 has no operation in `docs/contracts/billing.openapi.json` 1.2.0 (2026-10-19). Owner: architect.
- How does `refund` read the amount paid from `payment`, and the invoice's `customerId` for AR-3? No contract with either neighbour gives it yet (2026-10-19). Owner: architect.
- Can a payment cancelled within 24 hours (REQ-5) be refunded, or a refunded one cancelled? (1.1, 2026-10-19). Owner: PO.
