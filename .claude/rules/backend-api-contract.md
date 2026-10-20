---
paths:
  - "docs/contracts/**"
  - "commerce-app/target/generated-sources/openapi/**"
  - "commerce-app/src/main/java/**"
  - "payment-app/target/generated-sources/openapi/**"
  - "payment-app/src/main/java/**"
---

- The HTTP API is generated from its OpenAPI contract; a controller implements the generated interface.
- A client of an OpenAPI contract is generated from it, as the server side is: Spring HTTP interfaces (generator `spring`, library `spring-http-interface`) over a `RestClient` (TL, 2026-10-20).
- A JSON value of the wrong type is refused, never coerced (`com.commerce.JsonConfiguration`) (TL, 2026-10-19).
- An error response the contract declares, when it has a body, carries the common error envelope: `{timestamp, path, errorId, detail, errors}`, each item of `errors` being `{field, code, message}`.
- A request is refused in this order: its content (`400`), then the identifiers of its path (`404`, one that is not a UUID included), then the rules on what is held; the first refusal stops the checks.
- A value the contract's schema refuses is refused with `400`, whether it is kept or not.
