package com.example.inventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.example.inventory.ui.theme.InventoryTheme
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.navigation.InventoryNavHost
import com.example.inventory.data.InventoryDatabase
import androidx.lifecycle.lifecycleScope
import com.example.inventory.data.ItemsRepository
import com.example.inventory.data.ReceiptsRepository
import com.example.inventory.data.UsersRepository
import com.example.inventory.data.Item
import com.example.inventory.data.Receipt
import com.example.inventory.data.OfflineItemsRepository
import com.example.inventory.data.OfflineReceiptsRepository
import com.example.inventory.data.User
import com.example.inventory.data.OfflineUsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.inventory.BuildConfig
import com.example.inventory.ui.navigation.*
import com.example.inventory.ui.history.HistoryDestination
import com.example.inventory.ui.AppViewModel
import androidx.lifecycle.viewmodel.compose.viewModel



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val db = InventoryDatabase.getDatabase(this)
        val usersRepo = OfflineUsersRepository(db.userDao(), db.receiptDao(), db.itemDao(), db.recipeDao())
        val receiptsRepo = OfflineReceiptsRepository(db.receiptDao())
        val itemsRepo = OfflineItemsRepository(db.itemDao())
        lifecycleScope.launch(Dispatchers.IO) {
            DatabaseSeeder.seedDatabase(usersRepo, receiptsRepo, itemsRepo)
        }

        setContent {
            val navController = rememberNavController()
            val appViewModel: AppViewModel = viewModel()

            // Add listener to handle invalid routes and redirect to 404
            navController.addOnDestinationChangedListener { _, destination, _ ->
                val validRoutes = listOf(
                    LandingDestination.route,
                    LoginDestination.route,
                    RegisterDestination.route,
                    DashboardDestination.routeWithArgs,
                    UploadDestination.routeWithArgs,
                    HistoryDestination.routeWithArgs,
                    "edit_receipt/{receiptId}/{userId}",
                    SettingsDestination.routeWithArgs,
                    UpdateInformationDestination.routeWithArgs,
                    MyPantryDestination.routeWithArgs,
                    LegalDestination.routeWithArgs,
                    AboutDestination.routeWithArgs,
                    NotFoundDestination.routeWithArgs,
                    RecipeDestination.routeWithArgs
                )
                if (!validRoutes.any { it == destination.route }) {
                    navController.navigate(NotFoundDestination.route) {
                        popUpTo(0) // Clear back stack to prevent returning to invalid route
                        launchSingleTop = true
                    }
                }
            }

            InventoryTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
//                    Text(text = "API Key: ${BuildConfig.apikeysafe}")
                    InventoryNavHost(navController = navController, appViewModel = appViewModel)
                }
            }
        }
    }
}

//for testing only
class DatabaseSeeder {
    companion object {
        suspend fun seedDatabase(
            usersRepo: UsersRepository,
            receiptsRepo: ReceiptsRepository,
            itemsRepo: ItemsRepository
        ) {
            // Chèn User

            val userId = usersRepo.insertUser(
                User(
                    userId = 0,
                    username = "testuser",
                    phone = "1234567890",
                    email = "TranXinhDep@gmail.com",
                )
            )
            if (userId <= 0) {
                println("Error can not add new User.")
                return
            }

            // Chèn Receipt
            val receiptId = receiptsRepo.insertReceipt(
                Receipt(
                    receiptId = 1,
                    userId = userId.toInt(),
                    source = "Walmart, 123 Main St, CA",
                    date = java.sql.Date(System.currentTimeMillis()),
                    status = "Completed"
                )
            )
            if (receiptId <= 0) {
                println("Error can not add new Receipt.")
                return
            }

            // Chèn Items
            val items = listOf(
                Item(
                    id = 0,
                    name = "Apple",
                    price = 0.5,
                    quantity = 2f,
                    date = java.sql.Date(System.currentTimeMillis()),
                    store = "Walmart",
                    category = "Fruit",
                    receiptId = receiptId.toInt()
                ),
                Item(
                    id = 0,
                    name = "Banana",
                    price = 0.3,
                    quantity = 3f,
                    date = java.sql.Date(System.currentTimeMillis()),
                    store = "Walmart",
                    category = "Fruit",
                    receiptId = receiptId.toInt()
                ),
                Item(
                    id = 0,
                    name = "Milk",
                    price = 2.99,
                    quantity = 1f,
                    date = java.sql.Date(System.currentTimeMillis()),
                    store = "Walmart",
                    category = "Dairy",
                    receiptId = receiptId.toInt()
                ),
                Item(
                    id = 0,
                    name = "Chicken breast",
                    price = 4.99,
                    quantity = 1f,
                    date = java.sql.Date(System.currentTimeMillis()),
                    store = "Walmart",
                    category = "Meat",
                    receiptId = receiptId.toInt()
                )
            )
            items.forEach {
                val itemId = itemsRepo.insertItem(it)
                if (itemId.id <= 0) {
                    println("error: can not add new Item: ${it.name}")
                }
            }
        }
    }
}