package com.example.inventory.data
import kotlinx.coroutines.flow.Flow

class OfflineUsersRepository(private val userDao: UserDao) : UsersRepository {
    override fun getAllUsersStream(): Flow<List<User>> = userDao.getAllUsers()
    override fun getUser(id: Int): Flow<User?> = userDao.getUser(id)
    override fun searchUsers(query: String): Flow<List<User>> = userDao.searchUsers(query)
    override suspend fun deleteUser(user: User) = userDao.deleteUser(user)
    override suspend fun updateUser(user: User) = userDao.updateUser(user)
    override suspend fun insertUser(user: User): Long = userDao.insertUser(user)
    override fun getReceiptsForUser(userId: Int): Flow<List<Receipt>> = userDao.getReceiptsForUser(userId)
}