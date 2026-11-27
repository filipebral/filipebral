# npm Test and Audit Validation

Use the existing Node test file as the frontend regression gate and verify the resolved npm tree after framework patch upgrades.

## What Happened
During Giro task t2.3, Next.js `16.3.8` had a critical advisory. Moving `next` and `eslint-config-next` together to `16.4.6`, running npm audit repair, and preserving the existing Node test command cleared all advisories without source changes. The runtime validator did not parse `node:test` counts, so `npm test` was also run directly.

## Takeaway
Keep `next` and `eslint-config-next` on the same version within the immutable major. Use `node --test lib/*.test.mjs` when this existing test suite is the only test runner. Confirm with `npm audit --json`, `npm ls` for the critical dependency paths, the full production build, direct test counts, and an HTTP smoke probe.

## History
- 2026-11-23 (giro-app/t2.3): initial
