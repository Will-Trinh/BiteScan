package com.example.inventory.ui.loading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class StepStatus {
    PENDING, IN_PROGRESS, COMPLETED
}

data class LoadingStep(
    val label: String,
    val status: StepStatus = StepStatus.PENDING
)

data class LoadingState(
    val progress: Float = 0f,
    val steps: List<LoadingStep> = listOf(
        LoadingStep("Converting to base64"),
        LoadingStep("Calling OCR API"),
        LoadingStep("Saving receipt")
    )
)

class LoadingViewModel : ViewModel() {
    private val _loadingState = MutableStateFlow(LoadingState())
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    init {
        startLoading()  // Auto-start on creation (trigger from UploadViewModel)
    }

    private fun startLoading() {
        viewModelScope.launch {
            val steps = _loadingState.value.steps.toMutableList()
            var currentProgress = 0f

            steps.forEachIndexed { index, _ ->
                // Set in progress
                steps[index] = steps[index].copy(status = StepStatus.IN_PROGRESS)
                _loadingState.value = _loadingState.value.copy(steps = steps)

                // Simulate step time (e.g., 1-3s per step)
                delay((1000L + index * 1000L).toLong())

                // Set completed and increment progress
                steps[index] = steps[index].copy(status = StepStatus.COMPLETED)
                currentProgress = (index + 1f) / steps.size
                _loadingState.value = _loadingState.value.copy(steps = steps, progress = currentProgress)
            }

            // Final progress to 1f
            delay(500L)  // Brief pause
            _loadingState.value = _loadingState.value.copy(progress = 1f)
        }
    }

    fun reset() {
        _loadingState.value = LoadingState()  // Reset for new uploads
    }
}