package com.example.inventory.ui.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.history.HistoryDestination
import com.example.inventory.ui.AppViewModel

@Composable
fun BottomNavigationBar(navController: NavController,
                        appViewModel: AppViewModel) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,

    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val userId by appViewModel.userId
        val items = listOf(
            DashboardDestination to Icons.Default.Dashboard,
            UploadDestination to Icons.Default.CameraAlt,
            HistoryDestination to Icons.Default.History,
            SettingsDestination to Icons.Default.Settings
        )
        items.forEach { (destination, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = destination.route) },
                label = {
                    Text(
                        destination.route.replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp
                    )
                },
                selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                onClick = {
                    if (userId == 0) {
                        navController.navigate(LoginDestination.route) {
                            popUpTo(0) { inclusive = true } // back to login if lost data
                            launchSingleTop = true
                        }
                        return@NavigationBarItem
                    }
                    val route = when (destination) {
                        UploadDestination -> "${UploadDestination.route}/$userId"
                        DashboardDestination -> "${DashboardDestination.route}/$userId"
                        HistoryDestination -> "${destination.route}/$userId"
                        SettingsDestination -> "${SettingsDestination.route}/$userId"
                        else -> destination.route
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    val navController = rememberNavController() // Mock NavController
    BottomNavigationBar(navController, AppViewModel())
}