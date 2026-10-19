---
description: 'Backend database: Flyway migrations'
applyTo: 'src/main/resources/db/migration/**'
---

- Every schema change is a new Flyway file `V<n>__<what>.sql`; a migration already merged is never edited.
