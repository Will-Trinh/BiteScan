// ItemsRepositoryImpl.kt
package com.example.inventory.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.lang.IllegalStateException


class OfflineItemsRepository (val itemDao: ItemDao) : ItemsRepository {
    override fun getAllItemsStream(): Flow<List<Item>> = itemDao.getAllItems()

    override fun getItemStream(id: Int): Flow<Item?> = itemDao.getItem(id)

    override fun searchItems(query: String): Flow<List<Item>> = itemDao.searchItems(query)

    override suspend fun deleteItem(item: Item) = itemDao.delete(item)

    override suspend fun updateItem(item: Item) = itemDao.update(item)

    override fun getItemsForReceipt(receiptId: Int): Flow<List<Item>> = itemDao.getItemsForReceipt(receiptId)

    override suspend fun insertItem(item: Item): Item {
        val id = itemDao.insert(item)
        return itemDao.getItemStream(id.toInt()).first()
            ?: throw IllegalStateException("Item with id $id not found after insertion")
    }

    /**
     * Retrieve items for a specific user (joined via receipts).
     */
    override fun getItemsForUser(userId: Int): Flow<List<Item>> = itemDao.getItemsForUser(userId)

    /**
     * Retrieve items for a user by category (joined via receipts).
     */
    override fun getItemsForUserByCategory(userId: Int, category: String): Flow<List<Item>> =
        itemDao.getItemsForUserByCategory(userId, category)

    companion object
}