package com.example.inventory.ui.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.Receipt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Date
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class EditReceiptViewModelTest : ViewModel() {
    private val _editUiState = MutableStateFlow(EditUiState())
    val editUiState: StateFlow<EditUiState> = _editUiState.asStateFlow()

    // get draft from api
    fun loadDraftFromApi(draftCode: String) {
        viewModelScope.launch {
            try {
                val url = URL("http://abcxyz.com/draftcode/$draftCode")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                    val draftData = Json.decodeFromString<DraftResponse>(jsonString)

                    // Convert JSON data to Receipt and Item entities
                    val receipt = Receipt(
                        receiptId = 0,
                        userId = 0,
                        source = draftData.source ?: "",
                        date = Date(draftData.date),
                        status = draftData.status
                    )
                    val items = draftData.items.map { item ->
                        Item(
                            id = item.id,
                            name = item.name,
                            price = item.price,
                            quantity = item.quantity,
                            date = Date(System.currentTimeMillis()),
                            store = item.store ?: "",
                            category = item.category ?: "",
                            receiptId = 0
                        )
                    }

                    _editUiState.value = _editUiState.value.copy(
                        receipt = receipt,
                        itemList = items.sortedBy { it.id },
                        totalItems = calculateTotalItem(items),
                        totalPrice = calculateTotalPrice(items)
                    )
                }
                connection.disconnect()
            } catch (e: Exception) {
                _editUiState.value = _editUiState.value.copy(
                    itemList = emptyList(),
                    receipt = null,
                    totalItems = 0,
                    totalPrice = 0.0
                )
            }
        }
    }

    fun calculateTotalPrice(items: List<Item>): Double {
        return items.sumOf { it.price * it.quantity }
    }

    fun calculateTotalItem(items: List<Item>): Int {
        return items.count()
    }

    fun updateItem(index: Int, updatedItem: Item) {
        val currentItems = _editUiState.value.itemList.toMutableList()
        currentItems[index] = updatedItem
        _editUiState.value = _editUiState.value.copy(
            itemList = currentItems.sortedBy { it.id },
            totalItems = calculateTotalItem(currentItems),
            totalPrice = calculateTotalPrice(currentItems)
        )
    }

    fun receiveDate(): String {
        val currentReceipt = _editUiState.value.receipt
        return currentReceipt?.date?.toString() ?: "N/A"
    }

    fun updateSource(newSource: String) {
        val currentReceipt = _editUiState.value.receipt
        if (currentReceipt != null) {
            _editUiState.value = _editUiState.value.copy(
                receipt = currentReceipt.copy(source = newSource)
            )
        }
    }

    fun addItem(item: Item) {
        val newItem = Item(
            id = (_editUiState.value.itemList.maxOfOrNull { it.id } ?: 0) + 1,
            name = item.name,
            price = item.price,
            quantity = item.quantity,
            date = Date(System.currentTimeMillis()),
            store = "",
            category = "",
            receiptId = 0
        )
        val updatedItems = _editUiState.value.itemList + newItem
        _editUiState.value = _editUiState.value.copy(
            itemList = updatedItems.sortedBy { it.id },
            totalItems = calculateTotalItem(updatedItems),
            totalPrice = calculateTotalPrice(updatedItems)
        )
    }

    suspend fun saveReceipt(receipt: Receipt) {
        // TODO: (POST request)

    }
}

// Data class để parse JSON từ API
@Serializable
data class DraftResponse(
    val draftCode: String,
    val source: String? = null,
    val date: Long,
    val status: String,
    val items: List<DraftItem>
)

@Serializable
data class DraftItem(
    val id: Int,
    val name: String,
    val price: Double,
    val quantity: Float,
    val store: String? = null,
    val category: String? = null
)

data class EditUiStateTest(
    val itemList: List<Item> = emptyList(),
    val totalItems: Int = 0,
    val totalPrice: Double = 0.0,
    val receipt: Receipt? = null
)