---
description: 'Backend architecture of invoice: what its domain layer may import'
applyTo: 'src/main/java/com/commerce/invoice/domain/**'
---

- The domain of `invoice` imports nothing from outside itself: no Spring, no Lombok, no Jakarta annotation, no other framework. It is a rich hexagon (`docs/partition.yml`); verified by the architecture test.
