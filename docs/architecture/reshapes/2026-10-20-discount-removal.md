# Reshape — removal: `discount`

> **Decided on:** 2026-10-20, by Claire Martin (architect), from proposal: option 1 (delta catalog 1.1.0 → 2.0.0, `partition-shape`); validated by name to be carried out on 2026-10-20
>
> **From:** `discount` (createDiscount, getDiscount), area `catalog` **To:** nothing; `product` (7 units) is the only bounded context of `catalog`
>
> **Raised by:** the delta of a source: `docs/contracts/catalog.openapi.json` 2.0.0 (`1e1a066`) removes `createDiscount`, `getDiscount`, the `Discount` schema and the tag `Discount`

## Why here

`discount` owns no unit of the contract any more: its two operations were removed, none moved. R1 cannot hold for a bounded context with no unit (C2). The criteria leave one split: `product` alone (option 1, criterion broken: none); option 2, `discount` kept with no unit, broke R1.

| Term | Meaning on this side | Meaning on the other side |
| ---- | -------------------- | ------------------------- |
| Discount | "A reduction of 1 to 90 percent, with an optional minimum order" (`discount/spec.md`, Language) | not used by `product` |

## Challenge

Proposal: no workshop result to challenge.

## Integration

| Crosses the boundary | Direction | Form (event, call, file) | Contract |
| -------------------- | --------- | ------------------------ | -------- |
| `createDiscount`, `getDiscount` (tag `Discount`) | caller → `discount` | HTTP | `docs/contracts/catalog.openapi.json` 1.1.0; removed by 2.0.0 |
| none between `discount` and `product` | — | — | `discount` reads no neighbour (`discount` BR-6); it shared the `Money` schema of the contract, not code |

## Rules

| Rule | Was in | Goes to |
| ---- | ------ | ------- |
| BR-1 to BR-10 | `discount/spec.md` | to archive or reassign, by a person; none is copied by the assistant |
| AR-1 to AR-4, bind `product` and `discount` | `docs/specs/catalog/area.md` | bind `product` alone after the removal: to bring down to `product/spec.md`, by a person, and to leave the area file |
| Area words Product, Price list, Currency, Amount, Identifier, Minor units, Refused | `docs/specs/catalog/area.md` | used by `product` alone after the removal: to bring down to `product/spec.md`, by a person |
| "A currency is a known ISO 4217 code" (`product` BR-13, `discount` BR-4) | both specs | `product` BR-13 only; the gap of `bounded-contexts.md` to settle with the removal |
| `Persistence` | no schema documents declared | unchanged |

## Preconditions

- [x] Every caller reaches it through an interface, or a merged half through its contract: not a fission, a move, a promotion nor a merge
- [x] One adapter implements that interface: not a fission, a move or a promotion
- [x] The contract is written and frozen: not a fission, a move or a promotion
- [x] For a fission, the new bounded context has its durable spec and its own language: not a fission
- [x] Each rule both sides need has one home: the owner's spec, the area file or the register: no rule passes to another side; BR-13 of `product` stands alone
- [x] For a merge, the person who carries the hand-written sections over: not a merge
- [x] For a removal, nothing calls it any more: its units are gone from the contract (`node .claude/skills/partition-shape/scripts/contract.mjs inventory docs/contracts/catalog.openapi.json` → tags `PriceList`, `Product` only, no `createDiscount`, `getDiscount`); in the code, `grep -rn "com\.commerce\.discount\|DiscountApi\|discount" src --include=*.java` outside `src/{main,test}/java/com/commerce/discount/` finds nothing (`exit=1`), 2026-10-20. What remains is its own wiring: the generator execution `discount` of `pom.xml:158-167`, its migration `V8__create_discount.sql`, its `docs/partition.yml` entry

## Proof that it was a move

Carried out by `docs/plans/discount-removal.md`.

Ruled on 2026-10-20: the code, its tests and its generator execution deleted (PO); a new migration `V10__drop_discount.sql` drops the table, `V8` not edited (architect).

- Verification: `git status --short -- . ':!src/main/java/com/commerce/discount' ':!src/test/java/com/commerce/discount'` against `771e40e`, the commit before the removal; then `git diff --stat 771e40e --` `src/main/java` (but `discount`), `ArchitectureTest.java`, `docs/contracts`, `V8`
- Expected: outside the removed code, only the wiring (`pom.xml`, `docs/partition.yml`, `V10__drop_discount.sql`) and the documents of the reshape; unchanged: the code and tests of `product`, `invoice`, `payment`, `ArchitectureTest.java`, the contracts, `V8`
- Real output (2026-10-20): `M docs/partition.yml`, `M docs/specs/catalog/bounded-contexts.md`, `M docs/specs/catalog/discount/spec.md`, `M pom.xml`, `??` the record, the plan, `V10__drop_discount.sql`; the `--stat`: empty. No caller changed. `./mvnw clean verify`: `Tests run: 160, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` — 183 at the last green run less the 23 tests of `discount`; at `771e40e` the build failed, contract 2.0.0 no longer generating `DiscountApi`

## After

- [x] C4 model updated and regenerated: the component `discount` and its relation `discount -> h2: SQL` gone; `model.yml` 9 elements, 7 relations, 7 views, no error; `c4.drawio` seven pages, 2026-10-20
- [x] `docs/partition.yml` keeps `discount`, with its area only, while `docs/specs/catalog/discount/` exists; its entry is taken out once a person has deleted the folder (Léa Petit, BA, after the hand moves), 2026-10-20
- [x] Budget measured again: `docs/budget/2026-10-20.md` — `discount` 5 190 (16 %) while its spec exists, `product` 8 169 (26 %); the removal was raised by the delta of a source, not a gauge
- P-4 of `docs/architecture/patterns-grid.md` still names `discount` among the contexts with one error handler each: raised to the architect, not rewritten (architect, 2026-10-20)
