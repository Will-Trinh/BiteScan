package com.example.inventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
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
import com.example.inventory.ui.navigation.*
import com.example.inventory.ui.AppViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.data.ai.OpenRouterClient


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            OpenRouterClient.init(this@MainActivity)
        }

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val appViewModel: AppViewModel = viewModel()

            // Add listener to handle invalid routes and redirect to 404
            navController.addOnDestinationChangedListener { _, destination, _ ->
                val validRoutes = listOf(
                    LandingDestination.route,
                    LoginDestination.route,
                    RegisterDestination.route,
                    DashboardDestination.route,
                    UploadDestination.route,
                    HistoryDestination.route,
                    EditReceiptDestination.route,
                    SettingsDestination.route,
                    UpdateInformationDestination.route,
                    MyPantryDestination.route,
                    LegalDestination.route,
                    AboutDestination.route,
                    NotFoundDestination.route,
                    RecipeDestination.route,
                    PriceTrackerDestination.route
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
                    InventoryNavHost(navController = navController, appViewModel = appViewModel)
                }
            }
        }
    }
}
