# Nested Runtime and Provider Seams

Treat the tracked nested `giro-app/` tree as the authoritative runtime and keep all external provider/money behavior behind explicit BFF/provider seams.

## What Happened
During Giro t2, the workspace root contained duplicate-looking directories, but only `giro-app/` was tracked and contained the runnable Next.js/Spring build files. The cookie/JWT BFF and BaaS/Rabbit paths require cross-language and provider safety contracts rather than direct stack replacement.

## Takeaway
Anchor implementation and artifacts to `giro-app/`; preserve the HttpOnly cookie-to-Bearer bridge and external JSON contracts; keep `BAAS_ENABLED` and Rabbit listener startup disabled until provider contract, idempotency, credentials, and approval are real.

## History
- 2026-11-23 (giro-app/t2): initial
