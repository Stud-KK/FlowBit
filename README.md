
# **Lowbit Pokédex (Java)**

A compact **Spring Boot** application that exposes a **cached Pokémon search API** and a **rich single-page UI**. The service fetches data from [PokeAPI](https://pokeapi.co/) once and serves repeated queries from an **in-memory Caffeine cache** with TTL and max-size eviction.

---

## **Stack**

* Java 17 + Spring Boot 3
* Spring WebClient for calling PokeAPI
* Spring Cache + Caffeine (10-minute TTL, 200 entries)
* Vanilla HTML/CSS/JS UI served via static resources

---

## **Project Layout**

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

---

## **Running Locally**

1. Navigate to project folder:

```bash
cd apps/pokedex-java
```

2. Run the application:

```bash
mvn spring-boot:run
```

3. Open the UI: [http://localhost:8085](http://localhost:8085)
   API endpoint: `http://localhost:8085/api/pokemon?name=pikachu`

> **Note:** Ensure `JAVA_HOME` is set (e.g., `C:\Program Files\Java\jdk-17`) if required.

---

## **REST API**

**Endpoint:** `GET /api/pokemon?name=<pokemon-name>`

**Query Parameters:**

* `name` – required, alphanumeric/dash (validated)

**Sample Response:**

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

**Error Handling:**
Standard HTTP status codes with JSON payloads.

---

## **Caching Strategy**

* In-memory **Caffeine cache** named `pokemon`
* **Max 200 unique Pokémon**
* Entries expire **10 minutes** after write
* **Cache key** uses lowercase Pokémon names to avoid duplicates from casing differences

---

## **Documentation**

* `WORKFLOW.md` – Detailed architecture and request flow
* `TESTING_GUIDE.md` – Testing instructions with expected outputs




---

## **Quick Start**
<img width="1366" height="721" alt="Screenshot (500)" src="https://github.com/user-attachments/assets/b4fb24f0-8380-4d9e-bfd7-df312032f421" />
<img width="1366" height="725" alt="Screenshot (501)" src="https://github.com/user-attachments/assets/4e6f66ca-198e-4e65-a2b5-9df3ab7f355f" />

<img width="1366" height="721" alt="Screenshot (502)" src="https://github.com/user-attachments/assets/b6ca0792-e7fc-4fce-8e41-25e42703a484" />
<img width="1366" height="717" alt="Screenshot (503)" src="https://github.com/user-attachments/assets/3dd82e80-c05b-4c5c-a657-86415750bf6a" />


Set `JAVA_HOME` if needed:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```

Run:

```powershell
.\mvnw.cmd spring-boot:run
```

Access:

* Frontend: [http://localhost:8085](http://localhost:8085)
* API: `http://localhost:8085/api/pokemon?name=pikachu`

---

✅ **Tip:** Include screenshots of the UI in the repo to make it visually appealing for the reviewer.

---

If you want, I can also **draft the exact email text** to submit your GitHub repo **right now**, politely apologizing for the delay. That way, you can send it immediately and look professional.

Do you want me to do that?
