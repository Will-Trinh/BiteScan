package com.example.inventory.ui.receipt

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.inventory.ui.theme.CookingAssistantTheme
import com.example.inventory.ui.navigation.BottomNavigationBar
import com.example.inventory.R
import com.example.inventory.data.Item
import com.example.inventory.ui.navigation.NavigationDestination
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import java.sql.Date
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.inventory.InventoryApplication
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.userdata.FakeItemsRepository
import com.example.inventory.ui.userdata.FakeReceiptsRepository
import com.example.inventory.ui.userdata.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign

object EditReceiptDestination : NavigationDestination {
    override val route = "edit_receipt"
    const val receiptIdArg = "receiptId"
    override val titleRes = R.string.edit_receipt_title
    val routeWithArgs = "$route/{$receiptIdArg}"
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReceiptScreen(
    receiptId: Int,
    userId: Int,
    navigateUp: () -> Unit,
    navController: NavController,
    viewModel: EditReceiptViewModel? = null
) {
    val context = LocalContext.current
    val actualViewModel = viewModel ?: remember {
        if (context.applicationContext is InventoryApplication) {
            val appContainer = (context.applicationContext as InventoryApplication).container
            EditReceiptViewModel(
                itemsRepository = appContainer.itemsRepository,
                receiptsRepository = appContainer.receiptsRepository
            )
        } else {
            // Fallback for preview environment
            EditReceiptViewModel(
                itemsRepository = FakeItemsRepository(),
                receiptsRepository = FakeReceiptsRepository()
            )
        }
    }

    LaunchedEffect(receiptId) {
        actualViewModel.loadItems(receiptId)
        actualViewModel.loadReceipt(receiptId)
//        actualViewModel.loadDraftFromApi("2")
    }
    val editUiState by actualViewModel.editUiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    CookingAssistantTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Review & Edit Items", //&&&&& $receiptId",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = navigateUp) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },

            bottomBar = { BottomNavigationBar(navController) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = " Verify items before continuing",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light
                    )
                }

                // Todo: Add Top card for Source + total price + total Items Count
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = editUiState.receipt?.source ?: "Unknown Source",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Total Price: $${
                                    String.format(
                                        "%.2f",
                                        editUiState.totalPrice
                                    )
                                }",
                                fontSize = 14.sp,
                                color = Color(0xFF4CAF50), // Green color similar to the image
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // Total price and items
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = actualViewModel.receiveDate(),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${editUiState.totalItems} Items",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Todo: "Receipt items list"
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(editUiState.itemList) { index, item ->
                        ItemCard(
                            item = item,
                            onItemChange = { updatedItem ->
                                actualViewModel.updateItem(
                                    index,
                                    updatedItem
                                )
                            }
                        )
                    }
                }
                //Todo: When user swipe left on the item card, it will be deleted
                // .... Under Construction - delay to next sprint


                //Todo: "Bottom buttons"
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //Todo: "Missing Item"
                    OutlinedButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.padding(horizontal = 1.dp).fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF4CAF50)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(true)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Missing Item")
                    }

                    // Spacer
                    Spacer(modifier = Modifier.width(8.dp))

                    //Todo: "Confirm & Analyze Nutrition"
                    Button(
                        onClick = {
                            val receipt = editUiState.receipt
                            if (receipt != null) {
                                actualViewModel.processItems()
                                actualViewModel.viewModelScope.launch {
                                    actualViewModel.saveReceipt(
                                        receipt.copy(status = "Completed")
                                    )
                                }
                                navController.navigate("dashboard/$userId") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 1.dp).fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50), // Green color for the button
                            contentColor = Color.White
                        ),
                        enabled = editUiState.receipt != null
                    ) {
                        Text("Confirm & Analyze Nutrition")
                    }
                }
            }

            //Todo: "Popup for Missing Items"
            if (showAddDialog) {
                AddItemDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { newName, newPrice, newQuantity ->
                        val newItem = Item(
                            id = 0,
                            name = newName,
                            price = newPrice,
                            quantity = newQuantity,
                            date = Date(System.currentTimeMillis()),
                            store = editUiState.receipt?.source ?: "",
                            category = "",
                            receiptId = receiptId
                        )
                        actualViewModel.viewModelScope.launch {
//                            actualViewModel.addItem(receiptId, newItem)
//                            actualViewModel.addItem(receiptId,newItem)
                            actualViewModel.addItem(newItem)
                        }
                        showAddDialog = false
                    }
                )
            }
        }

    }
}


@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Float) -> Unit
) {
    var newName by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }
    var newQuantity by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Name") })
                OutlinedTextField(
                    value = newPrice,
                    onValueChange = { newPrice = it },
                    label = { Text("Price") })
                OutlinedTextField(
                    value = newQuantity,
                    onValueChange = { newQuantity = it },
                    label = { Text("Quantity") })
            }
        },
        confirmButton = {
            Button(onClick = {
                if (newName.isNotEmpty() && newPrice.toDoubleOrNull() != null && newQuantity.toFloatOrNull() != null) {
                    val priceDouble = newPrice.toDoubleOrNull() ?: 0.0
                    val quantityFloat = newQuantity.toFloatOrNull() ?: 0f
                    onConfirm(newName, priceDouble, quantityFloat)
                } else {

                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@SuppressLint("DefaultLocale")
@Composable
fun ItemCard(
    item: Item,
    onItemChange: (Item) -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var price by remember { mutableStateOf(item.price.toString()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            //
            Text(
                text = "$${String.format("%.2f", price.toDoubleOrNull() ?: item.price)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
        //Todo: editable fields
//        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
//            OutlinedTextField(
//                value = name,
//                onValueChange = {
//                    name = it
//                    onItemChange(item.copy(name = it))
//                },
//                label = { Text("Name") },
//                modifier = Modifier.fillMaxWidth()
//            )
//            OutlinedTextField(
//                value = price,
//                onValueChange = {
//                    price = it
//                    val newPrice = it.toDoubleOrNull() ?: item.price
//                    onItemChange(item.copy(price = newPrice))
//                },
//                label = { Text("Price") },
//                modifier = Modifier.fillMaxWidth()
//            )
//        }
    }
}


@Preview(showBackground = true)
@Composable
fun EditReceiptScreenPreview() {
    val navController = rememberNavController()
    val fakeViewModel = EditReceiptViewModel(
        itemsRepository = FakeItemsRepository(),
        receiptsRepository = FakeReceiptsRepository()
    )
//    fakeViewModel.loadItems(1)
//    fakeViewModel.loadReceipt(1)
    fakeViewModel.loadDraftFromApi("2")
    CookingAssistantTheme {
        EditReceiptScreen(
            receiptId = 1,
            userId = fakeUIuser.userId,
            navigateUp = {},
            navController = navController,
            viewModel = fakeViewModel
        )
    }
}

@Preview
@Composable
fun ItemCardPreview() {
    CookingAssistantTheme {
        ItemCard(
            item = Item(
                1,
                "Apple",
                0.5,
                2f,
                Date(System.currentTimeMillis()),
                "Walmart",
                "Fruit",
                1
            ),
            onItemChange = {}
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AddItemDialogPreview() {
    CookingAssistantTheme {
        AddItemDialog(onDismiss = {}, onConfirm = { _, _, _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun ItemsCardPreview() {
    CookingAssistantTheme {
        LazyColumn {
            itemsIndexed(
                listOf(
                    Item(
                        1,
                        "Apple",
                        0.5,
                        2f,
                        Date(System.currentTimeMillis()),
                        "Walmart",
                        "Fruit",
                        1
                    ),
                    Item(
                        2,
                        "Banana",
                        0.3,
                        3f,
                        Date(System.currentTimeMillis()),
                        "Walmart",
                        "Fruit",
                        1
                    ),
                    Item(
                        3,
                        "Milk",
                        2.99,
                        1f,
                        Date(System.currentTimeMillis()),
                        "Walmart",
                        "Dairy",
                        1
                    )
                )
            ) { index, item ->
                ItemCard(
                    item = item,
                    onItemChange = {}
                )
            }
        }
    }
}


