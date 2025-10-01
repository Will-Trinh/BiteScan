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
import java.sql.Date


class EditReceiptViewModel(
    private val itemsRepository: ItemsRepository,
    private val receiptsRepository: ReceiptsRepository,
) : ViewModel() {
    private val _editUiState = MutableStateFlow(EditUiState())
    val editUiState: StateFlow<EditUiState> = _editUiState.asStateFlow()

    fun loadItems(receiptId: Int) {
        viewModelScope.launch {
            itemsRepository.getItemsForReceipt(receiptId).collect { items ->
                _editUiState.value = _editUiState.value.copy(
                    itemList = items.sortedBy { it.id },
                    totalItems = calculateTotalItem(items),
                    totalPrice = calculateTotalPrice(items)
                )
            }
        }
    }

    fun loadReceipt(receiptId: Int) {
        viewModelScope.launch {
            val receipt = receiptsRepository.getReceipt(receiptId)
            if (receipt != null) {
                _editUiState.value = _editUiState.value.copy(receipt = receipt)
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

    fun updateItem(index: Int, updatedItem: Item) {
        val currentItems = _editUiState.value.itemList.toMutableList()
        currentItems[index] = updatedItem
        _editUiState.value = _editUiState.value.copy(itemList = currentItems.sortedBy { it.id })
        viewModelScope.launch {
            itemsRepository.updateItem(updatedItem)
        }
    }

    //return date of receipt
    fun receiveDate(receiptId: Int): String {
        val currentReceipt = _editUiState.value.receipt
        val receiptDate = currentReceipt?.date
        return (receiptDate).toString()
    }

    fun updateSource(newSource: String) {
        val currentReceipt = _editUiState.value.receipt
        if (currentReceipt != null) {
            _editUiState.value = _editUiState.value.copy(
                receipt = currentReceipt.copy(source = newSource)
            )
        }
    }

    fun addItem(receiptId: Int, item: Item) {
        val newItem = Item(
            id = 0,
            name = item.name,
            price = item.price,
            quantity = item.quantity,
            date = Date(System.currentTimeMillis()),
            store = "",
            category = "",
            receiptId = receiptId
        )
        viewModelScope.launch {
            itemsRepository.insertItem(newItem)
            loadItems(receiptId) // Refresh item list
        }
    }

    suspend fun saveReceipt(receipt: Receipt) {
        receiptsRepository.updateReceipt(receipt)
    }
}

data class EditUiState(
    val itemList: List<Item> = listOf(),
    val totalItems: Int = 0,
    val totalPrice: Double = 0.0,
    val receipt: Receipt? = null,
)