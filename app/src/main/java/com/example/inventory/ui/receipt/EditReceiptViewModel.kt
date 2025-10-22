package com.example.inventory.ui.receipt

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.Receipt
import com.example.inventory.data.ReceiptsRepository
import com.example.inventory.data.ItemsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Date

class EditReceiptViewModel(
    private val itemsRepository: ItemsRepository,
    private val receiptsRepository: ReceiptsRepository,
) : ViewModel() {

    private val _editUiState = MutableStateFlow(EditUiState())
    val editUiState: StateFlow<EditUiState> = _editUiState.asStateFlow()

    /**
     * Load draft receipt from API using JSONObject (no serialization plugin needed)
     */
    fun loadDraftFromApi(draftCode: String) {
        viewModelScope.launch {
            try {
                val jsonString = withContext(Dispatchers.IO) {
                    val url = URL("https://drive.google.com/uc?export=download&id=1Ic7t-FeXSnvUhKpkZOe5j_tCU-pGK7Vs")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.inputStream.bufferedReader().use { it.readText() }
                }

                Log.d("API_TEST", "Draft JSON: $jsonString")

                // Parse JSON manually using JSONObject
                val jsonObject = JSONObject(jsonString)

                val source = jsonObject.optString("source", "")
                val date = jsonObject.optLong("date")
                val status = jsonObject.optString("status")

                val itemsArray = jsonObject.getJSONArray("items")
                val items = mutableListOf<Item>()

                for (i in 0 until itemsArray.length()) {
                    val itemObj = itemsArray.getJSONObject(i)
                    items.add(
                        Item(
                            id = itemObj.getInt("id"),
                            name = itemObj.getString("name"),
                            price = itemObj.getDouble("price"),
                            quantity = itemObj.getDouble("quantity").toFloat(),
                            date = Date(System.currentTimeMillis()),
                            store = itemObj.optString("store", ""),
                            category = itemObj.optString("category", ""),
                            receiptId = 0
                        )
                    )
                }

                val receipt = Receipt(
                    receiptId = 0,
                    userId = 0,
                    source = source,
                    date = Date(date),
                    status = status
                )

                // Update UI state
                _editUiState.value = _editUiState.value.copy(
                    receipt = receipt,
                    itemList = items.sortedBy { it.id },
                    totalItems = items.size,
                    totalPrice = items.sumOf { it.price * it.quantity }
                )

            } catch (e: Exception) {
                Log.e("EditReceiptVM", "Error loading draft: ${e.message}")
                _editUiState.value = _editUiState.value.copy(
                    receipt = null,
                    itemList = emptyList(),
                    totalItems = 0,
                    totalPrice = 0.0
                )
            }
        }
    }

    /** Calculate total price */
    fun calculateTotalPrice(items: List<Item>): Double {
        return items.sumOf { it.price * it.quantity }
    }

    /** Calculate total items count */
    fun calculateTotalItem(items: List<Item>): Int {
        return items.size
    }

    /** Update a single item by index */
    fun updateItem(index: Int, updatedItem: Item) {
        val currentItems = _editUiState.value.itemList.toMutableList()
        currentItems[index] = updatedItem
        _editUiState.value = _editUiState.value.copy(
            itemList = currentItems.sortedBy { it.id },
            totalItems = calculateTotalItem(currentItems),
            totalPrice = calculateTotalPrice(currentItems)
        )
    }

    /** Get receipt date as string */
    fun receiveDate(): String {
        val currentReceipt = _editUiState.value.receipt
        return currentReceipt?.date?.toString() ?: "N/A"
    }

    /** Update receipt source */
    fun updateSource(newSource: String) {
        val currentReceipt = _editUiState.value.receipt
        if (currentReceipt != null) {
            _editUiState.value = _editUiState.value.copy(
                receipt = currentReceipt.copy(source = newSource)
            )
        }
    }

    /** Add new item */
    fun addItem(item: Item) {
        val newItem = Item(
            id = (_editUiState.value.itemList.maxOfOrNull { it.id } ?: 0) + 1,
            name = item.name,
            price = item.price,
            quantity = item.quantity,
            date = Date(System.currentTimeMillis()),
            store = item.store,
            category = item.category,
            receiptId = 0
        )
        val updatedItems = _editUiState.value.itemList + newItem
        _editUiState.value = _editUiState.value.copy(
            itemList = updatedItems.sortedBy { it.id },
            totalItems = calculateTotalItem(updatedItems),
            totalPrice = calculateTotalPrice(updatedItems)
        )
    }

    /** Save receipt (placeholder for API POST) */
    suspend fun saveReceipt(receipt: Receipt) {
        // TODO: Implement API call to save receipt
    }
}

/** UI state data class */
data class EditUiState(
    val itemList: List<Item> = emptyList(),
    val totalItems: Int = 0,
    val totalPrice: Double = 0.0,
    val receipt: Receipt? = null
)
