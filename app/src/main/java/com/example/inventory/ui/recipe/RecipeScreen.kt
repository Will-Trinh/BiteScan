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
import com.example.inventory.ui.theme.CookingAssistantTheme

private val PrimaryGreen = Color(0xFF4CAF50)
private val LightGreen = Color(0xFFE8F5E9)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecipeRecommendationScreen(
    navController: NavController,
    userId: Int,
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appContainer = (context.applicationContext as com.example.inventory.InventoryApplication).container

    val viewModel: RecipeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return RecipeViewModel(
                        userId = userId,
                        itemsRepository = appContainer.itemsRepository,
                        receiptsRepository = appContainer.receiptsRepository
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

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
                    onFilterClick = viewModel::toggleFilter,
                    navController = navController,
                    userId = userId,
                    modifier = Modifier.fillMaxSize(),
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
    userId: Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AvailableIngredientsCard(
                ingredients = uiState.availableIngredients,
                onCardClick = {
                    navController.navigate(
                        com.example.inventory.ui.navigation.MyPantryDestination.routeWithArgs
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

        items(uiState.recipes) { recipe ->
            RecipeCard(recipe = recipe)
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
fun RecipeCard(recipe: Recipe) {
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
            userId = 1,
            appViewModel = AppViewModel()
        )
    }
}