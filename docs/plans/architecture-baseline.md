# Architecture baseline — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-19

## Tasks

- [x] Task 1 — 2026-10-19 — C4 model from `docs/architecture/commerce.drawio`
- [x] Task 2 — 2026-10-19 — Form and layer map of the `invoice` hexagon
- [x] Task 3 — 2026-10-19 — Ruling on how the status of an invoice is coded

## Goal

Set the architecture baseline of partition-commerce before any code of `invoice` is written: the C4 model as text in `docs/architecture/model.yml`, transformed once from the hand-drawn `docs/architecture/commerce.drawio` and generated back as `docs/architecture/c4.drawio` (procedure: `partition-c4`); the form (thin or rich), its reason and the layer map of the `invoice` hexagon in `docs/partition.yml` (procedure: `partition-hexagon`); and a ruling, in `docs/architecture/patterns-grid.md`, on how the status of an invoice is coded (procedure: `partition-patterns`). It implements the durable spec `docs/specs/billing/invoice/spec.md` and writes no production code.

## Task 1 — C4 model from `docs/architecture/commerce.drawio`

- [x] 2026-10-19 — `docs/architecture/model.yml` written, `docs/architecture/c4.drawio` generated.
- Scope: `docs/architecture/model.yml` (new), `docs/architecture/c4.drawio` (generated). `docs/architecture/commerce.drawio` is read once, not changed.
- What the drawing shows: people `Customer` (pays) and `Accountant` (issues, cancels) → box `Commerce app (Spring Boot)`; inside it, box `Invoicing`; `Invoicing` → `H2` (cylinder, "SQL"); `Invoicing` → `CardPay` (dashed, "charges, reverses").
- Verification: `node .claude/skills/partition-c4/scripts/c4-drawio.mjs --check && node .claude/skills/partition-c4/scripts/c4-drawio.mjs`
- Expected: `--check` reports no error; the second run writes `docs/architecture/c4.drawio` with one page per view declared in the model, and the relations pays, issues/cancels, SQL and charges/reverses appear, lifted, in the context view.
- Result (2026-10-19): `docs/architecture/model.yml: 7 elements, 4 relations, 3 views, no error` (exit 0); generation exit 0, pages "C4 1 - Context" (4 elements), "C4 2 - Containers" (6), "C4 3 - Components of Commerce app" (4). The context page carries pays, issues/cancels and charges/reverses; SQL is not on it, against the expected: H2 is a container inside `commerce` (architect, answer c), so SQL is internal to the system and is drawn on the containers and components pages only. The expectation was written before that answer; the result is the correct C4 lifting.
- Decisions that bind it: 2026-10-19 · kinds and parents of the drawing (architect), see `## Decisions`.

## Task 2 — Form and layer map of the `invoice` hexagon

- Scope: `docs/partition.yml`, entry `id: invoice` of `contexts:` (`hexagons:` with `id`, `form`, `reason`, `layers`, `allowed`).
- Reading of `Business rules` of the spec against the table of `partition-hexagon` (no banner on the section): an invoice moves between issued, paid and cancelled, and some moves are forbidden (BR-4: cancelled only before any payment; BR-6: a paid invoice cancelled only within 24 hours, then unpaid again) — a `rich` row. The owner chooses the form.
- Layer map: written with the owner from `.apm/instructions/backend-architecture.instructions.md` (a controller never calls a repository; the domain does not import Spring), under `src/main/java/com/commerce/invoice/**`; not inferred from folders (none exist yet).
- Verification: `grep -n -A20 "id: invoice" docs/partition.yml`
- Expected: the `invoice` entry carries one hexagon with a `form`, a non-empty `reason`, a `layers` map whose paths are under `src/main/java/com/commerce/invoice/`, and an `allowed` list in which no layer depends on `inbound` or `outbound`, and, if rich, no line starts with `domain ->` (the domain depends on nothing).
- [x] 2026-10-19 — `hexagons:` entry written under `id: invoice` in `docs/partition.yml`, as given by the architect.
- Result (2026-10-19): lines 29–42 of `docs/partition.yml` show `id: invoice`, `form: rich`, reason "an invoice moves from issued to paid or cancelled under BR-3 to BR-6, and some transitions are forbidden", layers `domain`, `application`, `inbound`, `outbound` under `src/main/java/com/commerce/invoice/`, allowed `application -> domain`, `inbound -> application`, `outbound -> application`, `outbound -> domain`. No layer depends on `inbound` or `outbound`; no line starts with `domain ->`. As expected.
- Decisions that bind it: 2026-10-19 · form rich and layer map (architect), see `## Decisions`.

## Task 3 — Ruling on how the status of an invoice is coded

- Scope: `docs/architecture/patterns-grid.md` (new, from `.claude/skills/partition-patterns/assets/patterns-grid.md`, created when the ruling is made); `## Decisions` of this plan.
- Procedure: `partition-patterns`, _At design time_: problem in one sentence without a pattern, candidates including **no pattern**, every criterion of the grid answered with facts of this code base, recommendation; the owner rules.
- Verification: `grep -n -i "status" docs/architecture/patterns-grid.md`
- Expected: one ruling row for the status of an invoice in `invoice`, with a ruling, one cell per criterion, a reason and the date; the ruling names what would change its mind; the candidates that lost are listed in `## Decisions`.
- Decisions that bind it: 2026-10-19 · form rich (Task 2): the status lives in the domain layer `src/main/java/com/commerce/invoice/domain/**`, which imports nothing from outside itself (no Spring, no Lombok, no Jakarta). 2026-10-19 (architect): rule on BR-1 to BR-6 as confirmed in the spec; the gaps of `Open questions` stay open questions of the spec, and the ruling names them as what would change its mind.
- [x] 2026-10-19 — analysis set out below, for the architect to rule. Grid not yet created: `partition-patterns` creates it when a ruling is made.
- [x] 2026-10-19 — architect ruled A, `no pattern`; ruling P-1 written to `docs/architecture/patterns-grid.md`; verification run.
- Result (2026-10-19): `grep -n -i "status"` finds line 23, ruling P-1, where `invoice` / status of an invoice, ruling `no pattern`, one cell per criterion (need now, budget, cost of change, testability, security), a reason naming what would rule it again (State), date 2026-10-19; the candidates that lost (State, enum transition table) are in `## Decisions`. As expected.

### Analysis (2026-10-19)

**Problem.** An invoice's status changes only through what BR-3 to BR-6 allow, and a forbidden change must be refused by the invoice itself, whatever use case asks for it.

**What the confirmed rules give.** Statuses named by the spec: issued, paid, cancelled. Moves: an invoice is created issued, once per order (BR-1); issued → paid once its amount is paid in full (BR-5); issued → cancelled only before any payment, at no fee (BR-4); paid → "unpaid again" only within 24 hours of being paid (BR-6), which needs the moment of payment and the current time. Forbidden: any change to the content of an issued invoice (BR-3); cancelling after a payment (BR-4); cancelling a paid invoice after 24 hours (BR-6). Not given, left open: which status "unpaid again" is, and what it allows next; whether "cancelled" names one status or two; whether a partly paid status exists; whether cancelling a paid invoice carries a fee.

**Candidates.**

- **A — no pattern.** An enum `issued | paid | cancelled` held by the invoice aggregate; one method per command (pay, cancel, cancel a paid invoice) checks the current status and its guard, then sets the next status, or refuses with a domain exception. No status setter. The current time comes in as an argument.
- **B — State (GoF).** One class per status behind a common interface carrying the three commands; each returns the next state or refuses.
- **C — enum with a transition table.** The enum declares which status may follow which; the aggregate asks the table, then checks the guards (before any payment, paid in full, within 24 hours) itself.
- Out of the running: a state-machine library (e.g. Spring Statemachine). The domain imports nothing from outside itself (architect, 2026-10-19).

| Criterion | A — no pattern | B — State | C — transition table |
| --- | --- | --- | --- |
| Need now | Three statuses, three commands, four moves: one class and one enum hold them. | Pays off when behaviour differs per status beyond a guard; no such rule exists today: of nine state × command bodies, six only refuse. | The table holds which moves exist but none of the guards (BR-4, BR-5, BR-6), so each rule is split in two places. |
| Budget | One aggregate and one enum to read for any status change. | Interface plus three state classes plus the aggregate: five files read for one change. | Enum and aggregate, read together to know whether a move is allowed. |
| Cost of change | A new status or move (open questions) touches the enum and the methods it reaches in the aggregate: two files. | A new status adds a class; a new command touches the interface and every state class. | A new move touches the table and, when guarded, the aggregate: two files, the rule split across them. |
| Testability | Plain JUnit 5 and AssertJ on the aggregate, no Spring, no database; BR-6 tested with the time as an argument. | Same, plus each state class; the refusals are tested once per class. | Same as A, plus the table; a test of the table alone proves nothing about the guards. |
| Security | Every change of status goes through the aggregate's methods; there is no other path. | Also centralised, through the aggregate holding the state. | A move allowed by the table and a guard that is forgotten opens a path around BR-4 or BR-6. |

**Recommendation: A, ruling `no pattern`.** The plain enum and guarded methods hold every confirmed rule in one place, read the least, and leave no path around a guard.

**What would change my mind** (all open questions of the spec, owned by the PO): a partly paid status (partial payment); "unpaid again" becoming its own status with its own moves; "cancelled" splitting into two statuses; a fee or another behaviour that differs by status. When those make the aggregate's methods branch on the status in more than one place each, B is to be ruled again.

## Closing

### Gate 1 (2026-10-19) — verdict: SHIP, confidence High

Target: 53cce6e to the working tree — `docs/partition.yml` modified (+14, the `hexagons:` entry of `invoice`); untracked: `docs/architecture/commerce.drawio`, `model.yml`, `c4.drawio`, `patterns-grid.md`, this plan. No file under `src/` changed; one bounded context (`invoice`), no contract between two.

| Check | Command | Result |
| --- | --- | --- |
| Change context | `collect_change_context.sh --range 53cce6e` | 1 tracked file changed, 5 untracked; no deletion or rename; one heuristic lead ("auth / permission logic" on the `reason:` line, matched on "forbidden"), read: a sentence of configuration, not logic — dismissed |
| Test tampering | `detect_test_tampering.sh --range 53cce6e` | none in every category |
| Config consumer | `node .claude/skills/partition-budget/scripts/budget.mjs --check` | exit 0; `invoice` 2 997 tokens always-loaded, 9 % of 32 000, ok; this plan read on resume ok |
| C4 model | `node .claude/skills/partition-c4/scripts/c4-drawio.mjs --check` | `7 elements, 4 relations, 3 views, no error`, exit 0 |
| Build and tests | `./mvnw -q -o test` | exit 0; `CommerceApplicationTests`: 1 test, 0 failures, 0 errors, 0 skipped |
| Diff coverage | — | not applicable: no line of code changed |

Spec oracle: `hexagons:` reason, ruling P-1 and the model state nothing that contradicts BR-1 to BR-6 of `docs/specs/billing/invoice/spec.md`; P-1 settles none of its open questions and names them as what would rule it again. Regression classes skipped, none applies to a change with no code: behavioural, data and persistence, concurrency, performance, error handling, security, frontend, async; config: `docs/partition.yml` is read by the Partition scripts only, checked above.

Findings: [Low] `docs/architecture/commerce.drawio` stays beside the generated `c4.drawio` and no longer is the source (`partition-c4`: the model is) — a person editing it would change nothing the model knows; the owner keeps it as the record of the drawing or deletes it.

Not verified: the rendering of `c4.drawio` in draw.io (not opened); SnakeYAML on the test classpath (decision of the TL, for the plan that writes the ArchUnit test); readers of `docs/partition.yml` outside this repository (none known).

### Statements established, and where they went (2026-10-19)

| Statement | Level | Where it is |
| --- | --- | --- |
| C4 elements, relations and views of `commerce` | C4 model | `docs/architecture/model.yml`, generated to `docs/architecture/c4.drawio` (Task 1); no further C4 change |
| `invoice` hexagon rich, its layer map | configuration | `docs/partition.yml`, `contexts` › `invoice` › `hexagons` (Task 2) |
| Status of an invoice coded with no pattern | pattern grid | `docs/architecture/patterns-grid.md`, P-1 (Task 3) |
| `invoice/domain` imports nothing from outside itself (no Spring, Lombok, Jakarta) | rule, scoped to the code it binds | new `.apm/instructions/backend-architecture-invoice.instructions.md`, `applyTo: src/main/java/com/commerce/invoice/domain/**`. `backend-architecture.instructions.md` ("The domain does not import Spring", `src/main/java/**`) holds only the Spring part, for every domain; not merged into that wider file. The scope matches no file yet: it loads from the first class of `invoice/domain` |
| "One test class per use case" leaves the architecture test class outside | rule | `.apm/instructions/backend-testing.instructions.md`, its line rewritten |
| The architecture test: ArchUnit 1.5.1, layer maps read from `docs/partition.yml` with SnakeYAML, an empty rule passes | rule | `.apm/instructions/backend-testing.instructions.md`, one line added; scoped `src/test/java/**` because the class has no name yet (choice to confirm by the owner) |
| ArchUnit test written in the first plan for an `invoice` behaviour | plan of work | stays in `## Decisions` here; carried by the rule line above to that plan |

Durable spec: nothing moves; no statement of this plan is a business rule. Area file: 0 `AR-` added or changed. Invariant register: 0 `INV-` added or changed. Candidates rejected: P-1 (binds `invoice` only, held by `docs/architecture/patterns-grid.md` P-1); the domain import rule (how code is written, binds `invoice/domain` only, held by `backend-architecture-invoice.instructions.md`).

README unaffected: nothing users install, run or configure changed (no command, option, application configuration key or API).

### Gate 2 (2026-10-19) — verdict: SHIP WITH CONDITIONS, confidence Medium

Target: 53cce6e to the working tree, the writes of the closing included — `.apm/instructions/backend-testing.instructions.md` (+2/−1) and `docs/partition.yml` (+14) modified; untracked: `.apm/instructions/backend-architecture-invoice.instructions.md` and the files of gate 1.

| Check | Command | Result |
| --- | --- | --- |
| Change context | `collect_change_context.sh --range 53cce6e` | 2 tracked files changed, 6 untracked; no deletion or rename; same dismissed lead as gate 1 |
| Test tampering | `detect_test_tampering.sh --range 53cce6e` | no hit in any category |
| Config consumer and rule scopes | `node .claude/skills/partition-budget/scripts/budget.mjs --check` | exit 0; no rule listed without a scope; `invoice` scoped rules 0, unchanged, because the new and changed rules are not deployed yet |
| Deployment of the rules | `apm install` | not run: the command needs the owner's approval in this session |
| Build and tests | — | not rerun: no file under `src/` or `pom.xml` changed since gate 1 (`./mvnw -q -o test`, 1 test, 0 failures) |

Findings: gate 1's [Low] on `commerce.drawio` stands. No other.

Not verified: the deployed copies under `.claude/rules/` (until `apm install` runs); the rest as gate 1.

Conditions:

- [x] 2026-10-19 — Run `apm install` (run by the TL), then `node .claude/skills/partition-budget/scripts/budget.mjs --check`: expected `.claude/rules/backend-architecture-invoice.md` present, `.claude/rules/backend-testing.md` carrying the two lines, no rule without a scope — owner: Elie NEHME.
  - Result (2026-10-19): `.claude/rules/` lists `backend-architecture-invoice.md` (`paths: src/main/java/com/commerce/invoice/domain/**`, the domain import line) and `backend-testing.md` carrying the two lines; `budget.mjs --check` exit 0, no rule listed without a scope, `invoice` 2 997 tokens always-loaded (9 % of 32 000), scoped rules 0 because no file exists yet under the new scope. As expected. The plan closes on gate 2, which covers every change of the plan; this closing writes only the plan.

## Decisions

<!-- One line each: date · decision · reason · what was rejected. -->

- 2026-10-19 · Plan opened in `docs/plans/` · `paths.plans` in `docs/partition.yml` declares it and `.claude/CLAUDE.md` rule 9 agrees · rejected: `docs/superpowers/plans/`, named by the owner's global `~/.claude/CLAUDE.md` (disagreement flagged to the owner).
- 2026-10-19 · C4 kinds and parents (architect): `commerce` is a system; `Commerce app` a container of it, technology Spring Boot 4.1, jar (consistent with `spring-boot-starter-parent` 4.1.1 in `pom.xml`); `Invoicing` a component of `Commerce app`, bounded context `invoice`; `H2` a container of `commerce`, the database; `CardPay` an external system · the drawing left them open · rejected: H2 as an external system.
- 2026-10-19 · Views (architect): context and containers of `commerce`, components of `Commerce app` · no dynamic view asked.
- 2026-10-19 · Relations kept at the level drawn: Customer and Accountant → `Commerce app` (container), not → `Invoicing` · the drawing points them at the container; moving them to the component would be a guess.
- 2026-10-19 · Task 3 (architect): rule on the business rules as confirmed; the gaps stay open questions of the spec and the ruling says what would change its mind · rejected: waiting for the PO to settle the open questions before ruling. The open questions stay in the spec, owned by the PO, and still block any task that implements what they reach.
- 2026-10-19 · `invoice` hexagon is rich, with layers `domain`, `application`, `inbound`, `outbound` under `src/main/java/com/commerce/invoice/` and allowed `application -> domain`, `inbound -> application`, `outbound -> application`, `outbound -> domain` (architect) · an invoice moves from issued to paid or cancelled under BR-3 to BR-6, with forbidden transitions · rejected: thin. Inbound reaches the domain only through the application; the domain depends on nothing.
- 2026-10-19 · The dependency check that fails the build is ArchUnit (architect) · Java project, the tool `partition-hexagon` names for the ecosystem; the project had no such check.
- 2026-10-19 · The ArchUnit test is written with the first classes, in the first plan for an `invoice` behaviour, not in this plan (architect) · no class exists yet under `com.commerce.invoice` · rejected: Task 4 of this plan, removed.
- 2026-10-19 · ArchUnit dependency `com.tngtech.archunit:archunit-junit5`, test scope, version `1.5.1` (TL) · Spring Boot does not manage its version.
- 2026-10-19 · `invoice/domain` imports nothing from outside itself: no Lombok, no Spring, no Jakarta annotation; no framework annotation accepted (architect) · rich hexagon, domain independent of any framework.
- 2026-10-19 · The architecture test reads the layer map from `docs/partition.yml` at test time, parsed with SnakeYAML (TL says it is on the classpath through the Spring Boot starters; to be checked in that plan) · the map stays the only source; adding a hexagon needs no test change · rejected: rules written by hand from the map.
- 2026-10-19 · A rule that matches no class passes: `allowEmptyShould(true)` on each rule, or `archRule.failOnEmptyShould=false` in `archunit.properties` (TL) · layers have no class at first · the choice between the two is left to that plan.
- 2026-10-19 · "One test class per use case" (`.apm/instructions/backend-testing.instructions.md`) applies to tests of use cases; the single architecture test class sits outside it (TL) · the architecture test tests no use case · at closing, carried to the rule file through `partition-rules`, with the domain import rule above.

- 2026-10-19 · Status of an invoice: ruling P-1, `no pattern` — an enum held by the invoice aggregate, one guarded method per command, no status setter, current time as an argument (architect) · the confirmed rules fit in one place, least to read, no path around a guard · rejected: State (GoF), enum transition table, a state-machine library. Ruled again if the open questions bring a partly paid status, an "unpaid again" status, two "cancelled" statuses or a behaviour differing by status.

- 2026-10-19 · The ArchUnit rule line goes to `backend-testing.instructions.md` (`src/test/java/**`) · the architecture test class has no name yet, so no narrower scope matches it · rejected: a file scoped to that class, until it exists. To confirm by the owner.
- 2026-10-19 · The `invoice/domain` import rule goes to a new file scoped `src/main/java/com/commerce/invoice/domain/**` · it binds one bounded context; `partition-rules` forbids merging it into the file scoped `src/main/java/**` · rejected: rewriting the project-wide "The domain does not import Spring".

- 2026-10-19 · The ArchUnit rule line stays in `backend-testing.instructions.md` for now (TL) · confirms the placement above · rejected: a file scoped to the architecture test class, which has no name yet.

## Blockers

<!-- What is blocked · what unblocks it · who owns it. Delete the line when it clears. -->
