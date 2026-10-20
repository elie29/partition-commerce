# customer

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

Hold the account of each customer: their name, their email and their billing address.

## Scope

**In scope:** the customer accounts (`#2-customer-accounts`).

**Out of scope:** the fraud review the risk team runs on customers: `fraud` (`#3-fraud-review`).

## Language

One meaning per word, no synonyms. A word that changes meaning in a neighbour marks the boundary.

| Term | Meaning in this bounded context |
| ---- | ------------------------------- |
| Account | What a customer has: a name, an email, a billing address (#2-customer-accounts). (BA, 2026-10-20) |
| Customer | Who has an account, and changes its address at any time (#2-customer-accounts). (BA, 2026-10-20) |

## Source parts owned

What this bounded context owns in its source. The source says what each part holds; this spec does not repeat it.

| Unit | Where | What it is for |
| --- | --- | --- |
| #2-customer-accounts | [2. Customer accounts](../../../legacy/customer-spec.md#2-customer-accounts) | The account of a customer, and the change of its address |
| #1-overview | [1. Overview](../../../legacy/customer-spec.md#1-overview) | What the service covers: context, not copied |
| #4-data | [4. Data](../../../legacy/customer-spec.md#4-data) | The table of the accounts and the kind of their identifiers: not copied |

## Business rules

| # | Rule | Trigger | Expected effect | Source |
| --- | --- | --- | --- | --- |
| BR-1 | The email of an account is unique. | — | — | from source: #2-customer-accounts; confirmed: BA, 2026-10-20 |
| BR-2 | A customer changes their address at any time. | — | — | from source: #2-customer-accounts; confirmed: BA, 2026-10-20 |

No other rule (BA, 2026-10-20).

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

None.

## Open questions

1. Is the customer that `#3-fraud-review` places under fraud review the customer who has an account in `#2-customer-accounts`, one entity or two?
2. Who creates an account, and on what?
3. Can the name or the email of an account change, and by whom?
4. "A customer changes their address at any time": is it the billing address, the only address the account holds?
5. Which bounded contexts or services read an account, and what do they read?
