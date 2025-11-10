package com.example.inventory.ui.userdata

import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import com.example.inventory.data.Receipt
import com.example.inventory.data.ReceiptsRepository
import com.example.inventory.data.User
import com.example.inventory.data.UsersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.sql.Date
import java.util.concurrent.atomic.AtomicInteger

// Fake ReceiptsRepository for preview (updated userId to 1 for consistency)
class FakeReceiptsRepository : ReceiptsRepository {
    private val receiptIdCounter = AtomicInteger(4) // Start after initial fake receipts
    private val fakeReceipts = mutableListOf(
        Receipt(receiptId = 1, userId = 1, date = Date(System.currentTimeMillis()), source = "Lalala", status = "Completed"),
        Receipt(receiptId = 2, userId = 1, date = Date(System.currentTimeMillis() - 86400000), source = "Cosco", status = "Completed"),
        Receipt(receiptId = 3, userId = 1, date = Date(System.currentTimeMillis() - 172800000), source = "Walmart", status = "Completed"),
        Receipt(receiptId = 4, userId = 1, date = Date(System.currentTimeMillis() - 259200000), source = "Target", status = "Completed")
    )

    override fun getAllReceiptsStream(): Flow<List<Receipt>> = flowOf(fakeReceipts)

    override fun getReceiptsForUser(userId: Int): Flow<List<Receipt>> = flowOf(fakeReceipts.filter { it.userId == userId })

    override fun searchReceipts(query: String): Flow<List<Receipt>> = flowOf(
        fakeReceipts.filter { it.source.contains(query, ignoreCase = true) || it.status.contains(query, ignoreCase = true) }
    )

    override suspend fun getReceipt(id: Int): Receipt? = fakeReceipts.find { it.receiptId == id }

    override suspend fun insertReceipt(receipt: Receipt): Long {
        val newReceipt = receipt.copy(receiptId = receiptIdCounter.incrementAndGet())
        fakeReceipts.add(newReceipt)
        return newReceipt.receiptId.toLong()
    }

    override suspend fun deleteReceipt(receipt: Receipt) {
        fakeReceipts.removeIf { it.receiptId == receipt.receiptId }
    }
    fun clear() = fakeReceipts.clear()
    override suspend fun updateReceipt(receipt: Receipt) {
        val index = fakeReceipts.indexOfFirst { it.receiptId == receipt.receiptId }
        if (index != -1) {
            fakeReceipts[index] = receipt
        }
    }
}

// Updated Fake ItemsRepository (self-contained: hardcodes receipt IDs for user 1 to simulate join without calling getReceipt)
class FakeItemsRepository : ItemsRepository {
    private val idCounter = AtomicInteger(3) // Start ID after initial fake items
    private val fakeItems = mutableListOf(
        Item(id = 1, name = "Milk", price = 2.5, quantity = 1f, date = Date(System.currentTimeMillis()), store = "Cosco", category = "Dairy", receiptId = 1),
        Item(id = 2, name = "Bread", price = 3.0, quantity = 2f, date = Date(System.currentTimeMillis()), store = "Cosco", category = "Bakery", receiptId = 1),
        Item(id = 3, name = "Apple", price = 1.0, quantity = 5f, date = Date(System.currentTimeMillis()), store = "Cosco", category = "Fruit", receiptId = 2)
    )

    // Hardcoded receipt IDs for user 1 (simulates join without external getReceipt call)
    private fun getReceiptIdsForUser(userId: Int): List<Int> {
        return if (userId == 1) listOf(1, 2, 3, 4) else emptyList()  // All receipts for user 1
    }

    override fun getAllItemsStream(): Flow<List<Item>> = flowOf(fakeItems)

    override fun getItemStream(id: Int): Flow<Item?> = flowOf(fakeItems.find { it.id == id })

    override fun getItemsForReceipt(receiptId: Int): Flow<List<Item>> = flowOf(fakeItems.filter { it.receiptId == receiptId })

    override fun searchItems(query: String): Flow<List<Item>> = flowOf(
        fakeItems.filter { it.name.contains(query, ignoreCase = true) }
    )
    fun clear() = fakeItems.clear()
    override suspend fun insertItem(item: Item): Long {
        val newItem = item.copy(id = idCounter.incrementAndGet())
        fakeItems.add(newItem)
        return newItem.id.toLong()
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

    // Implement getItemsForUser (filters by hardcoded receipt IDs for user)
    override fun getItemsForUser(userId: Int): Flow<List<Item>> {
        val receiptIdsForUser = getReceiptIdsForUser(userId)
        return flowOf(fakeItems.filter { it.receiptId in receiptIdsForUser })
    }

    // Implement getItemsForUserByCategory (filters by receipt IDs + category)
    override fun getItemsForUserByCategory(userId: Int, category: String): Flow<List<Item>> {
        val receiptIdsForUser = getReceiptIdsForUser(userId)
        return flowOf(fakeItems.filter { it.receiptId in receiptIdsForUser && it.category.equals(category, ignoreCase = true) })
    }
}

class FakeUsersRepository : UsersRepository {
    private val fakeReceiptsRepo = FakeReceiptsRepository()
    private val fakeItemsRepo = FakeItemsRepository()
    // 1. insertUser
    override suspend fun insertUser(user: User): Long {
        return 1L
    }
    // 2. getAllUsersStream
    override fun getAllUsersStream(): Flow<List<User>> = flowOf(listOf(fakeUIuser))

    // 3. getUser
    override fun getUser(id: Int): Flow<User?> = flowOf(if (id == 1) fakeUIuser else null)

    // 4. searchUsers
    override fun searchUsers(query: String): Flow<List<User>> = flowOf(
        if (fakeUIuser.username.contains(query, true)) listOf(fakeUIuser) else emptyList()
    )
    // 5. deleteUser
    override suspend fun deleteUser(user: User) {  }
    // 6. updateUser
    override suspend fun updateUser(user: User) {  }
    // 7. getReceiptsForUser – QUAN TRỌNG NHẤT!
    override fun getReceiptsForUser(userId: Int): Flow<List<Receipt>> {
        return FakeReceiptsRepository().getReceiptsForUser(userId)
    }
    override suspend fun deleteAllData(){
        fakeReceiptsRepo.clear()
        fakeItemsRepo.clear()
    }
}

// Fake UI user (updated to userId = 1 for consistency)
val fakeUIuser = User(userId = 1, username = "previewUser", email = "TranXinhDep@gmail.com", phone = "1234567890")