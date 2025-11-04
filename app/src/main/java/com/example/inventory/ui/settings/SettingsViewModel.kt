package com.example.inventory.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.inventory.ui.AppViewModel
import com.example.inventory.data.UsersRepository


class SettingsViewModel(
    private val repository: UsersRepository,
    private val appViewModel: AppViewModel
) : ViewModel() {
    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId
    private val _logoutCompleted = MutableStateFlow(false)
    val logoutCompleted: StateFlow<Boolean> = _logoutCompleted

    init {
        // Load the user ID when the ViewModel is created
        viewModelScope.launch {
            val userId = appViewModel.userId
            if (userId != null) {
                _userId.value = userId.toString()
            } else {
                _userId.value = null
                _logoutCompleted.value = true
                android.util.Log.d("SettingsViewModel", "fail to get UID")

            }
        }
    }

    // Method to update user ID if current user changes
    fun setCurrentUserId(userId: Int) {
        viewModelScope.launch {
            val user = appViewModel.userId
            if (user != null) {
                _userId.value = user.toString()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                appViewModel.clearUserId()
                //print the log
                android.util.Log.d("SettingsViewModel", "Cleared UID")
                repository.deleteAllData()
                android.util.Log.d("SettingsViewModel", "Cleared DB")
                _logoutCompleted.value = true
            }
        }
    }


    fun launchUrl(context: Context, url: String) {
        viewModelScope.launch {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Required for starting activity from background
                context.startActivity(intent)
            } catch (e: Exception) {
                // Log or handle the error (e.g., no activity to handle intent)
                android.util.Log.e("SettingsViewModel", "Failed to launch URL: ${e.message}")
            }
        }
    }
}