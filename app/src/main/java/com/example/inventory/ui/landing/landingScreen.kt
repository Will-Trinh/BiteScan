package com.example.inventory.ui.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.ui.theme.CookingAssistantTheme
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.inventory.R

// Define a color for the primary green, consistent with your settings screen
val PrimaryGreen = Color(0xFF4CAF50)
val BackgroundGray = Color(0xFFEEEEEE) // Light gray for the outer background

@Composable
fun LandingScreen(
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Mimic the overall gray background and the rounded-corner content area
    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray) // Outer gray background
            .padding(16.dp), // Padding around the main content card
        color = BackgroundGray // Set Surface color to match
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(32.dp)) // Large corner radius for the main content box
                .background(Color.White)
                .padding(top = 16.dp), // Top padding for the content inside the white box
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // BiteScan Logo Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top=80.dp)
            ) {
                // Leaf Icon (Replace with your actual Image/Logo)
                Icon(
                    imageVector = Icons.Default.Eco, // Placeholder leaf icon
                    contentDescription = "BiteScan Logo",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BiteScan",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }

            // Image Placeholder Section (Receipt and Phone)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f), // Slightly smaller width
                contentAlignment = Alignment.Center,

            ) {
                Image(painter = painterResource(id = R.drawable.your_receipt_and_phone_image),
                    contentDescription = "Receipt and Phone Placeholder",
                    modifier = Modifier.size(300.dp)
                    )
            }

            // Main Title
            Text(
                text = buildAnnotatedString {
                    append("Track nutrition\nfrom your ")
                    withStyle(style = SpanStyle(color = PrimaryGreen)) {
                        append("receipts")
                    }
                },
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 40.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle/Description
            Text(
                text = "Snap a photo of your grocery receipt and get instant nutrition insights and spending analysis",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 48.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Get Started Button
            Button(
                onClick = onGetStartedClick,
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Controls the button width
                    .height(40.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp), clip = true), // Added shadow for lift
                shape = RoundedCornerShape(28.dp), // Pill-shaped button
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                contentPadding = PaddingValues(horizontal = 24.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 60.dp)
                ) {
                    Text(
                        text = "Get Started",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        tint = Color.White,
                        contentDescription = "Get Started",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LandingScreenPreview() {
    CookingAssistantTheme {
        LandingScreen(onGetStartedClick = { /* Do nothing for preview */ })
    }
}