package com.example.inventory.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.navigation.BottomNavigationBar
import com.example.inventory.ui.theme.CookingAssistantTheme
import androidx.compose.ui.platform.LocalContext
import com.example.inventory.InventoryApplication
import com.example.inventory.data.OfflineUsersRepository
import com.example.inventory.data.ItemsRepository
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.style.TextAlign
import com.example.inventory.ui.AppViewModel
import com.example.inventory.ui.recipe.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel
) {
    val userId = appViewModel.userId.value
    val context = LocalContext.current
    val appContainer = (context.applicationContext as InventoryApplication).container
    val viewModel = remember {
        DashboardViewModel(
            usersRepository = appContainer.usersRepository as OfflineUsersRepository,
            itemsRepository = appContainer.itemsRepository as ItemsRepository
        )
    }
    val recipeViewModel = remember {
        RecipeViewModel(
            onlineRecipesRepository = appContainer.onlineRecipesRepository,
            myPantryViewModel = appContainer.myPantryViewModel,
            appViewModel = appViewModel
        )
    }
    LaunchedEffect(userId) {
        viewModel.setCurrentUserId(userId?:0)
    }


    val metrics by viewModel.metrics.collectAsState()
    val macroBreakdown by viewModel.macroBreakdown.collectAsState()
    val spendingByCategory by viewModel.spendingByCategory.collectAsState()
    val insights by viewModel.insights.collectAsState()

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
            bottomBar = { BottomNavigationBar(navController,appViewModel)}
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
        val raw = listOf(
            macroBreakdown.proteinPercent.removeSuffix("%").toFloatOrNull() ?: 0f,
            macroBreakdown.carbsPercent.removeSuffix("%").toFloatOrNull() ?: 0f,
            macroBreakdown.fatsPercent.removeSuffix("%").toFloatOrNull() ?: 0f
        )
        // Fallback: if all zero, show an even split so the user still sees a pie.
        val values = if (raw.all { it <= 0f }) listOf(34f, 33f, 33f) else raw
        val total = values.sum()
        if (total <= 0f) return@Canvas

        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2f
        var startAngle = 0f

        val colors = listOf(
            Color(0xFF2F6C30), // Protein
            Color(0xFF711E81), // Carbs (match your legend color)
            Color(0xFFF4A142)  // Fats
        )

        values.forEachIndexed { index, value ->
            val sweepAngle = (value / total) * 360f
            drawArc(
                color = colors[index],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            startAngle += sweepAngle
        }

        // Optional thin ring for definition
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = radius,
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

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    val navController = rememberNavController()
    CookingAssistantTheme {
        DashboardScreen(navController = navController, appViewModel = AppViewModel())
    }
}