# Browser Session Management Handle

Keep the JWT `sid` private and use an independently generated browser-safe session management handle for authenticated listing and revocation.

## What Happened
During Giro task t6.1, the design review found that one contract forbade the JWT
`sid` in browser JSON while another required the browser session-list `id` to
equal that `sid`. The contradictory contracts prevented deterministic
implementation and privacy-safe revocation routing.

## Takeaway
Generate a distinct high-entropy `sessionHandle`, expose it only in the
authenticated owner session list and same-origin revoke request body, authorize
every lookup by user plus handle, and use the private JWT `sid` only for
server-side current-session and protected-request validation. Never derive the
public handle from the `sid` or place it in a URL.

## History
- 2026-11-23 (giro-app/t6.1): initial
