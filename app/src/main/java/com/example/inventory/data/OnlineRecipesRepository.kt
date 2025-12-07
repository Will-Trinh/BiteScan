package com.example.inventory.data

import android.util.Log
import com.example.inventory.data.ai.AiRecipeList
import com.example.inventory.data.ai.OpenRouterClient
import com.example.inventory.data.ai.OrChatRequest
import com.example.inventory.data.ai.OrMessage
import com.example.inventory.data.ai.OrResponseFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.sql.Date
import java.util.concurrent.TimeUnit
import com.example.inventory.ui.AppViewModel
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlin.collections.emptyList


open class OnlineRecipesRepository(
    private val recipesRepository: RecipesRepository,
) {
    companion object {
        private const val BASE_URL = "http://129.146.23.142:8080"

        fun create(
            recipesRepository: OfflineRecipesRepository,
            itemsRepository: ItemsRepository,
        ): OnlineRecipesRepository {
            return OnlineRecipesRepository(recipesRepository)
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

//    suspend fun syncRecipesFromServer(userId: Int) = withContext(Dispatchers.IO) {
//        Log.d("OnlineRecipes", "Start sync recipes for userId=$userId")
//
//        val request = Request.Builder()
//            .url("$BASE_URL/users/$userId/recipes")
//            .get()
//            .build()
//
//        client.newCall(request).execute().use { response ->
//            val responseBody = response.body?.string() ?: "{}"
//            Log.d("OnlineRecipes", "Sync response code: ${response.code}, body: $responseBody")
//
//            if (!response.isSuccessful) {
//                throw Exception("Sync failed: ${response.code} - ${response.message}")
//            }
//
//            val jsonResponse = JSONObject(responseBody)
//            val recipesArray = jsonResponse.optJSONArray("receipts") ?: JSONArray()
//
//            for (i in 0 until recipesArray.length()) {
//                val recipeJson = recipesArray.getJSONObject(i)
//
//                val recipe = Recipe(
//                    recipeId = (System.currentTimeMillis() + i), // Room sẽ tự generate
//                    dateSaved = Date(System.currentTimeMillis()),
//                    userId = userId,
//                    source = recipeJson.optString("url", ""),
//                    title = recipeJson.optString("name", "Untitled recipe"),
//                    description = "",
//                    ingredients = recipeJson.optString("ingredients", ""),
//                    instructions = recipeJson.optString("steps", ""),
//                    nutrition = "",
//                    servings = 0,
//                    totalTime = 0
//                )
//
//                recipesRepository.insertRecipe(recipe.copy(recipeId = 0))
//                Log.d("OnlineRecipes", "Upserted recipe: ${recipe.title}")
//            }
//
//            Log.d("OnlineRecipes", "Sync completed. Imported ${recipesArray.length()} recipes")
//        }
//    }

    /**
     * API POST /search-recipe
     * @param ingredients
     * @param country
     * @param style       (healthy, quick, vegetarian, …) null or empty
     * @return List<Recipe>
     */
    suspend fun searchRecipes(
        userId: Int,
        ingredients: List<String>,
        country: String? = null,
        style: String? = null
    ): List<Recipe> = withContext(Dispatchers.IO) {
        require(ingredients.isNotEmpty()) { "Ingredients cannot be empty" }
        if (userId == 0) {
            Log.w("OnlineRecipes", "Sync aborted: userId is not available.")
            return@withContext emptyList()
        }
        Log.d("OnlineRecipes", "Start search recipes for userId=$userId")
        Log.d(
            "OnlineRecipes",
            "Searching recipes with ingredients: $ingredients, country=$country, style=$style"
        )

        val jsonBody = JSONObject().apply {
            put("ingredients", JSONArray(ingredients))
            country?.takeIf { it.isNotBlank() }?.let { put("country", it) }
            style?.takeIf { it.isNotBlank() }?.let { put("style", it) }
        }

        val body = jsonBody.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("https://recipesearchapi.onrender.com/search-recipe")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: "{}"
            Log.d("OnlineRecipes", "Search response code: ${response.code}, body: $responseBody")

            if (!response.isSuccessful) {
                throw Exception("Search failed: ${response.code} - ${response.message}")
            }

            val jsonResponse = JSONObject(responseBody)
            val recipesArray = jsonResponse.optJSONArray("recipes") ?: JSONArray()
            val savedRecipes = mutableListOf<Recipe>()

            for (i in 0 until recipesArray.length()) {
                val item = recipesArray.getJSONObject(i)

                val recipe = Recipe(
                    dateSaved = Date(System.currentTimeMillis()),
                    userId = userId,
                    source = item.optString("url", ""),
                    title = item.optString("name", "Recipe $i"),
                    description = "",
                    ingredients = item.optJSONArray("ingredients")
                        ?.let { arr -> (0 until arr.length()).joinToString("\n") { arr.getString(it) } }
                        ?: "",
                    instructions = item.optJSONArray("steps")
                        ?.let { arr ->
                            (0 until arr.length()).joinToString("\n") {
                                "• " + arr.getString(
                                    it
                                )
                            }
                        }
                        ?: "",
                    nutrition = "",
                    servings = 0,
                    totalTime = 0
                )
                Log.d("OnlineRecipes", "inserting recipe: ${recipe.title}")
                val newRecipeId = recipesRepository.insertRecipe(recipe)
                recipesRepository.getRecipeStream(newRecipeId.toInt()).first()
                recipe.recipeId = newRecipeId.toInt()
                Log.d("OnlineRecipes", "inserted recipe: ${newRecipeId}")

                savedRecipes.add(recipe)

                Log.d("OnlineRecipes", "Saved searched recipe: ${recipe.title}")
            }

            Log.d("OnlineRecipes", "Search completed, found ${savedRecipes.size} recipes")
            return@withContext savedRecipes
        }
    }


    suspend fun searchRecipeAi(
        userId: Int,
        ingredients: List<String>,
        country: String? = null,
        style: String? = null,
        filters: List<String> = emptyList(),
    ): List<Recipe> = withContext(Dispatchers.IO) {
        require(ingredients.isNotEmpty()) { "Ingredients cannot be empty" }
        if (userId == 0) {
            Log.w("OnlineRecipes", "AI search aborted: userId is not available.")
            return@withContext emptyList()
        }

        Log.d("OnlineRecipes", "Starting AI recipe search for userId=$userId")
        Log.d(
            "OnlineRecipes",
            "Ingredients: $ingredients, country=$country, style=$style, filters=$filters"
        )

        val prompt = buildAiPrompt(
            ingredients = ingredients,
            country = if (country == "Any") null else country,
            style = if (style == "Any") null else style,
            filters = filters
        )

        Log.d("RecipeViewModel", "AI prompt:\n$prompt")

        // 1. Gọi OpenRouter
        val request = OrChatRequest(
            model = "openai/gpt-3.5-turbo", // hoặc model mạnh hơn nếu bạn muốn
            messages = listOf(
                OrMessage(role = "system", content = "You are a helpful cooking assistant."),
                OrMessage(role = "user", content = prompt)
            ),
            responseFormat = OrResponseFormat(type = "json_object"),
            temperature = 0.7
        )

        val response = try {
            OpenRouterClient.api.chatCompletion(request)
        } catch (e: Exception) {
            Log.e("OnlineRecipes", "OpenRouter API error", e)
            throw e
        }

        val rawContent = response.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("Empty response from AI")

        Log.d("RecipeViewModel", "Raw AI response (first 500 chars):\n${rawContent.take(500)}")

        // 2. Làm sạch ```json ... ```
        val cleanJson = rawContent
            .removeSurrounding("```json", "```")   // Xử lý cả ```json và ```
            .removePrefix("json")
            .trim()

        if (!cleanJson.startsWith("{")) {
            Log.e("RecipeViewModel", "Cleaned JSON is still invalid:\n$cleanJson")
            throw IllegalStateException("AI did not return valid JSON")
        }

        // 3. Parse JSON by Gson
        val gson = Gson()
        val aiResponse = gson.fromJson(cleanJson, AiRecipeList::class.java)

        if (aiResponse.recipes.isNullOrEmpty()) {
            Log.w("OnlineRecipes", "AI returned empty recipe list")
            return@withContext emptyList()
        }

        val savedRecipes = mutableListOf<Recipe>()

        // 4. Map to Recipe
        aiResponse.recipes.forEachIndexed { index, aiRecipe ->
            val recipe = Recipe(
                recipeId = 0, // Room sẽ tự generate
                dateSaved = Date(System.currentTimeMillis()),
                userId = userId,
                source = aiRecipe.sourceUrl ?: "",
                title = aiRecipe.name ?: "AI Recipe ${index + 1}",
                description = aiRecipe.description ?: "",
                ingredients = aiRecipe.ingredients?.joinToString("\n") { "• $it" } ?: "",
                instructions = aiRecipe.instructions?.mapIndexed { i, step -> "${i + 1}. $step" }
                    ?.joinToString("\n") ?: "",
                nutrition = Gson().toJson(
                    Nutrition(
                        calories = aiRecipe.calories ?: "",
                        protein  = aiRecipe.protein  ?: "",
                        carbs    = aiRecipe.carbs    ?: "",
                        fat      = aiRecipe.fat      ?: ""
                    )
                ),
                servings = aiRecipe.servings ?: 4,
                totalTime = aiRecipe.time_minutes ?: 30
            )

            val newId = recipesRepository.insertRecipe(recipe)
            savedRecipes.add(recipe.copy(recipeId = newId.toInt()))
            Log.d("OnlineRecipes", "Saved AI recipe: ${recipe.title} (id=$newId)")
        }
        Log.d("OnlineRecipes", "AI search completed, saved ${savedRecipes.size} recipes")
        return@withContext savedRecipes
    }


    private fun buildAiPrompt(
        ingredients: List<String>,
        country: String?,
        style: String?,
        filters: List<String>
    ): String {
        val sb = StringBuilder()
        sb.appendLine("You are helping a home cook plan meals using only ingredients from their pantry.")
        sb.appendLine()
        sb.appendLine("Available ingredients:")
        ingredients.forEach { sb.appendLine("- $it") }
        sb.appendLine()
        sb.appendLine("Generate 4 creative recipes in JSON format with this schema:")
        sb.appendLine(
            """
        {
          "recipes": [
            {
              "name": "string",
              "description": "short description",
              "time_minutes": 30,
              "servings": 4,
              "sourceUrl": "example.com",
              "calories": "approx, like '450 kcal'",
              "protein": "e.g. '25g'",
              "carbs": "e.g. '40g'",
              "fat": "e.g. '15g'",
              "ingredients": [
                "quantity unit ingredient, like 3 pounds russet potatoes",
                "quantity unit ingredient, like 1 cup mayonnaise",
              ],
              "instructions": [
                "...",
                "...",
                "..."
              ],
            }
          ]
        }
        """.trimIndent()
        )

        country?.let {
            sb.appendLine()
            sb.appendLine("Preferred cuisine: $it.")
        }

        style?.let {
            sb.appendLine("Preferred cooking style: $it.")
        }

        if (filters.isNotEmpty()) {
            sb.appendLine("Additional preferences: ${filters.joinToString(", ")}.")
        }

        sb.appendLine()
        sb.appendLine("Rules:")
        sb.appendLine("- Only use ingredients from the pantry list where possible.")
        sb.appendLine("- If something is missing, assume basic staples like salt, pepper, oil, water are available.")
        sb.appendLine("- Return ONLY valid JSON. No extra text, no explanations.")
        sb.appendLine("- For each recipe, include an 'ingredients' array with one string per ingredient, starting with a quantity and unit (e.g. '1 cup milk', '2 tbsp oil').")
        sb.appendLine("- For each recipe, include 4–10 clear steps in the 'instructions' array.")
        sb.appendLine("- Every ingredient used in 'instructions' must appear in 'ingredients', and vice versa.")
        sb.appendLine("- Return ONLY valid JSON. No extra text, no explanations.")

        return sb.toString()
    }
}