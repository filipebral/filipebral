## [t2.3] Remediate critical npm advisory and verify frontend
- The tracked nested `giro-app/` is the frontend runtime; the workspace root contains unrelated or stale duplicates.
- `next` and `eslint-config-next` must move together within the preserved major to keep the framework and lint toolchain aligned.
- The runtime validator left Next dev-server child processes on Windows; stop only the project-specific PIDs before rerunning `npm ci` to avoid an `EPERM` lock on the `lightningcss` native binary.
- Existing Node tests were present but `package.json` lacked a `test` script. Reusing `node --test lib/*.test.mjs` avoids adding a test framework solely for regression validation.
- The validator does not parse Node test output into counts, so run `npm test` separately and retain its explicit 3 passed / 0 failed evidence.
- Learnings consumed: [architect/nested-runtime-and-provider-seams, architect/wave1-session-and-provider-seams]
