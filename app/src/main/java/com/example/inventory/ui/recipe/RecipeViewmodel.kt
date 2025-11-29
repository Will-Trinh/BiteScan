package com.example.inventory.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.*
import com.example.inventory.ui.AppViewModel
import android.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.inventory.ui.settings.MyPantryViewModel
class RecipeViewModel(
    private val onlineRecipesRepository: OnlineRecipesRepository,
    private val myPantryViewModel: MyPantryViewModel,
    private val appViewModel: AppViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RecipeUiState(
            allFilters = listOf("High Protein", "Vegetarian", "Vegan", "Low Carb", "Quick (<30m)", "Italian", "Asian", "Healthy"),
            selectedFilters = emptySet()
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(isLoading = true)}
        observeAvailableIngredients()
    }

    private fun observeAvailableIngredients() {
        viewModelScope.launch {
            Log.d("RecipeViewModel", "observeAvailableIngredients...")
            myPantryViewModel.availableIngredientNames
                .collect { ingredients ->
                    if (ingredients.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                recipes = emptyList(),
                                availableIngredients = emptyList(),
                                errorMessage = "No ingredients in pantry. Add items to get recipe suggestions!"
                            )
                        }
                    } else {
                        searchAndDisplayRecipes(ingredients)
                    }
                }
        }
    }

    fun refreshRecommendations() {
        val userId = appViewModel.userId.value ?:0
        myPantryViewModel.loadPantryItems(userId)
    }

    private suspend fun searchAndDisplayRecipes(availableIngredients: List<String>) {
        try {
            Log.d("RecipeViewModel", "Searching recipes for ingredients: $availableIngredients")
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            Log.d("RecipeViewModel", "Searching recipes ....: $availableIngredients")

            val foundRecipes = onlineRecipesRepository.searchRecipes(
                ingredients = availableIngredients,
                country = "Hong Kong", //ToDo: Get user's country
                style = "healthy" //Todo: Get user's dietary preferences
            )

            if (foundRecipes.isEmpty()) {
                _uiState.update {
                    it.copy(
                        recipes = emptyList(),
                        availableIngredients = availableIngredients.take(25),
                        isLoading = false,
                        errorMessage = "No recipes found. Try adding more common ingredients like chicken, rice, eggs..."
                    )
                }
                return
            }

            val uiRecipes = foundRecipes.mapIndexed {index, recipe ->
                val matchedCount = 0

                RecipeUiModel(
                    id = index.toLong(),
                    name = recipe.title,
                    subtitle = recipe.source,
//                    subtitle = recipe.source.let {
//                        when {
//                            it.isBlank() -> "AI Suggested Recipe"
//                            it.contains("tasty.co", ignoreCase = true) -> "From Tasty"
//                            it.contains("allrecipes.com", ignoreCase = true) -> "From AllRecipes"
//                            else -> "View Source →"
//                        }
//                    },
                    time = "25–45 min",
                    servings = "4",
                    calories = "N/A",
                    protein = "N/A",
                    carbs = "N/A",
                    fat = "N/A",
                    ingredientUsage = "N/A",
//                    ingredientUsage = when {
//                        matchedCount >= 8 -> "Perfect match! Uses $matchedCount ingredients"
//                        matchedCount >= 5 -> "Great! Uses $matchedCount ingredients"
//                        matchedCount >= 3 -> "Good! Uses $matchedCount ingredients"
//                        matchedCount >= 1 -> "Uses $matchedCount ingredient"
//                        else -> "Worth trying!"
//                    },
                    sourceUrl = recipe.source
                )
            }
//                .sortedByDescending {
//                    it.ingredientUsage.let { str ->
//                        str.substringAfter("Uses ").substringBefore(" ").toIntOrNull() ?: 0
//                    }
//                }

            _uiState.update {
                it.copy(
                    recipes = uiRecipes,
                    availableIngredients = availableIngredients.take(25),
                    isLoading = false
                )
            }

        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Network error. Please check your connection and try again."
                )
            }
        }
    }

    private fun calculateMatchedIngredients(recipe: Recipe, available: List<String>): Int {
        if (recipe.ingredients.isBlank()) return 0

        Log.d("RecipeViewModel", "calculateMatchedIngredients: $recipe")

//        val recipeItems = recipe.ingredients
//            .split("\n")
//            .map { it.trim().lowercase() }
//            .filter { it.isNotBlank() && !it.startsWith("•") }

//        return recipeItems.count { ing ->
//            available.any { avail ->
//                avail.contains(ing) || ing.contains(avail) ||
//                        ing.contains("oil") && avail.contains("oil") ||
//                        ing.contains("salt") || ing.contains("pepper")
//            }
//        }.coerceAtMost(recipeItems.size)
        return 0
    }

    fun toggleFilter(filter: String) {
        _uiState.update {
            val set = it.selectedFilters.toMutableSet()
            if (set.contains(filter)) set.remove(filter) else set.add(filter)
            it.copy(selectedFilters = set)
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val currentIngredients = _uiState.value.availableIngredients
        if (currentIngredients.isNotEmpty()) {
            viewModelScope.launch {
                searchAndDisplayRecipes(currentIngredients)
            }
        }
    }
}


data class RecipeUiState(
    val availableIngredients: List<String> = emptyList(),
    val allFilters: List<String> = emptyList(),
    val selectedFilters: Set<String> = emptySet(),
    val recipes: List<RecipeUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class RecipeUiModel(
    val id: Long,
    val name: String,
    val subtitle: String,
    val time: String,
    val servings: String,
    val calories: String,
    val protein: String,
    val carbs: String,
    val fat: String,
    val ingredientUsage: String,
    val sourceUrl: String = ""
)