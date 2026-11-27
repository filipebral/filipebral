# KYC Provenance Tuple

Represent KYC state together with its truth source, simulation flag, and provider-confirmation flag so a test double cannot be rendered as real provider review.

## What Happened
During Giro task t6.1, `PROVIDER_REVIEW` was allowed as a reproducible
test-double scenario while the UX could interpret the same state as a real
provider process. State alone could not express simulation provenance
truthfully.

## Takeaway
Use the canonical tuple `state + truthSource + simulated + providerConfirmed`
plus an optional `simulatedScenario`. Normalize every test-double external
scenario to public `SIMULATED`; reserve public `APPROVED`, `REJECTED`, and
`PROVIDER_REVIEW` for the allowed provider/operator truth-source combinations.

## History
- 2026-11-23 (giro-app/t6.1): initial
