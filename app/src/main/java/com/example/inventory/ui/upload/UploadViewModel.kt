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
    private val _ocrState = MutableStateFlow<OcrState>(OcrState.Idle)
    val ocrState: StateFlow<OcrState> = _ocrState

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    // For emulator
    private val ocrApiUrl = "http://10.0.2.2:8000/ocr"  // Changed port

    // For physical device (replace with your IP)
//    private val ocrApiUrl = "http://192.168.1.100:5000/ocr"

    fun processReceiptImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isProcessing.value = true
            _ocrState.value = OcrState.Loading
            try {
                val base64Image = withContext(Dispatchers.Default) { convertBitmapToBase64(bitmap) }
                val receiptData = withContext(Dispatchers.IO) { callPythonOcrApi(base64Image) }
                _ocrState.value = OcrState.Success(receiptData)
            } catch (e: Exception) {
                Log.e("UploadViewModel", "OCR failed: ${e.message}")
                _ocrState.value = OcrState.Error(e.message ?: "Unknown error")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    suspend fun saveReceiptAndItems(receiptData: ReceiptData, userId: Int): Int {
        return withContext(Dispatchers.IO) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val parsedDate = Date(sdf.parse(receiptData.transaction_date ?: "")?.time ?: System.currentTimeMillis())

                val newReceipt = Receipt(
                    receiptId = 0,
                    userId = userId,
                    source = receiptData.merchant_name ?: "Unknown",
                    date = parsedDate,
                    status = "Pending"
                )
                val newReceiptId = receiptsRepository.insertReceipt(newReceipt).toInt()

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
                    itemsRepository.insertItem(newItem)
                }

                Log.d("UploadViewModel", "Saved receipt ID: $newReceiptId")
                newReceiptId
            } catch (e: Exception) {
                Log.e("UploadViewModel", "Save failed: ${e.message}")
                throw e
            }
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val byteArray = baos.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT or Base64.NO_WRAP)
    }


    private suspend fun callPythonOcrApi(base64Image: String): ReceiptData {
        val client = OkHttpClient()
        val json = JSONObject().apply { put("image", base64Image) }
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(ocrApiUrl)
            .post(body)
            .build()

        Log.d("UploadViewModel", "Request URL: $ocrApiUrl")
        Log.d("UploadViewModel", "Request Body (first 100 chars): ${json.toString().take(100)}...")  // Log JSON snippet
        Log.d("UploadViewModel", "Base64 length: ${base64Image.length}")

        val response = client.newCall(request).execute()
        Log.d("UploadViewModel", "Response code: ${response.code}, message: ${response.message}")
        Log.d("UploadViewModel", "Response headers: ${response.headers}")

        val responseBody = response.body?.string() ?: "{}"
        Log.d("UploadViewModel", "Full Response Body: $responseBody")  // Log full JSON/error

        try {
            if (!response.isSuccessful) throw Exception("API error: ${response.code} - ${response.message}. Body: $responseBody")
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