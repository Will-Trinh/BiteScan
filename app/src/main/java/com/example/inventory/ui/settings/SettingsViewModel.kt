package com.example.inventory.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.OfflineUsersRepository
import com.example.inventory.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.content.Intent
import android.net.Uri


class SettingsViewModel(private val repository: OfflineUsersRepository) : ViewModel() {
    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    init {
        // Fetch the first user's ID as a default (replace with actual current user ID logic)
        viewModelScope.launch {
            repository.getAllUsersStream().collect { users ->
                if (users.isNotEmpty()) {
                    _userId.value = users[0].userId.toString() // Default to first user
                }
            }
        }
    }

    // Method to update user ID if current user changes
    fun setCurrentUserId(userId: Int) {
        viewModelScope.launch {
            val user = repository.getUser(userId).collect { it }
            _userId.value = user.toString()
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