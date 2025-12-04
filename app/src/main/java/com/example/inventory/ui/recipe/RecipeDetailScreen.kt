package com.example.inventory.ui.recipe

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    navController: NavController,
    appViewModel: AppViewModel,
    recipeId: Long,
    viewModel: RecipeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val recipe = uiState.recipes.firstOrNull { it.id == recipeId }

    Log.d("RecipeDetail", "recipeId=$recipeId, recipes=${uiState.recipes.size}")


    CookingAssistantTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(recipe?.name ?: "Recipe Detail", fontWeight = FontWeight.Bold) },
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
    recipe: RecipeUiModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(recipe.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            recipe.subtitle,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = PrimaryGreen)
            Spacer(Modifier.width(4.dp))
            Text("${recipe.time} • ${recipe.servings} servings", fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
            Text(recipe.calories, fontSize = 14.sp, color = PrimaryGreen)
        }

        Spacer(Modifier.height(16.dp))

        Text("Ingredients", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        if (recipe.ingredients.isEmpty()) {
            Text("No ingredients available.", color = Color.Gray, fontSize = 14.sp)
        } else {
            recipe.ingredients.forEach { item ->
                Text("• $item", fontSize = 14.sp, modifier = Modifier.padding(vertical = 2.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Instructions", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        if (recipe.instructions.isEmpty()) {
            Text("No instructions available.", color = Color.Gray, fontSize = 14.sp)
        } else {
            recipe.instructions.forEachIndexed { index, step ->
                Text(
                    text = "${index + 1}. $step",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
