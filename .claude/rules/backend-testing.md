---
paths:
  - "commerce-app/src/test/java/**"
  - "payment-app/src/test/java/**"
---

- Tests: JUnit 5 and AssertJ; one test class per use case. The architecture test class of each app, and one test class per migration that moves data, test no use case and sit outside this rule (TL, 2026-10-19; one per app: TL, 2026-10-20).
- The architecture test uses ArchUnit (`com.tngtech.archunit:archunit-junit5` 1.5.1, test scope), reads the layer maps from `docs/partition.yml` at test time with SnakeYAML, so adding a hexagon needs no test change, and lets a rule that matches no class pass.
