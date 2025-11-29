package com.example.inventory.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.sql.Date
import java.util.concurrent.TimeUnit

class OnlineRecipesRepository(
    private val recipesRepository: OfflineRecipesRepository,
    private val itemsRepository: ItemsRepository,
) {
    companion object {
        private const val BASE_URL = "http://129.146.23.142:8080"

        fun create(
            recipesRepository: OfflineRecipesRepository,
            itemsRepository: ItemsRepository
        ): OnlineRecipesRepository {
            return OnlineRecipesRepository(recipesRepository, itemsRepository)
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun syncRecipesFromServer(userId: Int) = withContext(Dispatchers.IO) {
        Log.d("OnlineRecipes", "Start sync recipes for userId=$userId")

        val request = Request.Builder()
            .url("$BASE_URL/users/$userId/recipes")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: "{}"
            Log.d("OnlineRecipes", "Sync response code: ${response.code}, body: $responseBody")

            if (!response.isSuccessful) {
                throw Exception("Sync failed: ${response.code} - ${response.message}")
            }

            val jsonResponse = JSONObject(responseBody)
            val recipesArray = jsonResponse.optJSONArray("receipts") ?: JSONArray()

            for (i in 0 until recipesArray.length()) {
                val recipeJson = recipesArray.getJSONObject(i)

                val recipe = Recipe(
                    recipeId = (System.currentTimeMillis() + i), // Room sẽ tự generate
                    dateSaved = Date(System.currentTimeMillis()),
                    userId = userId,
                    source = recipeJson.optString("url", ""),
                    title = recipeJson.optString("name", "Untitled recipe"),
                    description = "",
                    ingredients = recipeJson.optString("ingredients", ""),
                    instructions = recipeJson.optString("steps", ""),
                    nutrition = "",
                    totalTime = 0
                )

                recipesRepository.insertRecipe(recipe.copy(recipeId = 0))
                Log.d("OnlineRecipes", "Upserted recipe: ${recipe.title}")
            }

            Log.d("OnlineRecipes", "Sync completed. Imported ${recipesArray.length()} recipes")
        }
    }

    /**
     * API POST /search-recipe
     * @param ingredients
     * @param country
     * @param style       (healthy, quick, vegetarian, …) null or empty
     * @return List<Recipe>
     */
    suspend fun searchRecipes(
        ingredients: List<String>,
        country: String? = null,
        style: String? = null
    ): List<Recipe> = withContext(Dispatchers.IO) {
        require(ingredients.isNotEmpty()) { "Ingredients cannot be empty" }

        Log.d("OnlineRecipes", "Searching recipes with ingredients: $ingredients, country=$country, style=$style")

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
                    recipeId = System.currentTimeMillis() + i,
                    dateSaved = Date(System.currentTimeMillis()),
                    userId = 0,
                    source = item.optString("url", ""),
                    title = item.optString("name", "Recipe $i"),
                    description = "",
                    ingredients = item.optJSONArray("ingredients")
                        ?.let { arr -> (0 until arr.length()).joinToString("\n") { arr.getString(it) } }
                        ?: "",
                    instructions = item.optJSONArray("steps")
                        ?.let { arr -> (0 until arr.length()).joinToString("\n") { "• " + arr.getString(it) } }
                        ?: "",
                    nutrition = "",
                    totalTime = 0
                )

                //recipesRepository.insertRecipe(recipe.copy(recipeId = 0))
                savedRecipes.add(recipe)

                Log.d("OnlineRecipes", "Saved searched recipe: ${recipe.title}")
            }

            Log.d("OnlineRecipes", "Search completed, found ${savedRecipes.size} recipes")
            return@withContext savedRecipes
        }
    }
}