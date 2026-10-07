package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CustomerWithSummary
import com.example.data.entity.Customer
import com.example.data.entity.Order
import com.example.ui.AppScreen
import com.example.ui.TeaStallViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.components.WhatsAppReportDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.util.WhatsAppUtils
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: TeaStallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TunaKakaTheme {
                TunaKakaApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TunaKakaApp(viewModel: TeaStallViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val dashboardSummary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val customersWithSummary by viewModel.customersWithSummary.collectAsStateWithLifecycle()
    val customerSearchQuery by viewModel.customerSearchQuery.collectAsStateWithLifecycle()
    val allMenuItems by viewModel.allMenuItems.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()

    val ledgerData by viewModel.currentCustomerLedgerData.collectAsStateWithLifecycle()
    val weeklySalesTrends by viewModel.weeklySalesTrends.collectAsStateWithLifecycle()
    val dateFilterType by viewModel.dateFilterType.collectAsStateWithLifecycle()
    val customStartDate by viewModel.customStartDate.collectAsStateWithLifecycle()
    val customEndDate by viewModel.customEndDate.collectAsStateWithLifecycle()
    val dateWiseOrders by viewModel.dateWiseReportOrders.collectAsStateWithLifecycle()
    val closingReportDate by viewModel.closingReportDate.collectAsStateWithLifecycle()

    val allInventoryItems by viewModel.allInventoryItems.collectAsStateWithLifecycle()
    val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
    val recentInventoryLogs by viewModel.recentInventoryLogs.collectAsStateWithLifecycle()

    // Order entry draft
    val orderCustomerId by viewModel.orderCustomerId.collectAsStateWithLifecycle()
    val orderCustomerName by viewModel.orderCustomerName.collectAsStateWithLifecycle()
    val orderCustomerMobile by viewModel.orderCustomerMobile.collectAsStateWithLifecycle()
    val draftItems by viewModel.draftItems.collectAsStateWithLifecycle()
    val orderPaidAmount by viewModel.orderPaidAmount.collectAsStateWithLifecycle()
    val orderPaymentMethod by viewModel.orderPaymentMethod.collectAsStateWithLifecycle()

    // WhatsApp Report Dialog state
    var showWhatsAppDialog by remember { mutableStateOf(false) }
    var whatsAppReportContent by remember { mutableStateOf("") }

    // Order receipt / detail dialog
    var selectedOrderForDetail by remember { mutableStateOf<Order?>(null) }

    // Quick Receive Payment modal state
    var quickPaymentCustomer by remember { mutableStateOf<CustomerWithSummary?>(null) }
    var quickPaymentAmount by remember { mutableStateOf("") }
    var quickPaymentMethod by remember { mutableStateOf("Cash") }

    // Quick Add Customer modal
    var showNewCustomerQuickDialog by remember { mutableStateOf(false) }
    var newCustQuickName by remember { mutableStateOf("") }
    var newCustQuickMobile by remember { mutableStateOf("") }

    // Back handling: If not on DASHBOARD, pressing Back navigates back to DASHBOARD
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    fun openWhatsAppDailyReport() {
        val now = System.currentTimeMillis()
        val startOfToday = FormatUtils.getStartOfDay(now)
        val endOfToday = FormatUtils.getEndOfDay(now)
        val todayOrders = allOrders.filter { it.orderDate in startOfToday..endOfToday }
        val breakdown = todayOrders.map {
            Triple(it.customerName, it.totalAmount, it.paidAmount)
        }
        whatsAppReportContent = WhatsAppUtils.generateDailyReport(
            dateStr = FormatUtils.formatDate(now),
            customerCount = dashboardSummary.todayCustomersCount,
            orderCount = dashboardSummary.todayOrdersCount,
            totalSales = dashboardSummary.todayTotalSales,
            totalPaid = dashboardSummary.todayTotalPaid,
            totalBalance = dashboardSummary.todayTotalBalance,
            customerBreakdown = breakdown
        )
        showWhatsAppDialog = true
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = ChaiBackgroundLight,
                modifier = Modifier.width(310.dp)
            ) {
                // Drawer Header
                Surface(
                    color = ChaiPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalCafe,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "TUNA KAKA",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Tea Stall Management System",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Items
                val navItems = listOf(
                    Triple(AppScreen.DASHBOARD, Icons.Default.Dashboard, "Dashboard"),
                    Triple(AppScreen.CUSTOMERS, Icons.Default.People, "Customer Management"),
                    Triple(AppScreen.ORDER_ENTRY, Icons.Default.AddShoppingCart, "Daily Order Entry"),
                    Triple(AppScreen.ITEMS_MASTER, Icons.Default.RestaurantMenu, "Item Master"),
                    Triple(AppScreen.INVENTORY, Icons.Default.Inventory, "Raw Material Inventory"),
                    Triple(AppScreen.SUPABASE_SYNC, Icons.Default.CloudSync, "Supabase Cloud Sync"),
                    Triple(AppScreen.PAYMENTS, Icons.Default.Payments, "Payment Management"),
                    Triple(AppScreen.DATE_WISE_REPORT, Icons.Default.Assessment, "Date-Wise Report"),
                    Triple(AppScreen.DAILY_CLOSING, Icons.Default.ReceiptLong, "Daily Closing Report"),
                    Triple(AppScreen.OUTSTANDING_REPORT, Icons.Default.Warning, "Outstanding Balances")
                )

                navItems.forEach { (screen, icon, title) ->
                    NavigationDrawerItem(
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(title, fontWeight = if (currentScreen == screen) FontWeight.Bold else FontWeight.Normal) },
                        selected = currentScreen == screen,
                        onClick = {
                            viewModel.navigateTo(screen)
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = ChaiCardWarm,
                            selectedTextColor = ChaiPrimary,
                            selectedIconColor = ChaiPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = ChaiBorder)

                // Quick WhatsApp Share button in drawer
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF25D366)) },
                    label = { Text("WhatsApp Daily Report", fontWeight = FontWeight.Bold, color = Color(0xFF25D366)) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        openWhatsAppDailyReport()
                    },
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Tuna Kaka Tea Stall",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = ChaiTextPrimary
                            )
                            Text(
                                text = currentScreen.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = ChaiPrimary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("btn_open_drawer")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Menu", tint = ChaiPrimary)
                        }
                    },
                    actions = {
                        if (currentScreen != AppScreen.ORDER_ENTRY) {
                            IconButton(
                                onClick = { viewModel.navigateTo(AppScreen.ORDER_ENTRY) },
                                modifier = Modifier.testTag("top_bar_new_order")
                            ) {
                                Icon(Icons.Default.AddShoppingCart, contentDescription = "New Order", tint = ChaiSecondary)
                            }
                        }

                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.SUPABASE_SYNC) },
                            modifier = Modifier.testTag("top_bar_supabase_sync")
                        ) {
                            Icon(
                                imageVector = if (viewModel.supabaseConfig.isConnected) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                contentDescription = "Supabase Cloud Sync",
                                tint = if (viewModel.supabaseConfig.isConnected) PaidGreen else ChaiPrimary
                            )
                        }

                        IconButton(
                            onClick = { openWhatsAppDailyReport() },
                            modifier = Modifier.testTag("top_bar_whatsapp")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "WhatsApp Report", tint = Color(0xFF25D366))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ChaiSurfaceLight
                    )
                )
            },
            bottomBar = {
                // Bottom bar for instant navigation between high-frequency screens
                NavigationBar(
                    containerColor = ChaiSurfaceLight,
                    tonalElevation = 8.dp
                ) {
                    val bottomNavScreens = listOf(
                        Triple(AppScreen.DASHBOARD, Icons.Default.Dashboard, "Home"),
                        Triple(AppScreen.CUSTOMERS, Icons.Default.People, "Customers"),
                        Triple(AppScreen.ORDER_ENTRY, Icons.Default.AddCircle, "Order"),
                        Triple(AppScreen.ITEMS_MASTER, Icons.Default.Fastfood, "Items"),
                        Triple(AppScreen.OUTSTANDING_REPORT, Icons.Default.Warning, "Due")
                    )

                    bottomNavScreens.forEach { (screen, icon, label) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (currentScreen == screen) FontWeight.Bold else FontWeight.Normal) },
                            selected = currentScreen == screen,
                            onClick = { viewModel.navigateTo(screen) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ChaiPrimary,
                                selectedTextColor = ChaiPrimary,
                                indicatorColor = ChaiCardWarm
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> {
                        DashboardScreen(
                            summary = dashboardSummary,
                            recentOrders = allOrders,
                            weeklyTrends = weeklySalesTrends,
                            lowStockCount = lowStockItems.size,
                            supabaseConfig = viewModel.supabaseConfig,
                            onQuickSync = {
                                Toast.makeText(context, "Syncing with Supabase cloud...", Toast.LENGTH_SHORT).show()
                                viewModel.syncWithSupabase { result ->
                                    when (result) {
                                        is com.example.data.supabase.SupabaseSyncResult.Success -> {
                                            Toast.makeText(context, "Supabase Cloud Sync completed!", Toast.LENGTH_SHORT).show()
                                        }
                                        is com.example.data.supabase.SupabaseSyncResult.Error -> {
                                            Toast.makeText(context, "Sync error: ${result.error}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            onNavigate = { viewModel.navigateTo(it) },
                            onOpenWhatsAppReport = { openWhatsAppDailyReport() },
                            onOpenReceivePayment = {
                                val cust = customersWithSummary.firstOrNull { it.balance > 0 } ?: customersWithSummary.firstOrNull()
                                if (cust != null) {
                                    quickPaymentCustomer = cust
                                    quickPaymentAmount = cust.balance.toInt().toString()
                                } else {
                                    Toast.makeText(context, "No customers with dues found", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onOpenNewCustomer = {
                                showNewCustomerQuickDialog = true
                            },
                            onOpenOrder = { order ->
                                selectedOrderForDetail = order
                            }
                        )
                    }

                    AppScreen.CUSTOMERS -> {
                        CustomerManagementScreen(
                            customers = customersWithSummary,
                            searchQuery = customerSearchQuery,
                            onSearchQueryChange = { viewModel.setCustomerSearchQuery(it) },
                            onViewHistory = { customerId ->
                                viewModel.selectCustomerForLedger(customerId)
                            },
                            onSaveCustomer = { id, customId, name, mobile, address, notes, onDone ->
                                viewModel.saveCustomer(id, customId, name, mobile, address, notes, onDone)
                            },
                            onDeleteCustomer = { viewModel.deleteCustomer(it) },
                            onReceivePaymentForCustomer = { cust ->
                                val sum = customersWithSummary.find { it.customer.id == cust.id }
                                quickPaymentCustomer = sum
                                quickPaymentAmount = (sum?.balance ?: 0.0).toInt().toString()
                            }
                        )
                    }

                    AppScreen.ORDER_ENTRY -> {
                        OrderEntryScreen(
                            customers = customersWithSummary,
                            menuItems = allMenuItems.filter { it.isActive },
                            selectedCustomerId = orderCustomerId,
                            customerName = orderCustomerName,
                            customerMobile = orderCustomerMobile,
                            draftItems = draftItems,
                            paidAmount = orderPaidAmount,
                            paymentMethod = orderPaymentMethod,
                            onSelectCustomer = { viewModel.selectCustomerForOrder(it) },
                            onSetCustomerManual = { name, mob -> viewModel.setOrderCustomerManual(name, mob) },
                            onAddDraftItem = { name, rate, qty -> viewModel.addDraftItem(name, rate, qty) },
                            onRemoveDraftItem = { viewModel.removeDraftItem(it) },
                            onUpdateQuantity = { id, delta -> viewModel.updateDraftQuantity(id, delta) },
                            onUpdateRate = { id, rate -> viewModel.updateDraftRate(id, rate) },
                            onSetPaidAmount = { viewModel.setOrderPaidAmount(it) },
                            onSetPaymentMethod = { viewModel.setOrderPaymentMethod(it) },
                            onSubmitOrder = { onDone -> viewModel.submitOrder(onDone) },
                            onOrderSuccess = { orderId ->
                                Toast.makeText(context, "Order created successfully!", Toast.LENGTH_SHORT).show()
                                viewModel.navigateTo(AppScreen.DASHBOARD)
                            }
                        )
                    }

                    AppScreen.ITEMS_MASTER -> {
                        ItemMasterScreen(
                            items = allMenuItems,
                            onSaveItem = { id, name, cat, rate, active, onDone ->
                                viewModel.saveMenuItem(id, name, cat, rate, active, onDone)
                            },
                            onToggleActive = { viewModel.toggleMenuItemActive(it) },
                            onDeleteItem = { viewModel.deleteMenuItem(it) }
                        )
                    }

                    AppScreen.INVENTORY -> {
                        InventoryManagementScreen(
                            items = allInventoryItems,
                            lowStockItems = lowStockItems,
                            logs = recentInventoryLogs,
                            onRecordConsumption = { itemId, qty, notes, onDone ->
                                viewModel.recordDailyConsumption(itemId, qty, notes, onDone)
                            },
                            onRecordRestock = { itemId, qty, notes, onDone ->
                                viewModel.recordRestock(itemId, qty, notes, onDone)
                            },
                            onSaveItem = { id, name, unit, stock, thresh, cost, onDone ->
                                viewModel.saveInventoryItem(id, name, unit, stock, thresh, cost, onDone)
                            },
                            onDeleteItem = { item ->
                                viewModel.deleteInventoryItem(item)
                            }
                        )
                    }

                    AppScreen.SUPABASE_SYNC -> {
                        SupabaseSyncScreen(
                            config = viewModel.supabaseConfig,
                            syncService = viewModel.supabaseSyncService
                        )
                    }

                    AppScreen.PAYMENTS -> {
                        PaymentManagementScreen(
                            customers = customersWithSummary,
                            payments = allPayments,
                            onRecordPayment = { custId, name, amt, mode, notes, onDone ->
                                viewModel.recordPayment(custId, name, amt, mode, notes, onDone)
                            },
                            onViewCustomerLedger = { custId ->
                                viewModel.selectCustomerForLedger(custId)
                            }
                        )
                    }

                    AppScreen.CUSTOMER_LEDGER -> {
                        CustomerLedgerScreen(
                            ledgerData = ledgerData,
                            allCustomers = customersWithSummary,
                            onSelectCustomer = { custId ->
                                viewModel.selectCustomerForLedger(custId)
                            },
                            onAddDebit = { custId, amt, title, details, onDone ->
                                viewModel.addDebitTransaction(custId, amt, title, details, onDone)
                            },
                            onAddCredit = { custId, amt, title, details, mode, onDone ->
                                viewModel.addCreditTransaction(custId, amt, title, details, mode, onDone)
                            },
                            onDeleteEntry = { entry ->
                                viewModel.deleteLedgerEntry(entry)
                            },
                            onBack = { viewModel.navigateTo(AppScreen.CUSTOMERS) }
                        )
                    }

                    AppScreen.DATE_WISE_REPORT -> {
                        DateWiseReportScreen(
                            dateFilterType = dateFilterType,
                            startDate = customStartDate,
                            endDate = customEndDate,
                            ordersWithSummary = dateWiseOrders,
                            onSelectDateFilter = { viewModel.setDateFilter(it) },
                            onOpenOrder = { selectedOrderForDetail = it }
                        )
                    }

                    AppScreen.DAILY_CLOSING -> {
                        DailyClosingReportScreen(
                            closingDate = closingReportDate,
                            orders = allOrders,
                            payments = allPayments,
                            onSelectDate = { viewModel.setClosingReportDate(it) }
                        )
                    }

                    AppScreen.OUTSTANDING_REPORT -> {
                        OutstandingReportScreen(
                            customers = customersWithSummary,
                            onViewLedger = { custId ->
                                viewModel.selectCustomerForLedger(custId)
                            },
                            onReceivePayment = { cust ->
                                quickPaymentCustomer = cust
                                quickPaymentAmount = cust.balance.toInt().toString()
                            }
                        )
                    }
                }
            }
        }
    }

    // WhatsApp Report Modal
    if (showWhatsAppDialog) {
        WhatsAppReportDialog(
            reportText = whatsAppReportContent,
            onDismiss = { showWhatsAppDialog = false }
        )
    }

    // Quick Payment Dialog
    quickPaymentCustomer?.let { cust ->
        val prevBalance = cust.balance
        val paymentVal = quickPaymentAmount.toDoubleOrNull() ?: 0.0
        val remaining = (prevBalance - paymentVal).coerceAtLeast(0.0)

        AlertDialog(
            onDismissRequest = { quickPaymentCustomer = null },
            title = {
                Text(
                    text = "Receive Payment: ${cust.customer.name}",
                    fontWeight = FontWeight.Bold,
                    color = PaidGreen
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = ChaiCardWarm,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Previous Balance:", style = MaterialTheme.typography.bodySmall)
                                Text(FormatUtils.formatRupees(prevBalance), fontWeight = FontWeight.Bold, color = BalanceRed)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Payment:", style = MaterialTheme.typography.bodySmall)
                                Text(FormatUtils.formatRupees(paymentVal), fontWeight = FontWeight.Bold, color = PaidGreen)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Remaining:", fontWeight = FontWeight.Bold)
                                Text(FormatUtils.formatRupees(remaining), fontWeight = FontWeight.Black, color = if (remaining > 0) BalanceRed else PaidGreen)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = quickPaymentAmount,
                        onValueChange = { quickPaymentAmount = it },
                        label = { Text("Payment Amount (₹) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Cash", "UPI", "PhonePe", "GPay").forEach { mode ->
                            FilterChip(
                                selected = quickPaymentMethod == mode,
                                onClick = { quickPaymentMethod = mode },
                                label = { Text(mode, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (paymentVal > 0) {
                            viewModel.recordPayment(
                                customerId = cust.customer.id,
                                customerName = cust.customer.name,
                                amount = paymentVal,
                                method = quickPaymentMethod,
                                notes = "Quick payment reception"
                            ) {
                                Toast.makeText(context, "Payment of ${FormatUtils.formatRupees(paymentVal)} received!", Toast.LENGTH_SHORT).show()
                                quickPaymentCustomer = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PaidGreen)
                ) {
                    Text("RECEIVE PAYMENT", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { quickPaymentCustomer = null }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Quick Add Customer Dialog
    if (showNewCustomerQuickDialog) {
        AlertDialog(
            onDismissRequest = { showNewCustomerQuickDialog = false },
            title = { Text("+ New Customer", fontWeight = FontWeight.Bold, color = ChaiPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCustQuickName,
                        onValueChange = { newCustQuickName = it },
                        label = { Text("Customer Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCustQuickMobile,
                        onValueChange = { newCustQuickMobile = it },
                        label = { Text("Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCustQuickName.isNotBlank()) {
                            viewModel.saveCustomer(
                                id = 0,
                                customId = "CUST-${1000 + customersWithSummary.size + 1}",
                                name = newCustQuickName,
                                mobile = newCustQuickMobile,
                                address = "",
                                notes = ""
                            ) {
                                Toast.makeText(context, "Customer $newCustQuickName added!", Toast.LENGTH_SHORT).show()
                                showNewCustomerQuickDialog = false
                                newCustQuickName = ""
                                newCustQuickMobile = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChaiPrimary)
                ) {
                    Text("SAVE CUSTOMER", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewCustomerQuickDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Order detail view
    selectedOrderForDetail?.let { ord ->
        AlertDialog(
            onDismissRequest = { selectedOrderForDetail = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(ord.orderNumber, fontWeight = FontWeight.Bold, color = ChaiPrimary)
                    StatusBadge(status = ord.status)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Customer: ${ord.customerName}", fontWeight = FontWeight.Bold)
                    Text("Date: ${FormatUtils.formatDateTime(ord.orderDate)}", style = MaterialTheme.typography.bodySmall)
                    Text("Payment Mode: ${ord.paymentMethod}")
                    HorizontalDivider(color = ChaiBorder)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount:")
                        Text(FormatUtils.formatRupees(ord.totalAmount), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Paid Amount:")
                        Text(FormatUtils.formatRupees(ord.paidAmount), fontWeight = FontWeight.Bold, color = PaidGreen)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Balance Due:")
                        Text(FormatUtils.formatRupees(ord.balanceAmount), fontWeight = FontWeight.Bold, color = if (ord.balanceAmount > 0) BalanceRed else PaidGreen)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedOrderForDetail = null }) {
                    Text("CLOSE")
                }
            }
        )
    }
}
