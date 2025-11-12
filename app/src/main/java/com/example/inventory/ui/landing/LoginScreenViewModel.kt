package com.example.inventory.ui.landing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.User
import com.example.inventory.data.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import com.example.inventory.data.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginScreenViewModel(
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult: StateFlow<LoginResult?> = _loginResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun checkLogin(email: String, password: String) {
        _isLoading.value = true
        _loginResult.value = null
        //for testing purpose
        _loginResult.value = LoginResult(success = true, uid = 1, phone = "123456789", email = "Tran@gmail.com", username = "Tran")
        val user = User(userId = 1, username = "Tran", email = "Tran@gmail.com", phone = "123456789")
        viewModelScope.launch { usersRepository.insertUser(user)}
        //end testing

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val apiUrl = URL("https://abc.com/api/login")
                    val conn = apiUrl.openConnection() as HttpURLConnection
                    //Set timeout (10s connect, 15s response)
                    conn.connectTimeout = 10_000
                    conn.readTimeout = 15_000
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.doOutput = true

                    val json = JSONObject().apply {
                        put("email", email)
                        put("password", password)
                    }
                    OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }

                    val responseCode = conn.responseCode
                    Log.d("LoginVM", "Response code: $responseCode")

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                        val jsonResponse = JSONObject(responseText)
                        val success = jsonResponse.getBoolean("success") // Dùng getBoolean

                        if (success) {
                            val uid = jsonResponse.getInt("uid")
                            val phone = jsonResponse.getString("phone")
                            val email = jsonResponse.getString("email")
                            val username = jsonResponse.getString("username")


                            val user = User(userId = uid, username = username, email = email, phone = phone)
                            usersRepository.insertUser(user)

                            LoginResult(success = true, uid = uid, phone = phone, email = email, username = username)
                        } else {
                            val msg = jsonResponse.optString("message", "login failed")
                            LoginResult(success = false, errorMessage = msg, uid = 0, phone = null, email = "", username = "")
                        }
                    } else {
                        LoginResult(success = false, errorMessage = "Connect failed with response code: $responseCode", uid = 0, phone = null, email = "", username = "")
                    }
                } catch (e: Exception) {
                    Log.e("LoginVM", "Error during login: ${e.message}", e)
                    LoginResult(success = false, errorMessage = "Connect failed: ${e.message}", uid = 0, phone = null, email = "", username = "")
                }
            }
            //change when api available
            //_loginResult.value = result
            //_isLoading.value = false
        }
    }
}

data class LoginResult(
    val success: Boolean,
    val errorMessage: String? = null,
    val uid: Int = 0,
    val phone: String? = null,
    val email: String,
    val username: String? = null,
)


