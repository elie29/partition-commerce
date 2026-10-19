---
paths:
  - "src/main/resources/db/migration/**"
---

- Every schema change is a new Flyway file `V<n>__<what>.sql`; a migration already merged is never edited.
- Every amount is stored in minor units, with its currency: a `BIGINT` and a three-letter ISO 4217 code, never a decimal (architect, 2026-10-19).
- An amount column is named `<what>_minor` and its currency column `<what>_currency` (TL, 2026-10-19).
