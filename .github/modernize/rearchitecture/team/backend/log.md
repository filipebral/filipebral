# Backend Session Log

## [t2.1] Restore and verify the backend Maven baseline
- Codebase/domain discoveries: the tracked backend is `giro-app/backend`; its tests live in the same `com.giro_app.backend` package as all reported missing classes.
- Wrong assumptions and corrections: the upstream 12-symbol test compilation failure was not a current POM, package, or source defect; a deterministic clean lifecycle completed successfully without source/build edits.
- Debugging dead-ends and what actually worked: inspecting symbols confirmed they existed, but the decisive evidence was running `.\mvnw.cmd -B clean verify` from the nested backend root and checking lifecycle phases.
- Techniques/patterns worth reusing: verify from `giro-app/backend`, not workspace-root duplicate-looking directories; use a clean lifecycle and inspect both compilation counts and final Surefire totals.
- Learnings consumed: [architect/nested-runtime-and-provider-seams]
