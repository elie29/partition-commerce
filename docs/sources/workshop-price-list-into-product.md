# Boundary workshop — catalog: merge `price-list` into `product`

Held on 2026-10-20 by Claire Martin (architect), Paul Durand (product owner), Léa Petit (business analyst).
Material: prepared by partition-reshape on 2026-10-20 (domain events, words, budget, dependencies, rules, and the contract between `product` and `price-list`: none).

| Item | Decision |
| --- | --- |
| Move | Merge: `product` absorbs `price-list` |
| Why | a price is never managed apart from its product; every change of one is a change of the other |
| Integration | none left between the two since the move of `getProductPrice` |
| Carry-over | Léa Petit (business analyst) |
| Decided by | Claire Martin (architect), Paul Durand (product owner), Léa Petit (business analyst) |
