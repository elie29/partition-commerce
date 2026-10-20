# Reshape — merge: `payment` absorbs `refund`

> **Decided on:** 2026-10-20, by Claire Martin (architect), Paul Durand (product owner), Léa Petit (business analyst), from workshop: `docs/sources/workshop-refund-into-payment.md`; validated by name to be carried out on 2026-10-20 (architect)
>
> **From:** `payment` (REQ-4, REQ-5) and `refund` (REQ-6), both in area `billing` **To:** `payment` (REQ-4, REQ-5, REQ-6), area `billing`
>
> **Raised by:** the workshop of 2026-10-20; no budget record raised it (`docs/budget/2026-10-20.md` proposes no workshop)

## Why here

"A refund is always the reverse of a payment; the team changes both together, release after release. They share the amount paid and AR-3, which binds only these two" (workshop). BR-30 of `payment` (a payment with a refund is not cancelled) and BR-29 of `refund` (a cancelled payment is not refunded) each need a fact of the other side: apart, BR-30 would need a contract from `payment` to `refund`, the reverse of `PaymentRefunds`.

| Term | Meaning on this side (`payment`) | Meaning on the other side (`refund`) |
| ---- | -------------------------------- | ------------------------------------ |
| Amount charged / Amount paid | "the amount charged" of a payment (BR-24) | "Amount paid": the amount charged of its payment, held by `payment` (Language); one meaning, two words |
| Reversed | of a payment: cancelled, "reversed within 24 hours of the payment" (Language, Cancelled; REQ-5 "CardPay reverses the charge") | not used; the workshop calls a refund "the reverse of a payment" (finding 2) |
| Caller | the customer (REQ-4), anyone holding the identifier (BR-21) | the accountant (REQ-6) |

## Challenge

| Finding | Improvement proposed | Outcome |
| --- | --- | --- |
| 1. **The criterion R4 prevails over is C2 and C4, not C3 and C4; and R1 still holds.** The result: "under R4 this sharing now prevails over C3 and C4". Against `payment`, `refund` was retained on "C2 ("The accountant requests a refund of all or part of a paid invoice"), C4 ("A refund is sent to the card through CardPay, then settled by CardPay, days later, on its own schedule")" (`docs/specs/billing/bounded-contexts.md`, _Bounded contexts retained_); C3 was never satisfied. The source has not changed (1.1), so from the source alone R1 still holds and R6 does not call this merge. R4: "if it prevails, the decision goes to `Decisions not to split`". "Release after release": `refund` exists since `f81a3c3` (2026-10-19), one change, which changed `payment` too. | Either (a) R4 prevails over C2 and C4: a line in `Decisions not to split` of `bounded-contexts.md` naming C2 and C4, with the workshop's reason; or (b) the architect holds that C4 no longer holds (the team changes both together), so R1 fails (C2 alone; C3 no; C5 no, CardPay on both sides) and R6 calls the merge, the source's "on its own schedule" kept against in `Kept against the criteria`. | (a): R4 prevails over C2 and C4; a line goes into `Decisions not to split` of `bounded-contexts.md`. The source is unchanged, R1 still holds; the sharing of the amount paid and of AR-3 prevails (architect, 2026-10-20) |
| 2. **"Reverse" would carry two meanings in the merged `payment`.** The result: "a refund is always the reverse of a payment". In `payment`, Cancelled is "reversed within 24 hours of the payment"; REQ-5: "CardPay reverses the charge"; the source's Glossary: "Of a paid invoice: reversed within 24 hours of being paid". BR-29 and BR-30 keep a refund and a cancel apart. | The merged Language keeps "reversed" for the cancel (REQ-5) only; a refund "gives back" (`refund`'s Business objective: "Give back to the customer's card"). The result's text stays as validated. | Taken: "reversed" stays the cancel within 24 hours; a refund "gives back" (architect, 2026-10-20) |
| 3. **One meaning, two words: "amount paid" and "amount charged".** The result names "the amount paid" as shared. `refund`: "Amount paid … One term: no other word for it here (BA)"; `payment`: "the amount charged" (BR-24). One bounded context, one word per meaning. BR-25 copies "amount paid" from REQ-6: a rule copied from the source keeps the source's word. | The business analyst picks the one word of the merged Language; BR-25 keeps "amount paid" as the source says it. | Taken: "Amount paid", the source's word (BA, 2026-10-20) |
| 4. **`bounded-contexts.md` keeps the decision this merge overturns.** The result "reverses the decision that rejected this merge when PRD 1.1 was decomposed". The _Bounded contexts retained_ line of `refund`, the _Alternatives rejected_ line of option 1 of version 1.1, the _Gaps detected_ line "kept apart by option 2 of version 1.1", the `Decided` line and _Coverage_ REQ-6 → `refund` still state it. | Move the overturned lines to `History`, dated, word for word; REQ-6 → `payment` in `Coverage`; the `Decided` line names this workshop. | Taken as proposed (architect, 2026-10-20) |

## Integration

| Crosses the boundary | Direction | Form (event, call, file) | Contract |
| -------------------- | --------- | ------------------------ | -------- |
| For any invoice: whether it exists, its customer, its payment when paid | `refund` → `payment` | call, in process | `PaymentRefunds` (with `RefundableInvoice`, `PaidPayment`), removed by the merge with its adapter `PaymentRefundsAdapter`; the call becomes a call inside `payment` |
| Read an invoice, record a payment, record a payment cancelled | `payment` → `invoice` | call, in process | `InvoicePayments`, frozen 2026-10-19: stays |
| `payInvoice` (tag `Payment`), `refundInvoice` (tag `Refund`) | caller → `payment` | HTTP | `docs/contracts/billing.openapi.json` 1.3.0: unchanged |

Other consumers of `PaymentRefunds`: none — `grep -rn "PaymentRefunds\b" --include=*.java src` finds only `payment`'s application layer, `refund/outbound/PaymentRefundsAdapter.java` and the contract rule `ArchitectureTest.java:127`.

## Rules

| Rule | Was in | Goes to |
| ---- | ------ | ------- |
| BR-25, BR-26, BR-27, BR-28, BR-29, BR-31 | `refund` | `payment`; numbers unique in `billing` |
| BR-5, BR-6, BR-20, BR-21, BR-22, BR-24, BR-30 | `payment` | `payment` |
| AR-3 (fraud review), binds `payment` and `refund` only | `docs/specs/billing/area.md` | `payment`'s spec; leaves the area file |
| AR-1, binds `invoice` too | `docs/specs/billing/area.md` | stays; `refund` leaves its `Binds` |
| DEC-1, CardPay | both specs | `payment`, once |
| `Persistence` | both specs: no schema documents declared | unchanged |

## Questions left open by the workshop

Settled on 2026-10-20:

- BR-31 stays a rule of refunds only; the business analyst decides otherwise at the carry-over if needed (TL).
- `refund_cap` keeps its copy of the amount paid in this change (`JdbcRefunds.java:31`, V6): no data moves (architect).
- The reason of the form of `payment` in `docs/partition.yml` is rewritten to cover the refund's life cycle (architect).

## Preconditions

- [x] Every caller reaches it through an interface, or a merged half through its contract: `refund` reaches `payment` through `PaymentRefunds` only (`refund/outbound/PaymentRefundsAdapter.java:33`, enforced by `ArchitectureTest.java:127`); both halves are reached by HTTP through `billing.openapi.json` 1.3.0, 2026-10-20
- [x] One adapter implements that interface: not a fission, a move or a promotion
- [x] The contract is written and frozen: not a fission, a move or a promotion
- [x] For a fission, the new bounded context has its durable spec and its own language: not a fission
- [ ] Each rule both sides need has one home: the owner's spec, the area file or the register: AR-3, DEC-1, the amount paid (finding 3)
- [x] For a merge, the person who carries the hand-written sections over: Léa Petit (business analyst), 2026-10-20
- [x] For a removal, nothing calls it any more: not a removal

## Proof that it was a move

Carried out by `docs/plans/refund-into-payment.md` (Tasks 3 to 5).

- Verification: `git status --short -- . ':!src/main/java/com/commerce/payment' ':!src/test/java/com/commerce/payment' ':!src/main/java/com/commerce/refund' ':!src/test/java/com/commerce/refund'` against `5709dae`, the commit before the merge; then `git diff --quiet 5709dae --` the callers and the contracts
- Expected: outside the merged code, only the wiring (`pom.xml`, `ArchitectureTest.java`, `docs/partition.yml`) and the documents of the reshape; unchanged: `invoice`'s code and tests, `billing.openapi.json`, `src/main/resources`, and inside `payment` the pay (`PayInvoiceController`, `PayInvoiceUseCase`, `PaymentErrors`, `InvoicePaymentsAdapter`, `PayInvoiceTest`, `PaymentApiTest`, `PaymentClient`, `PaymentMigrationTest`)
- Real output (2026-10-20): `M docs/partition.yml`, `M docs/specs/billing/bounded-contexts.md`, `M docs/specs/billing/payment/spec.md`, `M docs/specs/billing/refund/spec.md`, `M pom.xml`, `M src/test/java/com/commerce/ArchitectureTest.java`, `??` the record, the plan, the workshop result; `git diff --quiet`: `exit=0`. No caller changed. `./mvnw clean verify`: `Tests run: 183, Failures: 0`, as at `5709dae`

## After

- [x] C4 model updated and regenerated: the component `refund` and its three relations gone, `payment -> cardpay: charges, reverses, sends refunds`, the dynamic view `refund-invoice` through `payment` and `invoicing`; `c4.drawio` seven pages, 2026-10-20
- [x] `docs/partition.yml` keeps `refund`, with its area only, while `docs/specs/billing/refund/` exists; its entry is taken out once a person has deleted the folder, 2026-10-20
- [x] Budget measured again: `docs/budget/2026-10-20.md` — `payment` 6 057 (19 %), `refund` 5 144 (16 %) while its spec exists; the merge was raised by a workshop, not a gauge
