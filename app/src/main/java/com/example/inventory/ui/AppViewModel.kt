package com.example.inventory.ui

import androidx.activity.result.launch
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.UsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first


//save all data login
class AppViewModel(private val usersRepository: UsersRepository?= null ) : ViewModel() {
    private val _userId = mutableStateOf<Int?>(null)
    private val _oldUserId= MutableStateFlow<Int?>(null)

    val userId: State<Int?> = _userId

    val oldUserId: StateFlow<Int?> = _oldUserId
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady


    init {
        loadFirstUserIfExists()
    }

    private val _nameProfile = mutableStateOf<String?>(null)
    val nameProfile: State<String?> = _nameProfile

    private val _email = mutableStateOf<String?>(null)
    val email: State<String?> = _email

    fun setUserId(id: Int) {
        _userId.value = id
    }
    fun setNameProfile(name: String) {
        _nameProfile.value = name
    }

    fun setEmail(addr: String) {
        _email.value = addr
    }

    private fun loadFirstUserIfExists() {
        if (usersRepository == null) {
            _isReady.value = true
            return
        }

        viewModelScope.launch {
            try {
                val users = usersRepository.getAllUsersStream().first()
                val firstUser = users.firstOrNull()
                if (firstUser != null) {
                    _oldUserId.value = firstUser.userId
                    _userId.value = firstUser.userId
                    _nameProfile.value = firstUser.username
                    _email.value = firstUser.email
                }
            } catch (e: Exception) {
            } finally {
                _isReady.value = true
            }
        }
    }

    fun clearUserId() {
        _userId.value = null
        _nameProfile.value = null
        _email.value = null
    }
}