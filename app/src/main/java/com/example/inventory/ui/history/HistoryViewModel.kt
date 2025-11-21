package com.example.inventory.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import com.example.inventory.data.Receipt
import com.example.inventory.data.ReceiptsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import com.example.inventory.data.ReceiptsRepositoryImpl
import android.util.Log
import com.example.inventory.data.OfflineReceiptsRepository
import kotlinx.coroutines.Dispatchers

class ReceiptViewModel(
    private val receiptsRepository: ReceiptsRepository,
    private val itemsRepository: ItemsRepository,
) : ViewModel() {
    private val _receiptUiState = MutableStateFlow(ReceiptUiState())
    val receiptUiState: StateFlow<ReceiptUiState> = _receiptUiState.asStateFlow()
    fun loadReceiptsUser(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            receiptsRepository.getReceiptsForUser(userId).collect { receipts ->
                val summaryMap = mutableMapOf<Int, ReceiptSummary>()

                receipts.forEach { receipt ->
                    val items = itemsRepository.getItemsForReceipt(receipt.receiptId).first()
                    val totalPrice = items.sumOf { it.price * it.quantity.toDouble() }
                    val totalItems = items.size
                    summaryMap[receipt.receiptId] = ReceiptSummary(
                        totalPrice = totalPrice,
                        itemCount = totalItems
                    )

                }
                _receiptUiState.value = ReceiptUiState(
                    syncStatus = SyncStatus.SUCCESS,
                    receiptList = receipts,
                    receiptSummary = summaryMap,
                    dayAndPrice = _receiptUiState.value.dayAndPrice
                )
                calculateDayAndPrice(receipts, summaryMap)
            }
        }
    }


    private val _itemsByReceipt = MutableStateFlow<Map<Int, List<Item>>>(emptyMap())

    fun loadItems(receiptId: Int) {
        viewModelScope.launch {
            itemsRepository.getItemsForReceipt(receiptId).collect { items ->
                val updated = _itemsByReceipt.value.toMutableMap()
                updated[receiptId] = items
                _itemsByReceipt.value = updated
            }
        }
    }


    fun calculateTotalPrice(items: List<Item>): Double {
        val totalPrice = items.sumOf { it.price * it.quantity.toDouble() }  // Calculate total price
        return totalPrice
    }

    private fun calculateDayAndPrice(
        receiptList: List<Receipt>,
        summaryMap: Map<Int, ReceiptSummary>
    ) {
        viewModelScope.launch {
            val dayAndPrice = mutableListOf<Pair<String, Double>>()
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val today = calendar.timeInMillis

            val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            for (i in 6 downTo 0) {
                calendar.timeInMillis = today
                calendar.add(Calendar.DAY_OF_MONTH, -i)
                val dayStart = calendar.timeInMillis
                val dayEnd = dayStart + 24 * 60 * 60 * 1000

                val dayName = dayNames[calendar.get(Calendar.DAY_OF_WEEK) - 1]

                val totalPrice = receiptList
                    .filter { it.date.time in dayStart until dayEnd }
                    .sumOf { summaryMap[it.receiptId]?.totalPrice ?: 0.0 }

                dayAndPrice.add(Pair(dayName, totalPrice))
            }

            val ordered = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                .map { day -> dayAndPrice.find { it.first == day } ?: (day to 0.0) }

            _receiptUiState.value = _receiptUiState.value.copy(
                dayAndPrice = ordered
            )
        }
    }
}
data class ReceiptUiState(
    val syncStatus: SyncStatus = SyncStatus.LOADING,
    val receiptList: List<Receipt> = emptyList(),
    val itemList: List<Item> = emptyList(),
    val dayAndPrice: List<Pair<String, Double>> = emptyList(),
    val receiptSummary: Map<Int, ReceiptSummary> = emptyMap(),
)

enum class SyncStatus {
    LOADING,    // doing sync
    SUCCESS,    // Sync OK
    ERROR       // 404, server
}
data class ReceiptSummary(
    val totalPrice: Double,
    val itemCount: Int
)