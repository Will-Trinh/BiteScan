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

    val PANTRY_CATEGORIES = listOf("Grocery", "Fruit", "Veggies", "Meat", "Fish", "Dairy", "Other")
    private fun expiryDaysForCategory(category: String): Int = when (category.lowercase(Locale.US)) {
        "grocery" -> 180
        "fruit"   -> 7
        "veggies" -> 10
        "meat"    -> 5
        "fish"    -> 4
        "dairy"   -> 14
        else   -> 21
    }

    val categoryExpiryInfo = mapOf(
        "Grocery" to "≈180 days",
        "Fruit" to "< 7 days",
        "Veggies" to "< 10 days",
        "Meat" to "< 5 days",
        "Fish" to "< 4 days",
        "Dairy" to "< 14 days",
        "Other" to "≈21 days"
    )

    fun loadPantryItems(userId: Int) {
        viewModelScope.launch {
            // 1) get all receipts for this user, newest first
            val receipts = receiptsRepository.getReceiptsForUser(userId).first()
                .sortedByDescending { it.date.time }

            // 2) flatten items across all receipts (HISTORY)
            val receiptItems: List<Item> = receipts.flatMap { receipt ->
                val itemsForThisReceipt = itemsRepository
                    .getItemsForReceipt(receipt.receiptId)
                    .first()

                // Prefer the receipt’s metadata (date/store) when mapping for the UI
                itemsForThisReceipt.map { it.copy(
                    // keep the DB record as-is, but we’ll prefer receipt info in the UI mapping below
                    receiptId = receipt.receiptId
                ) }
            }

            // 3) Map to UI
            val pantryItemsForUi = receiptItems.map { dbItem ->
                // Prefer the receipt’s date/store if present on the item, fall back to item fields
                val receiptForItem = receipts.firstOrNull { it.receiptId == dbItem.receiptId }
                val purchaseDateSql = (receiptForItem?.date ?: dbItem.date) // java.sql.Date
                val storeName = receiptForItem?.source ?: dbItem.store

                val expiration = getExpirationDate(purchaseDateSql, dbItem.category)
                val daysLeft = calculateDaysLeft(expiration)
                val unit = determineUnit(dbItem.name)
                val quantity = "${dbItem.quantity} $unit"

                PantryItem(
                    id = dbItem.id,
                    name = dbItem.name,
                    quantity = quantity,
                    unitPrice = dbItem.price.toString(),
                    purchaseDate = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(purchaseDateSql),
                    expiration = expiration,
                    unit = unit,
                    store = storeName,
                    category = dbItem.category,
                    daysLeft = daysLeft
                )
            }

            _uiState.value = _uiState.value.copy(
                pantryItems = pantryItemsForUi.distinctBy { it.id }
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

    private fun getExpirationDate(purchaseDate: Date, category: String): String {
        val calendar = Calendar.getInstance().apply { time = purchaseDate }
        calendar.add(Calendar.DAY_OF_YEAR, expiryDaysForCategory(category))
        return SimpleDateFormat("MM/dd/yyyy", Locale.US).format(calendar.time)
    }

    fun addOrUpdatePantryItem(pantryItem: PantryItem) {
        viewModelScope.launch {
            val userId = _uiState.value.userId ?: 0

            // Keep existing row (to preserve receiptId on update)
            val existing: Item? = if (pantryItem.id != 0) {
                itemsRepository.getItemStream(pantryItem.id).first()
            } else null

            // Latest receipt id, but cast to Int? to match Item.receiptId
            val latestReceiptId: Int? = if (pantryItem.id == 0) {
                receiptsRepository.getReceiptsForUser(userId).first()
                    .maxByOrNull { it.date.time }
                    ?.receiptId
                    ?.toInt() // <-- convert Long? -> Int?
            } else null

            val quantityStr = pantryItem.quantity.replace(Regex("[^0-9.]"), "")
            val parsedDateMillis = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                .parse(pantryItem.purchaseDate)?.time ?: System.currentTimeMillis()

            val resolvedReceiptId: Int? = existing?.receiptId ?: latestReceiptId

            val item = Item(
                id = pantryItem.id,
                name = pantryItem.name,
                price = pantryItem.unitPrice.toDoubleOrNull() ?: 0.0,
                quantity = quantityStr.toFloatOrNull() ?: 0f,
                date = java.sql.Date(parsedDateMillis),
                store = pantryItem.store,
                category = pantryItem.category,
                receiptId = resolvedReceiptId?:0
            )

            if (pantryItem.id == 0) itemsRepository.insertItem(item)
            else itemsRepository.updateItem(item)

            loadPantryItems(userId)
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