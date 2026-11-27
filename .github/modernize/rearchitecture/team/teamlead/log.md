## [t3] Audited six-wave capability inventory with a binary specification-quality gate
- Codebase/domain discoveries: the tracked runnable application is the nested `giro-app/`; PM inventory covers the six product domains, but several requirements mix outcome language with implementation and provider-contract terminology.
- Wrong assumptions and corrections: submitted clarification resolves user scope gaps, but it does not supply the constitution required by the quality-gate workflow.
- Debugging dead-ends and what actually worked: no source-code investigation was needed; quote-level checklisting against t1, clarification, and project-structure evidence produced a reproducible FAIL.
- Techniques/patterns worth reusing for future tasks: require an explicit REQ-to-scenario map so requirement-level acceptance coverage cannot be inferred from a few wave examples.
- Learnings consumed: [pm/provider-safe-product-states, architect/nested-runtime-and-provider-seams]

## [t3.2] Added the canonical migration constitution required by the specification gate
- Codebase/domain discoveries (gotchas, non-obvious behavior): the operative application is `giro-app/`; the root fragment POM is not a valid build root, and quality gates search for `constitution.md` at the workflow root rather than only in task artifacts.
- Wrong assumptions and corrections: the artifact output path alone is not sufficient for a gate input; the task artifact links to one canonical workflow-root constitution to avoid duplicated policy copies.
- Debugging dead-ends and what actually worked: deriving obligations from the PM inventory, clarification answers, project structure, tech stack, and failed-gate checklist produced a deterministic constitution without reading application source.
- Techniques/patterns worth reusing for future tasks: encode provider truth, operative scope, stack preservation, contract compatibility, accessibility, traceability, and binary test-gate rules as declarative MUST statements.
- Learnings consumed: [(none)]

## [t3] Re-ran the specification-quality gate after PM and constitution remediation
- Codebase/domain discoveries (gotchas, non-obvious behavior): the remediated inventory now cleanly separates customer outcomes from delivery constraints, and the traceability artifact provides one acceptance scenario per requirement.
- Wrong assumptions and corrections: the earlier 63-ID validation was insufficient if the trace comparison normalized `REQ-###` differently; normalizing both sides to the numeric suffix produced the correct zero-missing result.
- Debugging dead-ends and what actually worked: a deterministic PowerShell audit of unique IDs, mappings, headings, clarification markers, and required gate inputs confirmed a binary PASS.
- Techniques/patterns worth reusing for future tasks: rerun structural checks after ownership remediations and preserve quote-level evidence in the checklist rather than relying on summary status.
- Learnings consumed: [teamlead/constitution-as-canonical-gate-input, pm/provider-safe-product-states, architect/nested-runtime-and-provider-seams, architect/wave1-session-and-provider-seams]

## [t6] Failed the combined architecture/data/UX design gate on cross-contract gaps
- Codebase/domain discoveries (gotchas, non-obvious behavior): architecture, DBA, and UX artifacts are structurally complete, but field identity and provenance behavior diverge when the same concept crosses role boundaries.
- Wrong assumptions and corrections: section presence and full REQ-001–REQ-063 UX coverage do not prove the backend session or KYC contracts are consistent or that every later-wave persistence need is owned.
- Debugging dead-ends and what actually worked: comparing exact `sid` exposure statements and test-double/provider-review tuples exposed blockers that heading and requirement-count checks alone missed.
- Techniques/patterns worth reusing for future tasks: require a cross-artifact matrix for identifier exposure, truth source, persisted state, customer copy, safe next action, and verification evidence.
- Learnings consumed: [teamlead/constitution-as-canonical-gate-input, architect/wave1-session-and-provider-seams, architect/nested-runtime-and-provider-seams, architect/enforceable-security-and-provider-contracts, dba/provider-readiness-as-runtime-config, ux/provider-truth-and-accessible-state-language]
