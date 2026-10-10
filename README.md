# Pokemon Go API

REST API built with **Java 25** and **Spring Boot 4.1.1**, using Clean Architecture and TDD. It reads Pokemon from [PokeAPI](https://pokeapi.co/docs/v2), copies chosen Pokemon into a local **PostgreSQL** database, lets you add your own fields to those copies and edit them, and protects all write operations with **JWT** authentication.

> **Frontend:** the Vue/Quasar client lives in a separate project, `fe-pokemon-go`. The backend does not configure CORS, so in development the frontend sends `/api` requests through its dev-server proxy to `http://localhost:8080`.

---

## Contents

1. [User stories](#user-stories)
2. [Tech stack](#tech-stack)
3. [Getting started](#getting-started)
4. [Configuration](#configuration)
5. [Running with Docker](#running-with-docker)
6. [Demo data and credentials](#demo-data-and-credentials)
7. [Quick tour with curl](#quick-tour-with-curl)
8. [API overview](#api-overview)
9. [Architecture](#architecture)
10. [Data model](#data-model)
11. [Testing](#testing)
12. [GenAI: Claude Code memories](#genai-claude-code-memories)
13. [Project structure](#project-structure)

---

## User stories

| Story | What it does | Endpoint(s) |
|---|---|---|
| **US01: Pokemon list** | Shows Pokemon in pages, each with its sprite, category, weight and abilities | `GET /api/v1/pokemon?page&size` |
| **US02: Detailed view** | Shows one Pokemon's image, types, base stats, description and evolution chain (as a tree) | `GET /api/v1/pokemon/{id}` |
| **US03: Data sync** | Copies a Pokemon from PokeAPI into the local DB, along with fields this service owns (`localizedName`, `region`, `tags`) | `POST /api/v1/local-pokemon/{id}/sync`, `GET /api/v1/local-pokemon`, `GET /api/v1/local-pokemon/{id}` |
| **US04: Local edit** | Replaces the custom fields of a stored Pokemon. Returns `404` if the Pokemon is not stored and `400` for an invalid body, listing every invalid field | `PUT /api/v1/local-pokemon/{id}` |
| **Auth** | Lets users register and log in, and returns the current user. Reads are public; writes need a token | `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `GET /api/v1/auth/me` |

> **Caching (nice to have):** this is not implemented yet. To keep a catalog page fast without a cache, the PokeAPI adapter makes its per-Pokemon calls in parallel (`PokeApiPokemonCatalogAdapter`). Because the API depends only on the `PokemonCatalogPort` interface, a cache could be added later as an extra adapter without changing any use case.

---

## Tech stack

| Concern | Choice |
|---|---|
| Language / build | Java 25, Gradle (Kotlin DSL), single module |
| Framework | Spring Boot 4.1.1: Web MVC, Data JPA, Actuator |
| Security | Spring Security OAuth2 Resource Server: stateless JWT (HS256), BCrypt password hashes |
| Database | PostgreSQL (schema created by Hibernate `ddl-auto=update`, demo data from `data.sql`) |
| External API | PokeAPI v2 through Spring `RestClient` (connect/read timeouts) |
| Tests | JUnit 5, AssertJ, Mockito, `@WebMvcTest`, `@DataJpaTest`, `MockRestServiceServer` |

---

## Getting started

### Prerequisites

- **JDK 25** (the Gradle wrapper does the rest, so you don't need to install Gradle)
- **PostgreSQL** running on `localhost:5432`, either installed locally or started with Docker (see below)
- Internet access to reach `https://pokeapi.co` (used by US01–US03)

### 1. Create the database

```bash
# Using psql (user/password postgres/postgres)
psql -U postgres -c "CREATE DATABASE pokemondb;"

# ...or start a throwaway PostgreSQL with Docker
docker run -d --name pokemon-db -p 5432:5432 \
  -e POSTGRES_DB=pokemondb -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:17
```

You don't need to create any tables. Hibernate builds the schema at startup, and then `data.sql` loads the demo data. The script is idempotent, so it can safely run again on every start.

### 2. Run the API

```bash
./gradlew bootRun                                          # default profile
./gradlew bootRun --args='--spring.profiles.active=dev'    # dev: SQL logging + DEBUG logs, DB from env vars
```

On Windows, use `gradlew.bat` in place of `./gradlew`.

The API listens on **http://localhost:8080**. Check that it is up:

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

### Useful commands

```bash
./gradlew compileJava   # compile
./gradlew test          # run the test suite (see Testing for DB requirements)
./gradlew bootRun       # run the API on port 8080
./gradlew bootJar       # build build/libs/pokemon-go-0.0.1-SNAPSHOT.jar
```

---

## Configuration

All settings are in `src/main/resources/application*.properties`. Use environment variables to override them.

| Property | Env var | Default | Notes |
|---|---|---|---|
| `spring.datasource.url` | `DB_URL` (dev/qa profiles) | `jdbc:postgresql://localhost:5432/pokemondb` | `qa` defaults to host `db` (the Compose service) |
| `spring.datasource.username` | `DB_USERNAME` (dev/qa) | `postgres` | |
| `spring.datasource.password` | `DB_PASSWORD` (dev/qa) | `postgres` | |
| `jwt.secret` | `JWT_SECRET` | local development value | **Set your own value outside local development** (HS256, at least 32 bytes) |
| `jwt.ttl` | | `1h` | Token lifetime. There is no refresh token |
| `jwt.issuer` | | `pokemon-go` | |
| `pokeapi.base-url` | | `https://pokeapi.co/api/v2` | |
| `pokeapi.connect-timeout` / `pokeapi.read-timeout` | | `3s` / `5s` | When PokeAPI is slow or down, the API returns `503` |
| `spring.jackson.deserialization.fail-on-unknown-properties` | | `true` | Unknown JSON fields are rejected with `400` |

**Profiles**

| Profile | Use | Differences |
|---|---|---|
| *(default)* | Local run | Values from `application.properties` |
| `dev` | Local development | SQL logging, `DEBUG` logs for the app, DB taken from env vars, exposes `health` and `info` |
| `qa` | Docker image | DB host `db`, `INFO` logs, exposes `health` only |

---

## Running with Docker

The Docker files are in [`docker/`](docker/), and [`docker/docker-commands.txt`](docker/docker-commands.txt) has the full list of commands. **Run every command from the project root.**

The image is a multi-stage build: a Temurin 25 JDK builds the jar, and a Temurin 25 JRE runs it as a non-root user with the `qa` profile. Tests are skipped inside the image build because `contextLoads` needs a running PostgreSQL, so run `./gradlew test` before you build.

### Docker Compose (API + PostgreSQL)

```bash
docker compose -f docker/docker-compose.yml up --build      # start (Ctrl+C to stop)
docker compose -f docker/docker-compose.yml up --build -d   # start in the background
docker compose -f docker/docker-compose.yml logs -f api     # follow API logs
docker compose -f docker/docker-compose.yml down            # stop (DB data kept in the pgdata volume)
docker compose -f docker/docker-compose.yml down -v         # stop and delete the data (re-seeds on next start)
```

Postgres is published on port `5432`. Stop any local PostgreSQL first.

### Plain Docker

```bash
docker build -f docker/Dockerfile -t pokemon-go-api .
docker network create pokemon-net

docker run -d --name pokemon-db --network pokemon-net -p 5432:5432 \
  -e POSTGRES_DB=pokemondb -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
  -v pokemon-pgdata:/var/lib/postgresql/data postgres:17

docker run -d --name pokemon-api --network pokemon-net -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=qa -e DB_URL=jdbc:postgresql://pokemon-db:5432/pokemondb \
  -e DB_USERNAME=postgres -e DB_PASSWORD=postgres pokemon-go-api
```

To use the PostgreSQL installed on your machine, point the API container at `host.docker.internal` (inside a container, `localhost` means the container itself):

```bash
docker run -d --name pokemon-api -p 8080:8080 -e SPRING_PROFILES_ACTIVE=qa \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/pokemondb \
  -e DB_USERNAME=postgres -e DB_PASSWORD=postgres pokemon-go-api
```

Clean up with `docker rm -f pokemon-api pokemon-db && docker network rm pokemon-net && docker volume rm pokemon-pgdata`.

---

## Demo data and credentials

`src/main/resources/data.sql` loads this data on every startup and never overwrites values you have edited through the API.

**Demo account:** username **`demo`**, password **`Pokemon123!`**

**Pokemon already in the local DB:**

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

The local endpoints work right away. Syncing one of these ids again returns `409`. To try the sync flow, use ids that aren't stored yet, such as `10`, `133` or `150`.

---

## Quick tour with curl

```bash
# US01: browse the PokeAPI catalog
curl "http://localhost:8080/api/v1/pokemon?page=0&size=5"

# US02: detailed view (stats, description, evolution chain)
curl http://localhost:8080/api/v1/pokemon/133

# Log in with the demo account and keep the token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"Pokemon123!"}' | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')

# US03: sync a Pokemon into the local DB (201 + Location), then list the local copies
curl -i -X POST http://localhost:8080/api/v1/local-pokemon/133/sync -H "Authorization: Bearer $TOKEN"
curl "http://localhost:8080/api/v1/local-pokemon?page=0&size=20"

# US04: replace the custom fields (full-replace semantics: always send all three)
curl -X PUT http://localhost:8080/api/v1/local-pokemon/133 \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"localizedName":"Évoli","region":"Kanto","tags":["eeveelution","favorite"]}'

# Validation error: 400 with one entry per broken rule
curl -X PUT http://localhost:8080/api/v1/local-pokemon/133 \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"localizedName":null,"region":null,"tags":["a","A"," "]}'

# Not stored locally: 404 / no token: 401
curl http://localhost:8080/api/v1/local-pokemon/150
curl -X PUT http://localhost:8080/api/v1/local-pokemon/25 -H "Content-Type: application/json" -d '{}'

# Register a new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" -d '{"username":"ash","password":"Pikachu123!"}'
```

---

## API overview

Every route starts with `/api/v1`, and `{id}` is always the **Pokédex number** (a positive whole number). For the full contract, including every field, validation rule and error example, see:

- **[docs/API.md](docs/API.md)**: endpoints, request and response types, error reference, TypeScript types
- **[docs/AUTH.md](docs/AUTH.md)**: registration, login, token handling and the expected frontend behavior

| Method | Path | Purpose | Auth | Success |
|---|---|---|---|---|
| `GET` | `/pokemon` | Browse the PokeAPI catalog (paged) | public | `200` |
| `GET` | `/pokemon/{id}` | Detailed view from PokeAPI | public | `200` |
| `POST` | `/local-pokemon/{id}/sync` | Copy one Pokemon into the local DB | **token** | `201` + `Location` |
| `GET` | `/local-pokemon` | List local Pokemon (paged) | public | `200` |
| `GET` | `/local-pokemon/{id}` | One local Pokemon | public | `200` |
| `PUT` | `/local-pokemon/{id}` | Replace the custom fields | **token** | `200` |
| `POST` | `/auth/register` | Create an account | public | `201` + `Location` |
| `POST` | `/auth/login` | Get an access token | public | `200` |
| `GET` | `/auth/me` | The current user | **token** | `200` |

**Paging:** `page` starts at `0` (default `0`) and `size` must be between `1` and `50` (default `20`).

**Route policy** (`infrastructure/config/SecurityConfig`): register, login, every `GET` under `/pokemon` and `/local-pokemon`, and `/actuator/health` are public. Writes and `/auth/me` need `Authorization: Bearer <token>`. **Any other route is denied by default**, so a new endpoint stays protected until it is deliberately made public. The API is stateless: no session and no CSRF.

### Response envelope

Every response body has the same shape:

```json
{ "success": true, "data": { } }
```

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
    "errors": [ { "field": "tags", "message": "must not contain duplicate tags" } ]
  }
}
```

- Error messages are written for end users. Stack traces, class names, SQL and framework messages never reach the client.
- `errorId` is also written to the backend log (4xx at `WARN` without a stack trace, 5xx at `ERROR` with the full stack trace), so a user report can be matched to the log line.

### Status mapping

| Situation | Status |
|---|---|
| Invalid paging, invalid id, body validation, malformed/unknown JSON, wrong type, malformed query string | `400` (with field `errors` when known) |
| Missing, invalid or expired token, wrong credentials | `401` |
| Authenticated but not allowed (reserved for future role checks) | `403` |
| Pokemon not found in PokeAPI / not stored locally, unknown route | `404` |
| Wrong HTTP method | `405` |
| Pokemon already synced, username taken | `409` |
| Missing `Content-Type: application/json` | `415` |
| PokeAPI unreachable or too slow | `503` |
| Unexpected fault on our side | `500` (generic message) |

---

## Architecture

The API follows **Clean Architecture** (ports and adapters) inside a **single Gradle module**. The layers are packages, and dependencies only point inward:

```
infrastructure  ──►  application  ──►  domain
 (Spring, HTTP,       (ports +          (models, rules,
  DB, adapters)        use cases)        exceptions)
```

```
src/main/java/com/interview/pokemon_go/
├── domain/
│   ├── model/        records + value objects (PokemonSummary, PokemonDetails, LocalPokemon,
│   │                 PokemonCustomization, Weight, PageQuery, PageResult, Credentials, ...)
│   └── exception/    DomainException + subclasses, each declaring an ErrorCategory
├── application/
│   ├── port/in/      input ports: *UseCase (ListPokemonUseCase, SyncPokemonUseCase, ...)
│   ├── port/out/     output ports: *Port (PokemonCatalogPort, LocalPokemonPort, UserAccountPort, ...)
│   └── usecase/      interactors: *Service, plain Java classes
└── infrastructure/
    ├── web/          controllers, request/response DTOs, mappers, error handling
    ├── pokeapi/      PokeAPI client + adapters (PokemonCatalogPort, PokemonDetailsPort)
    ├── persistence/  JPA entities, Spring Data repositories, Postgres adapters
    ├── security/     BCrypt password hasher, JWT token issuer
    └── config/       use-case bean wiring, security, PokeAPI client, properties
```

### Key design decisions

- **Framework-free core.** `domain` and `application` import no Spring, JPA, Jackson or HTTP types. Use-case classes have no annotations; they are registered as beans with `@Bean` methods in `infrastructure/config/UseCaseConfig`.
- **Controllers depend on input ports, and use cases depend on output ports.** Neither ever depends on an implementation.
- **External integration through DIP.** PokeAPI sits behind `PokemonCatalogPort` / `PokemonDetailsPort`. Inside `infrastructure/pokeapi`, the adapters use a package-private `PokeApiClient` interface (implemented by `PokeApiHttpClient`), and the PokeAPI DTOs never leave that package. To replace PokeAPI or add a cache, you only touch infrastructure.
- **Business rules live in the domain.** Records check every invariant in their compact constructors and fail fast. Value objects replace raw primitives: `Weight` converts PokeAPI hectograms to kg, `PageQuery` enforces size 1–50, and `PokemonCustomization` holds the US04 rules (trimming, max lengths, at most 10 unique tags). Validation errors are collected into a `DomainValidationException` with one `FieldViolation` per broken rule.
- **The data access layer stays isolated.** Entities, repositories and `DataAccessException` remain in `infrastructure/persistence`. Adapters return domain models only, and Spring `Page`/`Pageable` never cross a port (the domain uses `PageResult` instead). The database `id` never leaves persistence; the domain identifies Pokemon by their Pokédex number.
- **Sync is create-only.** Syncing an already stored Pokemon returns `409`, so custom fields are never silently overwritten. `PUT` replaces all the custom fields, and unknown JSON fields are rejected so that a typo can't clear a value.
- **Error handling in one place.** `GlobalExceptionHandler` (controller errors), `ApiErrorController` (replaces Boot's `/error`) and `ApiSecurityErrorHandler` (401/403) all build their responses through `ErrorResponses`. Any error caused by the client's request is a 4xx, even when Tomcat or Spring throws it; `500` is reserved for faults on our side.
- **Authentication.** Passwords are hashed with BCrypt. Login returns a 1-hour HS256 JWT, which Spring's resource server validates on each request. Usernames are case-insensitive and stored in lowercase. Login gives the same `401` message for an unknown user and a wrong password.

---

## Data model

PostgreSQL tables, created by Hibernate from the JPA entities:

| Table | Purpose | Keys |
|---|---|---|
| `pokemon` | Local copy (`pokedex_number`, `name`, `sprite_url`, `category`, `weight_hectograms`) plus the fields this service owns (`localized_name`, `region`) | `id` auto-increment PK, `pokedex_number` unique |
| `pokemon_ability` | Abilities of a stored Pokemon (`name`, `hidden`) | FK `pokemon_id`, unique (`pokemon_id`, `name`) |
| `pokemon_tag` | Custom tags | FK `pokemon_id`, unique (`pokemon_id`, `name`) |
| `app_user` | User accounts (`username`, `password_hash`, `created_at`) | `id` auto-increment PK, `username` unique |

Every table has an auto-increment primary key and at least two descriptive columns. Business keys (`pokedex_number`, `username`) have unique constraints and are never used as the primary key.

---

## Testing

```bash
./gradlew test
```

The project was developed **test-first** (red → green → refactor), one slice at a time. Tests mirror the main package structure under `src/test/java/com/interview/pokemon_go/`.

| Layer | How it is tested |
|---|---|
| domain | Plain JUnit 5 + AssertJ unit tests for every record and value object (invariants, normalization, conversions) |
| use cases | JUnit 5 with hand-written fakes of the output ports (`FakePokemonCatalogAdapter`, `InMemoryLocalPokemonAdapter`, `InMemoryUserAccountAdapter`, `FakePasswordHasher`, ...) |
| port contracts | Abstract contract tests (`*PortContractTest`) run against **both** the fakes and the real adapters, so the fakes behave like production |
| web | `@WebMvcTest` for status codes, the `ApiResponseDTO` envelope, no leaked technical details, and log levels (`OutputCaptureExtension`). Security rules come from `@ImportApiSecurity`, and protected calls use `.with(jwt())` |
| PokeAPI HTTP client | `MockRestServiceServer` with JSON fixtures in `src/test/resources/pokeapi` |
| PokeAPI adapters | Hand-written `FakePokeApiClient` (no HTTP mocking) |
| persistence | Spring Data / Postgres adapter tests against the database |
| security | BCrypt hasher and JWT issuer adapters, plus the route policy (`SecurityRulesTest`) |

> `PokemonGoApplicationTests.contextLoads` and the Postgres adapter tests need the PostgreSQL database from [Getting started](#1-create-the-database) to be running. The `@WebMvcTest` slices and the domain and use-case tests don't need a database.

---

## GenAI: Claude Code memories

This project was built with **Claude Code**: plan mode was used to design each user story, and every piece of generated code was reviewed before it was accepted. To keep the AI consistent from session to session, the architecture, coding standards and error-handling policy are stored as **Claude memories**, which are `CLAUDE.md` files that are checked into the repo and loaded automatically into every session. The rules were written step by step: after a correction was reviewed, it was turned into a rule so the same mistake would not come back.

| Memory | Scope | What it defines |
|---|---|---|
| [`.claude/CLAUDE.md`](.claude/CLAUDE.md) | Project context | Goal (REST API over PokeAPI), stack (Spring Boot 4.1.1, Java 25, Gradle Kotlin DSL), base package, deliverables and evaluation criteria |
| [`CLAUDE.md`](CLAUDE.md) | Whole backend | Hard constraints, Clean Architecture, external integrations (DIP), folder structure, naming conventions, domain modeling, coding rules, code smells, error handling, API conventions, testing/TDD and commands |
| [`infrastructure/persistence/CLAUDE.md`](src/main/java/com/interview/pokemon_go/infrastructure/persistence/CLAUDE.md) | Persistence package | Primary keys and attributes rules for JPA entities |

### Main rules they enforce

**Hard constraints**
- Single Gradle module; the Clean Architecture layers are packages.
- No dependency changes and no Docker/deployment files unless the user explicitly asks.

**Architecture**
- Dependencies point inward only. `domain`/`application` never import Spring, JPA, Jackson or HTTP types.
- Only `infrastructure` uses Spring annotations. Use cases are plain classes wired with `@Bean` methods in `infrastructure/config`.
- Controllers depend on `*UseCase` input ports, and use cases depend on `*Port` output ports.
- External services follow the Dependency Inversion Principle, behind a package-private `*Client` with a `*HttpClient` implementation.

**Domain and code quality**
- Prefer records that validate every component in the compact constructor, make defensive `List.copyOf` copies, and use value objects instead of primitives. No Lombok in `domain`/`application`.
- `Optional` for values that may be absent (never `null`, never as a field or parameter). Fail fast with `Objects.requireNonNull`. Constructor injection only.
- Javadoc explains *why* for business rules, unit conversions and error-translation decisions.
- A code-smell table: long parameter lists, duplication, magic strings, primitive obsession, encapsulation leaks, dead code, speculative generality, deep nesting, boolean flag parameters.

**Error handling and API**
- Never expose technical details. All errors use the `ApiResponseDTO`/`ErrorResponseDTO` shape built by `ErrorResponses`, with an `errorId` that matches the log.
- Errors are classified by who caused them (client → 4xx, our side → 5xx). When a client-caused error reaches the catch-all, add a handler, a `@WebMvcTest` and a row in the status table, and verify it end to end with `curl`.
- `/api/v1` prefix, standard verbs and statuses (`200`/`201` + `Location`/`204`), one response envelope, deny-by-default security.

**Persistence**
- Every entity has a `Long id` auto-increment primary key and at least two non-null descriptive columns.
- Business keys (`pokedex_number`, `username`) get unique constraints and are never the primary key.
- The database `id` never leaves the persistence package.

**Testing**
- Write the test first. The memory also lists which tooling to use for each layer (see [Testing](#testing)).

---

## Project structure

```
be-pokemon-go/
├── src/main/java/com/interview/pokemon_go/   application code (see Architecture)
├── src/main/resources/
│   ├── application.properties                 base configuration
│   ├── application-dev.properties             dev profile
│   ├── application-qa.properties              qa profile (Docker)
│   └── data.sql                               demo data + demo user
├── src/test/java/...                          tests, mirroring the main packages
├── src/test/resources/pokeapi/                PokeAPI JSON fixtures
├── docs/
│   ├── API.md                                 full API contract
│   └── AUTH.md                                authentication guide for the frontend
├── docker/
│   ├── Dockerfile                             multi-stage image (JDK build → JRE runtime)
│   ├── docker-compose.yml                     API + PostgreSQL
│   └── docker-commands.txt                    Docker cheat sheet
├── CLAUDE.md, .claude/CLAUDE.md               Claude Code memories
└── build.gradle.kts, settings.gradle.kts      Gradle build
```
