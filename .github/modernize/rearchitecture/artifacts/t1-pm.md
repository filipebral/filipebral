# t1 — Capability Inventory and Six-Wave Acceptance Criteria

**Role**: pm  
**Status**: ready for specification-quality gate  
**Classification**: brownfield upgrade  
**Created**: 2026-11-23

## Scope Baseline

- **Discovery method**: inspected the active product tree, product documentation, user-facing pages and dialogs, public route handlers, backend-facing user flows, and existing tests.
- **Operative product tree**: `giro-app/` is the only populated application tree; the outer repository is the workspace/release boundary.
- **User-visible surfaces discovered**: 2 pages, 4 dashboard feature components/dialogs, 3 authentication route flows, 1 authenticated proxy surface, and the backend operations listed below.
- **In scope**: all existing behavior plus the six completion waves described by the submitted clarification answers.
- **Out of scope**: real money movement without a configured provider; unrelated rewrite; invented provider claims; licensing/regulatory implementation before Wave 6; commits.

## Delivery Constraints (Non-Product Appendix)

> This appendix records project delivery constraints for downstream design and
> implementation. It is not part of the customer-facing capability definition;
> the product requirements below describe observable outcomes without prescribing
> how those outcomes are implemented.

1. Preserve the current Next.js major and Spring Boot 3 major; this task does not authorize a stack rewrite or downgrade.
2. Preserve existing external API compatibility and existing cookie/JWT behavior while hardening security.
3. Match existing mobile-first styling and visual tokens; the expanded product must satisfy WCAG 2.1 AA.
4. Verify the latest two major versions of Chrome, Firefox, Safari, and Edge.
5. Use provider-safe contracts and test doubles. A capability remains explicitly simulated until an approved real provider is configured.
6. Existing tests and observable behavior must remain passing. No regression is acceptable.
7. Account, ledger, reconciliation, provider, KYC, notification, payment-rail, card, credit, insurance, support, and legal provider decisions remain real external dependencies; absence of a provider must never be presented as readiness for real operations.

## Current Capability Inventory

### 1. Public access and authentication

| Current behavior | Observable rules and states |
|---|---|
| Switch between login and registration | One public page exposes “Entrar” and “Criar conta”; switching clears the current error. |
| Register with name, CPF, and password | Name is required; CPF is normalized to 11 digits and check-digits are validated; password is 12–72 characters; duplicate CPF returns a non-enumerating conflict. |
| Log in with CPF and password | Invalid credentials return 401 with a safe user-facing message. |
| Maintain an authenticated session | A successful login/registration receives a 15-minute token stored in an HttpOnly, SameSite=Lax cookie; production cookies are Secure. |
| Enter the dashboard | Valid sessions can access the dashboard; invalid or missing sessions are redirected to the public page and an existing stale cookie is cleared. |
| Log out | Logout clears the cookie and returns the user to the public page. |
| Handle unavailable authentication service | The UI shows a service-unavailable message instead of exposing an exception. |
| Recover access | Not present. |
| Verify identity/KYC or show onboarding status | Not present; registration currently proves only CPF format, not identity. |
| Manage/revoke sessions | Not present beyond logout. |

### 2. Provider account linking

| Current behavior | Observable rules and states |
|---|---|
| Enter an external customer identifier | The dashboard accepts a non-empty provider customer identifier. |
| Request account creation/linking | The action is blocked when the field is empty; the user sees processing, success, or error state. |
| Display real balance | Not available; the UI explicitly says real balance is unavailable without a banking integration. |
| Enable production provider onboarding | Disabled unless real-provider configuration is enabled; the default state is a truthful “integration pending” state. |
| Account status/history | A provider-created account can reach an active state, but the dashboard has no account status, account details, recovery, or failure/resume experience. |

### 3. Simulated account and movement

| Current behavior | Observable rules and states |
|---|---|
| Detect simulation availability | The simulation panel disappears if simulation is disabled or unavailable and displays an error if status cannot be loaded. |
| Create a simulated account | A user can create one fictitious zero-balance account. |
| Add fictitious balance | Only positive BRL values with at most two decimals and within the current maximum are accepted. |
| Simulate Pix and transfer | Recipient is a fictional name; insufficient fictitious balance is rejected; no provider is contacted and no money moves. |
| View statement | The latest 50 simulated operations are shown with type, amount, date, and an explicit simulated receipt. |
| Retry safely | Reusing an operation identifier with identical details returns the prior effect; changing details returns a conflict. |
| Schedule simulated Pix | Only future local date/time values are accepted; schedules can be pending, completed, failed for insufficient fictitious balance, or cancelled. |
| Cancel a schedule | Pending user-owned schedules can be cancelled; completed/failed/cancelled schedules cannot be restarted. |
| Create/pay simulated boleto | Description, amount, and due date are accepted; no barcode is collected. Payment updates fictitious balance and statement once. |
| Truthful warnings | Every movement area states that balances, receipts, recipients, Pix, transfers, and bill payments are simulated. |

### 4. Split-group calculation

| Current behavior | Observable rules and states |
|---|---|
| Create a split group | A user supplies a name and one or more member UUIDs with percentages totaling exactly 100.00%. |
| Edit members | Members can be added or removed, with at least one retained; the running total is visible. |
| Process a split | Only the authenticated owner may calculate a group; the group must exist; a result contains description, total, and each member share. |
| Persist calculation history | A successful calculation is stored and a transaction identifier is returned. |
| Move money | Not supported; the split is calculation-only. |
| Discover/list groups or prior splits | Not present in the UI. |
| Round-share explanation or discrepancy handling | Not presented to the user. |

### 5. Financial calculators

| Current behavior | Observable rules and states |
|---|---|
| Simulate a fixed-installment loan | User supplies principal, monthly rate, and term. Result shows installment, total, and estimated interest. |
| Simulate an investment | User supplies principal, monthly contribution, rate, and term. Result shows estimated final value and estimated earnings. |
| Validate inputs | Principal and contribution are bounded; rate is 0–100%; term is 1–600 months; invalid/overflowing values produce a specific error. |
| Product truthfulness | Results are clearly labeled estimates and state that they exclude fees, taxes, and inflation and do not contract a product. |

### 6. UI and platform coverage

| Area | Current baseline |
|---|---|
| Language | `pt-BR` labels and page language are present; no alternate locale is present. |
| Responsive design | Screens are single-column/mobile-first and use the existing dark palette, lime accent, rounded cards, and field/button treatments. |
| Accessibility | Basic labels and live alerts/status exist; modal focus management, dialog semantics, keyboard dismissal/trapping, complete accessible naming, focus visibility, and contrast/browse verification are incomplete. |
| Browser coverage | Evergreen browsers are not evidenced by a repeatable compatibility matrix. |
| Observability | No product-visible health/status, correlation, operational support, or user-safe diagnostic experience is present. |
| Support | No help, incident, dispute, support request, or contact-channel flow is present. |
| Fraud/limits/audit | No user-facing or operator-facing controls are present beyond form validation and simulation caps. |
| Licensing/legal | No structured onboarding consent, terms/privacy acceptance, notices, or readiness checklist is present. |

## External Contract Continuity Boundary

Existing customers and consumers MUST retain the request/response meaning and
user outcomes represented by the current capability inventory. Exact operation
names, routes, payloads, security mechanisms, and provider seam definitions are
owned by the architecture artifacts and MUST NOT change customer/product
meaning. Downstream teams must use `wire_contracts.yaml` and the architecture
unit contracts as the technical compatibility baseline.

## Six-Wave Product Definition

### Wave 1 — Security, Session, Recovery, and KYC-Ready Onboarding (P1)

**Outcome**: a customer can securely register, sign in, recover access, understand identity/onboarding status, and manage the session without the product claiming completed KYC or banking activation.

**Acceptance requirements**

- **REQ-001**: The existing registration and sign-in journeys MUST remain available with equivalent customer-visible outcomes for existing customers and consumers.
- **REQ-002**: Invalid credentials, duplicate registration, malformed identity data, stale sessions, and unavailable authentication services MUST return user-safe messages without revealing whether a specific customer exists beyond the currently accepted UX.
- **REQ-003**: A signed-in customer MUST be able to end the current session, have the protected page redirect to sign-in afterward, and receive no authenticated account data from that cleared session.
- **REQ-004**: A signed-in customer MUST see every unrevoked, successfully authenticated session with last-use and non-sensitive device/context labels. The current session is always revocable; any other active session is also revocable. If another session has already expired or been revoked, it is not shown as active.
- **REQ-005**: A customer who forgot the password and has a verified recovery channel MUST complete recovery by proving control of that channel, using one single-use recovery authorization valid for 30 minutes. Any newer recovery authorization invalidates older authorizations, and the customer receives an explicit reset confirmation. If no verified channel is available, the product MUST offer the support path and MUST NOT imply that reset is possible.
- **REQ-006**: After 5 unsuccessful verification attempts for the same identity or recovery target within 15 minutes, the product MUST pause further attempts for 30 minutes and show only a generic safe retry time. Expired or invalid verification values receive generic guidance that starts a new request rather than revealing identity existence.
- **REQ-007**: A legacy customer without a usable sign-in identity MUST enter the support-assisted recovery state rather than silently failing; successful authorized recovery MUST reopen the existing customer record and never create a duplicate customer.
- **REQ-008**: The onboarding flow MUST distinguish registration, identity review, KYC-provider review, approved, rejected, pending-information, and not-configured states when provider integration is unavailable.
- **REQ-009**: A KYC-ready contract MUST capture required customer evidence/status without claiming the person is verified until an approved provider reports that result.
- **REQ-010**: Before requesting personal evidence, the onboarding experience MUST explain its purpose and current review state. A step is available only when its required provider and legal approval are configured and active; otherwise the step is disabled or informational and states the next customer-safe action.
- **REQ-011**: A completed sign-in, password change, recovery, or session revocation MUST generate an understandable confirmation. If a delivery channel is configured, the confirmation also appears there; otherwise it remains visible inside the product with an explicit “provider not configured” delivery state.

**Acceptance scenarios**

1. **Given** an existing valid customer, **When** the customer signs in and opens the dashboard, **Then** the same existing dashboard capabilities are available and the session has an observable expiry/revocation policy.
2. **Given** repeated invalid recovery or sign-in attempts, **When** the threshold is exceeded, **Then** the product delays/denies further attempts without exposing account existence.
3. **Given** a legacy customer or an unconfigured KYC provider, **When** onboarding is opened, **Then** the UI shows a truthful recoverable or “not configured” state and does not claim a verified bank account.
4. **Given** a signed-in session is revoked, **When** a protected request is repeated, **Then** it is rejected and the customer is returned to sign-in.

### Wave 2 — Provider-Ready Account, Ledger, Reconciliation, and Notifications (P1)

**Outcome**: the product can expose a truthful provider account lifecycle and provider-ready financial state using test doubles, while withholding real money movement until a configured provider exists.

**Acceptance requirements**

- **REQ-012**: The account experience MUST show unconfigured, pending, active, failed, suspended, and reconciliation-needed states with non-misleading customer guidance.
- **REQ-013**: Repeating the same account-opening intent MUST show the original outcome and MUST NOT create a second customer-visible account, account request, or movement.
- **REQ-014**: A real account balance and transaction history MUST be shown only from an approved provider/reconciled source; simulated values MUST remain visibly separate and unusable as real-account truth.
- **REQ-015**: Account, balance, transaction, and status experiences MUST have the same customer-visible meaning whether exercised in the approved sandbox with predictable provider responses or with a configured real provider.
- **REQ-016**: Every money-affecting event MUST retain a customer-referencable history that distinguishes submitted, accepted, completed, rejected, reversed, cancelled, and unknown outcomes without overwriting prior outcomes.
- **REQ-017**: Reconciliation MUST produce a visible outcome showing matched, missing, duplicate, mismatched, and unresolved items without silently overwriting either source.
- **REQ-018**: A provider timeout or duplicate response MUST NOT duplicate a balance effect, account, payment, receipt, or customer notification.
- **REQ-019**: Transactional and account-status notifications MUST be delivered through a test-double channel and allow the customer to see delivery/read status without claiming an unconfigured channel was sent.
- **REQ-020**: Provider secrets, sensitive financial identifiers, and dispute-resolution details MUST never be displayed to the customer or included in customer-readable errors.
- **REQ-021**: With no configured financial provider, account-opening and movement actions MUST remain disabled or explicitly simulated, state that no real money moved, and provide no path that executes a real-money operation.

**Acceptance scenarios**

1. **Given** no configured provider, **When** a customer requests a real account, **Then** the product explains the unavailable dependency and does not simulate a real-provider success.
2. **Given** a provider test double reports a timeout followed by a duplicate success, **When** the request is reconciled, **Then** exactly one account/ledger/notification result is visible.
3. **Given** a provider balance differs from the ledger, **When** reconciliation runs, **Then** the mismatch is retained, surfaced for resolution, and not silently substituted.

### Wave 3 — Pix, Transfer, and Boleto Product Flows (P1)

**Outcome**: customers can use consistent payment journeys with truthful simulation/provider states, lifecycle tracking, receipts, and recovery for ambiguous outcomes.

**Acceptance requirements**

- **REQ-022**: The product MUST expose Pix, transfer, and boleto flows through the same customer-safe lifecycle: create/review, confirm, pending, completed, rejected, cancelled, or ambiguous.
- **REQ-023**: Before confirmation, a payment review MUST show destination or beneficiary, amount, date, disclosed fees, funding source, and whether the operation is simulated or provider-confirmed.
- **REQ-024**: A real Pix key, account destination, or bill code MUST pass provider-approved validation before submission and MUST be masked or omitted from generic errors and customer support notifications.
- **REQ-025**: Repeated payment confirmation with the same customer-provided intent MUST be safe and MUST not create a duplicate movement.
- **REQ-026**: Insufficient available funds, a reached limit, a blocked destination, invalid recipient data, provider rejection, timeout, and ambiguous status MUST each produce distinct plain-language guidance. Each state MUST offer one allowed next action: review/edit, wait with a displayed retry time, seek reconciliation/support, or close without resubmitting.
- **REQ-027**: Scheduled payments MUST permit future-date creation, list, cancellation, execution status, and post-failure notification.
- **REQ-028**: A payment receipt MUST clearly identify whether it is simulated or provider-confirmed and include a unique non-sensitive reference, result, date, amount, and masked destination.
- **REQ-029**: Existing simulated credit, Pix, transfer, schedule, boleto, and statement behaviors MUST remain available when simulation is enabled.
- **REQ-030**: Pix, transfer, and bill payment journeys MUST remain provider-neutral and fully exercisable in an approved test state, while real execution remains unavailable until a provider is configured and approved.

**Acceptance scenarios**

1. **Given** a valid simulated payment, **When** the customer confirms it, **Then** exactly one simulated debit and receipt are displayed and no provider or real recipient is contacted.
2. **Given** a provider times out after submission, **When** the customer reopens the payment, **Then** the product shows an ambiguous/pending state and does not offer a duplicate unsafe retry.
3. **Given** a configured provider confirms a payment, **When** the customer opens the receipt, **Then** it is marked provider-confirmed and never marked simulated.

### Wave 4 — Cards, Credit, Investments, Insurance, and Support Interfaces (P2)

**Outcome**: all remaining banking product surfaces exist as truthful, provider-aware customer interfaces, with non-binding simulations where external contracts are absent.

**Acceptance requirements**

- **REQ-031**: The dashboard MUST let a customer discover card, credit, investment, insurance, and support areas without implying a product is active when only an interface or simulation exists.
- **REQ-032**: Card flows MUST cover virtual/physical availability, issue/readiness state, reveal/hide of sensitive display data, activation, block/unblock, limits, and loss/fraud reporting behind an approved provider contract.
- **REQ-033**: Credit flows MUST show offer eligibility, simulation assumptions, amount/term/fees/total, acceptance record, application status, and decline reason without contracting an unapproved product.
- **REQ-034**: Investment flows MUST preserve the existing calculator and distinguish estimation, offer, application, allocation, and provider-confirmed position.
- **REQ-035**: Insurance flows MUST distinguish quote, coverage summary, exclusions, application, policy status, and cancellation request and MUST not imply coverage before confirmed issuance.
- **REQ-036**: Support flows MUST allow a customer to open a categorized request, attach safe non-secret context, receive a reference, follow status, and view product-appropriate response/dispute guidance.
- **REQ-037**: Product actions with no configured provider MUST operate only as informational, saved draft, or explicitly simulated flows with test doubles; no real purchase, issuance, lending, or policy claim is permitted.
- **REQ-038**: Financial estimates MUST retain their present disclaimers and MUST include an explicit distinction from provider-approved pricing.

**Acceptance scenarios**

1. **Given** the card provider is unconfigured, **When** the customer opens Cards, **Then** the product provides an informative/draft experience and states that no card exists.
2. **Given** a credit estimate, **When** the customer reviews it, **Then** fees, assumptions, installment/total, and lack of contract are visible before any approved application action.
3. **Given** a support request is submitted, **When** the service is available, **Then** the customer receives a unique reference and can follow status without sharing credentials or full financial secrets.

### Wave 5 — Observability, Antifraud, Limits, Audit, and Accessibility (P1)

**Outcome**: product operations are observable and controllable, customers receive safe outcomes, and all expanded interfaces meet the defined accessibility and browser gates.

**Acceptance requirements**

- **REQ-039**: For every product-critical journey, support staff MUST be able to follow one privacy-safe customer operation through each outcome, state change, retry, notification, and reconciliation result without seeing unnecessary personal data.
- **REQ-040**: A degraded or unavailable service state MUST offer at least one safe next action: retry now, wait until a displayed time, or use support. It MUST never expose technical error details or secrets.
- **REQ-041**: The product MUST disclose each applicable amount or count limit before confirmation and distinguish: allowed, additional verification required, temporarily held for review, released, and rejected. The response MAY state which safe next action is available, but MUST NOT disclose detection rules or thresholds used internally.
- **REQ-042**: Before confirmation, the customer MUST see the applicable per-operation and daily limits for the selected action, as well as any provider-specific restriction. When exceeded, the product MUST name the exceeded limit and the allowed next action without exposing internal rules.
- **REQ-043**: Security, onboarding, account, payment, card, credit, insurance, support, and administrative actions MUST produce reviewable history that records actor category, time, target category, outcome, and reason and cannot be silently replaced by later events.
- **REQ-044**: Product-operation history MUST exclude passwords, complete access secrets, full account identifiers, full Pix or bill codes, and any other personal data not required to understand the outcome.
- **REQ-045**: Every new and modified primary flow MUST support keyboard-only operation, visible focus, meaningful names, programmatic errors, live status messages, accessible dialogs, and a logical heading/reading order.
- **REQ-046**: WCAG 2.1 AA automated and manual checks MUST pass for sign-in, dashboard, onboarding, payment, product, and support states, including modal, error, loading, disabled, pending, and timeout states.
- **REQ-047**: Every primary flow MUST be verified at mobile, tablet, and desktop widths with no horizontal task-blocking, inaccessible controls, or clipped essential content.
- **REQ-048**: Every primary flow MUST remain usable on each browser supported by the clarified evergreen-browser compatibility matrix.
- **REQ-049**: Existing baseline UX, styling, tokens, Portuguese locale, and mobile-first interaction patterns MUST be preserved unless a clearly documented usability or accessibility requirement necessitates a visible adjustment.

**Acceptance scenarios**

1. **Given** an operation times out after submission, **When** support/operations inspects it, **Then** a single correlation trail links the request to provider and final reconciliation state without revealing secrets.
2. **Given** a limit or antifraud rule blocks an operation, **When** the customer sees the response, **Then** the reason is understandable, no internal rule is exposed, and the allowed next step is clear.
3. **Given** a keyboard-only user on any primary flow, **When** a dialog opens, fails, or closes, **Then** focus is trapped/returned correctly and the outcome is announced.

### Wave 6 — Licensing and Legal Readiness (P3, executed last)

**Outcome**: technical implementation is not claimed as legally launchable until applicable licensing, regulatory, privacy, contractual, and operational readiness is documented and accepted.

**Acceptance requirements**

- **REQ-050**: Licensing/legal readiness MUST be the final wave and MUST NOT gate or be represented as completed by earlier technical delivery.
- **REQ-051**: The launch-readiness package MUST identify required institution/partner permissions, license dependencies, regulatory ownership, responsible legal entity, and approval status for each real product capability.
- **REQ-052**: The product MUST provide versioned customer-facing terms, privacy notice, consent records, fee/pricing disclosures, complaint/dispute routes, and retention/deletion requests for applicable capabilities.
- **REQ-053**: Provider and partner contracts MUST have explicit responsibility for KYC, sanctions/fraud screening, safeguarding/settlement, custody, reconciliation, incident reporting, complaints, card/network rules, and data processing.
- **REQ-054**: Required legal/policy artifacts MUST have an owner, status, approval evidence, effective date, and a defined update/withdrawal process.
- **REQ-055**: A capability MUST remain unavailable, simulated, or marked pilot/not production-enabled where legal/provider approval is missing.
- **REQ-056**: Final launch sign-off MUST enumerate every outstanding jurisdiction, license, provider, contract, privacy, security, and operational approval; zero untracked launch blockers is required for a production claim.

**Acceptance scenarios**

1. **Given** technical payment flows pass, **When** licensing approval is missing, **Then** the product remains simulation/test-only and does not present itself as production-ready.
2. **Given** a customer accepts terms for an enabled product, **When** terms or product risk materially changes, **Then** the system records which version applied and obtains renewed acceptance where required.
3. **Given** a final readiness review, **When** one required approval is unresolved, **Then** the capability is explicitly listed as blocked and excluded from production claims.

## Cross-Wave Functional Requirements

- **REQ-057**: Every new product action MUST show the applicable loading, success, error, unavailable, unconfigured, simulated, and confirmed-real state without treating any of those states as equivalent.
- **REQ-058**: Existing consumers MUST continue to receive the established meaning of supported responses; additive capability changes MUST NOT force existing consumers to alter working behavior.
- **REQ-059**: Existing customer journeys MUST remain behaviorally available unless an explicitly documented acceptance criterion supersedes them for security or regulatory correctness.
- **REQ-060**: No action in the six waves MUST create, send, receive, settle, or represent real money as real before a provider is configured and approved.
- **REQ-061**: Every provider-dependent capability MUST be exercisable in a safe test state covering success, invalid input, provider rejection, timeout, duplicate response, incomplete data, and a later final response.
- **REQ-062**: The existing visual identity, current language, and current supported behavior MUST remain verifiable after every wave, with any permitted accessibility change documented.
- **REQ-063**: Completion evidence MUST NOT claim provider integration, identity approval, legal approval, or real-money readiness when only an unconfigured provider state, sandbox, or local approval evidence exists.

## Key Entities

- **Customer**: person using the public and authenticated product; identity and KYC status are distinct.
- **Session**: authenticated access lifecycle, expiry, device/context, and revocation state.
- **Recovery attempt**: one-time, expiring, throttled path for restoring credential access.
- **Onboarding case**: registration-to-approval lifecycle, requested evidence, provider status, and customer guidance.
- **Account**: local/provider account relationship, lifecycle, and safe balance source.
- **Ledger entry**: append-only financial intent/effect and lifecycle distinct from provider truth.
- **Reconciliation case**: matched, duplicate, missing, mismatched, and unresolved comparison outcome.
- **Payment intent**: customer-reviewed Pix, transfer, or boleto operation with lifecycle and idempotent identity.
- **Notification**: delivery attempt/status tied to a business event and channel.
- **Product application/policy/position/card/support request**: non-financially-affective lifecycle records until a provider confirms the corresponding real product event.
- **Audit event**: privacy-safe record of actor, action, target, result, time, correlation, and reason.
- **Consent/disclosure acceptance**: versioned evidence for applicable terms, privacy, fee, or product disclosures.

## Edge Cases

- Authentication secret missing/rotated, expired token, malformed token, clock skew, or token revocation after a protected action begins.
- Same CPF/name/password data retried, multiple tabs submitting the same request, and legacy user without CPF.
- Customer abandons KYC/recovery and resumes from another device or after provider status changes.
- Provider is disabled, partially configured, rejects a request, times out, calls back twice, calls back out of order, or sends unsupported statuses.
- Real and simulated balances/account operations coexist and could be confused.
- Reconciliation detects a missing/duplicate/reversed operation after a customer has already seen a pending or completed state.
- Scheduled payment executes while the account is blocked, insufficient, or the provider is unavailable.
- Rounded split shares do not sum exactly to the total or a member/account is no longer valid.
- Card/credit/insurance/support provider returns pending, denied, frozen, cancelled, or unknown state.
- Notification delivery succeeds late, fails, or is duplicated.
- Small-screen, zoom, keyboard-only, screen-reader, reduced-motion, and color-contrast scenarios.
- Provider or legal approval is revoked after prior approval evidence existed.

## Dependencies and Blockers

| Dependency | Needed for | Product-safe interim |
|---|---|---|
| BaaS/financial provider contract and credentials | real account, balance, ledger, and payments | test doubles and explicit simulation/unconfigured states |
| KYC provider and approved evidence policy | verified onboarding | KYC-ready status only; no verification claim |
| Notification provider/channel | delivery of recovery/transactional messages | in-product test status and recorded delivery attempts |
| Pix, transfer, boleto provider | real payment execution | existing simulation behavior |
| Cards, credit, investment, insurance, support providers | real product lifecycle | informational/draft/estimate/test-double flows |
| Ledger/reconciliation ownership decision | source of truth and mismatch resolution | architecture must select ownership before Wave 2 implementation |
| Legal/licensing/privacy review and jurisdiction | production launch | technical work may continue; capability remains blocked/unavailable |

## Success Criteria

- **SC-001**: 100% of inventoried existing customer behaviors are either preserved with acceptance evidence, intentionally improved with documented evidence, or removed through an approved product decision; zero are silently dropped.
- **SC-002**: 100% of REQ-001 through REQ-063 map to at least one product scenario, implementation task, and verification result before final sign-off.
- **SC-003**: 100% of Wave 1 through Wave 5 primary journeys complete at mobile, tablet, and desktop view sizes on every supported current browser, with zero critical accessibility blockers.
- **SC-004**: 100% of provider-unconfigured customer states clearly state that no real money moved or became real-account truth; zero non-simulation states imply a real movement.
- **SC-005**: For every repeated, timed-out, or duplicate provider outcome, the customer sees exactly one resulting account, movement, schedule effect, receipt, or notification.
- **SC-006**: Every existing customer journey remains available and produces no regression in its acceptance evidence after each wave.
- **SC-007**: Existing consumers complete their already-supported journeys without changes to their accepted request or response meaning.
- **SC-008**: A production-readiness claim is made only when the final review has zero unknown or untracked provider, reconciliation, licensing, regulatory, privacy, or security blockers.

## Assumptions

- “Preserve the current stack” means keeping the currently detected application majors, as evidenced by the active application manifests; exact versions remain an implementation constraint rather than a product promise.
- Portuguese (`pt-BR`) remains the authoritative locale because no additional current locale is present and no provider answer requested new localization.
- Provider selection is intentionally deferred to architecture/planning rather than treated as a false PM decision.
- Simulation stays available as a supported sandbox capability unless configuration disables it; it never becomes accounting or provider truth.

## Requirements Quality Checklist

- [x] Every user-facing behavior in the active product tree is inventoried, including error and empty states.
- [x] All six waves are covered and have independently testable customer outcomes.
- [x] All requirements use unique sequential REQ-001 through REQ-063 identifiers.
- [x] No unresolved clarification markers remain; unresolved providers are explicit external blockers, not ambiguous scope.
- [x] Acceptance requirements use observable conditions rather than internal implementation detail.
- [x] Scope exclusions and no-real-money/provider truth rules are explicit.
- [x] Accessibility, supported-browser, responsive, security, compatibility, and baseline-behavior gates are testable.
- [x] Each wave includes Given/When/Then scenarios.
- [x] Every REQ-001 through REQ-063 requirement maps to an independently executable scenario in `t3.1-pm-traceability.md`.
- [x] Downstream technical ownership is not substituted for product definition.

## Upstream Artifacts Consumed

- `.github/modernize/rearchitecture/clarification.md` — authoritative resolved scope, target-stack preservation, WCAG/browser/mobile constraints, provider-safe behavior, compatibility, and no-commit/legal-last guardrails.
- `.github/modernize/rearchitecture/artifacts/project-profile.yaml` — active application topology, scale, same-stack upgrade classification, and duplicated-tree planning risk.
- `.github/modernize/rearchitecture/clarification-answers.md` — submitted source answers, used to verify clarification defaults and explicit external provider gaps.
- `.github/modernize/rearchitecture/artifacts/t3-teamlead.md` — the specification-quality findings remediated in this artifact.

## Evidence Mapping

- `clarification.md#Frontend` → REQ-045–REQ-049 and SC-003 (WCAG 2.1 AA, evergreen browsers, mobile-first, existing styling/tokens).
- `clarification.md#Backend` → REQ-001–REQ-021, REQ-057–REQ-063, and SC-004–SC-007 (Spring 3/current stack, compatibility, hardened cookie/JWT, baseline SLA).
- `clarification.md#Generic` → six-wave structure, REQ-021/030/037/060, and SC-004 (provider-safe/test doubles, simulation-only money movement, legal last).
- `clarification.md#Downstream Usage Notes` → REQ-050–REQ-056 and the explicit account-ledger/reconciliation ownership dependency.
- `project-profile.yaml#assessment` → in-place brownfield scope and known-risk provider dependency.
- `t3-teamlead.md#Content Quality, Requirement Completeness, Feature Readiness` → clarified terms, technology-neutral success criteria, explicit product/implementation appendix, and `t3.1-pm-traceability.md`.
