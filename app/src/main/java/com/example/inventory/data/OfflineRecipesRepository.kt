

package com.example.inventory.data
import kotlinx.coroutines.flow.Flow

class OfflineRecipesRepository(private val recipeDao: RecipeDao) : RecipesRepository {
    override fun getAllRecipesStream(): Flow<List<Recipe>> = recipeDao.getAllRecipes()
    override fun getRecipeStream(id: Int): Flow<Recipe?> = recipeDao.getRecipe(id)
    override fun searchRecipes(query: String): Flow<List<Recipe>> = recipeDao.searchRecipes(query)
    override suspend fun insertRecipe(recipe: Recipe) = recipeDao.insertRecipe(recipe)
    override suspend fun deleteRecipe(recipe: Recipe) = recipeDao.deleteRecipe(recipe)
    override suspend fun updateRecipe(recipe: Recipe) = recipeDao.updateRecipe(recipe)
}