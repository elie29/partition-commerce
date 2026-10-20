# Merge: `payment` absorbs `refund` — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-20

## Tasks

- [x] Task 1 — 2026-10-20 — The specs: banners, `payment`'s derived sections, `bounded-contexts.md`, `payment`'s reason
- [x] Task 2 — 2026-10-20 — The design of the code merge, validated by the architect and the TL
- [x] Task 3 — 2026-10-20 — `refund`'s code moves into `payment`'s layers; `PaymentRefunds` and its adapter go
- [x] Task 4 — 2026-10-20 — `docs/partition.yml` and the architecture test: `refund` keeps its id and area only; the contract `refund` → `payment` goes
- [x] Task 5 — 2026-10-20 — Proof that it was a move
- [x] Task 6 — 2026-10-20 — After: C4, budget, the record
- [x] Task 7 — 2026-10-20 — Close: gate, levels, README, carry-over report

## Goal

Carry out the merge recorded in `docs/architecture/reshapes/2026-10-20-refund-into-payment.md` (workshop `docs/sources/workshop-refund-into-payment.md`, validated by name by the architect on 2026-10-20): `payment` absorbs `refund` (REQ-6). The code of `refund` comes behind the interfaces of `payment`; `PaymentRefunds`, the one contract between them, goes with its adapter; `InvoicePayments` stays; the HTTP contract `billing.openapi.json` 1.3.0 is unchanged. The specs move as `partition-reshape` `references/specs.md` says for a merge: no business rule is copied by the assistant; Léa Petit (BA) carries the hand-written sections over.

## Task 1 — The specs: banners, `payment`'s derived sections, `bounded-contexts.md`, `payment`'s reason

- Scope: `docs/specs/billing/payment/spec.md` (banner "absorbed `refund`"; header `Scope`; `Source parts owned` gains REQ-6; DEC-1 already there), `docs/specs/billing/refund/spec.md` (banner "merged into `payment`", frozen), `docs/specs/billing/bounded-contexts.md` (finding 4: `payment`'s retained line on REQ-4 to REQ-6, `refund`'s line to `Decisions not to split` under R4 against C2 and C4, the overturned lines to `History` word for word, `Coverage` REQ-6 → `payment`, the `Decided` line, a `History` line `merge`), `docs/partition.yml` (the reason of `payment`'s hexagon covers the refund's life cycle). Not touched: the hand-written sections of either spec, the area file (AR-3 comes down by a person)
- Verification: `grep -n "Status:\*\* absorbed\|Status:\*\* merged\|^> \*\*Scope" docs/specs/billing/payment/spec.md docs/specs/billing/refund/spec.md; grep -n "REQ-6" docs/specs/billing/bounded-contexts.md; grep -n "reason:" docs/partition.yml`
- Expected: one banner in each spec; `payment`'s Scope "REQ-4, REQ-5, REQ-6, DEC-1"; REQ-6 → `payment` in `Coverage`, a `Decisions not to split` line naming R4, C2 and C4, a `History` line `merge`; `payment`'s reason names the payment's and the refund's life cycles
- Decisions that bind it: 2026-10-20 · R4 prevails over C2 and C4 → `Decisions not to split` (architect); finding 4 taken as proposed (architect); no business rule copied by the assistant (`references/specs.md`)
- Result: `payment/spec.md:9` Scope "REQ-4, REQ-5, REQ-6, DEC-1", `:16` banner "absorbed `refund`"; `refund/spec.md:16` banner "merged into `payment`" (its header, frozen, keeps "REQ-6, DEC-1"); `bounded-contexts.md:37` `payment` on REQ-4 to REQ-6, `:45` `Decisions not to split` (R4, C2, C4), `:78` REQ-6 → `payment`, `:87` `History` `merge`, the overturned lines in it word for word; `partition.yml:50` `payment`'s reason names both life cycles (`:68`, `refund`'s, goes in Task 4) — as expected
- [x] 2026-10-20

## Task 2 — The design of the code merge, validated by the architect and the TL

- Scope: none written in code; the design below, proposed 2026-10-20, to be validated or changed:
  - D1. One hexagon `payment`: `refund`'s classes move into `com.commerce.payment.{domain,application,inbound,outbound}` (rule 3 of the project: one package per bounded context; one reason for `payment`'s form)
  - D2. The refund reads the invoice and its payment through `payment`'s own ports `PayableInvoices` and `Payments`, as `PaymentRefundsService` does today; gone: `PaymentRefunds`, `RefundableInvoice`, `PaidPayment`, `PaymentRefundsService` (`payment`), `RefundableInvoices`, `InvoiceToRefund`, `PaymentRefundsAdapter` (`refund`). Rejected: keeping `RefundableInvoices` implemented by an application class (no adapter left behind it)
  - D3. The copies collapse to `payment`'s: `FraudReview` and `ConfiguredFraudReview` (one bean, the list read once), `InvoiceIds`, `Text`, `CustomerId`; `InvalidRefund` → `InvalidPayment`; one `InvoiceNotFound` naming BR-21 and BR-27
  - D4. `Money`: one type, `payment`'s, gaining `ofRefund` (BR-26); its `toString` hidden for every amount, refunds' and payments' — question: BR-31 stays a rule of refunds only (TL, 2026-10-20); hiding a payment's amount in logs is more than BR-31 asks. Alternative: `Money` keeps the amount visible, and the refund types (`Refund`, `RefundView`) hide it, as they do today
  - D5. Two CardPay ports stay, `CardPay` (charge) and `CardPayRefunds` (send), each with its fake: interface segregation, one client each
  - D6. `pom.xml`: one generator execution `payment`, `FILTER=tag:Payment|Refund`, models `Invoice, Line, Money, PayInvoice, Refund, RefundInvoice`; the execution `refund` goes. `RefundErrors` stays scoped to `RefundInvoiceController`
  - D7. Tests: `RefundInvoiceTest` moves to `src/test/java/com/commerce/payment/`, its assertions unchanged; `RefundApiTest` and `RefundClient` move with it, or fold into `PaymentApiTest` and `PaymentClient` — to choose
- Verification: the architect's and the TL's answer, written in `## Decisions`
- Expected: each of D1 to D7 validated or changed, by name
- Result (architect, TL, 2026-10-20): D1, D2, D3, D5, D6 validated as written; D4 option B — `Money` shows the amount, the refund types hide it, as today (BR-31, refunds only); D7 option A — `RefundApiTest` and `RefundClient` move with `RefundInvoiceTest`, unchanged — as expected
- [x] 2026-10-20
- Decisions that bind it: the refund cap keeps its copy of the amount paid: no data moves, no migration (architect, 2026-10-20); BR-31 stays a rule of refunds only (TL, 2026-10-20)

## Task 3 — `refund`'s code moves into `payment`'s layers; `PaymentRefunds` and its adapter go

- Scope: `src/main/java/com/commerce/payment/**`, `src/main/java/com/commerce/refund/**` (emptied), `src/test/java/com/commerce/payment/**`, `src/test/java/com/commerce/refund/**` (emptied), `pom.xml` (D6). Not touched: migrations, `application.yaml`, `billing.openapi.json`, `invoice`
- Verification: `./mvnw clean verify`; `find src -path '*com/commerce/refund*'`; `grep -rn "PaymentRefunds\|RefundableInvoice\|PaidPayment\|InvoiceToRefund" src`
- Baseline, 2026-10-20 at `5709dae`: `./mvnw clean verify` → `Tests run: 183, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`
- Expected: `BUILD SUCCESS`, `Tests run: 183, Failures: 0`, `RefundInvoiceTest` in `com.commerce.payment` with its assertions unchanged; the find lists nothing; the grep finds nothing
- Done: 13 classes and the 3 tests moved with `git mv`; 15 deleted (`refund`'s copies of `FraudReview`, `InvoiceIds`, `InvoiceNotFound`, `CustomerId`, `InvalidRefund`, `Money`, `Text`, `ConfiguredFraudReview`; `InvoiceToRefund`, `RefundableInvoices`, `PaymentRefundsAdapter`; `payment`'s `PaymentRefunds`, `PaymentRefundsService`, `RefundableInvoice`, `PaidPayment`); `RefundInvoiceUseCase` reads `PayableInvoices.find`, `PayableInvoices.customerOf` and, while the invoice is `PAID`, `Payments.findOf`, as `PaymentRefundsService` did; `Money.ofRefund` (BR-26) on `payment`'s `Money`; `InvoiceNotFound` names BR-21, BR-27; `Refund` throws `InvalidPayment`; `RefundErrors`' comment names the tag `Refund`; `pom.xml` one execution `payment`, `FILTER=tag:Payment|Refund`
- Result: `Tests run: 183, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`; `RefundInvoiceTest` 14 tests in `com.commerce.payment`, its diff against `5709dae`: the package line and 5 imports, no assertion; generated `com.commerce.payment.inbound.api`: `ApiUtil.java PaymentApi.java RefundApi.java`, models `Invoice Line Money PayInvoice Refund RefundInvoice`, no `com/commerce/refund`; the find lists nothing; the grep finds `PaymentRefunds`, `RefundableInvoice`, `PaidPayment` only as strings in `ArchitectureTest.java:128-130`, Task 4's scope — as expected but for those strings, left to Task 4
- [x] 2026-10-20
- Decisions that bind it (2026-10-20): D1 one hexagon `payment`, `refund`'s classes in its four layers; D2 the refund reads through `PayableInvoices` and `Payments`, `PaymentRefunds`, `RefundableInvoice`, `PaidPayment`, `PaymentRefundsService`, `RefundableInvoices`, `InvoiceToRefund`, `PaymentRefundsAdapter` go; D3 `FraudReview`, `ConfiguredFraudReview` (one bean), `InvoiceIds`, `Text`, `CustomerId` collapse to `payment`'s, `InvalidRefund` → `InvalidPayment`, one `InvoiceNotFound` naming BR-21 and BR-27; D4 option B: `payment`'s `Money` shows the amount and gains `ofRefund` (BR-26), `Refund` and `RefundView` hide it as today; D5 `CardPay` and `CardPayRefunds` stay, each with its fake; D6 one generator execution `payment`, `FILTER=tag:Payment|Refund`; D7 option A: `RefundInvoiceTest`, `RefundApiTest`, `RefundClient` move to `com.commerce.payment` unchanged but for their package (architect, TL); no data moves (architect); BR-31 refunds only (TL)

## Task 4 — `docs/partition.yml` and the architecture test: `refund` keeps its id and area only; the contract `refund` → `payment` goes

- Scope: `docs/partition.yml` (`refund` keeps `id` and `area`, loses `code` and `hexagons`: it stays declared while `docs/specs/billing/refund/` exists), `src/test/java/com/commerce/ArchitectureTest.java` (the `Contract` `refund/refund` → `payment/payment` goes; the comment no longer cites `refund`'s spec)
- Verification: `./mvnw clean verify -Dtest=ArchitectureTest`, then `./mvnw clean verify`
- Expected: green, then `BUILD SUCCESS`, `Tests run: 183, Failures: 0`
- Decisions that bind it: the absorbed context stays declared with its area as long as its spec folder exists (`partition-reshape`)
- Result: `ArchitectureTest` `Tests run: 4, Failures: 0`, `BUILD SUCCESS`; `./mvnw clean verify` `Tests run: 183, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`; `grep -rn "PaymentRefunds\|RefundableInvoice\|PaidPayment\|InvoiceToRefund\|com\.commerce\.refund" src pom.xml`: nothing — as expected
- [x] 2026-10-20

## Task 5 — Proof that it was a move

- Scope: none written; the record's `Proof that it was a move`
- Verification: `git status --short -- . ':!src/main/java/com/commerce/payment' ':!src/test/java/com/commerce/payment' ':!src/main/java/com/commerce/refund' ':!src/test/java/com/commerce/refund'` against `5709dae`, then `git diff --quiet 5709dae -- <the callers and the contracts>`
- Expected (written 2026-10-20, before the run): outside `src/{main,test}/java/com/commerce/{payment,refund}`, only
  - the wiring: `pom.xml` (D6), `src/test/java/com/commerce/ArchitectureTest.java` (the contract gone), `docs/partition.yml`;
  - the documents of the reshape: `docs/plans/refund-into-payment.md`, `docs/architecture/reshapes/2026-10-20-refund-into-payment.md` and `docs/sources/workshop-refund-into-payment.md` (new), `docs/specs/billing/payment/spec.md`, `docs/specs/billing/refund/spec.md`, `docs/specs/billing/bounded-contexts.md`;
  - unchanged, the callers and the contracts: `src/main/java/com/commerce/invoice`, `src/test/java/com/commerce/invoice`, `docs/contracts/billing.openapi.json`, `src/main/resources` (no migration, `application.yaml`); inside `payment`, the pay untouched: `PayInvoiceController.java`, `PayInvoiceUseCase.java`, `PaymentErrors.java`, `InvoicePaymentsAdapter.java`, `PayInvoiceTest.java`, `PaymentApiTest.java`, `PaymentClient.java`, `PaymentMigrationTest.java`
- Result: HEAD `5709dae`; `git status --short` outside the merged code: `M docs/partition.yml`, `M docs/specs/billing/bounded-contexts.md`, `M docs/specs/billing/payment/spec.md`, `M docs/specs/billing/refund/spec.md`, `M pom.xml`, `M src/test/java/com/commerce/ArchitectureTest.java`, `?? docs/architecture/reshapes/2026-10-20-refund-into-payment.md`, `?? docs/plans/refund-into-payment.md`, `?? docs/sources/workshop-refund-into-payment.md`; `git diff --quiet 5709dae --` the callers and the contracts: `exit=0` — as expected: no caller changed
- [x] 2026-10-20
- Decisions that bind it: none

## Task 6 — After: C4, budget, the record

- Scope: `docs/architecture/model.yml` and its views (`partition-c4`: the component `refund` goes, its relations become `payment`'s, the dynamic view `refund-invoice` runs through `payment`), `docs/budget/` (`partition-budget`), the record's `After`
- Verification: `node .claude/skills/partition-c4/scripts/c4-drawio.mjs --check` then without `--check`; `node .claude/skills/partition-budget/scripts/budget.mjs`
- Done before the run: `model.yml` loses the component `refund` and `refund -> payment: PaymentRefunds`, `refund -> h2`, `refund -> cardpay`; `payment -> cardpay` reads "charges, reverses, sends refunds"; `refund-invoice` runs accountant → `payment` → `invoicing` (`InvoicePayments.find`, `.customerOf`, as `InvoicePaymentsAdapter.java:30,35` calls them) → `h2` → `cardpay`
- Expected (written 2026-10-20, before the run): `--check` no error, 10 elements (11 less `refund`), 9 relations (12 less 3), 7 views; `c4.drawio` regenerated, seven pages; budget: `payment` higher than its 5 960 of 2026-10-20 by the `REQ-6` row and the banner only (its hand-written sections not yet carried over), well under 32 000; `refund` still measured while its spec exists
- Result: `model.yml: 11 elements, 9 relations, 7 views, no error`; `c4.drawio` seven pages regenerated, every element placed automatically (no hand-set position kept); budget `payment` 6 057 (19 % ok, spec 2 898 → 2 995), `refund` 5 144 (16 % ok, scoped rules 263 → 0: its code path left `partition.yml`), plan read on resume 556 of 4 123; record `docs/budget/2026-10-20.md` replaced, the morning's figures quoted in it; the record's `After` ticked — as expected but for the element count: 11, not 10 — there were 12 before, the prediction miscounted; the model is right
- [x] 2026-10-20
- Decisions that bind it: none

## Task 7 — Close: gate, levels, README, carry-over report

- Scope: as `partition-plan` _Close_ says; and, as `references/specs.md` _At closing_ says: the hand-written sections of `refund` to carry into `payment` (Léa Petit, BA), AR-3 to bring down from `area.md` into `payment` and `refund` out of AR-1's `Binds` (by a person), then the folder `docs/specs/billing/refund/` to delete by hand and `refund`'s entry out of `docs/partition.yml`
- Verification: `partition-gate` on the changes of this plan
- Expected: SHIP or SHIP WITH CONDITIONS
- **Gate run 1, 2026-10-20 — SHIP WITH CONDITIONS, confidence High.** Target: `5709dae` to the working tree, 42 files (code of `payment` and `refund`, `pom.xml`, `ArchitectureTest.java`, the documents of the reshape); touches two bounded contexts and removes the contract between them (`PaymentRefunds`); the HTTP contract and `InvoicePayments` unchanged (Task 5)
  - Evidence: `./mvnw clean …jacoco prepare-agent verify …report` → `Tests run: 183, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`, the same 183 as at `5709dae` (run this session, before Task 3); `diff_coverage.py --range 5709dae` → `16/18` changed lines executed; the 15 files it lists "not measured" are the moved ones, whose changed lines are the package and imports only — each class is in `jacoco.xml`, executed; `detect_test_tampering.sh --range 5709dae` → nothing: no test deleted, skipped or weakened; the moved tests differ by their package and 5 imports. `./mvnw spring-boot:run` (port 18080, in-memory) → `Started CommerceApplication in 2.617 seconds`: one `FraudReview` bean, the list read once
  - Read: `RefundInvoiceUseCase` against the deleted `PaymentRefundsService` and `PaymentRefundsAdapter`: the same reads (`find`, `customerOf`, `findOf` while `PAID`) and the same order 400 → 404 → 403 → 409/400 → cap → send; `RefundErrors` and `PaymentErrors` each scoped to their controller (`assignableTypes`), so `InvalidRefund` → `InvalidPayment` changes no answer; BR-31's test (`RefundInvoiceTest.java:282-300`, nothing names the charge reference, the amount or a customer under review) green with `Money` showing its amount (D4)
  - Findings: [Low] `payment/domain/Refund.java:33,41` uncovered: the guards on the currency and on sending twice, unreachable through the use case (it checks the currency first and sends once); only their exception type changed. [Low] the hand-written sections of `payment`'s spec still name `refund` and `PaymentRefunds` (`Out of scope`, `Contracts with neighbours`), `area.md` keeps AR-3 and `refund` in AR-1's `Binds`: the merge's banners say so, the carry-over by Léa Petit (BA) settles it — reported at closing, not a condition
  - Skipped: migrations (none), performance (the same queries, one bean fewer), frontend, async (none)
  - Not verified: the look at the running application — the probe script (pay and refund by `curl`) was not approved in this session; the 14 tests of `RefundInvoiceTest` run through a real HTTP server
  - Deployment: no schema change, no configuration change; rollback by redeploying `5709dae`'s artifact is safe
  - Condition: G1 — the TL's look on this working tree: pay `200`; refund 500 EUR `201` `SENT`; refund USD `400`; refund over what remains `409`; refund `0` `400`; refund `nope` `404`; refund and pay of a customer under review `403`; refund of an invoice not paid `409`; the invoice stays `PAID`; no amount, charge reference or customer under review in the log
- Levels (2026-10-20): R4 over C2 and C4 is in `bounded-contexts.md` (`Decisions not to split`, Task 1); "reversed" for the cancel only, a refund "gives back", one word "Amount paid", BR-31 refunds only: hand-written sections of `payment`'s spec, carried by the BA (this task's carry-over); D1 to D7 stay in `## Decisions`, no level of their own. Area file: no entry added or changed by the assistant; AR-3 to come down by a person. Invariant register: no entry, no candidate
- README unaffected: API, configuration and commands unchanged; its line 28 still points to `docs/specs/billing/refund/spec.md` for the refund, true until the carry-over — realign then. C4: done in Task 6
- Carry-over report (`references/specs.md`, _At closing_): (1) the hand-written sections of `docs/specs/billing/refund/spec.md` into `docs/specs/billing/payment/spec.md`, with the decisions above, then the banner of `payment`'s spec removed — Léa Petit (BA); (2) AR-3 down from `docs/specs/billing/area.md` into `payment`'s `Business rules`, `refund` out of AR-1's `Binds` — a person; (3) the folder `docs/specs/billing/refund/` deleted by hand, then `refund` out of `docs/partition.yml`, README line 28 realigned
- G1 met (TL, 2026-10-20), the probe on the running app (port 18080, `commerce.fraud-review.customers=cust-review`): refund of an invoice not paid `409`; pay `200`; refund 500 EUR `201`, `SENT`; refund USD `400`; refund 751 over what remains `409`; refund 750, the rest, `201`; refund `0` `400`; refund `nope` `404`; refund and pay under review `403`; the invoice after the refunds `PAID`; the log holds no amount, no charge reference, no customer under review — as expected. The only condition of gate run 1 met; the closing wrote no file but this plan (no level, README unaffected, C4 in Task 6, before the gate), so run 1's verdict covers every change of the plan; the plan closes
- [x] 2026-10-20
- Decisions that bind it: the carry-over, by the BA: "reversed" stays the cancel within 24 hours, a refund "gives back" (architect); one word, "Amount paid" (BA); BR-31 refunds only unless the BA decides otherwise (TL); `payment`'s `Contracts with neighbours` loses its `refund` row and its `Out of scope` its refund line (all 2026-10-20)

## Decisions

- 2026-10-20 · Merge: `payment` absorbs `refund`; `PaymentRefunds` goes, `InvoicePayments` stays · workshop (Claire Martin, Paul Durand, Léa Petit), validated by name by the architect · rejected: `refund` kept apart (option 2 of PRD 1.1)
- 2026-10-20 · R4 prevails over C2 and C4: a line in `Decisions not to split`; the source is unchanged and R1 still holds · architect · rejected: (b) C4 failing and R6
- 2026-10-20 · "Reversed" stays the cancel within 24 hours; a refund "gives back" · architect, BA
- 2026-10-20 · One word, "Amount paid", the source's · BA · rejected: "amount charged"
- 2026-10-20 · `bounded-contexts.md`: the overturned lines to `History`, word for word; REQ-6 → `payment` · architect
- 2026-10-20 · BR-31 stays a rule of refunds only; the BA decides otherwise at the carry-over if needed · TL
- 2026-10-20 · The refund cap keeps its copy of the amount paid: no data moves in this change · architect · rejected: the cap reading `payment`'s table
- 2026-10-20 · `payment`'s reason in `docs/partition.yml` covers the refund's life cycle; form rich · architect
- 2026-10-20 · Léa Petit (BA) carries the hand-written sections of `refund` into `payment` · workshop
- 2026-10-20 · D1, D2, D3, D5, D6 as written in Task 2 · architect, TL · rejected for D2: `RefundableInvoices` implemented by an application class
- 2026-10-20 · D4: `Money` shows the amount; `Refund` and `RefundView` hide it, as today (BR-31, refunds only) · architect, TL · rejected: one `Money` hiding every amount
- 2026-10-20 · D7: `RefundApiTest` and `RefundClient` move with `RefundInvoiceTest`, unchanged · architect, TL · rejected: folding them into `PaymentApiTest` and `PaymentClient`

- 2026-10-20 · G1 · The TL's look on this working tree, with the assistant's probe script: every answer as expected, nothing to hide in the log · TL

## Blockers

None.
