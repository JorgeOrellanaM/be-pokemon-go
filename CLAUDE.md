# CLAUDE.md

Project overview and user stories: see `.claude/CLAUDE.md`.

## Hard constraints
- Keep this a **single Gradle module**. Clean Architecture layers are packages, not modules.
- Never modify `build.gradle.kts` or `settings.gradle.kts` unless the user explicitly asks.
- Never add Docker or deployment files unless the user explicitly asks.
- Work one user story at a time. Don't scaffold future stories ahead of time.

## Clean Architecture

```
infrastructure  ──►  application  ──►  domain
 (Spring, HTTP,       (ports +          (models, rules,
  DB, adapters)        use cases)        exceptions)
```

- Dependencies point **inward only**. `domain` depends on nothing; `application` depends only on `domain`.
- `domain` and `application` must not import Spring, JPA, Jackson or any HTTP types.
- Only `infrastructure` uses Spring annotations (`@Component`, `@RestController`, `@Configuration`, …).
- Use-case implementations are plain classes. They are exposed as beans via `@Bean` methods in `infrastructure/config`, never annotated themselves.
- Controllers depend on **input ports** (`*UseCase`), never on implementations.
- Use cases depend on **output ports** (`*Port`), never on adapters.

## Folder structure

```
src/main/java/com/interview/pokemon_go/
├── PokemonGoApplication.java
├── domain/
│   ├── model/        records + value objects (PokemonSummary, Ability, Weight, PageQuery, PageResult)
│   └── exception/    DomainException + subclasses
├── application/
│   ├── port/in/      input ports — *UseCase interfaces (e.g. ListPokemonUseCase)
│   ├── port/out/     output ports — *Port interfaces (e.g. PokemonCatalogPort)
│   └── usecase/      interactors — *Service implementing input ports
└── infrastructure/
    ├── web/          @RestControllers, *Request/*Response DTOs, *WebMapper, GlobalExceptionHandler
    ├── pokeapi/      PokeAPI adapter (RestClient), private PokeAPI DTOs, mapper, cache
    │                 (currently FakePokemonCatalogAdapter)
    ├── persistence/  JPA entities, Spring Data repos, persistence adapters   (later)
    ├── security/     auth / token / password adapters                       (later)
    └── config/       @Configuration: use-case bean wiring, cache, clients
```

Tests mirror this structure under `src/test/java/com/interview/pokemon_go/`.

## Naming conventions

| Kind | Pattern | Location |
|---|---|---|
| Input port | `*UseCase` | `application/port/in` |
| Output port | `*Port` | `application/port/out` |
| Interactor | `*Service` | `application/usecase` |
| Adapter | `*Adapter`; stand-ins are `Fake*` / `InMemory*` | `infrastructure/*` |
| Web DTO | `*Request` / `*Response` (records) | `infrastructure/web` |
| Mapper | `*Mapper` | in the adapter package that owns the mapping |

## Domain modeling
- Prefer Java `record`s. Validate invariants in the compact constructor and fail fast.
- Make defensive copies of collections with `List.copyOf(...)`.
- Use value objects instead of raw primitives where a unit or rule applies:
  - `Weight` is stored in hectograms (the PokeAPI unit) and exposes `kilograms()`.
  - `PageQuery` requires `page >= 0` and `1 <= size <= PageQuery.MAX_SIZE` (50).
- Paging uses the framework-agnostic `PageResult<T>`, never Spring's `Page`.
- No Lombok in `domain` or `application`.

## Error handling
- The domain and use cases throw subclasses of `DomainException` (unchecked).
- Adapters **translate** external failures into domain exceptions. A PokeAPI 5xx or timeout becomes `ExternalServiceUnavailableException`; a PokeAPI 404 becomes `*NotFoundException`.
- PokeAPI DTOs and HTTP client exceptions never leave the adapter.
- One `@RestControllerAdvice` (`GlobalExceptionHandler`) maps errors to RFC 7807 `ProblemDetail`:

| Exception | HTTP |
|---|---|
| `InvalidPageQueryException`, `DomainValidationException`, bean validation (`MethodArgumentNotValidException`, `HandlerMethodValidationException`), type mismatch | 400 (include field errors) |
| `*NotFoundException` | 404 |
| `*AlreadyExistsException`, `*AlreadySyncedException` | 409 |
| `InvalidCredentialsException` / unauthenticated | 401 |
| Access denied | 403 |
| `ExternalServiceUnavailableException` | 503 |
| Anything else | 500, generic message, logged, no stack trace in the body |

- No try/catch in controllers. Let exceptions reach the handler.

## API conventions
- Every route is prefixed with `/api/v1`.
- Use standard HTTP verbs and statuses:
  - `200` for reads and updates
  - `201` + `Location` for create
  - `204` for delete
- Paging uses the query parameters `page` (0-based) and `size`.
- Paged responses always have the same shape:
  ```json
  { "items": [...], "page": 0, "size": 20, "totalElements": 151, "totalPages": 8 }
  ```
- Request DTOs use Jakarta Validation annotations, and controllers use `@Valid`.

## Testing / TDD
- Write the test first (red → green → refactor) for each slice.

| Layer | Tooling |
|---|---|
| domain | JUnit 5 + AssertJ (pure unit tests) |
| use cases | JUnit 5 + Mockito, or hand-written fakes of the output ports |
| web | `@WebMvcTest` (status codes, `ProblemDetail` body) |
| pokeapi adapter | `MockRestServiceServer` with JSON fixtures in `src/test/resources` |

- **Known issue:** `PokemonGoApplicationTests.contextLoads` fails. The `spring-boot-starter-data-jpa-test` dependency triggers DataSource auto-config, and no datasource is configured. Leave it until persistence is added. Do not change Gradle to fix it without asking.

## Commands
```bash
./gradlew compileJava   # compile
./gradlew test          # run tests
./gradlew bootRun       # run the API (port 8080)
```

## Current status
- **Done:**
  - domain models: `PokemonSummary`, `Ability`, `Weight`, `PageQuery`, `PageResult`
  - domain exceptions
  - `ListPokemonUseCase`
  - `PokemonCatalogPort`
  - `FakePokemonCatalogAdapter`
- **Next:**
  1. `ListPokemonService` (TDD)
  2. `PokemonController` (`GET /api/v1/pokemon?page&size`)
  3. `GlobalExceptionHandler`
  4. Real PokeAPI adapter with caching
