-- Demo seed for the local Pokemon catalog (US01). Idempotent: safe to run on every startup.
-- Primary keys are auto-incremented; rows are matched by their business keys instead.

INSERT INTO pokemon (pokedex_number, name, sprite_url, category, weight_hectograms) VALUES
    (1,  'bulbasaur',  'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png',  'Seed Pokémon',        69),
    (2,  'ivysaur',    'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/2.png',  'Seed Pokémon',        130),
    (3,  'venusaur',   'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/3.png',  'Seed Pokémon',        1000),
    (4,  'charmander', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/4.png',  'Lizard Pokémon',      85),
    (5,  'charmeleon', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/5.png',  'Flame Pokémon',       190),
    (6,  'charizard',  'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/6.png',  'Flame Pokémon',       905),
    (7,  'squirtle',   'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/7.png',  'Tiny Turtle Pokémon', 90),
    (8,  'wartortle',  'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/8.png',  'Turtle Pokémon',      225),
    (9,  'blastoise',  'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/9.png',  'Shellfish Pokémon',   855),
    (25, 'pikachu',    'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png', 'Mouse Pokémon',       60)
ON CONFLICT (pokedex_number) DO NOTHING;

INSERT INTO pokemon_ability (pokemon_id, name, hidden)
SELECT p.id, a.name, a.hidden
FROM (VALUES
    (1,  'overgrow', false), (1,  'chlorophyll',   true),
    (2,  'overgrow', false), (2,  'chlorophyll',   true),
    (3,  'overgrow', false), (3,  'chlorophyll',   true),
    (4,  'blaze',    false), (4,  'solar-power',   true),
    (5,  'blaze',    false), (5,  'solar-power',   true),
    (6,  'blaze',    false), (6,  'solar-power',   true),
    (7,  'torrent',  false), (7,  'rain-dish',     true),
    (8,  'torrent',  false), (8,  'rain-dish',     true),
    (9,  'torrent',  false), (9,  'rain-dish',     true),
    (25, 'static',   false), (25, 'lightning-rod', true)
) AS a (pokedex_number, name, hidden)
JOIN pokemon p ON p.pokedex_number = a.pokedex_number
ON CONFLICT (pokemon_id, name) DO NOTHING;
