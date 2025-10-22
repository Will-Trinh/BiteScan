package com.example.inventory.ui.loading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

// Define primary colors
private val PrimaryGreen = Color(0xFF4CAF50)
private val LightGrayBackground = Color(0xFFF5F5F5)

// --- State Management (Now Publicly Accessible) ---

/** * Represents the status of a single step in the processing workflow.
 * Marked public (default) to be used by the public LoadingScreen function.
 */
enum class StepStatus {
    COMPLETED,
    IN_PROGRESS,
    PENDING
}

/** * State model to simulate the progression of the loading screen.
 * Marked public (default) to be used by the public LoadingScreen function.
 */
data class LoadingState(
    val progress: Float = 0f,
    val step1: StepStatus = StepStatus.PENDING,
    val step2: StepStatus = StepStatus.PENDING,
    val step3: StepStatus = StepStatus.PENDING
)

// * Static state for preview/initial display
private val STATIC_LOADING_STATE = LoadingState(
    progress = 0.8f, // 80% complete
    step1 = StepStatus.COMPLETED,
    step2 = StepStatus.COMPLETED,
    step3 = StepStatus.IN_PROGRESS
)

// --- Composable UI ---

@Composable
fun LoadingScreen(
    // Now that LoadingState is public (or internal), it can be used here.
    loadingState: LoadingState = STATIC_LOADING_STATE
) {
    CookingAssistantTheme {
        // Use Surface for the entire screen background (light gray to match screenshot)
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


                // Main Content Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 180.dp),
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
                        // 1. Large Circular Progress Indicator (top spinner)
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp).padding(vertical = 8.dp),
                            color = PrimaryGreen,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // 2. Title and Subtitle
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

                        // 3. Linear Progress Bar and Status Text
                        LinearProgressIndicator(
                            progress = loadingState.progress,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = PrimaryGreen,
                            trackColor = PrimaryGreen.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(loadingState.progress * 100).toInt()}% complete",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryGreen
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        // 4. Step-by-Step Status Checklist
                        StepStatusItem(
                            label = "Image uploaded",
                            status = loadingState.step1
                        )
                        StepStatusItem(
                            label = "Text extracted",
                            status = loadingState.step2
                        )
                        StepStatusItem(
                            label = "Analyzing nutrition data",
                            status = loadingState.step3
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }
            }
        }
    }
}

/** Composable for a single checklist item. */
@Composable
private fun StepStatusItem(label: String, status: StepStatus) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconSize = 18.dp

        when (status) {
            StepStatus.COMPLETED -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(iconSize)
                )
            }
            StepStatus.IN_PROGRESS -> {
                // Use CircularProgressIndicator for the spinning icon
                CircularProgressIndicator(
                    modifier = Modifier.size(iconSize),
                    color = PrimaryGreen,
                    strokeWidth = 2.dp
                )
            }
            StepStatus.PENDING -> {
                // Simple placeholder dot for pending
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color.LightGray)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text style changes based on status
        val textColor = if (status == StepStatus.IN_PROGRESS) PrimaryGreen else Color.DarkGray
        val textWeight = if (status == StepStatus.IN_PROGRESS) FontWeight.SemiBold else FontWeight.Normal

        Text(
            text = label,
            fontSize = 15.sp,
            color = textColor,
            fontWeight = textWeight
        )
    }
}

@Preview
@Composable
fun LoadingScreenPreview() {
    LoadingScreen()
}