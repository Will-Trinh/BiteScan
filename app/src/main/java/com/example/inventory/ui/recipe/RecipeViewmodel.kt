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
import com.google.gson.Gson
import com.example.inventory.data.Nutrition

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
        viewModelScope.launch {
            appViewModel.userDiet.collect { diet ->
                Log.d("RecipeViewModel", "Diet updated from AppViewModel: $diet")
                _uiState.update { it.copy(selectedFilters = diet) }
            }
        }
        appViewModel.loadUserDiet()
    }

    fun refresh() {
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
            val newValue = if (_uiState.value.selectedFilters == filter) "Any" else filter
            _uiState.value.copy(selectedFilters = newValue)
        }
    }
    fun findRecipesWithAi() {
        Log.d("RecipeViewModel", "findRecipesWithAi() called")
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
            _uiState.update { it.copy(isLoading = true, errorMessage = null, recipes = emptyList()) }

            val country = _uiState.value.selectedCountry
            val style = _uiState.value.selectedStyle
            val filters = _uiState.value.selectedFilters

            try {
                Log.d("RecipeViewModel", "Searching recipes with ingredients: $includedIngredients")
                val aiList = onlineRecipesRepository.searchRecipeAi(
                    userId = appViewModel.userId.value ?: 0,
                    ingredients = includedIngredients,
                    country = country,
                    style = style,
                    filters = filters
                )
                Log.d("RecipeViewModel", "Found recipes: $aiList")
                if (aiList.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            recipes = emptyList(),
                            isLoading = false,
                            errorMessage = "No recipes found. Try different ingredients or remove some filters."
                        )
                    }
                    return@launch
                }
                // 5) Map to UI model (unchanged)
                val uiRecipes = aiList.map { recipe ->
                    val nutrition = parseNutrition(recipe.nutrition)
                    RecipeUiModel(
                        id = recipe.recipeId,
                        name = recipe.title,
                        subtitle = recipe.description,
                        time = "${recipe.totalTime} min",
                        servings = recipe.servings.toString(),
                        calories = nutrition.calories.ifBlank { "N/A" },
                        protein  = nutrition.protein.ifBlank { "N/A" },
                        carbs    = nutrition.carbs.ifBlank { "N/A" },
                        fat      = nutrition.fat.ifBlank { "N/A" },
                        ingredientUsage = "AI from your pantry",
                        sourceUrl = recipe.source,
                    )
                }
                
                _uiState.update {
                    it.copy(recipes = uiRecipes, isLoading = false)
                }
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Error generating AI recipes", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "AI error: ${e.message ?: "Unknown error."}"
                    )
                }
            }
        }
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
                    userId = appViewModel.userId.value ?: 0,
                    ingredients = includedIngredients,
                    country = country,
                    style = style
                )
                Log.d("RecipeViewModel", "Found recipes: $foundRecipes")

                if (foundRecipes.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            recipes = emptyList(),
                            isLoading = false,
                            errorMessage = "No recipes found. Try different ingredients or remove some filters."
                        )
                    }
                    return@launch
                }

                val uiRecipes = foundRecipes.map{ recipe ->
                    RecipeUiModel(
                        id = recipe.recipeId,
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
     "Vegetarian", "Vegan", "Low Carb"
        , "Healthy", "gluten-free"
    ),

    val selectedFilters: String = "Any",
    val recipes: List<RecipeUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

private fun parseNutrition(json: String?): Nutrition {
    if (json.isNullOrBlank()) {
        return Nutrition("N/A", "N/A", "N/A", "N/A")
    }

    return try {
        Gson().fromJson(json, Nutrition::class.java)
    } catch (e: Exception) {
        Nutrition("N/A", "N/A", "N/A", "N/A")
    }
}



data class RecipeUiModel(
    val id: Int,
    val name: String,
    val subtitle: String,
    val time: String,
    val servings: String,
    val calories: String,
    val protein: String,
    val carbs: String,
    val fat: String,
    val ingredientUsage: String,
    val sourceUrl: String = "",
    val ingredients: String = "",
    val instructions: List<String> = emptyList()
)