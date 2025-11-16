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
        //_loginResult.value = LoginResult(success = true, uid = 1, phone = "123456789", email = "Tran@gmail.com", username = "Tran")
        //val user = User(userId = 1, username = "Tran", email = "Tran@gmail.com", phone = "123456789")
        //viewModelScope.launch { usersRepository.insertUser(user)}
        //end testing

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val apiUrl = URL("http://129.146.23.142:8080/users/login")
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

                    if (responseCode == 200) {
                        val raw = conn.inputStream.bufferedReader().use { it.readText() }
                        Log.d("API_RAW_RESPONSE", raw)

                        // Phân tích JSON trả về
                        val jsonResponse = JSONObject(raw)
                        val userId = jsonResponse.getInt("id")
                        val username = jsonResponse.getString("username")
                        val email = jsonResponse.getString("email")
                        // password không cần lấy, bỏ qua cho an toàn

                        return@withContext LoginResult(
                            success = true,
                            uid = userId,
                            username = username,
                            email = email,
                            phone = null, // nếu backend có thì thêm
                            errorMessage = "Login successful!" // hoặc để null
                        )
                    } else if (responseCode == 422) {

                        val errorText = conn.errorStream?.bufferedReader()?.use { it.readText() }
                        val detail = try {
                            JSONObject(errorText).getJSONArray("detail")
                                .getJSONObject(0)
                                .getString("msg")
                        } catch (e: Exception) {
                            "Validation error"
                        }

                        return@withContext LoginResult(
                            success = false,
                            errorMessage = detail,
                            email = ""
                        )

                    } else {

                        return@withContext LoginResult(
                            success = false,
                            errorMessage = "Login failed ($responseCode)",
                            email = ""
                        )
                    }
                } catch (e: Exception) {
                    Log.e("LoginVM", "Error during login: ${e.message}", e)
                    LoginResult(success = false, errorMessage = "Connect failed: ${e.message}", uid = 0, phone = null, email = "", username = "")
                }
            }
            //change when api available
            _loginResult.value = result
            _isLoading.value = false
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


