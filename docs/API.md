# Pokemon Go API — Frontend Contract

REST API over [PokeAPI](https://pokeapi.co/docs/v2). This file describes every endpoint, its request and its response, with JSON examples.

- **Base URL (local):** `http://localhost:8080`
- **Prefix:** every route starts with `/api/v1`
- **Content type:** `application/json` (UTF-8) for request and response bodies
- **Authentication:** JWT bearer token. Reads are public; writes (sync, update) and `/auth/me` need `Authorization: Bearer <accessToken>` from [login](#8-log-in). See [Authentication](#authentication).
- **CORS:** not configured on the backend. During development, send requests through a dev-server proxy (e.g. Vite `server.proxy` → `http://localhost:8080`).

## Endpoints at a glance

| # | Method | Path | Purpose | Auth | Success |
|---|---|---|---|---|---|
| 1 | `GET` | `/api/v1/pokemon` | Browse the PokeAPI catalog (paged) | public | `200` |
| 2 | `GET` | `/api/v1/pokemon/{id}` | Detailed view of one Pokemon, from PokeAPI | public | `200` |
| 3 | `POST` | `/api/v1/local-pokemon/{id}/sync` | Copy one Pokemon from PokeAPI into the local DB | **token** | `201` + `Location` |
| 4 | `GET` | `/api/v1/local-pokemon` | List Pokemon stored locally (paged) | public | `200` |
| 5 | `GET` | `/api/v1/local-pokemon/{id}` | One locally stored Pokemon | public | `200` |
| 6 | `PUT` | `/api/v1/local-pokemon/{id}` | Replace the custom fields of a local Pokemon | **token** | `200` |
| 7 | `POST` | `/api/v1/auth/register` | Create a user account | public | `201` + `Location` |
| 8 | `POST` | `/api/v1/auth/login` | Exchange username + password for an access token | public | `200` |
| 9 | `GET` | `/api/v1/auth/me` | The user the token belongs to | **token** | `200` |

Any route not listed is denied by default: without a token it answers `401`, with a valid token `404`.

`{id}` is always the **Pokédex number** (Bulbasaur = 1, Pikachu = 25). It must be a positive whole number.

---

## Response envelope

Every response body (success or error) has the same envelope.

**Success** — `success: true` and the payload under `data` (`error` is omitted):

```json
{
  "success": true,
  "data": { }
}
```

**Error** — `success: false` and the details under `error` (`data` is omitted):

```json
{
  "success": false,
  "error": {
    "status": 400,
    "error": "Bad Request",
    "message": "Please check the highlighted fields.",
    "path": "/api/v1/local-pokemon/25",
    "timestamp": "2026-10-09T14:32:10.123456Z",
    "errorId": "6f1c2a9e",
    "errors": [
      { "field": "region", "message": "must be at most 100 characters" }
    ]
  }
}
```

| Field | Type | Notes |
|---|---|---|
| `status` | number | HTTP status code, same as the response status |
| `error` | string | HTTP reason phrase (`"Not Found"`, `"Conflict"`…) |
| `message` | string | User-friendly text. Safe to show in the UI as-is |
| `path` | string | Path that was called, without the query string |
| `timestamp` | string | ISO-8601 instant, UTC |
| `errorId` | string | 8-character id that matches the backend log line. Show it in error UIs so it can be reported |
| `errors` | array | **Omitted when empty.** Present on validation errors: one `{ field, message }` per invalid field, to highlight form inputs |

> Check the `success` flag, or the HTTP status, to tell the two shapes apart.

---

## Shared types

### Page

The two list endpoints return a page:

| Field | Type | Notes |
|---|---|---|
| `items` | array | Items of the current page |
| `page` | number | Zero-based page index that was returned |
| `size` | number | Requested page size |
| `totalElements` | number | Total number of items across all pages |
| `totalPages` | number | `ceil(totalElements / size)` |

Paging query parameters (both optional):

| Param | Default | Rule |
|---|---|---|
| `page` | `0` | Whole number, `>= 0` (zero-based) |
| `size` | `20` | Whole number, `1`–`50` |

### Ability

| Field | Type | Notes |
|---|---|---|
| `name` | string | e.g. `"overgrow"` |
| `hidden` | boolean | `true` for the hidden ability. Regular abilities come first |

---

## 1. List Pokemon (catalog)

`GET /api/v1/pokemon?page={page}&size={size}`

Browses the full PokeAPI catalog, ordered by Pokédex number.

**Request**

```http
GET /api/v1/pokemon?page=0&size=2
```

**Response `200`**

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": 1,
        "name": "bulbasaur",
        "spriteUrl": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png",
        "category": "Seed Pokémon",
        "weightKg": 6.9,
        "abilities": [
          { "name": "overgrow", "hidden": false },
          { "name": "chlorophyll", "hidden": true }
        ]
      },
      {
        "id": 2,
        "name": "ivysaur",
        "spriteUrl": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/2.png",
        "category": "Seed Pokémon",
        "weightKg": 13.0,
        "abilities": [
          { "name": "overgrow", "hidden": false },
          { "name": "chlorophyll", "hidden": true }
        ]
      }
    ],
    "page": 0,
    "size": 2,
    "totalElements": 1302,
    "totalPages": 651
  }
}
```

**Item fields — `PokemonSummary`**

| Field | Type | Notes |
|---|---|---|
| `id` | number | Pokédex number |
| `name` | string | Lowercase PokeAPI name, e.g. `"mr-mime"`. Format it for display |
| `spriteUrl` | string \| null | Small sprite (96×96). Can be `null` for some forms |
| `category` | string \| null | English genus, e.g. `"Seed Pokémon"`. Can be `null` |
| `weightKg` | number | Kilograms (decimal) |
| `abilities` | Ability[] | Regular abilities first, then the hidden one |

**Errors:** `400` (invalid `page`/`size`), `503` (PokeAPI unreachable).

---

## 2. Pokemon details

`GET /api/v1/pokemon/{id}`

**Request**

```http
GET /api/v1/pokemon/1
```

**Response `200`**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "bulbasaur",
    "imageUrl": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
    "category": "Seed Pokémon",
    "types": ["grass", "poison"],
    "stats": [
      { "name": "hp", "value": 45 },
      { "name": "attack", "value": 49 },
      { "name": "defense", "value": 49 },
      { "name": "special-attack", "value": 65 },
      { "name": "special-defense", "value": 65 },
      { "name": "speed", "value": 45 }
    ],
    "description": "A strange seed was planted on its back at birth. The plant sprouts and grows with this POKéMON.",
    "evolutionChain": {
      "id": 1,
      "name": "bulbasaur",
      "evolvesTo": [
        {
          "id": 2,
          "name": "ivysaur",
          "evolvesTo": [
            { "id": 3, "name": "venusaur", "evolvesTo": [] }
          ]
        }
      ]
    }
  }
}
```

**Fields — `PokemonDetails`**

| Field | Type | Notes |
|---|---|---|
| `id` | number | Pokédex number |
| `name` | string | Lowercase PokeAPI name |
| `imageUrl` | string \| null | Large official artwork; falls back to the small sprite |
| `category` | string \| null | English genus |
| `types` | string[] | Primary type first |
| `stats` | `{ name: string, value: number }[]` | Base stats, `value` is `0`–`255` |
| `description` | string | English flavor text. Empty string `""` when none exists (never `null`) |
| `evolutionChain` | EvolutionStage | Root of the chain (the base form), **not** necessarily the requested Pokemon |

**`EvolutionStage`** is a **tree**, not a list: `{ id: number, name: string, evolvesTo: EvolutionStage[] }`. A stage can branch into several (e.g. Eevee → 8 evolutions). A final stage has `evolvesTo: []`.

Branching example (Eevee, shortened):

```json
{
  "id": 133,
  "name": "eevee",
  "evolvesTo": [
    { "id": 134, "name": "vaporeon", "evolvesTo": [] },
    { "id": 135, "name": "jolteon", "evolvesTo": [] },
    { "id": 136, "name": "flareon", "evolvesTo": [] }
  ]
}
```

**Errors:** `400` (non-numeric or `<= 0` id), `404` (unknown id), `503` (PokeAPI unreachable).

---

## 3. Sync a Pokemon to the local DB

`POST /api/v1/local-pokemon/{id}/sync`

Copies one Pokemon from PokeAPI into the local database, with empty custom fields. **No request body.** A Pokemon can be synced only once; syncing it again returns `409` (so custom fields are never overwritten).

**Request**

```http
POST /api/v1/local-pokemon/6/sync
```

**Response `201`**

Header: `Location: http://localhost:8080/api/v1/local-pokemon/6`

```json
{
  "success": true,
  "data": {
    "id": 6,
    "name": "charizard",
    "spriteUrl": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/6.png",
    "category": "Flame Pokémon",
    "weightKg": 90.5,
    "abilities": [
      { "name": "blaze", "hidden": false },
      { "name": "solar-power", "hidden": true }
    ],
    "localizedName": null,
    "region": null,
    "tags": []
  }
}
```

**Errors:** `400` (invalid id), `401` (missing/invalid/expired token), `404` (id does not exist in PokeAPI), `409` (already synced), `503` (PokeAPI unreachable).

`409` example:

```json
{
  "success": false,
  "error": {
    "status": 409,
    "error": "Conflict",
    "message": "Pokemon with id 6 is already synced",
    "path": "/api/v1/local-pokemon/6/sync",
    "timestamp": "2026-10-09T14:35:02.481Z",
    "errorId": "b41d07c3"
  }
}
```

---

## 4. List local Pokemon

`GET /api/v1/local-pokemon?page={page}&size={size}`

Same paging rules as the catalog, ordered by Pokédex number. Items are `LocalPokemon` (see below).

**Request**

```http
GET /api/v1/local-pokemon?page=0&size=2
```

**Response `200`**

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": 1,
        "name": "bulbasaur",
        "spriteUrl": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png",
        "category": "Seed Pokémon",
        "weightKg": 6.9,
        "abilities": [
          { "name": "overgrow", "hidden": false },
          { "name": "chlorophyll", "hidden": true }
        ],
        "localizedName": "Bulbizarre",
        "region": "Kanto",
        "tags": ["starter"]
      },
      {
        "id": 2,
        "name": "ivysaur",
        "spriteUrl": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/2.png",
        "category": "Seed Pokémon",
        "weightKg": 13.0,
        "abilities": [
          { "name": "overgrow", "hidden": false },
          { "name": "chlorophyll", "hidden": true }
        ],
        "localizedName": null,
        "region": null,
        "tags": []
      }
    ],
    "page": 0,
    "size": 2,
    "totalElements": 10,
    "totalPages": 5
  }
}
```

An empty store returns `"items": []`, `"totalElements": 0`, `"totalPages": 0`.

**Errors:** `400` (invalid `page`/`size`).

---

## 5. Get a local Pokemon

`GET /api/v1/local-pokemon/{id}`

**Request**

```http
GET /api/v1/local-pokemon/25
```

**Response `200`**

```json
{
  "success": true,
  "data": {
    "id": 25,
    "name": "pikachu",
    "spriteUrl": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png",
    "category": "Mouse Pokémon",
    "weightKg": 6.0,
    "abilities": [
      { "name": "static", "hidden": false },
      { "name": "lightning-rod", "hidden": true }
    ],
    "localizedName": "Pikachu",
    "region": "Kanto",
    "tags": ["mascot"]
  }
}
```

**Fields — `LocalPokemon`**

All the `PokemonSummary` fields (copied from PokeAPI, read-only) plus the fields this service owns:

| Field | Type | Notes |
|---|---|---|
| `localizedName` | string \| null | `null` when not set |
| `region` | string \| null | `null` when not set |
| `tags` | string[] | Empty array when there are none (never `null`) |

**Errors:** `400` (invalid id), `404` (not synced yet).

`404` example:

```json
{
  "success": false,
  "error": {
    "status": 404,
    "error": "Not Found",
    "message": "Pokemon with id 150 was not found",
    "path": "/api/v1/local-pokemon/150",
    "timestamp": "2026-10-09T14:36:44.019Z",
    "errorId": "0c9e5f12"
  }
}
```

---

## 6. Update a local Pokemon's custom fields

`PUT /api/v1/local-pokemon/{id}`

Replaces **all** custom fields (full-replace semantics). The PokeAPI fields cannot be edited.

**Request body — `UpdateLocalPokemonRequest`**

| Field | Type | Rules |
|---|---|---|
| `localizedName` | string \| null | Max 100 characters. Whitespace is trimmed; `null`, missing or `""` **clears** the value |
| `region` | string \| null | Max 100 characters. Same trimming/clearing as above |
| `tags` | string[] \| null | Max 10 tags, each max 50 characters, no empty tags, no duplicates (case-insensitive, after trimming). `null` or missing **clears** all tags |

> ⚠️ **Always send all three fields.** A missing field is not "unchanged" — it clears the stored value.
> ⚠️ **Unknown fields are rejected** with `400` (e.g. a typo like `"regoin"`). Do not send the read-only PokeAPI fields (`name`, `weightKg`…) in the body.

**Request**

```http
PUT /api/v1/local-pokemon/25
Content-Type: application/json
```

```json
{
  "localizedName": "Pikachu",
  "region": "Kanto",
  "tags": ["mascot", "electric", "favorite"]
}
```

**Response `200`** — the updated `LocalPokemon` (values as stored, i.e. trimmed):

```json
{
  "success": true,
  "data": {
    "id": 25,
    "name": "pikachu",
    "spriteUrl": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png",
    "category": "Mouse Pokémon",
    "weightKg": 6.0,
    "abilities": [
      { "name": "static", "hidden": false },
      { "name": "lightning-rod", "hidden": true }
    ],
    "localizedName": "Pikachu",
    "region": "Kanto",
    "tags": ["mascot", "electric", "favorite"]
  }
}
```

**Clearing every custom field:**

```json
{ "localizedName": null, "region": null, "tags": [] }
```

**Validation error `400`** — all invalid fields are reported at once:

Request:

```json
{
  "localizedName": "A name that is way longer than one hundred characters ... (truncated for the example)",
  "region": "Kanto",
  "tags": ["starter", "Starter", " "]
}
```

Response:

```json
{
  "success": false,
  "error": {
    "status": 400,
    "error": "Bad Request",
    "message": "Please check the highlighted fields.",
    "path": "/api/v1/local-pokemon/25",
    "timestamp": "2026-10-09T14:40:11.902Z",
    "errorId": "7a2be9d0",
    "errors": [
      { "field": "localizedName", "message": "must be at most 100 characters" },
      { "field": "tags", "message": "must not contain empty tags" },
      { "field": "tags", "message": "must not contain duplicate tags" }
    ]
  }
}
```

The same `field` can appear more than once (one entry per broken rule). Possible messages:

| Field | Message |
|---|---|
| `localizedName`, `region` | `must be at most 100 characters` |
| `tags` | `must contain at most 10 tags` |
| `tags` | `must not contain empty tags` |
| `tags` | `each tag must be at most 50 characters` |
| `tags` | `must not contain duplicate tags` |

**Unknown or wrongly typed JSON field `400`:**

```json
{ "localizedName": "Pikachu", "regoin": "Kanto", "tags": [] }
```

```json
{
  "success": false,
  "error": {
    "status": 400,
    "error": "Bad Request",
    "message": "The request body is missing or invalid. Please check it and try again.",
    "path": "/api/v1/local-pokemon/25",
    "timestamp": "2026-10-09T14:41:30.556Z",
    "errorId": "c3d8a144",
    "errors": [
      { "field": "regoin", "message": "is not a recognized field" }
    ]
  }
}
```

A value of the wrong type (e.g. `"tags": ["ok", {}]`) reports `{ "field": "tags[1]", "message": "has an invalid value" }`. A missing or malformed body returns the same message with no `errors`.

**Errors:** `400` (validation, invalid id, invalid/unknown JSON), `401` (missing/invalid/expired token), `404` (not synced yet), `415` (missing `Content-Type: application/json`).

---

## Authentication

> Frontend implementation guide (flow, every error case, TypeScript client): **[AUTH.md](AUTH.md)**.

Stateless JWT bearer tokens (HS256, valid for **1 hour**). There is no refresh token and no logout endpoint: the client forgets the token, and after `expiresAt` it must log in again. Send it on protected routes as:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

A protected route called without a token, or with a malformed, tampered or expired one, answers `401` with header `WWW-Authenticate: Bearer`:

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

### 7. Register

`POST /api/v1/auth/register`

| Field | Rules |
|---|---|
| `username` | required, 3–30 characters, letters, digits, `.`, `_`, `-`. Case-insensitive: stored in lowercase (`" Ash "` → `"ash"`). |
| `password` | required, 8–72 characters (72 UTF-8 bytes). Kept exactly as typed. |

```http
POST /api/v1/auth/register
Content-Type: application/json

{ "username": "ash", "password": "Pikachu123!" }
```

**Response `201`** — header `Location: http://localhost:8080/api/v1/auth/me`. Registering does **not** log in; call login next.

```json
{ "success": true, "data": { "username": "ash" } }
```

**Errors:** `400` (one entry per invalid field in `errors`, unknown JSON fields), `409` (`The username 'ash' is already taken`).

### 8. Log in

`POST /api/v1/auth/login`

```http
POST /api/v1/auth/login
Content-Type: application/json

{ "username": "demo", "password": "Pokemon123!" }
```

**Response `200`**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresAt": "2026-10-10T12:46:05Z"
  }
}
```

**Errors:** `400` (`username` or `password` missing/blank), `401` with `Invalid username or password.` (the same message for an unknown user and a wrong password).

### 9. Current user

`GET /api/v1/auth/me` (token required)

**Response `200`**

```json
{ "success": true, "data": { "username": "demo" } }
```

**Errors:** `401` (missing/invalid/expired token).

---

## Error reference

| Status | When | `message` (examples) | `errors` |
|---|---|---|---|
| `400` | Invalid `page`/`size` | `size must be between 1 and 50` · `page must be greater than or equal to 0` | — |
| `400` | Non-numeric query/path value (`?page=abc`, `/pokemon/abc`) | `Please check the 'page' parameter.` | `[{ "field": "page", "message": "must be a whole number" }]` |
| `400` | Id `<= 0` | `id must be a positive whole number` | — |
| `400` | Body validation (PUT, register, login) | `Please check the highlighted fields.` | one per invalid field |
| `400` | Missing/malformed JSON, unknown field | `The request body is missing or invalid. Please check it and try again.` | the offending field, when known |
| `400` | Malformed query string (e.g. `?page=%`) | `The request contains invalid characters. Please check it and try again.` | — |
| `401` | Protected route without a valid token | `Authentication is required to access this resource.` | — |
| `401` | Login with wrong username or password | `Invalid username or password.` | — |
| `403` | Authenticated but not allowed (reserved for future role checks) | `You do not have permission to access this resource.` | — |
| `404` | Pokemon not found / not synced | `Pokemon with id 150 was not found` | — |
| `404` | Unknown route (with a valid token; without one it is `401`) | `The requested resource was not found.` | — |
| `405` | Wrong HTTP method | `This operation is not supported for this resource.` | — |
| `409` | Sync of an already synced Pokemon | `Pokemon with id 6 is already synced` | — |
| `409` | Register with a taken username | `The username 'ash' is already taken` | — |
| `415` | Wrong/missing `Content-Type` on PUT/POST with a body | `The request format is not supported.` | — |
| `503` | PokeAPI is down or too slow (endpoints 1–3) | `The service is temporarily unavailable. Please try again later.` | — |
| `500` | Unexpected server fault | `An unexpected error occurred. Please try again later.` | — |

Every `message` is written for end users and never contains technical details, so the UI can display it directly. For `503`/`500`, offering a "retry" action is appropriate.

---

## Seeded demo data

On startup, the local DB holds these Pokemon (endpoints 4–6 work immediately; syncing them returns `409`):

| id | name | localizedName | region | tags |
|---|---|---|---|---|
| 1 | bulbasaur | Bulbizarre | Kanto | `starter` |
| 2 | ivysaur | — | — | — |
| 3 | venusaur | — | — | — |
| 4 | charmander | Salamèche | Kanto | `starter` |
| 5 | charmeleon | — | — | — |
| 6 | charizard | — | — | — |
| 7 | squirtle | Carapuce | Kanto | `starter` |
| 8 | wartortle | — | — | — |
| 9 | blastoise | — | — | — |
| 25 | pikachu | Pikachu | Kanto | `mascot` |

Ids such as `10`, `133` or `150` are good candidates to try the sync flow.

**Demo credentials:** username `demo`, password `Pokemon123!` (log in with endpoint 8 to call the protected routes).

---

## TypeScript types

Ready to copy into the frontend:

```ts
export type ApiResponse<T> =
  | { success: true; data: T }
  | { success: false; error: ApiError };

export interface ApiError {
  status: number;
  error: string;
  message: string;
  path: string;
  timestamp: string; // ISO-8601, UTC
  errorId: string;
  errors?: FieldError[]; // omitted when empty
}

export interface FieldError {
  field: string;   // e.g. "region", "tags", "tags[1]", "page"
  message: string;
}

export interface AccessTokenResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresAt: string; // ISO-8601, UTC
}

export interface UserResponse {
  username: string;
}

export interface Page<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Ability {
  name: string;
  hidden: boolean;
}

export interface PokemonSummary {
  id: number;
  name: string;
  spriteUrl: string | null;
  category: string | null;
  weightKg: number;
  abilities: Ability[];
}

export interface BaseStat {
  name: string;  // "hp" | "attack" | "defense" | "special-attack" | "special-defense" | "speed"
  value: number; // 0–255
}

export interface EvolutionStage {
  id: number;
  name: string;
  evolvesTo: EvolutionStage[];
}

export interface PokemonDetails {
  id: number;
  name: string;
  imageUrl: string | null;
  category: string | null;
  types: string[];
  stats: BaseStat[];
  description: string; // "" when none
  evolutionChain: EvolutionStage;
}

export interface LocalPokemon extends PokemonSummary {
  localizedName: string | null;
  region: string | null;
  tags: string[];
}

export interface UpdateLocalPokemonRequest {
  localizedName: string | null; // max 100 chars
  region: string | null;        // max 100 chars
  tags: string[];               // max 10, each max 50 chars, unique (case-insensitive)
}
```
