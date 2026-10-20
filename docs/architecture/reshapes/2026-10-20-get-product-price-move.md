# Reshape — move: `getProductPrice` from `product` to `price-list`

> **Decided on:** 2026-10-20, by Claire Martin, architect, from proposal: option 1
>
> **From:** `product` **To:** `price-list`
>
> **Raised by:** the delta of a source, `docs/contracts/catalog.openapi.json` 1.0.0 → 1.1.0 (`260ac41`)

## Why here

Version 1.1.0 moves `getProductPrice` from `GET /products/{productId}/price` to `GET /price-lists/{priceListId}/product-prices/{productId}` and tags it `PriceList`: its terminal resource is a sub-resource of `price-lists`, the root aggregate of `price-list` (C2, R3 of `.claude/skills/partition-shape/references/criteria.md`). The decomposition of 2026-10-19 had put it in `product` because it was read under `/products`.

| Term | Meaning on this side | Meaning on the other side |
| ---- | -------------------- | ------------------------- |

No word changes meaning across the line: Price, Day and Applies are words of `../../specs/catalog/area.md`.

## Challenge

Proposal: no workshop result to challenge.

## Integration

| Crosses the boundary | Direction | Form (event, call, file) | Contract |
| -------------------- | --------- | ------------------------ | -------- |

None after the move: `price-list` answers from its own data, `product` no longer calls `PriceListPrices` ([architect], 2026-10-20). The in-process contract `PriceListPrices` (`product` → `price-list`) is removed; it had no other consumer.

## Rules

| Rule | Was in | Goes to |
| ---- | ------ | ------- |
| BR-9 (the price that applies on a day; one 404, `PRICE_NOT_FOUND`), without its product check ([architect], 2026-10-20) | `product` | `price-list`, carried by a person |
| BR-10 (`getProductPrice` checks its content first), with `priceListId` now in the path: AR-3 answers it 404, no longer 400 | `product` | `price-list`, carried by a person |

## Preconditions

- [x] Every caller reaches it through an interface, or a merged half through its contract: the HTTP callers through the contract, the generated `PriceListApi` since 1.1.0; `GetProductPriceUseCase` reaches `price-list` only through `ProductPrices` (`src/main/java/com/commerce/product/application/ProductPrices.java`)
- [x] One adapter implements that interface: `src/main/java/com/commerce/product/outbound/PriceListPricesAdapter.java`
- [x] The contract is written and frozen: `docs/contracts/catalog.openapi.json`, 1.1.0 (`260ac41`)
- [ ] For a fission, the new bounded context has its durable spec and its own language: not a fission
- [ ] Each rule both sides need has one home: the owner's spec, the area file or the register: none needed by both; BR-9 and BR-10 go to `price-list` alone
- [ ] For a merge, the person who carries the hand-written sections over: not a merge
- [ ] For a removal, nothing calls it any more: not a removal

## Proof that it was a move

- Verification: `git diff --stat e6f4d22 -- src`
- Expected: only files of `src/main/java/com/commerce/product/`, `src/main/java/com/commerce/pricelist/`, their tests and `src/test/java/com/commerce/ArchitectureTest.java`
- Real output: 19 files changed, 73 insertions, 433 deletions (2026-10-20), every one under `src/main/java/com/commerce/pricelist/`, `src/main/java/com/commerce/product/`, `src/test/java/com/commerce/pricelist/`, `src/test/java/com/commerce/product/` or `src/test/java/com/commerce/ArchitectureTest.java`; three files added, untracked: `src/main/java/com/commerce/pricelist/application/GetProductPriceUseCase.java`, `src/main/java/com/commerce/pricelist/application/PriceNotFound.java`, `src/test/java/com/commerce/pricelist/GetProductPriceTest.java`. No caller outside the two sides changed. `./mvnw -q clean test`: exit 0, 19 suites, no error, no failure

## After

- [x] C4 model updated and regenerated: the relation `product -> price-list: PriceListPrices, in process` removed from `docs/architecture/model.yml`, `docs/architecture/c4.drawio` regenerated (2026-10-20)
- [x] `docs/partition.yml` declares the new bounded context, with its area, or no longer declares the one that went: no change, both stay
- [x] Budget measured again: `docs/budget/2026-10-20.md`, `product` 5 804 → 5 394, `price-list` 5 988 → 6 457
