package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CustomerWithSummary
import com.example.data.TeaStallRepository
import com.example.data.entity.*
import com.example.util.FormatUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppScreen(val title: String) {
    DASHBOARD("Dashboard"),
    CUSTOMERS("Customer Management"),
    ORDER_ENTRY("Daily Order Entry"),
    ITEMS_MASTER("Item Master"),
    PAYMENTS("Payment Management"),
    CUSTOMER_LEDGER("Customer Ledger"),
    INVENTORY("Inventory & Raw Materials"),
    SUPABASE_SYNC("Supabase Cloud Sync"),
    DATE_WISE_REPORT("Date-Wise Report"),
    DAILY_CLOSING("Daily Closing Report"),
    OUTSTANDING_REPORT("Outstanding Balances")
}

enum class DateFilterType {
    TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, CUSTOM
}

data class OrderItemDraft(
    val id: String = java.util.UUID.randomUUID().toString(),
    val itemName: String,
    val quantity: Int = 1,
    val rate: Double = 10.0
) {
    val amount: Double get() = quantity * rate
}

data class DailySalesTrend(
    val dateMillis: Long,
    val dayLabel: String, // "Mon", "Tue"
    val dateStr: String,  // "01/10"
    val salesAmount: Double,
    val paidAmount: Double,
    val orderCount: Int
)

data class DashboardSummary(
    val todayDateStr: String,
    val todayCustomersCount: Int,
    val todayOrdersCount: Int,
    val todayTotalSales: Double,
    val todayTotalPaid: Double,
    val todayTotalBalance: Double,
    val totalOutstandingBalance: Double,
    val pendingCustomersCount: Int
)

data class LedgerItemDisplay(
    val entry: CustomerLedgerEntry,
    val runningBalance: Double
)

data class CustomerLedgerData(
    val customerSummary: CustomerWithSummary?,
    val entries: List<LedgerItemDisplay>,
    val totalDebit: Double,
    val totalCredit: Double,
    val netBalance: Double
)

class TeaStallViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = TeaStallRepository(
        customerDao = database.customerDao(),
        menuItemDao = database.menuItemDao(),
        orderDao = database.orderDao(),
        paymentDao = database.paymentDao(),
        customerLedgerDao = database.customerLedgerDao(),
        inventoryDao = database.inventoryDao()
    )

    // Supabase Cloud Backend Sync
    val supabaseConfig = com.example.data.supabase.SupabaseConfig(application)
    val supabaseSyncService = com.example.data.supabase.SupabaseSyncService(supabaseConfig, database)

    // Inventory & Raw Material Tracking
    val allInventoryItems: StateFlow<List<InventoryItem>> = repository.allInventoryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockItems: StateFlow<List<InventoryItem>> = repository.lowStockItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentInventoryLogs: StateFlow<List<InventoryConsumptionLog>> = repository.recentInventoryLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun triggerAutoSync() {
        if (supabaseConfig.isConfigured && supabaseConfig.autoSyncEnabled) {
            viewModelScope.launch {
                try {
                    supabaseSyncService.syncAllData()
                } catch (_: Exception) {
                    // Non-blocking background sync
                }
            }
        }
    }

    fun syncWithSupabase(onComplete: (com.example.data.supabase.SupabaseSyncResult) -> Unit) {
        viewModelScope.launch {
            val result = supabaseSyncService.syncAllData()
            onComplete(result)
        }
    }

    fun recordDailyConsumption(
        itemId: Long,
        quantity: Double,
        notes: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.recordConsumption(itemId, quantity, notes)
            triggerAutoSync()
            onComplete()
        }
    }

    fun recordRestock(
        itemId: Long,
        quantity: Double,
        notes: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.recordRestock(itemId, quantity, notes)
            triggerAutoSync()
            onComplete()
        }
    }

    fun saveInventoryItem(
        id: Long = 0,
        name: String,
        unit: String,
        currentStock: Double,
        threshold: Double,
        costPerUnit: Double,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                repository.insertInventoryItem(
                    InventoryItem(
                        name = name.trim(),
                        unit = unit.trim(),
                        currentStock = currentStock,
                        lowStockThreshold = threshold,
                        costPerUnit = costPerUnit
                    )
                )
            } else {
                repository.updateInventoryItem(
                    InventoryItem(
                        id = id,
                        name = name.trim(),
                        unit = unit.trim(),
                        currentStock = currentStock,
                        lowStockThreshold = threshold,
                        costPerUnit = costPerUnit,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }
            onComplete()
        }
    }

    fun deleteInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
        }
    }

    // Navigation state
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // Customers and Search
    private val _customerSearchQuery = MutableStateFlow("")
    val customerSearchQuery: StateFlow<String> = _customerSearchQuery.asStateFlow()

    fun setCustomerSearchQuery(query: String) {
        _customerSearchQuery.value = query
    }

    val customersWithSummary: StateFlow<List<CustomerWithSummary>> =
        combine(repository.customersWithSummary, _customerSearchQuery) { list, query ->
            if (query.isBlank()) list
            else {
                val q = query.trim().lowercase()
                list.filter {
                    it.customer.name.lowercase().contains(q) ||
                            it.customer.mobile.contains(q) ||
                            it.customer.customId.lowercase().contains(q)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Menu Items
    val allMenuItems: StateFlow<List<MenuItem>> = repository.allMenuItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeMenuItems: StateFlow<List<MenuItem>> = repository.activeMenuItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Orders & Payments
    val allOrders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrderItems: StateFlow<List<OrderItem>> = repository.allOrderItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Customer for Ledger
    private val _selectedCustomerId = MutableStateFlow<Long?>(null)
    val selectedCustomerId: StateFlow<Long?> = _selectedCustomerId.asStateFlow()

    fun selectCustomerForLedger(customerId: Long) {
        _selectedCustomerId.value = customerId
        _currentScreen.value = AppScreen.CUSTOMER_LEDGER
    }

    // Reactive Customer Ledger Flow with Credit & Debit computation
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentCustomerLedgerData: StateFlow<CustomerLedgerData> =
        _selectedCustomerId.flatMapLatest { custId ->
            if (custId == null) {
                flowOf(CustomerLedgerData(null, emptyList(), 0.0, 0.0, 0.0))
            } else {
                combine(
                    customersWithSummary,
                    repository.getLedgerForCustomer(custId)
                ) { custList, rawEntries ->
                    val customerSum = custList.find { it.customer.id == custId }
                    val sorted = rawEntries.sortedWith(compareBy({ it.date }, { it.id }))

                    var runBal = 0.0
                    var totDebit = 0.0
                    var totCredit = 0.0

                    val displayList = sorted.map { entry ->
                        if (entry.transactionType.equals("DEBIT", ignoreCase = true)) {
                            runBal += entry.amount
                            totDebit += entry.amount
                        } else {
                            runBal -= entry.amount
                            totCredit += entry.amount
                        }
                        LedgerItemDisplay(entry = entry, runningBalance = runBal)
                    }

                    CustomerLedgerData(
                        customerSummary = customerSum,
                        entries = displayList.reversed(), // Latest transactions first in UI
                        totalDebit = totDebit,
                        totalCredit = totCredit,
                        netBalance = (totDebit - totCredit)
                    )
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CustomerLedgerData(null, emptyList(), 0.0, 0.0, 0.0)
        )

    fun addDebitTransaction(
        customerId: Long,
        amount: Double,
        title: String,
        details: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addLedgerDebit(customerId, amount, title, details)
            onComplete()
        }
    }

    fun addCreditTransaction(
        customerId: Long,
        amount: Double,
        title: String,
        details: String,
        paymentMode: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addLedgerCredit(customerId, amount, title, details, paymentMode)
            onComplete()
        }
    }

    fun deleteLedgerEntry(entry: CustomerLedgerEntry) {
        viewModelScope.launch {
            repository.deleteLedgerEntry(entry)
        }
    }

    // Dashboard Statistics
    val dashboardSummary: StateFlow<DashboardSummary> =
        combine(allOrders, allPayments, customersWithSummary) { orders, payments, customers ->
            val now = System.currentTimeMillis()
            val startOfToday = FormatUtils.getStartOfDay(now)
            val endOfToday = FormatUtils.getEndOfDay(now)

            val todayOrders = orders.filter { it.orderDate in startOfToday..endOfToday }
            val todayPayments = payments.filter { it.paymentDate in startOfToday..endOfToday }

            val todayCustIds = (todayOrders.map { it.customerId } + todayPayments.map { it.customerId }).toSet()

            val todaySales = todayOrders.sumOf { it.totalAmount }
            val todayPaid = todayPayments.sumOf { it.amount }
            val todayBalance = (todaySales - todayPaid).coerceAtLeast(0.0)

            val totalOutstanding = customers.sumOf { it.balance }
            val pendingCustCount = customers.count { it.balance > 0.0 }

            DashboardSummary(
                todayDateStr = FormatUtils.formatDateReadable(now),
                todayCustomersCount = todayCustIds.size,
                todayOrdersCount = todayOrders.size,
                todayTotalSales = todaySales,
                todayTotalPaid = todayPaid,
                todayTotalBalance = todayBalance,
                totalOutstandingBalance = totalOutstanding,
                pendingCustomersCount = pendingCustCount
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DashboardSummary(
                todayDateStr = FormatUtils.formatDateReadable(System.currentTimeMillis()),
                todayCustomersCount = 0,
                todayOrdersCount = 0,
                todayTotalSales = 0.0,
                todayTotalPaid = 0.0,
                todayTotalBalance = 0.0,
                totalOutstandingBalance = 0.0,
                pendingCustomersCount = 0
            )
        )

    // 7-Day Daily Sales & Revenue Trend
    val weeklySalesTrends: StateFlow<List<DailySalesTrend>> =
        combine(allOrders, allPayments) { orders, payments ->
            val result = mutableListOf<DailySalesTrend>()
            val dayFormat = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
            val dateFormat = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())

            for (i in 6 downTo 0) {
                val targetCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -i)
                }
                val startOfDay = FormatUtils.getStartOfDay(targetCal.timeInMillis)
                val endOfDay = FormatUtils.getEndOfDay(targetCal.timeInMillis)

                val dayOrders = orders.filter { it.orderDate in startOfDay..endOfDay }
                val dayPayments = payments.filter { it.paymentDate in startOfDay..endOfDay }

                val sales = dayOrders.sumOf { it.totalAmount }
                val paid = dayPayments.sumOf { it.amount }

                result.add(
                    DailySalesTrend(
                        dateMillis = targetCal.timeInMillis,
                        dayLabel = dayFormat.format(targetCal.time),
                        dateStr = dateFormat.format(targetCal.time),
                        salesAmount = sales,
                        paidAmount = paid,
                        orderCount = dayOrders.size
                    )
                )
            }
            result
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Order Entry Draft State
    private val _orderCustomerId = MutableStateFlow<Long?>(null)
    val orderCustomerId: StateFlow<Long?> = _orderCustomerId.asStateFlow()

    private val _orderCustomerName = MutableStateFlow("")
    val orderCustomerName: StateFlow<String> = _orderCustomerName.asStateFlow()

    private val _orderCustomerMobile = MutableStateFlow("")
    val orderCustomerMobile: StateFlow<String> = _orderCustomerMobile.asStateFlow()

    private val _draftItems = MutableStateFlow<List<OrderItemDraft>>(
        listOf(
            OrderItemDraft(itemName = "Tea", quantity = 2, rate = 10.0),
            OrderItemDraft(itemName = "Masala Tea", quantity = 1, rate = 20.0)
        )
    )
    val draftItems: StateFlow<List<OrderItemDraft>> = _draftItems.asStateFlow()

    private val _orderPaidAmount = MutableStateFlow("0")
    val orderPaidAmount: StateFlow<String> = _orderPaidAmount.asStateFlow()

    private val _orderPaymentMethod = MutableStateFlow("Cash")
    val orderPaymentMethod: StateFlow<String> = _orderPaymentMethod.asStateFlow()

    fun selectCustomerForOrder(customer: Customer) {
        _orderCustomerId.value = customer.id
        _orderCustomerName.value = customer.name
        _orderCustomerMobile.value = customer.mobile
    }

    fun setOrderCustomerManual(name: String, mobile: String) {
        _orderCustomerId.value = null
        _orderCustomerName.value = name
        _orderCustomerMobile.value = mobile
    }

    fun setOrderPaidAmount(amountStr: String) {
        _orderPaidAmount.value = amountStr
    }

    fun setOrderPaymentMethod(method: String) {
        _orderPaymentMethod.value = method
    }

    fun addDraftItem(name: String, rate: Double, quantity: Int = 1) {
        val existingIndex = _draftItems.value.indexOfFirst { it.itemName.equals(name, ignoreCase = true) }
        if (existingIndex >= 0) {
            val updated = _draftItems.value.toMutableList()
            val existing = updated[existingIndex]
            updated[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
            _draftItems.value = updated
        } else {
            _draftItems.value = _draftItems.value + OrderItemDraft(itemName = name, rate = rate, quantity = quantity)
        }
    }

    fun removeDraftItem(id: String) {
        _draftItems.value = _draftItems.value.filterNot { it.id == id }
    }

    fun updateDraftQuantity(id: String, delta: Int) {
        _draftItems.value = _draftItems.value.mapNotNull { item ->
            if (item.id == id) {
                val newQty = item.quantity + delta
                if (newQty <= 0) null else item.copy(quantity = newQty)
            } else item
        }
    }

    fun updateDraftRate(id: String, newRate: Double) {
        _draftItems.value = _draftItems.value.map {
            if (it.id == id) it.copy(rate = newRate) else it
        }
    }

    fun clearOrderForm() {
        _orderCustomerId.value = null
        _orderCustomerName.value = ""
        _orderCustomerMobile.value = ""
        _draftItems.value = listOf(OrderItemDraft(itemName = "Tea", quantity = 1, rate = 10.0))
        _orderPaidAmount.value = "0"
        _orderPaymentMethod.value = "Cash"
    }

    fun submitOrder(onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            var custId = _orderCustomerId.value
            val custName = _orderCustomerName.value.ifBlank { "Walk-in Customer" }
            val custMob = _orderCustomerMobile.value

            if (custId == null) {
                val existing = customersWithSummary.value.find {
                    it.customer.name.equals(custName, ignoreCase = true) ||
                            (custMob.isNotBlank() && it.customer.mobile == custMob)
                }
                custId = existing?.customer?.id ?: run {
                    val count = customersWithSummary.value.size + 1
                    repository.insertCustomer(
                        Customer(
                            customId = "CUST-${1000 + count}",
                            name = custName,
                            mobile = custMob,
                            notes = "Auto-created from order entry"
                        )
                    )
                }
            }

            val items = _draftItems.value
            val subtotal = items.sumOf { it.amount }
            val paid = _orderPaidAmount.value.toDoubleOrNull() ?: 0.0
            val balance = (subtotal - paid).coerceAtLeast(0.0)

            val status = when {
                balance <= 0.0 -> "PAID"
                paid > 0.0 -> "PARTIAL"
                else -> "UNPAID"
            }

            val orderNumber = "ORD-${1000 + allOrders.value.size + 1}"
            val newOrder = Order(
                orderNumber = orderNumber,
                customerId = custId,
                customerName = custName,
                customerMobile = custMob,
                orderDate = System.currentTimeMillis(),
                totalAmount = subtotal,
                paidAmount = paid,
                balanceAmount = balance,
                paymentMethod = _orderPaymentMethod.value,
                status = status
            )

            val orderEntities = items.map {
                OrderItem(
                    orderId = 0,
                    itemName = it.itemName,
                    quantity = it.quantity,
                    rate = it.rate,
                    amount = it.amount
                )
            }

            val createdId = repository.createOrderWithItems(
                order = newOrder,
                items = orderEntities,
                immediatePaymentAmount = paid,
                paymentMethod = _orderPaymentMethod.value
            )

            clearOrderForm()
            triggerAutoSync()
            onComplete(createdId)
        }
    }

    // Customer CRUD
    fun saveCustomer(
        id: Long = 0,
        customId: String,
        name: String,
        mobile: String,
        address: String,
        notes: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                val count = customersWithSummary.value.size + 1
                val assignedId = customId.ifBlank { "CUST-${1000 + count}" }
                repository.insertCustomer(
                    Customer(
                        customId = assignedId,
                        name = name.trim(),
                        mobile = mobile.trim(),
                        address = address.trim(),
                        notes = notes.trim()
                    )
                )
            } else {
                repository.updateCustomer(
                    Customer(
                        id = id,
                        customId = customId,
                        name = name.trim(),
                        mobile = mobile.trim(),
                        address = address.trim(),
                        notes = notes.trim()
                    )
                )
            }
            triggerAutoSync()
            onComplete()
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    // Item CRUD
    fun saveMenuItem(
        id: Long = 0,
        name: String,
        category: String,
        rate: Double,
        isActive: Boolean = true,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                repository.insertMenuItem(
                    MenuItem(
                        name = name.trim(),
                        category = category.trim(),
                        rate = rate,
                        isActive = isActive
                    )
                )
            } else {
                repository.updateMenuItem(
                    MenuItem(
                        id = id,
                        name = name.trim(),
                        category = category.trim(),
                        rate = rate,
                        isActive = isActive
                    )
                )
            }
            onComplete()
        }
    }

    fun toggleMenuItemActive(item: MenuItem) {
        viewModelScope.launch {
            repository.updateMenuItem(item.copy(isActive = !item.isActive))
        }
    }

    fun deleteMenuItem(item: MenuItem) {
        viewModelScope.launch {
            repository.deleteMenuItem(item)
        }
    }

    // Payment Operations
    fun recordPayment(
        customerId: Long,
        customerName: String,
        amount: Double,
        method: String,
        notes: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.recordPayment(
                Payment(
                    customerId = customerId,
                    customerName = customerName,
                    amount = amount,
                    paymentMethod = method,
                    notes = notes
                )
            )
            triggerAutoSync()
            onComplete()
        }
    }

    // Date-Wise Report Filters
    private val _dateFilterType = MutableStateFlow(DateFilterType.TODAY)
    val dateFilterType: StateFlow<DateFilterType> = _dateFilterType.asStateFlow()

    private val _customStartDate = MutableStateFlow(FormatUtils.getStartOfDay(System.currentTimeMillis()))
    val customStartDate: StateFlow<Long> = _customStartDate.asStateFlow()

    private val _customEndDate = MutableStateFlow(FormatUtils.getEndOfDay(System.currentTimeMillis()))
    val customEndDate: StateFlow<Long> = _customEndDate.asStateFlow()

    fun setDateFilter(filterType: DateFilterType) {
        _dateFilterType.value = filterType
        val now = System.currentTimeMillis()
        when (filterType) {
            DateFilterType.TODAY -> {
                _customStartDate.value = FormatUtils.getStartOfDay(now)
                _customEndDate.value = FormatUtils.getEndOfDay(now)
            }
            DateFilterType.YESTERDAY -> {
                val yest = now - (24 * 60 * 60 * 1000L)
                _customStartDate.value = FormatUtils.getStartOfDay(yest)
                _customEndDate.value = FormatUtils.getEndOfDay(yest)
            }
            DateFilterType.THIS_WEEK -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                }
                _customStartDate.value = FormatUtils.getStartOfDay(cal.timeInMillis)
                _customEndDate.value = FormatUtils.getEndOfDay(now)
            }
            DateFilterType.THIS_MONTH -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                _customStartDate.value = FormatUtils.getStartOfDay(cal.timeInMillis)
                _customEndDate.value = FormatUtils.getEndOfDay(now)
            }
            DateFilterType.CUSTOM -> {
                // Keep current start and end date
            }
        }
    }

    fun setCustomDateRange(start: Long, end: Long) {
        _dateFilterType.value = DateFilterType.CUSTOM
        _customStartDate.value = FormatUtils.getStartOfDay(start)
        _customEndDate.value = FormatUtils.getEndOfDay(end)
    }

    // Filtered Report Data
    val dateWiseReportOrders: StateFlow<List<Pair<Order, String>>> =
        combine(allOrders, allOrderItems, _customStartDate, _customEndDate) { orders, items, start, end ->
            orders.filter { it.orderDate in start..end }.map { ord ->
                val ordItems = items.filter { it.orderId == ord.id }
                val summary = if (ordItems.isNotEmpty()) {
                    ordItems.joinToString(", ") { "${it.itemName}×${it.quantity}" }
                } else "Chai & Snacks"
                Pair(ord, summary)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily Closing Report State
    private val _closingReportDate = MutableStateFlow(System.currentTimeMillis())
    val closingReportDate: StateFlow<Long> = _closingReportDate.asStateFlow()

    fun setClosingReportDate(timestamp: Long) {
        _closingReportDate.value = timestamp
    }
}
