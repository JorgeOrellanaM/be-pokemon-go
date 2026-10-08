# Persistence rules

## Primary keys and attributes
- Every entity has a `Long id` primary key with `@GeneratedValue(strategy = GenerationType.IDENTITY)` (auto-increment).
- Every entity has at least two descriptive, non-null columns besides the id.
- Natural or business keys (e.g. `pokemon.pokedex_number`, `app_user.username`) get a **unique constraint**. They are never the primary key.
- The database `id` never leaves this package. The domain identifies a Pokémon by its Pokédex number.

## US03: Data synchronization
- Sync upserts a Pokémon by `pokedex_number`.
- Proprietary fields (e.g. `local_name`, `region`, internal classification tags in a child table) are nullable and local-only. Sync **never overwrites** them.
- Re-syncing a record that already exists throws `*AlreadySyncedException` (409), unless a refresh is explicitly requested.

## US04: Local data modification
- Updates only touch records already stored locally. They never call PokeAPI.
- A missing record throws `PokemonNotFoundException` (404).
- A malformed payload or invalid field is rejected with 400, through bean validation or `DomainValidationException`.
- A unique-key collision on update (`DataIntegrityViolationException`) is translated to `*AlreadyExistsException` (409).
