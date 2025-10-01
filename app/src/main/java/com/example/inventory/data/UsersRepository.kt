
package com.example.inventory.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository for Users.
 */
interface UsersRepository {
    fun getAllUsersStream(): Flow<List<User>>
    fun getUser(id: Int): Flow<User?>
    fun searchUsers(query: String): Flow<List<User>>
    suspend fun insertUser(user: User): Long
    suspend fun deleteUser(user: User)
    suspend fun updateUser(user: User)
    fun getReceiptsForUser(userId: Int): Flow<List<Receipt>>
}