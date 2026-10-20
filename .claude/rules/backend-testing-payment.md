---
paths:
  - "payment-app/src/test/java/**"
---

- `payment-app`'s tests fake `commerce-app` entirely with one stub HTTP server on the JDK's `com.sun.net.httpserver.HttpServer` (`CommerceAppStub`), which the real generated client calls; it answers as `docs/contracts/billing-internal.openapi.json` says and tests the 2-second limit, the 404 and the 409; no new dependency (TL, 2026-10-20).
