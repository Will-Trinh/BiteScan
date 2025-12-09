package com.example.inventory.ui.recipe

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inventory.ui.AppViewModel
import com.example.inventory.ui.navigation.BottomNavigationBar
import com.example.inventory.ui.theme.CookingAssistantTheme
import com.example.inventory.ui.theme.PrimaryGreen
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.inventory.InventoryApplication
import com.example.inventory.data.Recipe
import java.sql.Date
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextDecoration
import android.widget.Toast
import androidx.compose.ui.platform.LocalUriHandler


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    navController: NavController,
    appViewModel: AppViewModel,
    recipeId: Int,
) {
    Log.d("RecipeDetailScreen", "Check in RecipeDetailScreen, recipeId: $recipeId")
    val context = LocalContext.current
    val viewModel: RecipeDetailViewModel = remember(recipeId) {
        val appContainer = (context.applicationContext as InventoryApplication).container
            RecipeDetailViewModel(
                recipesRepository = appContainer.recipesRepository,
                myPantryViewModel = appContainer.myPantryViewModel,
                appViewModel = appViewModel
            )

    }

    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val recipe = uiState.recipe

    Log.d("RecipeDetail", "recipeId=$recipeId, recipes=${uiState.recipe?.title}")


    CookingAssistantTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            recipe?.title ?: "Recipe Detail",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            bottomBar = { BottomNavigationBar(navController, appViewModel) }
        ) { innerPadding ->
            if (recipe == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Recipe not found.", color = Color.Gray)
                }
            } else {
                RecipeDetailContent(
                    recipe = recipe,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
fun RecipeDetailContent(
    recipe: Recipe,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Text(recipe.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            recipe.description,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = PrimaryGreen)
            Spacer(Modifier.width(4.dp))
            Text("${recipe.totalTime} • ${recipe.servings} servings", fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
        }

        Spacer(Modifier.height(16.dp))

        Text("Ingredients", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        if (recipe.ingredients.isEmpty()) {
            Text("No ingredients available.", color = Color.Gray, fontSize = 14.sp)
        } else {
            val ingredientLines = recipe.ingredients
                .lines() // same as split("\n")
                .map { it.trim().removePrefix("•").trim() }
                .filter { it.isNotEmpty() }

            ingredientLines.forEach { ingredient ->
                Text(
                    text = "• $ingredient",
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }


        Spacer(Modifier.height(16.dp))

        Text("Instructions", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        if (recipe.instructions.isEmpty()) {
            Text("Please visit the source to see the instructions. ${recipe.source}", color = Color.Gray, fontSize = 14.sp)
        } else {
            SimpleInstructions(recipe.instructions)
            Text(
                text = "Please visit the source to see more instructions:",
                color = Color.Gray,
                fontSize = 14.sp,)
            Text(
                text = recipe.source,
                color = Color.Blue,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable {
                        try {
                            uriHandler.openUri(recipe.source)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
                        }
                    },

                style = MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = TextDecoration.Underline
                )
            )
        }
    }
}

@Composable
fun SimpleInstructions(instructions: String) {
    Log.d("RecipeDetailScreen", "Raw instructions:\n$instructions")

    if (instructions.isBlank()) {
        Text("Please visit the source to see the instructions.", color = Color.Gray)
        return
    }

    // Split instructions by line only
    val lines = instructions
        .lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        lines.forEachIndexed { index, rawStep ->
            var stepText = rawStep

            // Remove leading numbering / bullets / 'Step'
            stepText = stepText.replaceFirst(Regex("^\\s*\\d+\\s*\\.?\\s*"), "").trim()
            stepText = stepText
                .removePrefix("•").removePrefix("·")
                .removePrefix("-").removePrefix("–").removePrefix("—")
                .removePrefix("Step").removePrefix("step")
                .trim()

            // Remove trailing dot
            if (stepText.endsWith(".")) {
                stepText = stepText.dropLast(1)
            }

            // Capitalize first letter
            if (stepText.isNotEmpty()) {
                stepText = stepText.replaceFirstChar { it.uppercase() }
            }

            if (stepText.isNotEmpty()) {
                Text(
                    text = "${index + 1}. $stepText",
                    fontSize = 15.sp,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeDetailScreenPreview() {
    CookingAssistantTheme {
        RecipeDetailContent(
            recipe = Recipe(
                recipeId = 1,
                userId = 1,
                source = "Sample Source",
                title = "Spaghetti Bolognese",
                description = "Classic Italian pasta dish",
                totalTime = 45,
                servings = 4,
                nutrition = "600 kcal",
                ingredients = "Spaghetti, Ground beef, Tomato sauce, Onion, Garlic",
                dateSaved = Date(System.currentTimeMillis()),
                instructions =
                    "Cook the spaghetti according to package instructions. In a large pot, sauté the onion and garlic until softened. Add the ground beef, tomato sauce, and sauté for another 5 minutes. Drain the spaghetti and add it to the sauce."
            ),

            )
    }
}

