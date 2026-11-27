# Session Handles and KYC Provenance

Persist a random browser session-management handle separately from the private JWT `sid`, and persist KYC truth as an invariant source/simulation/provider-confirmation tuple.

## What Happened

During Giro t6.2, the design gate found that the database contract exposed `sid`
to the browser through session listing and allowed a test-double
`PROVIDER_REVIEW` to be mistaken for real provider truth. The remediation made the
JWT `sid` server-private, added an independently generated globally unique
management handle, and constrained onboarding state, truth source, simulation, and
provider confirmation as one tuple.

## Takeaway

Never overload an authentication identifier as a browser-management identifier.
Generate a non-derivable high-entropy handle and retain uniqueness after
revocation. Normalize test-double KYC outcomes before persistence so provider-only
states cannot coexist with local or simulated provenance.

## History
- 2026-11-23 (giro-app/t6.2): initial
