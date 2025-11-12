package com.example.inventory.ui.settings_test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.OfflineUsersRepository
import com.example.inventory.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UpdateInformationViewModel(private val repository: OfflineUsersRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(UpdateInfoUiState())
    val uiState: StateFlow<UpdateInfoUiState> = _uiState

    fun loadUser(userId: Int) {
        viewModelScope.launch {
            val user = repository.getUser(userId).first() // Use first() to get the first emission
            user?.let {
                _uiState.value = _uiState.value.copy(
                    userId = userId,
                    userName = it.username,
                    currentPassword = "", // Not pre-filled for security
                    newPassword = "",
                    retypePassword = ""
                )
            }
        }
    }

    fun updateUserInfo(
        userName: String,
        currentPassword: String,
        newPassword: String,
        retypePassword: String
    ) {
        viewModelScope.launch {
            val currentUser = _uiState.value.userId?.let { repository.getUser(it).first() }
            if (currentUser != null) {
                // Temporarily bypass password verification until login is implemented
                val updatedUser = currentUser.copy(
                    username = userName
                )
                repository.updateUser(updatedUser)
                _uiState.value = _uiState.value.copy(
                    userName = userName,
                    currentPassword = "",
                    newPassword = "",
                    retypePassword = ""
                )
            }
        }
    }

    fun updateUiState(
        userName: String = _uiState.value.userName,
        currentPassword: String = _uiState.value.currentPassword,
        newPassword: String = _uiState.value.newPassword,
        retypePassword: String = _uiState.value.retypePassword
    ) {
        _uiState.value = _uiState.value.copy(
            userName = userName,
            currentPassword = currentPassword,
            newPassword = newPassword,
            retypePassword = retypePassword
        )
    }
}

data class UpdateInfoUiState(
    val userId: Int? = null,
    val userName: String = "",
    val currentPassword: String = "",
    val newPassword: String = "",
    val retypePassword: String = ""
)