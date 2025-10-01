package com.example.inventory.ui.receipt

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.userdata.FakeItemsRepository
import com.example.inventory.ui.userdata.FakeReceiptsRepository
import com.example.inventory.ui.userdata.*

object ReceiptDestination : NavigationDestination {
    override val route = "receipt"
    override val titleRes = R.string.receipt_title
    const val userIdArg = "userId"
    val routeWithArgs = "$route/{$userIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateUp: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 48.dp),
                horizontalArrangement = Arrangement.Center
            ) { Text(title) }
        },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back_button),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    navigateToReceiptEntry: () -> Unit,
    userId: Int,
    navigateToReceiptUpdate: (Int) -> Unit,
    canNavigateBack: Boolean = false,
    navigateUp: () -> Unit = {},
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
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        Scaffold(
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                InventoryTopAppBar(
                    title = stringResource(ReceiptDestination.titleRes),
                    canNavigateBack = canNavigateBack,
                    scrollBehavior = scrollBehavior,
                    navigateUp = navigateUp
                )
            },
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            ReceiptBody(
                receiptList = actualViewModel.receiptUiState.collectAsState().value.receiptList,
                onReceiptClick = navigateToReceiptUpdate,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    horizontal = dimensionResource(id = R.dimen.padding_small)
                ),
                navController = navController,
                viewModel = actualViewModel
            )
        }
    }
}

@Composable
fun ReceiptBody(
    receiptList: List<Receipt>,
    onReceiptClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    navController: NavController,
    viewModel: ReceiptViewModel
) {
    val calendar = Calendar.getInstance()
    val today = calendar.time
    calendar.add(Calendar.DAY_OF_YEAR, -7)
    val oneWeekAgo = calendar.time

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (receiptList.isEmpty()) {
            Text(
                text = stringResource(R.string.no_receipt_description),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(contentPadding)
            )
        } else {
            val recentReceipts =
                receiptList.filter { it.date.after(oneWeekAgo) && it.date.before(today) || it.date == today }
            val olderReceipts = receiptList.filter { it.date.before(oneWeekAgo) }

            if (recentReceipts.isNotEmpty()) {
                Text(
                    text = "Recent Upload",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ReceiptList(
                    recentReceipts,
                    onReceiptClick,
                    contentPadding,
                    navController,
                    viewModel
                )
            }
            if (olderReceipts.isNotEmpty()) {
                Text(
                    text = "Older Upload",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                ReceiptList(olderReceipts, onReceiptClick, contentPadding, navController, viewModel)
            }
        }
    }
}

@Composable
fun ReceiptList(
    receiptList: List<Receipt>,
    onReceiptClick: (Int) -> Unit,
    contentPadding: PaddingValues,
    navController: NavController,
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(items = receiptList, key = { it.receiptId }) { receipt ->
            ReceiptCard(
                receipt = receipt,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_small))
                    .clickable { onReceiptClick(receipt.receiptId) },
                viewModel = viewModel,
                navController = navController
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
                        text = "Total Price: $${String.format("%.2f", viewModel.calculateTotalPrice(receiptUiState.itemList))}",
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
            navigateUp = {}
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
