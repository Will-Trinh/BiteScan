package com.example.inventory.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.OnlineRecipesRepository
import com.example.inventory.ui.AppViewModel
import com.example.inventory.ui.settings.MyPantryViewModel
import android.util.Log
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.inventory.InventoryApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.inventory.data.ai.OrChatRequest
import com.example.inventory.data.ai.OrMessage
import com.example.inventory.data.ai.OrResponseFormat
import com.example.inventory.data.ai.AiRecipeList
import kotlinx.serialization.json.Json
import com.example.inventory.data.ai.OpenRouterClient
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.example.inventory.data.RecipesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.inventory.data.Recipe
import com.example.inventory.ui.receipt.EditReceiptViewModel
import com.example.inventory.ui.receipt.EditUiState
import com.example.inventory.ui.userdata.FakeItemsRepository
import com.example.inventory.ui.userdata.FakeReceiptsRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import java.sql.Date
import androidx.lifecycle.viewmodel.compose.viewModel



class RecipeDetailViewModel(
    private val recipesRepository: RecipesRepository,
    private val myPantryViewModel: MyPantryViewModel,
    private val appViewModel: AppViewModel
) : ViewModel() {


    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    fun loadRecipe(recipeId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val recipe = withContext(Dispatchers.IO) {
                    recipesRepository.getRecipeStream(recipeId).first()
                }
                if (recipe != null) {
                    _uiState.update {
                        it.copy(
                            recipe = recipe,
                            isLoading = false,
                            error = null
                        )
                    }
                    Log.d("RecipeDetailViewModel", "Loaded recipe: ${recipe.title}")
                } else {
                    _uiState.update {
                        it.copy(
                            recipe = null,
                            isLoading = false,
                            error = "Can't find recipe with id = $recipeId"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("RecipeDetailViewModel", "Error loading recipe", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Unknown error"
                    )
                }
            }
        }
    }
}
data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)