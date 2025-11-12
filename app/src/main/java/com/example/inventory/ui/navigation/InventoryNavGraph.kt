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
import com.example.inventory.ui.recipe.RecipeRecommendationScreen
import com.example.inventory.ui.landing.LandingScreen
import com.example.inventory.ui.landing.LoginScreen
import com.example.inventory.ui.landing.RegistrationScreen


@Composable
fun InventoryNavHost(
    navController: NavHostController,
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = LandingDestination.route,
        modifier = modifier
    ) {
        composable(route = LandingDestination.route) {
            LandingScreen(navController = navController, onGetStartedClick = { navController.navigate(LoginDestination.route)})
        }
        composable(route = LoginDestination.route) {
            LoginScreen(
                navController = navController,
                appViewModel = appViewModel,
                onLoginClick = { userId ->
                    navController.navigate("upload/$userId") {
                        popUpTo(LoginDestination.route) { inclusive = true }
                    }
                },
                onCreateAccountClick = {
                    //Todo: Create account
                },
            )
        }
        //register route
        composable(route = RegisterDestination.route) {
            RegistrationScreen(navController = navController)
        }

        composable(
            route = UploadDestination.routeWithArgs,
            arguments = listOf(navArgument(UploadDestination.userIdArg) { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(UploadDestination.userIdArg) ?: 0
            UploadScreen(
                navController = navController,
                appViewModel = appViewModel,
            )
        }
        composable(
            route = DashboardDestination.routeWithArgs,
            arguments = listOf(navArgument(DashboardDestination.userIdArg) {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(DashboardDestination.userIdArg) ?: 0
            DashboardScreen(
                navController = navController,
                appViewModel = appViewModel
            )
        }

        // New no-arg route for Dashboard (for bottom nav; extracts shared userId)
        composable(route = DashboardDestination.route) {
            val userId = navController.previousBackStackEntry?.savedStateHandle?.get<Int>("userId")
                ?: navController.currentBackStackEntry?.savedStateHandle?.get<Int>("userId") ?: 0
            if (userId != 0) {
                DashboardScreen(
                    navController = navController,
                    appViewModel = appViewModel
                )
            } else {
                NotFoundScreen(navController = navController)
            }
        }
        composable(
            route = HistoryDestination.routeWithArgs,
            arguments = listOf(navArgument(HistoryDestination.userIdArg) { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(HistoryDestination.userIdArg) ?: 0
            HistoryScreen(
                navigateToReceiptEntry = {},
                navigateToReceiptUpdate = { receiptId ->
                    navController.navigate("edit_receipt/$receiptId/$userId")
                },
                canNavigateBack = navController.previousBackStackEntry != null,
                navController = navController,
                appViewModel = appViewModel
            )
        }

        composable(
            route = "edit_receipt/{receiptId}/{userId}",
            arguments = listOf(
                navArgument(EditReceiptDestination.receiptIdArg) { type = NavType.IntType },
                navArgument("userId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val receiptId =
                backStackEntry.arguments?.getInt(EditReceiptDestination.receiptIdArg) ?: 0
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            EditReceiptScreen(
                receiptId = receiptId,
                navigateUp = {
                    navController.navigate("history/$userId") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                navController = navController,
                appViewModel = appViewModel
            )
        }

        composable(
            route = SettingsDestination.routeWithArgs,
            arguments = listOf(navArgument(SettingsDestination.userIdArg) {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(SettingsDestination.userIdArg) ?: 0
            SettingScreen(
                navController = navController,
                appViewModel = appViewModel
            )
        }

        composable(
            route = UpdateInformationDestination.routeWithArgs,
            arguments = listOf(navArgument(UpdateInformationDestination.userIdArg) {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            val userId =
                backStackEntry.arguments?.getInt(UpdateInformationDestination.userIdArg) ?: 0
            UpdateInformationScreen(
                navController = navController,
                appViewModel = appViewModel,
            )
        }

        composable(
            route = MyPantryDestination.routeWithArgs,
            arguments = listOf(navArgument(MyPantryDestination.userIdArg) {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(MyPantryDestination.userIdArg) ?: 0
            MyPantryScreen(
                navController = navController,
                appViewModel = appViewModel
            )
        }

        composable(
            route = LegalDestination.routeWithArgs,
            arguments = listOf(navArgument(LegalDestination.userIdArg) { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(LegalDestination.userIdArg)
            if (userId != null) {
                LegalScreen(
                    navController = navController
                )
            } else {
                // Fallback to NotFoundScreen if userId is null (though this should not happen with proper navigation)
                NotFoundScreen(navController = navController)
            }
        }

        composable(
            route = AboutDestination.routeWithArgs,
            arguments = listOf(navArgument(AboutDestination.userIdArg) { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(AboutDestination.userIdArg) ?: 0
            AboutScreen(
                navController = navController
            )
        }

        composable(route = NotFoundDestination.routeWithArgs) {
            NotFoundScreen(
                navController = navController
            )
        }

        composable(
            route = RecipeDestination.routeWithArgs,  // e.g., "recipe_recommendations/{userId}"
            arguments = listOf(navArgument(RecipeDestination.userIdArg) { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(RecipeDestination.userIdArg) ?: 0
            RecipeRecommendationScreen(
                navController = navController,
                userId = userId,
                appViewModel = appViewModel
            )
        }
    }
}