---
description: 'Backend API contract of catalog: how product, price-list and discount check their requests'
applyTo: 'src/main/java/com/commerce/product/**, src/main/java/com/commerce/discount/**'
---

- The generator executions of the catalog (`pom.xml`, `product`, for the tags `Product` and `PriceList`, and `discount`) map `UUID` to `String`, turn bean validation off and list no `modelsToGenerate`: `UUID.fromString` accepts `1-1-1-1-1` (AR-3), a generated `@Size` counts UTF-16 units against a Character, binding stops at the first field (AR-1), and `modelsToGenerate` drops the inline models (TL, 2026-10-19; the executions: TL, 2026-10-20).
- A `HandlerInterceptor` of the context checks the headers, then the query, before any parameter is bound (TL, 2026-10-19).
- A `RequestBodyAdvice` of the context judges the body on its JSON tree before it is bound, types included, and merges what the use case judges by field: one item per field at fault (AR-1) (TL, 2026-10-19).
- Each controller carries the base path `/commerce/v1/catalog` as its own type-level `@RequestMapping`: a `PathMatchConfigurer` prefix is keyed by its text, so two contexts under one prefix replace each other's (TL, 2026-10-19).
- Verified by the tests of each use case (`CreateProductTest`, `CreatePriceListTest`, `SetPriceTest`, `GetProductPriceTest`).
