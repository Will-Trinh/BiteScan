package com.example.inventory.ui.pricetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import com.example.inventory.data.ReceiptsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * ViewModel for the Price Tracker feature
 * Manages price history data for items across different stores
 */
class PriceTrackerViewModel(
    private val itemsRepository: ItemsRepository,
    private val receiptsRepository: ReceiptsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PriceTrackerUiState())
    val uiState: StateFlow<PriceTrackerUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Load all items for a user and group them by normalized name
     */
    fun loadPriceData(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(syncStatus = PriceTrackerSyncStatus.LOADING)

            try {
                // Use getItemsWithStoreForUser to get items with correct store name from receipt.source
                val items = itemsRepository.getItemsWithStoreForUser(userId).first()
                val priceGroups = groupItemsByName(items)

                _uiState.value = PriceTrackerUiState(
                    syncStatus = PriceTrackerSyncStatus.SUCCESS,
                    allPriceGroups = priceGroups,
                    filteredPriceGroups = priceGroups
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(syncStatus = PriceTrackerSyncStatus.ERROR)
            }
        }
    }

    /**
     * Update search query and filter results
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            val allGroups = _uiState.value.allPriceGroups
            val filtered = if (query.isBlank()) {
                allGroups
            } else {
                allGroups.filter { group ->
                    group.itemName.contains(query, ignoreCase = true)
                }
            }
            _uiState.value = _uiState.value.copy(filteredPriceGroups = filtered)
        }
    }

    /**
     * Group items by their normalized name and calculate price comparisons
     */
    private fun groupItemsByName(items: List<Item>): List<PriceGroup> {
        // Group items by normalized name (lowercase, trimmed)
        val groupedItems = items.groupBy { normalizeItemName(it.name) }

        return groupedItems.map { (normalizedName, itemList) ->
            // Sort by date (most recent first)
            val sortedItems = itemList.sortedByDescending { it.date.time }

            // Create price entries for each store occurrence
            val priceEntries = sortedItems.map { item ->
                PriceEntry(
                    store = item.store,
                    price = item.price,
                    date = item.date,
                    formattedDate = formatDate(item.date),
                    quantity = item.quantity,
                    unit = determineUnit(item)
                )
            }

            // Calculate potential savings
            val prices = priceEntries.map { it.price }
            val minPrice = prices.minOrNull() ?: 0.0
            val maxPrice = prices.maxOrNull() ?: 0.0
            val savingsPercentage = if (maxPrice > 0) {
                ((maxPrice - minPrice) / maxPrice * 100).toInt()
            } else 0

            // Use the display name from the first item (most recent)
            val displayName = sortedItems.firstOrNull()?.name?.capitalizeWords() ?: normalizedName

            PriceGroup(
                itemName = displayName,
                priceEntries = priceEntries,
                lowestPrice = minPrice,
                highestPrice = maxPrice,
                savingsPercentage = savingsPercentage
            )
        }.filter { it.priceEntries.isNotEmpty() }
            .sortedByDescending { it.savingsPercentage } // Sort by savings opportunity
    }

    /**
     * Normalize item name for grouping (remove extra spaces, lowercase)
     */
    private fun normalizeItemName(name: String): String {
        return name.lowercase(Locale.getDefault())
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    /**
     * Capitalize first letter of each word
     */
    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { word ->
            word.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                else it.toString()
            }
        }
    }

    /**
     * Format date for display
     */
    private fun formatDate(date: java.sql.Date): String {
        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Determine unit type based on item properties
     */
    private fun determineUnit(item: Item): String {
        // Try to infer unit from quantity or category
        return when {
            item.quantity == 1f -> "per each"
            item.category.contains("meat", ignoreCase = true) -> "per lbs"
            item.category.contains("produce", ignoreCase = true) -> "per lbs"
            item.category.contains("seafood", ignoreCase = true) -> "per lbs"
            else -> "per each"
        }
    }
}

/**
 * UI State for Price Tracker screen
 */
data class PriceTrackerUiState(
    val syncStatus: PriceTrackerSyncStatus = PriceTrackerSyncStatus.LOADING,
    val allPriceGroups: List<PriceGroup> = emptyList(),
    val filteredPriceGroups: List<PriceGroup> = emptyList()
)

/**
 * Sync status for loading state
 */
enum class PriceTrackerSyncStatus {
    LOADING,
    SUCCESS,
    ERROR
}

/**
 * Represents a group of price entries for the same item
 */
data class PriceGroup(
    val itemName: String,
    val priceEntries: List<PriceEntry>,
    val lowestPrice: Double,
    val highestPrice: Double,
    val savingsPercentage: Int
)

/**
 * Individual price entry from a store
 */
data class PriceEntry(
    val store: String,
    val price: Double,
    val date: java.sql.Date,
    val formattedDate: String,
    val quantity: Float,
    val unit: String
)

