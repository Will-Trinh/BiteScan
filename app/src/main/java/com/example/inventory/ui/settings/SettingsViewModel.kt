package com.example.inventory.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.UsersRepository
import com.example.inventory.ui.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val repository: UsersRepository,
    private val appViewModel: AppViewModel
) : ViewModel() {

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    private val _logoutCompleted = MutableStateFlow(false)
    val logoutCompleted: StateFlow<Boolean> = _logoutCompleted

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail


    init {
        viewModelScope.launch {
            val uid = appViewModel.userId.value
            if (uid != null) {
                _userId.value = uid.toString()
                loadUserDetails(uid)
            } else {
                _userId.value = null
                _logoutCompleted.value = true
                android.util.Log.d("SettingsViewModel", "fail to get UID")
            }
        }
    }

    fun setCurrentUserId(userId: Int) {
        viewModelScope.launch {
            val uid = appViewModel.userId.value ?: 0
            _userId.value = uid.toString()
            loadUserDetails(uid)
        }
    }

    private suspend fun loadUserDetails(userId: Int) {
        val user = repository.getUser(userId).firstOrNull()
        _userName.value = user?.username ?: appViewModel.nameProfile.value ?: "User"
        _userEmail.value = user?.email ?: appViewModel.email.value ?: "unknown@example.com"
    }

    fun logout() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                appViewModel.clearUserId()
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
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Failed to launch URL: ${e.message}")
            }
        }
    }
}
