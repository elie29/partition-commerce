# invoice

> **Source:** `docs/sources/billing.md`
>
> **Source version:** 1.0
>
> **Provenance:** 2026-10-19 · dc3486014a598118fa1b59468483e82314e63e7f
>
> **Scope:** whole source
>
> **Decomposition:** see `../bounded-contexts.md`

<!-- The header above is filled by partition-shape, or partition-reshape for a fission. -->
<!-- The empty ">" lines keep each header line on its own line when the Markdown is reformatted. -->

## Business objective

Bill the customer for a confirmed order, and collect the payment of the invoice by card.

## Scope

**In scope:** issuing an invoice, keeping it as issued, cancelling it, paying it by card and cancelling a paid invoice (REQ-1 to REQ-5).

**Out of scope:** the confirmation of the order that triggers the invoice (REQ-1): no contract of the project exposes it. Charging a card and reversing a charge: handed to CardPay (REQ-4, REQ-5).

## Language

One meaning per word, no synonyms. A word that changes meaning in a neighbour marks the boundary.

| Term | Meaning in this bounded context |
| ---- | ------------------------------- |
| Invoice | What the customer owes for one confirmed order, line by line. |
| Issued | Sent to the customer; from then on never modified. |
| Paid | Its amount has been paid in full. |

## Source parts owned

What this bounded context owns in its source. The source says what each part holds; this spec does not repeat it.

| Unit | Where | What it is for |
| --- | --- | --- |
| REQ-1 | [REQ-1](../../../sources/billing.md#req-1--issue-an-invoice) | Issue an invoice for a confirmed order |
| REQ-2 | [REQ-2](../../../sources/billing.md#req-2--keep-an-issued-invoice-as-issued) | Keep an issued invoice as issued, and how a correction is made |
| REQ-3 | [REQ-3](../../../sources/billing.md#req-3--cancel-an-invoice) | The accountant cancels an invoice |
| REQ-4 | [REQ-4](../../../sources/billing.md#req-4--pay-an-invoice) | The customer pays an invoice by card |
| REQ-5 | [REQ-5](../../../sources/billing.md#req-5--cancel-a-paid-invoice) | The customer cancels a paid invoice |
| DEC-1 | [DEC-1](../../../sources/billing.md#dec-1--cardpay) | The choice of the card payment provider |
| Glossary | [Glossary](../../../sources/billing.md#glossary) | The two meanings of "Cancelled", for an unpaid and a paid invoice |

## Business rules

| # | Rule | Trigger | Expected effect | Source |
| --- | --- | --- | --- | --- |
| BR-1 | An invoice is issued once per order. | — | — | from source: REQ-1 |
| BR-2 | The amount of an invoice is the sum of its lines. | — | — | from source: REQ-1 |
| BR-3 | An issued invoice is never modified. | — | — | from source: REQ-2 |
| BR-4 | An invoice is cancelled only before any payment, at no fee. | — | — | from source: REQ-3 |
| BR-5 | An invoice is paid once its amount is paid in full. | — | — | from source: REQ-4 |
| BR-6 | A paid invoice is cancelled only within 24 hours of being paid; the invoice is then unpaid again. | — | — | from source: REQ-5 |

BR-4 against BR-6, and BR-3 against BR-5 and BR-6, are kept as written in 1.0, knowingly (PO, 2026-10-19); see `../bounded-contexts.md`, _Gaps detected_.

## Decisions

The decisions the source states, copied with their reason. A decision taken while building lives in the plan that took it; what it established is written in the sections above.

| Decision | Reason | Source | Date |
| -------- | ------ | ------ | ---- |
| Card payments go through CardPay | it is the provider the company already has a contract with | DEC-1 | 1.0 |

## Persistence

No schema documents are declared for this project.

## External dependencies

- **CardPay** — charges the customer's card when an invoice is paid (REQ-4), reverses the charge when a paid invoice is cancelled (REQ-5).

## Contracts with neighbours

| Neighbour | Direction | Contract | What crosses |
| --------- | --------- | -------- | ------------ |

## Applicable area rules and invariants

None.

## Open questions

- "Cancelled" is given two meanings: "Voided before any payment, at no fee" and "Reversed within 24 hours". Which one holds in `invoice`, and does the other belong to another bounded context, or take another word?
- REQ-1 against REQ-2: is a correction issued as a second invoice for the same order, or does "issued once per order" hold? A later version of the PRD settles it (PO, 2026-10-19).
- Where does "an order is confirmed" (REQ-1) come from? No contract of the project exposes it.
- Can an invoice be partly paid? REQ-3 says "before any payment", REQ-4 "paid in full".
- Are the "payment" of REQ-3 and the "charge" of REQ-5 the same thing?
- After a paid invoice is cancelled and is "unpaid again" (REQ-5), can it be paid again, or cancelled by the accountant (REQ-3)?
- Does cancelling a paid invoice (REQ-5) carry a fee?
- What does an invoice line hold besides the ordered item?
