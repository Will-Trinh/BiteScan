package com.example.inventory.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.navigation.BottomNavigationBar
import com.example.inventory.ui.theme.CookingAssistantTheme
import androidx.compose.material.icons.filled.Restaurant
import com.example.inventory.R
import androidx.compose.ui.platform.LocalContext
import com.example.inventory.InventoryApplication
import com.example.inventory.data.OfflineUsersRepository
import com.example.inventory.data.ItemsRepository
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.style.TextAlign


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    userId: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as InventoryApplication).container
    val viewModel = remember {
        DashboardViewModel(
            usersRepository = appContainer.usersRepository as OfflineUsersRepository,
            itemsRepository = appContainer.itemsRepository as ItemsRepository
        )
    }
    LaunchedEffect(userId) {
        viewModel.setCurrentUserId(userId)
    }

    val metrics by viewModel.metrics.collectAsState()
    val macroBreakdown by viewModel.macroBreakdown.collectAsState()
    val spendingByCategory by viewModel.spendingByCategory.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val recipes by viewModel.recipes.collectAsState()

    // Loading state
    val isLoading = metrics.calories == 0 && metrics.items == 0

    CookingAssistantTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Nutrition Dashboard",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = { BottomNavigationBar(navController) }
        ) { paddingValues ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { MetricsSection(metrics = metrics, modifier = Modifier.padding(top = 8.dp)) }

                    item { MacroBreakdownSection(macroBreakdown = macroBreakdown) }

                    item { SpendingByCategorySection(spendingByCategory = spendingByCategory) }

                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Insights",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            insights.forEach { insight ->
                                InsightsCard(
                                    title = insight.title,
                                    message = insight.message,
                                    color = insight.color,
                                    backgroundColor = insight.backgroundColor
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }

                    item {
                        RecipesSection(
                            recipes = recipes,
                            onSeeAllClick = { /* TODO: Navigate to recipes list with userId */ }
                        )
                    }

                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

// --- Metrics Section ---

@Composable
fun MetricsSection(metrics: DashboardMetrics, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricPill(
                value = "${metrics.calories}",
                label = "Calories",
                color = Color(0xFF068378)
            )
            MetricPill(
                value = "${metrics.protein}g",
                label = "Protein",
                color = Color(0xFF2196F3)
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricPill(
                value = "$${String.format("%.2f", metrics.spend)}",
                label = "Total Spent",
                color = MaterialTheme.colorScheme.primary
            )
            MetricPill(
                value = "${metrics.items}",
                label = "Items",
                color = Color(0xFFF59304)
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

// --- Macro Breakdown Section ---

@Composable
fun MacroBreakdownSection(macroBreakdown: MacroBreakdown) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Macro Breakdown",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pie Chart (size 120.dp)
                PieChart(
                    macroBreakdown = macroBreakdown,
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.width(32.dp))

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroLegendItem(
                        color = Color(0xFF2F6C30),  // Green for Protein
                        label = "Protein",
                        percentage = macroBreakdown.proteinPercent
                    )
                    MacroLegendItem(
                        color = Color(0xFF711E81),  // Gray for Carbs
                        label = "Carbs",
                        percentage = macroBreakdown.carbsPercent
                    )
                    MacroLegendItem(
                        color = Color(0xFFF4A142),  // Orange for Fats
                        label = "Fats",
                        percentage = macroBreakdown.fatsPercent
                    )
                }
            }
        }
    }
}

@Composable
fun PieChart(
    macroBreakdown: MacroBreakdown,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val percentages = listOf(
            macroBreakdown.proteinPercent.removeSuffix("%").toFloatOrNull() ?: 0f,
            macroBreakdown.carbsPercent.removeSuffix("%").toFloatOrNull() ?: 0f,
            macroBreakdown.fatsPercent.removeSuffix("%").toFloatOrNull() ?: 0f
        )
        val total = percentages.sum()
        if (total == 0f) return@Canvas  // Skip if no data

        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2
        var startAngle = 0f

        // Colors for each slice
        val colors = listOf(
            Color(0xFF2F6C30),      // Protein: Green
            Color(0xFF757575),                      // Carbs: Gray
            Color(0xFFF4A142)                       // Fats: Orange
        )

        percentages.forEachIndexed { index, percent ->
            val sweepAngle = (percent / total) * 360f
            drawArc(
                color = colors[index],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
            startAngle += sweepAngle
        }

        // Optional: White border for clarity
        drawCircle(
            color = Color.White,
            radius = radius * 0.9f,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun MacroLegendItem(color: Color, label: String, percentage: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
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
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = percentage,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// --- Spending by Category Section ---

@Composable
fun SpendingByCategorySection(spendingByCategory: SpendingByCategory) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Spending by Category",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (spendingByCategory.categories.isEmpty()) {
            Text(
                text = "No data available",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            CategoryBarChartPlaceholder(categories = spendingByCategory.categories)
        }
    }
}

@Composable
fun CategoryBarChartPlaceholder(categories: List<Pair<String, Float>>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
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
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.height(4.dp))
                Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Text(
                    text = "🌱",
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

// --- Recipes Section ---

@Composable
fun RecipesSection(recipes: List<RecipeSuggestion>, onSeeAllClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Recipes You Can Make",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onSeeAllClick) {
                Text("See All", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(12.dp))

        if (recipes.isEmpty()) {
            Text(
                text = "No recipes available yet",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                recipes.forEach { recipe ->
                    RecipeItem(
                        title = recipe.title,
                        details = recipe.details,
                        iconResId = recipe.iconResId
                    )
                    if (recipe != recipes.last()) {
                        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeItem(title: String, details: String, iconResId: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.tertiary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = details,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    val navController = rememberNavController()
    CookingAssistantTheme {
        DashboardScreen(navController = navController, userId = 1)
    }
}