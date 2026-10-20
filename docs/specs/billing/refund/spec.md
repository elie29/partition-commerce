# refund

> **Source:** `docs/sources/billing.md`
>
> **Source version:** 1.1
>
> **Provenance:** 2026-10-19 · 972aeb0000d5498ea82996110ca39366fadffb37
>
> **Scope:** REQ-6, DEC-1 of the whole source
>
> **Decomposition:** see `../bounded-contexts.md`

<!-- The header above is filled by partition-shape, or partition-reshape for a fission. -->
<!-- The empty ">" lines keep each header line on its own line when the Markdown is reformatted. -->

> **Status:** merged into `payment` on 2026-10-20. Frozen spec: its derived sections are no longer kept up to date. It remains the source of its hand-written sections until they are carried over into `../payment/spec.md`. See `../bounded-contexts.md#history`.

## Business objective

Give back to the customer's card all or part of what was paid for an invoice, at the accountant's request.

## Scope

**In scope:** refunding all or part of a paid invoice (REQ-6).

**Out of scope:** the invoice, its issue, its read and its cancel: `invoice`. Paying an invoice and cancelling a payment (REQ-4, REQ-5): `payment`. Sending the refund to the card and settling it: handed to CardPay (REQ-6).

## Language

One meaning per word, no synonyms. A word that changes meaning in a neighbour marks the boundary. The words shared by `billing` (Amount, Blank, Character, Currency, Minor units, Payment, Refused) are in `../area.md`.

| Term | Meaning in this bounded context |
| ---- | ------------------------------- |
| Amount paid | Of an invoice: the amount charged of its payment, held by `payment` (its BR-24), in that payment's currency (PO, BA, architect, 2026-10-19). One term: no other word for it here (BA). |
| Refund | All or part of the amount paid, given back to the card through CardPay at the accountant's request: `REQUESTED`, then `SENT`, then `SETTLED` (REQ-6; contract 1.3.0). |
| What remains | The amount paid less the refunds of the invoice not refused, whether `REQUESTED`, `SENT` or `SETTLED` (PO, 2026-10-19). |

## Source parts owned

What this bounded context owns in its source. The source says what each part holds; this spec does not repeat it.

| Unit | Where | What it is for |
| --- | --- | --- |
| REQ-6 | [REQ-6](../../../sources/billing.md#req-6--refund-a-paid-invoice) | The accountant refunds all or part of a paid invoice |
| DEC-1 | [DEC-1](../../../sources/billing.md#dec-1--cardpay) | The choice of the card payment provider, shared with `payment` (architect, 2026-10-19) |

## Business rules

| # | Rule | Trigger | Expected effect | Source |
| --- | --- | --- | --- | --- |
| BR-25 | The refunds of an invoice never exceed its amount paid. | A refund whose content, invoice and customer are accepted (BR-26, BR-27, AR-3) | An amount in a currency other than the amount paid's: refused, 400, no body. An invoice not `PAID`: refused, 409, no body, whatever the currency: it has no payment to compare a currency against (architect). An amount greater than what remains: refused, 409, no body; of two refunds of one invoice at the same moment, the sum is checked against the amount paid in one conditional record: the first to commit wins, the other gets 409 (architect). Otherwise: recorded `REQUESTED` and counted against the amount paid from then on, then sent (BR-28); 201 with the refund. The invoice stays `PAID` | from source: REQ-6; confirmed: owner, 2026-10-19; trigger and effect: PO, architect, 2026-10-19 |
| BR-26 | A refund amount is a positive whole number of minor units, with its currency. | A refund of zero or of a negative amount; a refund the contract refuses: no body, a body that is not JSON, no `amount`, an `amount` without `amountMinor` or `currency` or of the wrong type | Refused: 400, no body, as for issuing | PO, 2026-10-19; contract refusals: architect, 2026-10-19 |
| BR-27 | A refund names its invoice by the invoice's identifier. | A refund | An identifier that names no invoice, one that is not a UUID included: 404, no body | architect, 2026-10-19 |
| BR-28 | A refund is sent to the card through CardPay once recorded. | A refund recorded `REQUESTED` (BR-25) | CardPay is asked to send it, with the charge reference and the amount; accepted: the refund becomes `SENT`. Sent, then the record of `SENT` failing, or the process stopping before the send: 500, as for a pay; the refund stays `REQUESTED` and counts against the amount paid (architect) | TL, architect, 2026-10-19 |
| BR-29 | A cancelled payment is not refunded. | A refund of an invoice whose payment was cancelled (REQ-5, not built) | Refused as an invoice not `PAID` (BR-25): 409, no body | PO, 2026-10-19 |
| BR-31 | A refund writes to a log or an error its identifiers and its status only. | Any log or error of `refund` | Never the charge reference, never the amount; never a customer under fraud review | TL, 2026-10-19 |

The order of the answers: the content (400, BR-26), the invoice (404, BR-27), the fraud review (403, AR-3), then the payment: its currency (400), its state and what remains (409) (BR-25) (architect, 2026-10-19).

The rule on fraud review REQ-6 states is AR-3 of `../area.md`, shared with `payment`. AR-3 says what it refuses and how the list is kept; no API edits the list in this version (PO, 2026-10-19). `refund` reads the list through a port of its own, `FraudReview` (architect, 2026-10-19).

## Decisions

The decisions the source states, copied with their reason. A decision taken while building lives in the plan that took it; what it established is written in the sections above.

| Decision | Reason | Source | Date |
| -------- | ------ | ------ | ---- |
| Card payments go through CardPay | it is the provider the company already has a contract with | DEC-1 | 1.0 |

## Persistence

No schema documents are declared for this project.

## External dependencies

- **CardPay** — a refund is sent to the card through CardPay, then settled by CardPay, days later, on its own schedule (REQ-6); DEC-1 covers refunds (PO, 2026-10-19). Reached through a port with one operation: send a refund, with the charge reference and the amount. Not reachable in this version: its only adapter is a fake that sends every refund (TL, 2026-10-19).

## Contracts with neighbours

| Neighbour | Direction | Contract | What crosses |
| --------- | --------- | -------- | ------------ |
| Callers: the accountant; anyone holding an identifier, accepted in this version: there is no login (PO, 2026-10-19) | inbound | `docs/contracts/billing.openapi.json` 1.3.0: `refundInvoice` (tag `Refund`) | in: the identifier of the invoice and the amount; out: the contract's `Refund` |
| `payment` | outbound, in process | `PaymentRefunds`, a contract of `payment` | for any invoice: whether it exists, the customer of the invoice, and its payment when it is paid: its charge reference, the amount charged (architect, 2026-10-19) |

What the contract does not declare (the 400, the bound of AR-1, no body on a refusal) the code enforces, as in `payment` (architect, 2026-10-19).

## Applicable area rules and invariants

| Entry | What it constrains here |
| ----- | ----------------------- |
| AR-1 | The charge reference it stores (architect, 2026-10-19) |
| INV-DATA-001 of `../../invariants.md` | The amount of a refund, stored (architect, 2026-10-19) |
| AR-3 | A refund of an invoice whose customer is under fraud review |

## Open questions

- How does `refund` learn a refund is `SETTLED`, and what happens to one CardPay does not settle? Through CardPay's notifications: nothing moves a refund to `SETTLED` in this version (TL, 2026-10-19). Owner: TL, architect.
- A send CardPay does not accept (BR-28): what follows? Open until CardPay is reachable; its fake always sends (architect, 2026-10-19). Owner: architect.
- The amount of a refund reaches the log when `org.springframework.web` runs at DEBUG or TRACE: the request model generated from the contract prints it, and Spring logs the body it reads (BR-31). Accepted in this version, as for the card token in `payment`: the demo never runs that logger at DEBUG or TRACE; open until a real CardPay adapter comes (TL, 2026-10-19). Owner: TL.
- A refund left `REQUESTED` (BR-28: sent and its `SENT` not recorded, or never sent) counts against the amount paid for good in this version: open until CardPay is reachable (architect, 2026-10-19). Owner: architect.

### Attack — 2026-10-19 — feature "refund a paid invoice, and the fraud review on payments and refunds"

Settled before the attack and written above or in the plan: two refunds at once (BR-25), a request sent again (two refunds, the cap bounds them), anyone holding the identifier refunds, the 403 telling that a customer is under review, the logs (BR-31), the list read at start-up, no read of a refund (PO, TL, architect, 2026-10-19).

Both findings were settled on 2026-10-19 (architect): the record of `SENT` failing (BR-28 and the open question on a refund left `REQUESTED`), and the list's rules written twice (now in AR-3 of `../area.md`).

Skipped: the siblings (AR-3 is enforced on both inbound surfaces that pay or refund, `payInvoice` and `refundInvoice`; a cancel is not reached by AR-3, PO), the session (the list changes only at a restart; a status changed under the caller is answered by BR-25's 409), the budget (`refund` reads `payment` only through `PaymentRefunds`), the words (the customer of AR-3 is defined in AR-3's trigger; Amount paid has one term).
