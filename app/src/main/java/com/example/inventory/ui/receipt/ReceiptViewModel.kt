package com.example.inventory.ui.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import com.example.inventory.data.Receipt
import com.example.inventory.data.ReceiptsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ReceiptViewModel(
    private val receiptsRepository: ReceiptsRepository,
    private val itemsRepository: ItemsRepository,
    private val userId: Int
) : ViewModel() {
    private val _receiptUiState = MutableStateFlow(ReceiptUiState())
    val receiptUiState: StateFlow<ReceiptUiState> = _receiptUiState.asStateFlow()

    fun loadReceiptsUser() {
        viewModelScope.launch {
            receiptsRepository.getReceiptsForUser(userId)
                .collect { receipts ->
                    println("Receipts for user $userId: $receipts")
                    _receiptUiState.value = ReceiptUiState(receiptList = receipts)
                }
        }
    }

    fun loadItems(receiptId: Int) {
        viewModelScope.launch {
            itemsRepository.getItemsForReceipt(receiptId).collect { items ->
                _receiptUiState.value = _receiptUiState.value.copy(
                    itemList = items.sortedBy { it.id },
                )
            }
        }
    }


    fun calculateTotalPrice(items: List<Item>): Double {
        val totalPrice = items.sumOf { it.price }  // Calculate total price
        return totalPrice
    }

    fun calculateTotalItem(items: List<Item>): Int {
        return items.count()
    }

}

data class ReceiptUiState(
    val receiptList: List<Receipt> = emptyList(),
    val itemList: List<Item> = emptyList(),
)