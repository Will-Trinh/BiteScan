package com.example.inventory.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inventory.ui.theme.CookingAssistantTheme
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.navigation.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    userId: Int,
    modifier: Modifier = Modifier
) {
    val activeItemCount = remember { 5 } // Mock data; replace with ViewModel if needed
    val recentReceipts = remember {
        listOf(
            "Receipt from StoreA - ${SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date())}",
            "Receipt from StoreB - 09/20/2025"
        )
    }
    CookingAssistantTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) { Text("Nutrition Dashboard", fontWeight =FontWeight.Bold) }
                    },
                )

            },
            bottomBar = { BottomNavigationBar(navController) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
            )

            {
                    // Pantry Summary Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Pantry Overview",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Active Items: $activeItemCount",
                                fontSize = 16.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }

                    // Recent Receipts Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Recent Receipts",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            recentReceipts.forEach { receipt ->
                                Text(
                                    text = receipt,
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Quick Actions
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Quick Actions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { navController.navigate("my_pantry/$userId") },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Kitchen,
                                    contentDescription = "Pantry",
                                    tint = Color.White
                                )
                                Text("Pantry", color = Color.White, fontSize = 14.sp)
                            }
                        }
                        Button(
                            onClick = { navController.navigate("receipt/$userId") },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = "Receipts",
                                    tint = Color.White
                                )
                                Text("Receipts", color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    val navController = rememberNavController()
    CookingAssistantTheme {
        DashboardScreen(
            navController = navController,
            userId = 1,
            modifier = Modifier.fillMaxSize()
        )
    }
}
