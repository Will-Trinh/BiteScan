package com.example.inventory.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.navigation.BottomNavigationBar
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.ui.theme.CookingAssistantTheme
import androidx.compose.material.icons.filled.Restaurant
import com.example.inventory.R

// Define colors based on the screenshot
private val PrimaryGreen = Color(0xFF4CAF50) // Used for total spent, bars, main accents
private val LightGreen = Color(0xFFE8F5E9) // Used for insight backgrounds
private val CardBackground = Color.White
private val DashboardBackground = Color(0xFFF5F5F5) // Light grey background

// Placeholder destinations (disabled for now)
object DashboardDestination : NavigationDestination {
    override val route = "dashboard"
    override val titleRes = R.string.dashboard
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    CookingAssistantTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Nutrition Dashboard",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DashboardBackground, // Match screen background
                        titleContentColor = Color.Black
                    )
                )
            },
            bottomBar = { BottomNavigationBar(navController) }
        ) { paddingValues ->
            // Use LazyColumn to make the entire content scrollable
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(DashboardBackground)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Metrics Section (Calories, Protein, Spend, Items)
                item { MetricsSection(modifier = Modifier.padding(top = 8.dp)) }

                // 2. Macro Breakdown Chart
                item { MacroBreakdownSection() }

                // 3. Spending by Category Chart
                item { SpendingByCategorySection() }

                // 4. Insights
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Insights",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        InsightsCard(
                            title = "Great produce choices",
                            message = "Spinach is packed with iron and folate - perfect for your fitness goals.",
                            color = PrimaryGreen.copy(alpha = 0.8f),
                            backgroundColor = LightGreen
                        )
                        Spacer(Modifier.height(8.dp))
                        InsightsCard(
                            title = "Higher protein than last trip",
                            message = "You're getting 16% more protein per dollar spent compared to your previous shopping trip.",
                            color = Color(0xFFF4A142), // Orange/Yellow
                            backgroundColor = Color(0xFFFFF3E0) // Light orange background
                        )
                    }
                }

                // 5. Recipes You Can Make
                item {
                    RecipesSection(
                        onSeeAllClick = { /* TODO: Navigate to recipes list */ }
                    )
                }

                // Add padding at the very bottom to ensure the last elements aren't cut off by the Bottom Bar
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

// --- Metrics Section ---

@Composable
fun MetricsSection(modifier: Modifier = Modifier) {
    // Placeholder Data
    val calories = 1847
    val protein = 142
    val spend = 19.76
    val items = 4

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricPill(
                value = "$calories",
                label = "Calories",
                color = Color.Black
            )
            MetricPill(
                value = "${protein}g",
                label = "Protein",
                color = Color.Black
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricPill(
                value = "$${String.format("%.2f", spend)}",
                label = "Total Spent",
                color = PrimaryGreen
            )
            MetricPill(
                value = "$items",
                label = "Items",
                color = Color.Black
            )
        }
    }
}

@Composable
fun RowScope.MetricPill(value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = value,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

// --- Macro Breakdown Section ---

@Composable
fun MacroBreakdownSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Macro Breakdown",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Donut Chart Placeholder
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Placeholder for Donut Chart (approx 120dp size)
                MacroChartPlaceholder(
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.width(32.dp))

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroLegendItem(Color(0xFF4CAF50), "Protein", "30%") // Green
                    MacroLegendItem(Color(0xFF757575), "Carbs", "45%") // Grey
                    MacroLegendItem(Color(0xFFF4A142), "Fats", "25%") // Orange/Yellow
                }
            }
        }
    }
}

@Composable
fun MacroChartPlaceholder(modifier: Modifier = Modifier) {
    // Simple Circular Box to represent the Donut Chart
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Color(0xFF757575)) // Carbs 45% (Grey)
    ) {
        // Overlay slices (simplistic representation)
        // This would be replaced by a proper charting library
    }
}

@Composable
fun MacroLegendItem(color: Color, label: String, percentage: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Color Dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label ",
            fontSize = 14.sp,
            color = Color.Black
        )
        Text(
            text = percentage,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray
        )
    }
}

// --- Spending by Category Section ---

@Composable
fun SpendingByCategorySection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Spending by Category",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Bar Chart Placeholder
        CategoryBarChartPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )
    }
}

@Composable
fun CategoryBarChartPlaceholder(modifier: Modifier = Modifier) {
    val categories = listOf(
        "Produce" to 0.8f,
        "Meat" to 0.4f,
        "Dairy" to 0.7f,
        "Grains" to 0.6f
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        categories.forEach { (label, heightRatio) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(heightRatio)
                        .width(24.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(PrimaryGreen)
                )
                Spacer(Modifier.height(4.dp))
                Text(text = label, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// --- Insights Section ---

@Composable
fun InsightsCard(title: String, message: String, color: Color, backgroundColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Placeholder Icon/Emoji
                Text(
                    text = "🌱", // Placeholder for spinach/leaf icon
                    fontSize = 18.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.8f)
            )
        }
    }
}

// --- Recipes Section ---

@Composable
fun RecipesSection(onSeeAllClick: () -> Unit) {
    val recipes = listOf(
        RecipeItemData("Chicken & Spinach Power Bowl", "Uses 7 of 9 items • 25 min", R.drawable.spanish_chicken_soup_recipe),
        RecipeItemData("Greek Yogurt Berry Parfait", "Uses 3 of 5 items • 5 min", R.drawable.spanish_chicken_soup_recipe)
    )

    // Using a regular Column since the LazyColumn is the parent
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Recipes You Can Make",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onSeeAllClick) {
                Text("See All", color = PrimaryGreen, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(12.dp))

        // Since the parent is a LazyColumn, we use a standard Column here for the items
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            recipes.forEach { recipe ->
                RecipeItem(recipe = recipe)
                if (recipe != recipes.last()) {
                    Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }
        }
    }
}

data class RecipeItemData(val title: String, val details: String, val iconResId: Int)

@Composable
fun RecipeItem(recipe: RecipeItemData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon/Image Placeholder
        // NOTE: Placeholder using a Restaurant Icon. You'd replace R.drawable.ic_recipe_xxx
        // with your actual image resources.
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LightGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant, // Placeholder
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = recipe.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = recipe.details,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

// --- Preview ---

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    val navController = rememberNavController()
    CookingAssistantTheme {
        DashboardScreen(navController = navController)
    }
}