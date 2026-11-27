# Provider Truth and Accessible State Language

Provider-dependent banking states must pair an explicit source-of-truth label, plain-language meaning, and exactly one safe next action.

## What Happened

During Giro task t5, the six-wave UX had to preserve the existing dark/lime
styling while distinguishing local simulation, unconfigured providers,
ambiguous outcomes, pending review, and provider-confirmed results. Color alone
could not safely carry those meanings, and a generic retry was unsafe after a
possible external submission.

## Takeaway

Reuse the existing visual tokens, but always combine a visible state word and
icon with explanatory copy. `SIMULATED`, `NOT_CONFIGURED`, `AMBIGUOUS`, and
provider-confirmed results require different next actions; never convert them
into one generic success or retry pattern.

## History

- 2026-11-23 (giro-app/t5): initial
