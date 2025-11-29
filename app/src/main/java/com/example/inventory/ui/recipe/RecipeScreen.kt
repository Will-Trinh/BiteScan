package com.example.inventory.ui.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.navigation.BottomNavigationBar
import com.example.inventory.ui.AppViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.ArrowForward
import com.example.inventory.InventoryApplication
import com.example.inventory.data.RecipesRepository
import com.example.inventory.ui.receipt.EditReceiptViewModel
import com.example.inventory.ui.theme.CookingAssistantTheme
import com.example.inventory.ui.theme.PrimaryGreen
import com.example.inventory.ui.theme.LightGreen
import com.example.inventory.ui.userdata.FakeItemsRepository
import com.example.inventory.ui.userdata.FakeReceiptsRepository
import androidx.compose.ui.text.style.TextAlign
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
        val app = context.applicationContext as InventoryApplication
        val container = app.container

        RecipeViewModel(
            onlineRecipesRepository = container.onlineRecipesRepository,
            myPantryViewModel = container.myPantryViewModel,
            appViewModel = appViewModel
        )

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
                    )
                    { Text( "Recipe Recommendations",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black )
                    } },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() })
                        {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black )
                        } }, ) },
            bottomBar = { BottomNavigationBar(navController, appViewModel) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .then(modifier),
            ) {
                RecipeBody(
                    uiState = uiState,
                    onFilterClick = actualViewModel::toggleFilter,
                    navController = navController,
                    modifier = Modifier.fillMaxSize(),
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
    navController: NavController,
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val userId = appViewModel.userId.value

    Box(modifier = modifier.fillMaxSize()) {
        when {
            // 1. loading
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen, strokeWidth = 4.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading recipes...",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 2. No recipes found
            uiState.recipes.isEmpty() && uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Check your ingredients",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.errorMessage ?: "No recipes found.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { navController.navigate("my_pantry/$userId") },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Check Ingredients", color = Color.White)
                    }
                }
            }

            // 3. Recipes found
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        AvailableIngredientsCard(
                            ingredients = uiState.availableIngredients,
                            onCardClick = {
                                navController.navigate(
                                    com.example.inventory.ui.navigation.MyPantryDestination.route
                                        .replace("{userId}", userId.toString())
                                )
                            },
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    item {
                        FiltersRow(
                            selectedFilters = uiState.selectedFilters,
                            allFilters = uiState.allFilters,
                            onFilterClick = onFilterClick,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(
                        items = uiState.recipes,
                        key = { it.id }
                    ) { recipe ->
                        RecipeCard(recipe = recipe)
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
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
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

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ingredients.forEach { ingredient ->
                    ReadOnlyIngredientChip(label = ingredient)
                }
            }

            TextButton(
                onClick = onCardClick,
                modifier = Modifier.padding(top = 8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Go To Pantry",
                        color = PrimaryGreen,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Go To Pantry",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReadOnlyIngredientChip(label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(LightGreen)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Black)
    }
}


// --- Filters Row ---
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
    val backgroundColor = if (isSelected) PrimaryGreen else Color.White
    val contentColor = if (isSelected) Color.White else Color.Black
    val borderColor = if (isSelected) PrimaryGreen else Color.LightGray

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text = label, color = contentColor, fontSize = 14.sp)
    }
}

// --- Recipe Card ---
@Composable
fun RecipeCard(recipe: RecipeUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { /* TODO: Navigate to recipe details */ },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recipe Image/Icon (Placeholder)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightGreen), // Placeholder background
                contentAlignment = Alignment.Center
            ) {
                // Using a generic icon as a placeholder
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = "Recipe Image",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = recipe.subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Details Row: Time and Servings
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = recipe.time, fontSize = 14.sp, color = Color.Black)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.WbSunny, // Used as a placeholder for people/servings icon
                        contentDescription = "Servings",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = recipe.servings, fontSize = 14.sp, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Nutrition Row (Cal, P, C, F)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NutritionValue(label = "cal", value = recipe.calories)
                    NutritionValue(label = "P:", value = recipe.protein)
                    NutritionValue(label = "C:", value = recipe.carbs)
                    NutritionValue(label = "F:", value = recipe.fat)
                }
            }

            // Uses Ingredients Badge
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(2.dp)) // Alignment offset
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
}

@Composable
fun NutritionValue(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

// --- Preview ---
@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
fun RecipeRecommendationScreenPreview() {
    val navController = rememberNavController()
    CookingAssistantTheme {
        RecipeRecommendationScreen(
            navController = navController,
            appViewModel = AppViewModel()
        )
    }
}