package com.example.inventory.ui.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inventory.ui.AppViewModel
import com.example.inventory.ui.navigation.BottomNavigationBar
import com.example.inventory.ui.theme.CookingAssistantTheme
import com.example.inventory.ui.theme.LightGreen
import com.example.inventory.ui.theme.PrimaryGreen
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun RecipeRecommendationScreen(
    navController: NavController,
    navigateToRecipeDetail: (Int) -> Unit,
    appViewModel: AppViewModel,
    viewModel: RecipeViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }


    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = { viewModel.refresh() }
    )
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Call a function in your ViewModel to reload the diet
                viewModel.refresh()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    CookingAssistantTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Recipe Recommendations",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    },
                )
            },
            bottomBar = { BottomNavigationBar(navController, appViewModel) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                RecipeBody(
                    uiState = uiState,
                    onIngredientToggle = viewModel::toggleIngredientExclusion,
                    onFindRecipesClick = viewModel::findRecipesWithGg,
                    onFindRecipesAIClick = viewModel::findRecipesWithAi,
                    navigateToRecipeDetail = navigateToRecipeDetail,
                    navController = navController,
                    viewModel = viewModel,
                    appViewModel = appViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeBody(
    uiState: RecipeUiState,
    onIngredientToggle: (String) -> Unit,
    onFindRecipesClick: () -> Unit,
    onFindRecipesAIClick: () -> Unit,
    navigateToRecipeDetail: (Int) -> Unit,
    navController: NavController,
    viewModel: RecipeViewModel,
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = PrimaryGreen, strokeWidth = 4.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Finding delicious recipes...", fontSize = 16.sp, color = Color.Gray)
                }
            }

            uiState.recipes.isEmpty() && uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Restaurant, null, tint = Color.LightGray, modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(24.dp))
                    Text("No recipes found", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text(uiState.errorMessage, textAlign = TextAlign.Center, color = Color.Gray)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { navController.navigate("my_pantry") },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Go to My Pantry", color = Color.White)
                    }
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Ingredients + AI Button
                    item {
                        AvailableIngredientsCard(
                            ingredients = uiState.availableIngredients,
                            excludedIngredients = uiState.excludedIngredients,
                            onIngredientToggle = onIngredientToggle,
                            onFindRecipesClick = onFindRecipesClick,
                            onFindRecipesAIClick = onFindRecipesAIClick,
                            onGoToPantry = { navController.navigate("my_pantry") }
                        )
                    }

                    // 2. Filters Card (Country, Style, Diet)
                    item {
                        FiltersCard(
                            uiState = uiState,
                            onSelectCountry = viewModel::selectCountry,
                            onSelectStyle = viewModel::selectStyle,
                            onToggleFilter = viewModel::toggleFilter
                        )
                    }

                    // 3. Recipe List
                    items(items = uiState.recipes, key = { it.id }) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onRecipeClick =  navigateToRecipeDetail )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AvailableIngredientsCard(
    ingredients: List<String>,
    excludedIngredients: Set<String>,
    onIngredientToggle: (String) -> Unit,
    onFindRecipesClick: () -> Unit,
    onFindRecipesAIClick: () -> Unit,
    onGoToPantry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Your Available Ingredients",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (ingredients.isEmpty()) {
                Text("Your pantry is empty. Add ingredients to get suggestions!", color = Color.Gray)
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ingredients.forEach { ingredient ->
                        IngredientWithCheckbox(
                            name = ingredient,
                            isExcluded = excludedIngredients.contains(ingredient),
                            onToggle = { onIngredientToggle(ingredient) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = modifier.fillMaxWidth()) {
                Button(
                    onClick = onGoToPantry,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = true
                ) {
                    Text("Manage my pantry", color = Color.White, fontSize = 16.sp)
                }
                Button(
                    onClick = onFindRecipesClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = ingredients.isNotEmpty() && ingredients.any { it !in excludedIngredients }
                ) {
                    Text("Find my recipe with Google", color = Color.White, fontSize = 16.sp)
                }
                Button(
                    onClick = onFindRecipesAIClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = ingredients.isNotEmpty() && ingredients.any { it !in excludedIngredients }
                ) {
                    Text("Find my recipe with AI", color = Color.White, fontSize = 16.sp)
                }
            }


            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Only unchecked ingredients will be used",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun IngredientWithCheckbox(
    name: String,
    isExcluded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.8.dp, if (isExcluded) Color.Gray else PrimaryGreen, RoundedCornerShape(20.dp))
            .background(if (isExcluded) Color.LightGray.copy(0.3f) else LightGreen.copy(0.4f))
            .clickable { onToggle() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Checkbox(
            checked = isExcluded,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(18.dp),
            colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen, checkmarkColor = Color.White)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = name,
            fontSize = 14.sp,
            color = if (isExcluded) Color.Gray else Color.Black,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.drawBehind {
                if (isExcluded) {
                    val y = size.height / 2
                    drawLine(Color.Gray.copy(0.8f), Offset(0f, y), Offset(size.width, y), 1.5.dp.toPx())
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FiltersCard(
    uiState: RecipeUiState,
    onSelectCountry: (String) -> Unit,
    onSelectStyle: (String) -> Unit,
    onToggleFilter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Filters", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

            // Country - Single Select
            Text("Country", fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                uiState.allCountries.forEach { country ->
                    FilterCheckboxChip(
                        label = country,
                        isSelected = uiState.selectedCountry == country,
                        onClick = { onSelectCountry(country) }
                    )
                }
            }

            // Cooking Style - Single Select
            Text("Cooking Style", fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                uiState.allStyles.forEach { style ->
                    FilterCheckboxChip(
                        label = style,
                        isSelected = uiState.selectedStyle == style,
                        onClick = { onSelectStyle(style) }
                    )
                }
            }

            // Diet & Time - Multi Select
            Text("Diet & Time", fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
//            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                uiState.allFilters.forEach { filter ->
//                    FilterCheckboxChip(
//                        label = filter,
//                        isSelected = uiState.selectedFilters.contains(filter),
//                        onClick = { onToggleFilter(filter) }
//                    )
//                }
//            }
            // User Diet - one Select
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                Log.d("RecipeViewModel", "Filter diet is: ${uiState.selectedFilters}")
                listOf("Any") + uiState.allFilters.forEach { filter ->
                    FilterCheckboxChip(
                        label = filter,
                        isSelected = when (filter) {
                            "Any" -> uiState.selectedFilters == "Any" || uiState.selectedFilters.isBlank()
                            else -> uiState.selectedFilters == filter
                        },
                        onClick = { onToggleFilter(filter) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterCheckboxChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, fontSize = 14.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PrimaryGreen,
            selectedLabelColor = Color.White,
            containerColor = Color(0xFFF1F8E9),
            labelColor = Color.Black
        ),
    )
}

@Composable
fun RecipeCard(
    onRecipeClick: (Int) -> Unit,
    recipe: RecipeUiModel,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRecipeClick((recipe.id).toInt()) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Restaurant, null, tint = PrimaryGreen, modifier = Modifier.size(32.dp))
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(recipe.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(recipe.subtitle, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(recipe.time, fontSize = 14.sp)
                    Spacer(Modifier.width(16.dp))
                    Icon(Icons.Default.WbSunny, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(recipe.servings, fontSize = 14.sp)
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NutritionValue(label = "Calories:", value = recipe.calories)
                    NutritionValue(label = "Protein:", value = recipe.protein)
                    NutritionValue(label = "Carbs:", value = recipe.carbs)
                    NutritionValue(label = "Fat:", value = recipe.fat)
                }
            }
        }
    }
}


@Composable
fun NutritionValue(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}


@Preview(showBackground = true)
@Composable
fun FiltersCardPreview() {
    CookingAssistantTheme {
        FiltersCard(
            uiState = RecipeUiState(),
            onSelectCountry = {},
            onSelectStyle = {},
            onToggleFilter = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeCardPreview() {
    CookingAssistantTheme {
        Surface(color = Color(0xFFF5F5F5)) {
            Box(modifier = Modifier.padding(16.dp)) {
                RecipeCard(
                    onRecipeClick = { /* no-op for preview */ },
                    recipe = RecipeUiModel(
                        id = 1,
                        name = "Cabbage Stir-Fry with Egg",
                        subtitle = "A simple and delicious stir-fry using cabbage and eggs",
                        time = "20 min",
                        servings = "4",
                        calories = "250 kcal",
                        protein = "12g",
                        carbs = "15g",
                        fat = "10g",
                        ingredientUsage = "AI",
                        sourceUrl = ""
                    )
                )
            }
        }
    }
}
