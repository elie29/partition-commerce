# Pay an issued invoice by card through CardPay — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-19

## Tasks

- [x] Task 1 — 2026-10-19 — The owners settle the questions this feature reaches
- [x] Task 2 — 2026-10-19 — The spec of `invoice` in step with the answers and with `billing.openapi.json` 1.2.0
- [x] Task 3 — 2026-10-19 — Attack the spec, then the design of the payment
- [x] Task 4 — 2026-10-19 — Form, layer map and patterns checked against the design
- [x] Task 5 — 2026-10-19 — Pay an invoice (`payInvoice`), CardPay behind an outbound port
- [x] Task 6 — 2026-10-19 — A paid invoice refused by the cancel (BR-4, `cancelInvoice` 409)
- [x] Task 8 — 2026-10-19 — The card token in no log at any level (gate run 1)
- [x] Task 7 — 2026-10-19 — Close: gate, levels, README, C4

## Goal

Deliver the feature "the customer pays an issued invoice by card, through CardPay" in the bounded context `invoice`: `payInvoice` as `docs/contracts/billing.openapi.json` 1.2.0 (5d8fb19) words it (200 the paid invoice, 402 CardPay refused the charge and the invoice stays issued, 404, 409 not issued), implementing REQ-4, BR-5 and DEC-1 of the durable spec `docs/specs/billing/invoice/spec.md`, and the refusal of a paid invoice that BR-4 deferred to payment. REQ-5 (the customer cancels a paid invoice, BR-6) is a later feature (PO, Q9).

## Task 1 — The owners settle the questions this feature reaches

- Scope: none of the repository; answers of the PO, the architect and the TL in the conversation
- Verification: run after Task 2 — `grep -n "BR-5\|partly paid\|1.2.0" docs/specs/billing/invoice/spec.md`
- Expected: BR-5 carries a trigger and an expected effect; the open question "partly paid" is gone or narrowed by the PO; the contract row names 1.2.0 and `payInvoice`
- Decisions that bind it: none
- Answers received 2026-10-19 (TL, PO, architect), one line each in `## Decisions`; the grep runs at the end of Task 2
- [x] 2026-10-19

## Task 2 — The spec of `invoice` in step with the answers and with `billing.openapi.json` 1.2.0

- Scope: `docs/specs/billing/invoice/spec.md`, through `partition-spec`; a CardPay contract under `docs/contracts/` if the TL provides one
- Verification: `grep -n "1.2.0\|payInvoice\|CardPay" docs/specs/billing/invoice/spec.md`
- Expected: the contract row at 1.2.0 with `payInvoice`; BR-5 and BR-4 (paid refusal) with trigger and effect from the answers; the CardPay dependency describes its port (Q1)
- Decisions that bind it: every 2026-10-19 answer in `## Decisions` (Q1–Q9, payment held on the invoice)
- Step 2a — 2026-10-19 — written through `partition-spec`: Language gains `Payment`, `Card token`, `Charge reference`, and `Paid` its status `PAID`; BR-3 trigger gains the pay and its record (time, charge reference, never the token); BR-4 gains the 409 of a paid invoice and an invoice whose pay was refused; BR-5 trigger and effect (200 / 402 / race 409); BR-20 (409 for paid or cancelled), BR-21 (anyone holding the identifier, 404), BR-22 (card token, 400); CardPay's port in `External dependencies`; contract row at 1.2.0 with `payInvoice`; open questions: "partly paid" and "payment vs charge" removed as settled (PO), BR-5 dropped from "no trigger", CardPay not answering, the losing charge, payment as an entity added; attack finding 6 narrowed to the cancel of a paid invoice
  - Verification: the task's grep, plus `grep -c "partly paid"` and `git diff --stat`
  - Expected: line with 1.2.0 and `payInvoice`; BR-5 with trigger and effect; CardPay port line; `0`; one file changed
  - Result: line 113 the contract row at 1.2.0 with `payInvoice`; line 72 BR-5 with trigger and effect; line 107 CardPay with its port; `0`; `spec.md | 27 +++++++++++++++++----------` — as expected
- Step 2b — the owners validate the change shown (PO for BR-3, BR-4, BR-5, BR-20, BR-21, Language; TL for BR-22, CardPay; architect for the race and the open questions), and answer Q7b, Q9b, Q10 in `## Blockers`; their answers written in the same way
  - 2026-10-19: approved as written (PO, TL, architect). Q7b written into BR-22 (content first, 400; then 404, then 409); Q9b into BR-1's trigger (a `PAID` invoice included); Q10 into BR-20 (CardPay not called; called only for `ISSUED`)
  - Verification: `grep -n "^| BR-1 \|^| BR-20\|^| BR-22" docs/specs/billing/invoice/spec.md`
  - Expected: BR-1 trigger names a `PAID` invoice; BR-20 says CardPay is not called; BR-22 gives the order 400, 404, 409
  - Result: line 68 BR-1 "… a `PAID` invoice included (PO, 2026-10-19)"; line 87 BR-20 "… CardPay is not called: it is called only for an invoice whose status is `ISSUED`"; line 89 BR-22 "… checked first (400), then the invoice (404, BR-21; 409, BR-20)" — as expected
  - [x] 2026-10-19
- [x] 2026-10-19

## Task 3 — Attack the spec, then the design of the payment

- Scope: `docs/specs/billing/invoice/spec.md` (`Open questions`), `docs/designs/pay-invoice-by-card.md`, through `partition-attack`
- Verification: `grep -c "^### Attack" docs/specs/billing/invoice/spec.md`
- Expected: 3; every finding that reaches Task 5 or 6 settled or listed in `## Blockers`
- Decisions that bind it: the answers of Task 1; Q7b, Q9b, Q10
- Step 3a — 2026-10-19 — attack of the spec written under `Open questions` ("Attack — 2026-10-19 — feature … pays …"): 7 findings, all listed in `## Blockers`. Result of the grep: `3` — as expected for the count; the findings are open
- Step 3b — once the findings are settled and written into the spec (`partition-spec`): write `docs/designs/pay-invoice-by-card.md`, then attack it
  - 2026-10-19 — answers A1–A7 and the further points written into the spec: Language `Declined`, `Time of the payment`, `Charge reference` bounded by BR-12; BR-3 (token in no log, error, trace); BR-5 (0 charged, CardPay decides; the losing charge not kept; record failing 500; reference out of bounds 500; "Declined" for 402); BR-11 and BR-22 point to BR-12's bound; BR-22 gains the contract refusals and the order; CardPay's fake accepted wherever the service runs; open questions: CardPay errors widened, the charge kept nowhere, retries after 402 testing tokens (accepted); this attack's section keeps finding 4 only, accepted until REQ-5
  - Design written: `docs/designs/pay-invoice-by-card.md`. Attack of the design: order 400 → 404 → 409 → CardPay → record, the races, the token's paths and the siblings checked against the spec; no new finding. The generated API puts `payInvoice` in `InvoicesApi` (no `PaymentApi`): the design names `InvoiceController`
  - Result of the task's grep: `3` — as expected; every finding settled or accepted by its owner, none left as a blocker
  - [x] 2026-10-19

## Task 4 — Form, layer map and patterns checked against the design

- Scope: `docs/partition.yml` (only if the form changes), `docs/architecture/patterns-grid.md`, through `partition-hexagon` and `partition-patterns`; P-1 re-ruled if a re-ruling condition is met (partly paid status, etc.)
- Verification: `./mvnw -q test -Dtest='*Architecture*'`
- Expected: BUILD SUCCESS, no dependency against the layer map; the CardPay adapter in `outbound`, its port in `application`
- Decisions that bind it: P-1
- Result: surefire `com.commerce.ArchitectureTest` tests="3" errors="0" failures="0" — as expected. Form `rich` unchanged; the port `CardPay` in `application`, `FakeCardPay` in `outbound`, the `CardToken` in `domain`, in the layer map. P-1 replayed: the pay is one guarded transition, `pay(chargeReference)`, with `requirePayable()` checking the same guard before CardPay is called; none of P-1's re-ruling conditions is met (no partly paid, no "unpaid again", one "cancelled"; a cancel of a paid or a cancelled invoice behaves the same, 409, naming its rule). No new pattern: ports and adapters is the hexagon's form; `Charge` a sealed interface
- [x] 2026-10-19

## Task 5 — Pay an invoice (`payInvoice`), CardPay behind an outbound port

- Scope: `src/main/java/com/commerce/invoice/**`, `src/main/resources/db/migration/V3__*.sql` if something is recorded, `src/main/resources/application.yaml` for CardPay's configuration, `src/test/java/com/commerce/invoice/**`, test first
- Verification: `./mvnw -q test`
- Expected: completed after Task 3; at least: 200 `PAID` for an `ISSUED` invoice, the whole amount charged once; 402 no body and status still `ISSUED` when the CardPay double refuses; 400 no body for a blank or over-255-character `cardToken`; 404 no body; 409 no body for a paid or a cancelled invoice; two simultaneous pays give one 200 and one 409; time of payment and charge reference recorded, the token never; earlier tests still pass
- Decisions that bind it: Q1 (TL): a port, one operation, a charge of a card token for an amount → accepted with a charge reference, or refused; one adapter, a fake accepting every card; a refusal tested with a test double; no URL, no credentials. Q2 (PO): one call charges the whole amount, once. Q4 (architect): the payment recorded by the same conditional update as a cancel, on an invoice still issued; first to commit wins, the other 409; the losing charge is not reversed in this feature. Q5 (PO): anyone holding the identifier. Q6 (PO): records status, time of payment, charge reference; never the token; not a modification (BR-3). Q7 (TL): token non-blank, at most 255 characters, else 400 no body. Q8 (PO): a refused pay is no payment, the invoice stays `ISSUED`. Payment held on the invoice (architect)

- Red first: `./mvnw -q test -Dtest=PayInvoiceTest` — 13 run, 13 failures, the controller not wired (generated default 501)
- Done: domain `InvoiceStatus.PAID`, `CardToken` (bounded by `Text`, `toString()` hidden), `InvoiceNotPayable`, `Invoice.requirePayable()` / `pay(chargeReference)` / `chargeReference()`, `restore` with the charge reference, `cancel()` refusing `PAID` (BR-4, used by Task 6); application `CardPay`, `Charge`, `PayInvoiceUseCase`, `PayRefused`, `CardDeclined`, `Invoices.recordPayment`, `InvoiceContentRefused` widened to a pay; outbound `FakeCardPay`, `JdbcInvoices.recordPayment` and `find` reading the charge reference; `V3__record_the_payment.sql`; inbound `InvoiceController.payInvoice`, `InvoiceErrors` 409 / 402; tests `PayInvoiceTest`, `InvoiceClient.pay`
- First green run: 1 failure — a pay with no body and no `Content-Type` answers 415, not 400: Spring refuses the media type while it chooses the handler, before `InvoiceController` is matched, so the advice scoped to it does not apply. Mapping it to 400 needs an advice for every controller or a filter: not chosen (blocker B1). The test's "no body" now sends `Content-Type: application/json` (400); `""` added
- Result: `PayInvoiceTest` tests="13" errors="0" failures="0" — as expected, except the case of blocker B1, not tested
- B1 settled (architect, option c): "no body" is a JSON request with an empty body; no content type keeps the framework's 415, for issuing and paying alike. Written into BR-13 and BR-22; tests `keeps_the_framework_415_for_a_pay_with_no_content_type` and `keeps_the_framework_415_for_an_issue_with_no_content_type` pin it (green at once: no code change, they fix the framework's behaviour as the rule)
- Verification: `./mvnw -q test`
- Expected: ArchitectureTest 3, CommerceApplicationTests 1, CancelInvoiceTest 8, IssueInvoiceTest 20, GetInvoiceTest 3, PayInvoiceTest 14; 0 failure, 0 error
- Result: surefire CancelInvoiceTest 8, ArchitectureTest 3, CommerceApplicationTests 1, GetInvoiceTest 3, IssueInvoiceTest 20, PayInvoiceTest 14; errors 0, failures 0 everywhere — as expected
- [x] 2026-10-19

## Task 6 — A paid invoice refused by the cancel (BR-4, `cancelInvoice` 409)

- Scope: `src/main/java/com/commerce/invoice/**`, `src/test/java/com/commerce/invoice/**`, test first
- Verification: `./mvnw -q test -Dtest=CancelInvoiceTest`
- Expected: a cancel of a paid invoice gives 409, no body, the invoice still paid; earlier cancel tests pass
- Decisions that bind it: BR-4 (PO, 2026-10-19): the refusal of a paid invoice comes with payment; Q9 (PO): `cancelInvoice` refuses a paid invoice with 409; Q8 (PO): an invoice whose pay was refused stays `ISSUED` and may be cancelled; Q9b (PO): an issue for the order of a paid invoice gets 409
- Not red first: `Invoice.cancel()` refusing `PAID` was written in Task 5; the tests came after it
- Tests added: `CancelInvoiceTest` — a cancel of a paid invoice 409, no body, read back `PAID`; a simultaneous pay and cancel, one 200 and one 409, the status of the winner. `IssueInvoiceTest` — an issue for the order of a paid invoice, 409, no body (BR-1)
- Verification run: `./mvnw -q test` (the whole suite, wider than the task's command)
- Expected: ArchitectureTest 3, CommerceApplicationTests 1, CancelInvoiceTest 8, IssueInvoiceTest 19, GetInvoiceTest 3, PayInvoiceTest 13; 0 failure, 0 error
- Result: surefire ArchitectureTest 3, CommerceApplicationTests 1, IssueInvoiceTest 19, GetInvoiceTest 3, PayInvoiceTest 13, CancelInvoiceTest 8; errors 0, failures 0 everywhere — as expected
- [x] 2026-10-19

## Task 8 — The card token in no log at any level (gate run 1)

- Scope: `src/main/resources/application.yaml`, `src/test/java/com/commerce/invoice/PayInvoiceTest.java`, the javadoc and message of `CancelRefused` and `CancelInvoiceUseCase`
- Verification: `./mvnw -q test`
- Expected: red first — `PayInvoiceTest` run with `org.springframework.web` at DEBUG fails on the token; then green: every class passes, PayInvoiceTest 14 at DEBUG
- Decisions that bind it: A5 (TL): the token never appears in a log, an error or a trace
- Red first: `PayInvoiceTest` with `logging.level.org.springframework.web=TRACE` — 14 run, 1 failure, `the_card_token_appears_in_no_log_and_no_answer`
- Tried: `RequestResponseBodyMethodProcessor` pinned at INFO in `application.yaml` — still 1 failure: `o.s.web.method.HandlerMethod` prints the arguments at TRACE, `PayInvoice {cardToken: tok_visa_4242}`. The cause is the generated `PayInvoice.toString()`; pinning loggers one by one is not a fix. Pin and TRACE property reverted (`git diff -- src/main/resources/application.yaml` empty; `PayInvoiceTest` 14 run, 0 failure): blocker B2
- Done: the Low finding — `CancelRefused` javadoc, `CancelInvoiceUseCase` javadoc and message name BR-4 and a simultaneous pay. `./mvnw -q test`: GetInvoiceTest 3, ArchitectureTest 3, CancelInvoiceTest 8, CommerceApplicationTests 1, IssueInvoiceTest 20, PayInvoiceTest 14; errors 0, failures 0
- B2 settled (TL, 2026-10-19): accepted for this version, no contract change; written as an open question of the spec (owner TL)
- [x] 2026-10-19

## Task 7 — Close: gate, levels, README, C4

- Scope: as `partition-plan` _Close_ says
- Verification: `partition-gate` on the plan's changes
- Expected: SHIP or SHIP WITH CONDITIONS; README realigned (new endpoint, CardPay configuration); C4 `Invoicing → CardPay` relation checked
- Decisions that bind it: TL (2026-10-19): a rollback is one-way once an invoice is paid; fix forward
- Gate run 1 — 2026-10-19 — target 5d8fb19 → working tree: 12 tracked files +221/−32, plus 12 untracked (10 code and test files, `V3`, the design; the plan itself)
  - Evidence: `collect_change_context.sh --range 5d8fb19` (untracked files read by hand); `detect_test_tampering.sh --range 5d8fb19`: none; `./mvnw -q org.jacoco:jacoco-maven-plugin:0.8.13:prepare-agent test …:report`: no failure; `diff_coverage.py --range 5d8fb19`: 33/33 tracked changed lines executed (100%), 6 files not measured (javadoc, an interface method, tests); jacoco.csv for the untracked classes: `PayInvoiceUseCase` 23/23 lines, 4/4 branches, `CardToken` 3/4 (its `toString()` never called), every other new class fully covered
  - Probe: `./mvnw -q test -Dtest='PayInvoiceTest#the_card_token_appears_in_no_log_and_no_answer' -Dlogging.level.org.springframework.web=DEBUG` — 1 run, 1 failure: `RequestResponseBodyMethodProcessor : Read "application/json…" to [class PayInvoice { cardToken: tok_15b9854a-… }]`
  - Findings: [High] the card token is logged as soon as `org.springframework.web` is at DEBUG — the generated `PayInvoice.toString()` prints it, Spring logs the body it read (`InvoiceController.java` `payInvoice`; BR-3, A5). [Low] `CancelRefused` and `CancelInvoiceUseCase` still say BR-18 only, and the losing cancel's message says "a simultaneous cancel" when a pay may have come first. [Low] `CardToken.toString()` not run by a test
  - Deployment: `V3` adds two nullable columns, no rewrite, no lock beyond the DDL; an earlier version reading a `PAID` row fails (`InvoiceStatus.valueOf`): rollback one-way, fix forward (TL); a rolling deploy answers 500 on old nodes for a paid invoice until they are gone. A charge accepted whose record loses leaves no log line (spec: kept nowhere, architect)
  - Verdict: **HOLD**, confidence High — the High finding is reproduced and unmitigated. Its fix becomes Task 8
- Gate run 2 — 2026-10-19 — same target, Task 8 and B2 included: 14 tracked files, plus the untracked ones
  - Evidence: `detect_test_tampering.sh --range 5d8fb19`: every category none; `./mvnw -q org.jacoco:jacoco-maven-plugin:0.8.13:prepare-agent test …:report`: CommerceApplicationTests 1, CancelInvoiceTest 8, ArchitectureTest 3, PayInvoiceTest 14, IssueInvoiceTest 20, GetInvoiceTest 3; errors 0, failures 0; `diff_coverage.py --range 5d8fb19`: 33/34 (97.1%) — uncovered `CancelInvoiceUseCase.java:36`, the cancel whose conditional update loses, reached only when the race interleaves so (message changed only); jacoco.csv: `PayInvoiceUseCase` 23/23, 4/4 branches; `CardToken` 3/4. Manual probe by the TL on `./mvnw spring-boot:run`: issue 201; pay 200 `PAID`; pay again 409; cancel the paid invoice 409; pay `nope` 404; blank token 400; pay a cancelled invoice 409; no card token in the log
  - Findings: [High, accepted by the TL] the token logged at DEBUG / TRACE of `org.springframework.web` (B2, spec open question). [Low] `CardToken.toString()` not run by a test. [Low] `CancelInvoiceUseCase.java:36` covered only by a race
  - Deployment: as run 1; rollback one-way, fix forward (TL)
  - Verdict: **SHIP WITH CONDITIONS**, confidence Medium — sound and tested, one High finding accepted under an operating condition. Condition: `org.springframework.web` never at DEBUG or TRACE where the service runs — met by the TL's statement of 2026-10-19 for the demo, to be revisited with a real CardPay adapter
- Levels (close steps 2–4): every answer of the owners is in `docs/specs/billing/invoice/spec.md` (Language, BR-1, BR-3, BR-4, BR-5, BR-11, BR-13, BR-20 to BR-22, External dependencies, Contracts with neighbours, Open questions); the design choices (time of the payment, `InvoiceController`, the port's shape) are of this feature, in `docs/designs/pay-invoice-by-card.md`. AR- and INV- entries added or changed: 0 — no area file, no register, no statement spans two areas or layers. Candidate not written, for the TL to validate through `partition-rules`: "a migration whose data an earlier version cannot read is one-way: the project fixes forward" (`.claude/rules/backend-database.md`), held today by `## Decisions` of this plan and by the README only
- README (close step 5): `README.md` API section — `payInvoice` and its answers, the cancel's 409 for a paid invoice, a paid invoice neither cancelled nor corrected, CardPay's fake with nothing to configure, 415 with no content type, `V3` and fix forward
- C4 (close step 5): `docs/architecture/model.yml` — view `pay-invoice` added, `cancel-invoice` steps name BR-4 and a simultaneous pay; relations `invoicing -> cardpay` and `customer -> commerce-app` unchanged. `c4-drawio.mjs --check`: 7 elements, 4 relations, 6 views, no error; `c4-drawio.mjs`: six pages written, "Dynamic - Pay an invoice by card" 4 placed; the design's sequence replaced by `--mermaid pay-invoice`
- Gate run 3 — 2026-10-19 — same target, close steps 3–5 included (`README.md`, `model.yml`, `c4.drawio`, `spec.md`, the design)
  - Evidence: `detect_test_tampering.sh --range 5d8fb19`: every category none; `./mvnw -q test`: ArchitectureTest 3, CancelInvoiceTest 8, GetInvoiceTest 3, CommerceApplicationTests 1, IssueInvoiceTest 20, PayInvoiceTest 14; errors 0, failures 0; each statement of the README checked against a test or a file of the tree (402, 409, 400 first, 415, the fake, `V3`)
  - Verdict: **SHIP WITH CONDITIONS**, confidence Medium — the same as run 2; the condition is met by the TL's statement

## Decisions

- 2026-10-19 · Plan in `docs/plans/` · `docs/partition.yml` `paths.plans` and project rule 9 say so · rejected: `docs/superpowers/plans/` of the global instructions.
- 2026-10-19 · No code before the spec moves: BR-5 has no trigger nor effect, the spec's contract row is still 1.1.0, and no CardPay contract exists in the repository · `.claude/rules/partition.md` _Truth_ · rejected: building `payInvoice` from the OpenAPI alone.

- 2026-10-19 · Q1 · CardPay is a port with one operation, a charge of a card token for an amount, answering accepted with a charge reference, or refused; its only adapter is a fake that accepts every card; a refusal is tested with a test double; no URL, no credentials · TL: CardPay is not reachable from the demo · rejected: an HTTP adapter.
- 2026-10-19 · Q2 · One call charges the whole amount, once; no partial payment in this version · PO · rejected: partial payment.
- 2026-10-19 · Q3 · CardPay not answering is an open question until CardPay is reachable; the fake always answers · architect · rejected: an interim timeout behaviour.
- 2026-10-19 · Q4 · The payment is recorded by the same conditional update as a cancel, on an invoice still issued: the first to commit wins, the other gets 409. A charge whose record loses is reversed with REQ-5: an open question until then · architect.
- 2026-10-19 · Q5 · Anyone holding the identifier pays, as for cancelling; no login in this version, accepted · PO.
- 2026-10-19 · Q6 · A payment records the status, the time of the payment and CardPay's charge reference, never the card token; paying changes the status, it is not a modification (BR-3) · PO.
- 2026-10-19 · Q7 · The card token is made by CardPay on the customer's side, never a card number: non-blank, at most 255 characters; otherwise 400 without a body, as for issuing · TL.
- 2026-10-19 · Q8 · A refused pay is no payment: the invoice stays issued and may be cancelled. `PAID` is an invoice whose charge is recorded. A payment is a charge recorded on the invoice; a declined charge is no payment · PO.
- 2026-10-19 · Q9 · Scope REQ-4 only; `cancelInvoice` now refuses a paid invoice with 409 (BR-4); a paid invoice cancelled by the customer (REQ-5) is a later feature · PO.
- 2026-10-19 · A payment is not an entity of its own in this feature: it is held on the invoice. The question stays open: a yes reopens the `invoice` / `payment` split · architect.

- 2026-10-19 · Q7b · As for reading and cancelling: 404 for an identifier that is not a UUID; the content of the request first (400), then the invoice (404, 409) · architect · rejected: the invoice checked before the token.
- 2026-10-19 · Q9b · A `PAID` invoice is not cancelled, so a new issue for its order gets 409 (BR-1) · PO.
- 2026-10-19 · Q10 · The refusals come first; CardPay is called only for an issued invoice. Read as: a pay of a `PAID` or `CANCELLED` invoice is refused with 409 without calling CardPay (the answer opens with "No", read against the rest of its sentence) · architect · rejected: charging before the status is read.
- 2026-10-19 · The spec change of Task 2 approved as written, each owner for their parts · PO, TL, architect.

- 2026-10-19 · A1 · A charge whose record loses leaves no trace: not kept in this version; the loser gets 409; open until REQ-5 · architect.
- 2026-10-19 · A2 · Charged, then the record fails for another reason: 500, the invoice stays issued; same open question as A1 · architect.
- 2026-10-19 · A3 · An invoice whose amount is 0 is charged like any other amount: CardPay decides · PO · rejected: not calling CardPay for 0.
- 2026-10-19 · A4 · A paid invoice cannot be corrected: accepted until REQ-5 · PO.
- 2026-10-19 · A5 · The card token never appears in a log, an error or a trace · TL.
- 2026-10-19 · A6 · No body, invalid JSON, no `cardToken`, a `null` or non-string token: 400 without a body, before 404 and 409 · architect.
- 2026-10-19 · A7 · CardPay declines a card; a refusal is ours · PO.
- 2026-10-19 · Retries of pays after 402, testing card tokens: accepted in this version, no login; an open question · PO.
- 2026-10-19 · The fake accepts every card wherever the service runs: accepted, the demo has no real customer; a real adapter comes with CardPay · TL.
- 2026-10-19 · The time of the payment is when the record commits; the contract does not return it · architect.
- 2026-10-19 · A charge reference out of bounds is a defect, 500, under the open question on CardPay errors · architect.
- 2026-10-19 · CardPay's charge reference is non-blank text of at most 255 characters; the 402 carries no body · TL.
- 2026-10-19 · "Non-blank, at most 255 characters" is written once, in the rule that bounds a label (BR-12); the others (BR-11, BR-22, the charge reference) point to it · architect · PO confirmed the wording written from these answers.
- 2026-10-19 · Design: the time of the payment is the database's `CURRENT_TIMESTAMP` in the one-statement transaction that records the payment; the domain does not hold it until BR-6 needs it · the record's commit is the moment the architect named, and one statement commits with its transaction · rejected: the application clock before the update (not the commit).
- 2026-10-19 · Design: `payInvoice` implemented by `InvoiceController`, the generator producing one `InvoicesApi` · the rule "a controller implements the generated interface" · rejected: a `PaymentController`, which would need the generator's `useTags`, a change of the build outside this feature.
- 2026-10-19 · Design: `CardPay` is an application port `charge(CardToken, Money)` → `Charge.Accepted(reference) | Charge.Declined`, `FakeCardPay` its only adapter in `outbound`; `Invoices` gains `recordPayment(Invoice)`, a conditional update on `ISSUED`; migration `V3` adds `paid_at` and `charge_reference` · Q1, Q4, Q6 · rejected: widening `changeStatus`, which records the status and nothing else.

- 2026-10-19 · No mapping of the 415 a pay with no body and no `Content-Type` gets: the advice scoped to `InvoiceController` cannot catch it, and an advice for every controller or a filter is a choice of the architect · `.claude/rules/partition.md`: ask, choose no interim behaviour · rejected: an unscoped `@RestControllerAdvice`, chosen alone.

- 2026-10-19 · B1 · "No body" is a JSON request with an empty body; a request with no content type keeps the framework's 415, for issuing and paying alike · architect · rejected: (a) an advice for every controller, (b) a filter of `invoice`.
- 2026-10-19 · A rollback is one-way once an invoice is paid (an earlier version cannot read `PAID`): the project fixes forward · TL · rejected: a rollback path.

- 2026-10-19 · Gate run 1: HOLD on the card token logged at DEBUG / TRACE of `org.springframework.web`; the fix is Task 8 · `partition-plan` _Close_: a HOLD reason becomes a task.
- 2026-10-19 · Logger pinning tried and reverted: two Spring loggers print the token (`RequestResponseBodyMethodProcessor` at DEBUG, `HandlerMethod` at TRACE); the cause is the generated `toString()`, whose fix touches the contract or the build · rejected: pinning loggers one by one, an interim behaviour.

- 2026-10-19 · B2 · The token logged at DEBUG / TRACE of `org.springframework.web`: accepted for this version — the demo never runs that logger at DEBUG or TRACE and has no real card token; open until a real CardPay adapter comes; no contract change · TL · rejected: (a) `format: password`, (b) a hand-written request class, (c) logger pinning.
- 2026-10-19 · The TL looked at the feature in the running application (`./mvnw spring-boot:run`): 201, 200 `PAID`, 409, 409, 404, 400, 409; no card token in the log · TL.

## Blockers

None.
