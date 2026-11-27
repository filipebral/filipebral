# t4 - Persistence Design and Migration Strategy

## Summary

This design adds two forward-only Flyway migrations after the immutable V1-V6 baseline. The first introduces session, recovery, rate-limit, onboarding, and security-audit persistence; the second introduces provider-operation, immutable-attempt, outbox, inbox, and notification persistence. All writes are additive, all sensitive values are absent or transformed, provider readiness remains configuration-owned, and test-double records can never be represented as provider-confirmed truth.

## Scope and Decisions

- Authoritative runtime: `giro-app/backend`.
- Existing V1-V6 migrations and existing tables remain byte-for-byte and source compatible.
- User decision A is preserved: simulated/test-double provider behavior is accepted; absent or ambiguous external-provider capabilities remain explicit blockers.
- `BAAS_ENABLED=false` and disabled Rabbit listener behavior remain the default. No schema state is considered permission to move real money.
- Runtime provider configuration, credentials, approval, adapter selection, and legal enablement are not stored in database “readiness” rows. Persisted operation rows record observed state only; they do not create readiness.
- PostgreSQL production timestamps use `TIMESTAMP WITH TIME ZONE`. Application and database sessions use UTC.
- The design is for one Spring Boot 3.3.4 backend and one PostgreSQL schema; it does not introduce a distributed database boundary.

## Proposed Logical Model

### Identity and session

| Table | Purpose | Critical columns and constraints | Main access paths |
|---|---|---|---|
| `user_sessions` | Server-recognized JWT `sid` and revocation inventory | `id UUID PK`; `user_id UUID NOT NULL FK app_users(id)`; `status VARCHAR(16)` in `ACTIVE/REVOKED/EXPIRED`; `context_label VARCHAR(160)`; `created_at`, `last_used_at`, `expires_at`, `revoked_at`, `revocation_reason_code VARCHAR(40)`; `row_version BIGINT`; checks for temporal order and revocation consistency; no token/password/secret columns | Current user active sessions ordered by expiry; `sid` plus user lookup; expiry sweeper |
| `recovery_channels` | Verified, non-raw recovery destinations and provider-owned references | `id UUID PK`; `user_id FK`; `channel_type VARCHAR(24)`; `destination_reference_ciphertext VARCHAR(1024)`; `destination_fingerprint CHAR(64)`; `status VARCHAR(16)`; `verified_at`; timestamps and row version; unique `(user_id, channel_type, destination_fingerprint)` | Active channels by user; fingerprint conflict detection |
| `recovery_requests` | Single-use recovery and support-assisted lifecycle | `id UUID PK`; nullable `user_id FK`; nullable `channel_id FK`; `recovery_path VARCHAR(32)`; `target_fingerprint CHAR(64)`; `verification_secret_hash CHAR(64)`; `reset_authorization_hash CHAR(64)` nullable; `state VARCHAR(24)`; issuance/expiry/use/invalidation timestamps; bounded reason code; optional `superseded_by_id`; per-state temporal checks | Latest target request; verify by opaque id; expiry sweeper; user support view |
| `security_attempt_buckets` | Concurrency-safe generic throttling without exposing identity | Composite PK `(purpose, target_fingerprint)`; `window_started_at`, `failure_count`, `blocked_until`, `updated_at`; purpose is `SIGN_IN` or `RECOVERY_VERIFY` | Atomic UPSERT by HMAC target; blocked-until read; retention cleanup |
| `security_audit_events` | Append-only user-visible and support-safe security confirmations | `id UUID PK`; nullable `user_id FK`; `actor_category`, `action_code`, `outcome_code`, `target_type`, nullable `target_reference`, `reason_code`, `correlation_id`, `occurred_at`; no secret/provider-message payload | User confirmation timeline; support correlation; bounded retention |

`user_sessions.id` is the opaque `sid` claim. It must not be derived from or exposed as a raw JWT. Issuance creates the row in the same transaction that authorizes token return; if the row cannot be committed, no browser cookie is issued. Legacy JWTs without `sid` never receive a row and cannot be renewed.

`verification_secret_hash` and `reset_authorization_hash` are salted hashes of high-entropy values. Neither the code nor reset authorization is logged. Marking a request terminal clears or nulls its secret/hash material after the required audit metadata is retained.

The recovery state check includes `REQUESTED`, `CHANNEL_UNCONFIGURED`, `DELIVERED`, `VERIFIED`, `RESET`, `EXPIRED`, `CANCELLED`, `FAILED`, and `SUPPORT_ASSISTED_RECOVERY`. `CHANNEL_UNCONFIGURED` and `SUPPORT_ASSISTED_RECOVERY` are truthful terminal/pending outcomes, not generic authentication failures.

### Onboarding

| Table | Purpose | Critical columns and constraints | Main access paths |
|---|---|---|---|
| `onboarding_cases` | Current customer onboarding/KYC-ready state | `id UUID PK`; `user_id UUID NOT NULL UNIQUE FK`; `current_state VARCHAR(32)`; `truth_source VARCHAR(24)` in `LOCAL/TEST_DOUBLE/PROVIDER/SYSTEM/OPERATOR`; `provider_adapter VARCHAR(64)` nullable; encrypted `provider_reference_ciphertext` and `evidence_reference_ciphertext`; `simulated BOOLEAN`; `last_provider_check_at`; timestamps; row version; check that `SIMULATED` cannot coexist with provider-approved truth and that `APPROVED/REJECTED` requires `PROVIDER` | One current case by user; state/provider reconciliation |
| `onboarding_state_history` | Immutable state transitions and safe reasons | `id UUID PK`; `case_id UUID NOT NULL FK`; transition number; from/to state; truth source; provider adapter/reference; bounded `reason_code`; optional correlation id; `occurred_at`; unique `(case_id, transition_number)` | Chronological case history; provider-callback audit |

New registrations initially persist `REGISTERED`. If the provider or legal gate is inactive, the system may add a `NOT_CONFIGURED` transition with `truth_source=SYSTEM`; it must not synthesize evidence, provider review, or approval. Existing users need no destructive backfill: a transactional lazy-create operation creates their `NOT_CONFIGURED` case on first onboarding access. A later optional data backfill, if desired for analytics, must be a separately reviewed `INSERT ... ON CONFLICT` migration.

### Provider operation, event, and notification safety

| Table | Purpose | Critical columns and constraints | Main access paths |
|---|---|---|---|
| `provider_operations` | Stable local intent and provider-safe state | `id UUID PK`; `operation_type VARCHAR(48)`; `idempotency_key VARCHAR(100)`; `payload_hash CHAR(64)`; `user_id UUID FK`; `local_entity_type VARCHAR(48)`; `local_entity_id UUID` nullable; `provider_adapter VARCHAR(64)`; `state VARCHAR(24)`; encrypted provider reference; `operation_version SMALLINT`; `last_error_code`; created/submitted/completed/reconcile timestamps; row version; unique `(operation_type, idempotency_key)`; no raw payload or secret | Same-key lookup; ambiguous/due reconciliation; state dashboards |
| `provider_operation_attempts` | Immutable call/result history | `id UUID PK`; operation FK; attempt number; outcome; safe error code; correlation id; provider-response fingerprint; started/completed timestamps; unique `(operation_id, attempt_number)` | Complete operation history; duplicate/timeout analysis |
| `outbox_events` | Transactional intent before external publication | `id UUID PK`; unique `event_id UUID`; optional operation FK; aggregate type/id; event type/schema version; minimized JSON payload stored as `TEXT`; correlation id; state; occurred/available/claimed/lease/completed timestamps; attempt count and safe last error; no broker delivery id as identity | Claim due pending rows atomically; operation/event correlation; dead-letter review |
| `inbox_events` | Idempotent at-least-once consumer identity | `id UUID PK`; `consumer_name`; `event_id`; optional operation FK; state; received/claimed/completed timestamps; attempt count and safe error; unique `(consumer_name, event_id)` | Insert-before-work deduplication; event retry/reconciliation |
| `notification_attempts` | Truthful in-product/provider delivery status | `id UUID PK`; user FK; audit event FK nullable; operation FK nullable; channel; truth source; state in `NOT_CONFIGURED/PENDING/DELIVERED/FAILED`; encrypted delivery reference; safe error; correlation; attempt and completion timestamps | Customer confirmation; provider-not-configured state; duplicate delivery detection |

Allowed operation states are `PENDING`, `SUBMITTED`, `ACCEPTED`, `SUCCEEDED`, `REJECTED`, `FAILED`, `CANCELLED`, and `AMBIGUOUS`. `test double` is an operation provenance, not a success state. Rows created by a test double cannot transition an onboarding case to provider `APPROVED`, activate a real account, or publish a customer-visible real-money result.

## Entity Relationship Diagram

<!-- mermaid-checked: every attribute is `<type> <name> [<key>] ["<description>"]` with at most one of PK/FK/UK, no \n in descriptions, no {} in descriptions, every relationship label is double-quoted -->
```mermaid
erDiagram
    AppUser ||--o{ UserSession : "owns"
    AppUser ||--o{ RecoveryChannel : "owns"
    AppUser o|--o{ RecoveryRequest : "requests"
    RecoveryChannel o|--o{ RecoveryRequest : "delivers"
    AppUser ||--o| OnboardingCase : "has"
    OnboardingCase ||--o{ OnboardingHistory : "records"
    AppUser ||--o{ ProviderOperation : "initiates"
    ProviderOperation ||--o{ ProviderAttempt : "records"
    ProviderOperation ||--o{ OutboxEvent : "produces"
    ProviderOperation o|--o{ InboxEvent : "correlates"
    AppUser o|--o{ SecurityAuditEvent : "appears on"
    SecurityAuditEvent o|--o{ NotificationAttempt : "confirms"

    AppUser {
        uuid id PK
        string name
        string passwordHash
    }
    UserSession {
        uuid id PK "JWT sid"
        uuid userId FK
        string status
        timestamptz createdAt
        timestamptz expiresAt
    }
    RecoveryChannel {
        uuid id PK
        uuid userId FK
        string channelType
        string destinationFingerprint
        string status
    }
    RecoveryRequest {
        uuid id PK
        uuid userId FK
        uuid channelId FK
        string state
        string targetFingerprint
        timestamptz expiresAt
    }
    OnboardingCase {
        uuid id PK
        uuid userId FK UK
        string currentState
        string truthSource
        boolean simulated
    }
    OnboardingHistory {
        uuid id PK
        uuid caseId FK
        string toState
        string truthSource
        timestamptz occurredAt
    }
    ProviderOperation {
        uuid id PK
        uuid userId FK
        string operationType
        string idempotencyKey UK
        string payloadHash
        string state
    }
    ProviderAttempt {
        uuid id PK
        uuid operationId FK
        int attemptNumber
        string outcome
    }
    OutboxEvent {
        uuid id PK
        uuid eventId UK
        uuid operationId FK
        string state
        timestamptz availableAt
    }
    InboxEvent {
        uuid id PK
        uuid eventId UK
        string consumerName
        string state
    }
    SecurityAuditEvent {
        uuid id PK
        uuid userId FK
        string actionCode
        string outcomeCode
        timestamptz occurredAt
    }
    NotificationAttempt {
        uuid id PK
        uuid userId FK
        uuid auditEventId FK
        string state
        string truthSource
    }
```

## Integrity and Query Rules

### Required constraints

1. Every ownership row references `app_users` with `ON DELETE RESTRICT`; do not rely on cascade deletion for security/history data.
2. `idempotency_key` is unique only with `operation_type`. Reusing the key with a changed `payload_hash` is application `409` and never another row/provider call.
3. `(consumer_name, event_id)` is unique. The inbox insert occurs before consumer business work in the consumer transaction.
4. Every enumerated state column has a database check constraint with a named constraint. Unrecognized provider values are normalized to `FAILED`/`AMBIGUOUS` plus a bounded error code, never copied directly.
5. `expires_at > created_at`, active rows have future/valid expiry at creation, and revocation timestamps/reason codes are consistent with state.
6. `onboarding_cases` rejects `APPROVED` or `REJECTED` unless `truth_source='PROVIDER'` and rejects `simulated=true` for either state.
7. Immutable history, attempt, audit, outbox, and inbox completion data is changed only through documented state transitions. Raw `UPDATE`/`DELETE` is not an application path.

### Required indexes

| Table | Index expression | Justification |
|---|---|---|
| `user_sessions` | `(user_id, status, expires_at DESC)` | List active/revocable sessions without table scan |
| `user_sessions` | `(status, expires_at)` | Expire/retention worker |
| `recovery_requests` | `(target_fingerprint, requested_at DESC)` | Newer-request invalidation and generic recovery decisions |
| `recovery_requests` | `(state, secret_expires_at)` | Expiry worker |
| `security_attempt_buckets` | `(blocked_until)` | Cleanup and bounded throttles |
| `onboarding_state_history` | `(case_id, transition_number)` | Ordered immutable history |
| `provider_operations` | `(state, reconcile_at)` | Ambiguous/pending reconciliation worker |
| `provider_operation_attempts` | `(operation_id, attempt_number)` | Immutable attempt order |
| `outbox_events` | `(state, available_at, created_at)` | `FOR UPDATE SKIP LOCKED` claim queue |
| `inbox_events` | `(state, claimed_at)` | Lease recovery and stuck-consumer visibility |
| `security_audit_events` | `(user_id, occurred_at DESC)` | Customer confirmation timeline |
| `notification_attempts` | `(state, attempted_at)` | Retry/reconciliation of configured channels only |

Index creation occurs only on new tables, minimizing locks on V1-V6 data. For future large production datasets, create a separately reviewed concurrent-index migration rather than editing a released Flyway file.

### Concurrency rules

- Session issuance: one transaction inserts the user session and commits before the JWT is accepted.
- Session last use: application throttles writes; updates use `GREATEST(last_used_at, observed_at)` and must not erase a concurrent revocation.
- Recovery verification: lock the selected request, require non-terminal state and valid expiry, then transition with one conditional update. Replaying the same value cannot reset a password twice.
- Rate limiting: use one atomic UPSERT on `(purpose, target_fingerprint)`; a bucket is reset only when the prior 15-minute window has elapsed.
- Outbox/inbox: one claimant at a time via `SKIP LOCKED`, lease expiry, and bounded exponential backoff. A successful external call can still be locally ambiguous and must reconcile by the original idempotency key.
- Operation insertion: database uniqueness is the final idempotency barrier; the application compares the stored hash before any provider I/O.

## Migration Strategy

### `V7__add_wave1_identity_persistence.sql`

Create:

- `user_sessions`
- `recovery_channels`
- `recovery_requests`
- `security_attempt_buckets`
- `security_audit_events`
- `onboarding_cases`
- `onboarding_state_history`

Use `CREATE TABLE IF NOT EXISTS` and `CREATE INDEX IF NOT EXISTS`. Put checks and constraints inline so PostgreSQL executes the whole migration transactionally. No `ALTER TABLE` touches a V1-V6 table.

### `V8__add_provider_operation_and_delivery_persistence.sql`

Create:

- `provider_operations`
- `provider_operation_attempts`
- `outbox_events`
- `inbox_events`
- `notification_attempts`

All provider/event capability defaults remain disabled in runtime configuration. A deployment can apply V8 while the Rabbit listener and real adapters remain disabled; the tables alone do not enable a provider.

### Rollout order

1. Back up PostgreSQL and record the V1-V6 schema-history version/checksums.
2. Run V7 and V8 on a disposable empty database and a copy upgraded from V6.
3. Deploy V7/V8 ahead of application use. The old application ignores the new tables.
4. Deploy Wave 1 application code. New login issuance creates a session row; legacy 15-minute JWTs are allowed to expire naturally.
5. Keep provider and listener flags off. Exercise only `NOT_CONFIGURED`, local, and test-double states.
6. Enable a new capability only after configuration, contract, credential, idempotency, approval, and reconciliation gates are independently satisfied.
7. If application rollback is required, stop the new app and deploy the previous app. Leave additive tables intact; do not drop V7/V8 data.

### Compatibility, rollback, and repair

- Existing `app_users`, `accounts`, simulation, split, schedule, and bill rows require no schema conversion.
- Existing users receive onboarding rows lazily on demand; no guessed KYC/provider state is backfilled.
- Existing tokens without `sid` remain valid only until natural expiry and cannot be listed, revoked, or renewed.
- V1-V8 migration failures on PostgreSQL are transactional and roll back. No partial table should be manually repaired by editing V1-V6.
- Flyway `repair` is not a normal rollback mechanism. Use it only after approved checksum/schema investigation, then rerun `validate` and the full migration tests.
- The pre-implementation rollback is application-only. Dropping V7/V8 tables after they contain recovery, audit, or provider state is irreversible and requires a later approved cleanup migration and retention review.
- Destructive future changes—dropping legacy columns, narrowing types, deleting audit history, or adding a constraint that may reject historical rows—are explicitly out of scope and must be separate migrations with data-quality backfill and rehearsal.

## Data Protection and Retention

| Data | Classification/control |
|---|---|
| Session metadata | Pseudonymous security data; coarse context label only; no IP, JWT, cookie, or secret |
| Recovery destination | PII; store only HMAC fingerprint plus converter-encrypted provider/local reference |
| Recovery code and reset authorization | Credential-class data; random, salted-hash only, short-lived, never logged or returned through unconfigured/test-double delivery |
| Onboarding references | Potential PII; provider/evidence identifiers encrypted with the existing `SensitiveDataConverter` policy; never raw evidence or full document values |
| Provider payload/event | Minimized JSON references only; no credential/header/provider error body; financial identifier values remain encrypted or omitted |
| Audit and notifications | User-safe codes and correlation ids only; target IDs are opaque UUIDs where possible |

Proposed non-production MVP retention defaults:

- Session terminal rows: 90 days after expiry/revocation.
- Recovery secret/hash material: null at terminal use or 24 hours after expiry; recovery metadata: 90 days.
- Rate-limit buckets: 31 days after last update.
- Outbox terminal rows: 30 days after completion; keep inbox dedup identity for at least 90 days.
- Security audit: 365 days.
- Onboarding history and provider-operation history: retain pending legal/provider policy approval; no automatic purge.

Before any production-enabled external provider, the legal/privacy wave must approve or replace these values. Jobs must be batch-deleted by bounded date/id ranges and must never purge a row still referenced by an active operation or unresolved reconciliation.

## Migration Verification Contract

The implementation task that writes V7/V8 is incomplete until all checks pass:

1. Fresh PostgreSQL database: Flyway migrates V1 through V8 successfully.
2. V6 baseline: Flyway migrates V7-V8 with representative existing `app_users`, account, simulation, split, schedule, and bill rows unchanged.
3. Reapplication: each new DDL body can be executed twice in a disposable database without duplicate tables/indexes or lost constraints.
4. Production validation: Spring Boot `prod` profile uses Flyway and `ddl-auto: validate`; Hibernate finds no missing/extra schema requirement.
5. Catalog assertions verify every named foreign key, unique constraint, check, and index in this design.
6. Idempotency concurrency test: duplicate `(operation_type, idempotency_key)` insert produces one row; changed hash produces no provider call.
7. Recovery concurrency test: one of two simultaneous verifications succeeds, the same authorization cannot reset twice, and newer request invalidates older rows.
8. Session test: revocation is visible to the next protected request and old-token issuance without `sid` has a maximum 15-minute tail.
9. Event test: duplicate `(consumer, eventId)` delivery performs zero repeated business/provider work.
10. Truthfulness test: `BAAS_ENABLED=false`, listener-disabled, and `TEST_DOUBLE` sources create zero real-provider calls and never create provider `APPROVED` or real-money state.

## Backend Handoff

- Add JPA entities and repositories only after V7/V8 land; do not depend on `ddl-auto:update` to create these tables.
- Token issuance and session creation must share a transaction; protected-request `sid` validation must query the authoritative registry.
- Treat `truth_source` and `simulated` as persisted invariants, not presentation hints inferred in the UI.
- Keep external I/O outside local transactions. A business transaction writes operation/outbox state; a worker claims and records attempts separately.
- Expose only safe codes, state, coarse labels, and retry times. Do not map database/provider exception text into APIs.

## Residual External Blockers

- Approved KYC provider, callback authentication, evidence handling, and legal basis are absent.
- Approved recovery channel and delivery contract are absent.
- Approved BaaS operation schema, credentials, sandbox, retries, query/reconciliation contract, and money-movement approval are absent.
- Rabbit event ownership/payload/version agreement is not provider-approved; listener startup remains disabled.
- Production privacy retention/legal hold policy is deferred to Wave 6.

These are preserved as `NOT_CONFIGURED`, blocked, or simulated states. They are not database failures and do not authorize retries into a real external system.

## Upstream Artifacts Consumed

- `.github/modernize/rearchitecture/artifacts/t1-pm.md` - Wave 1 requirements, six-wave entities, no-money and truthfulness rules.
- `.github/modernize/rearchitecture/artifacts/t2-architect.md` - authoritative runtime root, immutable V1-V6 boundary, and Wave 1 DBA handoff.
- `.github/modernize/rearchitecture/artifacts/t2.2-architect.md` - binding session, recovery, onboarding, operation, outbox/inbox, and migration invariants.
- `.github/modernize/rearchitecture/artifacts/data-model.md` - existing entity/schema and transaction inventory.
- `.github/modernize/rearchitecture/artifacts/migration_boundary.yaml` - additive implementation scope and V1-V6 copy-as-is rule.
- `.github/modernize/rearchitecture/clarification.md` - immutable stack/API/auth/provider/test-double constraints.

## Evidence Mapping

- `t1-pm.md#Wave 1` REQ-004 -> `user_sessions` registry, ownership indexes, revocation state, and session checks.
- `t1-pm.md#Wave 1` REQ-005/006/007 -> `recovery_requests`, `recovery_channels`, `security_attempt_buckets`, and support-assisted path.
- `t1-pm.md#Wave 1` REQ-008/009/010 -> `onboarding_cases`, immutable history, truth source, and provider gate.
- `t1-pm.md#Wave 1` REQ-011 -> `security_audit_events` and `notification_attempts`.
- `t2.2-architect.md#1 Session and JWT Contract` -> `sid` primary key, compatibility, registry, and active-only indexes.
- `t2.2-architect.md#2 Recovery Contract` -> hashed single-use state and generic throttling.
- `t2.2-architect.md#3 KYC-Ready Onboarding Contract` -> provider-only approval and immutable history constraints.
- `t2.2-architect.md#4-5 Provider and Event Contracts` -> operation uniqueness, attempts, outbox, inbox, and disabled defaults.
- `t2.2-architect.md#6 Migration and Runtime Contract` -> V7/V8 split, UTC timestamps, index, retention, and production validation gates.
- `migration_boundary.yaml::copy_as_is` -> V1-V6 preservation and additive-only rollback strategy.
- `clarification.md#Generic` -> simulation/test-double truth, no real-money movement, and no commit.

## Test Results

- Runtime code modified: no.
- Artifact validation command: PowerShell structure/evidence/Mermaid checks against V1-V6 SQL and upstream contracts.
- Backend command: `.\mvnw.cmd -B clean verify` from `giro-app/backend`.
- Passed: 12 backend tests.
- Failed: 0.
- Skipped: 0.
- Build: successful; 55 main and 3 test sources compiled and packaged.
- Passed: all specified migration-verification gates and table/constraint/index families are traced to t1/t2 contracts.
- Design checks failed: 0.
- Deferred: executable V7/V8 Flyway migration/concurrency tests are implementation-phase gates because the proposed SQL and mapped entities are not created by this design-only task.
