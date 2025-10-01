package com.example.inventory.ui.userdata

import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import com.example.inventory.data.Receipt
import com.example.inventory.data.ReceiptsRepository
import com.example.inventory.data.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.sql.Date
import java.util.concurrent.atomic.AtomicInteger


// Fake ItemsRepository preview
class FakeItemsRepository : ItemsRepository {
    private val idCounter = AtomicInteger(3) // Start ID after initial fake items
    private val fakeItems = mutableListOf(
        Item(id = 1, name = "Milk", price = 2.5, quantity = 1f, date = Date(System.currentTimeMillis()), store = "Cosco", category = "Dairy", receiptId = 1),
        Item(id = 2, name = "Bread", price = 3.0, quantity = 2f, date = Date(System.currentTimeMillis()), store = "Cosco", category = "Bakery", receiptId = 1),
        Item(id = 3, name = "Apple", price = 1.0, quantity = 5f, date = Date(System.currentTimeMillis()), store = "Cosco", category = "Fruit", receiptId = 1)
    )

    override fun getAllItemsStream(): Flow<List<Item>> = flowOf(fakeItems)

    override fun getItemStream(id: Int): Flow<Item?> = flowOf(fakeItems.find { it.id == id })

    override fun getItemsForReceipt(receiptId: Int): Flow<List<Item>> = flowOf(fakeItems.filter { it.receiptId == receiptId })

    override fun searchItems(query: String): Flow<List<Item>> = flowOf(
        fakeItems.filter { it.name.contains(query, ignoreCase = true) } // Mimics LIKE query
    )

    override suspend fun insertItem(item: Item): Item {
        val newItem = item.copy(id = idCounter.incrementAndGet()) // Simulate auto-generated ID
        fakeItems.add(newItem)
        return newItem
    }

    override suspend fun deleteItem(item: Item) {
        fakeItems.removeIf { it.id == item.id }
    }

    override suspend fun updateItem(item: Item) {
        val index = fakeItems.indexOfFirst { it.id == item.id }
        if (index != -1) {
            fakeItems[index] = item
        }
    }
}

// Fake ReceiptsRepository cho preview
class FakeReceiptsRepository : ReceiptsRepository {
    private val receiptIdCounter = AtomicInteger(0) // Start ID after initial fake receipts
    private val fakeReceipts = mutableListOf(
        Receipt(receiptId = 1, userId = 0, date = Date(System.currentTimeMillis()), source = "Lalala", status = "Completed"),
        Receipt(receiptId = 2, userId = 0, date = Date(System.currentTimeMillis() -86400000), source = "Cosco", status = "Completed"),
        Receipt(receiptId = 3, userId = 0, date = Date(System.currentTimeMillis() -172800000), source = "Walmart", status = "Completed"),
        Receipt(receiptId = 4, userId = 0, date = Date(System.currentTimeMillis() -259200000), source = "Target", status = "Completed")
    )

    override fun getAllReceiptsStream(): Flow<List<Receipt>> = flowOf(fakeReceipts)

    override fun getReceiptsForUser(userId: Int): Flow<List<Receipt>> = flowOf(fakeReceipts.filter { it.userId == userId })

    override fun searchReceipts(query: String): Flow<List<Receipt>> = flowOf(
        fakeReceipts.filter { it.source.contains(query, ignoreCase = true) || it.status.contains(query, ignoreCase = true) } // Mimics LIKE query on source/status
    )

    override suspend fun getReceipt(id: Int): Receipt? = fakeReceipts.find { it.receiptId == id }

    override suspend fun insertReceipt(receipt: Receipt): Long {
        val newReceipt = receipt.copy(receiptId = receiptIdCounter.incrementAndGet())
        fakeReceipts.add(newReceipt)
        return newReceipt.receiptId.toLong() // Simulate returning generated ID
    }

    override suspend fun deleteReceipt(receipt: Receipt) {
        fakeReceipts.removeIf { it.receiptId == receipt.receiptId }
    }

    override suspend fun updateReceipt(receipt: Receipt) {
        val index = fakeReceipts.indexOfFirst { it.receiptId == receipt.receiptId }
        if (index != -1) {
            fakeReceipts[index] = receipt
        }
    }
}

// fake UIuser

val fakeUIuser = User(userId = 0, username = "previewUser", password = "hashedPass", phone = "1234567890")

