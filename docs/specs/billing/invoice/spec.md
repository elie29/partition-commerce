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

**In scope:** issuing an invoice, keeping it as issued, cancelling it, paying it by card and cancelling a paid invoice (REQ-1 to REQ-5). Reading an invoice back by its identifier (`getInvoice`; PO, 2026-10-19).

**Out of scope:** the confirmation of the order that triggers the invoice (REQ-1): no contract of the project exposes it. The accountant's issue is how REQ-1's issue happens for now; an automatic issue on confirmation waits for an order context (PO, 2026-10-19). The order, its customer and its items: no bounded context owns them yet; the issue request carries `orderId`, `customerId` and the lines (architect, 2026-10-19). Charging a card and reversing a charge: handed to CardPay (REQ-4, REQ-5).

## Language

One meaning per word, no synonyms. A word that changes meaning in a neighbour marks the boundary.

| Term | Meaning in this bounded context |
| ---- | ------------------------------- |
| Invoice | What the customer owes for one confirmed order, line by line. |
| Issued | Recorded by the issue, with an identifier of its own; nothing is sent to the customer in this version, and there is no state before it; from then on never modified (BA, 2026-10-19). |
| Paid | Its amount has been paid in full. |
| Line | What the contract's `Line` holds: a label and a price; no quantity, no tax (PO, 2026-10-19). |
| Amount | The sum of the prices of an invoice's lines (PO, 2026-10-19). |
| Price | What one line costs (PO, 2026-10-19). |
| Blank | Empty or only white space; a non-breaking space (U+00A0, U+2007, U+202F) is white space (PO, BA, 2026-10-19). |
| Character | A Unicode code point: an emoji is one character (architect, 2026-10-19). |
| Order | What the customer confirmed, identified by its `orderId`; it lives outside `invoice` (BA, 2026-10-19). |
| Confirmed | The state of an order that may be invoiced; `invoice` takes it as given (BA, 2026-10-19). |
| Customer | Who owes the invoice, identified by its `customerId` (BA, 2026-10-19). |
| Accountant | The person who issues an invoice and cancels one before any payment (BA, 2026-10-19). |

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
| BR-1 | An invoice is issued once per order. | An issue for an order that already has an invoice, two issues of one order at the same moment included | Refused: 409, its description only, no body; the database refuses the second of two simultaneous issues; checked after BR-13 | from source: REQ-1; trigger and effect: PO, architect, 2026-10-19 |
| BR-2 | The amount of an invoice is the sum of its lines. | An issue accepted | The amount is the sum of the prices of its lines, in their currency (Amount, BR-7) | from source: REQ-1; trigger and effect: PO, 2026-10-19 |
| BR-3 | An issued invoice is never modified. | — | — | from source: REQ-2 |
| BR-4 | An invoice is cancelled only before any payment, at no fee. | — | — | from source: REQ-3 |
| BR-5 | An invoice is paid once its amount is paid in full. | — | — | from source: REQ-4 |
| BR-6 | A paid invoice is cancelled only within 24 hours of being paid; the invoice is then unpaid again. | — | — | from source: REQ-5 |
| BR-7 | The lines of an invoice are all in one currency. | An issue whose lines are in different currencies | Refused: 400, no body | PO, 2026-10-19 |
| BR-8 | A price is zero or more. | An issue with a negative price | Refused: 400, no body; a price of 0 is allowed | PO, 2026-10-19 |
| BR-9 | A currency is a known code: a code of the JDK's `Currency` list that does not start with `X`. The list is the JDK's, updated with the JDK; fund codes and the withdrawn codes it still lists are known in this version. | An issue with a currency the contract's pattern accepts but that is not a known code: one the JDK does not list, or one starting with `X` (`XXX`, `XTS`, `XAU`, `XDR`, …) | Refused: 400, no body; the contract checks the form, the code that the code is known | PO, 2026-10-19 |
| BR-10 | The amount stays within the range of the amount. | An issue whose sum of prices goes beyond that range | Refused: 400, no body | PO, 2026-10-19 |
| BR-11 | `orderId` and `customerId` are any non-blank text of at most 255 characters. | An issue with one of them blank or longer | Refused: 400, no body | PO, 2026-10-19 |
| BR-12 | A label is non-blank and holds at most 255 characters. | An issue with a label blank or longer | Refused: 400, no body | architect, 2026-10-19 |
| BR-13 | A request the contract refuses, or one BR-7 to BR-12 refuse, is refused before BR-1 is checked. | An issue whose content is refused | Refused: 400, no body, whether or not the order already has an invoice | architect, 2026-10-19 |
| BR-14 | An invoice has an identifier of its own, a UUID made by `invoice` when it issues. | An issue accepted | 201 with the issued invoice and its identifier | architect, 2026-10-19 |
| BR-15 | An invoice is read by anyone holding its identifier. | A read | 200 with the invoice; an identifier that names no invoice, one that is not a UUID included: 404 | PO, architect, 2026-10-19 |
| BR-16 | An invoice has at least one line; there is no maximum in this version. | An issue with no line | Refused by the contract: 400, no body (BR-13) | PO, 2026-10-19 |
| BR-17 | The lines of an invoice are read back in the order they were issued. | A read | The lines in the order of the issue request | PO, 2026-10-19 |

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
| Callers: the accountant (issue), anyone holding an identifier (read) | inbound | `docs/contracts/billing.openapi.json` 1.0.0: `issueInvoice`, `getInvoice` | in: `orderId`, `customerId`, the lines; out: the invoice, without its customer |

What the contract does not declare (the 400, the 255 characters, blank) the code enforces; the contract stays 1.0.0 (architect, 2026-10-19).

## Applicable area rules and invariants

None.

## Open questions

- "Cancelled" is given two meanings: "Voided before any payment, at no fee" and "Reversed within 24 hours". Which one holds in `invoice`, and does the other belong to another bounded context, or take another word?
- REQ-1 against REQ-2: is a correction issued as a second invoice for the same order, or does "issued once per order" hold? A later version of the PRD settles it (PO, 2026-10-19).
- Can an invoice be partly paid? REQ-3 says "before any payment", REQ-4 "paid in full".
- Are the "payment" of REQ-3 and the "charge" of REQ-5 the same thing?
- After a paid invoice is cancelled and is "unpaid again" (REQ-5), can it be paid again, or cancelled by the accountant (REQ-3)?
- Does cancelling a paid invoice (REQ-5) carry a fee?
- BR-3 to BR-6 have no trigger and no expected effect.
- A currency a later JDK drops (BR-9, "updated with the JDK"): an invoice issued in it no longer passes BR-9, yet is read as issued (BR-3, BR-15). How is it read once the JDK is upgraded? (2026-10-19). Owner: architect.

### Attack — 2026-10-19 — feature "the accountant issues the invoice of a confirmed order, and anyone can read it back"

The findings still open; the others were settled on 2026-10-19 and written in the sections above.

1. **Who may read an invoice.** Anyone holding its identifier reads it (BR-15); who may read stays open. Accepted for this version (PO, 2026-10-19). Owner: PO.
6. **Read access reaches payment and cancellation.** REQ-4 "The customer pays", REQ-5 "The customer can cancel a paid invoice": neither says _the customer of the invoice_. Scenario: B reads A's invoice, takes its identifier and cancels A's payment within 24 hours (REQ-5). Missing: which caller may pay or cancel which invoice. Owner: PO.
7. **Who may issue.** No login in this version: anyone can issue, and can issue first for an order, so that the accountant's issue is then refused by BR-1. Accepted for this version (PO, TL, 2026-10-19). Owner: PO.
13. **The H2 console** (`/h2-console`, enabled in `application.yaml`) is reachable. Accepted for this version (PO, TL, 2026-10-19). Owner: TL.
