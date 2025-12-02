package com.example.inventory.ui.recipe
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.inventory.ui.AppViewModel
import com.example.inventory.ui.navigation.BottomNavigationBar
import com.example.inventory.ui.theme.CookingAssistantTheme
import com.example.inventory.ui.theme.LightGreen
import com.example.inventory.ui.theme.PrimaryGreen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.draw.drawBehind
import com.example.inventory.InventoryApplication
import com.example.inventory.ui.userdata.FakeOnlineRecipesRepository
import com.example.inventory.ui.userdata.FakeMyPantryViewModel
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeRecommendationScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: RecipeViewModel? = null,
    appViewModel: AppViewModel
) {
    val userId = appViewModel.userId.value
    val context = androidx.compose.ui.platform.LocalContext.current
    val actualViewModel = viewModel ?: remember {
        if (context.applicationContext is InventoryApplication) {
            val appContainer = (context.applicationContext as InventoryApplication).container
            RecipeViewModel(
                onlineRecipesRepository = appContainer.onlineRecipesRepository,
                myPantryViewModel = appContainer.myPantryViewModel,
                appViewModel = appViewModel
            )
        } else {
            RecipeViewModel(
                onlineRecipesRepository = FakeOnlineRecipesRepository(),
                myPantryViewModel = FakeMyPantryViewModel(),
                appViewModel = appViewModel
            )
        }
    }

    val uiState by actualViewModel.uiState.collectAsState()

    CookingAssistantTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(end = 48.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Recipe Recommendations",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    }
                )
            },
            bottomBar = { BottomNavigationBar(navController, appViewModel) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .then(modifier)
            ) {
                RecipeBody(
                    uiState = uiState,
                    onFilterClick = actualViewModel::toggleFilter,
                    onIngredientToggle = actualViewModel::toggleIngredientExclusion,
                    onFindRecipesClick = actualViewModel::findRecipesWithAI,
                    navController = navController,
                    appViewModel = appViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeBody(
    uiState: RecipeUiState,
    onFilterClick: (String) -> Unit,
    onIngredientToggle: (String) -> Unit,
    onFindRecipesClick: () -> Unit,
    navController: NavController,
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            // 1. Loading state
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = PrimaryGreen, strokeWidth = 4.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading recipes...", fontSize = 16.sp, color = Color.Gray)
                }
            }

            // 2. No ingredients or no recipes
            uiState.recipes.isEmpty() && uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Text("Check your ingredients", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = uiState.errorMessage ?: "No recipes found.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { navController.navigate("my_pantry/${appViewModel.userId.value}") },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Check Ingredients", color = Color.White)
                    }
                }
            }

            // 3. Show recipes
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Available Ingredients Card with checkboxes
                    item {
                        AvailableIngredientsCard(
                            ingredients = uiState.availableIngredients,
                            excludedIngredients = uiState.excludedIngredients,
                            onIngredientToggle = onIngredientToggle,
                            onFindRecipesClick = onFindRecipesClick,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Filter chips
                    item {
                        FiltersRow(
                            selectedFilters = uiState.selectedFilters,
                            allFilters = uiState.allFilters,
                            onFilterClick = onFilterClick,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Recipe list
                    items(items = uiState.recipes, key = { it.id }) { recipe ->
                        RecipeCard(recipe = recipe)
                    }
                }
            }
        }
    }
}

// New: Ingredients card with checkboxes + AI button
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AvailableIngredientsCard(
    ingredients: List<String>,
    excludedIngredients: Set<String>,
    onIngredientToggle: (String) -> Unit,
    onFindRecipesClick: () -> Unit,
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
                Text(
                    text = "Your pantry is empty. Add ingredients to get recipe suggestions!",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

            // AI Search Button - Only triggers search when clicked
            Button(
                onClick = onFindRecipesClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp),
                enabled = ingredients.isNotEmpty() && ingredients.any { it !in excludedIngredients }
            ) {
                Text("Find my recipe with AI", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Only unchecked ingredients will be used for recipe search",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

// Single ingredient chip with checkbox and strikethrough effect when excluded
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
            .border(
                width = 1.8.dp,
                color = if (isExcluded) Color.Gray else PrimaryGreen,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = if (isExcluded) Color.LightGray.copy(alpha = 0.3f) else LightGreen.copy(alpha = 0.4f)
            )
            .clickable { onToggle() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Check box
        Checkbox(
            checked = isExcluded,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(18.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = PrimaryGreen,
                uncheckedColor = Color.Gray,
                checkmarkColor = Color.White
            )
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Strikethrough effect when excluded
        Text(
            text = name,
            fontSize = 14.sp,
            color = if (isExcluded) Color.Gray else Color.Black,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .drawBehind {
                    if (isExcluded) {
                        val strokeWidth = 1.5.dp.toPx()
                        val y = size.height / 2
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.8f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
                }
        )
    }
}

// Filter chips row
@Composable
fun FiltersRow(
    selectedFilters: Set<String>,
    allFilters: List<String>,
    onFilterClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        allFilters.forEach { filter ->
            val isSelected = selectedFilters.contains(filter)
            FilterChip(
                label = filter,
                isSelected = isSelected,
                onClick = { onFilterClick(filter) }
            )
        }
    }
}

@Composable
fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) PrimaryGreen else Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isSelected) PrimaryGreen else Color.LightGray),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 14.sp
        )
    }
}

// Recipe card (unchanged)
@Composable
fun RecipeCard(recipe: RecipeUiModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navigate to recipe detail */ },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(32.dp)
                )
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

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    NutritionValue("cal", recipe.calories)
                    NutritionValue("P:", recipe.protein)
                    NutritionValue("C:", recipe.carbs)
                    NutritionValue("F:", recipe.fat)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightGreen)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = recipe.ingredientUsage,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryGreen
                )
            }
        }
    }
}

@Composable
fun NutritionValue(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun RecipeRecommendationScreenPreview() {
    CookingAssistantTheme {
        RecipeRecommendationScreen(
            modifier = Modifier,
            navController = rememberNavController(),
            appViewModel = AppViewModel()
        )
    }
}