# Flowbit Pokédex (Java)

A compact Spring Boot application that exposes a cached Pokémon search API and a rich single-page UI. The service fetches data from [PokeAPI](https://pokeapi.co/) once and serves repeated queries from an in-memory Caffeine cache with TTL and max-size eviction.

## Stack

- Java 17 + Spring Boot 3
- WebClient for calling PokeAPI
- Spring Cache + Caffeine (10‑minute TTL, 200 entries)
- Vanilla HTML/CSS/JS UI served via static resources

## Project layout

```
apps/pokedex-java
├─ pom.xml
├─ src/main/java/com/flowbit/pokedex
│  ├─ PokedexApplication.java
│  ├─ controller/PokemonController.java
│  ├─ service/PokemonService.java
│  ├─ client/PokemonClient.java
│  ├─ dto/...
│  └─ exception/...
└─ src/main/resources
   ├─ application.yml
   └─ static/ (index.html, styles.css, app.js)
```

## Running locally

```bash
cd apps/pokedex-java
mvn spring-boot:run
```

Then open http://localhost:8085 to use the UI. The API is available at http://localhost:8085/api/pokemon?name=pikachu.

## REST API

`GET /api/pokemon?name=<pokemon-name>`

**Query params**

- `name` – required, alphanumeric/dash (validated)

**Response**

```json
{
  "id": 25,
  "name": "pikachu",
  "height": 4,
  "weight": 60,
  "baseExperience": 112,
  "order": 35,
  "sprite": "https://raw.githubusercontent.com/...",
  "types": ["electric"],
  "heldItems": ["oran-berry"],
  "abilities": [
    {"name": "static", "hidden": false},
    {"name": "lightning-rod", "hidden": true}
  ],
  "stats": [
    {"name": "speed", "value": 90}
  ],
  "moves": ["quick-attack", "thunder"]
}
```

Errors are returned with standard HTTP status codes and JSON payloads.

## Caching strategy

- In-memory Caffeine cache named `pokemon`
- Up to 200 unique Pokémon kept
- Entries expire 10 minutes after write
- Cache key uses lowercase name to avoid duplicates from casing differences

## Documentation

- **WORKFLOW.md** - Detailed architecture and request flow explanation
- **TESTING_GUIDE.md** - Comprehensive testing guide with expected outputs

## Testing

```bash
mvn test
```

For detailed testing instructions and expected outputs, see [TESTING_GUIDE.md](./TESTING_GUIDE.md).

## Quick Start

1. **Set JAVA_HOME** (if needed):
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
   ```

2. **Run the application**:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

3. **Access**:
   - Frontend: http://localhost:8085
   - API: http://localhost:8085/api/pokemon?name=pikachu

