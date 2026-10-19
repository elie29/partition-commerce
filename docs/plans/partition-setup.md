# Partition setup — execution plan

> **Status:** done
>
> **Resume at:** — (nothing; every task is checked)
>
> **Last updated:** 2026-10-19

## Tasks

- [x] Task 1 — 2026-10-19 — Configuration of `docs/partition.yml`
- [x] Task 2 — 2026-10-19 — Baseline measure and migration proposal for the rules of `.claude/CLAUDE.md`
- [x] Task 3 — 2026-10-19 — Move the validated rules to scoped rules in `.apm/instructions/`
- [x] Task 4 — 2026-10-19 — First budget assessment record
- [x] Task 5 — 2026-10-19 — README carrying the build command; build command leaves the entry file

## Goal

Set Partition up in partition-commerce: a complete `docs/partition.yml` (procedure: `partition-budget`, _Configuration_), the rules of `.claude/CLAUDE.md` migrated to scoped rule files under `.apm/instructions/` deployed by `apm install` (procedure: `partition-rules`, _Migrate existing rules_), and a first measure of the Context Budget recorded as `docs/budget/YYYY-MM-DD.md` (procedure: `partition-budget`, _The assessment_).

## Task 1 — Configuration of `docs/partition.yml`

- Scope: `docs/partition.yml`
- Steps:
  - [x] 2026-10-19 — Ask the owner the threshold (`budget.always`) and the schedule of the assessment (`budget.review`); keep every other key of `.claude/skills/partition-budget/assets/partition.yml` at its value and with its comment, `paths.plans: docs/plans` kept.
  - [x] 2026-10-19 — Leave `contexts:` empty: no bounded context is declared before `partition-shape` retains one.
  - [x] 2026-10-19 — Run the verification.
- Verification: `node .claude/skills/partition-budget/scripts/budget.mjs`
- Expected: the schema check passes with no unknown key listed, and a Markdown report is printed with the owner's threshold and no bounded context.
- Result (2026-10-19): exit 0, no unknown key listed; "Threshold per bounded context: _Always_ 32 000"; bounded contexts "(none declared)"; every-session total 1 912 tokens.
- Decisions that bind it: 2026-10-19 · plans live in `docs/plans/`; 2026-10-19 · `budget.always: 32000`, `budget.review: quarterly`.

## Task 2 — Baseline measure and migration proposal for the rules of `.claude/CLAUDE.md`

- Scope: read-only on `.claude/CLAUDE.md`, `.claude/rules/`; the proposal is written into this task's section.
- Steps:
  - [x] 2026-10-19 — Run `partition-budget` and record the every-session total and the rules without a scope, with their size.
  - [x] 2026-10-19 — For each rule of `.claude/CLAUDE.md`, propose topic, scope (`applyTo` on real paths) and whether it belongs elsewhere (e.g. the _Build_ section is a command, which `partition-rules` sends to a README; the repository has none yet).
  - [x] 2026-10-19 — Quote what is said twice across the files (e.g. rule 9 vs `paths.plans` in `docs/partition.yml`).
  - [x] 2026-10-19 — Present the proposal and wait for the owner's validation; record it in `## Decisions`.
- Verification: `node .claude/skills/partition-budget/scripts/budget.mjs`
- Expected: the report lists `CLAUDE.md` by name in "Loaded by every session" and the package instruction as `(instruction)`; the every-session total is written here as the baseline.
- Result (2026-10-19): exit 0. Loaded by every session: `.claude/CLAUDE.md` 184 · `.claude/rules/partition.md (instruction)` 621 · 13 skill and agent descriptions 1 107 · **baseline total 1 912**. Rules without a scope: `.claude/CLAUDE.md` only (184 tokens); `.claude/rules/partition.md` is the package instruction, not a rule file, and is not migrated.
- Decisions that bind it: none.

### Migration proposal — validated 2026-10-19

Code today: `src/main/java/com/commerce/CommerceApplication.java`, `src/test/java/com/commerce/CommerceApplicationTests.java`, `src/main/resources/application.yaml`. No `db/migration` folder, no OpenAPI contract, no README.

| CLAUDE.md line | Goes to | `applyTo` | Matches files today |
| --- | --- | --- | --- |
| 1. Constructor injection only; no field injection. | `backend-architecture.instructions.md` | `src/main/java/**` | yes |
| 2. A controller never calls a repository. | `backend-architecture.instructions.md` | `src/main/java/**` | yes |
| 3. One package per bounded context under `com.commerce`. | stays in the entry file (holds everywhere — architect) | — | — |
| 4. The domain does not import Spring. | `backend-architecture.instructions.md` | `src/main/java/**` | yes |
| 5. Every schema change is a new Flyway file `V<n>__<what>.sql`; a migration already merged is never edited. | `backend-database.instructions.md` | `src/main/resources/db/migration/**` | no (loads once the first migration exists — TL kept it) |
| 6. Tests: JUnit 5 and AssertJ; one test class per use case. | `backend-testing.instructions.md` | `src/test/java/**` | yes |
| 7. The HTTP API is generated from its OpenAPI contract; a controller implements the generated interface. | `backend-api-contract.instructions.md` | `docs/contracts/**, target/generated-sources/openapi/**, src/main/java/**` | `src/main/java/**` only |
| 8. Never log a payment card number. | `backend-security.instructions.md` | `src/main/**` (log calls and logging configuration) | yes |
| 9. Plans live in `docs/plans/`, not in `docs/superpowers/plans/`. | stays in the entry file (holds everywhere — TL) | — | — |
| `## Build` — `./mvnw clean install` builds and runs every test. | stays in the entry file until closing, then the README (Task 5) | — | — |

The entry file after migration: the title, "Spring Boot service that sells products from price lists and bills them.", `## Build` (until Task 5), and `## Rules` with lines 3 and 9 only. No pointer line to `.apm/instructions/`.

Said twice: rule 9 ↔ `paths.plans` in `docs/partition.yml` (both kept, owner's choice); rule 9 ↔ `~/.claude/CLAUDE.md` (left as is: outside the plan, the owner aligns their own file).

## Task 3 — Move the validated rules to scoped rules in `.apm/instructions/`

- Scope: `.apm/instructions/*.instructions.md`, `.claude/CLAUDE.md`, the copies `apm install` deploys.
- Steps:
  - [x] 2026-10-19 — Move the text without rewriting it, except headings (no `#`/`##`, sections at `###`), one topic per file, `applyTo` checked to match files. Created `backend-architecture` (lines 1, 2, 4), `backend-database` (5), `backend-testing` (6), `backend-api-contract` (7), `backend-security` (8).
  - [x] 2026-10-19 — Keep the old text until the owner removes it; while both exist the rules load twice. The TL removed lines 1, 2, 4, 5, 6, 7, 8 from `.claude/CLAUDE.md`; lines 3, 9 and `## Build` stay.
  - [x] 2026-10-19 — Run `apm install`, then the verification. The TL ran `apm install`: deployed as `.claude/rules/backend-{architecture,database,testing,api-contract,security}.md`, each with a `paths:` scope matching its `applyTo`.
- Verification: `apm install && node .claude/skills/partition-budget/scripts/budget.mjs`
- Expected: the report lists no rule "without a scope"; the every-session total before (Task 2 baseline) and after are both written here.
- Result (2026-10-19): exit 0. Loaded by every session: `.claude/CLAUDE.md` 74 · `.claude/rules/partition.md (instruction)` 621 · 13 skill and agent descriptions 1 107. No rule without a scope listed. **Every-session total: before 1 912, after 1 802** (−110, all from the entry file: 184 → 74).
- Decisions that bind it: 2026-10-19 · scopes per the validated proposal (Task 2); lines 3 and 9 and `## Build` stay in the entry file; no pointer line.

## Task 4 — First budget assessment record

- Scope: `docs/budget/YYYY-MM-DD.md`, made from `.claude/skills/partition-budget/assets/assessment.md`.
- Steps:
  - [x] 2026-10-19 — Run the script and paste its output unedited into the record of the day. `docs/budget/2026-10-19.md` drafted: output pasted, change since last record ("first record", 1 912 → 1 802), sign 1 from the gauges, finding and options filled; held with, what is coming, signs 2–4, decision and next date left as placeholders for the people.
  - [x] 2026-10-19 — Ask the people present about the three observed warning signs; record each answer with its evidence, or "not observed". Held by Elie NEHME (architect, TL), no product owner yet; signs 2–4 not observed (no history yet, TL); coming: the billing PRD, bounded context not declared yet (architect).
  - [x] 2026-10-19 — Record the decision, who took it and the date of the next assessment (per `budget.review`). Nothing (no gauge over its threshold), architect; next assessment 2027-01-19.
- Verification: `node .claude/skills/partition-budget/scripts/budget.mjs --check; echo "exit=$?"`
- Expected: `exit=0` — no gauge over the threshold set in Task 1; the record exists under `docs/budget/`.
- Result (2026-10-19): `exit=0`; `docs/budget/2026-10-19.md` exists.
- Decisions that bind it: 2026-10-19 · `budget.always: 32000`, `budget.review: quarterly` (next assessment one quarter after the record's date).

## Task 5 — README carrying the build command; build command leaves the entry file

- Scope: `README.md` (created with `partition-readme` at closing), `.claude/CLAUDE.md` (`## Build` removed).
- Steps:
  - [x] 2026-10-19 — Create the README with `partition-readme`, carrying `./mvnw clean install` builds and runs every test. `README.md` created: title, description, Prerequisites (Java 21, from `pom.xml` `java.version`), Build.
  - [x] 2026-10-19 — Remove `## Build` from `.claude/CLAUDE.md`. Removed by the TL (the assistant's edit was denied by the permission settings on `.claude/`).
- Verification: `grep -n "mvnw clean install" README.md .claude/CLAUDE.md`
- Expected: one match in `README.md`, none in `.claude/CLAUDE.md`.
- Result (2026-10-19, before the removal): `README.md:11` and `.claude/CLAUDE.md:7` — not yet as expected.
- Result (2026-10-19, after the removal): `README.md:11:- \`./mvnw clean install\` builds and runs every test.` only — as expected.
- Decisions that bind it: 2026-10-19 · build command goes to the README created at closing, stays in the entry file until then (TL).

## Closing

### Gate — `partition-gate`, 2026-10-19

Target: `e6a2d0b` → working tree, untracked files included — tracked: `.claude/CLAUDE.md` (−10), `apm.lock.yaml` (+57); untracked: `.apm/instructions/*` (5), `.claude/rules/backend-*.md` (5), `README.md`, `docs/partition.yml`, `docs/plans/partition-setup.md`, `docs/budget/2026-10-19.md`. No Java code, no `pom.xml`, no resource changed. Single repository, no bounded context declared: the audit ran without a durable spec (none exists yet).

**Verdict: SHIP** — confidence High for the build and runtime (nothing executable changed and the full build passes); the loading of the scoped rules by the assistant in a live session was checked by format only.

| Check | Command | Result |
| --- | --- | --- |
| Change context | `collect_change_context.sh --range e6a2d0b` | 2 tracked files, 14 untracked; sensitive path: `apm.lock.yaml` (lockfile); no heuristic lead |
| Test safety net | `detect_test_tampering.sh --range e6a2d0b` | one hit "assertion removed": `-6. Tests: JUnit 5 and AssertJ; …` — a rule line of `.claude/CLAUDE.md`, not a test; moved verbatim to `.claude/rules/backend-testing.md:6` |
| Build and tests | `./mvnw -B clean install` | BUILD SUCCESS; Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 |
| Rule text preserved | read of `.claude/CLAUDE.md` and the five `.claude/rules/backend-*.md` | rules 1, 2, 4 → `backend-architecture.md:6-8`; 5 → `backend-database.md:6`; 6 → `backend-testing.md:6`; 7 → `backend-api-contract.md:8`; 8 → `backend-security.md:6`; 3, 9 → `.claude/CLAUDE.md`; none lost, none reworded |
| Lockfile | `git diff e6a2d0b -- apm.lock.yaml` | adds the five local deployments and their hashes; the `elie29/partition` entries are unchanged |
| Budget | `budget.mjs --check` | exit 0 (Task 4) |

Findings:

- [Low] `.claude/CLAUDE.md:4-5` — two consecutive blank lines (left where `## Build` was removed), and the `## Rules` list keeps the prefixes `3.` and `9.`. With `.markdownlint.json`, MD012 (multiple blank lines) and MD029 (ordered list prefix) stay enabled, so a markdownlint run would flag both. No runtime effect. Fix: remove one blank line, renumber to `1.`/`2.` or use bullets — owner's edit (`.claude/` is closed to the assistant). Not run: markdownlint is not installed in the session.

Regression classes skipped: behavioural, data and persistence, concurrency, performance, error handling, frontend, async — no executable code changed. Security: rule 8 survives verbatim and its scope widened from the entry file (every session) to `src/main/**`. Config and deploy: `docs/partition.yml` is read by the Partition scripts only (`budget.mjs` validates it against its schema, exit 0).

Deployment: no artefact behaviour changes; rollback is reverting the files.

Not verified:

- That Claude Code loads each `.claude/rules/backend-*.md` when a file of its `paths:` is in play (checked by format only). Rule 5 (`src/main/resources/db/migration/**`) and the `docs/contracts/**`, `target/generated-sources/openapi/**` parts of rule 7 match no file yet, by the owner's decision.
- markdownlint on the changed Markdown files.
- Diff coverage: no code line changed.

### Statements established

| Statement | Level | Where it is held (read 2026-10-19) |
| --- | --- | --- |
| Plans live in `docs/plans/` | configuration + entry file | `docs/partition.yml` `paths.plans`; `.claude/CLAUDE.md` rule 9 |
| `budget.always: 32000`, `budget.review: quarterly` | configuration | `docs/partition.yml` |
| Rules 1, 2, 4, 5, 6, 7, 8 and their scopes | rule files scoped to the code they bind | `.apm/instructions/backend-*.instructions.md`, deployed to `.claude/rules/backend-*.md` |
| One package per bounded context under `com.commerce` | entry file (holds everywhere) | `.claude/CLAUDE.md` rule 3 |
| Build command | README | `README.md:11` |
| Budget decision "nothing", next assessment 2027-01-19 | assessment record | `docs/budget/2026-10-19.md` |

Area file and invariant register: **0 `AR-` and 0 `INV-` entries added or changed.** Candidate rejected: rule 8 "Never log a payment card number" as an invariant — rejected by the architect (2026-10-19); held by `.claude/rules/backend-security.md:6` (source `.apm/instructions/backend-security.instructions.md`). No durable spec exists, so nothing moved to a spec.

README: created in Task 5 (title, description, Prerequisites Java 21, Build). C4 model: unaffected — no architecture changed, and no model exists yet.

## Decisions

- 2026-10-19 · Plans live in `docs/plans/`, written as `paths.plans` in `docs/partition.yml` · rule 9 of `.claude/CLAUDE.md` names it · rejected: `docs/superpowers/plans/`, named by the owner's global `~/.claude/CLAUDE.md`, which disagrees with the project and should be aligned by the owner.
- 2026-10-19 · `budget.always: 32000` and `budget.review: quarterly` in `docs/partition.yml` · the architect kept the template defaults until the first assessment corrects them · rejected: none proposed. `target: claude`, `bridge: none` kept at the template values (no Superpowers plugin installed in this project).
- 2026-10-19 · Rule 5 (Flyway) scoped to `src/main/resources/db/migration/**` (TL) · that is where migrations live · rejected: widening to `src/main/resources/**` until the first migration exists.
- 2026-10-19 · Rule 7 (OpenAPI) scoped to `docs/contracts/**, target/generated-sources/openapi/**, src/main/java/**` (architect, TL) · contracts live in `docs/contracts/`, are generated into `target/generated-sources/openapi/`, backend code lives in `src/main/java/` · rejected: controllers only, contracts only.
- 2026-10-19 · Rule 8 (card number) is a scoped rule on `src/main/**` (architect) · a log call or the logging configuration breaks it · rejected: invariant register candidate.
- 2026-10-19 · Build command goes to the README created at closing; stays in the entry file until then (TL) · commands belong to the README · rejected: keeping it in the entry file permanently.
- 2026-10-19 · Lines that hold everywhere stay in the entry file: "Plans live in `docs/plans/`" (TL) and "One package per bounded context" (architect) · they bind the whole project · rejected: removing rule 9 as a duplicate of `paths.plans`; scoping rule 3 to `src/main/java/**`.
- 2026-10-19 · No pointer line to `.apm/instructions/` in the entry file (TL) · each rule loads by its scope · rejected: a pointer line.
- 2026-10-19 · Tests live in `src/test/java/**`; rule 6 scoped there (TL).
- 2026-10-19 · Budget assessment decision: nothing (architect) · no gauge is over its threshold · rejected: scoping a rule, trimming a spec, a boundary workshop, a promotion (none applicable). Next assessment 2027-01-19, one quarter from today.
- 2026-10-19 · Duplication between rule 9 and `~/.claude/CLAUDE.md` left as is · outside the plan; the owner aligns their own file.
- 2026-10-19 · Plan closed on a `partition-gate` SHIP verdict · build and tests pass, every rule preserved verbatim; the one Low finding (Markdown lint of `.claude/CLAUDE.md`) does not condition the ship.

## Blockers

<!-- What is blocked · what unblocks it · who owns it. Delete the line when it clears. -->

