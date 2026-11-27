---
schema: clarification-answers/v1
status: submitted
generated_at: "2026-11-23T02:55:37.047Z"
scope: [frontend, backend, generic]
questions_file: clarification-questions.json
---

# Rearchitecture Clarification Answers

## 🖥️ Frontend

- **F1** Which exact Next.js major version must the upgraded frontend target?
  - answer: Preserve current detected Next.js major
  - source: user
- **F2** Which component/UI library and major version should govern the expanded banking interfaces?
  - answer: Preserve existing detected library
  - source: user
- **F3** Provide screenshots, screen recordings, staging URLs, or Figma links for the current banking UI and key states.
  - answer: Referências locais atuais: tela de login em giro-app/app/page.tsx e dashboard bancário em giro-app/app/dashboard/page.tsx. Componentes de UI em giro-app/components/SimulationPanel.tsx, FinancialSimulators.tsx, CreateSplitGroupModal.tsx e ProcessSplitModal.tsx. Ainda não há URL de homologação, gravação ou link do Figma publicado.
  - source: user
- **F4** What is the authoritative design-system or token source for the new banking UI states?
  - answer: Use existing code as the source; match current styling pixel-for-pixel using extracted CSS custom properties.
  - source: user
- **F5** Which accessibility standard must the expanded UI satisfy?
  - answer: WCAG 2.1 AA
  - source: user
- **F6** Which browser and runtime compatibility matrix must be verified?
  - answer: modern evergreen (Chrome, Firefox, Safari, Edge — latest 2 major versions)
  - source: user
- **F7** Which responsive and breakpoint strategy should all new product interfaces follow?
  - answer: mobile-first using existing breakpoints detected from codebase
  - source: user
- **F8** Which locales must be supported across the expanded frontend?
  - answer: preserve current locales; keep existing i18n library if present
  - source: user
- **F9** Which client-side state-management approach should the new capability batches use?
  - answer: preserve existing pattern if identifiable; otherwise recommend minimal (component state + server-state library)
  - source: user
- **F10** Which Next.js routing strategy must the completed product use?
  - answer: use the de-facto router for the chosen target framework
  - source: user

## ⚙️ Backend

- **B1** Which exact Spring Boot major version must the upgraded backend target?
  - answer: Preserve current detected Spring Boot major
  - source: user
- **B2** What external API contract-preservation policy should govern the banking capability expansion?
  - answer: must preserve
  - source: user
- **B3** Which database and ledger data-migration approach is authorized for the capability completion?
  - answer: TBD / assess in research phase
  - source: user
- **B4** Which authentication and authorization framework policy should the security/session/recovery wave follow?
  - answer: Preserve cookie/JWT flow and harden it with Spring Security
  - source: user
- **B5** Which non-functional SLA and performance targets should gate the new backend capabilities?
  - answer: match current production baseline; no regression
  - source: user

## 📋 General

- **G1** What observable outcome constitutes successful completion of the banking MVP capability expansion?
  - answer: Complete all six prioritized waves in the existing codebase, preserve current behavior, provide provider-ready contracts and test doubles, keep money-movement behavior explicitly simulated until a configured real provider exists, and defer licensing/legal readiness to the final wave.
  - source: user
- **G2** What remains explicitly out of scope or must be excluded from implementation?
  - answer: Real money movement before a configured provider; unrelated rewrites; claiming an invented or unconfigured financial provider; committing changes; licensing/regulatory implementation before the final wave.
  - source: user
- **G3** What policy should apply to existing tests and baseline behavior during implementation?
  - answer: must pass
  - source: user
- **G5** Record any additional constraints and, if known, identify the intended BaaS/financial, KYC, notification, Pix/boleto, cards, credit, insurance, and support providers; required API/documentation locations; account/ledger ownership; settlement/reconciliation responsibilities; and provider sandbox availability.
  - answer: None beyond the decisions listed in this specification.
  - source: user
