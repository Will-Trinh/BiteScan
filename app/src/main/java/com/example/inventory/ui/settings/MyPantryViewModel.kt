package com.example.inventory.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import com.example.inventory.data.ReceiptsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*


class MyPantryViewModel(
    private val itemsRepository: ItemsRepository,
    private val receiptsRepository: ReceiptsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PantryUiState())
    val uiState: StateFlow<PantryUiState> = _uiState

    fun loadPantryItems(userId: Int) {
        viewModelScope.launch {
            val receipts = receiptsRepository.getReceiptsForUser(userId).first()
            val receiptItems = mutableListOf<Item>()
            receipts.forEach { receipt ->
                val items = itemsRepository.getItemsForReceipt(receipt.receiptId).first()
                receiptItems.addAll(items)
            }
            val allItems = itemsRepository.getAllItemsStream().first()
            val standalonePantryItems = allItems.filter { it.receiptId == null }
            val allPantryItems = (receiptItems + standalonePantryItems).distinctBy { it.id }
            println("Loaded ${allPantryItems.size} pantry items")
            _uiState.value = _uiState.value.copy(
                pantryItems = allPantryItems.map { item ->
                    val expiration = getExpirationDate(item.date)
                    val daysLeft = calculateDaysLeft(expiration)
                    val unit = determineUnit(item.name)
                    val quantity = "${item.quantity} $unit"
                    PantryItem(
                        id = item.id,
                        name = item.name,
                        quantity = quantity,
                        unitPrice = item.price.toString(),
                        purchaseDate = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(item.date),
                        expiration = expiration,
                        unit = unit,
                        store = item.store,
                        category = item.category,
                        daysLeft = daysLeft
                    )
                }
            )
        }
    }

    private fun determineUnit(name: String): String {
        val lowerName = name.lowercase()
        return when {
            lowerName.contains("banana") -> "pieces"
            lowerName.contains("yogurt") -> "oz"
            lowerName.contains("chicken") -> "lbs"
            lowerName.contains("spinach") -> "oz bag"
            lowerName.contains("bread") -> "loaf"
            lowerName.contains("milk") -> "gallon"
            else -> "units"
        }
    }

    private fun calculateDaysLeft(expiration: String): Int {
        try {
            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            val expDate = sdf.parse(expiration)
            val currentDate = Date(System.currentTimeMillis())
            val diff = expDate.time - currentDate.time
            return (diff / (24 * 60 * 60 * 1000)).toInt()
        } catch (e: Exception) {
            return 0
        }
    }

    private fun getExpirationDate(purchaseDate: Date): String {
        val calendar = Calendar.getInstance().apply { time = purchaseDate }
        calendar.add(Calendar.DAY_OF_YEAR, 30) // Fixed for simplicity
        return SimpleDateFormat("MM/dd/yyyy", Locale.US).format(calendar.time)
    }

    fun addOrUpdatePantryItem(pantryItem: PantryItem) {
        viewModelScope.launch {
            val quantityStr = pantryItem.quantity.replace(Regex("[^0-9.]"), "")
            val item = Item(
                id = pantryItem.id,
                name = pantryItem.name,
                price = pantryItem.unitPrice.toDoubleOrNull() ?: 0.0,
                quantity = quantityStr.toFloatOrNull() ?: 0f,
                date = java.sql.Date(
                    SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(pantryItem.purchaseDate)
                        ?.time ?: System.currentTimeMillis()
                ),
                store = pantryItem.store,
                category = pantryItem.category,
                receiptId = null
            )
            if (pantryItem.id == 0) {
                itemsRepository.insertItem(item)
            } else {
                itemsRepository.updateItem(item)
            }
            loadPantryItems(_uiState.value.userId ?: 0)
        }
    }

    fun deletePantryItem(itemId: Int) {
        viewModelScope.launch {
            itemsRepository.getItemStream(itemId).first()?.let { item ->
                itemsRepository.deleteItem(item)
                loadPantryItems(_uiState.value.userId ?: 0)
            }
        }
    }

    fun setUserId(userId: Int) {
        _uiState.value = _uiState.value.copy(userId = userId)
        loadPantryItems(userId)
    }
}

data class PantryUiState(
    val userId: Int? = null,
    val pantryItems: List<PantryItem> = emptyList()
)

data class PantryItem(
    val id: Int,
    var name: String,
    var quantity: String,
    var unitPrice: String,
    var purchaseDate: String,
    var expiration: String,
    var unit: String,
    var store: String,
    var category: String,
    val daysLeft: Int
)