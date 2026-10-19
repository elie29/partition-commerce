# Invariants

The project's single register of durable truths: statements true for every feature that span at least two areas or layers. One line per statement, in the present tense. This file says **what**; the dated **why** stays in the `## Decisions` section of the plan that produced the statement, and in the history of this file. It is not a journal: never append a story, never order by date.

## Entry format

`INV-{CAT}-{NNN}` — statement in the present tense, one line, verifiable.

- `{CAT}` is one of the categories below. `{NNN}` is the next free number of that category.
- An entry copied from a source keeps the source's words and ends with `(from source: <anchor>)`.
- An identifier is permanent. A removed one is never reassigned.

## Categories

| Category | Holds                                  |
| -------- | -------------------------------------- |
| `DATA`   | persistence, identity, consistency     |

## DATA

- INV-DATA-001 — Every amount is stored in minor units, with its currency. (from source: docs/contracts/catalog.openapi.json /info/description)
