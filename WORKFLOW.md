# Pokédex Application - Workflow Documentation

## 📋 Application Architecture

```
┌─────────────┐
│   Browser   │
│  (Frontend) │
└──────┬──────┘
       │ HTTP Request
       │ GET /api/pokemon?name=pikachu
       ▼
┌─────────────────────────────────────┐
│   Spring Boot Application            │
│   (Port 8085)                        │
│                                      │
│  ┌──────────────────────────────┐  │
│  │  PokemonController           │  │
│  │  - Validates input           │  │
│  │  - @GetMapping("/pokemon")   │  │
│  └──────────┬───────────────────┘  │
│             │                        │
│             ▼                        │
│  ┌──────────────────────────────┐  │
│  │  PokemonService              │  │
│  │  - @Cacheable annotation     │  │
│  │  - Checks cache first        │  │
│  │  - Maps JSON to DTO          │  │
│  └──────────┬───────────────────┘  │
│             │                        │
│             ▼                        │
│  ┌──────────────────────────────┐  │
│  │  PokemonClient               │  │
│  │  - WebClient (Reactive)      │  │
│  │  - Fetches from PokéAPI      │  │
│  └──────────┬───────────────────┘  │
│             │                        │
└─────────────┼──────────────────────┘
              │
              │ HTTP GET
              ▼
      ┌───────────────┐
      │   PokéAPI     │
      │ pokeapi.co    │
      └───────────────┘
```

---

## 🔄 Request Flow (Step-by-Step)

### **1. User Request**
```
User → Browser → http://localhost:8085/api/pokemon?name=pikachu
```

### **2. Controller Layer** (`PokemonController`)
**File:** `src/main/java/com/flowbit/pokedex/controller/PokemonController.java`

**Responsibilities:**
- Receives HTTP GET request
- Validates input parameters:
  - `@NotBlank`: Ensures name is not empty
  - `@Pattern`: Ensures name contains only alphanumeric characters or dashes
- Calls `PokemonService.findPokemon(name)`
- Returns `PokemonSummary` DTO as JSON

**Code Flow:**
```java
@GetMapping("/pokemon")
public PokemonSummary getPokemon(@RequestParam("name") String name) {
    return pokemonService.findPokemon(name);  // Delegates to service
}
```

### **3. Service Layer** (`PokemonService`)
**File:** `src/main/java/com/flowbit/pokedex/service/PokemonService.java`

**Responsibilities:**
- **Caching Logic**: `@Cacheable` annotation checks cache first
  - Cache Key: `name.toLowerCase()` (case-insensitive)
  - Cache Name: `"pokemon"`
  - If cache hit → returns immediately (fast!)
  - If cache miss → proceeds to fetch from API
- **Data Transformation**: Maps raw JSON from PokéAPI to `PokemonSummary` DTO
- Extracts and structures:
  - Basic info (id, name, height, weight)
  - Types, Abilities, Stats, Moves, Held Items
  - Sprite/Image URL

**Code Flow:**
```java
@Cacheable(cacheNames = "pokemon", key = "#name.toLowerCase()")
public PokemonSummary findPokemon(String name) {
    JsonNode payload = pokemonClient.fetchPokemon(name);  // Fetch from API
    return map(payload);  // Transform to DTO
}
```

### **4. Client Layer** (`PokemonClient`)
**File:** `src/main/java/com/flowbit/pokedex/client/PokemonClient.java`

**Responsibilities:**
- Makes HTTP request to PokéAPI using Spring WebClient (reactive)
- Base URL: `https://pokeapi.co/api/v2`
- Endpoint: `/pokemon/{name}`
- Error Handling:
  - 4xx errors → throws `PokemonNotFoundException`
  - 5xx errors → throws `RuntimeException`
- Returns raw JSON as `JsonNode`

**Code Flow:**
```java
public JsonNode fetchPokemon(String name) {
    return webClient
        .get()
        .uri("/pokemon/{name}", name.toLowerCase())
        .retrieve()
        .bodyToMono(JsonNode.class)
        .block();  // Blocking call (converts reactive to sync)
}
```

### **5. External API Call**
```
PokemonClient → HTTPS → https://pokeapi.co/api/v2/pokemon/pikachu
```

**Response:** Large JSON object with Pokemon data

### **6. Response Flow (Backwards)**

```
PokéAPI Response (JSON)
    ↓
PokemonClient.fetchPokemon() → Returns JsonNode
    ↓
PokemonService.map() → Transforms to PokemonSummary DTO
    ↓
Cache Storage (Caffeine) → Stores for 10 minutes
    ↓
PokemonService.findPokemon() → Returns PokemonSummary
    ↓
PokemonController.getPokemon() → Returns JSON response
    ↓
Browser receives JSON → Frontend displays Pokemon data
```

---

## 💾 Caching Workflow

### **Cache Configuration**
**File:** `src/main/java/com/flowbit/pokedex/config/CacheConfig.java`

**Settings:**
- **Cache Library**: Caffeine
- **TTL (Time To Live)**: 10 minutes
- **Max Size**: 200 entries
- **Cache Name**: `"pokemon"`

### **Cache Flow**

**First Request (Cache Miss):**
```
1. User requests "pikachu"
2. Service checks cache → NOT FOUND
3. Fetches from PokéAPI (500-2000ms)
4. Transforms data
5. Stores in cache
6. Returns response
```

**Subsequent Request (Cache Hit):**
```
1. User requests "pikachu" again
2. Service checks cache → FOUND!
3. Returns from cache (10-50ms) ⚡
4. No API call needed
```

**Cache Expiry:**
- After 10 minutes, cache entry expires
- Next request will fetch fresh data from API
- Cache automatically evicts oldest entries when limit (200) is reached

---

## 🎨 Frontend Workflow

### **Static Files**
Located in: `src/main/resources/static/`

**Files:**
- `index.html` - Main UI structure
- `styles.css` - Styling
- `app.js` - JavaScript logic

### **Frontend Flow**

```
1. User opens http://localhost:8085
   ↓
2. Spring Boot serves index.html (static resource)
   ↓
3. User types Pokemon name in search box
   ↓
4. JavaScript (app.js) makes fetch() call:
   fetch('/api/pokemon?name=pikachu')
   ↓
5. Receives JSON response
   ↓
6. Renders Pokemon card with:
   - Image
   - Stats bars
   - Abilities pills
   - Moves list
   - All attributes
```

---

## ⚠️ Error Handling Workflow

### **Exception Hierarchy**

**File:** `src/main/java/com/flowbit/pokedex/exception/GlobalExceptionHandler.java`

### **Error Flow**

```
1. Exception occurs (e.g., Pokemon not found)
   ↓
2. GlobalExceptionHandler catches it
   ↓
3. Maps to appropriate HTTP status:
   - PokemonNotFoundException → 404 Not Found
   - ConstraintViolationException → 400 Bad Request
   - Other exceptions → 500 Internal Server Error
   ↓
4. Returns JSON error response:
   {
     "timestamp": "2025-11-26T...",
     "status": 404,
     "error": "Not Found",
     "message": "Pokémon 'invalid' was not found"
   }
```

---

## 🔧 Configuration Files

### **1. application.yml**
**Location:** `src/main/resources/application.yml`

**Settings:**
- Server port: 8085
- Cache names: pokemon

### **2. pom.xml**
**Location:** `pom.xml`

**Dependencies:**
- Spring Boot Web (REST API)
- Spring Boot WebFlux (Reactive WebClient)
- Spring Boot Cache (Caching support)
- Caffeine (Cache implementation)
- Jackson (JSON processing)
- Validation (Input validation)

---

## 📊 Data Flow Example

### **Request:**
```
GET /api/pokemon?name=pikachu
```

### **Processing:**
1. **Controller** validates "pikachu" ✓
2. **Service** checks cache → MISS
3. **Client** calls PokéAPI → `GET https://pokeapi.co/api/v2/pokemon/pikachu`
4. **PokéAPI** returns large JSON (1000+ lines)
5. **Service** maps JSON to DTO:
   ```java
   PokemonSummary(
       id = 25,
       name = "pikachu",
       types = ["electric"],
       stats = [hp:35, attack:55, ...],
       abilities = [static, lightning-rod],
       ...
   )
   ```
6. **Service** stores in cache
7. **Controller** returns JSON response

### **Response:**
```json
{
  "id": 25,
  "name": "pikachu",
  "height": 4,
  "weight": 60,
  "baseExperience": 112,
  "types": ["electric"],
  "abilities": [
    {"name": "static", "hidden": false},
    {"name": "lightning-rod", "hidden": true}
  ],
  "stats": [
    {"name": "hp", "value": 35},
    {"name": "attack", "value": 55},
    ...
  ],
  "moves": ["mega-punch", "pay-day", ...],
  "sprite": "https://..."
}
```

---

## 🚀 Startup Sequence

1. **Spring Boot starts** (`PokedexApplication.main()`)
2. **Configuration loaded**:
   - `CacheConfig` → Sets up Caffeine cache
   - `application.yml` → Configures server port
3. **Beans created**:
   - `WebClient.Builder` → For API calls
   - `PokemonClient` → HTTP client
   - `PokemonService` → Business logic
   - `PokemonController` → REST endpoint
   - `GlobalExceptionHandler` → Error handling
4. **Tomcat embedded server starts** on port 8085
5. **Static resources** (HTML/CSS/JS) served from `/`
6. **API endpoints** available at `/api/*`

---

## 📝 Key Design Decisions

1. **Caching**: Reduces API calls, improves performance
2. **DTO Pattern**: Clean separation, type-safe data transfer
3. **Reactive WebClient**: Non-blocking I/O for external API calls
4. **Exception Handling**: Centralized error responses
5. **Validation**: Input sanitization at controller level
6. **Case Insensitivity**: Normalizes Pokemon names to lowercase

---

## 🔍 Component Responsibilities

| Component | Responsibility |
|-----------|---------------|
| `PokemonController` | HTTP request handling, validation |
| `PokemonService` | Business logic, caching, data transformation |
| `PokemonClient` | External API communication |
| `CacheConfig` | Cache configuration |
| `GlobalExceptionHandler` | Error handling |
| `PokemonSummary` | Data transfer object |
| Frontend (HTML/JS) | User interface, API calls, rendering |

---

## ✅ Summary

**Request Flow:**
```
Browser → Controller → Service (Cache Check) → Client → PokéAPI
                                                          ↓
Browser ← Controller ← Service (Cache Store) ← Client ← JSON
```

**Key Features:**
- ✅ RESTful API design
- ✅ Intelligent caching (10 min TTL, 200 max entries)
- ✅ Error handling with proper HTTP status codes
- ✅ Input validation
- ✅ Clean architecture (layered design)
- ✅ Frontend integration


