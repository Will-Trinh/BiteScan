package com.example.inventory.data

import org.json.JSONArray
import com.example.inventory.ui.AppViewModel
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.sql.Date
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
class OnlineReceiptsRepository(
    private val receiptsRepository: OfflineReceiptsRepository,
    private val itemsRepository: ItemsRepository,
    private val appViewModel: AppViewModel
) {
    companion object {
        fun create(
            receiptsRepository: OfflineReceiptsRepository,
            itemsRepository: ItemsRepository,
            appViewModel: AppViewModel
        ): OnlineReceiptsRepository {
            return OnlineReceiptsRepository(receiptsRepository, itemsRepository, appViewModel)
        }
    }
    // API ReceiptResponse
    suspend fun syncReceiptsFromServer() {
        val userId = appViewModel.userId.value
            ?: throw IllegalStateException("User ID is not available!")

        Log.d("OnlineReceipts", " start sync receipts for userId=$userId ")

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        val json = JSONObject().apply { put("user_id", userId) }
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("http://HoangHandsome.com/api/receipts")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: "{}"
        Log.d("OnlineReceipts", "Response code: ${response.code}, body: $responseBody")

        try {
            if (!response.isSuccessful) {
                throw Exception("Sync failed: ${response.code} - ${response.message}")
            }

            val jsonResponse = JSONObject(responseBody)

            //{ "receipts": [ { "receipt": {...}, "items": [...] }, ... ] }
            val receiptsArray = jsonResponse.getJSONArray("receipts")
            for (i in 0 until receiptsArray.length()) {
                val receiptObj = receiptsArray.getJSONObject(i)
                val receiptJson = receiptObj.getJSONObject("receipt")
                val itemsArray = receiptObj.getJSONArray("items")

                val receipt = Receipt(
                    userId = userId,
                    date = Date(receiptJson.getLong("date")),  // timestamp millis
                    source = receiptJson.optString("source", "Unknown"),
                    status = receiptJson.optString("status", "Pending")
                )
                val receiptId = receiptsRepository.insertReceipt(receipt).toInt()
                Log.d("OnlineReceipts", "Inserted receiptId=$receiptId")

                for (j in 0 until itemsArray.length()) {
                    val itemJson = itemsArray.getJSONObject(j)
                    val item = Item(
                        name = itemJson.optString("name", "Unknown"),
                        price = itemJson.optDouble("price", 0.0),
                        quantity = itemJson.optDouble("quantity", 1.0).toFloat(),
                        date = Date(itemJson.getLong("date")),
                        store = itemJson.optString("store", "Unknown"),
                        category = itemJson.optString("category", "Unknown"),
                        receiptId = receiptId,
                        calories = itemJson.optDouble("calories", 0.0),
                        protein = itemJson.optDouble("protein", 0.0),
                        carbs = itemJson.optDouble("carbs", 0.0),
                        fats = itemJson.optDouble("fats", 0.0)
                    )
                    val itemId = itemsRepository.insertItem(item)
                    Log.d("OnlineReceipts", "Inserted itemId=$itemId")
                }
            }

            Log.d("OnlineReceipts", "Sync completed. Receipts count: ${receiptsArray.length()}")

        } finally {
            response.close()
        }
    }
    // API UploadReceipt
    suspend fun uploadReceiptToServer(receiptId: Int) {
        val userId = appViewModel.userId.value
            ?: throw IllegalStateException("User ID is not available!")

        val receipt = receiptsRepository.getReceipt(receiptId)
            ?: throw IllegalStateException("Receipt không tồn tại: $receiptId")

        val items = itemsRepository.getItemsForReceipt(receiptId).first()


        Log.d("OnlineReceipts", "uploading receiptId=$receiptId for userId=$userId")

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        // { "user_id": 123, "receipt": {...}, "items": [...] }
        val receiptJson = JSONObject().apply {
            put("date", receipt.date.time)
            put("source", receipt.source)
            put("status", receipt.status)
        }
        val itemsArray = JSONArray()

        for (item in items) {
            val itemJson = JSONObject().apply {
                put("name", item.name)
                put("price", item.price)
                put("quantity", item.quantity)
                put("date", item.date.time)
                put("store", item.store)
                put("category", item.category)
                put("calories", item.calories)
                put("protein", item.protein)
                put("carbs", item.carbs)
                put("fats", item.fats)
            }
            itemsArray.put(itemJson)
        }

        val requestJson = JSONObject().apply {
            put("user_id", userId)
            put("receipt", receiptJson)
            put("items", itemsArray)
        }

        val body = requestJson.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("http://HoangHandsome.com/api/upload-receipt") // URL API upload receipt
            .post(body)
            .addHeader("User-Agent", "AndroidApp/1.0")
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: "{}"
        Log.d("OnlineReceipts", "Upload response: ${response.code}, body: $responseBody")

        try {
            if (!response.isSuccessful) {
                throw Exception("Upload failed: ${response.code} - ${response.message}")
            }

            Log.d("OnlineReceipts", "Upload receiptId=$receiptId completed.")

        } finally {
            response.close()
        }
    }
}