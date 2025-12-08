// ItemsRepository.kt
package com.example.inventory.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, and retrieve of [Item] from a given data source.
 */
interface ItemsRepository{
    /**
     * Retrieve all the items from the the given data source.
     */
    fun getAllItemsStream(): Flow<List<Item>>

    /**
     * Retrieve an item from the given data source that matches with the [id].
     */
    fun getItemStream(id: Int): Flow<Item?>

    /**
     * Search items based on query.
     */
    fun searchItems(query: String): Flow<List<Item>>

    /**
     * Delete item from the data source.
     */
    suspend fun deleteItem(item: Item)

    /**
     * Update item in the data source.
     */
    suspend fun updateItem(item: Item)

    /**
     * Retrieve items associated with a specific receipt.
     */
    fun getItemsForReceipt(receiptId: Int): Flow<List<Item>>


    /**
     * Insert item in the data source and return the inserted item with generated ID.
     */
    suspend fun insertItem(item: Item): Long
    suspend fun upsertItem(item: Item): Long


    /**
     * Retrieve items for a specific user (joined via receipts).
     */
    fun getItemsForUser(userId: Int): Flow<List<Item>>

    /**
     * Retrieve items for a user by category (joined via receipts).
     */
    fun getItemsForUserByCategory(userId: Int, category: String): Flow<List<Item>>

    /**
     * Retrieve items for a user with store name from receipt source.
     */
    fun getItemsWithStoreForUser(userId: Int): Flow<List<Item>>


}

