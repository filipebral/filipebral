## [t1] Inventoried product capabilities and defined six-wave acceptance criteria
- Codebase/domain discoveries: the executable product is the populated `giro-app/` application tree; it currently has two user pages, CPF/password cookie sessions, disabled-by-default provider account opening, isolated financial simulation, split calculation, and two local calculators.
- Wrong assumptions and corrections: the presence of “mock BaaS” does not make account opening provider-ready in product terms; the product must explicitly distinguish disabled, simulated, test-double, and provider-confirmed states.
- Debugging dead-ends and what actually worked: broad root-level discovery included stale/empty paths, while manifest, route, page, and controller evidence identified the operative application tree and observable contracts.
- Techniques/patterns worth reusing: attach every provider-capable capability to an explicit unconfigured/simulated/real state, and require duplicate/timeout/replay acceptance scenarios.
- Learnings consumed: [(none)]

## [t3.1] Remediated capability criteria and complete product traceability
- Codebase/domain discoveries (gotchas, non-obvious behavior): the existing capability inventory was complete, but the specification gate correctly treated embedded framework, route, ledger, and audit terminology as implementation leakage; the submitted clarification constraints remain valid but do not cure product-acceptance ambiguity.
- Wrong assumptions and corrections: a requirement can appear outcome-oriented yet still be untestable when qualifiers such as “eligible,” “available,” or “recoverable” lack observable boundaries; explicit state and timing definitions were needed before traceability could be deterministic.
- Debugging dead-ends and what actually worked: attempting to map only the existing 2–4 examples per wave left many requirements without direct proof; a one-to-one AC-001 through AC-063 matrix resolved every requirement and made self-validation mechanical.
- Techniques/patterns worth reusing: isolate technical constraints in a non-product appendix, define state vocabulary once, and assign each REQ a single executable Given/When/Then scenario with unique sequential IDs.
- Learnings consumed: [pm/provider-safe-product-states, architect/nested-runtime-and-provider-seams]

