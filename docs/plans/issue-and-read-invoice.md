# Issue and read an invoice — execution plan

> **Status:** blocked
>
> **Resume at:** Task 9 — once the blockers clear: apply the owners' answers (spec first, then code), finish Task 5, rerun `partition-gate` from `deb122f`, then close
>
> **Last updated:** 2026-10-19 · uncommitted

## Tasks

- [x] Task 1 — 2026-10-19 · uncommitted — The owners settle the attack findings
- [x] Task 2 — 2026-10-19 · deb122f — The contract of `invoice`, the role model and the invariant register
- [x] Task 3 — 2026-10-19 · uncommitted — The spec of `invoice` in step with the answers, and the design of the issue
- [x] Task 4 — 2026-10-19 · uncommitted — Form, layer map and P-1 checked against the spec
- [ ] Task 5 — The rules of code stated on 2026-10-19
- [x] Task 6 — 2026-10-19 · uncommitted — The build: generator, validation, architecture test
- [x] Task 7 — 2026-10-19 · uncommitted — Issue an invoice
- [x] Task 8 — 2026-10-19 · uncommitted — Read an invoice back
- [ ] Task 9 — Close: gate, levels, README, C4

## Goal

Deliver the feature "the accountant issues the invoice of a confirmed order, and anyone can read it back" in the bounded context `invoice`: `issueInvoice` and `getInvoice` of `docs/contracts/billing.openapi.json` 1.0.0, implementing the durable spec `docs/specs/billing/invoice/spec.md` (BR-1, BR-2, BR-7 to BR-15) and the design `docs/designs/issue-and-read-invoice.md`.

## Task 1 — The owners settle the attack findings

- Scope: none of the repository; answers of PO, BA, architect, TL in the conversation of 2026-10-19
- Verification: `grep -n "^[0-9]*\. \*\*" docs/specs/billing/invoice/spec.md`
- Expected: findings 1, 6, 7, 11, 13 only
- Result: `1. **Who may read an invoice.**`, `6. **Read access reaches payment and cancellation.**`, `7. **Who may issue.**`, `11. **Words.**`, `13. **The H2 console**` — as expected
- Decisions that bind it: the answers of 2026-10-19, below
- [x] 2026-10-19 · uncommitted

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
- [x] 2026-10-19 · uncommitted

## Task 4 — Form, layer map and P-1 checked against the spec

- Scope: `docs/partition.yml` (hexagon `invoice`), `docs/architecture/patterns-grid.md` (P-1) — through `partition-hexagon` and `partition-patterns`
- Verification: read the hexagon entry and P-1 against the validated spec
- Expected: form `rich` and its layer map hold; P-1 holds (this feature adds no status and no move: an invoice is created issued)
- Result: `rich` holds — BR-2, BR-7, BR-10 tie the lines, the currency and the amount together at every write (rich row "rules that tie several pieces of data together"); layer map unchanged, the generated API in `com.commerce.invoice.inbound.api` sits in the inbound layer; P-1 holds, none of its re-ruling triggers (partly paid, "unpaid again", two "cancelled", a fee) entered the spec — as expected
- Decisions that bind it: P-1 (2026-10-19, `docs/plans/architecture-baseline.md`); the generated API lands in `com.commerce.invoice.inbound.api` (TL)
- [x] 2026-10-19 · uncommitted

## Task 5 — The rules of code stated on 2026-10-19

- Scope: `.apm/instructions/backend-database.instructions.md` (amount in minor units, column names), a new `.apm/instructions/backend-configuration.instructions.md` scoped to `src/main/resources/**, src/test/resources/**` (YAML only), the log line of `invoice` (identifiers only) in a security rule scoped to `src/main/java/com/commerce/invoice/**` — through `partition-rules`, then `apm install`, then `partition-budget`
- Verification: `apm install`, then `partition-budget`
- Expected: the deployed copies under `.claude/rules/` carry the new lines; the budget report lists no rule "without a scope"
- Decisions that bind it: amounts in minor units (architect); column names (TL); YAML only (architect); identifiers only in logs (TL)

## Task 6 — The build: generator, validation, architecture test

- Scope: `pom.xml`, `src/test/java/com/commerce/**` (architecture test)
- Steps: property `openapi-generator.version` = `7.26.0` and the plugin as the TL gave it; `spring-boot-starter-validation` versioned by Spring Boot; `com.tngtech.archunit:archunit-junit5` 1.5.1, test scope; the architecture test as `backend-testing` states it
- Verification: `./mvnw -q test`
- Expected: BUILD SUCCESS; `IssueInvoice`, `Invoice`, `Line`, `Money` and an `InvoiceApi` interface generated under `target/generated-sources/openapi`, importing no Swagger annotation and no `jackson-databind-nullable`
- Decisions that bind it: the generator configuration (TL); ArchUnit 1.5.1 (TL); layer map of `docs/partition.yml`
- Result: `Tests run: 4, Failures: 0` — BUILD SUCCESS; `InvoicesApi`, `IssueInvoice`, `Invoice`, `Line`, `Money` (and `ApiUtil`) generated, `grep swagger|nullable` empty — as expected (the interface is `InvoicesApi`, from the tag). The test was also run with a domain class importing Spring and an inbound class referencing the domain: both caught (2 failures), the two classes then removed
- [x] 2026-10-19 · uncommitted

## Task 7 — Issue an invoice

- Scope: `src/main/java/com/commerce/invoice/**`, `src/main/resources/db/migration/**`, `src/test/java/com/commerce/invoice/**`
- Steps: test first, one test per rule: BR-1 (409, simultaneous issues included), BR-2, BR-7 to BR-14, BR-13 before BR-1; then the code: domain (invoice, line, money), application (issue), inbound (controller implementing the generated interface), outbound (`JdbcClient`, Flyway migration)
- Verification: `./mvnw -q test`
- Expected: BUILD SUCCESS, every rule test of the issue green, the architecture test green
- Decisions that bind it: P-1; amounts in minor units, `BIGINT` + ISO code, columns `<what>_minor` / `<what>_currency`; `JdbcClient`; UUID made by `invoice`; 400 before BR-1; the database refuses the second simultaneous issue; identifiers only in logs; no login; a character is a code point; strict JSON types; tests on an in-memory database
- Red first: `Tests run: 12, Failures: 11, Errors: 1` (no controller). Then two real failures fixed: `amountMinor: 1.5` was truncated (Jackson coercion → `JsonConfiguration`), a 255-emoji label gave a 500 (H2 counts UTF-16 units → columns of 510)
- Result: `Tests run: 16, Failures: 0, Errors: 0` — BUILD SUCCESS — as expected
- [x] 2026-10-19 · uncommitted

## Task 8 — Read an invoice back

- Scope: as Task 7
- Steps: test first: BR-15 (200; 404 for an unknown identifier and for one that is not a UUID); the read returns no customer; then the code
- Verification: `./mvnw -q test`
- Expected: BUILD SUCCESS, the BR-15 tests green
- Decisions that bind it: anyone holding the identifier reads (PO); 404 for a non-UUID (architect); lines in issue order (BR-17)
- Red first: `Tests run: 3, Failures: 3`. `GetInvoiceUseCase`, `InvoiceNotFound` and the read in `InvoiceController` / `InvoiceErrors` were written in the working tree by the owner between turns; the assistant added the missing import of `InvoiceNotFound` in `InvoiceErrors`. Only the canonical UUID form is read (`1-1-1-1-1` → 404)
- Result: `Tests run: 19, Failures: 0, Errors: 0` — BUILD SUCCESS — as expected
- [x] 2026-10-19 · uncommitted

## Task 9 — Close: gate, levels, README, C4

- Scope: as `partition-plan`, _Close_
- Verification: `partition-gate` from `deb122f` to the working tree
- Expected: SHIP or SHIP WITH CONDITIONS
- Decisions that bind it: none
- Gate 1 (2026-10-19, run by a separate agent, read-only): **SHIP WITH CONDITIONS**, confidence medium-high. Evidence: `./mvnw … jacoco … test` → `Tests run: 19, Failures: 0, Errors: 0, Skipped: 0`, BUILD SUCCESS; new production lines 145 covered / 1 missed (`lines == null`, unreachable behind `@NotNull`), branches 30/38 (defensive null checks); no test tampering. Findings, all Low: non-breaking spaces pass as non-blank (`Text.java:15`); "known" ISO code is the JDK list, `XXX` included (`Money.java:33`); no cap on lines with anonymous callers (within BR-16, residual risk). Not verified: base-vs-change comparison, jshell boundary probes (approval refused), engines other than H2, load beyond two threads, external consumers, deployed rule copies
- Conditions, each a task or a blocker:
  - [ ] `apm install`, then `partition-budget` — Task 5, blocker below
  - [ ] the architect confirms "255 characters = code points, `VARCHAR(510)`" — blocker below
  - [ ] PO, BA settle non-breaking spaces and "known ISO 4217 code" — written to the spec's Open questions; blocker below
  - [x] README realigned with `partition-readme` (2026-10-19 · uncommitted): `## Build` names the generated interfaces, new `## API` lists the two operations, their statuses, no body on refusal, no coercion
- Levels (close steps 2–4), proposed, nothing written yet: the spec already holds every business statement (Scope, Language, BR-1, BR-2, BR-7 to BR-17, the contract row); the rules of code are in `.apm/instructions/` (Task 5); candidate rule line for `backend-api-contract`: "A JSON value of the wrong type is refused, never coerced (`com.commerce.JsonConfiguration`)" — waits for the TL; area file and invariant register: no candidate (one bounded context in the area; no register, architect)
- C4 (close step 5): the model has no element for "anyone holding an identifier" who reads (`getInvoice`); adding one is a question to the architect, not drawn
- Left: when the conditions are met, run the gate again with the README and every later write included (close step 6), then set `Status: done`

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
- 2026-10-19 · "At most 255 characters" (BR-11, BR-12) counts Unicode code points; the text columns are `VARCHAR(510)` because H2 counts UTF-16 units · an emoji is one character to a person, and a column narrower than the rule turned an accepted label into a 500 whose log line carried the label · rejected: counting UTF-16 units (an emoji would count 2). To be confirmed by the architect.
- 2026-10-19 · A JSON value of the wrong type is refused, never coerced (`1.5`, `"100"` for an integer; `42` for a string), in `com.commerce.JsonConfiguration`, for the whole application · the contract types them, and a request the contract refuses is a 400 (architect); Jackson truncated `1.5` to `1` · rejected: two YAML keys (they cannot refuse a number for a string, and would split the setting in two places).
- 2026-10-19 · The amount of an invoice is computed from its lines, not stored; prices are stored as `price_minor` / `price_currency` · BR-2 makes it derived, and an issued invoice is never modified · rejected: an `amount_minor` column that could disagree with the lines.
- 2026-10-19 · `InvoiceStatus` holds `ISSUED` only · this version issues only; P-1 adds a status with its move · rejected: declaring `PAID`, `CANCELLED` ahead of their rules.
- 2026-10-19 · The API tests run through a real server (`RANDOM_PORT`, `java.net.http`) on an in-memory H2 set by `@InvoiceApiTest` · the main `application.yaml` points to the file database `./.local/h2`; a test `application.yaml` would shadow the main one · rejected: MockMvc (it would hide what the servlet container adds to a refusal).
- 2026-10-19 · The amount rule goes to `backend-database`, scoped to migrations, beside the column names · it says how an amount is stored · rejected: the spec (not a business rule) and the invariant register (none yet, architect).

## Blockers

- Task 9 · the architect confirms that "at most 255 characters" counts code points, with `VARCHAR(510)` columns (decision of 2026-10-19 above) · owner: architect
- Task 9 · two gate findings wait in the spec's Open questions: non-breaking spaces as blank; which ISO 4217 codes are "known" · owners: PO, BA
- Task 9 · the TL rules on the candidate rule line on JSON coercion, and the architect on a C4 element for the reader of `getInvoice` · owners: TL, architect
- Task 5 · `apm install` needs the owner's approval in this session (refused to run without it): the new rule lines are written in `.apm/instructions/`, not yet deployed to `.claude/rules/` · owner: Elie NEHME
