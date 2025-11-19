package com.example.inventory.ui.receipt

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
import java.sql.Date
import android.util.Log
import org.json.JSONArray
import kotlinx.coroutines.TimeoutCancellationException
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.collections.forEach
import com.example.inventory.data.OnlineReceiptsRepository

class EditReceiptViewModel(
    private val itemsRepository: ItemsRepository,
    private val receiptsRepository: ReceiptsRepository,
    private val onlineReceiptsRepository: OnlineReceiptsRepository?= null
) : ViewModel() {

    private val _editUiState = MutableStateFlow(EditUiState())
    private val nutritionApiUrl = "http://129.146.23.142:8080/nutrition/items"
    val editUiState: StateFlow<EditUiState> = _editUiState.asStateFlow()
    var deleteItems: List<Item> = emptyList()

//deleteItem and update listItem in EditReceiptScreen
    fun deleteItem(selectedItemIndex: Int) {
        try{
        val currentItems = _editUiState.value.itemList.toMutableList()
        //get item to delete
        deleteItems = deleteItems + currentItems[selectedItemIndex]
        currentItems.removeAt(selectedItemIndex)
        _editUiState.value = _editUiState.value.copy(
            itemList = currentItems.sortedBy { it.id },
            totalItems = calculateTotalItem(currentItems),
            totalPrice = calculateTotalPrice(currentItems)
        )}catch (e: Exception) {
            Log.e("EditReceiptVM", "Error deleting item: ${e.message}")}
    }

    fun loadReceipt(receiptId: Int) {
        viewModelScope.launch {
            try {
                val receipt = withContext(Dispatchers.IO) {
                    receiptsRepository.getReceipt(receiptId)  // Assume ReceiptsRepository has getReceipt(id: Int): Receipt? (suspend or Flow.first())
                }
                _editUiState.value = _editUiState.value.copy(receipt = receipt)
                Log.d("EditReceiptVM", "Loaded receipt: $receipt")
            } catch (e: Exception) {
                Log.e("EditReceiptVM", "Error loading receipt: ${e.message}")
                _editUiState.value = _editUiState.value.copy(receipt = null)
            }
        }
    }

    fun loadItems(receiptId: Int) {
        viewModelScope.launch {
            try {
                val items = withContext(Dispatchers.IO) {
                    itemsRepository.getItemsForReceipt(receiptId)
                        .first()  // Assume ItemsRepository.getItemsForReceipt(id: Int): Flow<List<Item>>
                }
                val totalItems = calculateTotalItem(items)
                val totalPrice = calculateTotalPrice(items)
                _editUiState.value = _editUiState.value.copy(
                    itemList = items.sortedBy { it.id },
                    totalItems = totalItems,
                    totalPrice = totalPrice
                )
                Log.d("EditReceiptVM", "Loaded ${items.size} items for receipt $receiptId")
            } catch (e: Exception) {
                Log.e("EditReceiptVM", "Error loading items: ${e.message}")
                _editUiState.value = _editUiState.value.copy(
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
    fun addItem(item: Item, receiptId: Int) {
        val newItem = item.copy(
            id = 0,
            name = item.name,
            price = item.price,
            quantity = item.quantity,
            date = Date(System.currentTimeMillis()),
            store = item.store,
            category = item.category,
            receiptId = receiptId
        )
        val updatedItems = _editUiState.value.itemList + newItem
        _editUiState.value = _editUiState.value.copy(
            itemList = updatedItems.sortedBy { it.id },
            totalItems = calculateTotalItem(updatedItems),
            totalPrice = calculateTotalPrice(updatedItems)
        )
    }

    //save updated list item to database with the receipt id
    fun saveUpdatedItems(receipt: Receipt, userId: Int) {
        viewModelScope.launch {
            try {
                //delete items
                deleteItems.forEach { item ->
                    itemsRepository.deleteItem(item)
                }
                //update receipt
                receiptsRepository.updateReceipt(receipt)
                //check if item is in receipt, update item in receipt, if item is new add, insert it in to the receipt.
                val updatedItems = _editUiState.value.itemList.map { it.copy(receiptId = receipt.receiptId) }
                updatedItems.forEach { item ->
                    if (item.id == 0) {
                        itemsRepository.insertItem(item.copy(receiptId = receipt.receiptId))
                        Log.d("EditReceiptVM", "Item ID ${item.id} saved successfully")
                    } else {
                        itemsRepository.updateItem(item.copy(receiptId = receipt.receiptId))
                        Log.d("EditReceiptVM", "Item ID ${item.id} updated successfully")
                    }
                }
                val receiptID = receipt.receiptId
                //syns client receipt to server
                Log.d("EditReceiptVM", "Receipt ID $receiptID uploading to server")
                onlineReceiptsRepository!!.uploadReceiptToServer(receiptID, userId)
                Log.d("EditReceiptVM", "Receipt ID $receiptID uploaded to server successfully")
                _editUiState.value = _editUiState.value.copy(
                    receipt = receipt,
                    itemList = updatedItems
                )
                Log.d("EditReceiptVM", "Items saved successfully")

            } catch (e: Exception) {
                Log.e("EditReceiptVM", " Error saving items: ${e}")
            }
        }
    }
    /** Delete receipt (placeholder for API DELETE) */
    suspend fun deleteReceipt(receipt: Receipt, userId: Int) {
        receiptsRepository.deleteReceipt(receipt)
        onlineReceiptsRepository!!.deleteReceiptFromServer(receipt.receiptId, userId)
    }

    suspend fun saveItems(nutritionData: MutableList<Item>) {
        nutritionData.forEach { item ->
            itemsRepository.updateItem(item)
        }
    }

    fun processItems() {
        viewModelScope.launch {
            try {
                withTimeout(300000L) {  // 30s timeout to prevent ANR
                    withContext(Dispatchers.IO) { _processItems() }
                }
            } catch (e: Exception) {
                Log.e("UploadViewModel", "nutrition failed: ${e.message}", e)  // Log stack trace
            }
        }
    }

    private suspend fun _processItems(): MutableList<Item> {
        // TODO:
        // this url needs to be dynamic so we dont gotta change it everywhere, put in config
        Log.d("UploadViewModel", "Starting API call to $nutritionApiUrl")

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)  // Short timeout for connect
            .readTimeout(180, TimeUnit.SECONDS)  // Read timeout
            .writeTimeout(20, TimeUnit.SECONDS)  // Write timeout
            .build()

        val items = editUiState.value.itemList
        val jsonArray = JSONArray()

        items.forEach { item ->
            val itemJson = JSONObject().apply {
                put("id", item.id)
                put("receiptId", item.receiptId)
                put("name", item.name)
                put("protein", item.protein)
                put("carbs", item.carbs)
                put("fats", item.fats)
                put("calories", item.calories)
                put("price", item.price)
                put("quantity", item.quantity)
                put("store", item.store)
                put("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(item.date))
                put("category", item.category)
            }
            jsonArray.put(itemJson)
        }
        val body =
            jsonArray.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(nutritionApiUrl)
            .post(body)
            .addHeader("User-Agent", "AndroidApp/1.0")  // Helps with some servers
            .build()

        Log.d("UploadViewModel", "Request built, executing...")

        val response = client.newCall(request).execute()
        Log.d(
            "UploadViewModel",
            "Execute complete - Code: ${response.code}, Message: ${response.message}"
        )
        Log.d("UploadViewModel", "Response headers: ${response.headers}")

        val responseBody = response.body?.string() ?: "{}"
        Log.d("UploadViewModel", "Full Response Body: $responseBody")

        try {
            if (!response.isSuccessful) {
                throw Exception("API error: ${response.code} - ${response.message}. Body: $responseBody")
            }
            val responseJson = JSONArray(responseBody)
            val nutritionList = mutableListOf<Item>()
            for (i in 0 until responseJson.length()) {
                val itemJson = responseJson.getJSONObject(i)
                val dateString = itemJson.optString("date")
                val date = Date(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString).time
                )
                nutritionList.add(
                    Item(
                        id = itemJson.optInt("id"),
                        receiptId = itemJson.optInt("receiptId"),
                        name = itemJson.optString("name"),
                        protein = itemJson.optDouble("protein"),
                        carbs = itemJson.optDouble("carbs"),
                        fats = itemJson.optDouble("fats"),
                        calories = itemJson.optDouble("calories"),
                        price = itemJson.optDouble("price"),
                        quantity = itemJson.optDouble("quantity").toFloat(),
                        store = itemJson.optString("store"),
                        date = date,
                        category = itemJson.optString("category")
                    )
                )
            }
            saveItems(nutritionList)
            return nutritionList
        } finally {
            response.close()
        }
    }
}

data class EditUiState(
    val itemList: List<Item> = emptyList(),
    val totalItems: Int = 0,
    val totalPrice: Double = 0.0,
    val receipt: Receipt? = null,
)