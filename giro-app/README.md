This is a [Next.js](https://nextjs.org) project bootstrapped with [`create-next-app`](https://nextjs.org/docs/app/api-reference/cli/create-next-app).

## Security and authentication

The backend provides email/password registration and login. Passwords are stored as BCrypt hashes, and successful authentication returns a 15-minute HS256 JWT. The Next.js server stores that token in an `HttpOnly`, `SameSite=Lax` cookie; `proxy.ts` validates it before allowing access to `/dashboard`. Browser API calls are sent through the Next.js server, which forwards the token as a bearer credential to Spring Security.

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

AES-256-GCM field encryption protects BaaS account ID, routing number, branch, and account number at rest. In production, Flyway applies `V2__add_auth_and_sensitive_fields.sql`; the backend encrypts any legacy plaintext values in batches during startup before the application becomes ready. Keep the encryption key stable and back it up securely: changing it without re-encrypting stored data prevents decryption.

## Deploy the Spring backend to Render

The Render Blueprint is in [`render.yaml`](./render.yaml). Connect this GitHub repository in Render and create a Blueprint instance from that file. It builds the Dockerfile in `backend/`, enables the `prod` Spring profile, and uses `/actuator/health` as its health check. Production database settings target Render PostgreSQL; the old GCP Cloud SQL socket configuration is not used by this profile.

Create a Render PostgreSQL database and set `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD` on the backend service from that database's **internal** connection details. Set `GIRO_JWT_SECRET_BASE64` to the same 32-byte Base64 JWT secret used by the Next.js server, and `GIRO_ENCRYPTION_KEY_BASE64` to the existing 32-byte Base64 encryption key. Add both through Render's environment-variable/secret settings; never put key values in `render.yaml`, Git, or build arguments. Keep `BAAS_ENABLED=false` until a real BaaS integration is configured.

After the first deploy, confirm the service health check passes and copy its HTTPS URL into the Next.js deployment's `GIRO_BACKEND_URL`. Use the Render-provided public URL for frontend-to-backend server traffic only if necessary; prefer private service connectivity when both services run in Render. Do not rotate either key without planning a token/session invalidation or re-encrypting existing account data, respectively.

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

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

You can start editing the page by modifying `app/page.tsx`. The page auto-updates as you edit the file.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy the Next.js frontend to Vercel

Import this GitHub repository in [Vercel](https://vercel.com/new). Set the project root to the repository root (`./`), where the frontend `package.json` is located, and keep the detected Next.js build settings.

Wait until the Render backend is deployed and `/actuator/health` reports healthy. In the Vercel project's environment variables, add:

- `GIRO_BACKEND_URL`: the backend's actual public HTTPS URL, for example `https://giro-backend-api.onrender.com` (without a trailing slash).
- `GIRO_JWT_SECRET_BASE64`: the exact same 32-byte Base64 JWT secret configured on the backend.

Add these variables to Production (and Preview too, if preview deployments should use the backend), then redeploy so the functions receive them. Do not add `GIRO_ENCRYPTION_KEY_BASE64` to Vercel; it is backend-only. Never commit secret values.

The browser calls the Next.js `/api/auth` and `/api/proxy` routes, which forward requests server-side. Therefore, do not configure `NEXT_PUBLIC_API_URL`; use the server-only `GIRO_BACKEND_URL` instead. This keeps the backend address out of the client bundle and preserves the HttpOnly JWT-cookie flow.
