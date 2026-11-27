# Provider Readiness as Runtime Configuration

Persist provider observations and operations, but never make the database a parallel source of truth for provider enablement.

## What Happened

During Giro t4, the Wave 1 persistence design separated external-provider readiness from normalized provider-operation, onboarding, outbox, and inbox state. Runtime configuration owns adapter selection, credentials, approval, and enabled/disabled gates; persisted rows record what was attempted or observed. Test-double rows retain explicit provenance and cannot imply `APPROVED`, active banking, or real-money movement.

## Takeaway

Keep V7+ schema additive and provider-neutral. Enforce provider-only truth and same-key payload identity in constraints and service transactions, but retain `BAAS_ENABLED`/listener/provider approval as startup configuration gates. Never infer readiness merely from a row, localhost endpoint, or completed test-double operation.

## History
- 2026-11-23 (giro-app/t4): initial
