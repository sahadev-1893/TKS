package com.example.data

import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class CustomerWithSummary(
    val customer: Customer,
    val totalOrders: Int,
    val totalAmount: Double,
    val totalPaid: Double,
    val balance: Double,
    val lastOrderDate: Long?
)

class TeaStallRepository(
    private val customerDao: CustomerDao,
    private val menuItemDao: MenuItemDao,
    private val orderDao: OrderDao,
    private val paymentDao: PaymentDao,
    private val customerLedgerDao: CustomerLedgerDao,
    private val inventoryDao: InventoryDao
) {
    // Customers
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()

    fun searchCustomers(query: String): Flow<List<Customer>> =
        if (query.isBlank()) customerDao.getAllCustomers()
        else customerDao.searchCustomers(query.trim())

    fun getCustomerById(id: Long): Flow<Customer?> = customerDao.getCustomerById(id)

    suspend fun insertCustomer(customer: Customer): Long = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)

    // Menu Items
    val allMenuItems: Flow<List<MenuItem>> = menuItemDao.getAllMenuItems()
    val activeMenuItems: Flow<List<MenuItem>> = menuItemDao.getActiveMenuItems()

    suspend fun insertMenuItem(item: MenuItem): Long = menuItemDao.insertMenuItem(item)
    suspend fun updateMenuItem(item: MenuItem) = menuItemDao.updateMenuItem(item)
    suspend fun deleteMenuItem(item: MenuItem) = menuItemDao.deleteMenuItem(item)

    // Orders
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    val allOrderItems: Flow<List<OrderItem>> = orderDao.getAllOrderItems()

    fun getOrdersByCustomer(customerId: Long): Flow<List<Order>> =
        orderDao.getOrdersByCustomer(customerId)

    fun getOrderItemsForOrder(orderId: Long): Flow<List<OrderItem>> =
        orderDao.getOrderItemsForOrder(orderId)

    suspend fun createOrderWithItems(
        order: Order,
        items: List<OrderItem>,
        immediatePaymentAmount: Double,
        paymentMethod: String
    ): Long {
        val orderId = orderDao.insertOrder(order)
        val itemsWithId = items.map { it.copy(orderId = orderId) }
        orderDao.insertOrderItems(itemsWithId)

        val itemsSummary = if (items.isNotEmpty()) {
            items.joinToString(", ") { "${it.itemName} (${it.quantity})" }
        } else "Tea Stall Order"

        // 1. Debit Entry for the Order
        customerLedgerDao.insertEntry(
            CustomerLedgerEntry(
                customerId = order.customerId,
                date = order.orderDate,
                transactionType = "DEBIT",
                amount = order.totalAmount,
                title = "Order #${order.orderNumber.ifBlank { "$orderId" }}",
                details = itemsSummary,
                paymentMode = "Credit Sale",
                referenceNo = order.orderNumber.ifBlank { "ORD-$orderId" }
            )
        )

        // 2. Immediate Credit Entry if paid
        if (immediatePaymentAmount > 0) {
            paymentDao.insertPayment(
                Payment(
                    customerId = order.customerId,
                    customerName = order.customerName,
                    orderId = orderId,
                    paymentDate = order.orderDate,
                    amount = immediatePaymentAmount,
                    paymentMethod = paymentMethod,
                    notes = "Payment for ${order.orderNumber.ifBlank { "Order #$orderId" }}"
                )
            )

            customerLedgerDao.insertEntry(
                CustomerLedgerEntry(
                    customerId = order.customerId,
                    date = order.orderDate,
                    transactionType = "CREDIT",
                    amount = immediatePaymentAmount,
                    title = "Payment received",
                    details = "Paid for ${order.orderNumber.ifBlank { "Order #$orderId" }}",
                    paymentMode = paymentMethod,
                    referenceNo = order.orderNumber.ifBlank { "ORD-$orderId" }
                )
            )
        }
        return orderId
    }

    suspend fun deleteOrder(order: Order) {
        orderDao.deleteItemsForOrder(order.id)
        orderDao.deleteOrder(order)
    }

    // Payments
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()

    fun getPaymentsByCustomer(customerId: Long): Flow<List<Payment>> =
        paymentDao.getPaymentsByCustomer(customerId)

    suspend fun recordPayment(payment: Payment): Long {
        val payId = paymentDao.insertPayment(payment)
        customerLedgerDao.insertEntry(
            CustomerLedgerEntry(
                customerId = payment.customerId,
                date = payment.paymentDate,
                transactionType = "CREDIT",
                amount = payment.amount,
                title = "Payment received",
                details = payment.notes.ifBlank { "Settlement" },
                paymentMode = payment.paymentMethod,
                referenceNo = "PAY-$payId"
            )
        )
        return payId
    }

    suspend fun deletePayment(payment: Payment) = paymentDao.deletePayment(payment)

    // Customer Ledger (Credit & Debit Tracking)
    fun getLedgerForCustomer(customerId: Long): Flow<List<CustomerLedgerEntry>> =
        customerLedgerDao.getEntriesForCustomer(customerId)

    suspend fun addLedgerDebit(
        customerId: Long,
        amount: Double,
        title: String,
        details: String,
        date: Long = System.currentTimeMillis()
    ): Long {
        return customerLedgerDao.insertEntry(
            CustomerLedgerEntry(
                customerId = customerId,
                date = date,
                transactionType = "DEBIT",
                amount = amount,
                title = title.ifBlank { "Debit / Udhar" },
                details = details,
                paymentMode = "Credit Sale",
                referenceNo = "DEB-${System.currentTimeMillis() % 100000}"
            )
        )
    }

    suspend fun addLedgerCredit(
        customerId: Long,
        amount: Double,
        title: String,
        details: String,
        paymentMode: String = "Cash",
        date: Long = System.currentTimeMillis()
    ): Long {
        return customerLedgerDao.insertEntry(
            CustomerLedgerEntry(
                customerId = customerId,
                date = date,
                transactionType = "CREDIT",
                amount = amount,
                title = title.ifBlank { "Payment / Jama" },
                details = details,
                paymentMode = paymentMode,
                referenceNo = "CRE-${System.currentTimeMillis() % 100000}"
            )
        )
    }

    suspend fun updateLedgerEntry(entry: CustomerLedgerEntry) =
        customerLedgerDao.updateEntry(entry)

    suspend fun deleteLedgerEntry(entry: CustomerLedgerEntry) =
        customerLedgerDao.deleteEntry(entry)

    // Inventory & Raw Material Tracking (Milk, Tea Leaves, Sugar, etc.)
    val allInventoryItems: Flow<List<InventoryItem>> = inventoryDao.getAllInventoryItems()
    val lowStockItems: Flow<List<InventoryItem>> = inventoryDao.getLowStockItems()
    val recentInventoryLogs: Flow<List<InventoryConsumptionLog>> = inventoryDao.getRecentLogs()

    suspend fun insertInventoryItem(item: InventoryItem): Long = inventoryDao.insertItem(item)
    suspend fun updateInventoryItem(item: InventoryItem) = inventoryDao.updateItem(item)
    suspend fun deleteInventoryItem(item: InventoryItem) = inventoryDao.deleteItem(item)

    suspend fun recordConsumption(
        itemId: Long,
        quantity: Double,
        notes: String
    ): Boolean {
        val item = inventoryDao.getItemById(itemId) ?: return false
        val newStock = (item.currentStock - quantity).coerceAtLeast(0.0)
        inventoryDao.updateItem(item.copy(currentStock = newStock, lastUpdated = System.currentTimeMillis()))
        inventoryDao.insertLog(
            InventoryConsumptionLog(
                itemId = item.id,
                itemName = item.name,
                quantity = quantity,
                unit = item.unit,
                type = "CONSUMPTION",
                remainingStockAfter = newStock,
                notes = notes
            )
        )
        return true
    }

    suspend fun recordRestock(
        itemId: Long,
        quantity: Double,
        notes: String
    ): Boolean {
        val item = inventoryDao.getItemById(itemId) ?: return false
        val newStock = item.currentStock + quantity
        inventoryDao.updateItem(item.copy(currentStock = newStock, lastUpdated = System.currentTimeMillis()))
        inventoryDao.insertLog(
            InventoryConsumptionLog(
                itemId = item.id,
                itemName = item.name,
                quantity = quantity,
                unit = item.unit,
                type = "RESTOCK",
                remainingStockAfter = newStock,
                notes = notes
            )
        )
        return true
    }

    // Combined: Customers with balances and order totals
    val customersWithSummary: Flow<List<CustomerWithSummary>> =
        combine(allCustomers, allOrders, allPayments) { customers, orders, payments ->
            customers.map { customer ->
                val customerOrders = orders.filter { it.customerId == customer.id }
                val totalAmount = customerOrders.sumOf { it.totalAmount }
                val customerPayments = payments.filter { it.customerId == customer.id }
                val totalPaid = customerPayments.sumOf { it.amount }
                val balance = (totalAmount - totalPaid).coerceAtLeast(0.0)
                val lastOrderDate = customerOrders.maxOfOrNull { it.orderDate }

                CustomerWithSummary(
                    customer = customer,
                    totalOrders = customerOrders.size,
                    totalAmount = totalAmount,
                    totalPaid = totalPaid,
                    balance = balance,
                    lastOrderDate = lastOrderDate
                )
            }
        }
}
