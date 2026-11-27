# Giro Architecture Index

## Implementation Guide

This index is not the full contract. Do not implement from this file alone; follow the artifact paths below.

### Global artifacts
- `unit_graph.yaml` — all implementation workers; use for unit boundaries, triggers, dependencies, exported signatures, dynamic entrypoints, and shared references. Select the assigned unit and treat `source_anchors` as evidence, not rewrite targets.
- `migration_boundary.yaml` — all implementation workers; use `must_rewrite`, `copy_as_is`, and `legacy_allowed_to_remain` to determine scope.
- `wire_contracts.yaml` — API/provider/event workers; filter rows where `unit` equals the assigned unit or is `global`.
- `shared_modules.yaml` — workers touching shared code; filter `used_by_units` and honor `fields` subset whitelist from `unit_graph`.
- `cross_unit_state.yaml` — session/idempotency/ownership work; filter flows whose `writer.unit` or `reader.unit` equals the assigned unit.
- `seams.yaml` — BFF/provider/event work; filter `cut_between`; declared seams and `frozen_side_rule` are binding.
- `project-structure.md` — use to resolve tracked runnable root and module boundaries.
- `tech-stack.md` — use to preserve exact existing majors/configuration and identify inline blockers.
- `data-model.md` — use before schema changes; this is inventory, not redesign permission.
- `t2.2-architect.md` — **binding Wave 1 design supplement** for session `sid` revocation, public-mutation Origin policy, recovery, KYC-ready states, provider idempotency/outbox/inbox, migration constraints, error truthfulness, and downstream conformance gates.

### Unit consumption
For every unit below, read its three per-unit files before implementation. Implement every `must_preserve: true` or `must_appear_in_target: true` row. Read relevant global rows by the filter described above. `unit_decomposition.yaml` is advisory only (`commit: false`). Security, recovery, KYC, provider, idempotency, or schema work must also read `t2.2-architect.md`; its mandatory contract is not superseded by the structured baseline inventory.

### Unit: next-auth-experience
- external trigger: `GET /`, login/register submissions.
- must read: `units/next-auth-experience/behavior.yaml`, `units/next-auth-experience/bindings.yaml`, `units/next-auth-experience/unit_decomposition.yaml`.
- relevant global: auth BFF wire row; security-cookie state/flow.
- before DONE report: artifacts read, preserved form branches and redirects, WCAG evidence, and E2E auth result.

### Unit: next-dashboard-experience
- external trigger: `GET /dashboard` and modal/simulation actions.
- must read: `units/next-dashboard-experience/behavior.yaml`, `units/next-dashboard-experience/bindings.yaml`, `units/next-dashboard-experience/unit_decomposition.yaml`.
- relevant global: simulation/split shared rows, proxy state, provider gate flow.
- before DONE report: UI/API branches, no-real-money wording, responsive/WCAG evidence, browser matrix.

### Unit: next-auth-bff
- external trigger: `POST /api/auth/login|register|logout`.
- must read: `units/next-auth-bff/behavior.yaml`, `units/next-auth-bff/bindings.yaml`, `units/next-auth-bff/unit_decomposition.yaml`.
- relevant global: auth wire row, cookie flow, cookie-to-bearer seam.
- before DONE report: cookie attributes, status mapping, malformed/unavailable response tests.

### Unit: next-api-boundary
- external trigger: `/api/proxy/[...path]` and `/dashboard` proxy.
- must read: `units/next-api-boundary/behavior.yaml`, `units/next-api-boundary/bindings.yaml`, `units/next-api-boundary/unit_decomposition.yaml`.
- relevant global: proxy wire row, JWT/cookie flows, BFF seam.
- before DONE report: origin/Cookie/Bearer behavior, 401/403/502 tests, dashboard redirect.

### Unit: backend-auth-api
- external trigger: `POST /api/auth/register|login`.
- must read: `units/backend-auth-api/behavior.yaml`, `units/backend-auth-api/bindings.yaml`, `units/backend-auth-api/unit_decomposition.yaml`.
- relevant global: auth wire row, security/user shared modules, JWT subject flows.
- before DONE report: exact JSON/status compatibility, encrypted/HMAC data evidence, session hardening tests.

### Unit: backend-account-provider-api
- external trigger: `POST /accounts`.
- must read: `units/backend-account-provider-api/behavior.yaml`, `units/backend-account-provider-api/bindings.yaml`, `units/backend-account-provider-api/unit_decomposition.yaml`.
- relevant global: provider wire row, provider seam, provider gate state.
- before DONE report: 503 gate, test-double evidence, no-real-money evidence, idempotency/recovery decisions.

### Unit: backend-simulation-api
- external trigger: authenticated `/simulation/*`.
- must read: `units/backend-simulation-api/behavior.yaml`, `units/backend-simulation-api/bindings.yaml`, `units/backend-simulation-api/unit_decomposition.yaml`.
- relevant global: simulation wire row, simulation shared module, request-ID flow.
- before DONE report: idempotency/conflict/balance transaction tests and simulated-only labeling.

### Unit: backend-split-api
- external trigger: `POST /api/split-groups|split-transactions`.
- must read: `units/backend-split-api/behavior.yaml`, `units/backend-split-api/bindings.yaml`, `units/backend-split-api/unit_decomposition.yaml`.
- relevant global: split wire row/shared module and JWT ownership flow.
- before DONE report: 100% validation, owner 403, HALF_EVEN allocation, persistence evidence.

### Unit: backend-simulation-worker
- external trigger: Spring fixed-delay scheduler.
- must read: `units/backend-simulation-worker/behavior.yaml`, `units/backend-simulation-worker/bindings.yaml`, `units/backend-simulation-worker/unit_decomposition.yaml`.
- relevant global: simulation shared module and schedule state.
- before DONE report: due selection, lock/transaction behavior, status transitions, per-item failure isolation.

### Unit: backend-split-event-consumer
- external trigger: Rabbit `giro.split-bills.queue`.
- must read: `units/backend-split-event-consumer/behavior.yaml`, `units/backend-split-event-consumer/bindings.yaml`, `units/backend-split-event-consumer/unit_decomposition.yaml`.
- relevant global: event wire row, provider/split seam and shared modules.
- before DONE report: listener stays disabled without provider contract; event idempotency test; no provider call.

### Unit: backend-mock-baas-api
- external trigger: non-prod `POST /mock-baas/accounts`.
- must read: `units/backend-mock-baas-api/behavior.yaml`, `units/backend-mock-baas-api/bindings.yaml`, `units/backend-mock-baas-api/unit_decomposition.yaml`.
- relevant global: provider wire row/seam/shared module.
- before DONE report: prod profile exclusion, exact fake response, provider test-double contract.

## Upstream Artifacts Consumed
- `.github/modernize/rearchitecture/clarification.md` — authoritative submitted-answer summary.
- `.github/modernize/rearchitecture/clarification-answers.md` — submitted user answers.
- `.github/modernize/rearchitecture/artifacts/t2.2-architect.md` — binding remediation contracts for the unresolved security/provider/onboarding findings.

## Evidence Mapping
- `clarification.md#Frontend` → Next/current major, WCAG, browser, mobile-first, existing tokens in all frontend units.
- `clarification.md#Backend` → API compatibility, cookie/JWT, provider-safe gate, no regression in backend units and seams.
- `clarification.md#Generic` → six-wave scope, simulation-only money, legal/licensing last, no commit.
- `t2.2-architect.md#1-7` → Wave 1 security/session/recovery/KYC/provider/event/migration contracts.
