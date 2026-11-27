# One Safe Action for Ambiguous States

Customer panels for provider-ambiguous states must expose exactly one primary action, with provenance wording resolved before state wording.

## What Happened

During Giro task t6.3, a test-double could reproduce `PROVIDER_REVIEW`, and the
existing UX sometimes offered both status checking and support. That combination
could imply a real provider review or allow implementers to choose inconsistent
retry behavior.

## Takeaway

For operations that may have reached an external provider, render
`Atualizar status` as the sole local primary action. Do not show retry, resend,
or support as a competing action. For KYC, simulation/test-double provenance
overrides the raw state wording so a simulated review can never become
provider-confirmed copy.

## History

- 2026-11-23 (giro-app/t6.3): initial
