package com.example.inventory.ui.upload

import android.Manifest
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.ui.theme.CookingAssistantTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.inventory.R
import com.example.inventory.ui.navigation.BottomNavigationBar
import com.example.inventory.ui.navigation.NavigationDestination
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.material.icons.Icons
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.draw.shadow

object UploadDestination : NavigationDestination {
    override val route = "upload"
    override val titleRes = R.string.upload
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    navController: NavController
) {
    val viewModel: HomeViewModel = viewModel()
    val extractedItems by viewModel.extractedItems.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    // Register gallery launcher unconditionally
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let {
                val inputStream = navController.context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                bitmap?.let { viewModel.processReceiptImage(it) }
            }
        }
    )

    // Register camera launcher unconditionally
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let { viewModel.processReceiptImage(it) }
    }
    // Register camera permission launcher unconditionally
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null) // Launch camera if permission granted
        } else {
            // Handle permission denied (e.g., show a snackbar or dialog)
        }
    }
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
                        ) { Text("") }
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
            bottomBar = { BottomNavigationBar(navController) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),

                ) {
                // Upload Area
                UploadArea(galleryLauncher, cameraPermissionLauncher, isProcessing)

                // Display extracted items
                if (extractedItems.isNotEmpty()) {
                    Text(
                        text = "Extracted Items from Receipt:",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                    extractedItems.forEach { item ->
                        Text("- $item", modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
                    }
                }

            }
        }
    }
}

@Composable
fun UploadArea(
    galleryLauncher: androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>,
    cameraPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    isProcessing: Boolean
) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Upload Receipt",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Take a photo or choose an existing image",
                fontSize = 18.sp,
                color = Color.Gray,
                modifier = Modifier.padding(10.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .shadow(8.dp, RoundedCornerShape(8.dp)), // Adds shadow effect
                shape = RoundedCornerShape(8.dp),
                elevation = ButtonDefaults.buttonElevation(4.dp), // Enhances button elevation
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Take Photo",
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take Photo", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    galleryLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                },
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .shadow(8.dp, RoundedCornerShape(8.dp)), // Adds shadow effect
                shape = RoundedCornerShape(8.dp),
                elevation = ButtonDefaults.buttonElevation(4.dp), // Enhances button elevation
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = "Choose File",
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Choose File", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Supported: JPG, PNG",
                fontSize = 16.sp,
                color = Color.Gray
            )
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
        }

}

// Preview cho HomeScreen
@Preview(showBackground = true)
@Composable
fun UploadScreenPreview() {
    val navController = rememberNavController() // Mock NavController
    CookingAssistantTheme {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) { paddingValues ->
            UploadScreen(paddingValues, navController)
        }
    }
}

// Preview cho UploadArea
@Preview(showBackground = true)
@Composable
fun UploadAreaPreview() {
    CookingAssistantTheme {
        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = {}
        )
        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = {}
        )
        val isProcessing = false

        UploadArea(
            galleryLauncher = galleryLauncher,
            cameraPermissionLauncher = cameraPermissionLauncher,
            isProcessing = isProcessing,

        )
    }
}

