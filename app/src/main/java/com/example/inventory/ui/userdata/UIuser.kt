package com.example.inventory.ui.userdata

import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import com.example.inventory.data.Receipt
import com.example.inventory.data.ReceiptsRepository
import com.example.inventory.data.User
import com.example.inventory.data.UsersRepository
import com.example.inventory.data.OnlineReceiptsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.sql.Date
import java.util.concurrent.atomic.AtomicInteger
import com.example.inventory.data.OnlineRecipesRepository
import com.example.inventory.data.Recipe
import com.example.inventory.ui.settings.MyPantryViewModel
import com.example.inventory.ui.recipe.RecipeUiModel
import com.example.inventory.data.RecipesRepository


// Fake ReceiptsRepository for preview (updated userId to 1 for consistency)
class FakeReceiptsRepository : ReceiptsRepository {
    private val receiptIdCounter = AtomicInteger(4) // Start after initial fake receipts
    private val fakeReceipts = mutableListOf(
        Receipt(receiptId = 1, userId = 1, date = Date(System.currentTimeMillis()), source = "Lalala", status = "Completed"),
        Receipt(receiptId = 2, userId = 1, date = Date(System.currentTimeMillis() - 86400000), source = "Cosco", status = "Completed"),
        Receipt(receiptId = 3, userId = 1, date = Date(System.currentTimeMillis() - 172800000), source = "Walmart", status = "Completed"),
        Receipt(receiptId = 4, userId = 1, date = Date(System.currentTimeMillis() - 259200000), source = "Target", status = "Completed")
    )
    override suspend fun fetchAndSyncReceipts(userId: Int): List<Receipt> {
        return fakeReceipts.filter { it.userId == userId }
    }
    override fun getAllReceiptsStream(): Flow<List<Receipt>> = flowOf(fakeReceipts)

    override fun getReceiptsForUser(userId: Int): Flow<List<Receipt>> = flowOf(fakeReceipts.filter { it.userId == userId })

    override fun searchReceipts(query: String): Flow<List<Receipt>> = flowOf(
        fakeReceipts.filter { it.source.contains(query, ignoreCase = true) || it.status.contains(query, ignoreCase = true) }
    )

    override suspend fun getReceipt(id: Int): Receipt? = fakeReceipts.find { it.receiptId == id }

    override suspend fun insertReceipt(receipt: Receipt): Long {
        val newReceipt = receipt.copy(receiptId = receiptIdCounter.incrementAndGet())
        fakeReceipts.add(newReceipt)
        return newReceipt.receiptId.toLong()
    }
    override suspend fun upsertReceipt(receipt: Receipt): Long {
        val newReceipt = receipt.copy(receiptId = receiptIdCounter.incrementAndGet())
        fakeReceipts.add(newReceipt)
        return newReceipt.receiptId.toLong()
    }

    override suspend fun deleteReceipt(receipt: Receipt) {
        fakeReceipts.removeIf { it.receiptId == receipt.receiptId }
    }
    fun clear() = fakeReceipts.clear()
    override suspend fun updateReceipt(receipt: Receipt) {
        val index = fakeReceipts.indexOfFirst { it.receiptId == receipt.receiptId }
        if (index != -1) {
            fakeReceipts[index] = receipt
        }
    }
}

// Updated Fake ItemsRepository (self-contained: hardcodes receipt IDs for user 1 to simulate join without calling getReceipt)
class FakeItemsRepository : ItemsRepository {
    private val idCounter = AtomicInteger(3) // Start ID after initial fake items
    private val fakeItems = mutableListOf(
        Item(id = 1, name = "Milk", price = 2.5, quantity = 1f, date = Date(System.currentTimeMillis()), store = "Cosco", category = "Dairy", receiptId = 1),
        Item(id = 2, name = "Bread", price = 3.0, quantity = 2f, date = Date(System.currentTimeMillis()), store = "Cosco", category = "Bakery", receiptId = 1),
        Item(id = 3, name = "Apple", price = 1.0, quantity = 5f, date = Date(System.currentTimeMillis()), store = "Cosco", category = "Fruit", receiptId = 2)
    )

    // Hardcoded receipt IDs for user 1 (simulates join without external getReceipt call)
    private fun getReceiptIdsForUser(userId: Int): List<Int> {
        return if (userId == 1) listOf(1, 2, 3, 4) else emptyList()  // All receipts for user 1
    }

    override fun getAllItemsStream(): Flow<List<Item>> = flowOf(fakeItems)

    override fun getItemStream(id: Int): Flow<Item?> = flowOf(fakeItems.find { it.id == id })

    override fun getItemsForReceipt(receiptId: Int): Flow<List<Item>> = flowOf(fakeItems.filter { it.receiptId == receiptId })

    override fun searchItems(query: String): Flow<List<Item>> = flowOf(
        fakeItems.filter { it.name.contains(query, ignoreCase = true) }
    )
    fun clear() = fakeItems.clear()
    override suspend fun insertItem(item: Item): Long {
        val newItem = item.copy(id = idCounter.incrementAndGet())
        fakeItems.add(newItem)
        return newItem.id.toLong()
    }
    override suspend fun upsertItem(item: Item): Long {
        val newItem = item.copy(id = idCounter.incrementAndGet())
        fakeItems.add(newItem)
        return newItem.id.toLong()
    }

    override suspend fun deleteItem(item: Item) {
        fakeItems.removeIf { it.id == item.id }
    }

    override suspend fun updateItem(item: Item) {
        val index = fakeItems.indexOfFirst { it.id == item.id }
        if (index != -1) {
            fakeItems[index] = item
        }
    }

    // Implement getItemsForUser (filters by hardcoded receipt IDs for user)
    override fun getItemsForUser(userId: Int): Flow<List<Item>> {
        val receiptIdsForUser = getReceiptIdsForUser(userId)
        return flowOf(fakeItems.filter { it.receiptId in receiptIdsForUser })
    }

    // Implement getItemsForUserByCategory (filters by receipt IDs + category)
    override fun getItemsForUserByCategory(userId: Int, category: String): Flow<List<Item>> {
        val receiptIdsForUser = getReceiptIdsForUser(userId)
        return flowOf(fakeItems.filter { it.receiptId in receiptIdsForUser && it.category.equals(category, ignoreCase = true) })
    }

    // Implement getItemsWithStoreForUser (same as getItemsForUser for fake data - store already set)
    override fun getItemsWithStoreForUser(userId: Int): Flow<List<Item>> {
        val receiptIdsForUser = getReceiptIdsForUser(userId)
        return flowOf(fakeItems.filter { it.receiptId in receiptIdsForUser })
    }
}

class FakeUsersRepository : UsersRepository {
    private val fakeReceiptsRepo = FakeReceiptsRepository()
    private val fakeItemsRepo = FakeItemsRepository()
    // 1. insertUser
    override suspend fun insertUser(user: User): Long {
        return 1L
    }
    // 2. getAllUsersStream
    override fun getAllUsersStream(): Flow<List<User>> = flowOf(listOf(fakeUIuser))

    // 3. getUser
    override fun getUser(id: Int): Flow<User?> = flowOf(if (id == 1) fakeUIuser else null)

    // 4. searchUsers
    override fun searchUsers(query: String): Flow<List<User>> = flowOf(
        if (fakeUIuser.username.contains(query, true)) listOf(fakeUIuser) else emptyList()
    )
    // 5. deleteUser
    override suspend fun deleteUser(user: User) {  }
    // 6. updateUser
    override suspend fun updateUser(user: User) {  }
    override suspend fun updateUserDiet(userId: Int, diet: String?) {  }

    override fun getReceiptsForUser(userId: Int): Flow<List<Receipt>> {
        return FakeReceiptsRepository().getReceiptsForUser(userId)
    }
    override suspend fun deleteAllData(){
        fakeReceiptsRepo.clear()
        fakeItemsRepo.clear()
    }
}
class FakeRecipeRepository : RecipesRepository {
    override fun getAllRecipesStream(): Flow<List<Recipe>> = flowOf(emptyList())
    override fun getRecipeStream(id: Int): Flow<Recipe?> = flowOf(null)
    override fun searchRecipes(query: String): Flow<List<Recipe>> = flowOf(emptyList())
    override suspend fun insertRecipe(recipe: Recipe) : Long { return 1L }
    override suspend fun upsertRecipe(recipe: Recipe): Long { return 1L }
    override suspend fun deleteRecipe(recipe: Recipe) {}
    override suspend fun updateRecipe(recipe: Recipe) {}
}
class FakeOnlineRecipesRepository : OnlineRecipesRepository(
    recipesRepository = FakeRecipeRepository(),
) {
    suspend fun searchRecipes(
        ingredients: List<String>,
        excludedIngredients: List<String>,
        filters: Set<String>
    ): List<RecipeUiModel> {

        return listOf(
            RecipeUiModel(
                id = 1,
                name = "Tomato Egg Stir-Fry",
                subtitle = "10-minute classic comfort food",
                time = "10 mins",
                servings = "2 servings",
                calories = "280 cal",
                protein = "15g",
                carbs = "12g",
                fat = "18g",
                ingredientUsage = "Uses 4/5 ingredients",
            ),
            RecipeUiModel(
                id = 2,
                name = "Chicken Fried Rice",
                subtitle = "Better than takeout",
                time = "20 mins",
                servings = "3 servings",
                calories = "580 cal",
                protein = "28g",
                carbs = "78g",
                fat = "22g",
                ingredientUsage = "Uses 8/10 ingredients",
            ),
            RecipeUiModel(
                id = 3,
                name = "Lemongrass Chili Chicken",
                subtitle = "Aromatic Vietnamese favorite",
                time = "25 mins",
                servings = "4 servings",
                calories = "420 cal",
                protein = "38g",
                carbs = "8g",
                fat = "24g",
                ingredientUsage = "Uses 6/8 ingredients",
            )
        )
    }
}
class FakeMyPantryViewModel: MyPantryViewModel(FakeItemsRepository(), FakeReceiptsRepository()) {
    override val availableIngredientNames: Flow<List<String>> = flowOf(
        listOf(
            "chicken", "tomato", "onion", "garlic", "rice", "egg", "potato",
            "carrot", "broccoli", "bell pepper", "spinach", "milk", "cheese",
            "butter", "bread", "beef", "pork", "shrimp", "pasta", "olive oil",
            "salt", "pepper", "sugar", "banana", "apple", "orange", "avocado",
            "cucumber", "ginger", "soy sauce", "lime", "coconut milk", "yogurt"
        )
    )
}

// Fake UI user (updated to userId = 1 for consistency)
val fakeUIuser = User(userId = 1, username = "previewUser", email = "TranXinhDep@gmail.com", phone = "1234567890", diet = "veteran")