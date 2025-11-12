package com.example.inventory.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

//save all data login
class AppViewModel : ViewModel() {
    private val _userId = mutableStateOf<Int?>(null)
    val userId: State<Int?> = _userId
    private val _nameProfile = mutableStateOf<String?>(null)
    val nameProfile: State<String?> = _nameProfile
    fun setUserId(id: Int) {
        _userId.value = id
    }
    fun setNameProfile(name: String) {
        _nameProfile.value = name
    }
    fun clearUserId() {
        _userId.value = null
    }
}