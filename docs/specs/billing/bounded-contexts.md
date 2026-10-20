# Bounded contexts — billing

> **Source:** `docs/sources/billing.md`
>
> **Source version:** 1.1
>
> **Provenance:** 2026-10-19 · 972aeb0000d5498ea82996110ca39366fadffb37
>
> **Scope:** whole source
>
> **Units covered:** 6 / 6
>
> **Decided:** 2026-10-20 · Claire Martin (architect), Paul Durand (PO), Léa Petit (BA) · workshop: `docs/sources/workshop-refund-into-payment.md` (merge: `payment` absorbs `refund`)

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
| invoice | REQ-1, REQ-2, REQ-3 | C6 (hint: C1, section "Invoices") | Every unit acts on the invoice: "an invoice is issued" (REQ-1), "An issued invoice is never modified" (REQ-2), "The accountant can cancel an invoice" (REQ-3). |
| payment | REQ-4, REQ-5, REQ-6 | C5 (CardPay: "The card is charged through CardPay" (REQ-4), "CardPay reverses the charge" (REQ-5), "A refund is sent to the card through CardPay" (REQ-6)); hint: C1, sections "Paying an invoice", "Refunds" | Fission of `invoice`, 2026-10-19: "Cancelled" is, of an unpaid invoice, "voided before any payment, at no fee", and of a paid invoice, "reversed within 24 hours of being paid" (Glossary); kept against C2, see below. Absorbed `refund` (REQ-6), 2026-10-20: R4, see `Decisions not to split`. |

## Decisions not to split

The decision of 2026-10-19 to keep REQ-1 to REQ-5 together was overturned by the fission of the same day: see `History`.

| Units kept together | Criteria | Rule and reason | Decided by | Date |
| --- | --- | --- | --- | --- |
| REQ-4, REQ-5 / REQ-6 | REQ-6 against REQ-4, REQ-5: C2 ("The accountant requests a refund of all or part of a paid invoice"), C4 ("A refund is sent to the card through CardPay, then settled by CardPay, days later, on its own schedule"); R1 still holds, the source is unchanged (1.1) | R4, merge of 2026-10-20 into `payment`: "a refund is always the reverse of a payment; the team changes both together, release after release. They share the amount paid and AR-3, which binds only these two"; the sharing of the amount paid and of AR-3 prevails over C2 and C4 | Claire Martin (architect), Paul Durand (PO), Léa Petit (BA); workshop `docs/sources/workshop-refund-into-payment.md` | 2026-10-20 |

## Alternatives rejected

Option 2 of the first decomposition, rejected on 2026-10-19, was taken by the fission of the same day; option 1 of version 1.1, rejected on 2026-10-19, was taken by the merge of 2026-10-20: see `History`.

## Kept against the criteria

| Point | Criterion | Decided by | Date | Reason |
| --- | --- | --- | --- | --- |
| `payment` (REQ-4, REQ-5) retained apart from `invoice` | C2 (R1): REQ-4 and REQ-5 act on the invoice ("pays an issued invoice", "cancel a paid invoice"); the source names no payment entity | Claire Martin (architect) | 2026-10-19 | "Cancelled" carries two meanings, and the PRD will name the payment; a payment is an entity of its own. Against R4: every write of the invoice's state stays inside `invoice`, behind `InvoicePayments` |

## Gaps detected

- REQ-1 against REQ-2: "An invoice is issued once per order" against "A correction is made by cancelling the invoice and issuing a new one". Open: a later version of the PRD settles it (PO, 2026-10-19). Resolved on 2026-10-19 (version 1.0.1): REQ-1 now reads "An order has at most one invoice that is not cancelled".
- REQ-3 against REQ-5: "An invoice is cancelled only before any payment" against "The customer can cancel a paid invoice". Kept as written in 1.0, knowingly (PO, 2026-10-19); no rule reworded.
- REQ-2 against REQ-4 and REQ-5: "An issued invoice is never modified" against an invoice becoming paid, then "unpaid again". Kept as written in 1.0, knowingly (PO, 2026-10-19); no rule reworded.
- No couplings outside the scope, no orphan entity: the scope is the whole source.
- REQ-4 and REQ-6 (1.1) both state "A customer under fraud review can neither pay nor be refunded.": copied once, as AR-3 of `area.md`, binding `payment` and `refund` (2026-10-19).
- REQ-5 against REQ-6 (1.1): a paid invoice cancelled within 24 hours "is then unpaid again", while REQ-6 refunds "all or part of a paid invoice"; whether a refunded payment can be cancelled, or a cancelled one refunded, is not stated. Resolved on 2026-10-19 (PO): the invoice stays `PAID` after a refund; a payment with a refund is not cancelled (BR-30 of `payment`), a cancelled payment is not refunded (BR-29 of `refund`).
- REQ-6 (1.1) caps refunds by the "amount paid" and refunds "all or part" of a paid invoice; neither the source nor `area.md` defines the amount paid, and `invoice`'s "Paid" means "its amount has been paid in full". Resolved on 2026-10-19 (PO, BA, architect): the amount paid is the amount charged held by `payment`, in its currency; one term (Language of `refund`).
- `payment` was kept against C2 on the ground that "the PRD will name the payment": version 1.1 names no payment entity yet (2026-10-19).
- The customer of AR-3 is the invoice's `customerId` (PO, 2026-10-19), yet `InvoicePayments`, frozen 2026-10-19, shows the invoice "as `getInvoice` shows it", without its customer. Resolved on 2026-10-19 (architect): `InvoicePayments` gains the `customerId`, agreed by `invoice` and `payment`; `refund` reads it through `PaymentRefunds` of `payment`.

## Coverage

| Unit | Grouping | Bounded context |
| --- | --- | --- |
| REQ-1 | Invoices | invoice |
| REQ-2 | Invoices | invoice |
| REQ-3 | Invoices | invoice |
| REQ-4 | Paying an invoice | payment |
| REQ-5 | Paying an invoice | payment |
| REQ-6 | Refunds | payment |

## History

| Date | Version | Provenance | Nature | Detail |
| ---- | ------- | ---------- | ------ | ------ |
| 2026-10-19 | 1.0 → 1.0.1 | dff137f | breaking | REQ-1: "An invoice is issued once per order" became "An order has at most one invoice that is not cancelled"; decomposition unchanged; BR-1 of `invoice` reported, not rewritten |
| 2026-10-19 | 1.0.1 | dff137f | fission | `invoice` becomes `invoice` (REQ-1, REQ-2, REQ-3) and `payment` (REQ-4, REQ-5), both in `billing`; workshop `docs/sources/workshop-invoice-fission.md`, record `docs/architecture/reshapes/2026-10-19-invoice-fission.md`; `payment` kept against C2. Overturned the same day's _Decisions not to split_ line "REQ-1, REQ-2, REQ-3 / REQ-4, REQ-5 — C2, C3, C4 — R1: C2 fails, REQ-4 and REQ-5 act on the invoice, the source names no payment entity. C3 fails: "Cancelled" is defined once, for the invoice (Glossary). C4 fails: REQ-3 depends on whether a payment exists, REQ-5 changes the invoice's state. C5 alone does not meet R1; C6 argues against the separation (R4)", and the _Alternatives rejected_ line "Option 2 — `invoice` (REQ-1, REQ-2, REQ-3) and `payment` (REQ-4, REQ-5) — Breaks R1: C2 fails for REQ-4 and REQ-5, C3 and C4 fail too; the invoice's state would be written from both sides (R4). Not chosen by [architect], 2026-10-19" |
| 2026-10-19 | 1.0.1 → 1.1 | 972aeb0 | breaking, additive | REQ-4 gains "A customer under fraud review can neither pay nor be refunded.", which REQ-6 states too: copied once, as AR-3 of `area.md`. REQ-6 added: new bounded context `refund` in `billing`, proposal option 2, [architect], validated by name; code path `src/main/java/com/commerce/refund/**` (TL). Option 1, REQ-6 in `payment` under R4, not chosen. Replaced `Decided` line: "2026-10-19 · [architect] · proposal: option 1; fission of `invoice` into `invoice` and `payment`: 2026-10-19 · Claire Martin (architect), Paul Durand (PO), Léa Petit (BA) · workshop: `docs/sources/workshop-invoice-fission.md`" |
| 2026-10-20 | 1.1 | 972aeb0 | merge | `refund` (REQ-6) absorbed by `payment`, both in `billing`; workshop `docs/sources/workshop-refund-into-payment.md`, record `docs/architecture/reshapes/2026-10-20-refund-into-payment.md`; no criterion lost: R1 still holds, R4 prevails over C2 and C4 (`Decisions not to split`); `PaymentRefunds` goes, `InvoicePayments` stays. Overturned the _Bounded contexts retained_ line "refund — REQ-6 — Against `payment`: C2 ("The accountant requests a refund of all or part of a paid invoice"), C4 ("A refund is sent to the card through CardPay, then settled by CardPay, days later, on its own schedule"). Against `invoice`: C2, C4, C5 (CardPay). Hint: C1, section "Refunds" — Version 1.1, 2026-10-19: "A refund lives on CardPay's schedule, days after the charge, and the accountant's operation stays apart from the customer's" ([architect]). Against R4: it shares with `payment` AR-3 (`area.md`), the amount paid and CardPay"; the _Alternatives rejected_ line "1 of version 1.1 (recommended) — `invoice` (REQ-1 to REQ-3), `payment` (REQ-4, REQ-5, REQ-6): REQ-6 kept with `payment` under R4, for the rule on fraud review, the amount paid and CardPay they share — "A refund lives on CardPay's schedule, days after the charge, and the accountant's operation stays apart from the customer's" — [architect] — 2026-10-19"; the _Gaps detected_ line "`payment` and `refund` share AR-3, the amount paid and CardPay (R4); kept apart by option 2 of version 1.1 (2026-10-19)."; the `payment` line of _Bounded contexts retained_ on REQ-4, REQ-5; _Coverage_ "REQ-6 — Refunds — refund". Replaced `Decided` line: "2026-10-19 · [architect] · proposal: option 2 of version 1.1 (new bounded context `refund`, REQ-6)" |
