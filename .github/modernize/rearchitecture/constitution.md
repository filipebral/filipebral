# Giro Digital Banking MVP Constitution

<!--
Sync Impact Report
- Version change: none (initial constitution) → 1.0.0
- Modified principles: none (initial adoption)
- Added sections: Migration Mode, Core Principles, Stack and Scope Constraints,
  Delivery and Quality Gates, Governance, Upstream Artifacts Consumed,
  Evidence Mapping
- Removed sections: none
- Templates requiring updates:
  - ✅ creating-implementation-plan/templates/plan-template.md: existing
    Constitution Check is aligned; no update required
  - ✅ feature-inventory/templates/spec-template.md: existing constitution
    governance reference and measurable-outcome structure are aligned
  - ✅ creating-implementation-plan/templates/tasks-template.md: existing
    constitution governance, dependency, and independent-test guidance are aligned
- Follow-up TODOs: none
-->

## Migration Mode

**Mode**: UPGRADE

**Justification**: Giro is an active brownfield Next.js and Spring Boot
application with working customer journeys. Capability completion is performed
in place by additive, backward-compatible changes. A rewrite, stack replacement,
or duplicate implementation is not authorized.

## Core Principles

### I. Preserve the Operative Product and Observable Behavior

All implementation work MUST target the populated `giro-app/` application tree.
The outer repository tree MUST remain the workspace and release boundary unless
an approved artifact explicitly changes scope. Existing registration, sign-in,
dashboard, simulation, split, calculator, endpoint, locale, and error behavior
MUST remain available unless a requirement-approved security or regulatory
correction is documented. No behavior may be silently removed or relocated.

**Rationale**: The project is a same-stack capability upgrade, and duplicate
trees create ambiguity and regression risk.

### II. Preserve External Contracts and the Current Stack

Changes MUST preserve the current Next.js major (Next.js 16 for the observed
baseline) and Spring Boot 3 major. Existing externally consumed request and
response meanings MUST remain backward compatible. Additions MUST be additive;
renames, removals, route changes, cookie-shape changes, or semantic changes
require an explicit approved migration contract and compatibility evidence.

The cookie/JWT authentication flow MUST be preserved while Spring Security
hardening is added. The existing same-origin BFF boundary MUST remain the browser
entry point for backend access.

**Rationale**: Client compatibility and the accepted stack are immutable project
constraints, not implementation preferences.

### III. Enforce Secure-by-Default Session, Identity, and Data Handling

Authentication, recovery, session, onboarding, provider, and administrative
paths MUST use least privilege, non-enumerating errors, throttling, expiration,
one-time secrets, revocation where supported, and explicit user-safe outcomes.
Passwords, tokens, provider credentials, and unnecessary personal or financial
data MUST NOT be logged or exposed to the browser. Cookies MUST remain HttpOnly
and SameSite, and MUST be Secure in production.

KYC and identity state MUST be represented separately from registration.
Onboarding MUST NOT claim identity verification, provider approval, or account
activation without authoritative provider evidence.

**Rationale**: Banking interfaces require security invariants even while a
provider integration is not configured.

### IV. Maintain Provider Truth and Prevent Unconfigured Money Movement

Provider capabilities MUST be isolated behind provider-safe contracts with
deterministic test doubles. A test double or local simulation MUST support the
relevant success, rejection, timeout, duplicate, partial-data, and eventual-state
cases without changing customer-visible meaning.

No action MAY create, send, receive, settle, reconcile, or represent real money as
real before a configured and approved provider exists. Simulated values MUST be
visibly separate from provider-confirmed values and MUST never become financial
source of truth. Missing provider, KYC, notification, account, ledger, or legal
approval MUST produce an unavailable, draft, test-only, or simulated state.

**Rationale**: Functional completeness must not be confused with readiness to
perform regulated financial operations.

### V. Use Vertical, Traceable, and Layer-Respecting Delivery

Work MUST be decomposed into independently executable vertical business slices,
not by an isolated entity/service/controller layer. Each implementation task
MUST map to one or more REQ-XXX identifiers, an approved design input, and
verification evidence. Every new requirement MUST map to an executable
acceptance scenario before implementation starts.

Frontend work MUST follow the existing direction: Next pages and components call
same-origin route handlers or BFF utilities, and browser code MUST NOT access
backend secrets or bypass the BFF. Backend work MUST follow the existing
controller/listener/scheduler → service/use-case → repository or provider
boundary. New persistence structures MUST use reviewed additive migrations;
database ownership and source-of-truth choices require DBA and architect approval.

**Rationale**: Vertical slices preserve observable behavior, enforce ownership
boundaries, and keep traceability auditable.

### VI. Preserve the Existing User Experience and Meet Accessibility Gates

New and modified UI MUST reuse the existing dark/lime mobile-first styling,
tokens, component conventions, Portuguese locale, and established interaction
patterns unless accessibility or usability evidence requires a documented
adjustment. Primary flows MUST satisfy WCAG 2.1 AA, support keyboard-only
operation, expose visible focus and programmatic errors, announce async state,
manage dialog focus, and avoid clipped or inaccessible essential content.

Primary flows MUST be verified at mobile, tablet, and desktop sizes and across
the latest two major versions of Chrome, Firefox, Safari, and Edge. A passing
build alone does not satisfy frontend, accessibility, browser, or runtime gates.

**Rationale**: The product must extend the accepted visual system without
excluding users or silently changing established behavior.

### VII. Require Evidence for Correctness, Idempotency, and Recovery

Security-sensitive and provider-sensitive work MUST demonstrate duplicate-safe
retries, timeout recovery, revocation, non-enumeration, and unambiguous final
state. Append-only or audit-oriented evidence MUST be privacy-safe and
tamper-evident, excluding secrets and unnecessary personal or financial data.
Unknown provider outcomes MUST remain unknown or pending until reconciled; they
MUST NOT be converted to success by retries or test doubles.

Existing baseline tests MUST pass after each implementation wave. New work MUST
include the minimum tests and runtime evidence needed for its contracts, while
the final conformance review MUST cover critical journeys and every requirement.

**Rationale**: Financial workflows fail safely only when retries, recovery, and
evidence are explicit and tested.

## Stack and Scope Constraints

- **Operative frontend**: `giro-app/app`, `giro-app/components`, and `giro-app/lib`.
- **Operative backend**: `giro-app/backend`, built with the backend POM.
- **Frontend baseline**: Next.js 16.3.8, React 19.2.8, TypeScript 5, Tailwind CSS 4.
- **Backend baseline**: Spring Boot 3.3.4 on Java 17, preserving the Spring Boot 3
  and Spring Security 6 contract family.
- **Persistence baseline**: PostgreSQL with Flyway for production, H2 for tests.
  Non-production convenience MUST NOT hide production migration drift.
- **Configuration**: `GIRO_BACKEND_URL` remains server-only; JWT and encryption
  keys remain secret environment configuration and MUST NOT be committed.
- **Compatibility**: Existing routes and public/internal request-response
  semantics remain compatibility baselines.
- **Provider state**: Real provider, KYC, notification, payment, ledger,
  reconciliation, card, credit, insurance, support, and legal integrations remain
  external blockers until their approved configuration and ownership exist.
- **Legal sequence**: Licensing, regulatory, privacy, and contractual readiness
  MUST be implemented and reviewed in the final wave only.
- **Version control**: Implementation MAY modify the current workspace, but MUST
  NOT commit changes unless the user later explicitly overrides this rule.

## Naming, Layering, and Change Rules

1. Preserve established package, route, component, test, and artifact names;
   avoid repository-wide renames during capability delivery.
2. New code MUST use the local language conventions in its containing module.
   Public contracts and requirement identifiers MUST use their existing forms.
3. Browser-facing DTOs MUST expose only fields needed for the customer
   experience. Provider, security, and reconciliation internals remain
   server-side unless deliberately and safely exposed.
4. Shared abstractions require a current cross-slice use. Do not introduce a
   framework, indirection layer, or state-management replacement without a
   traced requirement and architect approval.
5. Data changes MUST have a Flyway-forward production path, rollback or recovery
   reasoning, and test evidence. Do not rely on Hibernate update semantics as the
   production migration strategy.
6. Runtime configuration MUST have one authoritative source per environment.
   Duplicate parallel proxy or backend-address configuration is a review
   finding until its owner and purpose are documented.
7. Defect fixes, accessibility adjustments, and security corrections MUST be
   narrowly scoped and accompanied by regression evidence.

## Delivery and Quality Gates

1. Product specification, architecture, data, and UX design MUST exist before
   the corresponding implementation plan is approved.
2. The specification-quality gate MUST verify complete requirements, explicit
   acceptance-scenario mappings, testable outcomes, clarified constraints, and
   this constitution. A HIGH or CRITICAL finding produces a FAIL verdict.
3. A design or plan gate verdict MUST be binary: PASS or FAIL. Conditional
   passage is prohibited.
4. Every plan item MUST trace to a REQ-XXX and design evidence. Tasks MUST be
   independently executable by one role in one session.
5. Baseline unit, integration, build, and runtime checks MUST run from the
   operative application tree with the repository's primary full commands.
   Scoped commands may be diagnostic, but MUST NOT replace the full build.
6. Any modified backend endpoint MUST be verified at runtime, not only compiled.
   Any modified primary UI flow MUST be exercised in a browser or approved
   runtime-validation harness.
7. A test failure, HIGH or CRITICAL audit finding, failed runtime endpoint,
   unresolved contract break, or unresolved legal/provider claim blocks `[DONE]`.
8. Final completeness review MUST account for all REQ-001 through REQ-063 and
   produce no unknown or untracked production-readiness blockers.

## Governance

This constitution supersedes informal implementation preferences for the
reauthorization workflow. Clarified user constraints are immutable unless the
user or coordinator explicitly amends them in a new approved decision.

Amendments MUST identify the changed principle, classify the semantic version
bump, state the rationale and affected artifacts, and update dependent
traceability. MAJOR is required for incompatible principle removal or
redefinition; MINOR for a new principle or materially expanded obligation;
PATCH for a clarification that does not change compliance meaning.

Every task artifact and review MUST identify consumed upstream evidence.
Constitution violations are CRITICAL and therefore force a quality-gate failure.
The coordinator owns integration of approved amendments; the teamlead owns
wording and deterministic gate consistency; architects and role owners MUST
enforce compliance within their boundaries.

**Version**: 1.0.0 | **Ratified**: 2026-11-23 | **Last Amended**: 2026-11-23

## Upstream Artifacts Consumed

- `.github/modernize/rearchitecture/artifacts/t1-pm.md` — six-wave requirements,
  scope, provider boundaries, compatibility, accessibility, and stack guardrails.
- `.github/modernize/rearchitecture/artifacts/t3-teamlead.md` — the failed
  specification-quality verdict and mandatory-constitution requirement.
- `.github/modernize/rearchitecture/clarification.md` — authoritative target
  stack, user experience, security, provider, compatibility, and completion rules.
- `.github/modernize/rearchitecture/clarification-answers.md` — submitted source
  answers confirming the clarified choices and remaining external dependencies.
- `.github/modernize/rearchitecture/artifacts/project-structure.md` — operative
  `giro-app/` scope and existing layer boundaries.
- `.github/modernize/rearchitecture/artifacts/tech-stack.md` — observed versions,
  build roots, cookie/JWT flow, and current migration/security risks.

## Evidence Mapping

- `t1-pm.md#Clarified Product Guardrails` → Core Principles II–IV and VI.
- `t1-pm.md#Six-Wave Product Definition` → traceability, provider truth,
  security, accessibility, and final-wave legal rules.
- `t3-teamlead.md#Constitution Validation` → canonical constitution creation and
  binary gate governance.
- `clarification.md#Frontend` → Next.js preservation, mobile-first tokens,
  WCAG 2.1 AA, locales, and evergreen-browser constraints.
- `clarification.md#Backend` → Spring Boot 3, API compatibility, cookie/JWT
  hardening, and no-regression rules.
- `clarification.md#Generic` → no real movement, test doubles, all six waves,
  legal-last sequencing, and no commit.
- `project-structure.md#Module Breakdown` → operative scope and layering rules.
- `tech-stack.md#Compatibility and Risk Notes` → migration, BFF, build-root, and
  provider-readiness controls.
