# t2 — Next.js and Spring Boot Architecture Analysis

## Summary
The tracked runnable application is the nested `giro-app/` full-stack project. Eleven runtime units are documented with source-anchored behavior, bindings, API contracts, state flow, migration boundary, and split candidates. Submitted clarification answers are authoritative. The remediated frontend baseline is Next.js 16.4.6 with a clean dependency audit. Wave 1 implementation must center on the cookie/JWT BFF boundary, Spring Security, provider-safe onboarding, and the enforceable additive session/recovery/KYC-ready contracts in `t2.2-architect.md`.

## Key Architecture Decisions
- The tracked `giro-app/` tree is authoritative; workspace-root duplicate source directories are not implementation scope.
- Keep the Next same-origin BFF and HttpOnly cookie; send Bearer JWT only from server-side proxy.
- Keep Spring Boot 3.3.4 and Java 17 for this brownfield phase; no target-version substitution.
- Keep Next.js on its current major 16; the remediated baseline is Next.js/`eslint-config-next` 16.4.6 and remains React 19.2.8.
- Preserve `/api/auth/*`, `/accounts`, `/simulation/*`, `/api/split-*`, and their JSON components.
- Keep account/provider and Rabbit/event behavior behind provider-safe adapters and test doubles.
- `BAAS_ENABLED=false` and Rabbit listener auto-start false are safety gates; real money movement remains blocked until provider contract/configuration/approval exists.
- Existing V1-V6 Flyway migrations are immutable; future DB additions use new migrations after DBA design.
- A 15-minute HS256 JWT is currently self-contained. Session listing/revocation cannot be implemented safely from the token alone; a backward-compatible session identifier/registry is a Wave 1 design dependency.
- Public Next auth handlers must gain an explicit same-origin mutation policy before Wave 1 completion; only the authenticated proxy currently enforces Origin.
- Recovery, KYC-ready onboarding, and notification attempts require additive contracts and persistence. They must remain provider-unconfigured unless an approved channel/provider is supplied.
- `t2.2-architect.md` is the binding Wave 1 contract for `sid`-based revocation, recovery states, KYC-ready states, operation/outbox/inbox idempotency, truthful error states, and provider configuration gates.

## Deliverables
- [architecture_index.md](./architecture_index.md) — implementation guide with per-unit read/filter/evidence instructions.
- [unit_graph.yaml](./unit_graph.yaml) — 11-unit runtime spine, signatures, dependencies, dynamic entrypoints, and shared refs.
- [migration_boundary.yaml](./migration_boundary.yaml) — phased implementation boundary.
- [wire_contracts.yaml](./wire_contracts.yaml) — frozen/additive API, provider, and event contracts.
- [shared_modules.yaml](./shared_modules.yaml) — cross-unit contracts and migration strategy.
- [cross_unit_state.yaml](./cross_unit_state.yaml) — cookie/JWT, ownership, idempotency, and provider gate flows.
- [seams.yaml](./seams.yaml) — BFF, provider, and Rabbit seam rules.
- [project-structure.md](./project-structure.md) — runnable root, modules, layers, entrypoints.
- [tech-stack.md](./tech-stack.md) — observed versions, runtime config, compatibility risks.
- [data-model.md](./data-model.md) — existing entity/schema/transaction inventory.
- [t2.2-architect.md](./t2.2-architect.md) — binding security/provider/idempotency/migration/onboarding contracts for Wave 1.
- [t2.3-frontend.md](./t2.3-frontend.md) — remediated Next.js dependency baseline and validation evidence.
- [units/next-auth-experience/behavior.yaml](./units/next-auth-experience/behavior.yaml) — login/register page behavior.
- [units/next-auth-experience/bindings.yaml](./units/next-auth-experience/bindings.yaml) — route/framework bindings.
- [units/next-auth-experience/unit_decomposition.yaml](./units/next-auth-experience/unit_decomposition.yaml) — split candidates.
- [units/next-dashboard-experience/behavior.yaml](./units/next-dashboard-experience/behavior.yaml) — dashboard/modal behavior.
- [units/next-dashboard-experience/bindings.yaml](./units/next-dashboard-experience/bindings.yaml) — dashboard bindings.
- [units/next-dashboard-experience/unit_decomposition.yaml](./units/next-dashboard-experience/unit_decomposition.yaml) — dashboard split candidates.
- [units/next-auth-bff/behavior.yaml](./units/next-auth-bff/behavior.yaml) — BFF status/cookie behavior.
- [units/next-auth-bff/bindings.yaml](./units/next-auth-bff/bindings.yaml) — BFF routes/config.
- [units/next-auth-bff/unit_decomposition.yaml](./units/next-auth-bff/unit_decomposition.yaml) — BFF split candidates.
- [units/next-api-boundary/behavior.yaml](./units/next-api-boundary/behavior.yaml) — proxy/origin/dashboard guard behavior.
- [units/next-api-boundary/bindings.yaml](./units/next-api-boundary/bindings.yaml) — proxy/matcher/config bindings.
- [units/next-api-boundary/unit_decomposition.yaml](./units/next-api-boundary/unit_decomposition.yaml) — boundary split candidates.
- [units/backend-auth-api/behavior.yaml](./units/backend-auth-api/behavior.yaml) — Spring auth behavior.
- [units/backend-auth-api/bindings.yaml](./units/backend-auth-api/bindings.yaml) — Spring auth/security bindings.
- [units/backend-auth-api/unit_decomposition.yaml](./units/backend-auth-api/unit_decomposition.yaml) — auth split candidates.
- [units/backend-account-provider-api/behavior.yaml](./units/backend-account-provider-api/behavior.yaml) — account/provider behavior.
- [units/backend-account-provider-api/bindings.yaml](./units/backend-account-provider-api/bindings.yaml) — account/provider bindings.
- [units/backend-account-provider-api/unit_decomposition.yaml](./units/backend-account-provider-api/unit_decomposition.yaml) — provider seam split candidates.
- [units/backend-simulation-api/behavior.yaml](./units/backend-simulation-api/behavior.yaml) — simulated ledger behavior.
- [units/backend-simulation-api/bindings.yaml](./units/backend-simulation-api/bindings.yaml) — simulation route/config bindings.
- [units/backend-simulation-api/unit_decomposition.yaml](./units/backend-simulation-api/unit_decomposition.yaml) — simulation domain split candidates.
- [units/backend-split-api/behavior.yaml](./units/backend-split-api/behavior.yaml) — split group/transaction behavior.
- [units/backend-split-api/bindings.yaml](./units/backend-split-api/bindings.yaml) — split route bindings.
- [units/backend-split-api/unit_decomposition.yaml](./units/backend-split-api/unit_decomposition.yaml) — split split candidates.
- [units/backend-simulation-worker/behavior.yaml](./units/backend-simulation-worker/behavior.yaml) — scheduled simulation behavior.
- [units/backend-simulation-worker/bindings.yaml](./units/backend-simulation-worker/bindings.yaml) — scheduler bindings/config.
- [units/backend-simulation-worker/unit_decomposition.yaml](./units/backend-simulation-worker/unit_decomposition.yaml) — scheduler split candidates.
- [units/backend-split-event-consumer/behavior.yaml](./units/backend-split-event-consumer/behavior.yaml) — Rabbit consumer behavior.
- [units/backend-split-event-consumer/bindings.yaml](./units/backend-split-event-consumer/bindings.yaml) — Rabbit bindings/config.
- [units/backend-split-event-consumer/unit_decomposition.yaml](./units/backend-split-event-consumer/unit_decomposition.yaml) — event seam split candidates.
- [units/backend-mock-baas-api/behavior.yaml](./units/backend-mock-baas-api/behavior.yaml) — non-prod provider double behavior.
- [units/backend-mock-baas-api/bindings.yaml](./units/backend-mock-baas-api/bindings.yaml) — Feign/profile bindings.
- [units/backend-mock-baas-api/unit_decomposition.yaml](./units/backend-mock-baas-api/unit_decomposition.yaml) — test-double split candidates.

## Test Results
- Architecture self-check command: PyYAML parse plus unit/shared-ref/cross-state/decomposition assertions.
- Passed: 40 YAML files parsed; 11/11 units have behavior, bindings, and decomposition; shared refs, runtime confirmations, duplicate anchors, and `commit: false` checks passed.
- Failed: 0 architecture checks.
- Node command: `node --test giro-app/lib/financial-simulations.test.mjs`
- Passed: 3
- Failed: 0
- Skipped: 0
- Frontend install: `npm ci` in `giro-app`; return code 0; final audit is 0 vulnerabilities (0 critical/high/moderate/low).
- Frontend full build: `npm run build` in `giro-app`; return code 0.
- Frontend unit tests: `npm test`; passed 3, failed 0, skipped 0.
- Frontend startup revalidation: `npx next start -p 3100`; ready in 2.6 s; `GET /` returned 200 with 8293 bytes and `text/html; charset=utf-8`.
- Backend full command: `.\mvnw.cmd -B clean verify` in `giro-app/backend`; return code 0.
- Backend passed: 55 main sources, 3 test sources, 12 tests, and executable JAR packaging.
- Backend failed: 0.

## Baseline Blockers
- CLEARED: the prior backend test-classpath failure is not reproducible in the authoritative nested backend tree. `t2.1-backend.md` records a clean 12/12 `clean verify`.
- CLEARED: t2.3 upgraded Next.js and `eslint-config-next` within major 16 (`16.3.8` → `16.4.6`), added the checked-in Node test command, and verified a 0-vulnerability audit.
- HIGH: no approved BaaS/provider contract, credentials, sandbox, or idempotency specification exists; account and event money paths remain gated.
- HIGH: backend uses Hibernate `ddl-auto:update` outside production and Flyway validation in production; DBA must define additive migration handling before persistence changes.
- HIGH: stateless JWTs have no revocation/session inventory; Wave 1 session requirements depend on a session registry design that preserves the existing cookie and claims.
- HIGH: public login/register BFF mutations do not apply the Origin rule already used by the authenticated proxy; implementation must close that gap without changing public JSON contracts.
- HIGH: account creation combines a provider call and Rabbit publication inside a local DB transaction. Provider/event side effects are not rollbackable, so retries and reconciliation require explicit idempotency rather than relying on transaction rollback.
- MEDIUM: no KYC, recovery channel, or notification provider contract/configuration is available. Only KYC-ready and in-product/test-double states may be completed now.

## Upstream Artifacts Consumed
- `.github/modernize/rearchitecture/artifacts/t2.1-backend.md` — cleared backend baseline and full Maven verification.
- `.github/modernize/rearchitecture/artifacts/t2.2-architect.md` — enforceable session, recovery, KYC, provider, idempotency, event, and migration contracts.
- `.github/modernize/rearchitecture/artifacts/t2.3-frontend.md` — patched Next.js baseline and final frontend audit/build/test/runtime verification.
- `.github/modernize/rearchitecture/clarification.md` — accepted stack, UX, API, security, provider, and legal constraints.
- `.github/modernize/rearchitecture/clarification-answers.md` — submitted answers with `status: submitted`.

## Evidence Mapping
- `t2.1-backend.md#Test Results` → backend smoke gate updated to 12 passed, 0 failed.
- `t2.2-architect.md#Downstream Conformance Gates` → Wave 1 DBA/backend/frontend/tester acceptance and provider-safety requirements.
- `t2.3-frontend.md#Dependency and Advisory Results` → current-major Next.js baseline with 0 vulnerabilities.
- `clarification.md#Frontend` → `unit_graph`, frontend bindings, BFF/proxy wire contracts.
- `clarification.md#Backend B2-B5` → API wire contracts, security shared module, migration boundary, provider seams.
- `clarification.md#Generic G1-G3` → phased six-wave boundary, no-money gate, existing behavior preservation.
- `clarification-answers.md#B4` → cookie/JWT and Spring Security decisions in seam/security artifacts.

## Wave 1 Architecture Handoff
- Preserve `POST /api/auth/login|register`, `/api/auth/logout`, token/`expiresIn`/`user` server contract, and browser-visible `{user}` response.
- Preserve `giro_access_token` as HttpOnly, SameSite=Lax, Path=/ and Secure in production; rotate/expire through the existing BFF.
- Additive session state must not expose JWTs or secret material to the browser. A stable opaque session/revocation identity may be embedded in a new token claim while retaining required `iss`, `aud`, `sub`, `name`, `iat`, and `exp`.
- Recovery and KYC-ready contracts are additive and must distinguish simulated, pending, failed, unconfigured, approved, and rejected states without claiming verification.
- Provider and event transports remain behind existing gateway seams. Real operation stays disabled without approved provider configuration and an idempotency key.
- DBA, UX, teamlead, frontend, backend, and tester roles must consume the per-unit/global contracts before their respective implementation or validation tasks.
- Architecture/design/plan handoff remains gated: DBA and UX must complete their designs before teamlead runs the combined design quality gate and generates the six-wave plan/tasks.
