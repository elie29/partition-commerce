# Bounded contexts — billing

> **Source:** `docs/sources/billing.md`
>
> **Source version:** 1.0
>
> **Provenance:** 2026-10-19 · dc3486014a598118fa1b59468483e82314e63e7f
>
> **Scope:** whole source
>
> **Units covered:** 5 / 5
>
> **Decided:** 2026-10-19 · [architect] · proposal: option 1

The **Scope** line fixes the units analysed; the others appear in the `Coverage` table with the mention "out of scope". Change it only when a scope extension has been validated.

The **Provenance** line is the anchor of every later update: it fixes the exact state of the source this decomposition comes from. Never delete it; change it only together with the content it dates.

## Splitting criteria applied

The six criteria, evaluated unit by unit over the units in scope:

- **C1 — Functional grouping** (hint, outside the threshold): the unit carries a section of its own.
- **C2 — Root aggregate of its own** (mandatory): the business entity the unit acts on, as the source names it, differs from that of the others.
- **C3 — Business language of its own**: the vocabulary of the unit stands alone.
- **C4 — Life-cycle autonomy**: the unit can be used and evolve without coordination with the others.
- **C5 — Distinct external dependencies**: the unit relies on named third-party systems different from those of the others.
- **C6 — Shared data** (argues for grouping): the unit reads or writes the same entity as others.

Decision rules: **R1** a bounded context needs C2 and one of C3–C5; **R2** C1 never counts in the threshold; **R3** a unit failing R1 joins its nearest candidate; **R4** C6 never creates a bounded context and argues against a separation; **R5** a single unit neither justifies nor forbids a bounded context; **R6** two bounded contexts that no longer satisfy R1 against each other call for a merge.

## Bounded contexts retained

| Bounded context | Units | Criteria satisfied | Justification |
| --- | --- | --- | --- |
| invoice | REQ-1, REQ-2, REQ-3, REQ-4, REQ-5 | C6 (hint: C1 split in two sections) | Every unit acts on the invoice: "an invoice is issued" (REQ-1), "An issued invoice is never modified" (REQ-2), "The accountant can cancel an invoice" (REQ-3), "The customer pays an issued invoice by card" (REQ-4), "The customer can cancel a paid invoice" (REQ-5). |

## Decisions not to split

| Units kept together | Criteria failing | Reason |
| --- | --- | --- |
| REQ-1, REQ-2, REQ-3 / REQ-4, REQ-5 | C2, C3, C4 | R1: C2 fails, REQ-4 and REQ-5 act on the invoice, the source names no payment entity. C3 fails: "Cancelled" is defined once, for the invoice (Glossary). C4 fails: REQ-3 depends on whether a payment exists, REQ-5 changes the invoice's state. C5 (CardPay, REQ-4 and REQ-5) alone does not meet R1; C6 (the invoice, shared by all) argues against the separation (R4). |

## Alternatives rejected

| Split considered | Why rejected |
| --- | --- |
| Option 2 — `invoice` (REQ-1, REQ-2, REQ-3) and `payment` (REQ-4, REQ-5) | Breaks R1: C2 fails for REQ-4 and REQ-5 ("pays an issued invoice", "cancel a paid invoice"), C3 and C4 fail too; the invoice's state would be written from both sides (R4). Not chosen by [architect], 2026-10-19. |

## Kept against the criteria

None.

## Gaps detected

- REQ-1 against REQ-2: "An invoice is issued once per order" against "A correction is made by cancelling the invoice and issuing a new one". Open: a later version of the PRD settles it (PO, 2026-10-19).
- REQ-3 against REQ-5: "An invoice is cancelled only before any payment" against "The customer can cancel a paid invoice". Kept as written in 1.0, knowingly (PO, 2026-10-19); no rule reworded.
- REQ-2 against REQ-4 and REQ-5: "An issued invoice is never modified" against an invoice becoming paid, then "unpaid again". Kept as written in 1.0, knowingly (PO, 2026-10-19); no rule reworded.
- No couplings outside the scope, no orphan entity: the scope is the whole source.

## Coverage

| Unit | Grouping | Bounded context |
| --- | --- | --- |
| REQ-1 | Invoices | invoice |
| REQ-2 | Invoices | invoice |
| REQ-3 | Invoices | invoice |
| REQ-4 | Paying an invoice | invoice |
| REQ-5 | Paying an invoice | invoice |

## History

| Date | Version | Provenance | Nature | Detail |
| ---- | ------- | ---------- | ------ | ------ |
