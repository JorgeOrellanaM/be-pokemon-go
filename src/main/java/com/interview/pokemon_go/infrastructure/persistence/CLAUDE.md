# Persistence rules

## Primary keys and attributes
- Every entity has a `Long id` primary key with `@GeneratedValue(strategy = GenerationType.IDENTITY)` (auto-increment).
- Every entity has at least two descriptive, non-null columns besides the id.
- Natural or business keys (e.g. `pokemon.pokedex_number`, `app_user.username`) get a **unique constraint**. They are never the primary key.
- The database `id` never leaves this package. The domain identifies a Pokémon by its Pokédex number.