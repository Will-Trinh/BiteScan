package com.example.inventory.ui.history

import com.example.inventory.ui.receipt.ReceiptViewModel
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inventory.InventoryApplication
import com.example.inventory.R
import com.example.inventory.data.Receipt
import com.example.inventory.ui.navigation.BottomNavigationBar
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.ui.theme.CookingAssistantTheme
import java.sql.Date
import java.util.Calendar
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.userdata.FakeItemsRepository
import com.example.inventory.ui.userdata.FakeReceiptsRepository
import com.example.inventory.ui.userdata.*
import com.example.inventory.data.ItemsRepository
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.ShoppingBag
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

// Constants for styling
private val PrimaryGreen = Color(0xFF4CAF50)
private val LightGreen = Color(0xFFE8F5E9)

object ReceiptDestination : NavigationDestination {
    override val route = "receipt"
    override val titleRes = R.string.receipt_title
    const val userIdArg = "userId"
    val routeWithArgs = "$route/{$userIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    navigateToReceiptEntry: () -> Unit,
    userId: Int,
    navigateToReceiptUpdate: (Int) -> Unit,
    canNavigateBack: Boolean = false,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ReceiptViewModel? = null
) {
    val context = LocalContext.current
    val actualViewModel = viewModel ?: remember {
        if (context.applicationContext is InventoryApplication) {
            val appContainer = (context.applicationContext as InventoryApplication).container
            ReceiptViewModel(
                receiptsRepository = appContainer.receiptsRepository,
                itemsRepository = appContainer.itemsRepository,
                userId = userId
            )
        } else {
            ReceiptViewModel(
                receiptsRepository = FakeReceiptsRepository(),
                itemsRepository = FakeItemsRepository(),
                userId = fakeUIuser.userId
            )
        }
    }

    LaunchedEffect(Unit) {
        actualViewModel.loadReceiptsUser()
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
                        ) {
                            Text("History & Trends", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    },
                    navigationIcon = {
                        if (canNavigateBack) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = stringResource(R.string.back_button),
                                    tint = Color.Black
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White, // White background for TopBar
                        titleContentColor = Color.Black
                    )
                )
            },
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .then(modifier),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ReceiptBody(
                    receiptList = actualViewModel.receiptUiState.collectAsState().value.receiptList,
                    onReceiptClick = navigateToReceiptUpdate,
                    navController = navController,
                    viewModel = actualViewModel
                )
            }
        }
    }
}

@Composable
fun ReceiptBody(
    receiptList: List<Receipt>,
    onReceiptClick: (Int) -> Unit,
    navController: NavController,
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier,
) {
    // We use LazyColumn for the receipts, but the top elements (Chart, Button)
    // must be in a standard Column outside the LazyColumn, or use a LazyColumn with items
    // and items(header). To allow all elements to scroll together, we'll wrap everything
    // in a LazyColumn, using the chart and button as header items.

    // We use a Column here and rely on the receipts list to use LazyColumn
    // Since the receiptList component itself uses a LazyColumn, we need to restructure
    // this to use a standard Column for the top elements and let the list scroll.
    // Given the screenshot, it looks like a standard list structure, so let's stick
    // to wrapping the entire body in a scrollable column.

    // Since we can't nest LazyColumn inside LazyColumn, and we want the Chart/Button
    // to scroll with the list, we'll use a single LazyColumn and define the top elements as items.

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Chart Card
        item {
            ChartCard(modifier = Modifier.padding(bottom = 8.dp))
        }

        // 2. Track Prices Button
        item {
            TrackPricesButton(onClick = { /* TODO: Navigate to price trends screen */ }, modifier = Modifier.padding(bottom = 8.dp))
        }

        // 3. Recent Receipts Header
        item {
            Text(
                text = "Recent Receipts",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp)
            )
        }

        // 4. Receipt List
        if (receiptList.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_receipt_description),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            val sortedReceipts = receiptList.sortedByDescending { it.date }
            items(items = sortedReceipts, key = { it.receiptId }) { receipt ->
                HistoryReceiptCard(
                    receipt = receipt,
                    onReceiptClick = onReceiptClick,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun ChartCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Last 7 Days",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LineChartPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Text(
                text = "Daily spending trend",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun LineChartPlaceholder(modifier: Modifier = Modifier) {
    // Placeholder data for the chart's X-axis
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")

    Box(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .background(Color.White)
    ) {
        // Simple line drawing to simulate the chart curve
        // NOTE: A real implementation would use a charting library here.

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            val heights = listOf(0.4f, 0.6f, 0.7f, 0.5f, 0.8f, 0.6f) // Simulating data points
            days.forEachIndexed { index, day ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.weight(1f - heights[index])) // Space above the point

                    // Small green circle for the data point
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(PrimaryGreen)
                    )

                    Spacer(modifier = Modifier.height(8.dp)) // Space above the day label
                    Text(text = day, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun TrackPricesButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon and Text
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag, // Placeholder for the bag/price icon
                        contentDescription = "Track Prices",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Track Prices",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = "Monitor grocery price trends",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            // Arrow
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Go",
                tint = Color.Gray
            )
        }
    }
}

// HistoryReceiptCard now relies entirely on the ViewModel for data (except date formatting)
@SuppressLint("DefaultLocale")
@Composable
fun HistoryReceiptCard(
    receipt: Receipt,
    onReceiptClick: (Int) -> Unit,
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier,
) {
    // Load item details for this specific receipt
    LaunchedEffect(receipt.receiptId) {
        viewModel.loadItems(receipt.receiptId)
    }

    val receiptUiState by viewModel.receiptUiState.collectAsState()

    // Find the item list corresponding to this receipt ID
    val itemList = receiptUiState.itemList.filter { it.receiptId == receipt.receiptId }

    // Calculate values using ViewModel logic
    val totalPrice = viewModel.calculateTotalPrice(itemList)
    val itemCount = viewModel.calculateTotalItem(itemList)
    val totalCalories = 2000 // Placeholder for calorie calculation

    // Function to format time ago (simplistic for UI)
    val timeAgo: String = remember(receipt.date) {
        val now = System.currentTimeMillis()
        val diff = now - receipt.date.time
        when {
            diff < TimeUnit.HOURS.toMillis(1) -> "Just now"
            diff < TimeUnit.DAYS.toMillis(1) -> "Today"
            diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)} days ago"
            else -> SimpleDateFormat("MMM dd, yyyy").format(receipt.date)
        }
    }

    Card(
        modifier = modifier.clickable { onReceiptClick(receipt.receiptId) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = receipt.source,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = timeAgo,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Calorie Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(LightGreen)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    // Using placeholder calorie value for now, replace with actual calculation
                    Text(
                        text = "${totalCalories} calories",
                        fontSize = 12.sp,
                        color = PrimaryGreen
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", totalPrice)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryGreen,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "$itemCount items",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    val navController = rememberNavController()
    // Using a fake ViewModel for Preview
    val fakeViewModel = ReceiptViewModel(
        receiptsRepository = FakeReceiptsRepository(),
        itemsRepository = FakeItemsRepository(),
        userId = fakeUIuser.userId
    )

    // Initialize data loading for the preview
    LaunchedEffect(Unit) {
        fakeViewModel.loadReceiptsUser()
        fakeViewModel.loadItems(1) // Load items for a sample receipt
    }

    CookingAssistantTheme {
        // Set the background color to match the light gray from the overall design
        Surface(color = Color(0xFFF5F5F5)) {
            ReceiptScreen(
                navigateToReceiptEntry = {},
                navigateToReceiptUpdate = {},
                navController = navController,
                viewModel = fakeViewModel,
                userId = fakeUIuser.userId,
                canNavigateBack = true,
            )
        }
    }
}