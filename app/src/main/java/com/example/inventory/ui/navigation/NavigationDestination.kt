package com.example.inventory.ui.navigation

import com.example.inventory.R

/**
 * Interface to describe the navigation destinations for the app
 */
interface NavigationDestination {
    val route: String
    val titleRes: Int
}

object UploadDestination : NavigationDestination {
    override val route = "upload"
    override val titleRes = R.string.upload_title
}

object LandingDestination : NavigationDestination {
    override val route = "landing"
    override val titleRes = R.string.landing
}

object LoginDestination : NavigationDestination {
    override val route = "login"
    override val titleRes = R.string.login_title
}

object RegisterDestination : NavigationDestination {
    override val route = "register"
    override val titleRes = R.string.register_title
}

object UpdateInformationDestination : NavigationDestination {
    override val route = "update_information"
    override val titleRes = R.string.update_information_title
}

object MyPantryDestination : NavigationDestination {
    override val route = "my_pantry"
    override val titleRes = R.string.my_pantry_title
}

object NotFoundDestination : NavigationDestination {
    override val route = "not_found"
    override val titleRes = R.string.not_found_title
}

object LegalDestination : NavigationDestination {
    override val route = "legal"
    override val titleRes = R.string.legal_title
}

object AboutDestination : NavigationDestination {
    override val route = "about"
    override val titleRes = R.string.about_title
}

object SettingsDestination : NavigationDestination {
    override val route = "settings"
    override val titleRes = R.string.settings_title
}

object DashboardDestination : NavigationDestination {
    override val route = "dashboard"
    override val titleRes = R.string.dashboard_title
}

object RecipeDestination : NavigationDestination {
    override val route = "recipe_recommendations"
    override val titleRes = R.string.recipe_recommendations_title
}

object HistoryDestination : NavigationDestination {
    override val route = "history"
    override val titleRes = R.string.history_title
}

object EditReceiptDestination : NavigationDestination {
    override val route = "edit_receipt/{receiptId}"
    override val titleRes = R.string.edit_receipt_title
}

object RecipeDetailDestination : NavigationDestination {
    override val route = "recipe_detail/{recipeId}"
    override val titleRes = R.string.recipe_detail
}