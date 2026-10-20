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

**In scope:** the fraud review the risk team runs on customers (`#3-fraud-review`).

**Out of scope:** the customer accounts: `customer` (`#2-customer-accounts`). Refusing to pay or refund a customer under fraud review: `payment` in `billing` (its BR-32; `payInvoice` and `refundInvoice` of `docs/contracts/billing.openapi.json`).

## Language

One meaning per word, no synonyms. A word that changes meaning in a neighbour marks the boundary.

| Term | Meaning in this bounded context |
| ---- | ------------------------------- |

## Source parts owned

What this bounded context owns in its source. The source says what each part holds; this spec does not repeat it.

| Unit | Where | What it is for |
| --- | --- | --- |
| #3-fraud-review | [3. Fraud review](../../../legacy/customer-spec.md#3-fraud-review) | The risk team places a customer under fraud review and lifts it; the other services ask whether a customer is blocked |
| #1-overview | [1. Overview](../../../legacy/customer-spec.md#1-overview) | What the service covers: context, not copied |
| #4-data | [4. Data](../../../legacy/customer-spec.md#4-data) | The table of the reviews and the kind of their identifiers: not copied |
| #6-ruling-history | [6. Ruling history](../../../legacy/customer-spec.md#6-ruling-history) | The risk committee's rulings of 2024-03-12 and 2024-09-02 on reviews: copied below, nothing else |

## Business rules

> **TO VALIDATE BEFORE THIS BOUNDED CONTEXT IS USED.** The rules marked "from source" were copied word for word from the source that states them. A person confirms each one, adds the rules the source does not state, then removes this banner. Until then the bounded context is not fit for design or implementation.

| # | Rule | Trigger | Expected effect | Source |
| --- | --- | --- | --- | --- |
| BR-1 | A review is lifted only by a member of the risk team. | — | — | from source: #3-fraud-review |
| BR-2 | Reviews older than one year are archived, not deleted. | — | — | from source: #6-ruling-history, 2024-09-02 |

Remove the banner once the section is filled and confirmed.

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

## Applicable area rules and invariants

| Entry | What it constrains here |
| ----- | ----------------------- |
| Candidate: "A customer under fraud review can neither pay nor be refunded." (from source: #6-ruling-history, 2024-03-12) | What being under fraud review means for paying and refunding. Also held by `docs/specs/billing/payment/spec.md` as BR-32: spans two areas; candidate for the admission test of the invariant register, until the owner validates it at its level |

## Open questions

1. Is "blocked" the same as "under fraud review"?
2. Does only a member of the risk team place a customer under fraud review? The source says "only" for lifting alone (BR-1).
3. Can a customer already under fraud review be placed under review again?
4. "Reviews older than one year" (BR-2): counted from the placing or from the lifting?
5. Does an archived review still block its customer?
6. How does a review name its customer: the identifier of the account of `customer`, or another?
7. Which services ask whether a customer is blocked, and through which contract?
