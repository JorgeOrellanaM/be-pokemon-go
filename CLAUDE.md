# CLAUDE.md

## Hard constraints
- Keep this a **single Gradle module**. Clean Architecture layers are packages, not modules.
- Do not add, modify, upgrade, or remove any project dependencies.
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

## External integrations (DIP)

Design an External integration to minimize coupling. Integrations with external services must follow the Dependency Inversion Principle (DIP).


## Folder structure

```
src/main/java/com/interview/pokemon_go/
├── PokemonGoApplication.java
├── domain/
│   ├── model/        records + value objects (PokemonSummary, Ability, Weight, PageQuery, PageResult)
│   └── exception/    DomainException + subclasses, ErrorCategory (each exception declares one)
├── application/
│   ├── port/in/      input ports — *UseCase interfaces (e.g. ListPokemonUseCase)
│   ├── port/out/     output ports — *Port interfaces (e.g. PokemonCatalogPort)
│   └── usecase/      interactors — *Service implementing input ports
└── infrastructure/
    ├── web/          @RestControllers, *Request/*Response DTOs, *WebMapper, GlobalExceptionHandler
    ├── pokeapi/      PokeAPI adapter (RestClient), private PokeAPI DTOs, mapper, cache   (later)
    ├── persistence/  JPA entities, Spring Data repos, persistence adapters
    │                 (rules: infrastructure/persistence/CLAUDE.md)
    │                 (tables kept for the US03 sync; the active PokemonCatalogPort and
    │                  PokemonDetailsPort are the PokeAPI adapters in infrastructure/pokeapi)
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
| Remote client | `*Client` (package-private interface)          | `infrastructure/<service>` |
| Remote client implementation | `*HttpClient`                    | `infrastructure/<service>` |
| Integration settings | `*Properties` (`@ConfigurationProperties` record) | `infrastructure/config` |

## Domain modeling
- Prefer Java `record`s. Validate invariants in the compact constructor and fail fast.
- Make defensive copies of collections with `List.copyOf(...)`.
- Use value objects instead of raw primitives where a unit or rule applies.
- No Lombok in `domain` or `application`.

## Coding rules
- **Optional for absent values:**
  - Methods whose result may be absent (lookups such as `findBy*`, port queries for a single item) return `Optional<T>`, never `null`.
  - Wrap nullable values from third-party APIs with `Optional.ofNullable(...).map(...).orElse(...)` instead of ternary null checks.
  - Don't use `Optional` for fields, record components, method parameters or collections. Return an empty collection instead.
  - Required arguments are checked with `Objects.requireNonNull` (fail fast).
- **Constructor injection only:** Spring beans get their dependencies through a single constructor into `final` fields. No `@Autowired` on fields or setters in main code. Use-case beans receive their ports as `@Bean` method parameters in `infrastructure/config`. Test classes may use `@Autowired` fields.
- **Comment non-obvious business logic:** add Javadoc to methods that encode a business rule, a unit conversion, a transaction/fetch constraint or an error-translation decision. Explain *why*, not *what*. Don't comment trivial getters, delegations or obvious mappings, and don't add class comments that only restate the class name.

## Code smells
**Code smells to avoid**

| Smell | Rule |
|---|---|
| Long parameter list / data clump | More than 4 parameters, or the same values passed together repeatedly → introduce a parameter object (e.g. `ApiError`) or pass the owning object (e.g. `HttpServletRequest`) |
| Duplicated code | Extract once a second copy appears |
| Magic strings and numbers | User-facing messages and limits live in named constants, in one place per concern (`ErrorMessages`, `PageQuery.MAX_SIZE`) |
| Primitive obsession | Use value objects for units and rules (`Weight`, `PageQuery`) |
| Encapsulation leak | Getters never return mutable internal collections (`PokemonEntity.getAbilities()` is read-only); mutate through intention-revealing methods |
| Incomplete invariants | Records validate every component in the compact constructor; no defensive branches for states that should be impossible |
| Dead code / stale docs | Delete unused classes and endpoints; fix comments that no longer match the code |
| Speculative generality | Don't add an abstraction before the second real use |
| Deep nesting / long methods | Guard clauses and early returns; methods over ~20 lines are a signal to extract |
| Boolean flag parameters | Prefer two well-named methods or an enum |

## Error handling
- **Never expose technical details to the client:** no stack traces, exception class names, Java types, SQL, framework messages or query strings. They go only to the backend log.
- Domain exception messages for 4xx must be user-friendly, because they are returned as is (e.g. "size must be between 1 and 50").
- Every error body is an `ApiResponseDTO` with `success: false` and an `ErrorResponseDTO` under `error`. It is built only by `infrastructure/web/ErrorResponses` (`errors` is omitted when empty, and `data` is omitted on errors):
  ```json
  { "success": false,
    "error": { "status": 400, "error": "Bad Request", "message": "Please check the 'page' parameter.",
               "path": "/api/v1/pokemon", "timestamp": "...", "errorId": "6f1c2a9e",
               "errors": [ { "field": "page", "message": "must be a whole number" } ] } }
  ```
- **Logging** (in `ErrorResponses`): 4xx → one WARN line without a stack trace; 5xx → ERROR with the full stack trace. Both log the same `errorId` that is returned to the client: `errorId=… status=… method=… path=… reason=…`.
- `GlobalExceptionHandler` (extends `ResponseEntityExceptionHandler`) handles controller errors. All `DomainException`s go through one handler that maps `category()` → status. 5xx domain errors get a generic message. Spring MVC errors (404, 405, 415…) get a friendly per-status message from `ErrorMessages.forStatus`. All user-facing text lives in `ErrorMessages`. `ApiErrorController` replaces Boot's `/error`, so errors outside controllers (filters, security, container) use the same shape.
- Status mapping:

| Exception | HTTP |
|---|---|
| `InvalidPageQueryException`, `DomainValidationException`, bean validation (`MethodArgumentNotValidException`, `HandlerMethodValidationException`), type mismatch, malformed query string (Tomcat `InvalidParameterException`) | 400 (include field errors) |
| `*NotFoundException` | 404 |
| `*AlreadyExistsException`, `*AlreadySyncedException` | 409 |
| `InvalidCredentialsException` / unauthenticated | 401 |
| Access denied | 403 |
| `ExternalServiceUnavailableException` | 503 |
| Anything else | 500, generic message, logged, no stack trace in the body |

- **Classify errors by who caused them:**
  - Anything triggered by the client's request (malformed input, bad encoding, unknown route, wrong method/media type) is a **4xx**, even when the exception comes from Tomcat, Spring or a library.
  - **500** is reserved for faults on our side.
  - The catch-all `@ExceptionHandler(Exception.class)` is a safety net, not a mapping strategy.
- **When an ERROR log shows a client-caused exception reaching the catch-all:**
  - Add an explicit handler with the right 4xx status and a friendly message.
  - Add a `@WebMvcTest` that simulates it, by throwing the exception from the mocked use case if MockMvc can't reproduce it.
  - Add a row to the status-mapping table above. (Example: Tomcat's `InvalidParameterException` for `?page=%` → 400.)
- **Verify new error paths end to end:** MockMvc skips the servlet container's request parsing, so also check each new error case with `curl` against `./gradlew bootRun`. Confirm the status, that the body follows `ApiResponseDTO`/`ErrorResponseDTO`, and that the log level matches (4xx WARN, 5xx ERROR).
- No try/catch in controllers. Let exceptions reach the handler.

## API conventions
- Every route is prefixed with `/api/v1`.
- Use standard HTTP verbs and statuses:
  - `200` for reads and updates
  - `201` + `Location` for create
  - `204` for delete
- Every response body is the shared envelope `ApiResponseDTO<T>` (`infrastructure/web`). Successes return `ApiResponseDTO.ok(data)` → `{ "success": true, "data": … }`, and errors go through `ErrorResponses` (see Error handling). A `204` has no body.
- Request DTOs use Jakarta Validation annotations, and controllers use `@Valid`.

## Testing / TDD
- Write the test first (red → green → refactor) for each slice.

| Layer | Tooling |
|---|---|
| domain | JUnit 5 + AssertJ (pure unit tests) |
| use cases | JUnit 5 + Mockito, or hand-written fakes of the output ports |
| web | `@WebMvcTest` (status codes, `ApiResponseDTO` envelope / `ErrorResponseDTO` body, no leaked details; `OutputCaptureExtension` for log assertions) |
| pokeapi HTTP client | `MockRestServiceServer` with JSON fixtures in `src/test/resources/pokeapi` |
| pokeapi adapter | hand-written `FakePokeApiClient` (no HTTP mocking) |

- **Database:** PostgreSQL at `jdbc:postgresql://localhost:5432/pokemondb` (user/password `postgres`/`postgres`, see `application.properties`). `PokemonGoApplicationTests.contextLoads` loads the full context, so it needs that DB running. `@WebMvcTest` slices do not.

## Commands
```bash
./gradlew compileJava   # compile
./gradlew test          # run tests
./gradlew bootRun       # run the API (port 8080)
```