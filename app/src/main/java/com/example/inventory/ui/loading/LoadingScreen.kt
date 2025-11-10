package com.example.inventory.ui.loading

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.ui.theme.CookingAssistantTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt



private val PrimaryGreen = Color(0xFF4CAF50)
private val LightGrayBackground = Color(0xFFF5F5E9)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoadingScreen(
    viewModel: LoadingViewModel = viewModel()  // Load from ViewModel (dynamic)
) {
    val loadingState by viewModel.loadingState.collectAsState()

    // Hide if not loading
    if (loadingState.progress == 1f) return

    CookingAssistantTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 180.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightGrayBackground),
                    elevation = CardDefaults.cardElevation(4.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp).padding(vertical = 8.dp),
                            color = PrimaryGreen,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Processing Receipt...",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Analyzing your grocery items",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Animated Linear Progress Bar
                        val infiniteTransition = rememberInfiniteTransition()
                        val animatedProgress by infiniteTransition.animateFloat(
                            initialValue = loadingState.progress,
                            targetValue = loadingState.progress + 0.1f,  // Slight pulse for realism
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000),  // 2s loop
                                repeatMode = RepeatMode.Restart
                            )
                        )
                        LinearProgressIndicator(
                            progress = { animatedProgress.coerceIn(loadingState.progress, 1f) },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = PrimaryGreen,
                            trackColor = PrimaryGreen.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(loadingState.progress * 100).roundToInt()}% complete",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryGreen
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        // Dynamic Step List
                        loadingState.steps.forEachIndexed { index, step ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut()
                            ) {
                                StepStatusItem(label = step.label, status = step.status)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun StepStatusItem(label: String, status: StepStatus) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconSize = 18.dp

        val icon: @Composable () -> Unit = when (status) {
            StepStatus.COMPLETED -> {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
            StepStatus.IN_PROGRESS -> {
                {
                    CircularProgressIndicator(
                        modifier = Modifier.size(iconSize),
                        color = PrimaryGreen,
                        strokeWidth = 2.dp
                    )
                }
            }
            else -> {
                {
                    Box(
                        modifier = Modifier
                            .size(iconSize)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color.LightGray)
                    )
                }
            }
        }

        icon()

        Spacer(modifier = Modifier.width(16.dp))

        val textColor by animateColorAsState(
            targetValue = if (status == StepStatus.IN_PROGRESS) PrimaryGreen else Color.DarkGray,
            animationSpec = tween(300)
        )

        Text(
            text = label,
            fontSize = 15.sp,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Preview
@Composable
fun LoadingScreenPreview() {
    CookingAssistantTheme {
        LoadingScreen()
    }
}