package com.example.inventory.ui.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.inventory.data.ItemsRepository
import com.example.inventory.data.ReceiptsRepository

class UploadViewModelFactory(
    private val receiptsRepository: ReceiptsRepository,
    private val itemsRepository: ItemsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UploadViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UploadViewModel(receiptsRepository, itemsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}