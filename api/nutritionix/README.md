# BiteScan Nutrition API

FastAPI proxy server that securely integrates Nutritionix API for nutrition data lookups.

## Purpose

This server acts as a secure intermediary between the Android app and Nutritionix API, keeping API credentials server-side and providing clean, normalized responses.

## Setup

### 1. Install Dependencies

```bash
pip install -r requirements.txt
```

### 2. Set Environment Variables

```bash
export NUTRITIONIX_APP_ID="your_app_id_here"
export NUTRITIONIX_APP_KEY="your_app_key_here"
```

### 3. Run Server

```bash
python nutritionapi.py
```

Server will start at `http://localhost:8080`

## API Endpoints

### Health Check

```
GET /
```

**Response:**
```json
{
  "ok": true,
  "service": "nutritionapi"
}
```

---

### Natural Language Nutrition Query

```
POST /api/nutritionix/natural
Content-Type: application/json

{
  "query": "2 eggs and bacon"
}
```

**Success Response (200):**
```json
{
  "status": "ok",
  "name": "Egg",
  "serving_qty": 2.0,
  "serving_unit": "large",
  "serving_weight_grams": 100.0,
  "macros": {
    "calories": 143.0,
    "protein": 12.6,
    "carbs": 0.7,
    "fat": 9.5
  }
}
```

**Not Found Response (200):**
```json
{
  "status": "missing",
  "name": null,
  "serving_qty": null,
  "serving_unit": null,
  "serving_weight_grams": null,
  "macros": null
}
```

**Error Response (400/502):**
```json
{
  "detail": "Error message"
}
```

---

### Instant Search (Autocomplete)

```
GET /api/nutritionix/instant?query=chi
```

**Response (200):**
```json
{
  "common": [
    {"food_name": "chicken breast"},
    {"food_name": "chicken thigh"}
  ],
  "branded": [
    {
      "food_name": "Chicken Nuggets",
      "brand_name": "McDonald's"
    }
  ]
}
```

## Testing

Run integration tests to verify the API works:

```bash
python test_nutritionix.py
```

Make sure the server is running before executing tests.

## Android Integration Guide

### 1. Define Data Models

```kotlin
data class NutritionRequest(
    val query: String
)

data class Macros(
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float
)

data class NutritionResponse(
    val status: String,
    val name: String?,
    val serving_qty: Float?,
    val serving_unit: String?,
    val serving_weight_grams: Float?,
    val macros: Macros?
)
```

### 2. Make API Call with Retrofit

```kotlin
interface NutritionApiService {
    @POST("/api/nutritionix/natural")
    suspend fun getNutrition(@Body request: NutritionRequest): NutritionResponse
    
    @GET("/api/nutritionix/instant")
    suspend fun searchFoods(@Query("query") query: String): InstantSearchResponse
}
```

### 3. Handle Response

```kotlin
val response = nutritionApi.getNutrition(NutritionRequest("banana"))

when (response.status) {
    "ok" -> {
        // Show nutrition data
        println("${response.name}: ${response.macros?.calories} cal")
    }
    "missing" -> {
        // Show "nutrition data not available" message
        // Optionally let user manually search with instant endpoint
    }
}
```

## Notes
- Keep `NUTRITIONIX_APP_ID` and `NUTRITIONIX_APP_KEY` secret (never commit to repo)
- Nutritionix free tier: 500 requests/day
- Server returns first matched food item for natural queries
- Empty queries return 400 Bad Request
- Network errors return 502 Bad Gateway

