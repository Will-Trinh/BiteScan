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

class OnlineUsersRepository(
    private val userDao: UserDao

) : UsersRepository {
    override fun getAllUsersStream(): Flow<List<User>> = userDao.getAllUsers()
    override fun getUser(id: Int): Flow<User?> = userDao.getUser(id)
    override fun searchUsers(query: String): Flow<List<User>> = userDao.searchUsers(query)
    override suspend fun deleteUser(user: User) = userDao.deleteUser(user)
    override suspend fun updateUser(user: User) = userDao.updateUser(user)
    override suspend fun insertUser(user: User): Long = userDao.insertUser(user)
    override suspend fun updateUserDiet(userId: Int, diet: String?) = userDao.updateUserDiet(userId, diet)
    override fun getReceiptsForUser(userId: Int): Flow<List<Receipt>> = userDao.getReceiptsForUser(userId)

    override suspend fun deleteAllData() {}
    suspend fun updateUserToServer(userId: Int) = withContext(Dispatchers.IO) {
        val user = userDao.getUser(userId).first()?: throw IllegalStateException("User not found: $userId")


        Log.d("OnlineReceipts", "update for userId=$userId to server")

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        val userJson = JSONObject().apply {
            put("id", user.userId)
            put("username", user.username)
            put("email", user.email)
            put("phone", user.phone)
            put("diet", user.diet)
        }


        val requestJson = JSONObject().apply {
            put("user", userJson)
        }

        val body = requestJson.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("http://129.146.23.142:8080/users/$userId/") // URL API update user
            .put(body)
            .addHeader("User-Agent", "AndroidApp/1.0")
            .build()

        Log.d("OnlineUserRepository", "sending post request now")

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: "{}"
        Log.d("OnlineUserRepository", "Upload response: ${response.code}, body: $responseBody")

        try {
            if (!response.isSuccessful) {
                throw Exception("Upload failed: ${response.code} - ${response.message}")
            }

            Log.d("OnlineUserRepository", "Upload UserId=$userId completed.")

        } finally {
            response.close()
        }
    }
}