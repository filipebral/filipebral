# Existing Data Model

## Entity Inventory

### `app_users`
- UUID `id`, AES-GCM converted CPF, unique CPF lookup HMAC hash, display `name`, BCrypt `password_hash`.
- Legacy `email` column remains in V2 schema with nullability removed in V3; no Java field maps it.

### `accounts`
- UUID `id`, `user_id`, encrypted BaaS account/routing/branch/account identifiers, scale-4 `available_balance`, enum `status` (`PENDING`/`ACTIVE` from `AccountStatus`).
- No JPA relationship to `app_users`; ownership is a UUID wire/domain reference.

### `split_groups`
- UUID `id`, `name`, nullable `owner_user_id`.
- One-to-many `group_members`, cascade all and orphan removal.

### `group_members`
- UUID `id`, `user_id`, scale-2 `percentage`, many-to-one `splitGroup`.

### `split_transactions`
- UUID `id`, `split_group_id`, description, scale-2 total, creation timestamp.

### `simulation_accounts`
- Primary key is `user_id`; scale-2 fictitious balance.

### `simulation_transactions`
- UUID `id`, user/request UUIDs, operation type, scale-2 amount, destination/description, creation instant.
- Unique `(user_id, request_id)` for idempotency.

### `simulation_schedules`
- UUID id, user/request/execution IDs, amount, destination, due instant, status.
- Unique `(user_id, request_id)`; scheduler queries by pending status and due time.

### `simulation_bills`
- UUID id, user/request/execution IDs, description, amount, due date, status.
- Unique `(user_id, request_id)`.

## Transaction Boundaries
- Auth registration persists one user in a transaction and issues a token after save.
- Account opening transaction persists `PENDING`, calls BaaS, activates, and publishes; provider timeout is caught and rethrown. A Spring transaction rollback will undo the pending row despite the source comment claiming traceability.
- Simulation operation locks the account row and atomically updates balance plus idempotent transaction.
- Schedule execution locks the schedule, locks the account, calls the transactional operation, and marks completed.
- Split group creation and split processing persist atomically; split percentage validation occurs before save.

## Key Entities
- `AppUser`: authenticated identity and CPF/password material.
- `Account`: provider-backed account state.
- `SplitGroup`: percentage-based member allocation.
- `SplitTransactionEntity`: immutable-like record of a calculated split.
- `SimulationAccount`: fictitious ledger balance.
- `SimulationTransaction`: idempotent fictitious operation record.
- `SimulationSchedule`: due fictitious Pix request.
- `SimulationBill`: fictitious payable invoice.

## Upstream Artifacts Consumed
- `.github/modernize/rearchitecture/clarification.md` — data migration strategy is to be assessed and external provider behavior remains constrained.

## Evidence Mapping
- `clarification.md#Backend B3` → this artifact inventories, but does not redesign, schema.
