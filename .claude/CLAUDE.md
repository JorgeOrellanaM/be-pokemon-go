# Project

- REST API over [PokeAPI](https://pokeapi.co/docs/v2) for the Ballast Lane Java technical interview.
- Stack: Spring Boot 4.1.1, Java 25, Gradle Kotlin DSL.
- The frontend lives in a separate folder, with its own Claude session.
- Base package: `com.interview.pokemon_go`.

## User stories
Current focus: **US01** (as of 2026-10-07).

| Id | Story |
|---|---|
| US01 | Pokemon enumeration: paginated list with sprite, category, weight, abilities. Nice to have: response caching. |
| US02 | Detailed view: image, core stats, description, evolution chain. |
| US03 | Data synchronization: persist Pokemon into a local relational DB, adding proprietary fields (localized name, geo metadata, internal tags). |
| US04 | Local data modification: update locally stored Pokemon; 404 for missing records, 400 for malformed payloads. |
| Auth | User registration and authentication; protected vs. public routes. |

## Deliverables
- Public Git repo
- README (setup and technical docs)
- Seeded demo data and credentials
- Dockerfile
- GenAI section (prompt, output, how it was validated)
- Presentation and code review

Evaluation criteria:
- Clean Architecture
- Test coverage (TDD preferred)
- Code quality
- Functionality
- Presentation
- GenAI fluency
