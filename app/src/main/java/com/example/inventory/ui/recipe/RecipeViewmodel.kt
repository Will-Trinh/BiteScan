package com.example.inventory.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.OnlineRecipesRepository
import com.example.inventory.data.Recipe
import com.example.inventory.ui.AppViewModel
import com.example.inventory.ui.settings.MyPantryViewModel
import android.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RecipeViewModel(
    private val onlineRecipesRepository: OnlineRecipesRepository,
    private val myPantryViewModel: MyPantryViewModel,
    private val appViewModel: AppViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RecipeUiState(
            allFilters = listOf("High Protein", "Vegetarian", "Vegan", "Low Carb", "Quick (<30m)", "Italian", "Asian", "Healthy"),
            selectedFilters = emptySet(),
            excludedIngredients = emptySet()
        )
    )
    val uiState = _uiState.asStateFlow()

    // Lưu toàn bộ ingredients hiện tại từ pantry
    private var currentAllIngredients: List<String> = emptyList()

    init {
        _uiState.update { it.copy(isLoading = true) }
        observeAvailableIngredients()
    }

    private fun observeAvailableIngredients() {
        viewModelScope.launch {
            myPantryViewModel.availableIngredientNames.collect { ingredients ->
                currentAllIngredients = ingredients

                // Tự động loại bỏ những ingredient đã exclude nhưng không còn trong pantry
                val currentExcluded = _uiState.value.excludedIngredients
                val validExcluded = currentExcluded.intersect(ingredients.toSet())

                _uiState.update {
                    it.copy(
                        availableIngredients = ingredients,
                        excludedIngredients = validExcluded,
                        recipes = emptyList(), // reset khi pantry thay đổi
                        errorMessage = null,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refreshRecommendations() {
        val userId = appViewModel.userId.value ?: 0
        myPantryViewModel.loadPantryItems(userId)
    }

    // button search
    fun findRecipesWithAI() {
        val includedIngredients = currentAllIngredients.filter {
            it !in _uiState.value.excludedIngredients
        }

        if (includedIngredients.isEmpty()) {
            _uiState.update {
                it.copy(
                    recipes = emptyList(),
                    isLoading = false,
                    errorMessage = "Please select at least one ingredient to find recipes."
                )
            }
            return
        }

        viewModelScope.launch {
            searchAndDisplayRecipes(includedIngredients)
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

    private suspend fun searchAndDisplayRecipes(availableIngredients: List<String>) {
        try {
            Log.d("RecipeViewModel", "Searching recipes for: $availableIngredients")
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val foundRecipes = onlineRecipesRepository?.searchRecipes(
                ingredients = availableIngredients,
                country = "Hong Kong",
                style = "healthy"
            )

            if (foundRecipes?.isEmpty() ?: true) {
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
                it.copy(
                    recipes = uiRecipes,
                    availableIngredients = availableIngredients.take(25),
                    isLoading = false
                )
            }

        } catch (e: Exception) {
            Log.e("RecipeViewModel", "Error searching recipes", e)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Network error. Please check your connection and try again."
                )
            }
        }
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
        val included = currentAllIngredients.filter { it !in _uiState.value.excludedIngredients }
        if (included.isNotEmpty()) {
            viewModelScope.launch { searchAndDisplayRecipes(included) }
        }
    }
}

data class RecipeUiState(
    val availableIngredients: List<String> = emptyList(),
    val excludedIngredients: Set<String> = emptySet(), // MỚI
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