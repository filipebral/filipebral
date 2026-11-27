# Cross-Artifact State Contract Gates

Quality gates must compare identifiers and provenance states across architecture, data, and UX artifacts, not only check that each file is internally complete.

## What Happened

During Giro task t6, the session artifact contract separately prohibited and
required the same JWT `sid` in browser JSON, while test-double KYC provenance
could become externally worded as provider review. Structural checks passed even
though these cross-artifact contradictions blocked deterministic planning.

## Takeaway

For identifier and provider-state reviews, build a shared contract matrix covering
field identity, browser exposure, storage, truth source, simulated flag, user
copy, allowed action, and verification. A heading/coverage check alone cannot
prove consistency.

## History
- 2026-11-23 (giro-app/t6): initial

