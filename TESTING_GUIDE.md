# Pokédex Application - Testing Guide

## 🚀 How to Run

1. **Set JAVA_HOME** (if not already set):
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
   ```

2. **Start the application**:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

3. **Wait for startup** (look for: `Started PokedexApplication` in logs)

4. **Access the application**:
   - Frontend UI: http://localhost:8085
   - API Base: http://localhost:8085/api

---

## ✅ What to Check & Expected Outputs

### 1. **Frontend UI Test**

**What to check:**
- Open browser: http://localhost:8085
- Search for a Pokemon (e.g., "pikachu")

**Expected Output:**
- ✅ Page loads with "Flowbit Pokédex" title
- ✅ Search form is visible
- ✅ After searching, Pokemon card displays with:
  - Pokemon ID (e.g., "#0025")
  - Name (e.g., "Pikachu")
  - Types (e.g., "Electric")
  - Image/Sprite
  - Height, Weight, Base EXP
  - Abilities list
  - Base Stats with visual bars
  - Signature Moves
  - Held Items

---

### 2. **REST API - Valid Pokemon**

**Test Case 1: Pikachu**
```powershell
Invoke-RestMethod -Uri "http://localhost:8085/api/pokemon?name=pikachu" -Method Get
```

**Expected Output:**
```json
{
  "id": 25,
  "name": "pikachu",
  "height": 4,
  "weight": 60,
  "baseExperience": 112,
  "order": 35,
  "sprite": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png",
  "types": ["electric"],
  "heldItems": [],
  "abilities": [
    {
      "name": "static",
      "hidden": false
    },
    {
      "name": "lightning-rod",
      "hidden": true
    }
  ],
  "stats": [
    {
      "name": "hp",
      "value": 35
    },
    {
      "name": "attack",
      "value": 55
    },
    {
      "name": "defense",
      "value": 40
    },
    {
      "name": "special-attack",
      "value": 50
    },
    {
      "name": "special-defense",
      "value": 50
    },
    {
      "name": "speed",
      "value": 90
    }
  ],
  "moves": [
    "mega-punch",
    "pay-day",
    "thunder-punch",
    "slam",
    "double-team",
    "mega-kick"
  ]
}
```

**Test Case 2: Charizard**
```powershell
Invoke-RestMethod -Uri "http://localhost:8085/api/pokemon?name=charizard" -Method Get
```

**Expected Output:**
- ✅ Status Code: 200
- ✅ JSON response with:
  - `id`: 6
  - `name`: "charizard"
  - `types`: ["fire", "flying"]
  - `height`, `weight`, `baseExperience` present
  - `abilities` array with at least 1 ability
  - `stats` array with 6 stats
  - `moves` array with up to 6 moves

**Test Case 3: Bulbasaur**
```powershell
Invoke-RestMethod -Uri "http://localhost:8085/api/pokemon?name=bulbasaur" -Method Get
```

**Expected Output:**
- ✅ Status Code: 200
- ✅ `id`: 1
- ✅ `name`: "bulbasaur"
- ✅ `types`: ["grass", "poison"]

---

### 3. **Caching Test**

**What to check:**
- First request should fetch from API
- Second request (same Pokemon) should be faster (from cache)

**Test:**
```powershell
# First request (cache miss)
Measure-Command { Invoke-RestMethod -Uri "http://localhost:8085/api/pokemon?name=pikachu" } | Select-Object TotalMilliseconds

# Second request (cache hit - should be much faster)
Measure-Command { Invoke-RestMethod -Uri "http://localhost:8085/api/pokemon?name=pikachu" } | Select-Object TotalMilliseconds
```

**Expected Output:**
- ✅ First request: ~500-2000ms (network call to PokéAPI)
- ✅ Second request: ~10-50ms (cached response)
- ✅ Both return identical data

---

### 4. **Error Handling - Invalid Pokemon**

**Test Case: Non-existent Pokemon**
```powershell
try {
    Invoke-WebRequest -Uri "http://localhost:8085/api/pokemon?name=invalidpokemon123" -Method Get
} catch {
    $_.Exception.Response.StatusCode
    $_.Exception.Response | Get-Member
}
```

**Expected Output:**
- ✅ Status Code: **404 Not Found**
- ✅ Response Body:
```json
{
  "timestamp": "2025-11-26T...",
  "status": 404,
  "error": "Not Found",
  "message": "Pokémon 'invalidpokemon123' was not found"
}
```

**Test Case: Empty Name**
```powershell
Invoke-WebRequest -Uri "http://localhost:8085/api/pokemon?name=" -Method Get
```

**Expected Output:**
- ✅ Status Code: **400 Bad Request**
- ✅ Validation error message

**Test Case: Invalid Characters**
```powershell
Invoke-WebRequest -Uri "http://localhost:8085/api/pokemon?name=pikachu@123" -Method Get
```

**Expected Output:**
- ✅ Status Code: **400 Bad Request**
- ✅ Validation error (name must be alphanumeric or dash)

---

### 5. **Case Insensitivity Test**

**What to check:**
- Pokemon names should be case-insensitive

**Test:**
```powershell
$lower = Invoke-RestMethod -Uri "http://localhost:8085/api/pokemon?name=pikachu"
$upper = Invoke-RestMethod -Uri "http://localhost:8085/api/pokemon?name=PIKACHU"
$mixed = Invoke-RestMethod -Uri "http://localhost:8085/api/pokemon?name=PiKaChU"
```

**Expected Output:**
- ✅ All three requests return identical data
- ✅ All return `"name": "pikachu"` (normalized to lowercase)

---

### 6. **Multiple Pokemon Test**

**What to check:**
- API should handle different Pokemon correctly

**Test:**
```powershell
$pokemon = @("pikachu", "charizard", "bulbasaur", "squirtle", "mewtwo")
foreach ($p in $pokemon) {
    try {
        $result = Invoke-RestMethod -Uri "http://localhost:8085/api/pokemon?name=$p"
        Write-Host "✅ $($result.name) (ID: $($result.id), Types: $($result.types -join ', '))" -ForegroundColor Green
    } catch {
        Write-Host "❌ $p : $($_.Exception.Message)" -ForegroundColor Red
    }
}
```

**Expected Output:**
- ✅ All valid Pokemon return 200 status
- ✅ Each has unique ID, name, types
- ✅ Data structure is consistent

---

### 7. **Performance Test**

**What to check:**
- Response times should be reasonable
- Cached responses should be fast

**Test:**
```powershell
Write-Host "Testing response times..." -ForegroundColor Cyan
$times = @()
1..5 | ForEach-Object {
    $time = Measure-Command { 
        Invoke-RestMethod -Uri "http://localhost:8085/api/pokemon?name=pikachu" | Out-Null
    }
    $times += $time.TotalMilliseconds
    Write-Host "Request $_ : $([math]::Round($time.TotalMilliseconds, 0))ms"
}
Write-Host "`nAverage: $([math]::Round(($times | Measure-Object -Average).Average, 0))ms" -ForegroundColor Yellow
Write-Host "First request (cache miss): $([math]::Round($times[0], 0))ms" -ForegroundColor Yellow
Write-Host "Subsequent requests (cache hit): $([math]::Round(($times[1..4] | Measure-Object -Average).Average, 0))ms" -ForegroundColor Green
```

**Expected Output:**
- ✅ First request: 500-2000ms (network call)
- ✅ Subsequent requests: 10-100ms (cached)
- ✅ Average cached response: < 50ms

---

### 8. **Data Completeness Test**

**What to check:**
- All required fields are present
- Data types are correct

**Test:**
```powershell
$p = Invoke-RestMethod -Uri "http://localhost:8085/api/pokemon?name=pikachu"

# Check required fields
$checks = @{
    "Has ID" = $null -ne $p.id
    "Has Name" = $null -ne $p.name -and $p.name -ne ""
    "Has Types" = $p.types.Count -gt 0
    "Has Height" = $null -ne $p.height
    "Has Weight" = $null -ne $p.weight
    "Has Sprite" = $null -ne $p.sprite -and $p.sprite -ne ""
    "Has Abilities" = $p.abilities.Count -gt 0
    "Has Stats" = $p.stats.Count -eq 6
    "Has Moves" = $p.moves.Count -gt 0
}

$checks.GetEnumerator() | ForEach-Object {
    if ($_.Value) {
        Write-Host "✅ $($_.Key)" -ForegroundColor Green
    } else {
        Write-Host "❌ $($_.Key)" -ForegroundColor Red
    }
}
```

**Expected Output:**
- ✅ All checks should pass
- ✅ All fields populated with correct data types

---

## 📋 Complete Test Script

Run this comprehensive test:

```powershell
Write-Host "`n=== POKEDEX COMPREHENSIVE TEST ===" -ForegroundColor Cyan

# Test 1: Valid Pokemon
Write-Host "`n1. Testing valid Pokemon (Pikachu)..." -ForegroundColor White
try {
    $pika = Invoke-RestMethod -Uri "http://localhost:8085/api/pokemon?name=pikachu"
    Write-Host "   ✅ Success: $($pika.name) (ID: $($pika.id))" -ForegroundColor Green
    Write-Host "   Types: $($pika.types -join ', ')" -ForegroundColor Gray
    Write-Host "   Stats: $($pika.stats.Count), Abilities: $($pika.abilities.Count), Moves: $($pika.moves.Count)" -ForegroundColor Gray
} catch {
    Write-Host "   ❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Caching
Write-Host "`n2. Testing cache..." -ForegroundColor White
$first = Measure-Command { Invoke-RestMethod -Uri "http://localhost:8085/api/pokemon?name=pikachu" | Out-Null }
$second = Measure-Command { Invoke-RestMethod -Uri "http://localhost:8085/api/pokemon?name=pikachu" | Out-Null }
Write-Host "   First request: $([math]::Round($first.TotalMilliseconds, 0))ms" -ForegroundColor Yellow
Write-Host "   Cached request: $([math]::Round($second.TotalMilliseconds, 0))ms" -ForegroundColor Green
if ($second.TotalMilliseconds -lt $first.TotalMilliseconds / 2) {
    Write-Host "   ✅ Cache working (cached response is faster)" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  Cache may not be working optimally" -ForegroundColor Yellow
}

# Test 3: Error Handling
Write-Host "`n3. Testing error handling..." -ForegroundColor White
try {
    Invoke-WebRequest -Uri "http://localhost:8085/api/pokemon?name=invalid123" -ErrorAction Stop | Out-Null
    Write-Host "   ❌ Should have returned 404" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 404) {
        Write-Host "   ✅ Proper 404 error handling" -ForegroundColor Green
    } else {
        Write-Host "   ⚠️  Unexpected error: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
    }
}

# Test 4: Frontend
Write-Host "`n4. Testing frontend..." -ForegroundColor White
try {
    $web = Invoke-WebRequest -Uri "http://localhost:8085" -UseBasicParsing
    if ($web.Content -match "Flowbit Pokédex") {
        Write-Host "   ✅ Frontend accessible with expected content" -ForegroundColor Green
    } else {
        Write-Host "   ⚠️  Frontend accessible but content may differ" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ❌ Frontend error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Multiple Pokemon
Write-Host "`n5. Testing multiple Pokemon..." -ForegroundColor White
$testPokemon = @("bulbasaur", "charizard", "squirtle")
$success = 0
foreach ($p in $testPokemon) {
    try {
        $result = Invoke-RestMethod -Uri "http://localhost:8085/api/pokemon?name=$p"
        Write-Host "   ✅ $($result.name)" -ForegroundColor Green
        $success++
    } catch {
        Write-Host "   ❌ $p : $($_.Exception.Message)" -ForegroundColor Red
    }
}
Write-Host "   Success rate: $success/$($testPokemon.Count)" -ForegroundColor $(if ($success -eq $testPokemon.Count) { "Green" } else { "Yellow" })

Write-Host "`n=== TEST COMPLETE ===" -ForegroundColor Cyan
```

---

## ✅ Acceptance Criteria Checklist

- [x] **REST API Endpoint**: `/api/pokemon?name={name}` returns JSON
- [x] **Caching**: Responses cached, second request is faster
- [x] **Pokemon Data**: Complete attributes (name, ID, types, stats, abilities, moves)
- [x] **Error Handling**: 404 for invalid Pokemon, 400 for invalid input
- [x] **Frontend UI**: Accessible at root URL, displays Pokemon data
- [x] **Case Insensitive**: Works with any case (PIKACHU, pikachu, PiKaChU)
- [x] **Performance**: Cached responses < 100ms
- [x] **Code Quality**: Clean, structured, follows REST guidelines

---

## 🎯 Expected Final Status

When all tests pass, you should see:
- ✅ All API endpoints responding correctly
- ✅ Frontend UI displaying Pokemon data beautifully
- ✅ Fast cached responses
- ✅ Proper error handling
- ✅ Application running smoothly on port 8085

**Access Points:**
- 🌐 Frontend: http://localhost:8085
- 📡 API: http://localhost:8085/api/pokemon?name=pikachu


