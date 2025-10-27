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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log
import com.example.inventory.ui.upload.ReceiptData  // Import from upload package
import com.example.inventory.ui.upload.LineItem
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

data class NutritionData(
    val name: String? = null,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fats: Double? = null,
    val calories: Double? = null,
)

class EditReceiptViewModel(
    private val itemsRepository: ItemsRepository,
    private val receiptsRepository: ReceiptsRepository,
) : ViewModel() {

    private val _editUiState = MutableStateFlow(EditUiState())
    private val nutritionApiUrl = "http://10.0.2.2:8000/nutrition/items"
    val editUiState: StateFlow<EditUiState> = _editUiState.asStateFlow()

    /**
     * Load draft receipt from API.
     * Run network call in IO dispatcher to avoid blocking UI.
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

                // Log for debugging
                Log.d("API_TEST", "Draft JSON: $jsonString")

                // Parse JSON
                val json = Json { ignoreUnknownKeys = true } // ignores extra fields
                val draftData = json.decodeFromString<DraftResponse>(jsonString)

                // Convert to Receipt and Items
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

                // Update UI state
                _editUiState.value = _editUiState.value.copy(
                    receipt = receipt,
                    itemList = items.sortedBy { it.id },
                    totalItems = calculateTotalItem(items),
                    totalPrice = calculateTotalPrice(items)
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

    // New: Load from OCR ReceiptData (called after OCR success in UploadScreen)
    // Add this function to your EditReceiptViewModel class (after loadDraftFromApi)
    fun loadFromOcrData(receiptData: ReceiptData, receiptId: Int) {
        viewModelScope.launch {
            try {
                // Parse transaction_date to java.sql.Date (assume "YYYY-MM-DD" format)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val parsedDate = if (receiptData.transaction_date != null) {
                    Date(sdf.parse(receiptData.transaction_date)?.time ?: System.currentTimeMillis())
                } else Date(System.currentTimeMillis())

                // Convert to Receipt (match your data class)
                val receipt = Receipt(
                    receiptId = receiptId,
                    userId = 0,  // From nav/auth param
                    date = parsedDate,
                    source = receiptData.merchant_name ?: "Unknown Merchant",  // Merchant as source
                    status = "Pending"  // Ready for edit
                )

                // Convert line_items to Items (match your Item data class)
                val items = receiptData.line_items.map { lineItem ->
                    Item(
                        id = 0,  // Auto-generate or sequential
                        name = lineItem.item_name ?: "Unknown Item",
                        price = lineItem.item_price ?: 0.0,
                        quantity = (lineItem.item_quantity ?: 1).toFloat(),
                        date = parsedDate,
                        store = receiptData.merchant_name ?: "",  // Merchant as store
                        category = "Unknown",  // Derive from name if needed (e.g., "Banana" -> "Fruit")
                        receiptId = receiptId
                    )
                }

                // Update UI state with OCR data
                _editUiState.value = _editUiState.value.copy(
                    receipt = receipt,
                    itemList = items.sortedBy { it.name },  // Sort by name for better UX
                    totalItems = calculateTotalItem(items),
                    totalPrice = calculateTotalPrice(items)
                )

                Log.d("EditReceiptVM", "Loaded OCR data: ${items.size} items from '${receipt.source}', date ${receipt.date}")
            } catch (e: Exception) {
                Log.e("EditReceiptVM", "OCR data load error: ${e.message}", e)
                _editUiState.value = _editUiState.value.copy(
                    receipt = null,
                    itemList = emptyList(),
                    totalItems = 0,
                    totalPrice = 0.0
                )
            }
        }
    }

    // Add these functions to your EditReceiptViewModel class (after loadDraftFromApi)

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
                    itemsRepository.getItemsForReceipt(receiptId).first()  // Assume ItemsRepository.getItemsForReceipt(id: Int): Flow<List<Item>>
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

    fun processItems() {
        viewModelScope.launch {
            try {
                withTimeout(300000L) {  // 30s timeout to prevent ANR
                    val nutritionData = withContext(Dispatchers.IO) { _processItems() }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("UploadViewModel", "nutrition timed out")
            } catch (e: Exception) {
                Log.e("UploadViewModel", "nutrition failed: ${e.message}", e)  // Log stack trace
            }
        }
    }

    private suspend fun _processItems(): NutritionData {
        // TODO:
        // this url needs to be dynamic so we dont gotta change it everywhere, put in config
        Log.d("UploadViewModel", "Starting API call to $nutritionApiUrl")

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)  // Short timeout for connect
            .readTimeout(180, TimeUnit.SECONDS)  // Read timeout
            .writeTimeout(20, TimeUnit.SECONDS)  // Write timeout
            .build()

        val items = editUiState.value.itemList.map { it.name }
        val json = JSONArray(items)
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(nutritionApiUrl)
            .post(body)
            .addHeader("User-Agent", "AndroidApp/1.0")  // Helps with some servers
            .build()

        Log.d("UploadViewModel", "Request built, executing...")

        val response = client.newCall(request).execute()
        Log.d("UploadViewModel", "Execute complete - Code: ${response.code}, Message: ${response.message}")
        Log.d("UploadViewModel", "Response headers: ${response.headers}")

        val responseBody = response.body?.string() ?: "{}"
        Log.d("UploadViewModel", "Full Response Body: $responseBody")

        try {
            if (!response.isSuccessful) {
                throw Exception("API error: ${response.code} - ${response.message}. Body: $responseBody")
            }
            val responseJson = JSONObject(responseBody)
            return NutritionData(
                name = responseJson.optString("name"),
                protein = responseJson.optDouble("protein"),
                carbs = responseJson.optDouble("carbs"),
                fats = responseJson.optDouble("fats"),
                calories = responseJson.optDouble("calories"),
            )
        } finally {
            response.close()
        }
    }
}

// Data classes to parse JSON
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

data class EditUiState(
    val itemList: List<Item> = emptyList(),
    val totalItems: Int = 0,
    val totalPrice: Double = 0.0,
    val receipt: Receipt? = null
)