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
import com.example.inventory.ui.navigation.BottomNavigationBar
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.material.icons.Icons
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import com.example.inventory.InventoryApplication
import com.example.inventory.data.ReceiptsRepository
import com.example.inventory.data.ItemsRepository
import com.example.inventory.ui.loading.LoadingScreen
import android.util.Log
import com.example.inventory.ui.theme.md_theme_light_primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    navController: NavController,
    userId: Int = 1  // From auth/nav args
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as InventoryApplication).container
    val viewModel: UploadViewModel = viewModel(
        factory = UploadViewModelFactory(
            receiptsRepository = appContainer.receiptsRepository,
            itemsRepository = appContainer.itemsRepository
        )
    )
    val ocrState by viewModel.ocrState.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    // Register gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                bitmap?.let { viewModel.processReceiptImage(it) }
            }
        }
    )

    // Register camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let { viewModel.processReceiptImage(it) }
    }

    // Register camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            // Handle permission denied
        }
    }

    // Auto-save and navigate on OCR success
    // In UploadScreen.kt, update the LaunchedEffect:
    LaunchedEffect(ocrState) {
        val currentState = ocrState  // Extract to local for smart cast
        if (currentState is OcrState.Success) {
            try {
                val newReceiptId = viewModel.saveReceiptAndItems(currentState.receiptData, userId)
                navController.navigate("edit_receipt/$newReceiptId/$userId") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            } catch (e: Exception) {
                Log.e("UploadScreen", "Save failed: ${e.message}")
                // Show Snackbar or error dialog
            }
        }
    }

    CookingAssistantTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Row(modifier = Modifier.fillMaxWidth().padding(end = 48.dp), horizontalArrangement = Arrangement.Center) { Text("") } },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            bottomBar = { BottomNavigationBar(navController) }
        ) { scaffoldPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    UploadArea(
                        galleryLauncher = galleryLauncher,
                        cameraPermissionLauncher = cameraPermissionLauncher,
                        isProcessing = isProcessing
                    )

                    // Brief OCR results preview (before nav; optional)
                    if (ocrState is OcrState.Success && !isProcessing) {
                        OcrResultsSection(receiptData = (ocrState as OcrState.Success).receiptData)
                    } else if (ocrState is OcrState.Error && !isProcessing) {
                        ErrorSection(message = (ocrState as OcrState.Error).message)
                    }
                }

                // Loading Overlay
                if (isProcessing) {
                    LoadingScreen()
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
                tint = md_theme_light_primary
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
                tint = md_theme_light_primary

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

@Composable
fun OcrResultsSection(receiptData: ReceiptData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "OCR Results (Saving...)", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            // ... (keep existing JSON/line items display)
        }
    }
}

@Composable
fun ErrorSection(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Error", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Red)
            Text(text = message, fontSize = 14.sp, color = Color.Red)
        }
    }
}

// Previews (keep existing)
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
