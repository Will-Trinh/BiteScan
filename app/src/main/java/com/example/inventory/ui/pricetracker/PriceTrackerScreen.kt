package com.example.inventory.ui.pricetracker

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.inventory.InventoryApplication
import com.example.inventory.R
import com.example.inventory.ui.AppViewModel
import com.example.inventory.ui.navigation.BottomNavigationBar
import com.example.inventory.ui.theme.CookingAssistantTheme
import com.example.inventory.ui.theme.LightGreen
import com.example.inventory.ui.theme.PrimaryGreen
import com.example.inventory.ui.userdata.FakeItemsRepository
import com.example.inventory.ui.userdata.FakeReceiptsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceTrackerScreen(
    navController: NavController,
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier,
    viewModel: PriceTrackerViewModel? = null
) {
    val userId = appViewModel.userId.value
    val context = LocalContext.current

    val actualViewModel = viewModel ?: remember {
        if (context.applicationContext is InventoryApplication) {
            val appContainer = (context.applicationContext as InventoryApplication).container
            PriceTrackerViewModel(
                itemsRepository = appContainer.itemsRepository,
                receiptsRepository = appContainer.receiptsRepository
            )
        } else {
            PriceTrackerViewModel(
                itemsRepository = FakeItemsRepository(),
                receiptsRepository = FakeReceiptsRepository()
            )
        }
    }

    LaunchedEffect(userId) {
        actualViewModel.loadPriceData(userId ?: 0)
    }

    val uiState by actualViewModel.uiState.collectAsState()
    val searchQuery by actualViewModel.searchQuery.collectAsState()

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
                        ) {
                            Text(
                                text = stringResource(R.string.price_tracker_title),
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back_button),
                                tint = Color.Black
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black
                    )
                )
            },
            bottomBar = { BottomNavigationBar(navController, appViewModel) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF5F5F5))
                    .then(modifier)
            ) {
                PriceTrackerBody(
                    uiState = uiState,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { actualViewModel.updateSearchQuery(it) }
                )
            }
        }
    }
}

@Composable
fun PriceTrackerBody(
    uiState: PriceTrackerUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header with subtitle
        Text(
            text = stringResource(R.string.price_tracker_subtitle),
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Content based on sync status
        when (uiState.syncStatus) {
            PriceTrackerSyncStatus.LOADING -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen, strokeWidth = 3.dp)
                }
            }
            PriceTrackerSyncStatus.ERROR -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(Color(0xFFFFF3CD), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFE6A800),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.price_tracker_error),
                        color = Color(0xFFE6A800),
                        fontSize = 14.sp
                    )
                }
            }
            PriceTrackerSyncStatus.SUCCESS -> {
                if (uiState.filteredPriceGroups.isEmpty()) {
                    EmptyState(searchQuery = searchQuery)
                } else {
                    PriceGroupsList(priceGroups = uiState.filteredPriceGroups)
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = stringResource(R.string.price_tracker_search_hint),
                color = Color.Gray
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search_button),
                tint = Color.Gray
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryGreen,
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        singleLine = true
    )
}

@Composable
fun EmptyState(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (searchQuery.isBlank()) {
                stringResource(R.string.price_tracker_no_items)
            } else {
                stringResource(R.string.price_tracker_no_results)
            },
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PriceGroupsList(
    priceGroups: List<PriceGroup>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = priceGroups, key = { it.itemName }) { priceGroup ->
            PriceGroupCard(priceGroup = priceGroup)
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun PriceGroupCard(
    priceGroup: PriceGroup,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Item name and Savings badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = priceGroup.itemName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                if (priceGroup.savingsPercentage > 0 && priceGroup.priceEntries.size > 1) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(LightGreen)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Save up to ${priceGroup.savingsPercentage}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price entries from different stores
            priceGroup.priceEntries.forEachIndexed { index, entry ->
                PriceEntryRow(
                    entry = entry,
                    isLowestPrice = entry.price == priceGroup.lowestPrice && priceGroup.priceEntries.size > 1
                )
                if (index < priceGroup.priceEntries.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun PriceEntryRow(
    entry: PriceEntry,
    isLowestPrice: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isLowestPrice) LightGreen else Color(0xFFF8F8F8)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Store info
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Store,
                contentDescription = null,
                tint = if (isLowestPrice) PrimaryGreen else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = entry.store,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isLowestPrice) PrimaryGreen else Color.Black
                )
                Text(
                    text = entry.formattedDate,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // Price info
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "$${String.format("%.2f", entry.price)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLowestPrice) PrimaryGreen else Color(0xFFE53935)
            )
            Text(
                text = entry.unit,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PriceTrackerScreenPreview() {
    val navController = rememberNavController()
    val fakeViewModel = PriceTrackerViewModel(
        itemsRepository = FakeItemsRepository(),
        receiptsRepository = FakeReceiptsRepository()
    )

    CookingAssistantTheme {
        Surface(color = Color(0xFFF5F5F5)) {
            PriceTrackerScreen(
                navController = navController,
                appViewModel = AppViewModel(),
                viewModel = fakeViewModel
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PriceGroupCardPreview() {
    val samplePriceGroup = PriceGroup(
        itemName = "Chicken Breast",
        priceEntries = listOf(
            PriceEntry(
                store = "Costco",
                price = 4.99,
                date = java.sql.Date(System.currentTimeMillis()),
                formattedDate = "Jan 1, 2025",
                quantity = 1f,
                unit = "per lbs"
            ),
            PriceEntry(
                store = "Whole Foods",
                price = 5.99,
                date = java.sql.Date(System.currentTimeMillis()),
                formattedDate = "Jan 9, 2025",
                quantity = 1f,
                unit = "per lbs"
            ),
            PriceEntry(
                store = "Walmart",
                price = 10.98,
                date = java.sql.Date(System.currentTimeMillis()),
                formattedDate = "Oct 22, 2025",
                quantity = 1f,
                unit = "per each"
            )
        ),
        lowestPrice = 4.99,
        highestPrice = 10.98,
        savingsPercentage = 55
    )

    CookingAssistantTheme {
        Surface(color = Color(0xFFF5F5F5), modifier = Modifier.padding(16.dp)) {
            PriceGroupCard(priceGroup = samplePriceGroup)
        }
    }
}

