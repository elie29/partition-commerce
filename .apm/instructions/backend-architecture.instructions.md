---
description: 'Backend architecture: injection, layers, dependency direction'
applyTo: 'src/main/java/**'
---

- Constructor injection only; no field injection.
- A controller never calls a repository.
- The domain does not import Spring.
