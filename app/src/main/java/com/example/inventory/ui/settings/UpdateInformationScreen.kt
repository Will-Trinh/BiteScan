package com.example.inventory.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inventory.ui.theme.CookingAssistantTheme
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.navigation.BottomNavigationBar
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import com.example.inventory.InventoryApplication
import androidx.compose.ui.platform.LocalContext
import com.example.inventory.data.OfflineUsersRepository
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.inventory.ui.AppViewModel
import com.example.inventory.ui.theme.PrimaryGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateInformationScreen(
    navController: NavController,
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val userId = appViewModel.userId.value
    val context = LocalContext.current
    val appContainer = (context.applicationContext as InventoryApplication).container
    val viewModel = remember {
        UpdateInformationViewModel(
            repository = appContainer.usersRepository as OfflineUsersRepository
        )
    }
    LaunchedEffect(userId) {
        viewModel.loadUser(userId?:0)
    }
    val uiState by viewModel.uiState.collectAsState()

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
                        ) { Text("Update my Information") }
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
            },
            bottomBar = { BottomNavigationBar(navController,appViewModel) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .then(modifier),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hi ${uiState.userName.ifEmpty { "user" }}!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    modifier = Modifier.padding(top = 32.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = uiState.userName,
                    onValueChange = { viewModel.updateUiState(userName = it) },
                    label = { Text("User Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )


                OutlinedTextField(
                    value = uiState.currentPassword,
                    onValueChange = { viewModel.updateUiState(currentPassword = it) },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )

                OutlinedTextField(
                    value = uiState.newPassword,
                    onValueChange = { viewModel.updateUiState(newPassword = it) },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )

                OutlinedTextField(
                    value = uiState.retypePassword,
                    onValueChange = { viewModel.updateUiState(retypePassword = it) },
                    label = { Text("Re-type New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )


                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        viewModel.updateUserInfo(
                            userName = uiState.userName,
                            currentPassword = uiState.currentPassword,
                            newPassword = uiState.newPassword,
                            retypePassword = uiState.retypePassword
                        )
                        scope.launch(Dispatchers.IO) {
                            try {
                                val apiUrl = URL("http://129.146.23.142:8080/users/$userId")
                                val conn = apiUrl.openConnection() as HttpURLConnection
                                //Set timeout (10s connect, 15s response)
                                conn.connectTimeout = 10_000
                                conn.readTimeout = 15_000
                                conn.requestMethod = "PATCH"
                                conn.setRequestProperty("Content-Type", "application/json")
                                conn.doOutput = true
                                val json = JSONObject().apply {
                                    // this technically shouldnt be here, bnut im lazy, remove email later
                                    put("email", "_@gmail.com")
                                    put("password", uiState.newPassword)
                                    put("username", uiState.userName)
                                }
                                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                                val responseCode = conn.responseCode
                                println(responseCode)
                            } catch (e: Exception) {
                                println(e)
                            }
                        }
                        navController.popBackStack() // Return to Settings after update
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Update", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateInformationScreenPreview() {
    val navController = rememberNavController()
    CookingAssistantTheme {
        UpdateInformationScreen(navController = navController, appViewModel = AppViewModel())
    }
}