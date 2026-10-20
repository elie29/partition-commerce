# payment

> **Source:** `docs/sources/billing.md`; the boundary: `docs/sources/workshop-invoice-fission.md`
>
> **Source version:** 1.1
>
> **Provenance:** 2026-10-19 · 972aeb0000d5498ea82996110ca39366fadffb37
>
> **Scope:** REQ-4, REQ-5, REQ-6, DEC-1 of the whole source
>
> **Decomposition:** see `../bounded-contexts.md`

<!-- The header above is filled by partition-shape, or partition-reshape for a fission. -->
<!-- The empty ">" lines keep each header line on its own line when the Markdown is reformatted. -->

## Business objective

Collect the payment of an invoice by card, cancel a payment within 24 hours of it, and give back to the customer's card all or part of what was paid, at the accountant's request.

## Scope

**In scope:** paying an invoice by card, cancelling a paid invoice's payment, and refunding all or part of a paid invoice (REQ-4, REQ-5, REQ-6).

**Out of scope:** the invoice, its issue, its read and its cancel by the accountant: `invoice`, reached through its contract `InvoicePayments`. Charging a card, reversing a charge, sending a refund to the card and settling it: handed to CardPay (REQ-4, REQ-5, REQ-6).

## Language

One meaning per word, no synonyms. A word that changes meaning in a neighbour marks the boundary. The words shared by `billing` (Amount, Blank, Character, Currency, Minor units, Payment, Refused) are in `../area.md`.

| Term | Meaning in this bounded context |
| ---- | ------------------------------- |
| Amount paid | Of an invoice: the amount charged by its payment (BR-24), in that payment's currency (PO, BA, architect, 2026-10-19). One term: no other word for it here (BA, 2026-10-20). |
| Cancelled | Of a payment: reversed within 24 hours of the payment (BA, 2026-10-19). Not `invoice`'s cancelled, an invoice voided before any payment. |
| Card token | What CardPay makes on the customer's side to stand for a card; never a card number (TL, 2026-10-19). |
| Charge reference | What CardPay answers when it accepts a charge: text bounded by AR-1 (TL, 2026-10-19). |
| Customer under fraud review | A customer, the invoice's `customerId`, that `fraud` answers blocked through `CustomerStatus` (BR-32) (PO, 2026-10-19; `CustomerStatus`: PO, TL, architect, 2026-10-20). |
| Declined | CardPay's answer to a charge it does not accept: CardPay declines a card. Not Refused (`../area.md`) (PO, 2026-10-19). |
| Refund | All or part of the amount paid, given back to the card through CardPay at the accountant's request: `REQUESTED`, then `SENT`, then `SETTLED` (REQ-6; contract 1.3.0). A refund gives back; it does not reverse: "reversed" is a payment cancelled within 24 hours (BA, 2026-10-20). |
| Time of the payment | The moment the record of `payment` commits (architect, 2026-10-19). |
| What remains | The amount paid less the refunds of the invoice not refused, whether `REQUESTED`, `SENT` or `SETTLED` (PO, 2026-10-19). |

## Source parts owned

What this bounded context owns in its source. The source says what each part holds; this spec does not repeat it.

| Unit | Where | What it is for |
| --- | --- | --- |
| REQ-4 | [REQ-4](../../../sources/billing.md#req-4--pay-an-invoice) | The customer pays an invoice by card |
| REQ-5 | [REQ-5](../../../sources/billing.md#req-5--cancel-a-paid-invoice) | The customer cancels a paid invoice |
| REQ-6 | [REQ-6](../../../sources/billing.md#req-6--refund-a-paid-invoice) | The accountant refunds all or part of a paid invoice |
| DEC-1 | [DEC-1](../../../sources/billing.md#dec-1--cardpay) | The choice of the card payment provider |
| Glossary | [Glossary](../../../sources/billing.md#glossary) | "Cancelled" of a paid invoice |

## Business rules

| # | Rule | Trigger | Expected effect | Source |
| --- | --- | --- | --- | --- |
| BR-5 | An invoice is paid once its amount is paid in full. | A pay of an invoice whose status is `ISSUED` | CardPay is asked, in one call, to charge the card token for the whole amount, once; no partial payment; an amount of 0 is charged like any other, CardPay decides (PO). Accepted: the payment is recorded (BR-24) and the invoice recorded paid by `invoice` (its BR-23), in one transaction, 200 with the invoice: of two pays, or a pay and a cancel, of one invoice at the same moment, the first to commit wins and the other gets 409, no body; its charge is not kept (architect). Accepted, then the record failing otherwise than by losing a race: 500, the invoice stays `ISSUED` (architect). A charge reference that is not bounded by AR-1: a defect, 500 (architect). Declined: no payment, 402, no body; the invoice stays `ISSUED` | from source: REQ-4; trigger and effect: PO, architect, 2026-10-19 |
| BR-6 | A paid invoice is cancelled only within 24 hours of being paid; the invoice is then unpaid again. | — | — | from source: REQ-5 |
| BR-20 | Only an invoice whose status is `ISSUED` is paid. | A pay of an invoice whose status is `PAID` or `CANCELLED` | Refused: 409, no body; nothing changes; CardPay is not called: it is called only for an invoice whose status is `ISSUED` | PO, 2026-10-19; CardPay not called: architect, 2026-10-19 |
| BR-21 | An invoice is paid by anyone holding its identifier. | A pay | An identifier that names no invoice, one that is not a UUID included: 404, no body | PO, 2026-10-19 |
| BR-22 | A card token is bounded by AR-1. | A pay with a card token AR-1 refuses; a pay the contract refuses: no body (a JSON request with an empty body), a body that is not JSON, no `cardToken`, a `cardToken` `null` or not a string | Refused: 400, no body. The content of a pay is checked first (400), then the invoice (404, BR-21), the fraud review (403, AR-3), then its state (409, BR-20) (fraud review: architect, 2026-10-19). A request with no content type keeps the framework's 415 | TL, 2026-10-19; contract refusals, order and 415: architect, 2026-10-19 |
| BR-24 | A payment records its time, its charge reference and the amount paid, never the card token. | A pay accepted (BR-5) | The time of the payment, the charge reference and the amount paid are recorded by `payment`; the card token appears in no log, no error and no trace (TL, 2026-10-19) | PO, 2026-10-19 (the pay part of BR-3 of `invoice`); the amount paid: architect, 2026-10-19; one term: BA, 2026-10-20 |
| BR-30 | A payment with a refund is not cancelled. | A cancel (REQ-5, not built) of the payment of an invoice that has a refund not refused (BR-25) | — (REQ-5 has no operation yet) | PO, 2026-10-19 |
| BR-25 | The refunds of an invoice never exceed its amount paid. | A refund whose content, invoice and customer are accepted (BR-26, BR-27, BR-32) | An amount in a currency other than the amount paid's: refused, 400, no body. An invoice not `PAID`: refused, 409, no body, whatever the currency: it has no payment to compare a currency against (architect). An amount greater than what remains: refused, 409, no body; of two refunds of one invoice at the same moment, the sum is checked against the amount paid in one conditional record: the first to commit wins, the other gets 409 (architect). Otherwise: recorded `REQUESTED` and counted against the amount paid from then on, then sent (BR-28); 201 with the refund. The invoice stays `PAID` | from source: REQ-6; confirmed: owner, 2026-10-19; trigger and effect: PO, architect, 2026-10-19 |
| BR-26 | A refund amount is a positive whole number of minor units, with its currency. | A refund of zero or of a negative amount; a refund the contract refuses: no body, a body that is not JSON, no `amount`, an `amount` without `amountMinor` or `currency` or of the wrong type | Refused: 400, no body, as for issuing | PO, 2026-10-19; contract refusals: architect, 2026-10-19 |
| BR-27 | A refund names its invoice by the invoice's identifier. | A refund | An identifier that names no invoice, one that is not a UUID included: 404, no body | architect, 2026-10-19 |
| BR-28 | A refund is sent to the card through CardPay once recorded. | A refund recorded `REQUESTED` (BR-25) | CardPay is asked to send it, with the charge reference and the amount; accepted: the refund becomes `SENT`. Sent, then the record of `SENT` failing, or the process stopping before the send: 500, as for a pay; the refund stays `REQUESTED` and counts against the amount paid (architect) | TL, architect, 2026-10-19 |
| BR-29 | A cancelled payment is not refunded. | A refund of an invoice whose payment was cancelled (REQ-5, not built) | Refused as an invoice not `PAID` (BR-25): 409, no body | PO, 2026-10-19 |
| BR-31 | A refund writes to a log or an error its identifiers and its status only. | Any log or error of a refund | Never the charge reference, never the amount; never a customer under fraud review | TL, 2026-10-19 |
| BR-32 | A customer under fraud review can neither pay nor be refunded. | A pay (BR-5) or a refund (BR-25) of an invoice whose customer, the invoice's `customerId`, is under fraud review: `fraud` answers it blocked through `CustomerStatus`, asked with the invoice's `customerId`, the same text (BR-3 and BR-4 of `fraud`) | Refused: 403, no body; nothing is recorded and CardPay is not called; no log names a customer under review. `CustomerStatus` cannot answer: refused, 500, nothing charged, CardPay not called (architect, 2026-10-20) | from source: REQ-4, REQ-6 (1.1); trigger: PO, 2026-10-19; `CustomerStatus`: PO, TL, architect, 2026-10-20; confirmed: owner, 2026-10-19; was AR-3 of `../area.md`, carried by the BA on 2026-10-20 |

The order of the answers of a refund: the content (400, BR-26), the invoice (404, BR-27), the fraud review (403, BR-32), then the payment: its currency (400), its state and what remains (409) (BR-25) (architect, 2026-10-19).

`payment` asks it through one port, `FraudReview`, for pays and refunds (architect, 2026-10-20), answered by `fraud`'s `CustomerStatus` (PO, TL, architect, 2026-10-20). A cancel (REQ-5) by a customer under fraud review is not refused by BR-32, which names paying and being refunded only (PO, 2026-10-19).

## Decisions

The decisions the source states, copied with their reason. A decision taken while building lives in the plan that took it; what it established is written in the sections above.

| Decision | Reason | Source | Date |
| -------- | ------ | ------ | ---- |
| Card payments go through CardPay | it is the provider the company already has a contract with | DEC-1 | 1.0 |

## Persistence

No schema documents are declared for this project.

## External dependencies

- **CardPay** — charges the customer's card when an invoice is paid (REQ-4), reverses the charge when a payment is cancelled (REQ-5). Sends a refund to the card, then settles it, days later, on its own schedule (REQ-6); DEC-1 covers refunds (PO, 2026-10-19). Reached through two ports: a charge of a card token for an amount, answered accepted with a charge reference, or declined; and a refund sent with the charge reference and the amount (architect, 2026-10-20). Not reachable in this version: its only adapters are fakes that accept every card and send every refund, wherever the service runs, accepted since the demo has no real customer; a real adapter comes with CardPay; no URL, no credentials (TL, 2026-10-19).

## Contracts with neighbours

| Neighbour | Direction | Contract | What crosses |
| --------- | --------- | -------- | ------------ |
| Callers: anyone holding an identifier | inbound | `docs/contracts/billing.openapi.json` 1.3.0: `payInvoice` (tag `Payment`) | in: the identifier to pay and a `cardToken`; out: the contract's `Invoice`, without its customer, its time of payment and its charge reference |
| Callers: the accountant; anyone holding an identifier, accepted in this version: there is no login (PO, 2026-10-19) | inbound | `docs/contracts/billing.openapi.json` 1.3.0: `refundInvoice` (tag `Refund`) | in: the identifier of the invoice and the amount; out: the contract's `Refund` |
| `invoice` | outbound, in process | `InvoicePayments`, in `invoice`'s application layer, frozen 2026-10-19 | read an invoice as `getInvoice` shows it, with its customer, its `customerId` (agreed by `invoice` and `payment`: architect, 2026-10-19); record a payment, refused when the invoice is no longer `ISSUED`; record a payment cancelled (REQ-5, not built). Used from `payment`'s outbound adapter only (architect, 2026-10-19) |
| `fraud` (`customer`) | outbound, in process | `CustomerStatus`, an interface of `fraud`'s application layer (architect, 2026-10-20) | in: the invoice's `customerId`, the same text; out: is this customer blocked, yes or no (BR-4 of `fraud`) (PO, TL, architect, 2026-10-20) |

What the contract does not declare (the 400, the bound of AR-1, no body on a refusal) the code enforces (architect, 2026-10-19).

## Applicable area rules and invariants

| Entry | What it constrains here |
| ----- | ----------------------- |
| AR-1 | The card token (BR-22) and the charge reference (BR-5), the charge reference a refund stores (architect, 2026-10-19) |
| INV-DATA-001 of `../../invariants.md` | The amount paid, stored (BR-24), and the amount of a refund, stored (architect, 2026-10-19) |

## Open questions

- BR-6 has no trigger and no expected effect; REQ-5 has no operation in contract 1.3.0 (architect, 2026-10-19). Owner: PO.
- Does cancelling a payment (REQ-5) carry a fee? Owner: PO.
- CardPay does not answer, or answers out of bounds (BR-5): open until CardPay is reachable; its fake always answers (architect, 2026-10-19). Owner: architect.
- A charge accepted whose record loses to another pay or to a cancel, or fails otherwise (BR-5), is kept nowhere in this version: the card is charged and no payment is recorded. Reversed with REQ-5: open until then (architect, 2026-10-19). Owner: architect.
- A review placed between `payment`'s ask of `CustomerStatus` and the record of a pay or a refund lets that pay or refund through (BR-32): one check per pay or refund is accepted for this version (PO, architect, 2026-10-20). Owner: PO, architect.
- Anyone may retry a pay after a 402 and so test card tokens: accepted in this version, there is no login (PO, 2026-10-19). Owner: PO.
- The card token reaches the log when `org.springframework.web` runs at DEBUG or TRACE: the request model generated from the contract prints it, and Spring logs the body and the arguments it reads (BR-24). Accepted in this version: the demo never runs that logger at DEBUG or TRACE and has no real card token; open until a real CardPay adapter comes; no contract change (TL, 2026-10-19). Owner: TL.

- How is a refund learned a refund is `SETTLED`, and what happens to one CardPay does not settle? Through CardPay's notifications: nothing moves a refund to `SETTLED` in this version (TL, 2026-10-19). Owner: TL, architect.
- A send CardPay does not accept (BR-28): what follows? Open until CardPay is reachable; its fake always sends (architect, 2026-10-19). Owner: architect.
- The amount of a refund reaches the log when `org.springframework.web` runs at DEBUG or TRACE: the request model generated from the contract prints it, and Spring logs the body it reads (BR-31). Accepted in this version, as for the card token in `payment`: the demo never runs that logger at DEBUG or TRACE; open until a real CardPay adapter comes (TL, 2026-10-19). Owner: TL.
- A refund left `REQUESTED` (BR-28: sent and its `SENT` not recorded, or never sent) counts against the amount paid for good in this version: open until CardPay is reachable (architect, 2026-10-19). Owner: architect.

### Attack — 2026-10-19 — feature "the accountant issues the invoice of a confirmed order, and anyone can read it back"

Moved from `invoice` with the rules it attacks (PO, BA, 2026-10-19).

6. **Read access reaches payment and cancellation.** REQ-4 "The customer pays", REQ-5 "The customer can cancel a paid invoice": neither says _the customer of the invoice_. Scenario: B reads A's invoice, takes its identifier and cancels A's payment within 24 hours (REQ-5). Missing: which caller may cancel which paid invoice. Owner: PO. The pay (REQ-4): anyone holding the identifier, accepted for this version (BR-21; PO, 2026-10-19).

### Attack — 2026-10-19 — feature "the customer pays an issued invoice by card, through CardPay"

Moved from `invoice` with the rules it attacked (PO, BA, 2026-10-19). The findings still open; the others were settled on 2026-10-19 and written in the sections above. Findings 1 and 2 (a charge whose record loses or fails) are the open question on the charge kept nowhere, above.

4. **A paid invoice has no way out in this version.** It is not cancelled by the accountant (BR-4 of `invoice`), not cancelled by the customer (REQ-5 is a later feature), and blocks any new issue for its order (BR-1 of `invoice`); anyone holding the identifier pays it (BR-21). Scenario: the accountant issues wrong lines; someone holding the identifier pays before the correction; no correction can be issued. Accepted until REQ-5 (PO, 2026-10-19). Owner: PO.

Skipped: the siblings (the pay has one inbound surface, `payInvoice`; the refusal of a paid invoice reaches `cancelInvoice` (BR-4 of `invoice`) and `issueInvoice` (BR-1 of `invoice`), both written), the contract (the pay answers the contract's `Invoice`, without the time of the payment or the charge reference; CardPay is a port of `payment`, no neighbour's contract is relied on), the budget (nothing outside `payment` is read but the contracts), the session (a status changed under the caller is answered by BR-20's 409 before any charge; a race is finding 1).

### Attack — 2026-10-19 — feature "refund a paid invoice, and the fraud review on payments and refunds"

Carried from `refund` with the rules it attacked (BA, 2026-10-20).

Settled before the attack and written above or in the plan: two refunds at once (BR-25), a request sent again (two refunds, the cap bounds them), anyone holding the identifier refunds, the 403 telling that a customer is under review, the logs (BR-31), the list read at start-up, no read of a refund (PO, TL, architect, 2026-10-19).

Both findings were settled on 2026-10-19 (architect): the record of `SENT` failing (BR-28 and the open question on a refund left `REQUESTED`), and the list's rules written twice (then AR-3 of `../area.md`, now BR-32).

Skipped: the siblings (AR-3 is enforced on both inbound surfaces that pay or refund, `payInvoice` and `refundInvoice`; a cancel is not reached by AR-3, PO), the session (the list changes only at a restart; a status changed under the caller is answered by BR-25's 409), the budget (then: `refund` read `payment` only through `PaymentRefunds`), the words (the customer of AR-3 is defined in AR-3's trigger; Amount paid has one term).
