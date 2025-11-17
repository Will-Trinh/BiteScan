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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.AppViewModel
import androidx.compose.material3.NavigationBarItemDefaults
import com.example.inventory.ui.theme.PrimaryGreen
import androidx.compose.ui.graphics.Color

@Composable
fun BottomNavigationBar(
    navController: NavController,
    appViewModel: AppViewModel
) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val userId by appViewModel.userId

        val items = listOf(
            DashboardDestination to Icons.Filled.Dashboard,
            UploadDestination to Icons.Filled.CameraAlt,
            HistoryDestination to Icons.Filled.History,
            SettingsDestination to Icons.Filled.Settings
        )

        items.forEach { (destination, icon) ->
            val selected =
                currentDestination?.hierarchy?.any { it.route == destination.route } == true

            NavigationBarItem(
                icon = { Icon(icon, contentDescription = destination.route) },
                label = {
                    Text(
                        destination.route.replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp
                    )
                },
                selected = selected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                onClick = {
                    if (userId == 0) {
                        navController.navigate(LoginDestination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = false
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                        return@NavigationBarItem
                    }

                    if (selected) return@NavigationBarItem

                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
    val navController = rememberNavController()
    BottomNavigationBar(navController, AppViewModel())
}
