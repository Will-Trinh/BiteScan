package com.example.inventory.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.inventory.ui.upload.UploadScreen
import com.example.inventory.ui.receipt.EditReceiptScreen
import com.example.inventory.ui.history.HistoryScreen
import com.example.inventory.ui.settings.SettingScreen
import com.example.inventory.ui.settings.UpdateInformationScreen
import com.example.inventory.ui.settings.MyPantryScreen
import com.example.inventory.ui.settings.AboutScreen
import com.example.inventory.ui.settings.LegalScreen
import com.example.inventory.ui.NotFoundScreen
import com.example.inventory.ui.dashboard.DashboardScreen
import com.example.inventory.ui.AppViewModel
import com.example.inventory.ui.landing.CreateAccountScreenPreview
import com.example.inventory.ui.recipe.RecipeRecommendationScreen
import com.example.inventory.ui.landing.LandingScreen
import com.example.inventory.ui.landing.LoginScreen
import com.example.inventory.ui.landing.RegistrationScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
//for progress bar
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import android.util.Log
@Composable
fun InventoryNavHost(
    navController: NavHostController,
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier,
) {

    val oldUserId: Int? by appViewModel.oldUserId.collectAsState()
    val isReady by appViewModel.isReady.collectAsState()
    if (!isReady) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    Log.d("InventoryNavHost", "oldUserId: $oldUserId")

    val startDestination = if (oldUserId != null) UploadDestination.route else LandingDestination.route

    NavHost(
        navController = navController,
        startDestination =startDestination,
        modifier = modifier
    ) {
        // Landing
        composable(route = LandingDestination.route) {
            LandingScreen(
                navController = navController,
                onGetStartedClick = { navController.navigate(LoginDestination.route) }
            )
        }

        // Login
        composable(route = LoginDestination.route) {
            Log.d("InventoryNavHost - Login", "oldUserId: $oldUserId")
            LoginScreen(
                navController = navController,
                appViewModel = appViewModel,
                onLoginClick = { _ ->
                    navController.navigate(UploadDestination.route) {
                        popUpTo(LoginDestination.route) { inclusive = true }
                    }
                }
            )
        }

        // Register
        composable(route = RegisterDestination.route) {
            RegistrationScreen(navController = navController)
        }

        // Upload
        composable(route = UploadDestination.route) {
            UploadScreen(
                navController = navController,
                appViewModel = appViewModel,
            )
        }

        // Dashboard
        composable(route = DashboardDestination.route) {
                DashboardScreen(
                    navController = navController,
                    appViewModel = appViewModel
                )
        }

        // History
        composable(route = HistoryDestination.route) {
            HistoryScreen(
                navigateToReceiptEntry = {},
                navigateToReceiptUpdate = { receiptId ->
                    navController.navigate("edit_receipt/$receiptId")
                },
                canNavigateBack = navController.previousBackStackEntry != null,
                navController = navController,
                appViewModel = appViewModel
            )
        }

        // Edit receipt
        composable(
            route = EditReceiptDestination.route,
            arguments = listOf(
                navArgument("receiptId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val receiptId = backStackEntry.arguments?.getInt("receiptId") ?: 0
            EditReceiptScreen(
                receiptId = receiptId,
                navigateUp = {
                    navController.navigate(HistoryDestination.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                navController = navController,
                appViewModel = appViewModel
            )
        }

        // Settings
        composable(route = SettingsDestination.route) {
            SettingScreen(
                navController = navController,
                appViewModel = appViewModel
            )
        }

        // Update Information
        composable(route = UpdateInformationDestination.route) {
            UpdateInformationScreen(
                navController = navController,
                appViewModel = appViewModel,
            )
        }

        // My Pantry
        composable(route = MyPantryDestination.route) {
            MyPantryScreen(
                navController = navController,
                appViewModel = appViewModel
            )
        }

        // Legal
        composable(route = LegalDestination.route) {
                LegalScreen(navController = navController)
        }

        // About
        composable(route = AboutDestination.route) {
            AboutScreen(navController = navController)
        }

        // Not Found
        composable(route = NotFoundDestination.route) {
            NotFoundScreen(navController = navController)
        }

        // Recipe Recommendations
        composable(route = RecipeDestination.route) {
            RecipeRecommendationScreen(
                navController = navController,
                appViewModel = appViewModel
            )
        }
    }
}
