---
paths:
  - "src/main/resources/db/migration/**"
---

- Every schema change is a new Flyway file `V<n>__<what>.sql`; a migration already merged is never edited.
- An amount column is named `<what>_minor` and its currency column `<what>_currency` (TL, 2026-10-19).
