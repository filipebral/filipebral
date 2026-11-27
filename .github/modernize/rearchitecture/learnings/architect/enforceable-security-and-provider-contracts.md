# Enforceable Security and Provider Contracts

Preserve the cookie/JWT and external APIs while making session, recovery, idempotency, migration, and KYC/provider readiness testable through additive provider-neutral contracts.

## What Happened

During Giro t2.2, stateless JWT revocation, cross-origin mutation, external provider side effects, event redelivery, and KYC/recovery uncertainty were resolved into explicit target contracts. An opaque `sid` registry extends rather than replaces the existing HS256 flow, and a persistent operation/outbox/inbox model makes provider retries and ambiguous outcomes safe.

## Takeaway

Never call a provider or broker inside a local transaction. Persist the intent and outbox first, commit, then use the same operation key and payload fingerprint for provider retries. Treat unknown outcomes as ambiguous until reconciled. KYC and recovery test doubles may be truthful states but must never produce real-provider approval or delivery success.

## History
- 2026-11-23 (giro-app/t2.2): initial
