package com.example.inventory.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define your custom colors here (reusable across the app)

private val LightGreen = Color(0xFFE8F5E9) // Used for insight backgrounds
private val CardBackground = Color.White

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,  // Use PrimaryGreen for main accents (e.g., buttons, bars)
    secondary = PrimaryGreen.copy(alpha = 0.8f),  // Subtle variant for secondary elements (e.g., icons)
    tertiary = LightGreen,  // Use LightGreen for tertiary elements like insight backgrounds
    background = Color.White,  // Light grey for main screen backgrounds
    surface = CardBackground,  // White for cards and surfaces (e.g., TopAppBar, BottomNav)
    onPrimary = Color.Black,  // White text/icons on primary green
    onSecondary = Color.White,  // White on secondary
    onTertiary = Color.Black.copy(alpha = 0.87f),  // Dark text on light green backgrounds
    onBackground = Color.Black.copy(alpha = 0.87f),  // Dark text on dashboard background
    onSurface = Color.Black.copy(alpha = 0.87f),  // Dark text on white cards/surfaces
    surfaceVariant = LightGreen,  // Reuse LightGreen for variant surfaces (e.g., subtle cards)
    onSurfaceVariant = Color.Black.copy(alpha = 0.74f)  // Slightly faded text on variants
)

@Composable
fun CookingAssistantTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}