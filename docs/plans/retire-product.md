# Retire a product — execution plan

> **Status:** in progress
>
> **Resume at:** Task 2 — with `partition-hexagon`, have the architect rule on the form of the `product` hexagon, since BR-9 keeps a retired state that the thin form's reason excludes
>
> **Last updated:** 2026-10-20 · uncommitted

## Tasks

- [x] Task 1 — 2026-10-20 · uncommitted — The spec of `product` takes BR-9 (retirement)
- [ ] Task 2 — Rule on the form of the `product` hexagon (thin today) with `partition-hexagon`
- [ ] Task 3 — Challenge the spec (`partition-attack`), then design the retirement
- [ ] Task 4 — The retirement use case
- [ ] Task 5 — Close: gate, levels, README, C4

## Goal

The catalog manager retires a product: a product can be retired, and a retired product is never reactivated (BA, 2026-10-20). The retirement is a use case of the domain of `product`. It is not exposed through `docs/contracts/catalog.openapi.json`, which stays at 1.1.0. Reads and the neighbours `price-list` and `discount` are unchanged. The durable spec `docs/specs/catalog/product/spec.md` moves first, and then the code implements it. The commit before this plan's first task is `e07be03`.

## Task 1 — The spec of `product` takes BR-9 (retirement)

- Scope: `docs/specs/catalog/product/spec.md` (Business objective, Language, Business rules, Open questions), via `partition-spec`
- Verification: `grep -c '^| BR-' docs/specs/catalog/product/spec.md; grep -n '^| BR-9' docs/specs/catalog/product/spec.md`
- Expected: `9` BR rows; BR-9 reads "A product can be retired; a retired product is never reactivated.", with its trigger, its effect and its source
- Decisions that bind it: every answer of 2026-10-20 in `## Decisions`
- Done 2026-10-20 · uncommitted. Output: `9`, and line 60 is BR-9 with the rule's wording, its trigger, its effect and its source, as expected. Also written: "retire it" in the Business objective, the Language row "Retired", and Open questions "None (2026-10-20)". The BA's answer on the unknown product went into BR-9's effect.

## Task 2 — Rule on the form of the `product` hexagon

- Scope: `docs/partition.yml` (`product` hexagon: form, reason, layers, allowed), via `partition-hexagon`
- Verification: `grep -n -A3 'id: product$' docs/partition.yml`
- Expected: the form and reason that the architect rules on. The declared reason ("there is no state to protect") no longer holds once BR-9 keeps a retired state
- Decisions that bind it: 2026-10-20 · the retirement is a use case of the domain (architect)

## Task 3 — Challenge the spec, then design the retirement

- Scope: `docs/specs/catalog/product/spec.md` (findings), `docs/designs/retire-product.md`
- Verification: `test -f docs/designs/retire-product.md && grep -c 'BR-9' docs/designs/retire-product.md`
- Expected: the design exists and cites BR-9
- Decisions that bind it: every answer of 2026-10-20 in `## Decisions`

## Task 4 — The retirement use case

- Scope: `src/main/java/com/commerce/product/**`, `src/test/java/com/commerce/product/**`, the store's migration if the design calls for one
- Verification: set by the design (Task 3), written here before it is run
- Expected: set by the design (Task 3)
- Decisions that bind it: 2026-10-20 · no operation, `Product` unchanged, reads unchanged, neighbours not reached, retiring a retired product changes nothing, retiring an unknown product is refused as not found, the reference stays taken

## Task 5 — Close

- Scope: as `partition-plan` _Close_ says
- Verification: `partition-gate` on `e07be03`..working tree
- Expected: SHIP or SHIP WITH CONDITIONS
- Decisions that bind it: none

## Decisions

<!-- One line each: date · decision · reason · what was rejected. -->

- 2026-10-20 · BA states the rule "A product can be retired; a retired product is never reactivated." on top of contract 1.1.0 · a person's rule, the source of BR-9 · rejected: amending the contract to 1.2.0 first
- 2026-10-20 · PO: the feature that needs it is "the catalog manager retires a product" · the actor of the use case
- 2026-10-20 · Architect: the retirement is a use case of the domain, and the catalog contract stays at 1.1.0 with no new operation · this version does not expose it · rejected: a retire operation in the API
- 2026-10-20 · Architect: `Product` of the contract is unchanged, so a reader cannot see the retirement in this version · rejected: a status field on `Product`
- 2026-10-20 · BA: retiring a product that is already retired leaves it retired and changes nothing. No use case reactivates a product · this is the meaning of "never reactivated"
- 2026-10-20 · BA: `getProduct` and `listProducts` are unchanged for a retired product · reads are unchanged in this version
- 2026-10-20 · Architect: `price-list` and `discount` read nothing of `product`, so the retirement does not reach them in this version
- 2026-10-20 · BA: a retired product keeps its reference, which stays taken (BR-1)
- 2026-10-20 · BA, PO, architect: BR-9 and the "Retired" row are validated as shown, each owner for their part
- 2026-10-20 · BA: retiring a product that does not exist is refused. The use case reports the product not found, as BR-8 does for `getProduct`, and nothing changes · no open question remains

## Blockers

<!-- What is blocked · what unblocks it · who owns it. Delete the line when it clears. -->
