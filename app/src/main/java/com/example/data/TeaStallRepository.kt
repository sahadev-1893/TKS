package com.example.data

import com.example.data.dao.CustomerDao
import com.example.data.dao.MenuItemDao
import com.example.data.dao.OrderDao
import com.example.data.dao.PaymentDao
import com.example.data.entity.Customer
import com.example.data.entity.MenuItem
import com.example.data.entity.Order
import com.example.data.entity.OrderItem
import com.example.data.entity.Payment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

data class CustomerWithSummary(
    val customer: Customer,
    val totalOrders: Int,
    val totalAmount: Double,
    val totalPaid: Double,
    val balance: Double,
    val lastOrderDate: Long?
)

data class LedgerEntry(
    val date: Long,
    val type: String, // "ORDER" or "PAYMENT"
    val referenceNumber: String,
    val itemsSummary: String,
    val debitAmount: Double, // Order total
    val creditAmount: Double, // Payment made
    val balanceAfter: Double
)

class TeaStallRepository(
    private val customerDao: CustomerDao,
    private val menuItemDao: MenuItemDao,
    private val orderDao: OrderDao,
    private val paymentDao: PaymentDao
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

    suspend fun recordPayment(payment: Payment): Long = paymentDao.insertPayment(payment)
    suspend fun deletePayment(payment: Payment) = paymentDao.deletePayment(payment)

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
