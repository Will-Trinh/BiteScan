package com.example.inventory.ui.landing

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.InventoryApplication
import com.example.inventory.ui.theme.CookingAssistantTheme
import com.example.inventory.ui.theme.md_theme_light_primary
import com.example.inventory.ui.theme.*
import com.example.inventory.ui.userdata.FakeUsersRepository
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: (Int) -> Unit = {},
    onCreateAccountClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    appViewModel: AppViewModel,
    navController: NavController,
    viewModel: LoginScreenViewModel? = null
) {

    val context = LocalContext.current
    val actualViewModel = viewModel ?: remember {
        if (context.applicationContext is InventoryApplication) {
            val appContainer = (context.applicationContext as InventoryApplication).container
            LoginScreenViewModel(
                usersRepository = appContainer.usersRepository
            )
        } else {
            throw IllegalStateException("Application context is not an instance of InventoryApplication")
    }
    }

    val loginResult = actualViewModel.loginResult.collectAsState().value
    val isLoading = actualViewModel.isLoading.collectAsState().value


    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginSelected by remember { mutableStateOf(true) }
    var showResetDialog by remember { mutableStateOf(false) }

    val primaryColor = md_theme_light_primary

    CookingAssistantTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

                Row {
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFF5F5F5)),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ToggleButton(
                        text = "Login",
                        isSelected = isLoginSelected,
                        onClick = { isLoginSelected = true },

                        primaryColor = primaryColor
                    )
                    ToggleButton(
                        text = "Create Account",
                        isSelected = !isLoginSelected,
                        onClick = { navController.navigate("register") {
                            launchSingleTop = true}},
                        primaryColor = primaryColor
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))

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
                Spacer(modifier = Modifier.height(32.dp))

                // Added: Login button with loading indicator
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth(0.2f)
                            .height(56.dp),
                        color = primaryColor,
                        strokeWidth = 4.dp
                    )
                } else {
                    Button(
                        onClick = {
                            actualViewModel.checkLogin(email, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Forgot password?",
                    color = primaryColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.None,
                    modifier = Modifier.clickable { showResetDialog = true }
                )
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "By continuing, you agree to BiteScan's Terms & \nPrivacy Policy.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    //Added: Show login result as Toast
    LaunchedEffect(loginResult) {
        loginResult?.let { result ->
            Toast.makeText(
                context,
                if (result.success) "Login success: UID=${result.uid}" else "Login failed: ${result.errorMessage}",
                Toast.LENGTH_LONG
            ).show()
            if (result.success) {
                //save userId to navController
                navController.currentBackStackEntry?.savedStateHandle?.set("userId", result.uid)
                appViewModel.setUserId(result.uid)
                onLoginClick(result.uid)
            }
        }
    }

    if (showResetDialog) {
        PasswordResetDialog(
            primaryColor = primaryColor,
            onDismiss = { showResetDialog = false },
            onConfirm = { email ->
                if (email.isNotBlank()) {
                    // TODO: API reset password
                    Toast.makeText(context, "Check new password in your mail!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Please enter your email!", Toast.LENGTH_SHORT).show()
                }
                showResetDialog = false  // Đóng dialog
            }
        )
    }
}

// --- Password Reset Dialog Composable ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordResetDialog(
    primaryColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var resetEmail by remember { mutableStateOf("") }

    AlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(50))
                        .background(PrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield",
                        tint = primaryColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Reset Password", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter your email to reset your password",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Email",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = resetEmail,
                    onValueChange = { resetEmail = it },
                    placeholder = { Text("Enter your email") },
                    modifier = Modifier
                        .fillMaxWidth()
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

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onConfirm(resetEmail) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("Confirm", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun RowScope.ToggleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.White else Color.Transparent,
            contentColor = if (isSelected) primaryColor else Color.Gray
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val fakeViewModel = LoginScreenViewModel(FakeUsersRepository())

    CookingAssistantTheme {
        LoginScreen(
            viewModel = fakeViewModel,
            navController = rememberNavController(),
            onLoginClick = {},
            onCreateAccountClick = {},
            onBackClick = {},
            appViewModel = AppViewModel()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PasswordResetDialogPreview() {
    PasswordResetDialog(primaryColor = md_theme_light_primary, onDismiss = {}, onConfirm = {})
}
