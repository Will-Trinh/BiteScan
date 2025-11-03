package com.example.inventory.ui.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.theme.CookingAssistantTheme
import com.example.inventory.ui.theme.md_theme_light_primary
import com.example.inventory.ui.AppViewModel



// Define secondary colors
val OutlineGray = Color(0xFFE0E0E0) // Lighter gray for outlines

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    navController: NavController,
    onBackClick: () -> Unit = {} ,
    viewModel: RegistrationViewModel? = null
) {
    var email by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") } // New state for confirmation
    var isLoginSelected by remember { mutableStateOf(false) } // Default to 'Create Account' selected
    val primaryColor = md_theme_light_primary

    CookingAssistantTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White // Use a lighter background color to match the design style
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar with Back Arrow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.DarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Logo and Welcome Text
                Row () {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = "BiteScan Logo",
                        tint = primaryColor,
                        modifier = Modifier.size(50.dp)
                    )
                    Text(
                        text = "BiteScan",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Welcome to BiteScan",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Login / Create Account Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFF5F5F5)), // Changed to OutlineGray for better contrast
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                    content = {
                        ToggleButton(
                            text = "Login",
                            isSelected = isLoginSelected,
                            onClick = { isLoginSelected = true;},
                            primaryColor = primaryColor
                        )
                        ToggleButton(
                            text = "Create Account",
                            isSelected = !isLoginSelected,
                            onClick = { isLoginSelected = false },
                            primaryColor = primaryColor
                        )
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
                // User name input
                Text(
                    text = "User Name",
                    modifier = Modifier.fillMaxWidth(0.9f),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    placeholder = { Text("Enter your User Name") },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(56.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = OutlineGray,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Email Input
                Text(
                    text = "Email",
                    modifier = Modifier.fillMaxWidth(0.9f),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Enter your email") },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(56.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = OutlineGray,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Password Input
                Text(
                    text = "Password",
                    modifier = Modifier.fillMaxWidth(0.9f),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Enter your password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(56.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = OutlineGray,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password Input (NEW FIELD)
                Text(
                    text = "Confirm Password",
                    modifier = Modifier.fillMaxWidth(0.9f),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("Confirm your password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(56.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = OutlineGray,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Create Account Button (Text "Sign Up" to match the screenshot)
                Button(
                    onClick = {
                        if (userName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) return@Button
                        if (password != confirmPassword) return@Button
                        if (password.length < 6) return@Button

                        viewModel?.checkSignUp(userName, email, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("Sign Up", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(32.dp))

                // Terms & Privacy Policy
                Text(
                    text = "By continuing, you agree to BiteScan's Terms & \nPrivacy Policy.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CreateAccountScreenPreview() {

    RegistrationScreen(navController = rememberNavController())
}
