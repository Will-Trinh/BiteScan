package com.example.inventory.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.OnlineRecipesRepository
import com.example.inventory.ui.AppViewModel
import com.example.inventory.ui.settings.MyPantryViewModel
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val onlineRecipesRepository: OnlineRecipesRepository,
    private val myPantryViewModel: MyPantryViewModel,
    private val appViewModel: AppViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState = _uiState.asStateFlow()

    private var currentAllIngredients: List<String> = emptyList()

    init {
        observeAvailableIngredients()
    }
    fun refreshRecommendations() {
        val userId = appViewModel.userId.value ?: 0
        myPantryViewModel.loadPantryItems(userId)
    }
    private fun observeAvailableIngredients() {
        viewModelScope.launch {
            myPantryViewModel.availableIngredientNames.collect { ingredients ->
                currentAllIngredients = ingredients

                val currentExcluded = _uiState.value.excludedIngredients
                val validExcluded = currentExcluded.intersect(ingredients.toSet())

                _uiState.update {
                    it.copy(
                        availableIngredients = ingredients,
                        excludedIngredients = validExcluded,
                        recipes = emptyList(),
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun toggleIngredientExclusion(ingredient: String) {
        _uiState.update { current ->
            val set = current.excludedIngredients.toMutableSet()
            if (set.contains(ingredient)) set.remove(ingredient)
            else set.add(ingredient)
            current.copy(excludedIngredients = set)
        }
    }

    fun selectCountry(country: String) {
        _uiState.update { it.copy(selectedCountry = country) }
    }

    fun selectStyle(style: String) {
        _uiState.update { it.copy(selectedStyle = style) }
    }

    fun toggleFilter(filter: String) {
        _uiState.update {
            val set = it.selectedFilters.toMutableSet()
            if (set.contains(filter)) set.remove(filter) else set.add(filter)
            it.copy(selectedFilters = set)
        }
    }
    fun findRecipesWithAi() {
        return
    }
    fun findRecipesWithGg() {
        val includedIngredients = currentAllIngredients.filter {
            it !in _uiState.value.excludedIngredients
        }

        if (includedIngredients.isEmpty()) {
            _uiState.update {
                it.copy(
                    recipes = emptyList(),
                    errorMessage = "Please keep at least one ingredient to search."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val country = _uiState.value.selectedCountry
            val style = _uiState.value.selectedStyle

            try {
                Log.d("RecipeViewModel", "Searching recipes with ingredients: $includedIngredients")
                val foundRecipes = onlineRecipesRepository.searchRecipes(
                    ingredients = includedIngredients,
                    country = country,
                    style = style
                )
                Log.d("RecipeViewModel", "Found recipes: $foundRecipes")

                if (foundRecipes.isNullOrEmpty()) {
                    _uiState.update {
                        it.copy(
                            recipes = emptyList(),
                            isLoading = false,
                            errorMessage = "No recipes found. Try different ingredients or remove some filters."
                        )
                    }
                    return@launch
                }

                val uiRecipes = foundRecipes.mapIndexed { index, recipe ->
                    RecipeUiModel(
                        id = index.toLong(),
                        name = recipe.title,
                        subtitle = recipe.source.ifBlank { "AI Suggested Recipe" },
                        time = "25–45 min",
                        servings = "4",
                        calories = "N/A",
                        protein = "N/A",
                        carbs = "N/A",
                        fat = "N/A",
                        ingredientUsage = "Suggested",
                        sourceUrl = recipe.source
                    )
                }

                _uiState.update {
                    it.copy(recipes = uiRecipes, isLoading = false)
                }

            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Error searching recipes", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Network error. Please check your connection. I'm Old"
                    )
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}

data class RecipeUiState(
    val allCountries: List<String> = listOf("Any") + listOf(
        "Vietnamese", "Chinese", "Japanese", "Korean",
        "Thai", "Indian", "Italian", "Mexican", "French", "American"
    ),
    val selectedCountry: String = "Any",

    val allStyles: List<String> = listOf("Any") + listOf(
        "Stir-fry", "Grilled", "Steamed", "Soup",
        "Salad", "Baked", "Fried", "One-pot", "No-cook"
    ),
    val selectedStyle: String = "Any",

    val availableIngredients: List<String> = emptyList(),
    val excludedIngredients: Set<String> = emptySet(),
    val allFilters: List<String> = listOf(
        "High Protein", "Vegetarian", "Vegan", "Low Carb",
        "Quick (<30m)", "Healthy"
    ),
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