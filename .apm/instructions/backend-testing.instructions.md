---
description: 'Backend testing: frameworks, one test class per use case'
applyTo: 'src/test/java/**'
---

- Tests: JUnit 5 and AssertJ; one test class per use case. The single architecture test class, and one test class per migration that moves data, test no use case and sit outside this rule (TL, 2026-10-19).
- The architecture test uses ArchUnit (`com.tngtech.archunit:archunit-junit5` 1.5.1, test scope), reads the layer maps from `docs/partition.yml` at test time with SnakeYAML, so adding a hexagon needs no test change, and lets a rule that matches no class pass.
