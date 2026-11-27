# Wave 1 Session and Provider Seams

Preserve the existing cookie/JWT and gateway boundaries while Wave 1 adds revocation, recovery, and KYC-ready state.

## What Happened
The submitted Giro clarification made the current Next.js major, Spring Boot 3, external API compatibility, HttpOnly cookie, and bearer bridge immutable. Static architecture evidence showed that revocation cannot be derived from the current self-contained JWT and provider retries cannot be made safe from a local transaction alone. Originating project/task: Giro t2.

## Takeaway
Add an opaque, backward-compatible session identity/registry for revocation while retaining existing JWT claims and the BFF cookie. Apply Origin validation to public mutation handlers too. Keep recovery/KYC/provider contracts additive and use idempotency plus test doubles; never infer real-money success from local transaction rollback or an unconfigured mock.

## History
- 2026-11-23 (giro-app/t2): initial
