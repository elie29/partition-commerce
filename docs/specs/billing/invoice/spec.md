# invoice

> **Source:** `docs/sources/billing.md`
>
> **Source version:** 1.1
>
> **Provenance:** 2026-10-19 · 972aeb0000d5498ea82996110ca39366fadffb37
>
> **Scope:** REQ-1 to REQ-3 of the whole source; REQ-4, REQ-5 and DEC-1 went to `payment` (fission, 2026-10-19)
>
> **Decomposition:** see `../bounded-contexts.md`

<!-- The header above is filled by partition-shape, or partition-reshape for a fission. -->
<!-- The empty ">" lines keep each header line on its own line when the Markdown is reformatted. -->

## Business objective

Bill the customer for a confirmed order.

## Scope

**In scope:** issuing an invoice, keeping it as issued, cancelling it (REQ-1 to REQ-3). Reading an invoice back by its identifier (`getInvoice`; PO, 2026-10-19). Recording, for `payment`, an invoice paid and a payment cancelled (`InvoicePayments`; architect, 2026-10-19).

**Out of scope:** the confirmation of the order that triggers the invoice (REQ-1): no contract of the project exposes it. The accountant's issue is how REQ-1's issue happens for now; an automatic issue on confirmation waits for an order context (PO, 2026-10-19). The order, its customer and its items: no bounded context owns them yet; the issue request carries `orderId`, `customerId` and the lines (architect, 2026-10-19). Paying an invoice by card and cancelling a payment (REQ-4, REQ-5): `payment` (fission, 2026-10-19).

## Language

One meaning per word, no synonyms. A word that changes meaning in a neighbour marks the boundary. The words shared by `billing` (Amount, Blank, Character, Currency, Minor units, Payment, Refused) are in `../area.md`.

| Term | Meaning in this bounded context |
| ---- | ------------------------------- |
| Invoice | What the customer owes for one confirmed order, line by line. |
| Issued | Recorded by the issue, with an identifier of its own; nothing is sent to the customer in this version, and there is no state before it; from then on never modified (BA, 2026-10-19). Not the status `ISSUED`, which is an invoice neither paid nor cancelled (PO, BA, 2026-10-19). |
| Paid | Its amount has been paid in full: its charge is recorded; its status `PAID` (PO, 2026-10-19). |
| Line | What the contract's `Line` holds: a label and a price; no quantity, no tax (PO, 2026-10-19). |
| Price | What one line costs (PO, 2026-10-19). |
| Order | What the customer confirmed, identified by its `orderId`; it lives outside `invoice` (BA, 2026-10-19). |
| Confirmed | The state of an order that may be invoiced; `invoice` takes it as given (BA, 2026-10-19). |
| Customer | Who owes the invoice, identified by its `customerId` (BA, 2026-10-19). |
| Accountant | The person who issues an invoice and cancels one before any payment (BA, 2026-10-19). |
| Cancelled | Of an invoice not paid: voided before any payment, at no fee (BR-4); its status `CANCELLED` (PO, 2026-10-19). Voided only: an invoice whose payment is cancelled is not cancelled, for BR-1 too; it is unpaid again, `ISSUED`, and can be paid again (PO, 2026-10-19). Not `payment`'s cancelled, a payment reversed. |
| Correction | Two actions: the accountant cancels an invoice, then issues a new one for the same order with what any issue carries, which may differ from the cancelled invoice; the new invoice keeps no link to the cancelled one (PO, 2026-10-19). |

## Source parts owned

What this bounded context owns in its source. The source says what each part holds; this spec does not repeat it.

| Unit | Where | What it is for |
| --- | --- | --- |
| REQ-1 | [REQ-1](../../../sources/billing.md#req-1--issue-an-invoice) | Issue an invoice for a confirmed order |
| REQ-2 | [REQ-2](../../../sources/billing.md#req-2--keep-an-issued-invoice-as-issued) | Keep an issued invoice as issued, and how a correction is made |
| REQ-3 | [REQ-3](../../../sources/billing.md#req-3--cancel-an-invoice) | The accountant cancels an invoice |
| Glossary | [Glossary](../../../sources/billing.md#glossary) | "Cancelled" of an unpaid invoice |

## Business rules

| # | Rule | Trigger | Expected effect | Source |
| --- | --- | --- | --- | --- |
| BR-1 | An order has at most one invoice that is not cancelled. | An issue for an order that already has an invoice that is not cancelled, a `PAID` invoice included (PO, 2026-10-19) | Refused, nothing is issued; of two issues of one order at the same moment, the second is refused. 409, no body; an issue that races the cancel of its order's invoice gets 409 until the cancel has committed; the database holds the rule, for simultaneous issues too | from source: REQ-1 (1.0.1); trigger and effect: owner, 2026-10-19; response and database: architect, 2026-10-19 |
| BR-2 | The amount of an invoice is the sum of its lines. | An issue accepted | The amount is the sum of the prices of its lines, in their currency (Amount, BR-7) | from source: REQ-1; trigger and effect: PO, 2026-10-19 |
| BR-3 | An issued invoice is never modified. | A cancel (BR-4), the record of a payment (BR-23) or of a payment cancelled (REQ-5) | Not a modification: only the status changes, to cancelled, to paid, or back to `ISSUED` when its payment is cancelled (PO, 2026-10-19); the lines, the amount, the order and the customer stay as issued; a cancel and a record record nothing else: the time of the payment and the charge reference are `payment`'s | from source: REQ-2; trigger and effect: PO, 2026-10-19 |
| BR-4 | An invoice is cancelled only before any payment, at no fee. | A cancel of an invoice whose status is `ISSUED`: neither paid nor cancelled (PO); an invoice whose pay was refused included | 200 with the invoice, cancelled (BR-3); at no fee: the cancel charges nothing and calls no third party. A cancel of an invoice whose status is `PAID`: refused, 409, no body; nothing changes | from source: REQ-3; trigger and effect: PO, architect, 2026-10-19 |
| BR-7 | The lines of an invoice are all in one currency. | An issue whose lines are in different currencies | Refused: 400, no body | PO, 2026-10-19 |
| BR-8 | A price is zero or more. | An issue with a negative price | Refused: 400, no body; a price of 0 is allowed | PO, 2026-10-19 |
| BR-9 | A currency is a known code: a code of the JDK's `Currency` list that does not start with `X`. The list is the JDK's, updated with the JDK; fund codes and the withdrawn codes it still lists are known in this version. | An issue with a currency the contract's pattern accepts but that is not a known code: one the JDK does not list, or one starting with `X` (`XXX`, `XTS`, `XAU`, `XDR`, …) | Refused: 400, no body; the contract checks the form, the code that the code is known | PO, 2026-10-19 |
| BR-10 | The amount stays within the range of the amount. | An issue whose sum of prices goes beyond that range | Refused: 400, no body | PO, 2026-10-19 |
| BR-11 | `orderId` and `customerId` are any text bounded as a label is (BR-12). | An issue with one of them blank or longer | Refused: 400, no body | PO, 2026-10-19 |
| BR-12 | A label is bounded by AR-1. | An issue with a label blank or longer | Refused: 400, no body | architect, 2026-10-19 |
| BR-13 | A request the contract refuses, or one BR-7 to BR-12 refuse, is refused before BR-1 is checked. | An issue whose content is refused | Refused: 400, no body, whether or not the order already has an invoice. A request with no content type keeps the framework's 415 | architect, 2026-10-19 |
| BR-14 | An invoice has an identifier of its own, a UUID made by `invoice` when it issues. | An issue accepted | 201 with the issued invoice and its identifier | architect, 2026-10-19 |
| BR-15 | An invoice is read by anyone holding its identifier. | A read | 200 with the invoice; an identifier that names no invoice, one that is not a UUID included: 404 | PO, architect, 2026-10-19 |
| BR-16 | An invoice has at least one line; there is no maximum in this version. | An issue with no line | Refused by the contract: 400, no body (BR-13) | PO, 2026-10-19 |
| BR-17 | The lines of an invoice are read back in the order they were issued. | A read | The lines in the order of the issue request | PO, 2026-10-19 |
| BR-18 | A cancelled invoice is not cancelled again. | A cancel of a cancelled invoice, the second of two cancels of one invoice at the same moment included | Refused: 409, no body; nothing changes | PO, architect, 2026-10-19 |
| BR-19 | An invoice is cancelled by anyone holding its identifier. | A cancel | An identifier that names no invoice, one that is not a UUID included: 404, no body | PO, architect, 2026-10-19 |
| BR-23 | A payment is recorded only on an invoice still `ISSUED`. | A record of a payment, by `payment` through `InvoicePayments` | The status becomes `PAID` by a conditional update on an invoice still `ISSUED`, as a cancel is, in the caller's transaction: of a pay and a cancel of one invoice at the same moment, the first to commit holds. An invoice no longer `ISSUED`: refused, nothing changes | architect, 2026-10-19 (the conditional record of BR-5 of `payment`) |

BR-4 against BR-6 of `payment`, and BR-3 against BR-5 and BR-6 of `payment`, are kept as written in 1.0, knowingly (PO, 2026-10-19); see `../bounded-contexts.md`, _Gaps detected_.

## Decisions

The decisions the source states, copied with their reason. A decision taken while building lives in the plan that took it; what it established is written in the sections above.

None: DEC-1 went to `payment` (fission, 2026-10-19).

## Persistence

No schema documents are declared for this project.

## External dependencies

None: CardPay is `payment`'s (fission, 2026-10-19).

## Contracts with neighbours

| Neighbour | Direction | Contract | What crosses |
| --------- | --------- | -------- | ------------ |
| Callers: the accountant (issue), anyone holding an identifier (read, cancel) | inbound | `docs/contracts/billing.openapi.json` 1.3.0: `issueInvoice`, `getInvoice`, `cancelInvoice` (tag `Invoice`) | in: `orderId`, `customerId`, the lines; the identifier to cancel; out: the invoice, without its customer |
| `payment` | inbound, in process | `InvoicePayments`, in `invoice`'s application layer, frozen 2026-10-19 | read an invoice as `getInvoice` shows it, with its customer, its `customerId` (agreed by `invoice` and `payment`: architect, 2026-10-19); record a payment (BR-23); record a payment cancelled (REQ-5, not built). Used from `payment`'s outbound adapter only (architect, 2026-10-19) |

What the contract does not declare (the 400, the bound of AR-1, no body on a refusal) the code enforces (architect, 2026-10-19).

## Applicable area rules and invariants

AR-1, AR-2 of `../area.md`, with its language.

## Open questions

- An invoice whose payment is cancelled is `ISSUED` again and is not cancelled by the accountant: REQ-3 holds (PO, 2026-10-19). Yet BR-4 cancels any invoice whose status is `ISSUED`. How does `invoice` tell an invoice whose payment was cancelled from one never paid? Settled with REQ-5 (2026-10-19). Owner: PO, architect.
- After a refund of all or part of a paid invoice (REQ-6, `refund`), what is the invoice's status, and does a fully refunded invoice still count for BR-1? (1.1, 2026-10-19). Owner: PO.
- A currency a later JDK drops (BR-9, "updated with the JDK"): an invoice issued in it no longer passes BR-9, yet is read as issued (BR-3, BR-15). How is it read once the JDK is upgraded? (2026-10-19). Owner: architect.

### Attack — 2026-10-19 — feature "the accountant issues the invoice of a confirmed order, and anyone can read it back"

The findings still open; the others were settled on 2026-10-19 and written in the sections above.

1. **Who may read an invoice.** Anyone holding its identifier reads it (BR-15); who may read stays open. Accepted for this version (PO, 2026-10-19). Owner: PO.
6. Moved to `payment` with the rules it attacks (fission, 2026-10-19). The accountant's cancel before any payment (REQ-3): anyone holding the identifier, accepted for this version (BR-19; PO, 2026-10-19).
7. **Who may issue.** No login in this version: anyone can issue, and can issue first for an order, so that the accountant's issue is then refused by BR-1. Accepted for this version (PO, TL, 2026-10-19). Owner: PO.
13. **The H2 console** (`/h2-console`, enabled in `application.yaml`) is reachable. Accepted for this version (PO, TL, 2026-10-19). Owner: TL.

### Attack — 2026-10-19 — feature "the accountant cancels an invoice before any payment, and issues its correction"

The findings still open; the others were settled on 2026-10-19 and written in the sections above.

2. **Replacing someone else's invoice.** B reads A's invoice (BR-15), cancels it (BR-19), then issues for the same order with B's own `customerId` and lines (BR-1, Correction): A's order now carries B's invoice. Accepted for this version, with the cancel: there is no login (PO, 2026-10-19). Owner: PO.
4. **The correction cannot be found from the cancelled invoice** (Correction). Accepted for this version (PO, 2026-10-19). Owner: PO.

Skipped: the data (a cancel answers the invoice as a read does, without its customer, and records only the status), the budget (nothing outside `invoice` is read but its contract), the session (no session: a stale status is answered by BR-18 or BR-1).

The attack on the feature "the customer pays an issued invoice by card, through CardPay" moved to `payment` with the rules it attacked (fission, 2026-10-19).
