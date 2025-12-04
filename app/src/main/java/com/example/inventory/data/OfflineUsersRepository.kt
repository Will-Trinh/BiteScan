package com.example.inventory.data
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OfflineUsersRepository(
    private val userDao: UserDao,
    private val receiptDao: ReceiptDao,
    private val itemDao: ItemDao,
    private val recipeDao: RecipeDao

) : UsersRepository {
    override fun getAllUsersStream(): Flow<List<User>> = userDao.getAllUsers()
    override fun getUser(id: Int): Flow<User?> = userDao.getUser(id)
    override fun searchUsers(query: String): Flow<List<User>> = userDao.searchUsers(query)
    override suspend fun deleteUser(user: User) = userDao.deleteUser(user)
    override suspend fun updateUser(user: User) = userDao.updateUser(user)
    override suspend fun insertUser(user: User): Long = userDao.insertUser(user)
    override suspend fun updateUserDiet(userId: Int, diet: String?) = userDao.updateUserDiet(userId, diet)
    override fun getReceiptsForUser(userId: Int): Flow<List<Receipt>> = userDao.getReceiptsForUser(userId)

    override suspend fun deleteAllData() {
        withContext(Dispatchers.IO) {
            userDao.deleteAllUsers()
            itemDao.deleteAllItems()
            receiptDao.deleteAllReceipts()
            recipeDao.deleteAllRecipes()
        }
    }
}