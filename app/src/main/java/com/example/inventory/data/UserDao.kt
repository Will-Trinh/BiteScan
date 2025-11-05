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
 * Database access object for Users.
 */
@Dao
interface UserDao {

    @Query("SELECT * from users ORDER BY username ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * from users WHERE userId = :id")
    fun getUser(id: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE username LIKE :query OR phone LIKE :query")
    fun searchUsers(query: String): Flow<List<User>>

    @Query("SELECT * FROM receipts WHERE userId = :userId")
    fun getReceiptsForUser(userId: Int): Flow<List<Receipt>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User): Long

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()



    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}