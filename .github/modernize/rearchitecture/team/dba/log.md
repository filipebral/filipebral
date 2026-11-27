# DBA Session Log

## [t4] Design persistence for Wave 1 identity, recovery, onboarding, and provider safety
- Anchored all schema work to the tracked nested `giro-app/backend`; V1-V6 are immutable and production Flyway uses `ddl-auto: validate`.
- Existing `app_users` is the ownership anchor; new records should use UUID ownership keys and `ON DELETE RESTRICT` rather than cascading away security/audit evidence.
- Provider readiness is runtime configuration, not a database flag. Persisted operation and onboarding rows record observed truth with explicit provenance.
- A same-key/changed-payload operation is a database-enforced uniqueness-plus-hash conflict, while external I/O belongs after the local transaction through outbox/inbox.
- Recovery controls need separate channel metadata, hashed short-lived secrets, and an atomic rate-limit bucket; a single table is insufficient for safe invalidation and throttling.
- Wrong assumption: a database approval row could safely enable a provider. Correction: that would create a second truth source and could bypass `BAAS_ENABLED`, approval, credentials, or listener gates.
- Technique worth reusing: define migration sequencing, concurrency queries, indexes, retention, and executable catalog assertions together so backend implementation cannot rely on JPA `ddl-auto:update`.
- Learnings consumed: [architect/enforceable-security-and-provider-contracts, architect/nested-runtime-and-provider-seams, architect/wave1-session-and-provider-seams, backend/maven-verification-root]

## [t6.2] Remediate session-handle, KYC truth, and six-wave persistence
- Design discovery: the private JWT `sid` needs a separate random `management_handle`; browser DTO `id` and revocation lookup must map only to that handle.
- KYC test-double `PROVIDER_REVIEW` must be normalized to an explicitly simulated state before insert; `state + truth_source + simulated + provider_confirmed` is a database invariant, not presentation metadata.
- Six-wave coverage requires explicit local records for account lifecycle/reconciliation, payments/receipts, product requests, support, review holds, and final-wave legal/readiness evidence.
- Wrong assumption: generic `provider_operations` is sufficient for every wave. Correction: it records external intents but does not replace owned local lifecycle records or accepted explicit deferrals.
- Technique worth reusing: write a 63-row persistence disposition matrix and pair each proposed table with a constraint/test evidence class.
- No source/migration code changed in this planning remediation; V1-V6 remain untouched and V7-V10 remain implementation work.
- Learnings consumed: [dba/provider-readiness-as-runtime-config, backend/maven-verification-root]
