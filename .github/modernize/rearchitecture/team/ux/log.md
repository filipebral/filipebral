# UX Team Log

## [t5] Mobile-first six-wave accessible flow specification

- Codebase/domain discoveries: the operative UI is the tracked nested
  `giro-app/` tree; the baseline is Portuguese, dark/lime, rounded, and
  one-column mobile-first, with existing modal and focus-management gaps.
- Wrong assumptions and corrections: architecture evidence initially did not
  expose exact color values, so the UX artifact defines semantic use of the
  existing tokens rather than inventing a parallel palette.
- Debugging dead-ends and what actually worked: direct source inspection was
  avoided because complete architecture research existed; PM requirements,
  architecture state contracts, and unit behavior/bindings supplied the
  implementable behavior boundary.
- Techniques/patterns worth reusing: every provider-dependent state must pair
  source-of-truth wording, an icon/word treatment, and one safe next action;
  every critical flow gets a responsive/keyboard/screen-reader matrix.
- Learnings consumed: [pm/provider-safe-product-states,
  architect/wave1-session-and-provider-seams,
  architect/enforceable-security-and-provider-contracts]

## [t6.3] Align simulated KYC rendering and one safe ambiguous-state action
- Codebase/domain discoveries: T6-F02 affects the presentation precedence, not
  just copy; `PROVIDER_REVIEW` from `TEST_DOUBLE` must be visibly normalized to a
  local simulation. T6-F05 spans account, reconciliation, payment, notification,
  and service-state panels.
- Wrong assumptions and corrections: “review pending” is unsafe unless provider
  provenance is confirmed. “Check status/support” violates the one-action rule;
  support remains globally reachable but is not a competing local action.
- Debugging dead-ends and what actually worked: a state-only KYC matrix was
  insufficient. The deterministic tuple `state + truthSource + simulated +
  providerConfirmed`, with provenance precedence, closes contradictory
  rendering paths without requiring frontend to infer truth.
- Techniques/patterns worth reusing: define an exhaustive state/provenance/action
  table, name the single exact CTA, and validate that pending, ambiguous, and
  simulated paths cannot expose retry or provider-confirmed language.
- Learnings consumed: [ux/provider-truth-and-accessible-state-language]
