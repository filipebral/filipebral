# t6 — Architecture, Data, and UX Design Quality Gates

## Verdict

**Aggregate verdict: FAIL**

| Gate | Verdict | Findings |
|---|---|---|
| Architecture design | FAIL | T6-F01, T6-F02, T6-F04 |
| Persistence/data design | FAIL | T6-F01, T6-F02, T6-F03, T6-F04 |
| UX design | FAIL | T6-F02, T6-F05 |

Severity totals: **0 CRITICAL, 4 HIGH, 1 MEDIUM**.

A design gate must be binary. The accepted provider and legal blockers do not
cause a finding, but contradictory contracts and unowned six-wave persistence
coverage block `t7` from producing a deterministic implementation plan.

## Inputs and Scope

Consumed and present:

- `t2-architect.md`
- `t2.2-architect.md`, the binding architecture contract referenced by `t2`
- `t4-dba.md`
- `t5-ux.md`
- `clarification.md`
- `constitution.md`

The workflow-level `context.md`, `decisions.md`, and `team/teamlead/inbox.md`
were not present. They do not replace the submitted task dependencies, but the
missing files are a workflow hygiene warning for the coordinator.

## Gate Criteria

### Architecture

| Check | Result | Evidence |
|---|---:|---|
| Authoritative `giro-app/` runtime and existing stack preserved | PASS | `t2-architect.md#Summary`, `Key Architecture Decisions` |
| Existing external routes and cookie/Bearer boundary frozen | PASS | `t2-architect.md#Wave 1 Architecture Handoff`; `t2.2-architect.md#1` |
| Provider and money safety gates explicit | PASS | `t2.2-architect.md#4–7` |
| Outbox/inbox and idempotency behavior defined | PASS | `t2.2-architect.md#4.2–5` |
| Session browser contract internally consistent | FAIL | `T6-F01` |
| Test-double onboarding truth is unambiguous across consumers | FAIL | `T6-F02` |
| Six-wave backend/data traceability is complete enough for planning | FAIL | `T6-F03`, `T6-F04` |

### Data

| Check | Result | Evidence |
|---|---:|---|
| V1–V6 boundary and additive V7/V8 approach | PASS | `t4-dba.md#Migration Strategy` |
| Session, recovery, throttling, onboarding, operation/outbox/inbox models | PASS | `t4-dba.md#Proposed Logical Model` |
| Integrity, indexes, concurrency, retention, and migration verification | PASS | `t4-dba.md#Integrity and Query Rules`, `Migration Verification Contract` |
| Provider readiness remains configuration-owned | PASS | `t4-dba.md#Scope and Decisions` |
| Session identifier contract matches architecture/UX exposure rules | FAIL | `T6-F01` |
| Persistence coverage/deferment covers all six waves | FAIL | `T6-F03` |
| Requirement-level persistence mapping is complete | FAIL | `T6-F04` |

### UX

| Check | Result | Evidence |
|---|---:|---|
| Existing dark/lime, mobile-first, Portuguese interface preserved | PASS | `t5-ux.md#Design Intent`, `Existing Visual System and Responsive Rules` |
| WCAG 2.1 AA interaction and responsive matrix specified | PASS | `t5-ux.md#WCAG 2.1 AA interaction requirements`, `Automated and Manual Verification Contract` |
| All REQ-001 through REQ-063 referenced | PASS | `t5-ux.md#Requirement Coverage` |
| Provider/unconfigured/ambiguous truth states defined | PASS | `t5-ux.md#UX Guardrails`, Waves 1–6 |
| Test-double provenance rendering is deterministically defined | FAIL | `T6-F02` |
| Exactly one safe next action is used consistently | FAIL | `T6-F05` |

## Findings and Required Remediation

### T6-F01 — Session identifier is both forbidden and required in browser JSON (HIGH)

**Evidence**

- `t2.2-architect.md#1.1` says the JWT `sid` **MUST NOT** appear in browser
  JSON, URLs, storage, logs, telemetry, or analytics.
- `t2.2-architect.md#1.3` requires session-list `id` to match the current token's
  `sid`.
- `t4-dba.md#Identity and session` makes `user_sessions.id` the token `sid`.
- The UX session flow needs a stable value to revoke a listed session, but must
  not display or leak `sid`.

**Risk**

The backend cannot satisfy both normative statements. Implementers would make
different privacy choices for the same response and revocation route.

**Required remediation**

Architect and DBA must define a browser-safe session management handle separate
from the JWT `sid` (or another approved one-way alias). Specify its persistence,
uniqueness, lookup, rotation, URL/JSON treatment, and non-reuse behavior. Update
the session API, DBA table/index rules, tests, and UX handoff consistently.

### T6-F02 — Test-double KYC provenance is not mapped to a truthful UI state (HIGH)

**Evidence**

- `t2.2-architect.md#3.2` permits a KYC test-double adapter to reproduce
  `PROVIDER_REVIEW` and failure outcomes.
- `t4-dba.md#Onboarding` permits onboarding history to carry a truth source and
  `simulated` flag, but does not prohibit `PROVIDER_REVIEW` with test-double
  provenance.
- `t5-ux.md#Onboarding/KYC readiness` labels `PROVIDER_REVIEW` as provider
  review pending, which a test double did not actually perform.

**Risk**

A simulated `PROVIDER_REVIEW` can be rendered as “review pending” and imply an
external provider or legal process exists, contradicting provider truth rules.

**Required remediation**

Define a canonical response tuple for KYC state, for example
`state + truthSource + simulated + providerConfirmed`. Publish an exhaustive
rendering matrix in architecture, DBA constraints, and UX:

- `SIMULATED` must be visibly prefixed as local/test-double behavior.
- A test-double must never produce provider-confirmed language.
- `PROVIDER_REVIEW` in a non-provider source must not be rendered as external
  review; either normalize it to an explicit simulated state or add an
  unambiguous provenance-specific copy/state.

### T6-F03 — Six-wave persistence coverage is only partially designed (HIGH)

**Evidence**

- `t4-dba.md` fully designs sessions, recovery, onboarding, generic provider
  operations, outbox/inbox, and notifications.
- It does not define persistence ownership, compatibility, migrations, or
  explicit deferral decisions for the support/request lifecycle (REQ-036,
  REQ-039), versioned legal/consent/disclosure records (REQ-052, REQ-054),
  limits/review holds, or product-specific local drafts/history needed by
  Waves 3–6.
- A generic `provider_operations.local_entity_type/local_entity_id` is not a
  substitute for a defined local record or an explicit approved deferral.

**Risk**

`t7` cannot assign independently executable persistence tasks for all six
waves without inventing schema responsibilities after the design gate.

**Required remediation**

DBA, with architect sign-off, must either:

1. add the minimal additive local models and verification contracts for the
   remaining approved/test-double capabilities; or
2. produce a requirement-indexed coverage table that maps each requirement to
   an existing table, a new model, or an accepted blocker/placeholder with the
   owner and safe behavior.

Provider configuration must remain runtime-owned; persisted rows must describe
observations, intents, drafts, or legal evidence only.

### T6-F04 — Architecture and data designs lack full REQ traceability (HIGH)

**Evidence**

- `t5-ux.md` references REQ-001–REQ-063.
- `t4-dba.md` evidence mapping covers REQ-004 through REQ-011 only.
- `t2-architect.md` and `t2.2-architect.md` define enforceable contracts but do
  not provide a REQ-to-contract mapping for Waves 2–6.

**Risk**

The future plan can map a task to a design artifact without proving that the
specific requirement is actually designed, violating the constitution's
traceable-delivery requirement.

**Required remediation**

Add a deterministic requirement coverage matrix to the architecture/data gate
handoff. For every REQ-001–REQ-063, identify:

- architecture contract or accepted external blocker;
- persistence contract or no-persistence rationale;
- UX contract;
- planned verification evidence class.

Accepted blockers must remain explicit and must not be represented as coverage
by a configured provider.

### T6-F05 — Ambiguous flows expose multiple “next actions” (MEDIUM)

**Evidence**

- `t5-ux.md#Reusable interaction patterns` requires one allowed next action per
  provider state.
- Wave 2 account ambiguity and Wave 3 timeout/ambiguity list combinations such
  as status check/support or wait/reconcile/support.

**Risk**

Multiple choices weaken the safe-action rule and can invite an unsafe
resubmission or inconsistent implementation.

**Required remediation**

Choose one primary state action for every ambiguous, pending, failed, and
unconfigured combination. Support remains globally reachable, but must not
appear as a competing primary action unless the specification assigns it as
the sole action.

## Accepted Conditions (Not Findings)

- No approved KYC, recovery, BaaS, Rabbit, money, ledger, card, credit,
  investment, or insurance provider exists.
- Real-money operations remain disabled, configuration-gated, simulated, or
  explicit placeholders.
- Existing V1–V6 migrations remain immutable.
- Licensing/legal execution remains the final wave.
- No commit is authorized.

These conditions are accepted by the submitted clarification and constitution;
they are not design-gate failures.

## Required Gate Rerun

The gate becomes eligible for rerun when architect, DBA, and UX owners have
remediated T6-F01 through T6-F05 in their task artifacts. No plan approval
should proceed while any HIGH finding remains.

## Test Results

- Command: PowerShell here-string piped to `python -` for artifact structure,
  requirement-ID, tail-section, and cross-contract token checks.
- Artifacts loaded: 5/5 required design/governance inputs plus `t2.2-architect.md`.
- Structural sections: all required section titles found.
- UX requirement IDs: 63/63 (`REQ-001` through `REQ-063`).
- Architecture cross-contract checks: FAILED (session identifier contradiction).
- Data cross-wave coverage: FAILED (unmapped/deferred persistence scope).
- UX provenance/action checks: FAILED (test-double provenance and multi-action).
- Gate findings: 0 CRITICAL, 4 HIGH, 1 MEDIUM.
- Runtime/source tests: not applicable; this is a read-only planning gate.

## Upstream Artifacts Consumed

- `.github/modernize/rearchitecture/artifacts/t2-architect.md` — architecture
  index, frozen contracts, blockers, and Wave 1 handoff.
- `.github/modernize/rearchitecture/artifacts/t2.2-architect.md` — detailed
  session, recovery, KYC, operation, event, migration, and conformance contracts.
- `.github/modernize/rearchitecture/artifacts/t4-dba.md` — additive persistence
  model, constraints, migrations, verification, and residual blockers.
- `.github/modernize/rearchitecture/artifacts/t5-ux.md` — six-wave interaction
  states, accessibility, responsive requirements, and full REQ coverage.
- `.github/modernize/rearchitecture/clarification.md` — submitted stack,
  compatibility, provider, UX, no-money, legal-last, and no-commit decisions.
- `.github/modernize/rearchitecture/constitution.md` — authoritative migration,
  security, provider-truth, traceability, accessibility, and gate rules.

## Evidence Mapping

- `t2.2-architect.md#1.1 + #1.3` → `T6-F01`
- `t2.2-architect.md#3.2` + `t4-dba.md#Onboarding` + `t5-ux.md#4. Onboarding/KYC readiness` → `T6-F02`
- `t4-dba.md#Proposed Logical Model` + `t1-pm.md` Wave 2–6 requirements → `T6-F03`
- `t4-dba.md#Evidence Mapping` + `t2/t2.2` absence of REQ-001–REQ-063 matrix + `t5-ux.md#Requirement Coverage` → `T6-F04`
- `t5-ux.md#Reusable interaction patterns` + provider ambiguity flows → `T6-F05`
- `clarification.md#Generic` + `constitution.md#IV` → accepted no-money/provider conditions
- `constitution.md#Delivery and Quality Gates` → binary aggregate FAIL

