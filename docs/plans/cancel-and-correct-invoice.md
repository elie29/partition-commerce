# Cancel an invoice and issue its correction — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-19

## Tasks

- [x] Task 1 — 2026-10-19 — The owners settle the questions this feature reaches
- [x] Task 2 — 2026-10-19 — The spec of `invoice` in step with the answers and with `billing.openapi.json` 1.1.0
- [x] Task 3 — 2026-10-19 — Attack the spec, then the design of the cancel and of the correction
- [x] Task 4 — 2026-10-19 — Form, layer map and patterns checked against the design
- [x] Task 5 — 2026-10-19 — Cancel an invoice (`cancelInvoice`)
- [x] Task 6 — 2026-10-19 — Issue the correction of a cancelled invoice (`issueInvoice` after a cancel)
- [x] Task 7 — 2026-10-19 — Close: gate, levels, README, C4

## Goal

Deliver the feature "the accountant cancels an invoice before any payment, and issues its correction" in the bounded context `invoice`: `cancelInvoice`, and `issueInvoice` with its 409 as `docs/contracts/billing.openapi.json` 1.1.0 (12f428c) words it, implementing BR-1, BR-3, BR-4, BR-18 and BR-19 of the durable spec `docs/specs/billing/invoice/spec.md` and the design `docs/designs/cancel-and-correct-invoice.md`.

## Task 1 — The owners settle the questions this feature reaches

- Scope: none of the repository; answers of the PO and the architect in the conversation of 2026-10-19
- Verification: `grep -n "REQ-1 against REQ-2\|two meanings\|BR-3 to BR-6 have no trigger" docs/specs/billing/invoice/spec.md`
- Expected: run after Task 2; no line left
- Result: two lines — line 59, the derived `Source parts owned` row of the Glossary (the grep was too wide: it is not a question), and line 117, the open question "Cancelled", whose half for an invoice not paid is settled and whose half for a paid invoice waits for payment (PO). Not as expected as written; every question this feature reaches is answered
- Decisions that bind it: the answers of 2026-10-19, below
- [x] 2026-10-19
## Task 2 — The spec of `invoice` in step with the answers and with `billing.openapi.json` 1.1.0

- Scope: `docs/specs/billing/invoice/spec.md`, `docs/specs/billing/bounded-contexts.md`, through `partition-spec` (and `partition-shape` if the PRD changes version)
- Verification: `grep -n "1.1.0\|cancelInvoice" docs/specs/billing/invoice/spec.md`
- Expected: the contract row names 1.1.0 and `cancelInvoice`; BR-1 and BR-4 carry a trigger and an expected effect from the owners' answers
- Decisions that bind it: the answers of Task 1
- Step 2a — `partition-shape` delta, `billing.md` 1.0 (dc34860) → 1.0.1 (dff137f): only REQ-1 modified; plan validated by the owner, area `billing`, 2026-10-19. Wrote `bounded-contexts.md` (version, provenance, gap "REQ-1 against REQ-2" marked resolved, History line) and `invoice/spec.md` (`Source version`, `Provenance` only). BR-1 is not rewritten by the delta: its new wording is confirmed by a person, then written through `partition-spec` (step 2b).
  - Verification: `git diff --stat -- docs/specs` and grep of the headers, gap, History and BR-1
  - Expected: 2 files; both headers at 1.0.1 · dff137f; gap resolved; one History line; BR-1 unchanged
  - Result: `bounded-contexts.md | 7 ++++---`, `invoice/spec.md | 4 ++--`; headers, gap, History line as expected; BR-1 still "An invoice is issued once per order." — as expected
  - [x] 2026-10-19- Step 2b — through `partition-spec`: BR-1 on the confirmed wording, the open question "REQ-1 against REQ-2", the contract row at 1.1.0 with `cancelInvoice`, BR-4's trigger and effect from the answers of Task 1
  - BR-1 replaced by "An order has at most one invoice that is not cancelled.", trigger and effect as the owner gave them; the open question "REQ-1 against REQ-2" removed. Verified: the new BR-1 row; `0`; `invoice/spec.md | 7 +++----` — as expected
  - Then: BR-1's effect gains 409, no body, the race with a cancel and the database (architect); BR-3 and BR-4 gain trigger and effect (PO, architect); BR-18 (a cancelled invoice is not cancelled again) and BR-19 (anyone holding the identifier cancels; 404) added; Language gains `Cancelled` (for an invoice not paid) and `Correction`; the contract row at 1.1.0 with `cancelInvoice`, "the contract stays 1.0.0" dropped; the open questions on "Cancelled" and on BR-3 to BR-6 narrowed to what payment leaves; finding 6 notes the accepted cancel
  - Result of the task's verification: line 107, the contract row at 1.1.0 with `cancelInvoice`; BR-3, BR-4, BR-18, BR-19 rows with trigger and effect — as expected
  - [x] 2026-10-19
## Task 3 — Attack the spec, then the design of the cancel and of the correction

- Scope: `docs/specs/billing/invoice/spec.md` (`Open questions`), `docs/designs/cancel-and-correct-invoice.md`, through `partition-attack`
- Verification: `grep -c "^### Attack" docs/specs/billing/invoice/spec.md`
- Expected: 2 (the attack of 2026-10-19 on the issue, and this one); every finding of this attack that reaches Task 5 or 6 settled or listed in `## Blockers`
- Result: `2`; findings 1 to 3 listed in `## Blockers`; design written — as expected
- Decisions that bind it: the answers of Task 1
- [x] 2026-10-19
## Task 4 — Form, layer map and patterns checked against the design

- Scope: `docs/partition.yml` (only if the form or its reason changes), through `partition-hexagon` and `partition-patterns`
- Verification: `./mvnw -q test -Dtest='*Architecture*'`
- Expected: BUILD SUCCESS, no dependency against the layer map
- Result: no failure printed; surefire `tests="3" errors="0" failures="0"` — as expected. Form `rich` and its reason (issued to cancelled, forbidden moves) unchanged; the layer map holds the design; P-1 rules the status (one guarded method `cancel`, no State): none of its re-ruling conditions is met
- Decisions that bind it: P-1 (`docs/architecture/patterns-grid.md`)
- [x] 2026-10-19
## Task 5 — Cancel an invoice (`cancelInvoice`)

- Scope: `src/main/java/com/commerce/invoice/**`, `src/test/java/com/commerce/invoice/**`, test first
- Verification: `./mvnw -q test`
- Expected: BUILD SUCCESS; one test class `CancelInvoiceTest` with a test per trigger of BR-3, BR-4, BR-18, BR-19: 200 cancelled with lines, amount and order as issued; 404 no body for an unknown identifier and for one not a UUID; 409 no body for a cancelled invoice; two simultaneous cancels give one 200 and one 409; a cancelled invoice read back cancelled; earlier tests still pass
- Decisions that bind it: P-1; BR-3 (PO): a cancel changes only the status, the customer included, records nothing else; BR-4 (PO): an issued invoice is the status `ISSUED`, neither paid nor cancelled; at no fee: charges nothing, calls no third party; no paid refusal before payment; BR-18 and BR-19 (PO, architect): 409 / 404, no body; the identifier read by one definition shared with `getInvoice` (design)
- Red first: `./mvnw -q test -Dtest=CancelInvoiceTest` — 6 run, 6 failures, each on the generated default 501
- First green run: 4 failures, 500 from a `NullPointerException` in `Invoice`'s constructor: `lines.contains(null)` throws on the immutable list `cancel()` passes; replaced by `lines.stream().anyMatch(Objects::isNull)`
- Result: exit 0; surefire `ArchitectureTest` 3, `CommerceApplicationTests` 1, `CancelInvoiceTest` 6, `IssueInvoiceTest` 15, `GetInvoiceTest` 3, 0 failure, 0 error — as expected
- [x] 2026-10-19
## Task 6 — Issue the correction of a cancelled invoice (`issueInvoice` after a cancel)

- Scope: `src/main/java/com/commerce/invoice/**`, `src/main/resources/db/migration/V2__*.sql`, `src/test/java/com/commerce/invoice/**`, test first
- Verification: `./mvnw -q test`
- Expected: BUILD SUCCESS; tests in `IssueInvoiceTest`: after a cancel, an issue for the same order gives 201 with lines that may differ; an order with an invoice not cancelled still gives 409, no body; two simultaneous issues still give one 201 and one 409
- Decisions that bind it: BR-1 (owner, architect, 2026-10-19): "An order has at most one invoice that is not cancelled."; refused 409, no body; the second of two simultaneous issues refused; an issue racing a cancel gets 409 until the cancel has committed; a `V2` migration replaces the unique key on `order_id` with one on the order of the invoices not cancelled. Correction (PO): two actions, no link to the cancelled invoice. Finding 2 of the attack accepted (PO)
- Red first: `./mvnw -q test -Dtest=IssueInvoiceTest` — 18 run, 3 failures, each 409 from the unique key on `order_id`
- Done: `V2__one_invoice_not_cancelled_per_order.sql` drops `uk_invoice_order` and puts `uk_invoice_open_order` on a generated column `open_order_id` (the order while not cancelled, NULL once cancelled: H2 has no partial index); BR-1 wording realigned in `OrderAlreadyInvoiced`, `Invoices`, `IssueInvoiceUseCase`, `JdbcInvoices`
- Result: exit 0; surefire `ArchitectureTest` 3, `IssueInvoiceTest` 18, `CommerceApplicationTests` 1, `GetInvoiceTest` 3, `CancelInvoiceTest` 6, 0 failure, 0 error — as expected
- [x] 2026-10-19
## Task 7 — Close: gate, levels, README, C4

- Scope: this plan, the README, `docs/architecture/model.yml`, through `partition-gate`, `partition-spec`, `partition-readme`, `partition-c4`
- Verification: the `partition-gate` verdict on `12f428c..` and the working tree
- Expected: SHIP or SHIP WITH CONDITIONS
- Decisions that bind it: none yet
- Step 7.1 — gate, run 1 (2026-10-19), target `12f428c..` + working tree, untracked included: **SHIP WITH CONDITIONS**, confidence High
  - Evidence: `./mvnw -q test` exit 0, 31 tests (Architecture 3, CommerceApplication 1, CancelInvoice 6, IssueInvoice 18, GetInvoice 3), 0 failure; JaCoCo run from the command line (`org.jacoco:jacoco-maven-plugin:0.8.13`, pom untouched): diff coverage 16/16 changed tracked lines; new classes `CancelInvoiceUseCase` 15/15, `InvoiceIds` 5/5, `CancelRefused` 2/2, `InvoiceNotCancellable` 2/2 lines; tampering check: no test deleted, skipped or loosened (two hits are Markdown lines of the spec); probe (temporary test, deleted): an issue while the cancel of its order is uncommitted gets an immediate unique-key violation on `uk_invoice_open_order` (→ `DuplicateKeyException` → 409), and is inserted once the cancel commits — BR-1 as written
  - Finding [Medium] rollback: the artifact of `12f428c` knows only `ISSUED` (`InvoiceStatus`); after a cancel is recorded, it reads a cancelled invoice through `InvoiceStatus.valueOf("CANCELLED")` (`JdbcInvoices.java:54` there) and answers 500. Redeploying the previous artifact is not a clean rollback once one invoice is cancelled; the same holds for an old instance during a rolling deploy (N−1)
  - Condition: the TL accepts forward-fix only once a cancel is recorded, or asks for a rollback path (blocker below)
  - Not verified: Flyway of the old artifact against an applied `V2` it does not know (whether it starts); the `V2` migration on the file database `./.local/h2` (run on the in-memory one only); the losing compare-and-set branch of two cancels is reached by timing, covered in this run, not guaranteed every run; no consumer outside the repository is known
- Step 7.2 — levels: every statement of `## Decisions` is in the spec (BR-1, BR-3, BR-4, BR-18, BR-19, Language) or is how this code is written for this change only (generated column, null check, shared identifier): no `AR-`, no `INV-` (no area file; one bounded context), no rule-file line
- Step 7.3 — README: API section realigned with `partition-readme` (the 409 of `POST /invoices`, `POST /invoices/{invoiceId}/cancel`, the correction); C4 with `partition-c4`: step 4 of `issue-invoice` reads "order has an invoice not cancelled", new dynamic view `cancel-invoice` from the confirmed design; `--check`: 7 elements, 4 relations, 5 views, no error; `c4.drawio` regenerated, 5 pages; Mermaid blocks pasted into `docs/designs/cancel-and-correct-invoice.md` and `docs/designs/issue-and-read-invoice.md`- Step 7.4 — gate, run 2 (2026-10-19), the writes of 7.3 included: since run 1 only `README.md`, `docs/architecture/*`, `docs/designs/*` changed, no file under `src/`; `./mvnw -q test` exit 0, 31 tests, 0 failure (a stale surefire report of the deleted probe, 19:15, not counted). **SHIP WITH CONDITIONS**, the same condition- Step 7.5 — the condition met: forward-fix only, no rollback path in this version (TL). The open finding on "Issued" settled (PO, BA) and written into the spec: Language `Issued` gains "not the status `ISSUED`, which is an invoice neither paid nor cancelled"; the finding leaves `Open questions`. Level of that statement: the spec of `invoice` (one bounded context); no `AR-`, no `INV-`
- Step 7.6 — gate, run 3 (2026-10-19), the spec write included: since run 2 only `docs/specs/billing/invoice/spec.md` and this plan changed, no file under `src/`; `./mvnw -q clean test` exit 0, 31 tests (CommerceApplication 1, Architecture 3, IssueInvoice 18, CancelInvoice 6, GetInvoice 3), 0 failure. The condition met: **SHIP**, confidence High. Still not verified, accepted with forward-fix only: the previous artifact against an applied `V2`; `V2` on the file database
- [x] 2026-10-19

## Decisions

- 2026-10-19 · This plan lives in `docs/plans/` · `docs/partition.yml` `paths.plans` and rule 9 of the project's `CLAUDE.md` say so · rejected: `docs/superpowers/plans/`, named by the owner's global `CLAUDE.md`, which the project's declaration overrides.
- 2026-10-19 · `billing.md` 1.0.1 is brought in through a `partition-shape` delta, not by editing the spec directly · a new version of a source is that skill's procedure · rejected: rewriting BR-1 by hand from the PRD (a copied rule changes only on a person's confirmation).
- 2026-10-19 · BR-1 is "An order has at most one invoice that is not cancelled."; trigger: an issue for an order that already has an invoice that is not cancelled; effect: refused, nothing is issued; of two issues of one order at the same moment, the second is refused · confirmed by the owner on REQ-1 1.0.1 · rejected: the former "An invoice is issued once per order".
- 2026-10-19 · The delta plan for `billing.md` 1.0.1 is validated as shown, area `billing` · owner's validation · rejected: nothing else proposed (decomposition unchanged).
- 2026-10-19 · A change of status to cancelled is not a modification (BR-3): only the status changes, never the lines, the amount or the order · PO: REQ-2 itself corrects by cancelling · rejected: treating the cancel as a breach of BR-3.
- 2026-10-19 · The correction is two actions: cancel, then `issueInvoice` for the same order, with what any issue carries; no link to the cancelled invoice in this version · PO, as REQ-2 says · rejected: one action; a link field.
- 2026-10-19 · This feature's "Cancelled" is BR-4's: voided before any payment, at no fee; BR-6 comes with payment · PO · rejected: settling the paid meaning now.
- 2026-10-19 · No refusal of a paid invoice now: no invoice can be paid yet; REQ-5 out of this feature · PO · rejected: building the paid guard ahead of payment.
- 2026-10-19 · Anyone holding the identifier may cancel · PO: no login in this version, as for issuing · rejected: a role check.
- 2026-10-19 · A cancel records only the status · PO · rejected: recording who or when.
- 2026-10-19 · BR-1 held by the database: a `V2` migration replaces the unique key on `order_id` with one on the order of the invoices not cancelled · architect: holds simultaneous issues too · rejected: a check in the use case.
- 2026-10-19 · Responses: 404 for an identifier that is not a UUID; 409 without a body; the second of two simultaneous cancels 409; an issue racing the cancel of its order 409 until the cancel has committed · architect, as for reading and issuing · rejected: an idempotent 200 on an already cancelled invoice.
- 2026-10-19 · An issued invoice is the status `ISSUED`: neither paid nor cancelled; BR-4's trigger names the status · PO (finding 1) · rejected: the trigger by the word "issued", which Language reads as "recorded by the issue".
- 2026-10-19 · Anyone may cancel then issue for the same order with another customer and lines: accepted for this version · PO (finding 2): no login · rejected: a guard before login exists.
- 2026-10-19 · "Not a UUID: 404" stays in both BR-15 and BR-19 · BA (finding 3): each says it of its own operation · rejected: stating it once.
- 2026-10-19 · Which "not cancelled" in BR-1 once a paid invoice can be reversed: open, settled with payment · PO · rejected: settling it now.
- 2026-10-19 · A cancel leaves the customer unchanged (BR-3); "at no fee" means the cancel charges nothing and calls no third party (BR-4); the correction cannot be found from the cancelled invoice, accepted · PO.
- 2026-10-19 · The spec wording written from the answers (rules, triggers, effects) is confirmed · PO. The design choices (status move per P-1, two cancels at once, `V2`, the shared identifier) are confirmed · architect.
- 2026-10-19 · `Invoice`'s null check on lines uses `stream().anyMatch(Objects::isNull)` · `List.contains(null)` throws on the immutable list a change of status passes back · rejected: copying the lines into a mutable list in `cancel()`.
- 2026-10-19 · BR-1's key sits on a generated column `open_order_id`, NULL once the invoice is cancelled · H2, the project's database, has no partial unique index; it keeps the architect's choice of a unique key on the order of the invoices not cancelled · rejected: a check in the use case (does not hold simultaneous issues).
- 2026-10-19 · Forward-fix only: no rollback path in this version · TL, on the gate's condition (the previous artifact answers 500 on a cancelled invoice) · rejected: a rollback path.
- 2026-10-19 · `Issued` keeps its definition: recorded by the issue, nothing sent to the customer in this version; the status `ISSUED` is an invoice neither paid nor cancelled · PO, BA · rejected: "sent to the customer".
- 2026-10-19 · C4 gains a dynamic view `cancel-invoice`, embedded in the design as generated Mermaid · the design says "drawn from the C4 model, never edit here"; a hand-written diagram would drift · rejected: prose only.
- 2026-10-19 · The architect's "409 without a body" settles BR-1's body too, written into BR-1's effect · it is the answer for the responses the contract leaves open, the issue's 409 among them · rejected: leaving the body to the contract.

## Blockers

None.
