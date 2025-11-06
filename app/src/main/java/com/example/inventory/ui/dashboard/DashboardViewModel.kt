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
import com.example.inventory.R

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
    val color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    val backgroundColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified
)

data class RecipeSuggestion(
    val title: String = "",
    val details: String = "",
    val iconResId: Int = 0
)

class DashboardViewModel(
    private val usersRepository: OfflineUsersRepository,
    private val itemsRepository: ItemsRepository
) : ViewModel() {

    private val _currentUserId = MutableStateFlow(0)
    val currentUserId: StateFlow<Int> = _currentUserId.asStateFlow()

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
                        color = androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.8f),
                        backgroundColor = androidx.compose.ui.graphics.Color(0xFFE8F5E9)
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

            val totalSpend = userItems.sumOf { it.price * it.quantity.toDouble() }
            val totalItemsCount = userItems.size
            val calories =  userItems.sumOf { it.calories ?: 0.0 }.toInt()
            val fats =  userItems.sumOf { it.fats ?: 0.0 }.toInt()
            val carbs =  userItems.sumOf { it.carbs ?: 0.0 }.toInt()
            val protein = userItems.sumOf { it.protein ?: 0.0 }.toInt()
            _metrics.value = DashboardMetrics(
                calories = calories,
                protein = protein,
                spend = totalSpend,
                items = totalItemsCount
            )

            val categoryMacros = userItems.groupBy { it.category }
            val totalGrams = (fats + carbs + protein).coerceAtLeast(1)
            _macroBreakdown.value = MacroBreakdown(
                proteinPercent = (protein / totalGrams * 100).toString() + "%",
                carbsPercent = (fats / totalGrams * 100).toString() + "%",
                fatsPercent = (carbs / totalGrams * 100).toString() + "%",
            )

            val categorySpend = userItems.groupBy { it.category }.mapValues { entry ->
                entry.value.sumOf { item -> item.price * item.quantity.toDouble() }
            }
            _spendingByCategory.value = SpendingByCategory(
                categories = if (totalSpend > 0) {
                    categorySpend.map { (cat, spend) -> cat to (spend / totalSpend).toFloat() }
                } else emptyList()
            )

            val topCategory = userItems.groupBy { it.category }.maxByOrNull { it.value.size }?.key ?: ""
            _insights.value = listOf(
                if (topCategory.lowercase().contains("produce")) {
                    Insight(
                        title = "Great produce choices",
                        message = "Items like your $topCategory are packed with iron and folate - perfect for your fitness goals.",
                        color = androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.8f),
                        backgroundColor = androidx.compose.ui.graphics.Color(0xFFE8F5E9)
                    )
                } else {
                    Insight(
                        title = "Higher variety than last time",
                        message = "You're getting ${((totalItemsCount / 4.0) * 100).toInt()}% more items compared to average.",
                        color = androidx.compose.ui.graphics.Color(0xFFF4A142),
                        backgroundColor = androidx.compose.ui.graphics.Color(0xFFFFF3E0)
                    )
                },
                Insight(
                    title = "Budget insight",
                    message = "Total spend: $${String.format("%.2f", totalSpend)} across ${categorySpend.size} categories.",
                    color = androidx.compose.ui.graphics.Color(0xFF2196F3),
                    backgroundColor = androidx.compose.ui.graphics.Color(0xFFE3F2FD)
                )
            )

            _recipes.value = suggestRecipesFromItems(userItems)
        }
    }

    private fun estimateCalories(items: List<Item>): Int {
        return items.sumOf { item ->
            when (item.category.lowercase()) {
                "produce" -> (item.quantity * 50).toInt()
                "meat" -> (item.quantity * 200).toInt()
                "dairy" -> (item.quantity * 150).toInt()
                else -> (item.quantity * 100).toInt()
            }
        }
    }

    private fun estimateProtein(items: List<Item>): Int {
        return items.sumOf { item ->
            when (item.category.lowercase()) {
                "meat" -> (item.quantity * 25).toInt()
                "dairy" -> (item.quantity * 8).toInt()
                else -> (item.quantity * 2).toInt()
            }
        }
    }

    private fun estimateMacroPercent(categoryMap: Map<String, List<Item>>, macroType: String): String {
        val totalItems = categoryMap.values.sumOf { it.size }
        val relevantCount = when (macroType) {
            "Protein" -> (categoryMap["Meat"]?.size ?: 0) + (categoryMap["Dairy"]?.size ?: 0)
            "Carbs" -> categoryMap["Grains"]?.size ?: 0
            "Fats" -> (categoryMap["Dairy"]?.size ?: 0) + ((categoryMap["Meat"]?.size ?: 0) / 2)
            else -> 0
        }
        val percent = if (totalItems > 0) (relevantCount.toFloat() / totalItems * 100).toInt() else 0
        return "${percent}%"
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