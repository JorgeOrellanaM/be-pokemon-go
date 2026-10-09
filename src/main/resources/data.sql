-- Demo seed for the local Pokemon store (US03). Idempotent: safe to run on every startup.
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

-- Demo values for the fields this service owns (US03): French localized names, region and tags.
-- Only filled while both custom texts are still empty, so values edited through the API are kept.
UPDATE pokemon p
SET localized_name = v.localized_name, region = v.region
FROM (VALUES
    (1,  'Bulbizarre', 'Kanto'),
    (4,  'Salamèche',  'Kanto'),
    (7,  'Carapuce',   'Kanto'),
    (25, 'Pikachu',    'Kanto')
) AS v (pokedex_number, localized_name, region)
WHERE p.pokedex_number = v.pokedex_number
  AND p.localized_name IS NULL
  AND p.region IS NULL;

INSERT INTO pokemon_tag (pokemon_id, name)
SELECT p.id, t.name
FROM (VALUES
    (1, 'starter'), (4, 'starter'), (7, 'starter'), (25, 'mascot')
) AS t (pokedex_number, name)
JOIN pokemon p ON p.pokedex_number = t.pokedex_number
ON CONFLICT (pokemon_id, name) DO NOTHING;
