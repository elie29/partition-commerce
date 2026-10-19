# Issue and read an invoice — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-19

## Tasks

- [x] Task 1 — 2026-10-19 · 8cdf468 — The owners settle the attack findings
- [x] Task 2 — 2026-10-19 · deb122f — The contract of `invoice`, the role model and the invariant register
- [x] Task 3 — 2026-10-19 · 8cdf468 — The spec of `invoice` in step with the answers, and the design of the issue
- [x] Task 4 — 2026-10-19 · 8cdf468 — Form, layer map and P-1 checked against the spec
- [x] Task 5 — 2026-10-19 · 8cdf468 — The rules of code stated on 2026-10-19
- [x] Task 6 — 2026-10-19 · 8cdf468 — The build: generator, validation, architecture test
- [x] Task 7 — 2026-10-19 · 8cdf468 — Issue an invoice
- [x] Task 8 — 2026-10-19 · 8cdf468 — Read an invoice back
- [x] Task 9 — 2026-10-19 — Close: gate, levels, README, C4
- [x] Task 10 — 2026-10-19 — The spec of `invoice` in step with the answers of 2026-10-19
- [x] Task 11 — 2026-10-19 — A text of only non-breaking spaces is blank
- [x] Task 12 — 2026-10-19 — A currency starting with `X` is not known
- [x] Task 13 — 2026-10-19 — The rule line on JSON coercion
- [x] Task 14 — 2026-10-19 — The known currency codes read once
- [x] Task 15 — 2026-10-19 — Reading a stored invoice that today's rules refuse

## Goal

Deliver the feature "the accountant issues the invoice of a confirmed order, and anyone can read it back" in the bounded context `invoice`: `issueInvoice` and `getInvoice` of `docs/contracts/billing.openapi.json` 1.0.0, implementing the durable spec `docs/specs/billing/invoice/spec.md` (BR-1, BR-2, BR-7 to BR-15) and the design `docs/designs/issue-and-read-invoice.md`.

## Task 1 — The owners settle the attack findings

- Scope: none of the repository; answers of PO, BA, architect, TL in the conversation of 2026-10-19
- Verification: `grep -n "^[0-9]*\. \*\*" docs/specs/billing/invoice/spec.md`
- Expected: findings 1, 6, 7, 11, 13 only
- Result: `1. **Who may read an invoice.**`, `6. **Read access reaches payment and cancellation.**`, `7. **Who may issue.**`, `11. **Words.**`, `13. **The H2 console**` — as expected
- Decisions that bind it: the answers of 2026-10-19, below
- [x] 2026-10-19 · 8cdf468

## Task 2 — The contract of `invoice`, the role model and the invariant register

- Scope: `docs/contracts/billing.openapi.json` (architect, committed alone)
- Verification: `git log --oneline -1 -- docs/contracts/billing.openapi.json`
- Expected: `deb122f docs(contracts): billing 1.0.0`
- Result: `deb122f docs(contracts): billing 1.0.0` — as expected
- Decisions that bind it: no login in this version; no invariant register yet
- [x] 2026-10-19 · deb122f

## Task 3 — The spec of `invoice` in step with the answers, and the design of the issue

- Scope: `docs/specs/billing/invoice/spec.md`, `docs/designs/issue-and-read-invoice.md`, `docs/architecture/model.yml` (view `issue-invoice`), `docs/architecture/c4.drawio`
- Done so far: spec written (Scope, Language, BR-1 trigger and effect, BR-7 to BR-15, Contracts with neighbours, Open questions); view `issue-invoice` added, `--check` gave "7 elements, 4 relations, 4 views, no error", `c4.drawio` regenerated; design written with the Mermaid of that view
- Then: answers of 2026-10-19 (second round) written: Language Order, Confirmed, Customer, Accountant (BA); BR-16 at least one line, no maximum (PO); BR-17 lines read back in issue order (PO); BR-2 trigger and effect from the PO's "Amount"; open questions reduced to "BR-3 to BR-6"; design steps 3–4 confirmed (architect)
- Verification: `git diff -- docs/specs/billing/invoice/spec.md` read with the owners
- Expected: validated by PO (rules, scope), BA (Language), architect (BR-12 to BR-15, contract row, design)
- Result: "We validate the spec diff as written" (PO, BA, architect); design confirmed (architect) — as expected. The second-round additions (Language, BR-16, BR-17, BR-2 cells) are the owners' words, written after that validation
- Decisions that bind it: every answer of 2026-10-19
- [x] 2026-10-19 · 8cdf468

## Task 4 — Form, layer map and P-1 checked against the spec

- Scope: `docs/partition.yml` (hexagon `invoice`), `docs/architecture/patterns-grid.md` (P-1) — through `partition-hexagon` and `partition-patterns`
- Verification: read the hexagon entry and P-1 against the validated spec
- Expected: form `rich` and its layer map hold; P-1 holds (this feature adds no status and no move: an invoice is created issued)
- Result: `rich` holds — BR-2, BR-7, BR-10 tie the lines, the currency and the amount together at every write (rich row "rules that tie several pieces of data together"); layer map unchanged, the generated API in `com.commerce.invoice.inbound.api` sits in the inbound layer; P-1 holds, none of its re-ruling triggers (partly paid, "unpaid again", two "cancelled", a fee) entered the spec — as expected
- Decisions that bind it: P-1 (2026-10-19, `docs/plans/architecture-baseline.md`); the generated API lands in `com.commerce.invoice.inbound.api` (TL)
- [x] 2026-10-19 · 8cdf468

## Task 5 — The rules of code stated on 2026-10-19

- Scope: `.apm/instructions/backend-database.instructions.md` (amount in minor units, column names), a new `.apm/instructions/backend-configuration.instructions.md` scoped to `src/main/resources/**, src/test/resources/**` (YAML only), the log line of `invoice` (identifiers only) in a security rule scoped to `src/main/java/com/commerce/invoice/**` — through `partition-rules`, then `apm install`, then `partition-budget`
- Verification: `apm install`, then `partition-budget`
- Expected: the deployed copies under `.claude/rules/` carry the new lines; the budget report lists no rule "without a scope"
- Decisions that bind it: amounts in minor units (architect); column names (TL); YAML only (architect); identifiers only in logs (TL)
- Result: the deployed copies `.claude/rules/backend-{configuration,database,security-invoice}.md` carry the new lines, same text as `.apm/instructions/` (read 2026-10-19; `apm install` landed in `8cdf468` with `apm.lock.yaml`). `node .claude/skills/partition-budget/scripts/budget.mjs --check` (2026-10-19): every session 1 786 tokens = `.claude/CLAUDE.md` 58 + instruction 621 + 13 descriptions 1 107, no rule without a scope; `invoice` _Always_ 4 551 of 32 000 (14 % ok); plan 4 366 whole, 461 on resume, ok; exit 0 — as expected. Measurement only; no assessment record written (the assessment is held with the people, `budget.review: quarterly`)
- [x] 2026-10-19 · 8cdf468

## Task 6 — The build: generator, validation, architecture test

- Scope: `pom.xml`, `src/test/java/com/commerce/**` (architecture test)
- Steps: property `openapi-generator.version` = `7.26.0` and the plugin as the TL gave it; `spring-boot-starter-validation` versioned by Spring Boot; `com.tngtech.archunit:archunit-junit5` 1.5.1, test scope; the architecture test as `backend-testing` states it
- Verification: `./mvnw -q test`
- Expected: BUILD SUCCESS; `IssueInvoice`, `Invoice`, `Line`, `Money` and an `InvoiceApi` interface generated under `target/generated-sources/openapi`, importing no Swagger annotation and no `jackson-databind-nullable`
- Decisions that bind it: the generator configuration (TL); ArchUnit 1.5.1 (TL); layer map of `docs/partition.yml`
- Result: `Tests run: 4, Failures: 0` — BUILD SUCCESS; `InvoicesApi`, `IssueInvoice`, `Invoice`, `Line`, `Money` (and `ApiUtil`) generated, `grep swagger|nullable` empty — as expected (the interface is `InvoicesApi`, from the tag). The test was also run with a domain class importing Spring and an inbound class referencing the domain: both caught (2 failures), the two classes then removed
- [x] 2026-10-19 · 8cdf468

## Task 7 — Issue an invoice

- Scope: `src/main/java/com/commerce/invoice/**`, `src/main/resources/db/migration/**`, `src/test/java/com/commerce/invoice/**`
- Steps: test first, one test per rule: BR-1 (409, simultaneous issues included), BR-2, BR-7 to BR-14, BR-13 before BR-1; then the code: domain (invoice, line, money), application (issue), inbound (controller implementing the generated interface), outbound (`JdbcClient`, Flyway migration)
- Verification: `./mvnw -q test`
- Expected: BUILD SUCCESS, every rule test of the issue green, the architecture test green
- Decisions that bind it: P-1; amounts in minor units, `BIGINT` + ISO code, columns `<what>_minor` / `<what>_currency`; `JdbcClient`; UUID made by `invoice`; 400 before BR-1; the database refuses the second simultaneous issue; identifiers only in logs; no login; a character is a code point; strict JSON types; tests on an in-memory database
- Red first: `Tests run: 12, Failures: 11, Errors: 1` (no controller). Then two real failures fixed: `amountMinor: 1.5` was truncated (Jackson coercion → `JsonConfiguration`), a 255-emoji label gave a 500 (H2 counts UTF-16 units → columns of 510)
- Result: `Tests run: 16, Failures: 0, Errors: 0` — BUILD SUCCESS — as expected
- [x] 2026-10-19 · 8cdf468

## Task 8 — Read an invoice back

- Scope: as Task 7
- Steps: test first: BR-15 (200; 404 for an unknown identifier and for one that is not a UUID); the read returns no customer; then the code
- Verification: `./mvnw -q test`
- Expected: BUILD SUCCESS, the BR-15 tests green
- Decisions that bind it: anyone holding the identifier reads (PO); 404 for a non-UUID (architect); lines in issue order (BR-17)
- Red first: `Tests run: 3, Failures: 3`. `GetInvoiceUseCase`, `InvoiceNotFound` and the read in `InvoiceController` / `InvoiceErrors` were written in the working tree by the owner between turns; the assistant added the missing import of `InvoiceNotFound` in `InvoiceErrors`. Only the canonical UUID form is read (`1-1-1-1-1` → 404)
- Result: `Tests run: 19, Failures: 0, Errors: 0` — BUILD SUCCESS — as expected
- [x] 2026-10-19 · 8cdf468

## Task 9 — Close: gate, levels, README, C4

- Scope: as `partition-plan`, _Close_
- Verification: `partition-gate` from `deb122f` to the working tree
- Expected: SHIP or SHIP WITH CONDITIONS
- Decisions that bind it: none
- Gate 1 (2026-10-19, run by a separate agent, read-only): **SHIP WITH CONDITIONS**, confidence medium-high. Evidence: `./mvnw … jacoco … test` → `Tests run: 19, Failures: 0, Errors: 0, Skipped: 0`, BUILD SUCCESS; new production lines 145 covered / 1 missed (`lines == null`, unreachable behind `@NotNull`), branches 30/38 (defensive null checks); no test tampering. Findings, all Low: non-breaking spaces pass as non-blank (`Text.java:15`); "known" ISO code is the JDK list, `XXX` included (`Money.java:33`); no cap on lines with anonymous callers (within BR-16, residual risk). Not verified: base-vs-change comparison, jshell boundary probes (approval refused), engines other than H2, load beyond two threads, external consumers, deployed rule copies
- Conditions, each a task or a blocker:
  - [x] `apm install`, then `partition-budget` — Task 5 (2026-10-19 · 8cdf468): no rule without a scope, `invoice` 4 551 of 32 000
  - [x] the architect confirms "255 characters = code points, `VARCHAR(510)`" — confirmed 2026-10-19 (Decisions); written to the spec in Task 10
  - [x] PO, BA settle non-breaking spaces and "known ISO 4217 code" — settled 2026-10-19 (Decisions); Tasks 10, 11, 12 (2026-10-19)
  - [x] TL's rule line on JSON coercion — accepted 2026-10-19; Task 13 (2026-10-19)
  - [x] README realigned with `partition-readme` (2026-10-19 · 8cdf468): `## Build` names the generated interfaces, new `## API` lists the two operations, their statuses, no body on refusal, no coercion
- Levels (close steps 2–4), proposed, nothing written yet: the spec already holds every business statement (Scope, Language, BR-1, BR-2, BR-7 to BR-17, the contract row); the rules of code are in `.apm/instructions/` (Task 5); candidate rule line for `backend-api-contract`: "A JSON value of the wrong type is refused, never coerced (`com.commerce.JsonConfiguration`)" — waits for the TL; area file and invariant register: no candidate (one bounded context in the area; no register, architect)
- C4 (close step 5): the model has no element for "anyone holding an identifier" who reads (`getInvoice`); not drawn in this version, the model keeps the accountant (architect, 2026-10-19): C4 unchanged
- Levels, updated 2026-10-19: the JSON rule line goes to `backend-api-contract` (TL accepted; Task 13); the blank and known-currency statements go to the spec (Task 10)
- Levels after Tasks 10–13 (2026-10-19): Blank, Character, BR-9 are in the spec (validated by PO, BA, architect); the JSON line is in `backend-api-contract` (deployed); area file and invariant register: no candidate (one bounded context in `billing`; no register yet, architect). `AR-` / `INV-` entries added or changed: zero
- README after Tasks 10–13: unaffected — it points to the spec for the rules and already says "A JSON value of the wrong type is refused, never coerced" (`README.md:21`); nothing users install, run or configure changed
- Gate 2 (2026-10-19, separate agent, read-only, `deb122f` to the working tree, 41 files +1500/−12): **SHIP WITH CONDITIONS**, confidence medium-high. Evidence: `collect_change_context.sh` leads read, none confirmed; `detect_test_tampering.sh` none; `./mvnw -B jacoco:prepare-agent test jacoco:report` → `Tests run: 22, Failures: 0, Errors: 0, Skipped: 0`, BUILD SUCCESS; `diff_coverage.py` → 147/148 changed lines (99.3 %), the one miss `IssueInvoiceUseCase.java:42` unreachable behind `@NotNull`; deployed `backend-api-contract.md` sha256 equals the `apm.lock.yaml` entry. Gate-1 findings 1, 2 closed; 3 residual (BR-16). Every BR-1 to BR-17 effect checked against the code. Findings, both Low: (1) `Invoice.restore` re-runs issue-time checks, so a stored invoice that fails today's rules (a pre-fix row in the file database `./.local/h2`, or a code a later JDK drops, BR-9) reads as 500, not BR-15's 200 (`JdbcInvoices.java:54-58`, `Invoice.java:20-29`, `Money.java:9`); (2) `Currency.getAvailableCurrencies()` copied for every `Money` (`Money.java:33`). Not verified: red-first by the auditor (export refused), JDK probe (refused), engines other than H2, load with many lines, external consumers, `JsonConfiguration` on future contexts
- Conditions of gate 2:
  - [x] finding 1 — Task 15 (2026-10-19): database cleared, no design change, the dropped-currency case an open question (architect)
  - [x] finding 2 — Task 14 (2026-10-19)
  - [x] red-first of the two new tests against `8cdf468`'s `Text.java` and `Money.java` — observed in this session before each fix, with the tests added on top of `8cdf468`: Task 11 `expected: 400 but was: 201`, Task 12 `expected: 400 but was: 201` (sections above)
- Gate 3 (2026-10-19, same separate agent, read-only, `deb122f` to the working tree, 41 files +1538/−12): **SHIP**, confidence medium-high, no condition. Evidence: change context leads as gate 2, none confirmed; `detect_test_tampering.sh` none; `./mvnw -B jacoco:prepare-agent test jacoco:report` → `Tests run: 22, Failures: 0, Errors: 0, Skipped: 0`, BUILD SUCCESS; `diff_coverage.py` → 150/151 (99.3 %), the miss `IssueInvoiceUseCase.java:42` unreachable; `Money.KNOWN` same answer for every input, matches BR-9; red-first judged sufficient from the records of Tasks 11 and 12 and the code of `8cdf468`. Gate-2 finding 1: not a defect under the owners' ruling, residual tracked as the spec's open question (architect); finding 2 closed. Finding, Low, optional: `CommerceApplicationTests` recreates `./.local/h2` on every `./mvnw test` (Task 15 result corrected above); fix offered: the in-memory override of `InvoiceApiTest` on that class — not applied, the owner's call. Not verified: red-first re-run by the gate, JDK probe, engines other than H2, load with many lines, external consumers, the content of the recreated file (inferred from the test log)
- Close step 6: gate 3 covers every write of the plan, Tasks 10–15, the spec, the rule and the code included. Levels: the dropped-currency open question is in the spec; no `AR-` / `INV-` entry. README unaffected. C4 unchanged (architect)
- [x] 2026-10-19

## Task 14 — The known currency codes read once

- Scope: `src/main/java/com/commerce/invoice/domain/Money.java` — gate 2, finding 2; no behaviour change
- Verification: `./mvnw test`
- Expected: `Tests run: 22, Failures: 0`, as before
- Decisions that bind it: BR-9 as corrected by the PO (the JDK's list, without X codes)
- Done: `KNOWN`, a `static final` unmodifiable set of the JDK's codes not starting with `X`, built once when the class loads; `isKnown` reads it
- Result: `./mvnw test` → `Tests run: 22, Failures: 0, Errors: 0, Skipped: 0`, BUILD SUCCESS — as expected
- [x] 2026-10-19

## Task 15 — Reading a stored invoice that today's rules refuse

- Scope: clear `./.local/h2`; add to the spec's Open questions "a currency a later JDK drops" (BR-9, BR-15) through `partition-spec`; no code change
- Verification: `ls -a .local`, then read the spec's Open questions; `./mvnw test`
- Expected: no `h2` left under `.local`; the open question present; `Tests run: 22, Failures: 0`
- Decisions that bind it: 200 with the invoice as issued (PO); no design change in this version, the dropped-currency case an open question (architect); clear `./.local/h2` (TL)
- Result: `.local/h2` held one file, `commerce.mv.db`; removed; `ls -a .local` → `prompts`, `runs` only. Correction (gate 3): the `./mvnw test` that followed recreated `.local/h2/commerce.mv.db` (`ls -la .local/h2` → 24576 bytes, 18:58) — `CommerceApplicationTests` boots on the main `application.yaml`, which points to the file database, and Flyway migrates it to V1; the invoice tests run on `jdbc:h2:mem:invoice` and `contextLoads` issues nothing, so the file holds the schema and no invoice: the architect's premise holds. Spec Open questions: "A currency a later JDK drops (BR-9…) … How is it read once the JDK is upgraded? Owner: architect." BR-15 unchanged: it already says "200 with the invoice", which the PO confirmed. `./mvnw test` → `Tests run: 22, Failures: 0, Errors: 0, Skipped: 0`, BUILD SUCCESS — as expected
- [x] 2026-10-19

## Task 10 — The spec of `invoice` in step with the answers of 2026-10-19

- Scope: `docs/specs/billing/invoice/spec.md` — Language (Blank, Character), BR-9, the two Open questions of the gate — through `partition-spec`
- Verification: read BR-9, BR-11, BR-12, Blank and Open questions in the spec against the four answers
- Expected: Blank names the non-breaking spaces as white space; a character is a Unicode code point; BR-9 says a known code is a code of the JDK's `Currency` list not starting with `X` (PO's correction); both gate questions gone from Open questions
- Decisions that bind it: blank includes non-breaking spaces (PO, BA); known = JDK list minus X codes (PO, corrected); 255 characters = code points (architect)
- Result: Language `Blank` names U+00A0, U+2007, U+202F as white space; new `Character` = a Unicode code point; BR-9 "a code of the JDK's `Currency` list that does not start with `X`… fund codes and the withdrawn codes it still lists are known in this version", trigger names `XXX`, `XTS`, `XAU`, `XDR`; both gate questions removed from Open questions — as expected. Validated by PO, BA, architect, 2026-10-19
- [x] 2026-10-19

## Task 11 — A text of only non-breaking spaces is blank

- Scope: `src/main/java/com/commerce/invoice/domain/Text.java`, a case in `IssueInvoiceTest` (BR-11, BR-12) — test first
- Verification: `./mvnw -q test`
- Expected: red first on the new case (201 instead of 400); then all tests pass
- Decisions that bind it: blank includes non-breaking spaces (U+00A0, U+2007, U+202F) (PO, BA)
- Red first: `refuses_a_text_of_only_non_breaking_spaces_as_blank` (orderId, customerId, label of `   `) → `expected: 400 but was: 201`, `Tests run: 13, Failures: 1`. Fix: `Text.isBlank` takes every code point that is `Character.isWhitespace` or `Character.isSpaceChar` (the Zs, Zl, Zp categories, non-breaking spaces included) instead of `String.isBlank`
- Result: `./mvnw test` → `Tests run: 20, Failures: 0, Errors: 0, Skipped: 0`, BUILD SUCCESS — as expected
- [x] 2026-10-19

## Task 12 — A currency starting with `X` is not known

- Scope: `src/main/java/com/commerce/invoice/domain/Money.java`, cases in `IssueInvoiceTest` (BR-9) — test first
- Verification: `./mvnw -q test`
- Expected: red first on `XXX`, `XTS`, `XAU`, `XDR` (201 instead of 400); a fund code (`BOV`) and a withdrawn code the JDK lists accepted; then all tests pass
- Decisions that bind it: known = a code of the JDK's `Currency` list not starting with `X`; fund codes and withdrawn codes the JDK lists accepted; the list updated with the JDK (PO, corrected 2026-10-19)
- Red first: `refuses_a_currency_starting_with_x` → `expected: 400 but was: 201` on `XXX`; `accepts_a_fund_code_and_a_withdrawn_code_the_jdk_lists` (`BOV`, `DEM`) already green; `Tests run: 15, Failures: 1`. Fix: `Money.isKnown` refuses a code starting with `X` before looking in `Currency.getAvailableCurrencies()`
- Result: `./mvnw test` → `Tests run: 22, Failures: 0, Errors: 0, Skipped: 0`, BUILD SUCCESS — as expected
- [x] 2026-10-19

## Task 13 — The rule line on JSON coercion

- Scope: `.apm/instructions/backend-api-contract.instructions.md` — through `partition-rules`, then `apm install`
- Verification: `apm install`, then read `.claude/rules/backend-api-contract.md`
- Expected: the deployed copy carries "A JSON value of the wrong type is refused, never coerced (`com.commerce.JsonConfiguration`)"
- Decisions that bind it: the TL's acceptance, 2026-10-19
- Written 2026-10-19, uncommitted: the line added to `.apm/instructions/backend-api-contract.instructions.md` (`applyTo` already holds `src/main/java/**`, where `JsonConfiguration` binds the whole application; no near-duplicate). `apm install` run by the TL, 2026-10-19
- Result: `.claude/rules/backend-api-contract.md` carries "A JSON value of the wrong type is refused, never coerced (`com.commerce.JsonConfiguration`) (TL, 2026-10-19)", scope `docs/contracts/**`, `target/generated-sources/openapi/**`, `src/main/java/**` (read 2026-10-19) — as expected
- [x] 2026-10-19

## Decisions

- 2026-10-19 · Plans of this feature live in `docs/plans/` · `paths.plans` in `docs/partition.yml` and rule 9 of `.claude/CLAUDE.md` name it · rejected: `docs/superpowers/plans/` (the owner's global instructions, which disagree).
- 2026-10-19 · Reading an invoice is a requirement: anyone holding its identifier, as `getInvoice` shows it; who may read stays open · PO · rejected: waiting for a new version of the source.
- 2026-10-19 · The accountant's action is how REQ-1's issue happens for now; an automatic issue waits for an order context · PO · `docs/sources/billing.md` 1.0 still reads "When an order is confirmed, an invoice is issued": gap reported, the source unamended.
- 2026-10-19 · "Issued": recorded by the issue, with an identifier of its own; nothing sent; no state before it; never modified after · BA.
- 2026-10-19 · The order, its customer and its items have no bounded context yet; the request carries `orderId`, `customerId` and the lines · architect.
- 2026-10-19 · A line: a label and a price; no quantity, no tax · PO.
- 2026-10-19 · A second issue for the same order: 409, description only, no body (BR-1); two at the same moment: the database refuses the second, 409 · PO, architect.
- 2026-10-19 · The identifier of an invoice: a UUID made by `invoice` when it issues · architect.
- 2026-10-19 · No login in this version; anyone can issue, and issue first; the H2 console: accepted for this version, open questions, the console the TL's · PO, TL.
- 2026-10-19 · Lines in different currencies, a negative price, a sum beyond the range of the amount, an unknown currency code: 400, no body; a price of 0 is allowed · PO.
- 2026-10-19 · A request the contract refuses, a blank value, a label over 255 characters: 400, no body; the 400 comes before BR-1 · architect.
- 2026-10-19 · `orderId`, `customerId`: non-blank text of at most 255 characters; blank: empty or only white space · PO.
- 2026-10-19 · An identifier that is not a UUID: 404, like any identifier naming no invoice · architect.
- 2026-10-19 · What the contract does not declare (400, 255, blank), the code enforces; the contract stays 1.0.0 · architect.
- 2026-10-19 · Every amount stored in minor units: `BIGINT` with a three-letter ISO 4217 code, never a decimal · architect · goes to a rule of code (Task 5).
- 2026-10-19 · An amount column is `<what>_minor`, its currency `<what>_currency`: a convention of code, not a business rule · TL · Task 5.
- 2026-10-19 · The Spring configuration is YAML (`application.yaml`, the tests' included), never `.properties` · architect · Task 5.
- 2026-10-19 · The database is reached through `JdbcClient` · TL.
- 2026-10-19 · Logs carry identifiers only, nothing of a line or a price · TL · Task 5.
- 2026-10-19 · The HTTP API is generated by `openapi-generator-maven-plugin` 7.26.0 with the TL's configuration; `spring-boot-starter-validation` from Spring Boot; ArchUnit 1.5.1, test scope · TL.
- 2026-10-19 · No invariant register yet; `docs/specs/invariants.md` is created with its first admitted invariant · architect.
- 2026-10-19 · The design of the issue is the dynamic view `issue-invoice` of the C4 model · architect.
- 2026-10-19 · The spec diff is validated as written; BR-1 held by a unique key on the order, the invoice and its lines written in one transaction · PO, BA, architect.
- 2026-10-19 · Order, Confirmed, Customer, Accountant enter the Language as the BA worded them · BA.
- 2026-10-19 · At least one line, no maximum in this version (BR-16) · PO.
- 2026-10-19 · Lines are read back in the order they were issued (BR-17) · PO.
- 2026-10-19 · The log rule (identifiers only) is scoped to `invoice` (`backend-security-invoice`), not to all of `src/main/**` · the TL stated it for this feature · rejected: widening `backend-security`; the TL widens it if it holds for every context.
- 2026-10-19 · "At most 255 characters" (BR-11, BR-12) counts Unicode code points; the text columns are `VARCHAR(510)` because H2 counts UTF-16 units · an emoji is one character to a person, and a column narrower than the rule turned an accepted label into a 500 whose log line carried the label · rejected: counting UTF-16 units (an emoji would count 2). Confirmed by the architect, 2026-10-19.
- 2026-10-19 · A JSON value of the wrong type is refused, never coerced (`1.5`, `"100"` for an integer; `42` for a string), in `com.commerce.JsonConfiguration`, for the whole application · the contract types them, and a request the contract refuses is a 400 (architect); Jackson truncated `1.5` to `1` · rejected: two YAML keys (they cannot refuse a number for a string, and would split the setting in two places).
- 2026-10-19 · The amount of an invoice is computed from its lines, not stored; prices are stored as `price_minor` / `price_currency` · BR-2 makes it derived, and an issued invoice is never modified · rejected: an `amount_minor` column that could disagree with the lines.
- 2026-10-19 · `InvoiceStatus` holds `ISSUED` only · this version issues only; P-1 adds a status with its move · rejected: declaring `PAID`, `CANCELLED` ahead of their rules.
- 2026-10-19 · The API tests run through a real server (`RANDOM_PORT`, `java.net.http`) on an in-memory H2 set by `@InvoiceApiTest` · the main `application.yaml` points to the file database `./.local/h2`; a test `application.yaml` would shadow the main one · rejected: MockMvc (it would hide what the servlet container adds to a refusal).
- 2026-10-19 · The amount rule goes to `backend-database`, scoped to migrations, beside the column names · it says how an amount is stored · rejected: the spec (not a business rule) and the invariant register (none yet, architect).
- 2026-10-19 · Blank (BR-11, BR-12) is empty or only white space, and a non-breaking space (U+00A0, U+2007, U+202F) is white space: a text of only non-breaking spaces is blank, refused with 400 · PO, BA · settles the gate finding on `Text.java` · rejected: Java's `isBlank` (it takes non-breaking spaces as non-blank). Binds Tasks 10, 11.
- 2026-10-19 · A known currency (BR-9) is an active ISO 4217 currency: `XXX`, `XTS` and the other special or withdrawn codes are refused with 400 · PO · settles the gate finding on `Money.java` · rejected: any code the JDK lists. Binds Tasks 10, 12. Superseded the same day (next line).
- 2026-10-19 · Correction of the PO, replacing "active ISO 4217": a known currency (BR-9) is a code of the JDK's `Currency` list that does not start with `X`; the X codes (`XXX`, `XTS`, `XAU`, `XDR`, …) refused with 400; fund codes accepted; a withdrawn code the JDK still lists accepted in this version; the list is the JDK's, updated with the JDK · PO, to keep it simple for this version (the JDK carries no active flag, an explicit list would need its own upkeep) · rejected: an explicit list of active ISO 4217 currencies. Binds Tasks 10, 12.
- 2026-10-19 · The spec diff (Blank, Character, BR-9 as corrected, Open questions) is validated · PO, BA, architect.
- 2026-10-19 · Gate 2's conditions: finding 2 fixed as Task 14 (same behaviour); finding 1 made Task 15 and a blocker, not fixed by the assistant · restoring without re-checking changes how the rich domain builds `Money` and `Line` (records that always check), a design the architect owns, and BR-15's effect on such an invoice is the PO's · rejected: a trusted-restore path written without the owners. Red-first condition met by the runs of Tasks 11 and 12.
- 2026-10-19 · The plan closes on gate 3 (SHIP); its optional finding (`CommerceApplicationTests` on the file database) is left to the owner, not applied · it is outside this plan's tasks and not a condition · rejected: changing the test class without the owner.
- 2026-10-19 · Reading an invoice gives 200 with the invoice as issued (BR-3, BR-15) · PO · binds Task 15.
- 2026-10-19 · No design change in this version: once `./.local/h2` is cleared, every stored invoice was written through today's checks; a currency a later JDK drops stays an open question of the spec · architect · rejected: a restore path that skips the checks of `Money` and `Line`. Binds Task 15.
- 2026-10-19 · `./.local/h2` (local data) is cleared · TL · binds Task 15.
- 2026-10-19 · Rule line accepted: "A JSON value of the wrong type is refused, never coerced (`com.commerce.JsonConfiguration`)", in `backend-api-contract` · TL · binds Task 13.
- 2026-10-19 · Whoever holds an identifier (the reader of `getInvoice`) is not drawn in the C4 model in this version; the model keeps the accountant · architect · rejected: a new person element. Task 9's C4 step changes nothing.
- 2026-10-19 · The four answers become Tasks 10 to 13 before the close (Task 9) resumes: spec first, then code, then the rule · `partition.md` _Truth_ (spec before code) and `partition-plan` _Close_ (a condition becomes a task).

## Blockers


