# fraud

> **Source:** `docs/legacy/customer-spec.md`
>
> **Source version:** no version in the source; read on 2026-10-20
>
> **Provenance:** 2026-10-20 · 747a8dc79c13a273f103abb879e407ce6b9047d3
>
> **Scope:** whole source
>
> **Decomposition:** see `../bounded-contexts.md`

<!-- The header above is filled by partition-shape, or partition-reshape for a fission. -->
<!-- The empty ">" lines keep each header line on its own line when the Markdown is reformatted. -->

## Business objective

Let the risk team place a customer under fraud review and lift it, and tell the other services whether a customer is blocked.

## Scope

**In scope:** the fraud review the risk team runs on customers (`#3-fraud-review`): placing, lifting and reading a review, use cases with no entry point in this version, called by their tests only, as the retirement of a product (architect, 2026-10-20); and whether a customer is blocked, through `CustomerStatus`.

**Out of scope:** the customer accounts: `customer` (`#2-customer-accounts`). Paying and refunding, and refusing them to a customer under fraud review: `payment` in `billing` (`payInvoice` and `refundInvoice` of `docs/contracts/billing.openapi.json`), which asks `fraud` through `CustomerStatus` (BR-3). The HTTP operations of the risk team: they come with a later contract (architect, 2026-10-20). Archiving: nothing archives in this version (PO, 2026-10-20).

## Language

One meaning per word, no synonyms. A word that changes meaning in a neighbour marks the boundary.

| Term | Meaning in this bounded context |
| ---- | ------------------------------- |
| Archived | What becomes of a review older than one year, instead of being deleted (BR-2). (BA, 2026-10-20) |
| Blocked | Of a customer: while a review of it is open, not lifted (BR-4). (PO, BA, 2026-10-20) |
| Customer | Who a review names, by the `customerId` billing holds, the same text. (architect, 2026-10-20) |
| Fraud review | What a member of the risk team places a customer under, and lifts (#3-fraud-review). (BA, 2026-10-20) |
| Lifted | Of a review: ended by the risk team. (BA, 2026-10-20) |
| Open | Of a review: placed, not lifted; a customer has one at most (BR-6). (PO, BA, 2026-10-20) |
| Risk team | Its members place a customer under fraud review and are the only ones who lift it (#3-fraud-review, BR-1). (BA, 2026-10-20) |
| Under fraud review | Of a customer: with an open review (BR-3). (BA, 2026-10-20) |

## Source parts owned

What this bounded context owns in its source. The source says what each part holds; this spec does not repeat it.

| Unit | Where | What it is for |
| --- | --- | --- |
| #3-fraud-review | [3. Fraud review](../../../legacy/customer-spec.md#3-fraud-review) | The risk team places a customer under fraud review and lifts it; the other services ask whether a customer is blocked |
| #1-overview | [1. Overview](../../../legacy/customer-spec.md#1-overview) | What the service covers: context, not copied |
| #4-data | [4. Data](../../../legacy/customer-spec.md#4-data) | The table of the reviews and the kind of their identifiers: not copied |
| #6-ruling-history | [6. Ruling history](../../../legacy/customer-spec.md#6-ruling-history) | The risk committee's rulings of 2024-03-12 and 2024-09-02 on reviews: copied below, nothing else |

## Business rules

| # | Rule | Trigger | Expected effect | Source |
| --- | --- | --- | --- | --- |
| BR-1 | A review is lifted only by a member of the risk team. | A lift, which names the customer by its `customerId` (architect, 2026-10-20) | The customer's open review is lifted, its time lifted recorded. A customer with no open review: nothing changes (PO, 2026-10-20). Of a lift and a place of one customer crossing, the last to commit wins (PO, 2026-10-20). Nothing is checked about who lifts in this version: the demo runs without a login (PO, 2026-10-20) | from source: #3-fraud-review; confirmed: BA, 2026-10-20 |
| BR-2 | Reviews older than one year are archived, not deleted. | — | Nothing archives in this version (PO, 2026-10-20) | from source: #6-ruling-history, 2024-09-02; confirmed: BA, 2026-10-20 |
| BR-3 | A customer under fraud review can neither pay nor be refunded. | A pay or a refund in `payment`, which asks `CustomerStatus` | `CustomerStatus` answers blocked as BR-4 says; `payment` refuses (its BR-32) | from source: #6-ruling-history, 2024-03-12; owned by `fraud`, `payment` consumes it through `CustomerStatus` (owner, 2026-10-20); confirmed: BA, 2026-10-20 |
| BR-4 | A customer is blocked while a review of it is open, not lifted. | `CustomerStatus` asked for a customer | Yes while a review of it is open; no otherwise | PO, BA, 2026-10-20 |
| BR-5 | Only a member of the risk team places a review. | A place, which names the customer by its `customerId` | A customer with no open review: a new review is recorded, open, with its time placed; after a lift too, a new review (PO, 2026-10-20). Nothing is checked about who places in this version: the demo runs without a login | PO, 2026-10-20 |
| BR-6 | A customer has one open review at most. | A place of a customer already under review; two places of one customer at the same moment | Nothing changes. Held at every write, by the store: of two places at the same moment, the first to commit wins, the other changes nothing (architect, 2026-10-20) | PO, 2026-10-20; held by the store: architect, 2026-10-20 |
| BR-7 | The `customerId` of a place is non-blank and holds at most 255 characters, as billing's AR-1 bounds it. | A place with a `customerId` blank or longer | Refused; nothing is placed. A lift, a read and `CustomerStatus` take the text as given: one no review names lifts nothing, reads none, answers no (PO, TL, 2026-10-20) | PO, TL, 2026-10-20 |
| BR-8 | A customer's reviews are read by its `customerId`. | A read | The customer's reviews, by time placed, oldest first (PO, 2026-10-20), each with its identifier, open or lifted, its time placed and its time lifted; none for a customer never placed | PO, architect, 2026-10-20 |
| BR-9 | A log or an error of `fraud` names a review's identifier, never its `customerId`. | Any log or error of `fraud` | Never the `customerId` | TL, 2026-10-20 |

No other rule (BA, 2026-10-20); BR-4 to BR-9 are the answers of the PO, the BA, the architect and the TL of the same day.

A review's own identifier is a UUID `fraud` makes (architect, 2026-10-20; `#5-conventions`). A review records its identifier, the `customerId`, open or lifted, its time placed and its time lifted, to the millisecond, in UTC (architect, TL, 2026-10-20).

## Decisions

The decisions the source states, copied with their reason. A decision taken while building lives in the plan that took it; what it established is written in the sections above.

| Decision | Reason | Source | Date |
| -------- | ------ | ------ | ---- |

## Persistence

No schema documents are declared for this project.

## External dependencies

None.

## Contracts with neighbours

| Neighbour | Direction | Contract | What crosses |
| --------- | --------- | -------- | ------------ |
| Callers: none in this version | inbound | none: placing, lifting and reading a review are use cases with no entry point, called by their tests only (architect, 2026-10-20) | — |
| `payment` (`billing`), in `payment-app` | inbound, over HTTP | `docs/contracts/billing-internal.openapi.json` 1.0.0, `getCustomerStatus` (tag `CustomerStatus`), frozen | in: a customer, by the `customerId` billing holds, the same text (architect, 2026-10-20); out: is this customer blocked, yes or no (BR-4) |

## Applicable area rules and invariants

| Entry | What it constrains here |
| ----- | ----------------------- |

None.

## Open questions

4. "Reviews older than one year" (BR-2): counted from the placing or from the lifting? Open until an operation needs it (PO, 2026-10-20).
5. Does an archived review still block its customer? Open until an operation needs it (PO, 2026-10-20).
7. Which services besides `payment` ask whether a customer is blocked, and through which contract? (`payment`: `CustomerStatus`, architect, 2026-10-20)
13. Nobody can place or lift a review in the running service: placing and lifting have no entry point in this version, so `CustomerStatus` answers no for every customer outside the tests. Accepted for this version, open until the HTTP operations come (PO, 2026-10-20). Owner: PO.

### Attack — 2026-10-20 — feature "the risk team places and lifts a fraud review, and `payment` asks `fraud` whether a customer is blocked"

Eight findings, all settled on 2026-10-20 and written above or in `../../billing/payment/spec.md`: nobody can block in the running service (question 13, PO); two places at the same moment (BR-6, architect); a review placed between `payment`'s ask and its record (an open question of `payment`, PO, architect); `CustomerStatus` failing (BR-32 of `payment`, architect); a place and a lift crossing (BR-1, PO); what a review stores (the paragraph under `Business rules`, architect, TL); the `customerId` in a log (BR-9, TL); three words for one state (`Language`: Under fraud review, Lifted, BA).

Skipped: the siblings (BR-3 is enforced on both inbound surfaces that pay or refund, `payInvoice` and `refundInvoice`, by BR-32 of `payment`; a cancel is not reached, PO, 2026-10-19), the budget (`fraud` reads nothing of a neighbour; `payment` reaches `fraud` through `CustomerStatus` only; the two change apart), the inputs beyond question 11 (the `customerId` is matched as the same text, answer D).
