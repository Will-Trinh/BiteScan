package com.example.inventory.ui.upload

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.filter
import kotlin.collections.flatMap
import kotlin.collections.map
import kotlin.text.isNotEmpty
import kotlin.text.matches
import kotlin.text.trim

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