# Architect Log

## [t2] Analyze Next.js, Spring Boot, contracts, security, and migration risks
- Discovery: the tracked runnable full-stack root is `giro-app/`; similarly named workspace-root folders are not the git source tree.
- Discovery: Next 16 BFF owns the HttpOnly `giro_access_token` cookie, while Spring 3 issues/validates HS256 JWT claims.
- Gotcha: account creation places external provider I/O inside a DB transaction and catches failures after an initial PENDING save; actual rollback/persistence behavior conflicts with the source comment.
- Gotcha: the Rabbit split consumer is placeholder logic with no stable event id; it must remain disabled before a provider idempotency contract exists.
- What worked: source-anchored unit graph with 11 runtime units and per-unit contracts; PyYAML self-checks passed 40 files.
- Validation finding: `npm ci` + Next build + Node tests + `GET /` passed, while full backend `clean verify` failed in testCompile despite classes existing in `target/classes`.
- Learnings consumed: (none)

## [t2] Architecture refresh after submitted clarifications and backend recovery
- Codebase/domain discoveries: the authoritative nested backend now compiles/tests cleanly; auth remains a stateless 15-minute HS256 cookie/JWT flow, and Origin enforcement exists only on the authenticated proxy.
- Wrong assumptions and corrections: the earlier Maven blocker was transient/stale; t2.1 reproduced full `clean verify` with 12/12 tests and no source change.
- Debugging dead-ends and what actually worked: architecture YAML validation passed 40 files and 11 complete unit sets; full frontend install/build, 3 Node tests, HTTP startup, and backend verification provided independent smoke evidence.
- Techniques/patterns worth reusing: preserve browser-visible contracts at the BFF seam, use an additive opaque session/revocation identity rather than replacing JWT/cookies, and require provider idempotency before retry across DB/provider/event side effects.
- Learnings consumed: (none)

## [t2.2] Remediated unresolved security, provider, idempotency, migration, and onboarding architecture findings
- Codebase/domain discoveries: the BFF has an Origin gap on public auth mutations; account creation performs provider HTTP and event publication inside a DB transaction; prod Flyway/validate already provides the correct production migration boundary.
- Wrong assumptions and corrections: `@Transactional` does not make provider/Rabbit side effects rollback-safe; a new opaque `sid` registry can harden existing HS256 JWTs without replacing browser-visible auth responses.
- Debugging dead-ends and what actually worked: source-anchored contract review across the t2 YAML set and runtime entrypoints resolved all six t2 findings without changing production code.
- Techniques/patterns worth reusing: enforce session/recovery/KYC state as additive contracts, use same-key plus payload-hash idempotency, reconcile ambiguous outcomes, and publish provider/event commands only after a local outbox transaction commits.
- Learnings consumed: [architect/nested-runtime-and-provider-seams, architect/wave1-session-and-provider-seams]

## [t2] Final architecture refresh and validation after submitted clarifications
- Codebase/domain discoveries: the authoritative `giro-app/` full-stack baseline is Next.js 16.4.6/React 19.2.8 plus Spring Boot 3.3.4/Java 17; the submitted clarification and t2.2 remediation jointly define the Wave 1 implementation contract.
- Wrong assumptions and corrections: the architecture baseline no longer has the npm critical advisory or missing test script; t2.3 cleared both. Source implementation remains outside the architect charter and must be handed to frontend/backend/dba/ux/tester tasks.
- Debugging dead-ends and what actually worked: reran frozen install, full Next build, Node tests, audit, HTTP smoke, Maven `clean verify`, and YAML structural assertions; all passed without production-source edits in this turn.
- Techniques/patterns worth reusing: keep the structured baseline artifacts as current-state evidence and link binding remediation/design supplements from the implementation index so downstream roles cannot implement from stale inventory alone.
- Learnings consumed: [architect/nested-runtime-and-provider-seams, architect/wave1-session-and-provider-seams, architect/enforceable-security-and-provider-contracts]

## [t6.1] Remediate session-handle, KYC provenance, and REQ traceability contracts
- Codebase/domain discoveries: the private JWT `sid` and browser session-management identity must be separate values; KYC state alone is insufficient when the source is a test double.
- Wrong assumptions and corrections: a stable public session `id` cannot safely reuse `sid`, and normalized `PROVIDER_REVIEW` cannot be emitted for non-provider provenance.
- Debugging dead-ends and what actually worked: a cross-artifact matrix exposed the session and provenance contradictions; a 63-row REQ matrix made all persistence, UX, contract, and verification handoffs explicit.
- Techniques/patterns worth reusing: POST revoke handles in a same-origin JSON body, identify the current session server-side from private `sid`, and normalize test-double scenarios through a canonical provenance tuple.
- Learnings consumed: [architect/wave1-session-and-provider-seams, architect/nested-runtime-and-provider-seams, architect/enforceable-security-and-provider-contracts, teamlead/cross-artifact-state-contract-gates, teamlead/constitution-as-canonical-gate-input]
