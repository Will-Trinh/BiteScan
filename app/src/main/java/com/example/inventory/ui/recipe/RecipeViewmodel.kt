package com.example.inventory.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import com.example.inventory.data.ReceiptsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

class RecipeViewModel(
    private val userId: Int,
    private val itemsRepository: ItemsRepository,
    private val receiptsRepository: ReceiptsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RecipeUiState(
            // Filters can stay static or be loaded later
            allFilters = listOf("High Protein", "Vegetarian", "Vegan", "Low Carb", "Quick (<30m)"),
            selectedFilters = setOf("High Protein", "Quick (<30m)")
        )
    )
    val uiState: StateFlow<RecipeUiState> = _uiState

    init {
        refreshTopPantryAndRecipes()
    }

    /** Loads pantry items from the user's receipt history and takes the Top 5 by total quantity (fallback to count). */
    private fun refreshTopPantryAndRecipes() {
        viewModelScope.launch {
            // 1) Get all receipts for the user, newest first
            val receipts = receiptsRepository.getReceiptsForUser(userId).first()
                .sortedByDescending { it.date.time }

            // 2) Flatten items across all receipts (pantry history)
            val allReceiptItems: List<Item> = receipts.flatMap { r ->
                itemsRepository.getItemsForReceipt(r.receiptId).first()
            }

            // 3) Top 5 ingredient names by total quantity (fallback to frequency)
            val top5: List<String> = allReceiptItems
                .groupBy { it.name.trim() }
                .mapValues { (_, list) ->
                    val qtySum = list.sumOf { it.quantity.toDouble() }
                    if (qtySum > 0.0) qtySum else list.size.toDouble()
                }
                .toList()
                .sortedByDescending { it.second }
                .take(5)
                .map { it.first }

            // 4) (Optional) Build/refresh recipes – keep your sample data or wire server here
            val sampleRecipes = listOf(
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

            _uiState.value = _uiState.value.copy(
                availableIngredients = top5,
                recipes = sampleRecipes
            )
        }
    }

    fun toggleFilter(filter: String) {
        val current = _uiState.value.selectedFilters.toMutableSet()
        if (current.contains(filter)) current.remove(filter) else current.add(filter)
        _uiState.value = _uiState.value.copy(selectedFilters = current)
    }
}
