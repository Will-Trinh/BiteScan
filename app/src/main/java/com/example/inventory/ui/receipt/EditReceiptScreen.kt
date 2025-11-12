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
import com.example.inventory.data.Item
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
import androidx.compose.foundation.clickable
import com.example.inventory.ui.AppViewModel
import com.example.inventory.ui.theme.PrimaryGreen

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReceiptScreen(
    receiptId: Int,
    navigateUp: () -> Unit,
    navController: NavController,
    appViewModel: AppViewModel,
    viewModel: EditReceiptViewModel? = null
) {
    val userId = appViewModel.userId.value
    var deleteItemList by remember { mutableStateOf<List<Item>?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedItemIndex by remember { mutableStateOf(-1) }
    var selectedItem by remember { mutableStateOf<Item?>(null) }
    val context = LocalContext.current
    val actualViewModel = viewModel ?: remember {
        if (context.applicationContext is InventoryApplication) {
            val appContainer = (context.applicationContext as InventoryApplication).container
            EditReceiptViewModel(
                itemsRepository = appContainer.itemsRepository,
                receiptsRepository = appContainer.receiptsRepository,
                onlineReceiptsRepository= appContainer.onlineReceiptsRepository
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
        actualViewModel.loadReceipt(receiptId)
        actualViewModel.loadItems(receiptId)
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

            bottomBar = { BottomNavigationBar(navController, appViewModel) }
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
                                color = PrimaryGreen,
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
                            onClick = {
                                selectedItemIndex = index
                                selectedItem = item
                                showEditDialog = true
                            }
                        )
                    }
                }

                if (showEditDialog && selectedItem != null) {
                    EditOrDeleteItemDialog(
                        item = selectedItem!!,
                        onDismiss = { showEditDialog = false },
                        onUpdate = { updatedItem ->
                            actualViewModel.updateItem(selectedItemIndex, updatedItem)
                            showEditDialog = false
                        },
                        onDelete = {
                            actualViewModel.deleteItem(selectedItemIndex)
                            showEditDialog = false
                        }
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //Todo: "Missing Item"
                    OutlinedButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .padding(horizontal = 1.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryGreen
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

                    //Todo: Add delete button for receipt
                    Button(
                        onClick = {
                            val receipt = editUiState.receipt
                            if (receipt != null) {
                                actualViewModel.processItems()
                                actualViewModel.viewModelScope.launch {
                                    actualViewModel.deleteReceipt(
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
                        modifier = Modifier
                            .padding(horizontal = 1.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            contentColor = Color.White
                        ),
                        enabled = editUiState.receipt != null
                    ) {
                        Text("Delete Receipt")
                    }

                    //Todo: "Confirm & Analyze Nutrition"
                    Button(
                        onClick = {
                            val receipt = editUiState.receipt
                            if (receipt != null) {
                                actualViewModel.viewModelScope.launch {
                                    actualViewModel.saveUpdatedItems(
                                        receipt.copy(status = "Completed")
                                    )
                                    actualViewModel.processItems()
                                }
                                navController.navigate("history/$userId") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 1.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
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
                            actualViewModel.addItem(newItem, receiptId)
                        }
                        showAddDialog = false
                    }
                )
            }
        }

    }
}

@Composable
fun EditOrDeleteItemDialog(
    item: Item,
    onDismiss: () -> Unit,
    onUpdate: (Item) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var price by remember { mutableStateOf(item.price.toString()) }
    var quantity by remember { mutableStateOf(item.quantity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit or Delete Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row {
                Button(
                    onClick = {
                        val updatedItem = item.copy(
                            name = name,
                            price = price.toDoubleOrNull() ?: item.price,
                            quantity = quantity.toFloatOrNull() ?: item.quantity
                        )
                        onUpdate(updatedItem)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Update")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
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
            Column {
                Text(item.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text("Qty: ${item.quantity}", fontSize = 14.sp, color = Color.Gray)
            }
            Text(
                "$${String.format("%.2f", item.price)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
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
    fakeViewModel.loadReceipt(1)
    fakeViewModel.loadItems(1)
    CookingAssistantTheme {
        EditReceiptScreen(
            receiptId = 1,
            navigateUp = {},
            navController = navController,
            viewModel = fakeViewModel,
            appViewModel = AppViewModel()
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
            onClick = {}
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
                    onClick = {}
                )
            }
        }
    }
}


