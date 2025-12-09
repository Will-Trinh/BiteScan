package com.example.inventory.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.ui.AppViewModel
import com.example.inventory.ui.settings.MyPantryViewModel
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.inventory.data.RecipesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.inventory.data.Recipe
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first



class RecipeDetailViewModel(
    private val recipesRepository: RecipesRepository,
    private val myPantryViewModel: MyPantryViewModel,
    private val appViewModel: AppViewModel
) : ViewModel() {

    //get user diet from appViewModel
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