# Pattern grid

The rulings of this project on design patterns and structuring principles. Read it before introducing a pattern or an abstraction; replay it to audit the code. A ruling is added when a real need raises the question, never in advance.

## Criteria

Every ruling answers the criteria below. The list is the project's: add a column, remove one.

| Criterion      | Question                                                                              |
| -------------- | ------------------------------------------------------------------------------------- |
| Need now       | Does the code need it today? Name the two cases that exist, not the third that might. |
| Budget         | With it, does an ordinary change make the assistant read less, or more?               |
| Cost of change | When the business rule changes, how many files and callers move, with and without it? |
| Testability    | What becomes verifiable without starting the framework or the database?               |
| Security       | Does it centralise a control, or open a path around one?                              |

## Rulings

`adopted` · `conditional` (say when) · `rejected` · `no pattern` (the plain code stays, on purpose).

| # | Where | Pattern | Ruling | Need now | Budget | Cost of change | Testability | Security | Reason | Date |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| P-1 | `invoice` / status of an invoice | none: an enum held by the invoice aggregate, one guarded method per command, no status setter (candidates that lost: State (GoF), enum transition table) | no pattern | Three statuses (issued, paid, cancelled), three commands, four moves under BR-1, BR-4, BR-5, BR-6: one class and one enum hold them; with State, six of nine state × command bodies only refuse | One aggregate and one enum to read for a change of status; State: five files; table: enum and aggregate read together | A new status or move touches the enum and the methods it reaches: two files; State: a class per status and every class per command; table: the rule split in two files | JUnit 5 and AssertJ on the aggregate, no Spring, no database; BR-6 with the current time as an argument | Every change of status goes through the aggregate's methods; a table allowing a move whose guard is forgotten would open a path around BR-4 or BR-6 | The confirmed rules fit one enum and guarded methods in one place. Ruled again (State) if a partly paid status, an "unpaid again" status with its own moves, two "cancelled" statuses, or a fee or behaviour that differs by status makes the methods branch on the status in more than one place each — all open questions of `docs/specs/billing/invoice/spec.md`. After the fission of `invoice` (2026-10-19), P-1 holds; its reference to BR-5 and BR-6 now reads `payment`'s rules (architect) | 2026-10-19 |

## Audits

| Date | Rulings replayed | Conform | Deviations |
| ---- | ---------------- | ------- | ---------- |
