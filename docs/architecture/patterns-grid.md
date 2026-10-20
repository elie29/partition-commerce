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
| P-2 | `refund` / status of a refund | none: an enum held by the `Refund` aggregate, one guarded method per move, no status setter (candidate that lost: State (GoF)) | no pattern | Three statuses (requested, sent, settled), one move built (`REQUESTED` → `SENT`, BR-28); the move to settled is an open question | One aggregate and one enum; State: four files for one move | A new move touches the enum and one method: two files | JUnit 5 and AssertJ on the aggregate, no Spring, no database | Every change of status goes through the aggregate's guarded method | As P-1. Ruled again when CardPay's notifications bring the move to `SETTLED` and a failed send brings its own moves (open questions of `docs/specs/billing/refund/spec.md`) (architect: the recommended option) | 2026-10-19 |
| P-3 | `refund` / the cap of the refunds of an invoice (BR-25) | a running total per invoice, guarded by one conditional update in the transaction that records the refund (candidates that lost: a sum checked by `INSERT … SELECT … WHERE`, a lock on `payment`'s row) | adopted | Two refunds of one invoice at the same moment: the first to commit wins, the other gets 409 (architect, 2026-10-19) | One adapter, `JdbcRefunds`, holds the check | The cap changes in one statement; the sum checked at insert: the same, but wrong under READ COMMITTED | A concurrent test through the API, on the database | The cap holds under concurrency; the lock would reach into another context's table | The conditional update is the shape BR-23 already uses for a change of status (architect: the recommended option) | 2026-10-19 |
| P-4 | `product`, `price-list`, `discount` / where the error envelope of AR-1 (`docs/specs/catalog/area.md`) is built | one `@RestControllerAdvice` per context, each building the envelope and its log line, as `billing` has one per context (candidate that lost: one `CatalogErrors` and one `ErrorEnvelope` in a package `com.commerce.catalog`) | no pattern | Three contexts answer the same envelope today (`discount` since 2026-10-20, architect) | Each context reads its own handler only; shared: a package outside both to read | A change of AR-1 moves two files; shared: one, and the layer maps of both | Through the API of each context | Each handler holds its own guard on what is logged (AR-2); shared: one guard for both | Rule 3, "One package per bounded context", kept as written; a shared package would need it changed (architect: option 1); extended to `discount`, one error handler per context (architect, 2026-10-20) | 2026-10-19 |
| P-5 | `price-list` / one price per product and list (BR-1) | a primary key on `(price_list_id, product_id)` and one `MERGE`: the last to commit replaces (candidates that lost: an `UPDATE` then an `INSERT`, a version column) | adopted | A second price replaces the first; two first prices at the same moment both answer 200 (architect, 2026-10-19) | One adapter, `JdbcPrices`, holds it | BR-1 changes in one statement | A concurrent test through the API, on the database | The rule holds under concurrency; `UPDATE` then `INSERT` gives one 500 of two first prices; a version column refuses where BR-1 replaces | The store's key holds the rule, as the architect stated it (architect: the recommended option) | 2026-10-19 |

## Audits

| Date | Rulings replayed | Conform | Deviations |
| ---- | ---------------- | ------- | ---------- |
