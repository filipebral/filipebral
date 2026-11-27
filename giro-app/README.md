This is a [Next.js](https://nextjs.org) project bootstrapped with [`create-next-app`](https://nextjs.org/docs/app/api-reference/cli/create-next-app).

## Security and authentication

The backend provides CPF/password registration and login. Passwords are stored as BCrypt hashes, and successful authentication returns a 15-minute HS256 JWT. CPF values are encrypted at rest; a keyed HMAC index supports lookups without storing a plain CPF search key. The Next.js server stores the token in an `HttpOnly`, `SameSite=Lax` cookie; `proxy.ts` validates it before allowing access to `/dashboard`. Browser API calls are sent through the Next.js server, which forwards the token as a bearer credential to Spring Security.

The CPF check-digit validation is only a format check; it does not verify identity or open a bank account. Existing email-only users are not assigned a CPF automatically. No legacy-account migration/recovery flow is implemented yet, so those users cannot log in until a verified migration flow is added and completed. A real financial account requires a contracted BaaS/banking provider, its approved onboarding and KYC flow, and securely configured API credentials. The current mock BaaS is not a real bank integration; do not enable it for production onboarding.

New personal registrations require an authoritative regular CPF result matching the CPF and birth date. `TaxRegistrationVerificationPort` defines the future CPF and CNPJ lookup boundary; no adapter is registered, so new registrations return 503 until a provider is implemented and configured. Existing CPF/password logins are unchanged. Birth dates are passed only for verification and are not stored by the application. A provider must fail closed on errors, mismatches and inconclusive results.

Serpro is one potential provider: [Consulta CPF v3](https://centraldeajuda.serpro.gov.br/duvidas/pt/avisos/avisoconsultacpfv3/) requires CPF and birth date, and [Consulta CNPJ](https://loja.serpro.gov.br/consulta-cnpj/product/consultacnpj) provides company registry information. Provider selection, contracting where required, credentials, actual adapters, response-contract verification, safeguards against lookup abuse, and privacy review remain necessary. No real lookup occurs today. CNPJ verification must require an active registration, and business access must additionally verify the representative's identity and authority.

### Future business access

Business login remains unavailable. An authenticated personal user can register one pending business application in the dashboard or via `POST /api/business-applications` with `{"cnpj":"..."}` and retrieve their own via `GET /api/business-applications/current`. CNPJs are normalized and encrypted at rest. The application cannot yet be edited or deleted; no documents are collected and no verification is initiated. Personal registration itself remains unavailable without a CPF verifier. The default in-memory H2 database loses applications when the backend stops.

`CnpjValidator` checks format and check digits for numeric and alphanumeric CNPJs but does not establish existence, active status, identity, or authority. `TaxRegistrationVerificationPort` and `BusinessVerificationProviderPort` are unimplemented boundaries. `POST /api/business-applications/current/verification` records a request once and reports all three checks as `WAITING_FOR_PROVIDER`. It does not collect documents, contact a provider or grant access. Applications remain pending indefinitely. Merely configuring an adapter will not process or approve them. The future workflow must bind provider results to the application and applicant, verify active status, representative identity and authority, and grant access only after a verified decision. Pending, rejected, unavailable or inconclusive verification must not grant access. Implement secure collection, auditability, retention, correction/deletion, authentication and authorization before enabling the flow. Business access is separate from opening a real banking account.

### Account and Pix simulation

The dashboard offers a separate simulated account, fictitious balance, paginated simulated transactions, simulated receipts and simulated Pix, transfer and bill debits. Simulated bills accept only a fictitious description, amount and due date, not a barcode. Users can add fictitious balance and specify a fictional recipient name; no provider is contacted, no real money moves, and no recipient is credited. Do not enter real Pix keys, barcodes or personal information. Simulation data is stored separately from real account data and is never a source of truth for a banking provider.

The authenticated `/simulation` endpoints are available in production as well as development; set `APP_SIMULATION_ENABLED=false` to disable them. Each credit, Pix or scheduled Pix request uses a user-scoped `requestId` for safe retries; reusing it for different operation details is rejected. Simulated Pix schedules are checked every 30 seconds while the backend is running; overdue schedules are processed after it restarts. Insufficient fictitious balance marks the schedule as failed without retry. A schedule is processed atomically and only once, including across backend instances.

`GET /simulation/history?page=0&size=50` provides paginated simulated transaction history, and `GET /simulation/operations/{requestId}` retrieves a completed simulated operation for its authenticated owner. A 404 does not prove that a concurrent request cannot still complete. Retry only with the original request ID; the current dashboard keeps pending IDs in memory, not across page reloads. These endpoints do not track real payments or reconcile with a provider.

The simulation reconciliation queue registers request IDs through `POST /simulation/reconciliation` with `{"requestId":"<uuid>"}`. `GET /simulation/reconciliation` lists the authenticated owner's checks. A background worker checks only committed simulated balance transactions. A missing transaction remains `UNKNOWN`, even if the request may still be in flight. `/simulation/reconciliation/{id}/check` requests a new check and `/stop` stops monitoring without cancelling the underlying operation or proving failure. Registering the same ID resumes a stopped check. At most 50 unknown checks per user may be active. The queue never submits or retries payments, cannot reconcile a real provider, and cannot recover original operation inputs. The dashboard registers checks before simulated credit, Pix and transfer operations.

Before offering real services, implement the chosen provider's contracts, onboarding and identity verification, authorization, reconciliation, transaction lifecycle and operational/security controls; the simulation does not provide these.

The dashboard also includes local loan and investment calculators. The loan estimate uses fixed monthly installments (Price system); the investment estimate compounds monthly and adds contributions at each month-end. Rates and terms are supplied by the user. Neither calculator includes taxes, fees or inflation, and neither offers or contracts a financial product.

### Simulated cards, finance organization, splits and notifications

The virtual card is a simulation only and has no PAN, expiration date, CVV, physical-card issuance or real authorization. Its user-configured fictional credit limit tracks monthly simulated credit purchases; debit purchases and statement payments use only the simulated account balance. Blocking, unblocking, loss and fraud reports affect only the simulation. Simulated purchase history, statements and categories contain no provider-confirmed transactions.

Financial organization classifies recorded simulated expenses, tracks monthly category budgets, and displays in-app budget alerts. Goals reserve part of the simulated balance, reducing the simulated available balance until the user releases the amount. These features do not connect to real balances or yield.

Rateio history records each calculated share. The owner can edit groups and mark a participant's share as manually reported as paid; a participant can report only their own share. This is an acknowledgement, not proof of settlement or a money transfer. External invitations remain disabled until an email or SMS service is configured.

After a simulated account, card, or financial-organization action, the dashboard refreshes the related panels. Creating a split expense also refreshes split history. These updates are local to the current dashboard; automatically processed schedules and changes in other sessions require a later refresh.

The in-app notification inbox supports read status. Email, push, and SMS are reported as `NOT_CONFIGURED`; the app does not claim delivery and has no external notification sender. `GET /simulation/providers/readiness` shows whether provider adapters are registered.

Successful simulated balance operations, schedules, bills, card actions, goal movements and business application creation write audit events in the same database transaction. Successful logins and rejected credentials are also recorded; a rejected login has no identified actor or resource. Events contain IDs, type and timestamp, not CPF/CNPJ, passwords or payment details. Validation failures rejected before the login handler, other actions and access failures are not all covered.

This is partial operational auditing, not tamper-proof audit storage: database administrators can alter it, and there is no retention policy, alerting or centralized monitoring. Before public use, ship audit and gateway security events to independently controlled, append-only storage, restrict and periodically review access, define retention and incident procedures, and ensure the storage is monitored. Do not rely on this table as a complete regulatory audit trail.

`AuditArchivePort` and `AuditAlertPort` prepare an export worker, disabled by default. When explicitly enabled with `app.audit.export.enabled=true`, both adapters must be configured or startup fails. Committed audit events stay pending until the archive acknowledges them; failures are logged, postponed and passed to the alert adapter. Delivery may be repeated after a crash, so the archive must deduplicate by event ID. The archive must independently enforce immutable retention. No archive or alert adapter is provided, no retention has been selected, and alerts themselves are not durably queued. This preparation does not make the local database immutable or guarantee alerts.

Unknown accounts still perform a password-hash check to reduce one account-enumeration timing difference; this is not a guarantee of constant-time authentication. Login throttling remains a deployment prerequisite. Apply rate limits and abuse detection at a trusted gateway that sees the actual client, and protect direct backend access as well. The Next.js server forwards login requests to the backend without a trusted client address, so treating the backend peer or an arbitrary forwarded header as the client would be ineffective or unsafe. Configure trusted proxy boundaries and distributed limits for the chosen deployment; no gateway limiter is currently installed or enabled. The CPF-based lockout remains deliberately deferred.

### Provider integration boundary

`FinancialProviderPort`, `CardProviderPort`, `CreditProviderPort`, `InvestmentProviderPort`, `InsuranceProviderPort`, and `NotificationDeliveryPort` define provider-neutral contracts only. This repository does not select or connect a financial provider through these ports. Do not register a production adapter until provider, KYC/AML, safeguarding/settlement, dispute, reconciliation, card-network, data-processing, secret-management, and incident-response responsibilities are contractually approved. `BAAS_ENABLED` must remain false until that work and the production integration are separately implemented and validated.

Backend tests include authenticated API integration tests against a temporary PostgreSQL container; running the full Maven test suite requires a working Docker daemon. Test data and test keys are isolated from the configured application database.

The legacy `POST /accounts` endpoint always returns 503, including when `BAAS_ENABLED=true`; enabling the flag alone cannot open an account. `FinancialProviderPort` also defines a preliminary Pix-key submission and lookup contract, with distinct pending, settled, rejected and unknown outcomes. No adapter, endpoint, operation journal, recipient verification, provider reconciliation or real Pix execution implements this contract. A provider timeout or an empty lookup must not be interpreted as a failed payment or automatically retried; integration must preserve the idempotency key and resolve uncertain outcomes with the provider. QR codes, refunds, scheduling, transfer and bill-payment provider contracts remain future work.

Run Next.js as a server (for example, `npm run start`). Static export is disabled because authentication cookies, API routes, and `proxy.ts` require server-side execution.

Generate two independent 32-byte keys and provide them as Base64 secrets. Do not commit the values:

```powershell
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
$jwtKey = New-Object byte[] 32
$encryptionKey = New-Object byte[] 32
$rng.GetBytes($jwtKey)
$rng.GetBytes($encryptionKey)
$env:GIRO_JWT_SECRET_BASE64 = [Convert]::ToBase64String($jwtKey)
$env:GIRO_ENCRYPTION_KEY_BASE64 = [Convert]::ToBase64String($encryptionKey)
```

Set `GIRO_JWT_SECRET_BASE64` for both the backend and Next.js server. Set `GIRO_ENCRYPTION_KEY_BASE64` for the backend only, preferably through the deployment platform's secret manager. Set `GIRO_BACKEND_URL` for Next.js when the backend is not at `http://localhost:8080`.

For local RabbitMQ, provide `RABBITMQ_DEFAULT_USER` and `RABBITMQ_DEFAULT_PASS` to Docker Compose; the compose file intentionally contains no default credentials.

AES-256-GCM field encryption protects CPF and BaaS account ID, routing number, branch, and account number at rest. In production, Flyway applies the database migrations; the backend encrypts any legacy plaintext account fields in batches during startup before the application becomes ready. Keep the encryption key stable and back it up securely: changing it without re-encrypting stored data prevents decryption and invalidates CPF lookup indexes.

## Deploy the Spring backend to Render

The Render Blueprint is in [`giro-app/render.yaml`](./render.yaml). Connect this GitHub repository in Render and select that Blueprint file (`giro-app/render.yaml`). The service builds from `giro-app/backend/`, enables the `prod` Spring profile, and uses `/actuator/health` as its health check. By default, production connects to Render PostgreSQL through `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD`.

Create a Render PostgreSQL database and set `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD` on the backend service from that database's **internal** connection details. Set `GIRO_JWT_SECRET_BASE64` to the same 32-byte Base64 JWT secret used by the Next.js server, and `GIRO_ENCRYPTION_KEY_BASE64` to the existing 32-byte Base64 encryption key. Add both through Render's environment-variable/secret settings; never put key values in `render.yaml`, Git, or build arguments. Keep `BAAS_ENABLED=false` until a real BaaS integration is configured.

After the first deploy, confirm the service health check passes and copy its HTTPS URL into the Next.js deployment's `GIRO_BACKEND_URL`. Use the Render-provided public URL for frontend-to-backend server traffic only if necessary; prefer private service connectivity when both services run in Render. Do not rotate either key without planning a token/session invalidation or re-encrypting existing account data, respectively.

### Connect the Render backend to Google Cloud SQL for PostgreSQL

The backend includes the Google Cloud SQL PostgreSQL Socket Factory. To use it instead of Render PostgreSQL:

1. Enable the Cloud SQL Admin API and grant a dedicated Google service account the `Cloud SQL Client` role for the project containing the instance.
2. Ensure the Cloud SQL instance has a public IP. The connector encrypts and authenticates the connection, but Render still needs a network route to the instance; this profile selects the public IP.
3. In the Render backend service, add the service account JSON as a **Secret File** named `cloudsql-service-account.json`. Render mounts it at `/etc/secrets/cloudsql-service-account.json`; do not commit or paste this file into Git.
4. Set `CLOUD_SQL_INSTANCE` to the instance connection name (`project-id:region:instance-id`), and set `DB_NAME`, `DB_USER`, and `DB_PASSWORD` to the Cloud SQL database credentials. `DB_HOST` and `DB_PORT` are not used by this profile.
5. After the secret file and variables are present, set `SPRING_PROFILES_ACTIVE` to `prod,cloudsql` and redeploy. The `cloudsql` profile uses the Google connector with `ipTypes=PUBLIC`; it does not rely on the Render PostgreSQL host.

Do not grant the service account broader roles than required, and never share its JSON key or the database password in chat or source control. The Render Blueprint keeps `prod` as the default profile so existing deployments continue to use their configured PostgreSQL host until Cloud SQL credentials are installed and the profile is explicitly switched.

### Run the backend container against Cloud SQL from Docker Desktop

Cloud SQL is a managed database; Docker Desktop does not start a local copy of it. The optional `docker-compose.cloudsql.yml` override runs the backend container with the Cloud SQL connector and leaves the database in Google Cloud.

1. Install the Google Cloud CLI and run `gcloud auth application-default login` with an account that has the `Cloud SQL Client` role on the instance's project. The Cloud SQL Admin API must be enabled.
2. Create a root `.env` file (it is ignored by Git) with the Cloud SQL connection name, database name, user, password, and the full Windows path to the ADC credentials file:

   ```dotenv
   CLOUD_SQL_INSTANCE=project-id:region:instance-id
   DB_NAME=database-name
   DB_USER=database-user
   DB_PASSWORD=replace-with-database-password
   GOOGLE_APPLICATION_CREDENTIALS_FILE=C:\Users\your-user\AppData\Roaming\gcloud\application_default_credentials.json
   ```

3. Ensure the instance has a public IP, then start the backend from the `giro-app` directory in PowerShell:

   ```powershell
   docker compose -f docker-compose.yml -f docker-compose.cloudsql.yml up --build backend
   ```

   The Cloud SQL Compose override also starts RabbitMQ for the backend. Set `RABBITMQ_DEFAULT_USER` and `RABBITMQ_DEFAULT_PASS` in `.env` to unique local development credentials before starting it. The ADC credentials file is mounted read-only and is not copied into the image. Keep `.env` and all credential files out of source control. To stop the containers, press `Ctrl+C`; remove containers with `docker compose -f docker-compose.yml -f docker-compose.cloudsql.yml down`. The Cloud SQL database and its data are not removed by these commands.

## Getting Started

First, run the development server:

```bash
npm run dev
# or
yarn dev
# or
pnpm dev
# or
bun dev
```

The development script uses Webpack to work around a Turbopack/PostCSS subprocess crash observed on Windows. To try Turbopack separately, run `npx next dev --turbopack` after stopping the existing development server.

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

You can start editing the page by modifying `app/page.tsx`. The page auto-updates as you edit the file.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy the Next.js frontend to Vercel

Import this GitHub repository in [Vercel](https://vercel.com/new). Set the project root to `giro-app`, where the frontend `package.json` is located, and keep the detected Next.js build settings.

Wait until the Render backend is deployed and `/actuator/health` reports healthy. In the Vercel project's environment variables, add:

- `GIRO_BACKEND_URL`: the backend's actual public HTTPS URL, for example `https://giro-backend-api.onrender.com` (without a trailing slash).
- `GIRO_JWT_SECRET_BASE64`: the exact same 32-byte Base64 JWT secret configured on the backend.

Add these variables to Production (and Preview too, if preview deployments should use the backend), then redeploy so the functions receive them. Do not add `GIRO_ENCRYPTION_KEY_BASE64` to Vercel; it is backend-only. Never commit secret values.

The browser calls the Next.js `/api/auth` and `/api/proxy` routes, which forward requests server-side. Therefore, do not configure `NEXT_PUBLIC_API_URL`; use the server-only `GIRO_BACKEND_URL` instead. This keeps the backend address out of the client bundle and preserves the HttpOnly JWT-cookie flow.
