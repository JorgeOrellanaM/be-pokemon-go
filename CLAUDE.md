# CLAUDE.md

## Hard constraints
- Keep this a **single Gradle module**. Clean Architecture layers are packages, not modules.
- Never modify `build.gradle.kts` or `settings.gradle.kts` unless the user explicitly asks.
- Never add Docker or deployment files unless the user explicitly asks.

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
- **Data access layer:** JPA entities, Spring Data repositories and `DataAccessException` stay in `infrastructure/persistence`. Persistence adapters implement `*Port` and return domain models only. Spring `Page`/`Pageable` never cross the port; use `PageResult`.
- Business rules and validation live in `domain`/`application` and stay independent of both the API and the data access layer.

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
    ├── pokeapi/      PokeAPI adapter (RestClient), private PokeAPI DTOs, mapper, cache   (later)
    ├── persistence/  JPA entities, Spring Data repos, persistence adapters
    │                 (rules: infrastructure/persistence/CLAUDE.md)
    │                 (PostgresPokemonCatalogAdapter is the active PokemonCatalogPort)
    ├── security/     auth / token / password adapters                       (later)
    └── config/       @Configuration: use-case bean wiring, cache, clients
```

Tests mirror this structure under `src/test/java/com/interview/pokemon_go/`.

## Naming conventions

| Kind | Pattern                                         | Location |
|---|-------------------------------------------------|---|
| Input port | `*UseCase`                                      | `application/port/in` |
| Output port | `*Port`                                         | `application/port/out` |
| Interactor | `*Service`                                      | `application/usecase` |
| Adapter | `*Adapter`; stand-ins are `Fake*` / `InMemory*` | `infrastructure/*` |
| Web DTO | `*RequestDTO` / `*ResponseDTO` (records)        | `infrastructure/web` |
| Mapper | `*Mapper`                                       | in the adapter package that owns the mapping |

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

- **Database:** PostgreSQL at `jdbc:postgresql://localhost:5432/pokemondb` (user/password `postgres`/`postgres`, see `application.properties`). `PokemonGoApplicationTests.contextLoads` loads the full context, so it needs that DB running. `@WebMvcTest` slices do not.

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
  - `PostgresPokemonCatalogAdapter`: entities `PokemonEntity` → `pokemon`, `AbilityEntity` → `pokemon_ability` (one-to-many), Hibernate `ddl-auto=update`, seeded by idempotent `src/main/resources/data.sql`
    - Every entity has an auto-increment (`IDENTITY`) `Long id` primary key. The Pokédex number is the unique business key `pokemon.pokedex_number`, and it is what `PokemonSummary.id` exposes, so the database id never leaves the adapter.
  - `FakePokemonCatalogAdapter`: moved to test sources as a hand-written port fake for use-case tests (not a bean)
  - **US01 API** `GET /api/v1/pokemon?page=0&size=20`: `ListPokemonService` (wired in `infrastructure/config/UseCaseConfig`), `PokemonController`, `PokemonWebMapper`, DTOs (`PageResponseDTO<T>`, `PokemonSummaryResponseDTO` with `weightKg`, `AbilityResponseDTO`)
    - Paging params are validated by `PageQuery` itself (no validation starter on the classpath)
  - `GlobalExceptionHandler` extends `ResponseEntityExceptionHandler`, so Spring MVC errors (405, missing param…) keep their status. Type mismatches add `errors: [{field, message}]`.
- **Next:**
  1. Real PokeAPI adapter with caching (US01 nice-to-have)
  2. US02 detailed view
  3. US03 sync, US04 local update
  4. `app_user` + auth (protected vs public routes)
