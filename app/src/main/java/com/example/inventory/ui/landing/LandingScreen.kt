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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.inventory.R
import com.example.inventory.ui.theme.PrimaryGreen
import androidx.navigation.NavController
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextAlign

@Composable
fun LandingScreen(
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .verticalScroll(rememberScrollState())     // 👈 make it scrollable
                .padding(top = 16.dp, bottom = 24.dp),     // add bottom padding so button isn't clipped
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Logo and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(top = 40.dp)                  // a bit smaller than 80dp
                    .fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = "BiteScan Logo",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "BiteScan",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }

            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.your_receipt_and_phone_image),
                    contentDescription = "Receipt and Phone Placeholder",
                    modifier = Modifier.size(300.dp)
                )
            }

            // Title text
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
                textAlign = TextAlign.Center,
                lineHeight = 40.sp,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
            )

            // Subtitle
            Text(
                text = "Snap a photo of your grocery receipt and get instant nutrition insights and spending analysis",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 48.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Button
            Button(
                onClick = onGetStartedClick,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        clip = true
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                ) {
                    Text(
                        text = "Get Started",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        tint = Color.White,
                        contentDescription = "Get Started",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}