# Provider-Safe Product States

Every provider-capable capability should define unconfigured, simulated/test-double, pending, completed/confirmed, failed, and unknown states in product acceptance criteria.

## What Happened

During Giro task t1, the existing mock/test paths could otherwise be described as if a bank/provider integration existed. Separating product truth states prevented requirements from treating local success, simulation, and real provider confirmation as equivalent.

## Takeaway

For banking and other externally dependent capabilities, acceptance criteria must state what the user sees for each provider state, prohibit false real-operation claims, and include timeout, duplicate callback, and reconciliation scenarios.

## History

- 2026-11-23 (giro-app/t1): initial
