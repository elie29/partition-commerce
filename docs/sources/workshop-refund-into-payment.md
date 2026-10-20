# Boundary workshop — billing: merge `refund` into `payment`

Held on 2026-10-20 by Claire Martin (architect), Paul Durand (product owner), Léa Petit (business analyst).
Material: prepared by partition-reshape on 2026-10-20 (domain events, words, budget, dependencies, rules and the contract `PaymentRefunds` between `refund` and `payment`).

| Item | Decision |
| --- | --- |
| Move | Merge: `payment` absorbs `refund` |
| Why | a refund is always the reverse of a payment; the team changes both together, release after release. They share the amount paid and AR-3, which binds only these two: under R4 this sharing now prevails over C3 and C4, and reverses the decision that rejected this merge when PRD 1.1 was decomposed |
| Integration | `PaymentRefunds`, the one contract between the two, goes with the merge; `InvoicePayments`, from `payment` to `invoice`, stays |
| Carry-over | the business analyst carries the hand-written sections of `refund` into `payment` |
| Decided by | Claire Martin (architect), Paul Durand (product owner), Léa Petit (business analyst) |
