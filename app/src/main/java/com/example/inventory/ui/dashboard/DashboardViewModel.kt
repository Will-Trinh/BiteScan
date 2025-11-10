package com.example.inventory.ui.dashboard

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
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Color

data class DashboardMetrics(
    val calories: Int = 0,
    val protein: Int = 0,
    val spend: Double = 0.0,
    val items: Int = 0
)

data class MacroBreakdown(
    val proteinPercent: String = "0%",
    val carbsPercent: String = "0%",
    val fatsPercent: String = "0%"
)

data class SpendingByCategory(
    val categories: List<Pair<String, Float>> = emptyList()
)

data class Insight(
    val title: String = "",
    val message: String = "",
    val color: Color = Color.Unspecified,
    val backgroundColor: Color = Color.Unspecified
)

data class RecipeSuggestion(
    val title: String = "",
    val details: String = "",
    val iconResId: Int = 0
)

private data class MacroTotals(val protein: Double, val carbs: Double, val fats: Double)



class DashboardViewModel(
    private val usersRepository: OfflineUsersRepository,
    private val itemsRepository: ItemsRepository
) : ViewModel() {

    private val _currentUserId = MutableStateFlow(0)
    private val _metrics = MutableStateFlow(DashboardMetrics())
    val metrics: StateFlow<DashboardMetrics> = _metrics.asStateFlow()

    private val _macroBreakdown = MutableStateFlow(MacroBreakdown())
    val macroBreakdown: StateFlow<MacroBreakdown> = _macroBreakdown.asStateFlow()

    private val _spendingByCategory = MutableStateFlow(SpendingByCategory())
    val spendingByCategory: StateFlow<SpendingByCategory> = _spendingByCategory.asStateFlow()

    private val _insights = MutableStateFlow<List<Insight>>(emptyList())
    val insights: StateFlow<List<Insight>> = _insights.asStateFlow()

    private val _recipes = MutableStateFlow<List<RecipeSuggestion>>(emptyList())
    val recipes: StateFlow<List<RecipeSuggestion>> = _recipes.asStateFlow()


    fun setCurrentUserId(userId: Int) {
        _currentUserId.value = userId
        loadUserData(userId)
    }

    private fun loadUserData(userId: Int) {
        viewModelScope.launch {
            val userItemsFlow = itemsRepository.getItemsForUser(userId)
            val userItems = userItemsFlow.first()

            if (userItems.isEmpty()) {
                // Fallback placeholders
                _metrics.value = DashboardMetrics(1847, 142, 19.76, 4)
                _macroBreakdown.value = MacroBreakdown("30%", "45%", "25%")
                _spendingByCategory.value = SpendingByCategory(
                    listOf("Produce" to 0.8f, "Meat" to 0.4f, "Dairy" to 0.7f, "Grains" to 0.6f)
                )
                _insights.value = listOf(
                    Insight(
                        title = "Get started",
                        message = "Add some receipts to see personalized insights.",
                        color = Color(0xFF4CAF50).copy(alpha = 0.8f),
                        backgroundColor = Color(0xFFE8F5E9)
                    )
                )
                _recipes.value = listOf(
                    RecipeSuggestion(
                        title = "Sample Recipe",
                        details = "Uses 0 of 0 items • 10 min",
                    )
                )
                return@launch
            }

            // 1. Derive categories if not present (based on OCR item names)
            val itemsWithCategory = userItems.map { item ->
                item.copy(category = deriveCategory(item.name))
            }

            val totalSpend = itemsWithCategory.sumOf { it.price * it.quantity.toDouble() }
            val totalItemsCount = itemsWithCategory.size
            val calories = itemsWithCategory.sumOf { it.calories ?: 0.0 }.toInt()
            val protein = itemsWithCategory.sumOf { it.protein ?: 0.0 }.toInt()
            _metrics.value = DashboardMetrics(
                calories = calories,
                protein = protein,
                spend = totalSpend,
                items = totalItemsCount
            )

            // 2. Group by derived category for macros and spending
            val categoryMacros = itemsWithCategory.groupBy { it.category }
            _macroBreakdown.value = computeMacroBreakdown(itemsWithCategory)

            val categorySpend = categoryMacros.mapValues { entry ->
                entry.value.sumOf { item -> item.price * item.quantity.toDouble() }
            }
            _spendingByCategory.value = SpendingByCategory(
                categories = if (totalSpend > 0) {
                    categorySpend.map { (cat, spend) -> cat to (spend / totalSpend).toFloat() }
                } else emptyList()
            )

            // 3. Insights based on top category
            val topCategory = categoryMacros.maxByOrNull { it.value.size }?.key ?: ""
            _insights.value = listOf(
                if (topCategory.lowercase().contains("produce")) {
                    Insight(
                        title = "Great produce choices",
                        message = "Items like your $topCategory are packed with iron and folate - perfect for your fitness goals.",
                        color = Color(0xFF4CAF50).copy(alpha = 0.8f),
                        backgroundColor = Color(0xFFE8F5E9)
                    )
                } else {
                    Insight(
                        title = "Higher variety than last time",
                        message = "You're getting ${((totalItemsCount / 4.0) * 100).toInt()}% more items compared to average.",
                        color = Color(0xFFF4A142),
                        backgroundColor = Color(0xFFFFF3E0)
                    )
                },
                Insight(
                    title = "Budget insight",
                    message = "Total spend: $${String.format("%.2f", totalSpend)} across ${categorySpend.size} categories.",
                    color = Color(0xFF2196F3),
                    backgroundColor = Color(0xFFE3F2FD)
                )
            )

            _recipes.value = suggestRecipesFromItems(itemsWithCategory)
        }
    }

    private fun deriveCategory(itemName: String): String {
        val lowerName = itemName.lowercase().trim()
        return when {
            lowerName.contains("banana") || lowerName.contains("apple") || lowerName.contains("strawberry") || lowerName.contains("avocado") || lowerName.contains("tomato") -> "Fruit"
            lowerName.contains("potato") || lowerName.contains("carrot") || lowerName.contains("cabbage") -> "Produce"
            lowerName.contains("milk") || lowerName.contains("cheese") || lowerName.contains("egg") || lowerName.contains("yogurt") || lowerName.contains("mozzarella") -> "Dairy"
            lowerName.contains("chicken") || lowerName.contains("breast") || lowerName.contains("meatball") || lowerName.contains("ground beef") || lowerName.contains("beef") || lowerName.contains("tuna") -> "Meat"
            lowerName.contains("granola") || lowerName.contains("bread") || lowerName.contains("rice") -> "Grains"
            else -> "Unknown"
        }
    }

    private fun suggestRecipesFromItems(items: List<Item>): List<RecipeSuggestion> {
        val topItem = items.groupBy { it.name }.maxByOrNull { it.value.size }?.key ?: "ingredients"
        val topCategory = items.groupBy { it.category }.maxByOrNull { it.value.size }?.key ?: ""
        return listOf(
            RecipeSuggestion(
                title = "${topCategory} Power Bowl",
                details = "Uses ${items.size} of ${items.size + 2} items • 25 min",
            ),
            RecipeSuggestion(
                title = "Quick $topItem Salad",
                details = "Uses ${min(items.size, 5)} of 8 items • 10 min",
            )
        )
    }
}

private fun computeMacroBreakdown(items: List<Item>): MacroBreakdown {
    // 1) Try real grams first
    val realProtein = items.sumOf { it.protein ?: 0.0 }
    val realCarbs   = items.sumOf { it.carbs   ?: 0.0 }
    val realFats    = items.sumOf { it.fats    ?: 0.0 }

    val hasReal = (realProtein + realCarbs + realFats) > 0.0

    val (p, c, f) = if (hasReal) {
        MacroTotals(realProtein, realCarbs, realFats)
    } else {
        // 2) Fallback: estimate grams from category composition (weights per item).
        val weight = mapOf(
            "Meat"    to MacroTotals(protein = 25.0, carbs = 0.0,  fats = 8.0),
            "Dairy"   to MacroTotals(protein = 8.0,  carbs = 5.0, fats = 5.0),
            "Grains"  to MacroTotals(protein = 5.0,  carbs = 30.0, fats = 2.0),
            "Fruit"   to MacroTotals(protein = 1.0,  carbs = 20.0, fats = 0.0),
            "Produce" to MacroTotals(protein = 1.0,  carbs = 10.0, fats = 0.0)
        )

        items.groupBy { it.category }
            .values
            .map { group ->
                val key = group.firstOrNull()?.category ?: "Unknown"
                val w = weight[key] ?: MacroTotals(2.0, 8.0, 2.0) // fallback for Unknown
                MacroTotals(
                    protein = w.protein * group.size,
                    carbs   = w.carbs   * group.size,
                    fats    = w.fats    * group.size
                )
            }
            .fold(MacroTotals(0.0, 0.0, 0.0)) { acc, t ->
                MacroTotals(acc.protein + t.protein, acc.carbs + t.carbs, acc.fats + t.fats)
            }
    }

    // 3) Convert to percentages that add to 100
    val total = (p + c + f).coerceAtLeast(1.0)
    var pp = ((p / total) * 100.0).roundToInt()
    var cc = ((c / total) * 100.0).roundToInt()
    var ff = 100 - pp - cc
    if (ff < 0) { ff = 0; cc = (100 - pp).coerceAtLeast(0) }

    return MacroBreakdown(
        proteinPercent = "$pp%",
        carbsPercent   = "$cc%",
        fatsPercent    = "$ff%"
    )
}