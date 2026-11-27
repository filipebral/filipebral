# Project Structure

## Classification
- `fullstack`: Next.js application and Spring Boot API share one git repository, with the runnable application rooted at `giro-app/`.
- The workspace-level top `app/`, `backend/`, `components/`, and `lib/` paths are not the tracked runnable tree; implementation scope must use `giro-app/`.

## Module Breakdown
- `giro-app/app/`: Next.js 16 App Router pages and same-origin route handlers.
- `giro-app/components/`: client-side banking dashboard, simulation, financial calculator, and split modals.
- `giro-app/lib/`: BFF auth cookie adapter and deterministic financial simulation functions.
- `giro-app/backend/src/main/java/...`: one Spring Boot package containing controllers, services/use cases, entities, repositories, security, BaaS Feign gateway, RabbitMQ config, and scheduler.
- `giro-app/backend/src/main/resources/db/migration/`: six Flyway PostgreSQL migrations.
- Tests: 1 Node `.mjs` financial-simulation test; 3 Spring JPA/service/configuration test classes.

## Layers
- Frontend: pages/components → route handlers/BFF → backend HTTP.
- Backend: controllers/listener/scheduler → services/use cases → repositories/JPA and provider gateway/event publisher.
- Persistence: PostgreSQL production, Flyway production schema, H2 available for tests.

## Entry Points
- Next: `/`, `/dashboard`, `/api/auth/*`, `/api/proxy/[...path]`, `proxy.ts`.
- Spring: `BackendApplication`; REST controllers; `SimulationScheduler`; `SplitBillListener`.

## Functional Areas
- Cookie/JWT authentication
- Account creation and BaaS-provider bridge
- Simulated balance, transfer, Pix, schedule, bill, and statement
- Financial loan/investment calculators
- Split groups and split transactions
- Provider/event test doubles

## Upstream Artifacts Consumed
- `.github/modernize/rearchitecture/clarification.md` — stack, compatibility, accessibility, provider, and no-commit constraints.

## Evidence Mapping
- `clarification.md#Backend` → Spring 3, cookie/JWT hardening, external API compatibility.
- `clarification.md#Frontend` → current Next.js major, existing styling/tokens, mobile-first and WCAG 2.1 AA.
