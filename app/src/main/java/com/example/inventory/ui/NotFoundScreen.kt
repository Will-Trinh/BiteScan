package com.example.inventory.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inventory.ui.theme.CookingAssistantTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.example.inventory.ui.navigation.UploadDestination

@Composable
fun NotFoundScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    CookingAssistantTheme {
        // Use Surface to set the background color from the theme
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background // Match the theme's background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "404",
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Page Not Found",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "The page you're looking for doesn't exist or has been moved.",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 32.dp),
                    textAlign = TextAlign.Center

                )
                Button(
                    onClick = { navController.navigate(UploadDestination.route) { popUpTo(0) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Go to Dashboard  ", fontSize = 25.sp, color = Color.White)
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = "Dashboard",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotFoundScreenPreview() {
    val navController = rememberNavController()
    CookingAssistantTheme {
        NotFoundScreen(navController = navController)
    }
}