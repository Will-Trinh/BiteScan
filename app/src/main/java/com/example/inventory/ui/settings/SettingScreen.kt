package com.example.inventory.ui.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.inventory.InventoryApplication
import com.example.inventory.ui.AppViewModel
import com.example.inventory.ui.navigation.BottomNavigationBar
import com.example.inventory.ui.theme.CookingAssistantTheme
import com.example.inventory.ui.theme.PrimaryGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.inventory.data.OnlineUsersRepository

data class SettingNav(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

// Single-line nav row with leading icon + trailing lead
@Composable
fun SettingNavRow(
    item: SettingNav,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable(onClick = item.onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(item.icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text(item.label, fontSize = 16.sp)
        }
        Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(24.dp))
    }
    Spacer(Modifier.height(8.dp))
}

// Toggle row (checkbox on the right)
@Composable
fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        Text(label, fontSize = 16.sp)
        Spacer(Modifier.weight(1f))
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = 8.dp),
            colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen)
        )
    }
}

@Composable
fun ProfileCard(
    userIdText: String?,
    userName: String,
    userEmail: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text(
            text = "Profile",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryGreen)
            ) {
                Text(
                    text = userIdText ?: "—",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.width(16.dp))
            Column {
                Text(userName, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                Text(userEmail, fontSize = 16.sp, color = Color.Gray)
            }
        }
    }
}


@Composable
fun CheckboxWithLabel(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label, fontSize = 16.sp)
        Spacer(modifier = Modifier.weight(1f))
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = 8.dp),
            colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen)
        )
    }
}

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFDE0E0))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Delete Account",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Are you sure you want to delete this account?\nThis action cannot be undone.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onConfirmDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", fontSize = 16.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, Color.LightGray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", fontSize = 16.sp, color = Color.Black)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel
) {
    val scope = rememberCoroutineScope()
    val userId = appViewModel.userId.value
    val context = LocalContext.current
    val appContainer = if (context.applicationContext is InventoryApplication) {
        (context.applicationContext as InventoryApplication).container
    } else {
        null // Preview mode
    }

    // ViewModel wiring (same behavior as before)
    val viewModel = remember {
        SettingsViewModel(
            repository = appContainer?.usersRepository!!,
            onlineUserRepository = appContainer.onlineUsersRepository!!,
            appViewModel = appViewModel
        )
    }

    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val diet by viewModel.diet.collectAsState()


    val isLoggedOut by viewModel.logoutCompleted.collectAsState()
    LaunchedEffect(userId) { viewModel.setCurrentUserId(userId?:0) }
    val userIdText = viewModel.userId.collectAsState().value

    var showExternalLinkDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    // Local toggles (kept from original file)
    var vegetarian by remember { mutableStateOf(false) }
    var vegan by remember { mutableStateOf(false) }
    var glutenFree by remember { mutableStateOf(false) }
    var lowCarb by remember { mutableStateOf(false) }
    var pushNotifications by remember { mutableStateOf(true) }

    // Navigate away on logout (unchanged)
    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
            android.util.Log.d("Log Out", "Logged out successfully")
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_LONG).show()
        }
    }

    CookingAssistantTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) { Text("Settings", fontWeight = FontWeight.Bold) }
                    },
                )
            },
            bottomBar = { BottomNavigationBar(navController, appViewModel) }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Profile
                ProfileCard(
                    userIdText = userIdText,
                    userName = userName ?: "Loading...",
                    userEmail = userEmail ?: "unknown@example.com"
                )


                // Dietary Preferences
                SectionCard(title = "Dietary Preferences") {
                    val selectedDiet by viewModel.selectedDiet.collectAsState()

                    val diets = listOf("None", "Vegetarian", "Vegan", "Gluten-Free", "Low Carb")

                    diets.forEach { diet ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable { viewModel.selectDiet(diet) }
                                .padding(horizontal = 16.dp)
                        ) {
                            RadioButton(
                                selected = (selectedDiet == diet) || (diet == "None" && selectedDiet == null),
                                onClick = null
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(diet, fontSize = 16.sp)
                        }
                    }
                }

                // App Settings
                SectionCard(title = "App Settings") {
                    ToggleRow(
                        label = "Push Notifications",
                        checked = pushNotifications,
                        onCheckedChange = { pushNotifications = it }
                    )
                }

                // Other Settings (data-driven)
                SectionCard(title = "Other Settings") {
                    val items = listOf(
                        SettingNav("Update my information", Icons.Default.Edit) {
                            navController.navigate("update_information")
                        },
                        SettingNav("History", Icons.Default.Receipt) {
                            navController.navigate("history")
                        },
                        SettingNav("My Pantry", Icons.Default.Kitchen) {
                            navController.navigate("my_pantry")
                        },
                        SettingNav("Like us on Facebook", Icons.Default.ThumbUp) {
                            showExternalLinkDialog = true
                        },
                        SettingNav("Legals", Icons.Default.Handshake) {
                            navController.navigate("legal")
                        },
                        SettingNav("About\n1.0.0", Icons.Default.Info) {
                            navController.navigate("about")
                        }
                    )
                    items.forEach { SettingNavRow(it) }

                    if (showExternalLinkDialog) {
                        AlertDialog(
                            onDismissRequest = { showExternalLinkDialog = false },
                            title = { Text("External Link") },
                            text = { Text("This will take you to an external website in your browser. Do you want to continue?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showExternalLinkDialog = false
                                        viewModel.launchUrl(
                                            context,
                                            "https://www.facebook.com/people/BiteScan/61581305012013/"
                                        )
                                    }
                                ) { Text("Yes", color = Color.Black, fontWeight = FontWeight.Bold) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showExternalLinkDialog = false }) {
                                    Text("Cancel", color = Color.Red)
                                }
                            }
                        )
                    }
                }

                // Privacy & Data
                SectionCard(title = "Privacy & Data") {
                    Button(
                        onClick = { showDeleteAccountDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.4.dp, Color.Gray)
                    ) {
                        Text(
                            "Delete Account",
                            color = Color.Red,
                            fontSize = 16.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (showDeleteAccountDialog) {
                        DeleteAccountDialog(
                            onDismiss = { showDeleteAccountDialog = false },
                            onConfirmDelete = {
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val apiUrl = URL("http://129.146.23.142:8080/users/$userId")
                                        val conn = apiUrl.openConnection() as HttpURLConnection
                                        //Set timeout (10s connect, 15s response)
                                        conn.connectTimeout = 10_000
                                        conn.readTimeout = 15_000
                                        conn.requestMethod = "DELETE"
                                        conn.setRequestProperty("Content-Type", "application/json")
                                        conn.doOutput = true
                                        val responseCode = conn.responseCode
                                        println(responseCode)
                                    } catch (e: Exception) {
                                        println(e)
                                    }
                                }
                                showDeleteAccountDialog = false
                                Toast.makeText(
                                    context,
                                    "Account deletion initiated (Placeholder)",
                                    Toast.LENGTH_LONG
                                ).show()
                                navController.navigate("login")
                            }
                        )
                    }

                    Button(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.4.dp, Color.Gray)
                    ) {
                        Text(
                            "Log Out",
                            color = Color.Red,
                            fontSize = 16.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SettingScreenPreview() {
    val navController = rememberNavController()
    CookingAssistantTheme {
        SettingScreen(
            navController = navController,
            appViewModel = AppViewModel()
        )
    }
}
