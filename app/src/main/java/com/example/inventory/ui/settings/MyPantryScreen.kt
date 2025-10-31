package com.example.inventory.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inventory.ui.theme.CookingAssistantTheme
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.navigation.BottomNavigationBar
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import com.example.inventory.InventoryApplication
import androidx.compose.foundation.lazy.LazyColumn
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.inventory.ui.userdata.FakeItemsRepository
import com.example.inventory.ui.userdata.FakeReceiptsRepository
import com.example.inventory.ui.userdata.fakeUIuser
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.inventory.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPantryScreen(
    navController: NavController,
    userId: Int,
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    // Custom ViewModel factory to provide dependencies
    val viewModel: MyPantryViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MyPantryViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return MyPantryViewModel(
                        itemsRepository = FakeItemsRepository(),
                        receiptsRepository = FakeReceiptsRepository()
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val pantryItems = uiState.pantryItems

    val activeCount = pantryItems.filter { it.daysLeft > 0 }.size

    val tabs = listOf("All", "Expiring Soon", "Expired")
    var selectedTab by remember { mutableStateOf("All") }

    var selectedItem by remember { mutableStateOf<PantryItem?>(null) }

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
                        ) { Text("My Pantry", fontWeight =FontWeight.Bold) }
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
            floatingActionButton = {
                FloatingActionButton(onClick = { selectedItem = PantryItem(0, "", "", "", SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date()), "", "", "", "", 0) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text("$activeCount active items", fontSize = 16.sp, color = Color(0xFF4CAF50))

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tabs.forEach { tab ->
                        Button(
                            onClick = { selectedTab = tab },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTab == tab) Color(0xFF4CAF50) else Color.White,
                                contentColor = if (selectedTab == tab) Color.White else Color.Gray
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("$tab (${getCount(tab, pantryItems)})")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    items(items = getFilteredItems(selectedTab, pantryItems)) { item ->
                        PantryItemCard(
                            item = item,
                            onEdit = { selectedItem = item },
                            onDelete = { viewModel.deletePantryItem(item.id) }
                        )
                    }
                }
            }

            selectedItem?.let { item ->
                AlertDialog(
                    onDismissRequest = { selectedItem = null },
                    title = { Text(if (item.id == 0) "Add Item" else "Edit Item") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = item.name,
                                onValueChange = { selectedItem = item.copy(name = it) },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = item.quantity,
                                onValueChange = { selectedItem = item.copy(quantity = it) },
                                label = { Text("Quantity (e.g., 6 pieces)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = item.unitPrice,
                                onValueChange = { selectedItem = item.copy(unitPrice = it) },
                                label = { Text("Unit Price") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = item.purchaseDate,
                                onValueChange = { selectedItem = item.copy(purchaseDate = it) },
                                label = { Text("Purchase Date (MM/DD/YYYY)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = item.store,
                                onValueChange = { selectedItem = item.copy(store = it) },
                                label = { Text("Store") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = item.category,
                                onValueChange = { selectedItem = item.copy(category = it) },
                                label = { Text("Category") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.addOrUpdatePantryItem(item); selectedItem = null }) {
                            Text("Save", color = Color.Black)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedItem = null }) {
                            Text("Cancel", color = Color.Black)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PantryItemCard(item: PantryItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (item.daysLeft in 1..3) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Expiring Soon",
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier
                            .background(Color(0xFFFFA500), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { onEdit(); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { onDelete(); showMenu = false }
                    )
                }
            }
            Text(
                text = "${item.quantity} • ${item.store}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            val daysText = if (item.daysLeft > 0) "${item.daysLeft} days left" else "Expired"
            val daysColor = when {
                item.daysLeft <= 0 -> Color.Red
                item.daysLeft <= 3 -> Color(0xFFFFA500)
                else -> Color(0xFF4CAF50)
            }
            Text(
                text = daysText,
                fontSize = 14.sp,
                color = daysColor
            )
        }
    }
}

private fun getCount(tab: String, items: List<PantryItem>): Int {
    return getFilteredItems(tab, items).size
}

private fun getFilteredItems(tab: String, items: List<PantryItem>): List<PantryItem> {
    return when (tab) {
        "All" -> items.filter { it.daysLeft > 0 }
        "Expiring Soon" -> items.filter { it.daysLeft in 1..3 }
        "Expired" -> items.filter { it.daysLeft <= 0 }
        else -> emptyList()
    }
}

@Preview(showBackground = true)
@Composable
fun MyPantryScreenPreview() {
    val navController = rememberNavController()
    val fakeViewModel = MyPantryViewModel(
        itemsRepository = FakeItemsRepository(),
        receiptsRepository = FakeReceiptsRepository()
    )
    // Simulate loading some sample data
    fakeViewModel.setUserId(1) // Mock userId

    CookingAssistantTheme {
        MyPantryScreen(
            navController = navController,
            userId = 1,
            modifier = Modifier.fillMaxSize(),
            appViewModel= AppViewModel(),
        )
    }
}