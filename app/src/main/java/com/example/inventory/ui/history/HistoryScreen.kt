package com.example.inventory.ui.history

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
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.userdata.FakeItemsRepository
import com.example.inventory.ui.userdata.FakeReceiptsRepository
import com.example.inventory.ui.userdata.*
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Fill
import android.graphics.Paint


private val PrimaryGreen = Color(0xFF4CAF50)
private val LightGreen = Color(0xFFE8F5E9)

object HistoryDestination : NavigationDestination {
    override val route = "history"
    override val titleRes = R.string.history_title
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
                            Text(
                                "History & Trends",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
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
                    dayAndPrice = actualViewModel.receiptUiState.collectAsState().value.dayAndPrice,
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
    dayAndPrice: List<Pair<String, Double>>,
    onReceiptClick: (Int) -> Unit,
    navController: NavController,
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(dayAndPrice) {
        println("dayAndPrice: $dayAndPrice")
    }
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
            ChartCard(dayAndPrice = dayAndPrice, modifier = Modifier.padding(bottom = 8.dp))
        }

        // 2. Track Prices Button
        item {
            TrackPricesButton(
                onClick = { /* TODO: Navigate to price trends screen */ },
                modifier = Modifier.padding(bottom = 8.dp)
            )
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
fun ChartCard(dayAndPrice: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
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
            LineChart(
                dayAndPrice = dayAndPrice,
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
fun LineChart(
    dayAndPrice: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val priceMap = dayAndPrice.associate { it.first to it.second }
    val prices = days.map { priceMap[it] ?: 0.0 }
    val maxPrice = prices.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val nonZeroIndices = prices.indices.filter { prices[it] > 0 }

    Canvas(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .background(Color.White)
    ) {
        val chartWidth = size.width
        val chartHeight = size.height
        val dayWidth = chartWidth / days.size
        val baseY = chartHeight - 40.dp.toPx()  // Leave space for labels
        val maxHeight = baseY - 20.dp.toPx()  // Padding from top

        // Draw connecting lines between non-zero points
        if (nonZeroIndices.size > 1) {
            var previousX = 0f
            var previousY = 0f
            nonZeroIndices.forEachIndexed { index, dayIndex ->
                val x = dayIndex * dayWidth + dayWidth / 2
                val normalizedPrice = (prices[dayIndex] / maxPrice).toFloat()  // Cast to Float
                val y = baseY - (normalizedPrice * maxHeight)
                if (index > 0) {
                    drawLine(
                        color = PrimaryGreen,
                        start = Offset(previousX, previousY),  // Public constructor with Floats
                        end = Offset(x, y),  // Public constructor with Floats
                        strokeWidth = 3f
                    )
                }
                previousX = x
                previousY = y
            }
        }

        // Draw dots for each non-zero spending day
        nonZeroIndices.forEach { dayIndex ->
            val x = dayIndex * dayWidth + dayWidth / 2
            val normalizedPrice = (prices[dayIndex] / maxPrice).toFloat()  // Cast to Float
            val y = baseY - (normalizedPrice * maxHeight)
            drawCircle(
                color = PrimaryGreen,
                radius = 6.dp.toPx(),
                center = Offset(x, y),  // Public constructor with Floats
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
        }

        // Draw day labels
        days.forEachIndexed { index, day ->
            val x = index * dayWidth + dayWidth / 2
            drawContext.canvas.nativeCanvas.drawText(
                day,
                x,
                baseY + 20.dp.toPx(),
                android.graphics.Paint().apply {
                    textSize = 12.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    color = android.graphics.Color.GRAY
                }
            )
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

@SuppressLint("DefaultLocale")
@Composable
fun ReceiptCard(
    receipt: Receipt,
    modifier: Modifier = Modifier,
    viewModel: ReceiptViewModel,
    navController: NavController
) {

    LaunchedEffect(receipt.receiptId) {
        viewModel.loadItems(receipt.receiptId)
    }

    val receiptUiState by viewModel.receiptUiState.collectAsState()

    CookingAssistantTheme {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_small)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = receipt.source,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Total Price: $${
                            String.format(
                                "%.2f",
                                viewModel.calculateTotalPrice(receiptUiState.itemList)
                            )
                        }",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = receipt.date.toString(),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${viewModel.calculateTotalItem(receiptUiState.itemList)} Items",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ReceiptScreenPreview() {
    val navController = rememberNavController()
    val fakeViewModel = ReceiptViewModel(
        receiptsRepository = FakeReceiptsRepository(),
        itemsRepository = FakeItemsRepository(),
        userId = 0
    )
    LaunchedEffect(Unit) {
        fakeViewModel.loadReceiptsUser()
        fakeViewModel.loadItems(1)
    }
    CookingAssistantTheme {
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

@Preview(showBackground = true)
@Composable
fun ReceiptCardPreview() {
    val navController = rememberNavController()
    val fakeViewModel = ReceiptViewModel(
        receiptsRepository = FakeReceiptsRepository(),
        itemsRepository = FakeItemsRepository(),
        userId = 0
    )
    val fakeReceipt = Receipt(
        receiptId = 1,
        userId = fakeUIuser.userId,
        source = "Walmart",
        date = Date(System.currentTimeMillis()),
        status = "Pending"
    )
    LaunchedEffect(Unit) {
        fakeViewModel.loadItems(1)
        fakeViewModel.loadReceiptsUser()
    }
    CookingAssistantTheme {
        ReceiptCard(
            receipt = fakeReceipt,
            viewModel = fakeViewModel,
            navController = navController
        )
    }
}
