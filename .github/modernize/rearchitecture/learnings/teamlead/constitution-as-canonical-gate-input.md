# Canonical Constitution Placement

The migration constitution must be discoverable at the rearchitecture workflow root, not only inside the task artifacts directory.

## What Happened

During Giro task t3.2, the quality-gate finding required `constitution.md` under
`.github/modernize/` or `.github/modernize/rearchitecture/`, while the task
artifact directory was only an output location. A canonical root-level
constitution with a task-level artifact link satisfies both governance
discovery and task deliverable traceability.

## Takeaway

Keep the canonical constitution at
`.github/modernize/rearchitecture/constitution.md`; reference it from the
`tX-teamlead.md` task artifact. Do not create an unsynchronized copy under
`artifacts/`.

## History
- 2026-11-23 (giro-app/t3.2): initial
