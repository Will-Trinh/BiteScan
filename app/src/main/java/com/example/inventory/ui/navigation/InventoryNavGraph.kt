package com.example.inventory.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.inventory.ui.upload.UploadScreen
import com.example.inventory.ui.upload.UploadDestination
import com.example.inventory.ui.receipt.EditReceiptDestination
import com.example.inventory.ui.receipt.EditReceiptScreen
import com.example.inventory.ui.receipt.ReceiptDestination
import com.example.inventory.ui.receipt.ReceiptScreen
import com.example.inventory.ui.receipt.ReceiptViewModel
import com.example.inventory.InventoryApplication
import com.example.inventory.ui.settings.SettingScreen
import com.example.inventory.ui.settings.UpdateInformationScreen
import com.example.inventory.ui.settings.MyPantryScreen
import com.example.inventory.ui.settings.AboutScreen
import com.example.inventory.ui.NotFoundScreen
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import com.example.inventory.ui.settings.LegalScreen


@Composable
fun InventoryNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = UploadDestination.route,
        modifier = modifier
    ) {
        composable(route = UploadDestination.route) {
            UploadScreen(
                paddingValues = PaddingValues(0.dp),
                navController = navController
            )
        }

        composable(
            route = ReceiptDestination.routeWithArgs,
            arguments = listOf(navArgument(ReceiptDestination.userIdArg) { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(ReceiptDestination.userIdArg) ?: 0
            ReceiptScreen(
                navigateToReceiptEntry = {},
                navigateToReceiptUpdate = { receiptId ->
                    navController.navigate("edit_receipt/$receiptId/$userId")
                },
                canNavigateBack = navController.previousBackStackEntry != null,
                navController = navController,
                userId = userId,
            )
        }

        composable(
            route = "edit_receipt/{receiptId}/{userId}",
            arguments = listOf(
                navArgument(EditReceiptDestination.receiptIdArg) { type = NavType.IntType },
                navArgument("userId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val receiptId = backStackEntry.arguments?.getInt(EditReceiptDestination.receiptIdArg) ?: 0
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            EditReceiptScreen(
                receiptId = receiptId,
                userId = userId,
                navigateUp = {
                    navController.navigate("receipt/$userId") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                navController = navController
            )
        }

        composable(
            route = SettingsDestination.routeWithArgs,
            arguments = listOf(navArgument(SettingsDestination.userIdArg) { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(SettingsDestination.userIdArg) ?: 0
            SettingScreen(
                navController = navController,
                userId = userId
            )
        }

        composable(
            route = UpdateInformationDestination.routeWithArgs,
            arguments = listOf(navArgument(UpdateInformationDestination.userIdArg) { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(UpdateInformationDestination.userIdArg) ?: 0
            UpdateInformationScreen(
                navController = navController,
                userId = userId
            )
        }

        composable(
            route = MyPantryDestination.routeWithArgs,
            arguments = listOf(navArgument(MyPantryDestination.userIdArg) { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(MyPantryDestination.userIdArg) ?: 0
            MyPantryScreen(
                navController = navController,
                userId = userId
            )
        }


        composable(
            route = LegalDestination.routeWithArgs,
            arguments = listOf(navArgument(LegalDestination.userIdArg) { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(LegalDestination.userIdArg)
            if (userId != null) {
                LegalScreen(
                    navController = navController,
                    userId = userId
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
                navController = navController,
                userId = userId
            )
        }

        composable(route = DashboardDestination.route) {
            navController.navigate(NotFoundDestination.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                launchSingleTop = true
            }
        }

        composable(route = HistoryDestination.route) {
            navController.navigate(ReceiptDestination.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                launchSingleTop = true
            }
        }

        composable(route = NotFoundDestination.routeWithArgs) {
            NotFoundScreen(
                navController = navController
            )
        }
    }
}