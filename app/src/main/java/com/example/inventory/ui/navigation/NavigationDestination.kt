package com.example.inventory.ui.navigation

import com.example.inventory.R

/**
 * Interface to describe the navigation destinations for the app
 */
interface NavigationDestination {
    val route: String
    val titleRes: Int
}

object UpdateInformationDestination : NavigationDestination {
    override val route = "update_information"
    const val userIdArg = "userId"
    override val titleRes = R.string.update_information_title
    val routeWithArgs = "$route/{userId}"
}

object MyPantryDestination : NavigationDestination {
    override val route = "my_pantry"
    const val userIdArg = "userId"
    override val titleRes = R.string.my_pantry_title
    val routeWithArgs = "$route/{${userIdArg}}"
}

object NotFoundDestination : NavigationDestination {
    override val route = "not_found"
    override val titleRes = R.string.not_found_title
    val routeWithArgs = route
}

object LegalDestination : NavigationDestination {
    override val route = "legal/{userId}"
    const val userIdArg = "userId"
    override val titleRes = R.string.legal_title
    val routeWithArgs = "$route"
}

object AboutDestination : NavigationDestination {
    override val route = "about/{userId}"
    const val userIdArg = "userId"
    override val titleRes = R.string.about_title // Add this to strings.xml, e.g., <string name="about_title">About</string>
    val routeWithArgs = "$route"
}

object SettingsDestination : NavigationDestination {
    override val route = "settings"
    const val userIdArg = "userId"
    override val titleRes = R.string.settings_title
    val routeWithArgs = "$route/{userId}" // Corrected placeholder syntax
}

// Placeholder destinations (disabled for now)
object DashboardDestination : NavigationDestination {
    override val route = "dashboard"
    override val titleRes = R.string.dashboard
}

object HistoryDestination : NavigationDestination {
    override val route = "history"
    override val titleRes = R.string.history
}

