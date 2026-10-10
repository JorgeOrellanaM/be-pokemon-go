# Authentication — Frontend Guide

How to register users, log them in and call protected endpoints. It complements the general contract in [API.md](API.md) (response envelope, Pokemon endpoints, error reference).

- **Base URL (local):** `http://localhost:8080`
- **Scheme:** stateless JWT bearer token. No cookies, no server session, no CSRF token.
- **Token lifetime:** 1 hour. There is no refresh token: when it expires, the user logs in again.
- **CORS:** not configured on the backend. During development, proxy `/api` to `http://localhost:8080` (e.g. Vite `server.proxy`).
- **Demo account:** username `demo`, password `Pokemon123!`

---

## 1. Flow at a glance

```
 ┌──────────┐   POST /api/v1/auth/register  {username, password}   ┌─────────┐
 │          │ ───────────────────────────────────────────────────► │         │  201 { username }
 │          │                                                      │         │
 │          │   POST /api/v1/auth/login     {username, password}   │         │
 │ Frontend │ ───────────────────────────────────────────────────► │ Backend │  200 { accessToken, tokenType, expiresAt }
 │          │                                                      │         │
 │          │   PUT /api/v1/local-pokemon/25                       │         │
 │          │   Authorization: Bearer <accessToken>                │         │
 │          │ ───────────────────────────────────────────────────► │         │  200 … or 401 if missing/invalid/expired
 └──────────┘                                                      └─────────┘
```

1. **Register** (optional, once). Registering does **not** log the user in.
2. **Log in** → keep `accessToken` and `expiresAt`.
3. Send `Authorization: Bearer <accessToken>` on every **protected** request (it is harmless to send it on public ones too, as long as it is valid — see [§5](#5-which-routes-need-the-token)).
4. On any `401` from a protected route, or once `expiresAt` has passed: drop the token and send the user to the login screen.
5. **Log out** = forget the token on the client. There is no logout endpoint.

---

## 2. Register

`POST /api/v1/auth/register` — public

### Request

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "ash",
  "password": "Pikachu123!"
}
```

| Field | Type | Rules |
|---|---|---|
| `username` | string | Required. 3–30 characters. Letters, digits, `.`, `_`, `-` only. **Case-insensitive**: surrounding spaces are removed and it is stored in lowercase (`" Ash "` → `"ash"`). |
| `password` | string | Required. 8–72 characters (precisely: at most 72 UTF-8 bytes, so fewer when using accented letters or emoji). Kept exactly as typed, spaces included. |

No other fields are accepted (see the unknown-field error below).

### Response `201 Created`

Header: `Location: http://localhost:8080/api/v1/auth/me`

```json
{
  "success": true,
  "data": {
    "username": "ash"
  }
}
```

`data.username` is the **normalized** username — show this one to the user, since it is what they must log in with.

### Errors

**`400` — invalid fields.** Every invalid field is listed at once; map `errors[].field` to the form inputs.

```json
{
  "success": false,
  "error": {
    "status": 400,
    "error": "Bad Request",
    "message": "Please check the highlighted fields.",
    "path": "/api/v1/auth/register",
    "timestamp": "2026-10-10T11:46:04.993Z",
    "errorId": "fc18f326",
    "errors": [
      { "field": "username", "message": "must be between 3 and 30 characters" },
      { "field": "password", "message": "must be between 8 and 72 characters" }
    ]
  }
}
```

All possible field messages:

| `field` | `message` |
|---|---|
| `username` | `must not be blank` |
| `username` | `must be between 3 and 30 characters` |
| `username` | `may only contain letters, digits, '.', '_' and '-'` |
| `password` | `must not be blank` |
| `password` | `must be between 8 and 72 characters` |

**`400` — unknown field** (e.g. sending `"role": "ADMIN"`):

```json
{
  "success": false,
  "error": {
    "status": 400,
    "error": "Bad Request",
    "message": "The request body is missing or invalid. Please check it and try again.",
    "path": "/api/v1/auth/register",
    "timestamp": "2026-10-10T11:47:10.120Z",
    "errorId": "0b7d4e19",
    "errors": [
      { "field": "role", "message": "is not a recognized field" }
    ]
  }
}
```

An empty or non-JSON body gets the same `message` without `errors`.

**`409` — username already taken:**

```json
{
  "success": false,
  "error": {
    "status": 409,
    "error": "Conflict",
    "message": "The username 'ash' is already taken",
    "path": "/api/v1/auth/register",
    "timestamp": "2026-10-10T11:46:04.916Z",
    "errorId": "3e321912"
  }
}
```

**`415`** — missing `Content-Type: application/json` header (`"message": "The request format is not supported."`).

---

## 3. Log in

`POST /api/v1/auth/login` — public

### Request

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "demo",
  "password": "Pokemon123!"
}
```

| Field | Type | Rules |
|---|---|---|
| `username` | string | Required. Case-insensitive (`"Demo"` works). |
| `password` | string | Required. Case-sensitive, exactly as registered. |

### Response `200 OK`

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJwb2tlbW9uLWdvIiwic3ViIjoiZGVtbyIsImV4cCI6MTc5MTYzMzk2NSwiaWF0IjoxNzkxNjMwMzY1fQ.3q2x…",
    "tokenType": "Bearer",
    "expiresAt": "2026-10-10T12:46:05Z"
  }
}
```

| Field | Meaning |
|---|---|
| `accessToken` | Opaque string for the client. Send it back as-is; do not parse it to make decisions. |
| `tokenType` | Always `"Bearer"`. Build the header as `` `${tokenType} ${accessToken}` ``. |
| `expiresAt` | ISO-8601 UTC instant. After it, every protected call returns `401`. |

### Errors

**`401` — wrong username or password.** The message is identical for an unknown user and a wrong password; show it as-is, don't try to tell them apart.

```json
{
  "success": false,
  "error": {
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid username or password.",
    "path": "/api/v1/auth/login",
    "timestamp": "2026-10-10T11:46:05.170Z",
    "errorId": "c4d2db3f"
  }
}
```

**`400` — a field is missing or blank** (format rules are *not* checked on login):

```json
{
  "success": false,
  "error": {
    "status": 400,
    "error": "Bad Request",
    "message": "Please check the highlighted fields.",
    "path": "/api/v1/auth/login",
    "timestamp": "2026-10-10T11:48:31.402Z",
    "errorId": "9a51c0de",
    "errors": [
      { "field": "username", "message": "must not be blank" },
      { "field": "password", "message": "must not be blank" }
    ]
  }
}
```

Unknown fields / invalid JSON (`400`) and missing `Content-Type` (`415`) behave as in [Register](#2-register).

---

## 4. Current user

`GET /api/v1/auth/me` — **token required**

Use it to restore the session on page reload (token still in storage → check it is still accepted) and to show who is logged in.

### Request

```http
GET /api/v1/auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Response `200 OK`

```json
{
  "success": true,
  "data": {
    "username": "demo"
  }
}
```

### Errors

`401` — missing, malformed, tampered or expired token (body in [§6](#6-the-401-response-of-protected-routes)).

---

## 5. Which routes need the token

| Method | Path | Token |
|---|---|---|
| `POST` | `/api/v1/auth/register` | no |
| `POST` | `/api/v1/auth/login` | no |
| `GET` | `/api/v1/auth/me` | **yes** |
| `GET` | `/api/v1/pokemon`, `/api/v1/pokemon/{id}` | no |
| `GET` | `/api/v1/local-pokemon`, `/api/v1/local-pokemon/{id}` | no |
| `POST` | `/api/v1/local-pokemon/{id}/sync` | **yes** |
| `PUT` | `/api/v1/local-pokemon/{id}` | **yes** |
| any | any other path | **yes** (denied by default: `401` without a token, `404` with a valid one) |

UI suggestions: show the *Sync* and *Edit* actions only to logged-in users (or redirect to login when clicked); browsing works for everyone.

> **Important:** if a token is sent but is invalid or expired, the backend answers `401` **even on public routes**, because the token is checked before the route. So never keep sending a token you know is expired — clear it as soon as you get a `401` or `expiresAt` has passed, and public pages keep working anonymously.

---

## 6. The 401 response of protected routes

Returned for: no `Authorization` header, a malformed or tampered token, a token signed by another server, or an expired token. The body never says which one (details go only to the server log, matched by `errorId`).

Headers: `WWW-Authenticate: Bearer`, `Content-Type: application/json`

```json
{
  "success": false,
  "error": {
    "status": 401,
    "error": "Unauthorized",
    "message": "Authentication is required to access this resource.",
    "path": "/api/v1/local-pokemon/25",
    "timestamp": "2026-10-10T11:46:04.217Z",
    "errorId": "c472d152"
  }
}
```

How to tell the two `401`s apart:

| Where | `message` | What the UI should do |
|---|---|---|
| `POST /auth/login` | `Invalid username or password.` | Stay on the login form, show the message. |
| any other route | `Authentication is required to access this resource.` | Clear the token, redirect to login (remember where the user was, to come back after login). |

A `403` (`You do not have permission to access this resource.`) is reserved for future role checks; no endpoint returns it today. Treat it as "logged in but not allowed" — do not log the user out.

---

## 7. TypeScript

```ts
// --- Shared envelope (same as API.md) ---
export type ApiResponse<T> =
  | { success: true; data: T }
  | { success: false; error: ApiError };

export interface ApiError {
  status: number;
  error: string;
  message: string;      // user-friendly, safe to display
  path: string;
  timestamp: string;    // ISO-8601, UTC
  errorId: string;      // quote it in bug reports
  errors?: FieldError[]; // omitted when empty
}

export interface FieldError {
  field: string;   // "username" | "password" | unknown field name
  message: string;
}

// --- Auth ---
export interface RegisterRequest {
  username: string;
  password: string;
}

export type LoginRequest = RegisterRequest;

export interface UserResponse {
  username: string;
}

export interface AccessTokenResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresAt: string; // ISO-8601, UTC
}
```

### Reference client

A minimal sketch of the expected behaviour (adapt it to your HTTP client / store):

```ts
const TOKEN_KEY = 'auth';

interface StoredToken { accessToken: string; tokenType: 'Bearer'; expiresAt: string }

function storedToken(): StoredToken | null {
  const raw = sessionStorage.getItem(TOKEN_KEY);
  if (!raw) return null;
  const token = JSON.parse(raw) as StoredToken;
  if (Date.parse(token.expiresAt) <= Date.now()) {   // expired: never send it
    sessionStorage.removeItem(TOKEN_KEY);
    return null;
  }
  return token;
}

export async function api<T>(path: string, init: RequestInit = {}): Promise<ApiResponse<T>> {
  const headers = new Headers(init.headers);
  if (init.body) headers.set('Content-Type', 'application/json');
  const token = storedToken();
  if (token) headers.set('Authorization', `${token.tokenType} ${token.accessToken}`);

  const response = await fetch(`/api/v1${path}`, { ...init, headers });
  const body = (await response.json()) as ApiResponse<T>;

  if (response.status === 401 && path !== '/auth/login') {
    sessionStorage.removeItem(TOKEN_KEY);
    // e.g. router.push({ name: 'login', query: { redirect: currentRoute } })
  }
  return body;
}

export async function login(request: LoginRequest): Promise<ApiResponse<AccessTokenResponse>> {
  const result = await api<AccessTokenResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify(request),
  });
  if (result.success) sessionStorage.setItem(TOKEN_KEY, JSON.stringify(result.data));
  return result;
}

export function logout(): void {
  sessionStorage.removeItem(TOKEN_KEY);
}
```

Notes:

- **Storage:** `sessionStorage` (cleared when the tab closes) or in-memory state is preferred over `localStorage`. Never put the token in a URL or log it.
- **Form validation:** mirror the rules of [§2](#2-register) client-side for instant feedback, but always display the server's `errors[]` too — the server is the source of truth.
- **Error display:** `error.message` is always written for end users and can be shown directly.
- **Network failures / `5xx`:** keep the token; only `401` (outside login) means it is no longer valid.

---

## 8. Quick test with curl

```bash
# Register
curl -i -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"ash","password":"Pikachu123!"}'

# Log in and keep the token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"Pokemon123!"}' | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')

# Who am I?
curl -s http://localhost:8080/api/v1/auth/me -H "Authorization: Bearer $TOKEN"

# Protected write
curl -s -X PUT http://localhost:8080/api/v1/local-pokemon/25 \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"localizedName":"Pikachu","region":"Kanto","tags":["mascot"]}'
```
