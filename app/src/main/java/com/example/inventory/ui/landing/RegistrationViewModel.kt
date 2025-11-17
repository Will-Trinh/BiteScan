package com.example.inventory.ui.landing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.User
import com.example.inventory.data.UserDao
import com.example.inventory.data.UsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RegistrationViewModel(
) : ViewModel() {

    private val _signUpResult = MutableStateFlow<SignUpResult?>(null)
    val signUpResult: StateFlow<SignUpResult?> = _signUpResult

    private val _isLoading = MutableStateFlow(false)

    fun checkSignUp(userName:String, email: String, password: String) {
        _isLoading.value = true
        _signUpResult.value = SignUpResult(success = false, signupMessage = "")
        //for testing purpose
        //_signUpResult.value = SignUpResult(success = true, signupMessage = "Sign Up successful")

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val apiUrl = URL("http://129.146.23.142:8080/users/signup")
                    val conn = apiUrl.openConnection() as HttpURLConnection
                    //Set timeout (10s connect, 15s response)
                    conn.connectTimeout = 10_000
                    conn.readTimeout = 15_000
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.doOutput = true

                    val json = JSONObject().apply {
                        put("username", userName)
                        put("email", email)
                        put("password", password)

                    }
                    OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }

                    val responseCode = conn.responseCode
                    Log.d("LoginVM", "Response code: $responseCode")

                    if (responseCode in 200..299) {

                        val responseText = conn.inputStream?.bufferedReader()?.use { it.readText() }

                        // API "User created" ?
                        SignUpResult(
                            success = true,
                            signupMessage = responseText ?: "Sign up successful"
                        )

                    } else {

                        val errorText = conn.errorStream?.bufferedReader()?.use { it.readText() }

                        SignUpResult(
                            success = false,
                            signupMessage = errorText ?: "Error $responseCode"
                        )
                    }

                } catch (e: Exception) {
                    Log.e("LoginVM", "Error during signup: ${e.message}", e)
                    SignUpResult(false, "Connect failed: ${e.message}")
                }
            }
            //change when api available
            _signUpResult.value = result
            _isLoading.value = false
        }
    }
}

data class SignUpResult(
    val success: Boolean,
    val signupMessage: String? = null,
)