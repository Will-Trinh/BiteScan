package com.example.inventory.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

//save all data login
class AppViewModel : ViewModel() {
    private val _userId = mutableStateOf<Int?>(null)
    val userId: State<Int?> = _userId

    fun setUserId(id: Int) {
        _userId.value = id
    }
    fun clearUserId() {
        _userId.value = null
    }
}