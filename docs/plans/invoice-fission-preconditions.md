# Preconditions of the fission of `invoice` — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-19

## Tasks

- [x] Task 1 — 2026-10-19 — The owners validate the proposals the preconditions need (P1 to P5)
- [x] Task 2 — 2026-10-19 — One generated interface per tag of the contract
- [x] Task 3 — 2026-10-19 — `InvoicePayments` written and frozen, with its one adapter
- [x] Task 4 — 2026-10-19 — The area file of `billing`
- [x] Task 5 — 2026-10-19 — The spec of `payment`, the spec of `invoice` without what left, `bounded-contexts.md`, `payment` declared
- [x] Task 6 — 2026-10-19 — Close: gate, levels, README, C4

## Goal

Meet the preconditions of the fission of `invoice` into `invoice` and `payment`, recorded in `docs/architecture/reshapes/2026-10-19-invoice-fission.md` (workshop `docs/sources/workshop-invoice-fission.md`): every caller of the pay through an interface, `InvoicePayments` written, frozen and implemented by one adapter, the area file of `billing` holding the rules and words both sides need, and the durable spec of `payment` with its own language. The move itself is `docs/plans/invoice-fission.md`, which waits for this plan.

## Task 1 — The owners validate the proposals the preconditions need (P1 to P5)

- Scope: none of the repository; answers in the conversation, written into `## Decisions`
- Verification: `grep -c "^- 2026-10-19 · P[1-5]" docs/plans/invoice-fission-preconditions.md`
- Expected: 5
- Decisions that bind it: the answers to the challenge (`## Decisions`, lines "Challenge")
- Answers received 2026-10-19 (architect, PO, BA), one line each in `## Decisions`. Result of the grep: `5` — as expected
- [x] 2026-10-19

## Task 2 — One generated interface per tag of the contract

- Scope: `pom.xml` (the `openapi-generator-maven-plugin` execution: `useTags`), `src/main/java/com/commerce/invoice/inbound/InvoiceController.java`, a new `src/main/java/com/commerce/invoice/inbound/PayInvoiceController.java` implementing the interface of the tag `Payment`; tests unchanged
- Verification: `./mvnw -q verify; ls target/generated-sources/openapi/src/main/java/com/commerce/invoice/inbound/api/`
- Expected: the build passes with every test green; the folder lists `InvoiceApi.java`, `PaymentApi.java`, `ApiUtil.java` and no `InvoicesApi.java`; `InvoiceController` no longer depends on `PayInvoiceUseCase`
- Decisions that bind it: one interface per tag, in this plan (TL, 2026-10-19); the packages stay `com.commerce.invoice.inbound.api` until the move
- Done: `pom.xml` gains `<useTags>true</useTags>`; `InvoiceController` implements `InvoiceApi` (issue, read, cancel); `PayInvoiceController` implements `PaymentApi`; the mapping to the contract's `Invoice` moves to `InvoiceModels`, shared by both; `InvoiceErrors` covers both controllers (`assignableTypes`)
- Result (`./mvnw clean verify`, the first run without `clean` left a stale `InvoicesApi.java`): `mvn exit=0`, `Tests run: 49, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`; folder: `ApiUtil.java InvoiceApi.java PaymentApi.java model`; `grep -c PayInvoiceUseCase InvoiceController.java`: `0` — as expected. The ERROR lines of the log are the 500 scenarios of `PayInvoiceTest`
- [x] 2026-10-19

## Task 3 — `InvoicePayments` written and frozen, with its one adapter

- Scope: `src/main/java/com/commerce/invoice/application/InvoicePayments.java`, its one implementation in `src/main/java/com/commerce/invoice/application/`, `InvoiceNotIssued`, a test of it under `src/test/java/com/commerce/invoice/`; the record quotes the signatures. `PayInvoiceUseCase` is not rewired here: the charge reference has no home until V4 (move plan)
- Verification: `./mvnw -q verify; grep -n "InvoiceView\|void\|default" src/main/java/com/commerce/invoice/application/InvoicePayments.java`
- Expected: the build passes; the three operations of P1 exactly as validated; the record's `Integration` quotes them and calls them frozen
- Decisions that bind it: P1; the frozen text is the Java interface in `invoice`'s application layer (architect); recording a cancelled payment is a `default` method that throws until REQ-5 (architect); every write of the invoice's state stays in `invoice` (architect)
- Done: `InvoicePayments`, `InvoiceNotIssued`, `InvoicePaymentsService` (the one adapter, GoF Adapter over `Invoices`), `InvoicePaymentsTest` (6 tests), written test first: red `cannot find symbol` before the code
- First green run failed 1 of 6: a second `recordPayment` inside the rolled-back transaction read the invoice `PAID` with no charge reference, which `Invoice` refuses (`Invoice.java:33-35`) until the move takes the charge reference out of it. The assertion was dropped here; the refusal on a paid invoice stays covered by `refuses_to_record_a_payment_on_a_paid_invoice`; it comes back in Task 4 of `docs/plans/invoice-fission.md`
- Result (`./mvnw clean verify`): `mvn exit=0`, `Tests run: 55, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`; grep: line 14 `Optional<InvoiceView> find`, line 23 `InvoiceView recordPayment`, line 31 `default InvoiceView recordPaymentCancelled` — as expected; the record quotes them
- [x] 2026-10-19

## Task 4 — The area file of `billing`

- Scope: `docs/specs/billing/area.md` (path as `partition-spec` gives it), `docs/specs/billing/invoice/spec.md` (BR-12, the words that leave), `.apm/instructions/backend-database.instructions.md` line 7 through `partition-rules`, then `apm install` regenerates `.claude/rules/backend-database.md`
- Verification: `grep -n "^| AR-\|^| Amount\|^| Minor units\|^| Currency\|^| Refused\|^| Payment\|^| Blank" docs/specs/billing/area.md; grep -c "^| BR-12\|^| Blank\|^| Amount" docs/specs/billing/invoice/spec.md; grep -c "minor units" .claude/rules/backend-database.md`
- Expected: two area rules (the amount line word for word, the bound of BR-12) and six words in the area file; `0` in the spec of `invoice`; `0` in the rule file, which keeps the column convention
- Decisions that bind it: findings 4, 5, 6 (architect, BA); P2, P3, P4
- Draft shown 2026-10-19, to validate (`## Blockers`). `docs/specs/billing/area.md`, from `partition-spec`'s `assets/area.md`, banners removed once validated (every entry is a person's):
  - `## Bounded contexts`: `invoice` → `invoice/spec.md`; `payment` → `payment/spec.md` (written by Task 5)
  - `## Language`:
    - `| Amount | A sum of money in one currency, stored in minor units; of an invoice, the sum of the prices of its lines; of a payment, the amount charged (BA, 2026-10-19). |`
    - `| Blank | Empty or only white space; a non-breaking space (U+00A0, U+2007, U+202F) is white space (PO, BA, 2026-10-19). |`
    - `| Character | A Unicode code point: an emoji is one character (architect, 2026-10-19). |` — point (b)
    - `| Currency | A three-letter ISO 4217 code a context accepts (BR-9) (BA, 2026-10-19). |`
    - `| Minor units | The smallest unit of a currency, counted as a whole number (BA, 2026-10-19). |`
    - `| Payment | A card charge CardPay accepted, against one invoice; a declined charge is no payment (BA, 2026-10-19). |`
    - `| Refused | What a bounded context of `billing` answers to a request one of its rules does not accept; nothing changes. Not Declined: CardPay declines a card (BA, 2026-10-19). |`
  - `## Area rules` (`# | Rule | Trigger | Expected effect | Binds | Source`):
    - `| AR-1 | A bounded text is non-blank and holds at most 255 characters. | A text a rule of the area bounds by AR-1: a label, `orderId`, `customerId` (`invoice`); a card token, a charge reference (`payment`) | A text blank or longer is refused, as the rule that bounds it says | invoice, payment | architect, 2026-10-19 (the bound of BR-12) |`
    - `| AR-2 | Every amount is stored in minor units, with its currency: a `BIGINT` and a three-letter ISO 4217 code, never a decimal. | An amount stored: the prices and the amount of an invoice (`invoice`), the amount charged (`payment`) | Stored as a whole number of minor units and the code of its currency | invoice, payment | architect, 2026-10-19 (from `.apm/instructions/backend-database.instructions.md`) |`
  - `docs/specs/billing/invoice/spec.md`: Language loses Amount, Blank, Character, Payment (the last says "held on the invoice, not an entity of its own", no longer true); BR-12 becomes `A label is bounded by AR-1.`, trigger, effect and source unchanged — point (a); BR-11, BR-13, BR-22 unchanged; `Applicable area rules and invariants`: `AR-1, AR-2 of ../area.md`
  - `.apm/instructions/backend-database.instructions.md`: line 7 (the amounts) removed; lines 6 and 8 kept; then `apm install`
- Validated 2026-10-19 (architect: AR-1, AR-2, (a), (b); BA: the seven words). Written: `docs/specs/billing/area.md` as drafted; `invoice`'s spec as drafted; the line removed from `.apm/instructions/backend-database.instructions.md` (`partition-rules`)
- Result: area file lines 18–24 the seven words, lines 30–31 AR-1 and AR-2; `invoice`'s spec `0`; rule source `0`; deployed `.claude/rules/backend-database.md` `1` — not as expected: `apm install` was not approved in this session
- `apm install` run by the TL, 2026-10-19. Rerun: `.claude/rules/backend-database.md:0`, `.apm/instructions/backend-database.instructions.md:0` — as expected
- [x] 2026-10-19

## Task 5 — The spec of `payment`, the spec of `invoice` without what left, `bounded-contexts.md`, `payment` declared

- Scope: `docs/partition.yml` (`payment`, area `billing`, code `src/main/java/com/commerce/payment/**`, hexagon rich with its layer map, through `partition-hexagon`), `docs/specs/billing/payment/spec.md` (new, through `partition-spec`), `docs/specs/billing/invoice/spec.md`, `docs/specs/billing/bounded-contexts.md` (retained, coverage, `Decided`, `Kept against the criteria` for C2, `History`)
- Verification: `grep -n "^| BR-" docs/specs/billing/payment/spec.md | cut -c1-12; grep -n "^| BR-5 \|^| BR-6 \|^| BR-20\|^| BR-21\|^| BR-22" docs/specs/billing/invoice/spec.md; grep -n "payment" docs/specs/billing/bounded-contexts.md | head`
- Expected: `payment` holds BR-5, BR-6, BR-20, BR-21, BR-22 and the pay part of BR-3 under its P2 number; `invoice` holds none of them; `bounded-contexts.md` retains `payment` with REQ-4 and REQ-5, keeps it against C2, and moves the 2026-10-19 rejection to `History`
- Decisions that bind it: findings 1, 2, 3, 7, 10; the language of `payment` (BA); P2
- Done: `docs/partition.yml` declares `payment` (area `billing`, rich, the four layers of `invoice`; `partition-hexagon`); `docs/specs/billing/payment/spec.md` written with no banner (PO, BA); `invoice`'s spec loses REQ-4, REQ-5, DEC-1, BR-5, BR-6, BR-20, BR-21, BR-22, CardPay, `payInvoice`, the words Card token, Charge reference, Declined, Time of the payment, and the questions and attacks that moved; gains BR-23, BR-3 widened (back to `ISSUED`, not a modification; PO), `Cancelled` voided only (PO), the contract row of `payment`, an open question (BR-4 against a reversed invoice, REQ-3 holds); `bounded-contexts.md`: `payment` retained, coverage, `Decided`, `Kept against the criteria` (C2), the overturned lines to `History`
- Wordings written without being shown first, for review: `payment`'s objective; `Declined` "Not Refused (`../area.md`)"; `Cancelled` of `payment` and of `invoice` (boundary sentence); BR-5 of `payment` (the record now through BR-23 and BR-24); BR-23; BR-24; BR-3 of `invoice`; the new open question of `invoice`; attack finding 6 split (the BR-19 part stays in `invoice`)
- Result: `payment` BR-5, BR-6, BR-20, BR-21, BR-22, BR-24; `invoice` none of them (empty); `bounded-contexts.md` lines 37 (retained), 51 (kept against C2), 67–68 (coverage), 75 (`History`) — as expected. `./mvnw clean verify` with `payment` declared: `mvn exit=0`, `Tests run: 55, Failures: 0`, `BUILD SUCCESS`
- [x] 2026-10-19

## Task 6 — Close: gate, levels, README, C4

- Scope: as `partition-plan` _Close_ says
- Verification: `partition-gate` on the changes of this plan
- Expected: SHIP or SHIP WITH CONDITIONS
- Decisions that bind it: none
- **Gate, 2026-10-19 — SHIP WITH CONDITIONS, confidence Medium-High.** Target: 7d83e28 to the working tree, 9 tracked files (+59/−96) and 13 untracked. One bounded context's code (`invoice`); the specs of `invoice`, `payment` and the area file. HTTP contract unchanged: same paths, statuses and bodies, `payInvoice` now served by `PayInvoiceController` through `PaymentApi`
  - Evidence: `./mvnw clean org.jacoco:jacoco-maven-plugin:0.8.13:prepare-agent verify …:report` → `Tests run: 55, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` (`PayInvoiceTest` 14/14 through the new controller); `detect_test_tampering.sh --range 7d83e28` → none; `diff_coverage.py` tracked lines 1/1 (`InvoiceErrors.java` changes an annotation only); untracked classes from `jacoco.xml`: `InvoiceModels` 8/8, `PayInvoiceController` 4/4, `InvoiceController` 14/14, `InvoiceNotIssued` 2/2, `InvoicePayments` 1/1, `InvoicePaymentsService` 11/12, missed line 35; `./mvnw spring-boot:run` (port 18080, in-memory database) → `Started CommerceApplication in 2.165 seconds`; `git grep InvoicesApi` → none
  - Findings: [Low] `InvoicePaymentsService.java:35`, the refusal when a simultaneous pay or cancel commits between the read and the conditional update, runs in no test. [Low] `InvoicePaymentsService.java:34` writes `PAID` with no charge reference; until the move `Invoice` refuses to read that row back (`Invoice.java:33-35`), so any read, cancel or pay of it would answer 500. No production caller today (grep: only the old `Invoices.recordPayment`). [Low] `docs/partition.yml`: `invoice`'s form reason still cites BR-3 to BR-6. [Info] the specs are ahead of the code: the pay's rules are `payment`'s, its code is still in `com.commerce.invoice` — the move plan
  - Skipped: migrations (none), concurrency beyond the line above, performance (no new query in a hot path), security (no new endpoint, no auth change; the card token still goes only to CardPay), frontend, async (none)
  - Not verified: the look at the running application by me — `curl` was not permitted in this session; the TL's look of 2026-10-19 stands for it (issue `201`; pay `200` `PAID`; pay again `409`; cancel the paid invoice `409`; pay `nope` `404`; blank token `400`; pay a cancelled invoice `409`; no card token in the log). No run of the suite at the base 7d83e28
  - Conditions: C1 — `InvoicePayments.recordPayment` has no production caller until `Invoice` no longer requires a charge reference on `PAID`: met today (grep above), binds Tasks 2 and 4 of `docs/plans/invoice-fission.md`. C2 — a test covers `InvoicePaymentsService.java:35`: it cannot be written before the move (the concurrent reader hits the unreadable row), carried to Task 4 of `docs/plans/invoice-fission.md`. C3 — `invoice`'s form reason rewritten: Task 7 of `docs/plans/invoice-fission.md`
- Close: statements of this plan and their level — the workshop's boundary, the pay's rules, BR-23, BR-24, the words: in the specs of `invoice` and `payment`; AR-1, AR-2 and seven words: area file, validated by the architect and the BA, 2026-10-19; the column convention: rule file, unchanged; the design choices (one adapter, transaction bean, generator per tag): `## Decisions` of both plans, no level of their own. Area entries added: AR-1, AR-2; changed: none. Invariant register: no entry, no candidate — nothing spans two areas or layers. README unaffected: no command, option, configuration key or API changed. C4: no element moved yet; the move plan's Task 7 updates the model once `payment` has code. No file written by the close beyond this plan, so no second gate run
- [x] 2026-10-19

## Decisions

- 2026-10-19 · Challenge 1 · Fission kept against C2 (R1): "Cancelled" carries two meanings and the PRD will name the payment; a payment is an entity of its own · architect · rejected: amend the PRD first
- 2026-10-19 · Challenge 2 · Every write of the invoice's state stays inside `invoice`, behind `InvoicePayments`; the 2026-10-19 rejection of the split moves to `History` · architect
- 2026-10-19 · Challenge 3 · `invoice`'s "cancelled" is voided only; an invoice whose payment is cancelled is not cancelled for BR-1; REQ-3 and REQ-5 stay as written until the PRD names the payment · PO
- 2026-10-19 · Challenge 4 · The minor-units line goes to the area file word for word: both contexts store amounts (`payment` the amount charged); the rule file keeps the column convention only · architect · rejected: leave it a rule of code
- 2026-10-19 · Challenge 5 · BR-12's bound moves to the area file, "Blank" with it · architect
- 2026-10-19 · Challenge 6 · "Refused" and "Payment" are shared words of the area file; "Minor units", "Amount" and "Currency" are business words · BA, architect
- 2026-10-19 · Challenge 7 · The rule-by-rule split of the record is confirmed; a rule keeps its number when it moves; the dated attack on paying moves to `payment` with the rules it attacked · PO, BA
- 2026-10-19 · Challenge 8 · The time and the charge reference go to `payment`'s own table (V4), written in the same transaction as `recordPayment`; the invoice row's columns go with the move; recording a cancelled payment is a `default` method that throws until REQ-5 · architect
- 2026-10-19 · Challenge 9 · One generated interface per tag, in this plan · TL; `payment` answers the contract's `Invoice` schema · architect
- 2026-10-19 · Challenge 10 · `payment` is rich; BR-6's missing trigger and effect are open questions of its spec · architect
- 2026-10-19 · A paid invoice whose payment is cancelled can be paid again ("unpaid again" is issued again) and is not cancelled by the accountant (REQ-3 holds) · PO
- 2026-10-19 · Recording a cancelled payment is not a modification (BR-3): it changes the status · PO; its home is `invoice` · architect
- 2026-10-19 · `payment` stores the amount charged · architect
- 2026-10-19 · The time of a payment is the moment the record of `payment` commits · architect
- 2026-10-19 · `InvoicePayments` is written and frozen first, a precondition with its own plan; its frozen text is the Java interface in `invoice`'s application layer; the record quotes its signatures · architect
- 2026-10-19 · Language of `payment`: Payment — a card charge CardPay accepted, against one invoice; a declined charge is no payment. Cancelled — reversed within 24 hours of the payment · BA
- 2026-10-19 · The fission is validated by name to be carried out · owner
- 2026-10-19 · P1 · `InvoicePayments` as proposed: `Optional<InvoiceView> find(UUID)`; `InvoiceView recordPayment(UUID)` (`ISSUED` → `PAID`, nothing else, in the caller's transaction; `InvoiceNotIssued` when no longer `ISSUED`); `default InvoiceView recordPaymentCancelled(UUID)` throwing until REQ-5; `InvoiceView` and `LineData` frozen with it · architect
- 2026-10-19 · P2 · BR-3 stays in `invoice`, BR-5 moves to `payment`; the conditional record of BR-5 becomes BR-23 of `invoice`, the pay part of BR-3 BR-24 of `payment` · PO, BA · rejected: one number in two specs
- 2026-10-19 · P3 · Area words: Amount — a sum of money in one currency, stored in minor units; of an invoice, the sum of the prices of its lines; of a payment, the amount charged. Currency — a three-letter ISO 4217 code a context accepts (BR-9). Minor units — the smallest unit of a currency, counted as a whole number · BA
- 2026-10-19 · P4 · "Refused — what a bounded context of `billing` answers to a request one of its rules does not accept; nothing changes. Not Declined: CardPay declines a card." · BA; AR-1 the bound "non-blank, at most 255 characters" (was BR-12), AR-2 the amounts in minor units · architect · rejected: the reverse numbering proposed
- 2026-10-19 · P5 · "Paid" stays in `invoice` · BA; `payment` has the four layers of `invoice` under `src/main/java/com/commerce/payment/`, with the same allowed directions · architect
- 2026-10-19 · Area file: AR-1 and AR-2 as worded; BR-12 stays in `invoice` as "A label is bounded by AR-1."; "Character" moves to the area file with "Blank" · architect; the seven words as written · BA
- 2026-10-19 · The specs written for the move (rules, triggers, effects, the objective of each) are confirmed; the banner of `payment`'s spec goes · PO, BA
- 2026-10-19 · The design of a precondition task: a key of its own; no foreign key across contexts; the data already stored is copied by the migration; two writes in one transaction, held by a separate `@Transactional` bean (not a project rule) · architect
- 2026-10-19 · A design that overturns a pattern ruling of the grid is re-ruled with `partition-patterns`; a ruling whose reopening condition is met is reopened · architect
- 2026-10-19 · If the gate finds the paying code still importing types of `invoice`: they move with `payment`, or are written again on its side for AR-1 and AR-2, in the move plan · architect
- 2026-10-19 · A time taken in the transaction just before its commit is accepted as the time of that commit · architect
- 2026-10-19 · A version older than a migration that moves or drops data (V4, V5) never runs after it: no instance of it runs after the migration, no rollback to it · TL
- 2026-10-19 · The design of the move: a port of `payment` with its own types, an outbound adapter of `payment` that calls `InvoicePayments`; one generator run per tag; own copies of shared helpers · architect, TL
- 2026-10-19 · The ArchUnit exception: `InvoicePayments` and the types of its signature, used from `payment`'s outbound adapter only, declared in the test from `Contracts with neighbours` of both specs (`docs/partition.yml` declares no dependency between contexts); the layer map gives way for `InvoicePayments` only, and the test says so · architect
- 2026-10-19 · Budget record: nothing to do, each context is far under the threshold · architect
- 2026-10-19 · When the gate asks to see the feature running, the TL's look (`./mvnw spring-boot:run`): issue `201`; pay `200`, `PAID`; pay again `409`; cancel the paid invoice `409`; pay `nope` `404`; pay with a blank token `400`; pay a cancelled invoice `409`; the log holds no card token · TL
- 2026-10-19 · The wordings listed in Task 5 are confirmed as written · PO, BA, architect
- 2026-10-19 · Plans in `docs/plans/` as `docs/partition.yml` declares; the global instruction naming `docs/superpowers/plans/` is overridden by the project · project rule 9

## Blockers

None.
