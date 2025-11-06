/*
- Recipe Viewmodel receive the data from the api and save to client data
- Recipe viewmodel will check the recipe from server to make sure the recipe is in the correct form.
- Recipe viewmodel will update recipe screen with the overview of the valid recipe.
- Recipe viewmodel need to get data from receipt such as item inside and receipt ID, also it should have syn function to update the items for use in the receipt.
- Also, recipe viewmodel should have a link to account to show limited recipe for type of user.
*/

package com.example.inventory.ui.recipe

import androidx.compose.foundation.layout.add
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import com.example.inventory.data.OfflineUsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.min
import com.example.inventory.R
import kotlin.collections.remove
import kotlin.text.contains

// --- Data & ViewModel (To be moved to separate files) ---

data class Recipe(
    val id: Int,
    val name: String,
    val subtitle: String,
    val time: String,
    val servings: String,
    val calories: String,
    val protein: String,
    val carbs: String,
    val fat: String,
    val ingredientUsage: String
)

data class RecipeUiState(
    val availableIngredients: List<String> = emptyList(),
    val allFilters: List<String> = emptyList(),
    val selectedFilters: Set<String> = emptySet(),
    val recipes: List<Recipe> = emptyList()
)


class RecipeViewModel(userId: Int) : ViewModel() {
    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    init {
        // Initialize with sample data that matches the screenshot
        _uiState.value = RecipeUiState(
            availableIngredients = listOf("Chicken Breast", "Spinach", "Brown Rice", "Greek Yogurt"),
            allFilters = listOf("High Protein", "Vegetarian", "Vegan", "Low Carb", "Quick (<30m)"),
            selectedFilters = setOf("High Protein", "Quick (<30m)"),
            recipes = listOf(
                Recipe(
                    id = 1,
                    name = "Chicken & Spinach Power Bowl",
                    subtitle = "High Protein Dinner",
                    time = "25 min",
                    servings = "2",
                    calories = "420",
                    protein = "35g",
                    carbs = "28g",
                    fat = "18g",
                    ingredientUsage = "Uses 7 of 9 items"
                ),
                Recipe(
                    id = 2,
                    name = "Greek Yogurt Berry Parfait",
                    subtitle = "Healthy Breakfast",
                    time = "5 min",
                    servings = "1",
                    calories = "280",
                    protein = "20g",
                    carbs = "35g",
                    fat = "8g",
                    ingredientUsage = "Uses 3 of 5 items"
                )
            )
        )
    }

    fun removeIngredient(ingredient: String) {
        val current = _uiState.value.availableIngredients.toMutableList()
        current.remove(ingredient)
        _uiState.value = _uiState.value.copy(availableIngredients = current)
        // TODO: Trigger recipe search/update
    }

    fun toggleFilter(filter: String) {
        val current = _uiState.value.selectedFilters.toMutableSet()
        if (current.contains(filter)) {
            current.remove(filter)
        } else {
            current.add(filter)
        }
        _uiState.value = _uiState.value.copy(selectedFilters = current)
        // TODO: Trigger recipe search/update
    }
}