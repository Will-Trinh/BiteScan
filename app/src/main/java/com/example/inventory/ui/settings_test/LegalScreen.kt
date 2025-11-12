package com.example.inventory.ui.settings_test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inventory.ui.theme.CookingAssistantTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.filled.ArrowBack
import com.example.inventory.ui.AppViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(navController: NavController, appViewModel: AppViewModel, userId: Int, modifier: Modifier = Modifier, ) {
    CookingAssistantTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 48.dp),
                            horizontalArrangement = Arrangement.Center
                        ) { Text("Legals", fontWeight =FontWeight.Bold) }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            // Wrap Column with verticalScroll for scrolling
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()) // Enable vertical scrolling
            ) {
                Text(
                    text = "Legal Information",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Last Updated: September 22, 2025",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Terms of Use
                Text(
                    text = "Terms of Use",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                Text(
                    text = "Welcome to the app BiteScan provided by Team TasteMates. " +
                            "By using this App, you agree to comply with the following terms:\n" +
                            "-Eligibility: You must be at least 13 years old to use this App.\n" +
                            "-License: We grant you a non-exclusive, non-transferable license to use the App for personal, non-commercial purposes.\n" +
                            "-User Conduct: You agree not to use the App for illegal activities, to upload malicious content, or to infringe on others' intellectual property.\n" +
                            "-Termination: We reserve the right to terminate or suspend your access to the App at our discretion if you violate these terms.\n" +
                            "-Modifications: We may update these terms at any time, and continued use of the App constitutes acceptance of the revised terms.",
                    fontSize = 16.sp
                )

                // Privacy Policy
                Text(
                    text = "Privacy Policy",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                Text(
                    text = "We value your privacy and are committed to protecting your personal information. This policy outlines our practices:" +
                            "\n-Data Collection: We collect user IDs and optionally receipt or inventory data you input into the App to provide personalized features.\n" +
                            "-Data Usage: Your data is used to enhance app functionality (e.g., recipe suggestions) and is not shared with third parties except as required by law.\n" +
                            "-Data Storage: Data is stored securely on your device and, if synced, on our servers with encryption.\n" +
                            "-Cookies and Tracking: The App may use local storage to save preferences but does not employ third-party tracking unless explicitly stated.\n" +
                            "-Your Rights: You can request access to or deletion of your data by contacting us at [bitescansupport@gmail.com].\n" +
                            "-Changes: We may update this policy, and notifications will be provided in the App.",
                    fontSize = 16.sp
                )

                // Disclaimer
                Text(
                    text = "Disclaimer",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                Text(
                    text = "The Cooking Assistant app is provided \"as is\" without warranties of any kind, either express or implied. " +
                            "We do not guarantee the accuracy, completeness, or reliability of recipe suggestions or inventory data. " +
                            "Your use of the App is at your own risk. We are not liable for any indirect, incidental, or consequential damages arising from your use of the App.",
                    fontSize = 16.sp
                )

                // Contact Us
                Text(
                    text = "Contact Us",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                Text(
                    text = "If you have questions about these legal terms, please contact us at:\n" +
                            "- Email: bitescansupport@gmail.com",
                    fontSize = 16.sp
                )

                // Copyright
                Text(
                    text = "Copyright",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                Text(
                    text = "©2025 BiteScan. All rights reserved. The content, design, and code of this App are protected by copyright law.",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LegalScreenPreview() {
    val navController = rememberNavController()
    CookingAssistantTheme {
        LegalScreen(navController = navController, userId = 1, appViewModel = AppViewModel())
    }
}