# Maven Verification Root

Run Giro backend verification from the tracked nested `giro-app/backend` module with a clean full lifecycle; do not infer classpath failures from workspace-root duplicate directories.

## What Happened
During t2.1, an upstream report described 12 unresolved test symbols even though the classes existed in the nested backend. Running `.\mvnw.cmd -B clean verify` from `giro-app/backend` compiled 55 main and 3 test sources, passed 12 tests, and packaged the application without code or POM changes.

## Takeaway
For backend baseline diagnostics, first use the tracked nested module as the working directory, run `clean verify`, and confirm `compile`, `testCompile`, Surefire counts, and package output. Only alter POM/source after a clean lifecycle reproduces the defect.

## History
- 2026-11-23 (giro-app/t2.1): initial
