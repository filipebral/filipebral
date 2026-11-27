# Technology Stack

## Frontend
- Next.js 16.4.6, React 19.2.8, React DOM 19.2.8.
- TypeScript 5, strict mode, no emit, bundler module resolution; compile target ES2017.
- Tailwind CSS 4.3.3 via `@tailwindcss/postcss`; all UI styling is utility-class inline with existing dark/green tokens.
- ESLint 9 and `eslint-config-next` 16.4.6; `npm test` runs the checked-in Node test file.
- Node engine is not pinned in `package.json`.

## Backend
- Spring Boot 3.3.4 on Java 17.
- Spring Web, Data JPA, Security OAuth2 Resource Server, Validation, Actuator, AMQP, OpenFeign, Flyway, PostgreSQL, H2, Cloud SQL socket factory, Lombok.
- Spring Cloud 2023.0.3.
- BCrypt 12, Nimbus HS256 JWT encoder/decoder, AES-256-GCM sensitive-column converter.
- Default deployment is PostgreSQL with `ddl-auto:update`; production profile switches Flyway on and Hibernate validation.

## Runtime Configuration
- `GIRO_JWT_SECRET_BASE64` is required by Spring and Next proxy, minimum 32 decoded bytes.
- `GIRO_ENCRYPTION_KEY_BASE64` is required, exactly 32 decoded bytes.
- `GIRO_BACKEND_URL` defaults to `http://localhost:8080` in Next.
- `BAAS_ENABLED` defaults false; `baas.api.url` defaults to local non-prod mock.
- PostgreSQL DB env vars are mandatory in the base backend profile; Rabbit listener auto-startup is false.

## Compatibility and Risk Notes
- HIGH: `giro-app/pom.xml` is a dependency fragment, not a valid aggregator POM; root build must target `giro-app/backend/pom.xml`.
- HIGH: Java package baseline is Spring Boot 3.3.4 while user intent permits the existing Spring Boot 3 major, so future 3.x patch/minor upgrades must preserve Jakarta/Spring Security 6 contracts.
- HIGH: Next auth verification exists in `proxy.ts`, while `SecurityConfig` disables CSRF for a stateless JWT API; same-origin origin checks are only in the proxy, so the BFF seam is security-critical.
- CLEARED: the package now declares `test: node --test lib/*.test.mjs`; all 3 existing Node tests pass.
- CLEARED: the frontend dependency remediation retained Next.js major 16 and the final `npm audit` reports 0 vulnerabilities.
- MEDIUM: npm reports that the resolved ESLint 9.39.5 release is no longer supported by the ESLint support policy; this is a support warning, not a failing audit or build finding.
- MEDIUM: production schema depends on Flyway V1-V6, but non-prod defaults to Hibernate update; drift between entity mappings and migrations is a migration risk.
- HIGH: `clean verify` from `giro-app/backend` now passes 12/12 tests; the prior stale/transient test-classpath blocker is cleared.
- CRITICAL: no provider credentials/contract are configured; account/event paths must remain unavailable or simulated until an approved provider exists.
- All version choices above are observations, not target substitutions.

## Upstream Artifacts Consumed
- `.github/modernize/rearchitecture/clarification.md` — immutable stack-preservation and compatibility constraints.
- `.github/modernize/rearchitecture/artifacts/t2.3-frontend.md` — remediated Next.js 16.4.6 dependency evidence.

## Evidence Mapping
- `clarification.md#Frontend` → preserve Next.js 16 major; do not replace the stack.
- `clarification.md#Backend` → preserve Spring Boot 3 and cookie/JWT flow.
- `t2.3-frontend.md#Dependency and Advisory Results` → Next.js and lint config remain on major 16 with 0 vulnerabilities.
