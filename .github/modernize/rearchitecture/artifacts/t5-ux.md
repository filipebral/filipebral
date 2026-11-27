# t5 — Mobile-First UX and WCAG 2.1 AA Flow Specification

**Role**: ux  
**Status**: ready for combined design-quality gate  
**Classification**: brownfield upgrade  
**Scope**: existing `giro-app/` customer experience across all six completion waves  

## Design Intent

Extend Giro without replacing its recognizable dark, lime-accented, rounded,
mobile-first interface or its Portuguese (`pt-BR`) customer language. The
front door remains the existing one-page sign-in/registration experience; the
authenticated shell remains a one-column dashboard at phone widths and may
progress to a constrained multi-column arrangement only after existing
content can remain readable and keyboard-order coherent.

The specification is documentation-only. Frontend owns implementation details;
backend and DBA own the data and service contracts. No screen may expose
credentials, JWTs, recovery codes, full account identifiers, provider
exceptions, or internal fraud thresholds.

## UX Guardrails

1. Preserve `/`, `/dashboard`, existing login/register copy and outcomes,
   existing simulation, calculators, split dialogs, and established
   component-level interaction patterns.
2. Add capabilities inside the existing dashboard information architecture;
   do not turn the public page into a marketing landing page or a provider
   onboarding wizard.
3. Keep the same-origin browser boundary invisible to customers. Loading and
   failure messages must not imply that the browser contacted Spring or a
   provider directly.
4. “Simulado”, “teste”, “não configurado”, “pendente”, and
   “confirmado pelo provedor” are distinct product truths. Never style or word
   a simulated/test outcome as a real balance, real account, real movement,
   verified identity, or delivered message.
5. Every primary action has an explicit loading, unavailable/unconfigured,
   pending, success, error, and safe-next-action presentation as applicable.
6. Never expose a retry for a provider-ambiguous operation until status is
   reconciled. The safe next actions are wait, check status, or support.
7. Use no invented provider names, real financial data, legal promises, or
   production-readiness claims.

## Existing Visual System and Responsive Rules

### Semantic token use

Frontend must reuse the exact current Tailwind utility tokens/custom properties
from the operative UI. This UX design introduces semantic roles, not replacement
hex values:

| Semantic role | Existing visual source | Required treatment |
|---|---|---|
| Page background | Existing dark application background | Fill the viewport; do not introduce a new brand background |
| Primary surface | Existing rounded dark card | Group one task or status history; maintain current radius and depth treatment |
| Secondary surface | Existing darker/lighter neutral already used in forms | Use for review summaries, histories, and disabled provider panels |
| Brand/accent | Existing lime accent | Use for primary action, selected navigation, and confirmed actionable focus; never for body copy alone |
| Primary text | Existing light foreground | Minimum 4.5:1 on normal text |
| Secondary text | Existing muted foreground | Minimum 4.5:1; if the current muted value fails, select the nearest existing higher-contrast neutral token |
| Error | Existing semantic error treatment if present | Minimum 4.5:1 and an icon/label; color is never the only signal |
| Status/warning | Existing neutral/lime/error family | Pair with state word and icon; never rely on red/green/lime alone |
| Focus | Existing accent or accessible existing high-contrast outline | Visible 3:1 focus indicator against adjacent colors; minimum 2 CSS-pixel outline or equivalent |

If any exact existing text/token pairing fails WCAG AA, the permitted visible
adjustment is a switch to the nearest existing higher-contrast foreground or
surface token. Do not add a parallel palette unless the coordinator approves it.

### Layout

- Base layout is 100% width, single column, with the existing page gutter.
- On phone widths, action rows stack in document order; primary action appears
  last and spans the available width. Essential controls never sit off-screen.
- Tablet/desktop may use at most a two-column content area after the heading
  and status summary. Forms remain in one reading column; a separate column may
  hold non-blocking context, disclosed limits, or status history.
- Modals become full-width bottom sheets or near-full-height panels on small
  screens. Additive dialogs must trap focus, close on Escape, preserve entered
  draft data on accidental dismissal where safe, and restore focus to the
  triggering control.
- Critical labels, amounts, references, and next actions must wrap. Do not
  truncate information required to understand a decision.
- Tables with more than three task-critical columns become stacked definition
  lists on phones. Horizontal scrolling is not acceptable for the only copy of
  essential data.
- Support 200% zoom and 400% zoom/reflow testing. No interaction requires
  dragging, hover, pinch, or color-only interpretation.
- Motion is functional only. Respect `prefers-reduced-motion`; never animate
  balance, status, or route transitions in a way that delays an announced
  outcome.

### Reusable interaction patterns

- **Page heading**: one `h1`, followed by a short status/purpose sentence.
- **Section heading**: `h2`; card headings do not skip levels.
- **Form field**: persistent label, relevant `autocomplete`, concise help,
  programmatic `aria-describedby`, and inline error adjacent to the field.
- **Submit**: disable repeated activation only while request is in flight;
  preserve the button name and add a visible/announced “Enviando…”.
- **Global result**: use a live `status` for progress and completion; use a live
  `alert` for actionable failure, with focus moved to a summary for errors
  preventing progression.
- **Provider state badge**: icon + state word + one-sentence meaning + one
  allowed next action. The badge alone never carries the truth claim.
- **Confirmation**: never use an unlabelled “OK”. Name the result and next action
  (for example, “Sessão encerrada” and “Voltar ao início”).
- **Destructive action**: require a clearly named confirmation when the effect
  cannot be undone. “Encerrar sessão”, not “Excluir”, is the session action.
- **Empty state**: name what is empty, why, and the available safe action. Do
  not present a provider absence as a system error.

## Information Architecture

Keep the existing dashboard entry and simulations. Add one compact, clearly
named mobile navigation system after the dashboard header:

1. **Início** — summary, truthful account/onboarding/provider readiness,
   outstanding safe actions, simulations.
2. **Pagamentos** — Pix, transferência, boletos, agendamentos, receipts;
   provider/simulation truth is visible at entry.
3. **Produtos** — cartões, crédito, investimentos, seguros.
4. **Histórico** — sessions, operations, notifications, support requests,
   with privacy-safe references.
5. **Ajuda** — recovery/support, disputes, terms/privacy.

Within the current product, the simulation, financial calculator, and split
controls remain in their existing discoverable locations. New navigation must
not hide them behind a provider-dependent feature gate.

## Wave 1 — Security, Session, Recovery, and KYC-Ready Onboarding

### 1. Public sign-in and registration

**Preserve**
- “Entrar” / “Criar conta” switch on `/`; switching clears the previous error.
- Name appears only for registration; CPF and password rules and existing
  safe conflict/credential messages remain.
- A successful request replaces history with `/dashboard`.
- Service-unavailable errors remain on the public page.

**Behavior**
1. Show one task at a time with persistent labels and a single submit.
2. Password reveal control, if added, must have the accessible name
   “Mostrar senha” / “Ocultar senha” and must not steal focus from the field.
3. During submit, disable repeat activation and keep field values intact.
4. Validation is announced inline. If multiple fields fail, focus the
   error-summary heading, which links to each field.
5. The role `alert` message never reveals whether CPF/password/recovery
   identity exists beyond the existing accepted wording.
6. A successful sign-in announces “Acesso realizado” before navigation where
   the runtime permits; navigation remains the visual completion.

### 2. Recovery entry and three-step flow

The public “Esqueci minha senha” action is placed directly after the password
field, not in provider/product navigation.

**Step 1 — Identify and request**
- Fields: identity (current CPF convention), then “Enviar instrução”.
- Supporting copy: “Responderemos da mesma forma, mesmo se não encontrarmos
  uma conta.” This is deliberate non-enumeration behavior.
- On every accepted request, display “Se uma forma de recuperação verificada
  estiver disponível, siga a próxima etapa. Caso contrário, fale com o
  suporte.” Do not confirm account existence.
- `CHANNEL_UNCONFIGURED` displays an informational panel with the support
  action; it is not an error that should automatically retry.
- The throttle state shows only “Tente novamente após <safe time>”.

**Step 2 — Verify**
- Fields: recovery reference (when supplied by the channel flow) and one-time
  code. Never echo a recovery code in activity history or logs.
- Expired, invalid, used, newer-request, or unknown codes share one safe
  message: “Não foi possível validar. Solicite uma nova recuperação.”
- Focus the error summary. A sixth failed attempt moves to a timed pause;
  fields become unavailable and the safe retry time remains visible.

**Step 3 — Reset**
- New password and confirmation, existing password policy displayed before
  entry.
- Success page: “Senha alterada. Todas as sessões anteriores foram
  encerradas.” Primary action returns to sign-in.
- The channel delivery status is separate from reset truth:
  `NOT_CONFIGURED`, `PENDING`, `DELIVERED`, or `FAILED`.

Legacy identity absence uses a distinct “Recuperação assistida” route to the
support request flow. Copy must say that a human must review access; no duplicate
customer record is created or implied.

### 3. Session management

Add **Segurança > Sessões** to the dashboard user/account menu. It is not a
provider feature.

**List**
- Heading “Sessões ativas”.
- Intro: “Somente sessões válidas e não encerradas aparecem aqui.”
- Each card shows existing-safe fields only: context label, created date/time,
  last use, expiry, and badge “Sessão atual”.
- Never show `sid`, IP address, JWT, user agent string, or hidden provider data.
- Current session appears once and is first.
- Empty/error state: “Não foi possível carregar as sessões.” + “Tentar
  novamente”; a 401 immediately follows the protected-page redirect behavior.

**Revoke another session**
- Action “Encerrar sessão em <context>”.
- Confirmation dialog restates context and effect. Do not require a second
  password unless a later approved security contract requires it.
- Success row disappears and the live region says “Sessão encerrada”.
- Idempotent repeat or stale list response remains a completion, not a failure
  page, when the session is no longer active.

**Revoke current session**
- Action “Encerrar sessão atual”.
- Confirmation then existing logout behavior: clear authenticated access and
  replace navigation with `/`.
- If server revocation reporting fails, the browser still signs out; copy on
  the next safe page is “Saída concluída. A sessão pode levar um instante para
  deixar de constar no histórico.” Never claim server revocation succeeded when
  it was not confirmed.

### 4. Onboarding/KYC readiness

Add a dashboard summary card titled **Status de cadastro**. Registration does
not mean identity approved.

**Status model**

| State | Customer meaning | Primary treatment |
|---|---|---|
| `REGISTERED` | Basic registration complete; review has not started | Lime-neutral informational card; “Ver próximos passos” |
| `EVIDENCE_REQUESTED` | Additional evidence is requested by an active approved flow | Form only after purpose/status disclosure |
| `SUBMITTED` | Evidence accepted for review, not approved | Pending summary; no “verified” icon |
| `PROVIDER_REVIEW` / `PENDING_INFORMATION` | Review or additional information pending | Safe wait/update action; no retry upload unless allowed |
| `APPROVED` | Approved provider source reports approval | Confirmed word; show safe date/reference, not evidence |
| `REJECTED` | Approved provider source rejected or did not approve | Explain safe reason category and support/appeal if available |
| `FAILED` | Technical attempt failed, not an identity rejection | “Tentar novamente” only if safe; otherwise support |
| `NOT_CONFIGURED` | Required provider/legal gate is inactive | “Indisponível no momento”; no evidence controls |
| `SIMULATED` | Approved local test-double state | Prominent “Simulação — não verifica identidade” |

Before evidence entry, display: purpose, provider/legal readiness, data items
to be collected, expected review outcome, and abandonment/return behavior.
When `NOT_CONFIGURED`, controls are disabled or omitted; there is always a
support/information action. Test-double `SIMULATED` can never show
“Aprovado”, a verified checkmark, or a real-account success state.

### 5. Security confirmations and notification truth

For sign-in, password reset, recovery, and revocation, show an in-product
confirmation record. Add a secondary line:
- `NOT_CONFIGURED`: “Nenhum canal externo está configurado; esta confirmação
  fica disponível aqui.”
- `PENDING`: “Envio externo iniciado; ainda não confirmado.”
- `DELIVERED`: “Confirmação entregue pelo canal configurado.”
- `FAILED`: “A confirmação externa não foi entregue; sua alteração de acesso
  não foi desfeita.”

## Wave 2 — Provider-Ready Account, Ledger, Reconciliation, and Notifications

### Account overview

The account card has one source-of-truth label:
- **Conta simulada** — fictional balance and actions only.
- **Conta confirmada pelo provedor** — only when the approved provider confirms.
- **Integração não configurada** — no account-opening action is enabled.
- **Pendente / Falhou / Suspensa / Requer conciliação** — each has a single
  customer-safe next action.

Never place simulated and confirmed-real balances in the same unlabeled total.
On provider absence, keep the current real-balance message and disable the
real-account action with a visible explanation. Do not replace it with a fake
success flow.

### Account opening

1. Entry shows provider readiness and the non-sensitive information needed.
2. Review names the operation and whether it is a test double, simulation, or
   provider-confirmed path. Real path remains absent until configuration.
3. Confirm is disabled while pending. Reopening the same intent returns the
   existing outcome; the UI must not create “account 2”.
4. `AMBIGUOUS` shows “Estamos confirmando o resultado. Não envie novamente”
   plus status check/support.
5. Failure states preserve the exact submitted values in a reviewable draft
   while preventing automatic resubmission.

### Operation and reconciliation history

Each row shows a privacy-safe reference, submitted/accepted/completed/rejected/
reversed/cancelled/unknown state, timestamp, and amount where appropriate.
State history is additive: show a chronological disclosure under “Ver etapas”;
do not overwrite previous outcomes. Reconciliation badges may be
`matched`, `missing`, `duplicate`, `mismatched`, or `unresolved`, with an
explanation and support action. “Matched” is the only automatic resolution
presentation; unresolved never becomes success.

Notifications use the same status list as above and are always independently
labelled from the underlying account/payment result.

## Wave 3 — Pix, Transfer, and Boleto

Use the same five-stage shell for all three flows:

1. **Criar/editar** — identify beneficiary/bill, amount, date, funding account.
2. **Revisar** — destination (masked), amount, date, fees, funding source,
   applicable limits, and “Simulado” or “Confirmado pelo provedor”.
3. **Confirmar** — explicit action naming the operation, not generic “Continuar”.
4. **Acompanhar** — pending, completed, rejected, cancelled, or ambiguous;
   only one permitted next action is offered.
5. **Comprovante/histórico** — safe reference, result, date, amount, masked
   destination, and source-of-truth label.

### Required state guidance

| Outcome | Message intent | Allowed next action |
|---|---|---|
| Invalid input | Identify field; do not echo a secret or full Pix/bill value | Edit field |
| Insufficient balance / limit | Plain-language cause and disclosed limit name | Edit, wait, or close |
| Blocked destination | No internal rule details | Review/support |
| Rejected | Provider-safe reason category | Review or support |
| Timeout/ambiguous | May exist externally; never resubmit | Check status/support |
| Completed | One receipt, explicitly simulated or provider-confirmed | View receipt/close |
| Cancelled | No further effect; state why/when if safe | Start a new separate intent only if user chooses |

Before confirmation, disclose daily/per-operation limits. If the provider is
unconfigured, the real-operation control is disabled/omitted and the existing
simulation route remains available. A test state must visibly say “Fluxo de
teste — nenhuma quantia real será movimentada.”

Scheduled payments retain current future-time rules, list all lifecycle states,
and require named cancellation confirmation. Receipts cannot be shared/exported
until exact fields are privacy-reviewed; the in-product receipt is sufficient.

Preserve existing simulated Pix, transfer, balance, schedule, boleto, and
statement actions and their one-operation idempotency behavior.

## Wave 4 — Cards, Credit, Investments, Insurance, and Support

### Product discovery

The **Produtos** page shows cards, credit, investment, insurance, and support
as named entries. Each entry starts with a large state phrase:
“Informativo”, “Rascunho”, “Simulação”, “Pendente”, or
“Confirmado pelo provedor”. Never use an empty card as evidence that a product
is active.

### Cards

An unconfigured card area explains “Nenhum cartão foi emitido”. The interface
supports informational readiness, draft request, and provider-confirmed views
when later enabled. Card-detail controls for reveal/hide, activate, block/unblock,
limits, and lost/fraud reporting are disabled or absent until the approved
provider exists. Sensitive display reveal must require a separate named action,
use a status announcement, and auto-hide after leaving the view.

### Credit

Review screen order is: amount, term, estimated installment, disclosed fees,
total, assumptions, and “Este cálculo não contrata crédito.” Provider-approved
offer, application, and accepted/deferred/declined states are separate.
Unconfigured action: save estimate only.

### Investments

Preserve the existing calculator and result disclaimer. Add a source-of-truth
progression around it:
`Estimativa` → `Oferta` → `Solicitação` → `Alocação` →
`Posição confirmada pelo provedor`.
Do not describe an estimate as an owned position.

### Insurance

Flow: quote → coverage summary → exclusions → application → policy status →
cancel request. A quote or saved request never states that coverage exists.
Cancellation is a request state, not an automatic policy cancellation claim.

### Support

Entry appears from every blocking error. Form categories are operation,
onboarding, card/product, access/security, dispute, and privacy/legal.
The form tells users not to attach passwords, access codes, full account data,
full Pix keys, or bill codes. On submit, show one non-sensitive reference and
pending status. Detail view lists submitted context, status, latest safe
response, and allowed next action.

## Wave 5 — Observable, Safe, and Accessible States

### Service degradation

Every critical view can show:
- **Disponível** with last useful timestamp when context requires it.
- **Lento/pendente** with a safe wait time or “check status”.
- **Indisponível** with retry now or support.

Technical codes, stack traces, provider payloads, fraud rules, and thresholds
are never shown. A privacy-safe customer operation reference may be shown for
support. If a request may have been submitted, remove unsafe “try again” and
offer status check/support instead.

### Limits and review holds

Before confirmation, state applicable operation/daily limits and provider
restriction in plain language. Outcomes:
`allowed`, `additional verification required`, `temporarily held for review`,
`released`, `rejected`. Show the customer-safe reason and one next action; do
not expose rules, risk score, or internal threshold.

### Operation history

The customer view excludes secrets, raw evidence, full account/Pix/bill
identifiers, and internal actor data. Keep only event time, target category,
safe reference, state, and customer-safe reason. The underlying audit
requirement does not justify exposing more data in the UI.

### WCAG 2.1 AA interaction requirements

- All controls are native or expose complete name, role, value, and state.
- Keyboard sequence follows visual and reading order; no positive `tabindex`.
- Focus never becomes invisible or remains inside a removed element.
- Dialog initial focus goes to the heading for complex destructive content or
  the first relevant control for short data-entry dialogs. Escape closes unless
  doing so would lose an irreversible operation.
- Dialog close returns focus to the trigger. Route transitions place focus at
  the page `h1` after asynchronous content is established.
- Loading uses visible text plus a live status. Do not announce repetitive
  percentage/progress noise.
- Errors have text and icon; form fields set invalid state and error
  association. Focus the summary when more than one field blocks progress.
- Status is also conveyed by a word, not only lime/red color.
- Touch targets are at least 44 by 44 CSS pixels for standalone controls.
- Current page and selected tabs use programmatic state and a non-color marker.
- Reduced motion disables non-essential animation and smooth scrolling.
- Page content remains operable at 320 CSS-pixel equivalent width and 200%/400%
  zoom scenarios.
- Supported runtime matrix: latest two major versions of Chrome, Firefox,
  Safari, and Edge.

## Wave 6 — Licensing and Legal Readiness

Legal readiness is a final-wave concern and must never be implied by earlier
technical completion.

### Customer surfaces

- Add **Termos, privacidade e contato** under Ajuda.
- List each currently available product/capability with status:
  `Indisponível`, `Simulado`, `Piloto`, `Habilitado com aprovação`.
- Before an eligible product action, show only the disclosure and consent
  version applicable to that capability. Preserve a submitted version and
  effective date; do not pre-check consent.
- If terms materially change, require renewed consent only when the policy
  decision says it is required; route the customer to the changed disclosure.
- Pricing/fee disclosures remain adjacent to the operation review, not buried
  only in general terms.
- Complaints/disputes and data access/deletion requests use support-reference
  flows, not ordinary chat-like submission.

When any legal/provider approval is missing, the capability remains unavailable,
simulated, or pilot/test-only. “Concluído tecnicamente” must not be rendered as
“pronto para produção”.

## Content and Localization Rules

- Continue `pt-BR` as the authoritative locale; labels, currency, dates, and
  decimal input follow the locale without changing wire semantics.
- Prefer explicit terms: **Simulado**, **Não configurado**,
  **Confirmado pelo provedor**, **Aguardando confirmação**, **Ambíguo**.
- Do not use “verificado” for registration or `SIMULATED`; reserve verification
  for an authoritative approved provider/legal state.
- Do not use “concluído” for an ambiguous, pending, or merely locally accepted
  provider operation.
- Rephrase rather than expose technical state names where helpful, but preserve
  the state meaning exactly in accessible text.
- All times displayed to customers include timezone context when required for
  expiry/retry correctness; do not silently display backend-only UTC.

## Responsive Journey Checklist

| Journey | Phone | Tablet | Desktop | Zoom/keyboard | Status/error variant |
|---|---:|---:|---:|---:|---|
| Login/register | Required | Required | Required | Required | Invalid, 409, 502, loading |
| Recovery/reset | Required | Required | Required | Required | Unknown, expiry, throttle, unconfigured |
| Session list/revoke | Required | Required | Required | Required | Empty, load error, current revoke |
| KYC-ready onboarding | Required | Required | Required | Required | All normalized states and simulated gate |
| Account/provider | Required | Required | Required | Required | Unconfigured, pending, ambiguous, active, failed |
| Pix/transfer/boleto | Required | Required | Required | Required | Review, pending, rejected, ambiguous, receipt |
| Cards/credit/investment/insurance | Required | Required | Required | Required | Informational/draft/test/confirmed |
| Support/dispute/privacy | Required | Required | Required | Required | Submit, pending, response, unavailable |
| Terms/consent/legal | Required | Required | Required | Required | Missing/changed/outstanding approval |

## Automated and Manual Verification Contract

Frontend validation should include the following design acceptance checks:

1. WCAG automated scan for every primary state: normal, loading, disabled,
   error, empty, pending, timeout, dialog, and unconfigured.
2. Keyboard-only completion for login/register, recovery, session revoke,
   onboarding, payment review, support, and terms.
3. Screen-reader smoke checks for heading order, labels, error association,
   live result, status transition, dialog entry/exit, and focus return.
4. Mobile (320 CSS-pixel equivalent), tablet, desktop, 200%, and 400% zoom
   inspection with no clipped essential content or task-blocking horizontal
   scroll.
5. Latest two majors of Chrome, Firefox, Safari, and Edge.
6. Contrast checks for every existing token pairing actually used in interactive
   and disabled states; disabled controls are visually subdued but essential
   status copy remains readable.
7. Multi-step provider-gated flows prove that local test doubles never become
   real provider approval, real account truth, or real movement.
8. No test stores or displays JWT, `sid`, recovery code, raw provider error,
   full financial identifiers, or provider secrets.

## Requirement Coverage

| UX area | Requirements |
|---|---|
| Preserve/auth/recovery/sessions/KYC | REQ-001, REQ-002, REQ-003, REQ-004, REQ-005, REQ-006, REQ-007, REQ-008, REQ-009, REQ-010, REQ-011 |
| Account/provider/reconciliation/notifications | REQ-012, REQ-013, REQ-014, REQ-015, REQ-016, REQ-017, REQ-018, REQ-019, REQ-020, REQ-021 |
| Pix/transfer/boleto/schedules/receipts | REQ-022, REQ-023, REQ-024, REQ-025, REQ-026, REQ-027, REQ-028, REQ-029, REQ-030 |
| Cards/credit/investments/insurance/support | REQ-031, REQ-032, REQ-033, REQ-034, REQ-035, REQ-036, REQ-037, REQ-038 |
| Observability/limits/history/accessibility | REQ-039, REQ-040, REQ-041, REQ-042, REQ-043, REQ-044, REQ-045, REQ-046, REQ-047, REQ-048, REQ-049 |
| Legal/consent/readiness | REQ-050, REQ-051, REQ-052, REQ-053, REQ-054, REQ-055, REQ-056 |
| Cross-wave truth, compatibility, preservation | REQ-057, REQ-058, REQ-059, REQ-060, REQ-061, REQ-062, REQ-063 |
| Measurable UX/browser outcome | SC-003 |
| Provider-unconfigured truth | SC-004 |
| Duplicate outcome and behavior preservation | SC-005–SC-007 |

## Upstream Artifacts Consumed

- `.github/modernize/rearchitecture/artifacts/t1-pm.md` — six-wave customer
  requirements, REQ-001–REQ-063, existing UI inventory, provider truth rules,
  responsive/accessibility acceptance, and SC-003–SC-007.
- `.github/modernize/rearchitecture/artifacts/t2-architect.md` — operative
  `giro-app/` scope, frozen frontend units, BFF/cookie continuity, and Wave 1
  architecture handoff.
- `.github/modernize/rearchitecture/artifacts/t2.2-architect.md` — additive
  session, recovery, KYC state, provider truth, error, and downstream frontend
  conformance contracts.
- `.github/modernize/rearchitecture/clarification.md` — mobile-first existing
  token/styling, `pt-BR`, evergreen-browser, WCAG 2.1 AA, provider/test-double,
  simulation-only, legal-last, API-compatibility, and no-commit rules.
- `.github/modernize/rearchitecture/constitution.md` — Principle VI visual and
  accessibility gates, Principle IV provider truth, and delivery evidence rules.

## Evidence Mapping

- `t1-pm.md#Current Capability Inventory` → preserved flows, baseline language,
  dark/lime visual roles, and modal/accessibility gaps.
- `t1-pm.md#Six-Wave Product Definition` and `t1-pm.md#Cross-Wave Functional
  Requirements` → Wave 1–Wave 6 flow/state sections and requirement coverage.
- `t1-pm.md` REQ-045–REQ-049 / SC-003 → responsive journey matrix,
  WCAG interaction requirements, and browser/zoom verification contract.
- `t2-architect.md#Wave 1 Architecture Handoff` → BFF-safe session,
  recovery/KYC, and no-secret browser behavior.
- `t2.2-architect.md#1 Session and JWT Contract` → session-list/revoke/current
  session and sign-out behavior.
- `t2.2-architect.md#2 Recovery Contract` → three-step recovery, throttle,
  non-enumeration, reset, and delivery-status copy.
- `t2.2-architect.md#3 KYC-Ready Onboarding Contract` → provider/legal gate,
  normalized states, and prohibition on simulated approval.
- `t2.2-architect.md#7 Error and Truthfulness Contract` → simulation, pending,
  ambiguous, unconfigured, and safe-next-action wording.
- `clarification.md#Frontend` → exact stack-preservation, mobile-first,
  existing-style/token, `pt-BR`, browser, and WCAG constraints.

## Test Results

- Command: `python -c "…validate REQ-001 through REQ-063, required sections, unresolved markers, and notify-tag placement…"`
- Passed: 63/63 requirement IDs; required sections present; 0 unresolved
  markers; 0 misplaced notification tags.
- Failed: 0.
- Skipped: 0.
- Runtime UI tests: not applicable at design phase; required as downstream
  implementation evidence by the automated/manual verification contract above.

## Handoff

The frontend implementation must preserve the semantic roles, flow states,
focus rules, responsive behavior, and exact existing dark/lime utility tokens.
Backend responses must provide the additive machine-readable states needed by
this copy without exposing provider internals. Tester should use the
verification contract and responsive journey checklist as the UX evidence
matrix after each wave.
