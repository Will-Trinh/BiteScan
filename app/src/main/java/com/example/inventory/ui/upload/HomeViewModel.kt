///*
// * Copyright (C) 2023 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.example.inventory.ui.home
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.inventory.data.Item
//import com.example.inventory.data.ItemsRepository
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.flow.stateIn
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.flow.flatMapLatest
///**
// * ViewModel to retrieve all items in the Room database.
// */
//class HomeViewModel(private val itemsRepository: ItemsRepository) : ViewModel() {
//
//    /**
//     * Holds home UI state. The list of items is retrieved from [ItemsRepository] and mapped to [HomeUiState].
//     */
//    val homeUiState: StateFlow<HomeUiState> =
//        itemsRepository.getAllItemsStream().map { HomeUiState(it) }
//            .stateIn(
//                scope = viewModelScope,
//                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
//                initialValue = HomeUiState()
//            )
//
//    private val _searchQuery = MutableStateFlow("")
//    val searchUiState: StateFlow<SearchUiState> =
//        _searchQuery
//            .flatMapLatest { query ->
//                itemsRepository.searchItems(query).map { SearchUiState(it) }
//            }
//            .stateIn(
//                scope = viewModelScope,
//                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
//                initialValue = SearchUiState()
//            )
//
//
//
//
////    private val _searchResults = MutableStateFlow<List<Item>>(emptyList())
////    val searchResults: StateFlow<List<Item>> = _searchResults
//    fun searchItems(query: String) {
//        _searchQuery.value = query
////        viewModelScope.launch {
////            itemsRepository.searchItems(query)
////                .collect { results ->
////                    _searchResults.value = results
////                }
////        }
//    }
//
//
//
//    companion object {
//        private const val TIMEOUT_MILLIS = 5_000L
//    }
//}
//
///**
// * UI state for HomeScreen
// */
//data class HomeUiState(val itemList: List<Item> = listOf())
//data class SearchUiState(val itemList: List<Item> = listOf())


package com.example.inventory.ui.upload

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class HomeViewModel : ViewModel() {
    private val _extractedItems = MutableStateFlow<List<String>>(emptyList())
    val extractedItems: StateFlow<List<String>> = _extractedItems

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    fun processReceiptImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val items = visionText.textBlocks
                            .flatMap { it.lines }
                            .map { it.text.trim() }
                            .filter { it.isNotEmpty() && it.matches(Regex("^[a-zA-Z ]+$")) }
                        _extractedItems.value = items
                        Log.d("HomeViewModel", "Extracted items: $items")
                        _isProcessing.value = false
                    }
                    .addOnFailureListener { e ->
                        Log.e("HomeViewModel", "OCR failed: ${e.message}")
                        _isProcessing.value = false
                    }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error in processReceiptImage: ${e.message}")
                _isProcessing.value = false
            }
        }
    }
}