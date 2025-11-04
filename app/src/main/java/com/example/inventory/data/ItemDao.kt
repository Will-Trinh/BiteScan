/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Database access object to access the Inventory database
 */
@Dao
interface ItemDao {

    @Query("SELECT * from items ORDER BY name ASC")
    fun getAllItems(): Flow<List<Item>>

    @Query("SELECT * from items WHERE id = :id")
    fun getItem(id: Int): Flow<Item>

    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemStream(id: Int): Flow<Item?>


    @Query("SELECT * FROM items WHERE name = :query OR LOWER(name) LIKE LOWER(:query)")
    fun searchItems(query: String): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE receiptId = :receiptId ORDER BY name ASC")
    fun getItemsForReceipt(receiptId: Int): Flow<List<Item>>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Item): Long

    @Query("DELETE FROM items")
    suspend fun deleteAllItems()
    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)

    /**
     * Retrieve items for a specific user (joined via receipts.userId).
     */
    @Query("""
    SELECT i.* FROM items i 
    INNER JOIN receipts r ON i.receiptId = r.receiptId 
    WHERE r.userId = :userId 
    ORDER BY i.name ASC
""")
    fun getItemsForUser(userId: Int): Flow<List<Item>>

    /**
     * Retrieve items for a user by category (joined via receipts).
     */
    @Query("""
    SELECT i.* FROM items i 
    INNER JOIN receipts r ON i.receiptId = r.receiptId 
    WHERE r.userId = :userId AND i.category = :category 
    ORDER BY i.name ASC
""")
    fun getItemsForUserByCategory(userId: Int, category: String): Flow<List<Item>>
}
