//package com.example.inventory.ui.theme
//
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.lightColorScheme
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.graphics.Color
//
//private val LightColorScheme = lightColorScheme(
//    primary = Color(0xFFFFA500), // Orange nhạt cho button/card
//    secondary = Color(0xFFFF4500), // Red accent cho deals
//    background = Color(0xFFFFF5E1), // Pastel nhạt (beige)
//    surface = Color(0xFFFFF5E1), // Nền card
//    onPrimary = Color.White,
//    onSecondary = Color.White,
//    onBackground = Color.Black,
//    onSurface = Color.Black
//)
//
//@Composable
//fun CookingAssistantTheme(content: @Composable () -> Unit) {
//    MaterialTheme(
//        colorScheme = LightColorScheme,
//        typography = MaterialTheme.typography, // Giữ mặc định hoặc custom nếu cần
//        content = content
//    )
//}
//

package com.example.inventory.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color.White, // Light Orange for buttons/cards
    secondary = Color.White, // Red accent for deals
    background = Color.White, // New background color for main screen
    surface = Color.White, // New surface color for TopAppBar and BottomNavigationBar
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.Black, // Changed to pure black for text on background
    onSurface = Color.Black // Changed to pure black for text on surface
)

@Composable
fun CookingAssistantTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}