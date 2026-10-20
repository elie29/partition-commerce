# Retire a product — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-20

## Tasks

- [x] Task 1 — 2026-10-20 · 90f98b5 — The spec of `product` takes BR-9 (retirement)
- [x] Task 2 — 2026-10-20 — Rule on the form of the `product` hexagon (thin today) with `partition-hexagon`
- [x] Task 3 — 2026-10-20 — Challenge the spec (`partition-attack`), then design the retirement
- [x] Task 4 — 2026-10-20 — Move the product code into the domain layer, no behaviour change
- [x] Task 5 — 2026-10-20 — The retirement use case
- [x] Task 6 — 2026-10-20 — Close: gate, levels, README, C4

## Goal

The catalog manager retires a product: a product can be retired, and a retired product is never reactivated (BA, 2026-10-20). The retirement is a use case of the domain of `product`. It is not exposed through `docs/contracts/catalog.openapi.json`, which stays at 1.1.0. Reads and the neighbours `price-list` and `discount` are unchanged. The durable spec `docs/specs/catalog/product/spec.md` moves first, and then the code implements it. The commit before this plan's first task is `e07be03`.

## Task 1 — The spec of `product` takes BR-9 (retirement)

- Scope: `docs/specs/catalog/product/spec.md` (Business objective, Language, Business rules, Open questions), via `partition-spec`
- Verification: `grep -c '^| BR-' docs/specs/catalog/product/spec.md; grep -n '^| BR-9' docs/specs/catalog/product/spec.md`
- Expected: `9` BR rows; BR-9 reads "A product can be retired; a retired product is never reactivated.", with its trigger, its effect and its source
- Decisions that bind it: every answer of 2026-10-20 in `## Decisions`
- Done 2026-10-20 · 90f98b5. Output: `9`, and line 60 is BR-9 with the rule's wording, its trigger, its effect and its source, as expected. Also written: "retire it" in the Business objective, the Language row "Retired", and Open questions "None (2026-10-20)". The BA's answer on the unknown product went into BR-9's effect.

## Task 2 — Rule on the form of the `product` hexagon

- Scope: `docs/partition.yml` (`product` hexagon: form, reason, layers, allowed), via `partition-hexagon`
- Verification: `grep -n -A3 'id: product$' docs/partition.yml`
- Expected: the form and reason that the architect rules on. The declared reason ("there is no state to protect") no longer holds once BR-9 keeps a retired state
- Decisions that bind it: 2026-10-20 · the retirement is a use case of the domain (architect)
- Reading of 2026-10-20, shown to the architect: BR-9 matches the rich row "a status that moves under conditions, with transitions that are forbidden" (retired, never reactivated), so the reading points to rich. Proposed entry: `form: rich`; reason "a product can be retired and is never reactivated (BR-9); the unique reference (BR-1) stays held by the store's unique constraint (architect, 2026-10-20)"; the layers and `allowed` of `price-list` (adds `domain: src/main/java/com/commerce/product/domain/**`, `application -> domain`, `outbound -> domain`). `ArchitectureTest` reads the map from `docs/partition.yml`, so no test change is needed. If rich, a task inserted before the use case moves the existing product code into the domain with no behaviour change, proven by the existing tests and `ArchitectureTest`.
- Done 2026-10-20. Claire Martin (architect) ruled rich, with this reason and this layer map. Written to `docs/partition.yml` with her name in the reason. Output: line 85 `form: rich`, line 86 the reason, line 88 `domain: src/main/java/com/commerce/product/domain/**`, as expected.

## Task 3 — Challenge the spec, then design the retirement

- Scope: `docs/specs/catalog/product/spec.md` (findings), `docs/designs/retire-product.md`
- Verification: `test -f docs/designs/retire-product.md && grep -c 'BR-9' docs/designs/retire-product.md`
- Expected: the design exists and cites BR-9
- Decisions that bind it: every answer of 2026-10-20 in `## Decisions`; `product` is rich (Claire Martin, 2026-10-20)
- In progress 2026-10-20. `partition-attack` is done: 4 findings were written to `Open questions` of the spec (1: no reachable trigger; 2: the state before retirement, at creation and for existing products; 3: what a retirement keeps; 4: "catalog manager" undefined). The design has not been started, and waits for the owners' answers.
- 2026-10-20. The answers to findings 1 to 4 were written into the spec: BR-9's trigger (only its tests reach it, nothing is checked about who), its effect (a new product and every existing one are active; the retired state only is kept), Language "Active" and "Catalog manager", and Open questions "None (2026-10-20)". The design `docs/designs/retire-product.md` is written. Verification output: `10`, as expected. The task stays open until the design is validated, along with its three questions (P-6, the `UUID` input, the test class).
- Done 2026-10-20. The design is validated as written (architect, TL). P-6 is written to `docs/architecture/patterns-grid.md`, and the design's questions are marked settled.

## Task 4 — Move the product code into the domain layer, no behaviour change

- Scope: `src/main/java/com/commerce/product/**` and its tests (imports only), as `docs/designs/retire-product.md` _Task 4_ names: `domain/Product`, `Blank` moved, the port takes `Product`, `ProductView.of`; no behaviour added or changed, no migration
- Verification: `./mvnw -q clean test` before the move (the baseline count), then after
- Expected: the same count of tests run before and after, 0 failures and 0 errors both times; `ArchitectureTest` passes with `product/domain` present
- Decisions that bind it: 2026-10-20 · `product` is rich, the layer map as written in `docs/partition.yml` (Claire Martin)
- Done 2026-10-20. Baseline before the move: 19 test classes, 173 tests, 0 failures, 0 errors, 0 skipped. After the move: 19 classes, 173 tests, 0 failures, 0 errors, 0 skipped, with the same count per class, and `ArchitectureTest` 4/4. As expected. Moved: `domain/Product` (record), `domain/Blank` (made public, application's copy deleted), `Products.add/find` on `Product`, `ProductView.of`, `CreateProductUseCase` and `GetProductUseCase` map to `ProductView`, and `JdbcProducts` maps rows to `Product`. `inbound` and the tests are unchanged. `git rm` staged the deletion of `application/Blank.java` in the index, which is a staging only and not a commit.

## Task 5 — The retirement use case

- Scope: `src/main/java/com/commerce/product/**`, `src/test/java/com/commerce/product/RetireProductTest.java`, `src/main/resources/db/migration/V9__add_product_status.sql`, as the design _Task 5_ and _Data_ say
- Verification: `./mvnw -q clean test`
- Expected: every case of the design's _Acceptance_ for `RetireProductTest` passes; `ArchitectureTest` passes; the tests from before still pass; 0 failures, 0 errors
- Decisions that bind it: 2026-10-20 · no operation, `Product` unchanged, reads unchanged, neighbours not reached, retiring a retired product changes nothing, retiring an unknown product is refused as not found, the reference stays taken; built on the domain layer of Task 4
- Done 2026-10-20. Red first: `RetireProductTest` did not compile without `ProductStatus` and `RetireProductUseCase`. Output: 20 test classes, 183 tests (173 + the 10 of `RetireProductTest`), 0 failures, 0 errors, 0 skipped; `RetireProductTest` 10/10, `ArchitectureTest` 4/4. As expected. Written: `domain/ProductStatus`; `Product` with `status`, `create`, `retire` and `isRetired`; `RetireProductUseCase.retire(UUID)`; `Products.changeStatus`; `JdbcProducts` (column `status`, `changeStatus`); `CreateProductUseCase` uses `Product.create`; `V9__add_product_status.sql`.

## Task 6 — Close

- Scope: as `partition-plan` _Close_ says
- Verification: `partition-gate` on `e07be03`..working tree
- Expected: SHIP or SHIP WITH CONDITIONS
- Decisions that bind it: none
- Gate 1, 2026-10-20, on `e07be03`..working tree, untracked included: **SHIP WITH CONDITIONS**, confidence Medium–High.
  - Evidence: `./mvnw -q clean test` exit 0, 20 classes, 183 tests, 0 failures, 0 errors, 0 skipped; `ArchitectureTest` 4/4; `RetireProductTest` 10/10. `detect_test_tampering.sh --range e07be03`: nothing deleted, skipped or weakened.
  - Blast radius: confined to `product`. No class outside `com.commerce.product` uses its port, its domain or its table (`git grep`). The contract is unchanged.
  - Spec: BR-9 holds as written (created active, retired once, a second retirement writes nothing, unknown product not found, reference taken, reads unchanged, nothing logged). BR-1 to BR-8 are unchanged, and the suite from before passes.
  - Migration V9: an `ADD COLUMN` with a default and `NOT NULL`, plus a `CHECK`. Every existing row is active. N−1: the previous artifact inserts without `status` and gets the default, and reads named columns, so it survives the new schema. Rollback by redeploy is safe.
  - Findings: none.
  - Not verified: diff coverage (the project has no JaCoCo; each changed line was mapped by reading to a test that runs it); performance of V9 on a large table (H2 in tests); a database other than H2.
  - Condition 1: the architect confirms the `ProductPage` deviation (see `## Decisions`), then the design's line is realigned · owner: architect.
- README: V9 changes an existing table, so the paragraph on migrations was realigned with `partition-readme` (2026-10-20), adding one sentence on V9. Its claims rest on V9 and on `RetireProductTest.a_product_stored_before_its_status_existed_is_active`.
- Gate 2, 2026-10-20, on `e07be03`..working tree, the README included: **SHIP WITH CONDITIONS**, confidence Medium–High. Since gate 1, only `README.md` and this plan changed (`git status`), and no code, so gate 1's test run stands for the code. The README sentence matches V9. Findings: none. Condition 1 still stands.
- Condition 1 met, 2026-10-20: Claire Martin (architect) confirmed that `ProductPage` keeps `ProductView`, and the line in `docs/designs/retire-product.md` _Task 4_ is realigned.
- Gate 3, 2026-10-20, on `e07be03`..working tree, the design correction included: **SHIP**, confidence Medium–High. Since gate 2, only the design and this plan changed (`git status`), and no code, so gate 1's run stands: 183 tests, 0 failures, 0 errors. The design's line now matches `ProductPage(List<ProductView> items, long totalElements)` and `JdbcProducts` (`grep`). Findings: none; no conditions. Not verified, as before: diff coverage (no JaCoCo), V9 on a large table, a database other than H2.
- C4: unaffected, because the component `product` and its relation `product -> h2` are unchanged.
- Levels: every statement established is in the spec of `product` (BR-9, Active, Retired, Catalog manager), the patterns grid (P-6), `docs/partition.yml` (rich) or the design. No `AR-` or `INV-` entry was added or changed, and no candidate was rejected: no statement reaches a second bounded context or area.

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
- 2026-10-20 · Claire Martin (architect): the `product` hexagon becomes rich, with reason "a product can be retired and is never reactivated (BR-9); the unique reference (BR-1) stays held by the store's unique constraint", and the layer map of `price-list` · BR-9 is a state with a forbidden transition, which the form table puts in a rich row · rejected: keeping it thin
- 2026-10-20 · a move task (Task 4) goes in before the use case, after the design · `partition-hexagon` requires the move to be proven with no behaviour change before any behaviour is built on the domain; the design names what moves
- 2026-10-20 · Architect, PO: in this version the retirement use case is reached by its tests only, with no operation, job, command line or message; an operation comes with a later version of the contract
- 2026-10-20 · BA: a new product is active, and every product already in the store is active. The word is "Active": a product not retired
- 2026-10-20 · BA: a retirement keeps only the retired state, neither when nor who · rejected: a retirement time, and who retired
- 2026-10-20 · PO: the catalog manager is the person who maintains the catalog. Nothing is checked about who retires a product in this version, because the demo runs without a login
- 2026-10-20 · Claire Martin (architect): P-6 records the same ruling as P-1, an enum held by the aggregate and one guarded method, citing P-1 · not reopened · rejected: State (GoF)
- 2026-10-20 · Claire Martin (architect): `RetireProductUseCase` takes a `UUID`; the inbound adapter will check the form when an operation arrives · rejected: a text checked by the 8-4-4-4-12 form
- 2026-10-20 · TL: the aggregate is tested inside `RetireProductTest` · one test class per use case (`backend-testing`)
- 2026-10-20 · architect, TL: the design `docs/designs/retire-product.md` is validated as written
- 2026-10-20 · implementer: `ProductPage` keeps `ProductView` items, built by `JdbcProducts` with `ProductView.of`, and does not hold `Product` · the validated design said both "`ProductPage` holds `Product`" and "`inbound` does not change", which cannot both hold because the controller reads its items as `ProductView`; "`inbound` does not change" was kept · rejected: a generic `ProductPage<T>`, which changes the controller · to be confirmed by the architect
- 2026-10-20 · Claire Martin (architect): confirmed, `ProductPage` keeps `ProductView` and `inbound` is unchanged; the design is corrected to say so

## Blockers

<!-- What is blocked · what unblocks it · who owns it. Delete the line when it clears. -->

