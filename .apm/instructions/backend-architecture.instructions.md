---
description: 'Backend architecture: injection, layers, dependency direction'
applyTo: 'commerce-app/src/main/java/**, payment-app/src/main/java/**'
---

- Constructor injection only; no field injection.
- A controller never calls a repository.
- The domain does not import Spring.
- No module is shared between `commerce-app` and `payment-app` in this version: what both need, such as `JsonConfiguration`, is copied into each (TL, 2026-10-20).
