package com.example.inventory.ui.upload

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.Receipt
import com.example.inventory.data.ItemsRepository
import com.example.inventory.data.ReceiptsRepository
import com.example.inventory.data.ReceiptsRepositoryImpl
import com.example.inventory.ui.history.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay


data class ReceiptData(
    val merchant_name: String? = null,
    val merchant_address: String? = null,
    val transaction_date: String? = null,
    val transaction_time: String? = null,
    val total_amount: Double? = null,
    val line_items: List<LineItem> = emptyList()
)

data class LineItem(
    val item_name: String? = null,
    val item_quantity: Int? = null,
    val item_price: Double? = null,
    val item_total: Double? = null
)

sealed class OcrState {
    object Idle : OcrState()
    object Loading : OcrState()
    data class Success(val receiptData: ReceiptData) : OcrState()
    data class Error(val message: String) : OcrState()
}


class UploadViewModel(
    private val receiptsRepository: ReceiptsRepository,
    private val itemsRepository: ItemsRepository
) : ViewModel() {
    //for loading screen
    data class LoadingStep(
        val label: String,
        val status: StepStatus = StepStatus.PENDING
    )

    enum class StepStatus { PENDING, IN_PROGRESS, COMPLETED, FAILED }

    data class UploadProgress(
        val isProcessing: Boolean = false,
        val progress: Float = 0f,
        val steps: List<LoadingStep> = listOf(
            LoadingStep("Loading the Image..."),
            LoadingStep("Checking the Image..."),
            LoadingStep("Connecting to Server..."),
            LoadingStep("Reading the receipt..."),
            LoadingStep("Processing the receipt..."),
            LoadingStep("Saving the receipt..."),
            LoadingStep("Completed!")
        )
    )
    private fun updateStep(index: Int, status: StepStatus, progress: Float? = null) {
        val newSteps = _uploadProgress.value.steps.toMutableList().apply {
            this[index] = this[index].copy(status = status)
        }
        _uploadProgress.value = _uploadProgress.value.copy(
            steps = newSteps,
            progress = progress ?: _uploadProgress.value.progress
        )
    }

    private fun finishAll() {
        _uploadProgress.value = _uploadProgress.value.copy(
            isProcessing = false,
            progress = 1f
        )
    }

    private val _uploadProgress = MutableStateFlow(UploadProgress())
    val uploadProgress: StateFlow<UploadProgress> = _uploadProgress.asStateFlow()


    private val _ocrState = MutableStateFlow<OcrState>(OcrState.Idle)
    val ocrState: StateFlow<OcrState> = _ocrState

    private val _isProcessing = MutableStateFlow(false)

    private val ocrApiUrl = "http://129.146.23.142:8080/ocr"


    fun processReceiptImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isProcessing.value = true
            _ocrState.value = OcrState.Loading
            _uploadProgress.value = UploadProgress(isProcessing = true)
            try {
                //Convert bitmap to base64 + Loading step 1 "Checking the Image"
                updateStep(0, StepStatus.IN_PROGRESS, 0.05f)
                withTimeout(300000L) {  // 30s timeout to prevent ANR
                    //Loading step 2: "Loading the Image"
                    delay(500)
                    updateStep(0, StepStatus.COMPLETED)
                    updateStep(1, StepStatus.IN_PROGRESS, 0.15f)
                    val base64Image =
                        withContext(Dispatchers.Default) { convertBitmapToBase64(bitmap) }
                    //Loading step 3: Your receipt is processed...
                    updateStep(1, StepStatus.COMPLETED)
                    delay(600)
                    updateStep(2, StepStatus.IN_PROGRESS, 0.35f)
                    updateStep(2, StepStatus.COMPLETED)
                    //Loading step 4: Connecting to Server...
                    updateStep(3, StepStatus.IN_PROGRESS, 0.4f)
                    val receiptData = withContext(Dispatchers.IO) { callPythonOcrApi(base64Image) }
                    updateStep(3, StepStatus.COMPLETED)
                    //Loading step 5: Finishing the receipt...
                    delay(600)
                    updateStep(4, StepStatus.IN_PROGRESS, 0.7f)
                    _ocrState.value = OcrState.Success(receiptData)
                    updateStep(4, StepStatus.COMPLETED)
                }
            } catch (e: Exception) {
                Log.e("UploadViewModel", "OCR failed: ${e.message}", e)  // Log stack trace
                _ocrState.value = OcrState.Error(e.message ?: "Unknown error")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    suspend fun saveReceiptAndItems(receiptData: ReceiptData, userId: Int): Long {
        return withContext(Dispatchers.IO) {
            try {
                ////Loading step 6: Saving the receipt...
                delay(600)
                updateStep(5, StepStatus.IN_PROGRESS, 0.75f)
                Log.d("UploadViewModel", "Saving receipt with userId=$userId")

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val parsedDate = Date(System.currentTimeMillis())
                if (userId <= 0) throw IllegalArgumentException("Invalid userId: $userId. Please log in first.")
                val newReceipt = Receipt(
                    receiptId = 0,
                    userId = userId,
                    source = receiptData.merchant_name ?: "Unknown",
                    date = parsedDate,
                    status = "Pending"
                )
                val newReceiptID = receiptsRepository.insertReceipt(newReceipt)
                Log.d("UploadViewModel", "Inserted receipt ID: $newReceiptID")
                val newReceiptId = newReceiptID.toInt()
                receiptData.line_items.forEach { lineItem ->
                    val newItem = Item(
                        id = 0,
                        name = lineItem.item_name ?: "Unknown",
                        price = lineItem.item_price ?: 0.0,
                        quantity = (lineItem.item_quantity ?: 1).toFloat(),
                        date = parsedDate,
                        store = receiptData.merchant_address ?: "Unknown",
                        category = "Unknown",
                        receiptId = newReceiptId
                    )
                    val itemID = itemsRepository.insertItem(newItem)
                    Log.d("UploadViewModel", "Inserted item ID: $itemID")
                }
                updateStep(5, StepStatus.COMPLETED)
                delay(600)
                //Loading step 7: Completed! Enjoy your receipt!
                updateStep(6, StepStatus.IN_PROGRESS, 0.85f)
                Log.d("UploadViewModel", "Saved receipt ID: $newReceiptId")
                updateStep(6, StepStatus.COMPLETED)
                delay(600)
                newReceiptID
            } catch (e: Exception) {
                updateStep(5, StepStatus.FAILED)
                Log.e("UploadViewModel", "Save failed: ${e.message}")
                finishAll()
                throw e
            } finally {
                resetState()
            }
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val byteArray = baos.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT or Base64.NO_WRAP)
    }

    fun resetState() {
        _isProcessing.value = false
        _ocrState.value = OcrState.Idle  // Or _extractedItems.value = emptyList() if you have it
    }


        private suspend fun callPythonOcrApi(base64Image: String): ReceiptData {
        Log.d("UploadViewModel", "Starting API call to $ocrApiUrl")
        Log.d("UploadViewModel", "Base64 preview: ${base64Image.take(50)}... (length: ${base64Image.length})")

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)  // Short timeout for connect
            .readTimeout(90, TimeUnit.SECONDS)  // Read timeout
            .writeTimeout(20, TimeUnit.SECONDS)  // Write timeout
            .build()

        val json = JSONObject().apply { put("image", base64Image) }
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(ocrApiUrl)
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
            return ReceiptData(
                merchant_name = responseJson.optString("merchant_name"),
                merchant_address = responseJson.optString("merchant_address"),
                transaction_date = responseJson.optString("transaction_date"),
                transaction_time = responseJson.optString("transaction_time"),
                total_amount = responseJson.optDouble("total_amount"),
                line_items = (0 until (responseJson.optJSONArray("line_items")?.length() ?: 0)).map { i ->
                    val itemJson = responseJson.optJSONArray("line_items")?.getJSONObject(i)
                    LineItem(
                        item_name = itemJson?.optString("item_name"),
                        item_quantity = itemJson?.optInt("item_quantity"),
                        item_price = itemJson?.optDouble("item_price"),
                        item_total = itemJson?.optDouble("item_total")
                    )
                }
            )
        } finally {
            response.close()
        }
    }
}