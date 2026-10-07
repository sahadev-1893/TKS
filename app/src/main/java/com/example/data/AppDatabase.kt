package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CustomerDao
import com.example.data.dao.MenuItemDao
import com.example.data.dao.OrderDao
import com.example.data.dao.PaymentDao
import com.example.data.entity.Customer
import com.example.data.entity.MenuItem
import com.example.data.entity.Order
import com.example.data.entity.OrderItem
import com.example.data.entity.Payment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Customer::class,
        MenuItem::class,
        Order::class,
        OrderItem::class,
        Payment::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun orderDao(): OrderDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tuna_kaka_tea_stall.db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val itemDao = db.menuItemDao()
            val customerDao = db.customerDao()
            val orderDao = db.orderDao()
            val paymentDao = db.paymentDao()

            // 1. Prepopulate default menu items
            val defaultItems = listOf(
                MenuItem(name = "Tea", category = "Chai & Beverages", rate = 10.0),
                MenuItem(name = "Masala Tea", category = "Chai & Beverages", rate = 20.0),
                MenuItem(name = "Milk Tea", category = "Chai & Beverages", rate = 15.0),
                MenuItem(name = "Black Tea", category = "Chai & Beverages", rate = 15.0),
                MenuItem(name = "Coffee", category = "Chai & Beverages", rate = 25.0),
                MenuItem(name = "Cold Drink", category = "Chai & Beverages", rate = 25.0),
                MenuItem(name = "Water Bottle", category = "Chai & Beverages", rate = 20.0),
                MenuItem(name = "Samosa", category = "Snacks & Bites", rate = 15.0),
                MenuItem(name = "Pakoda", category = "Snacks & Bites", rate = 20.0),
                MenuItem(name = "Biscuit", category = "Bakery & Biscuits", rate = 10.0),
                MenuItem(name = "Cake", category = "Bakery & Biscuits", rate = 15.0),
                MenuItem(name = "Bread", category = "Bakery & Biscuits", rate = 20.0),
                MenuItem(name = "Egg", category = "Snacks & Bites", rate = 12.0),
                MenuItem(name = "Maggi", category = "Snacks & Bites", rate = 40.0),
                MenuItem(name = "Chips", category = "Snacks & Bites", rate = 10.0),
                MenuItem(name = "Other", category = "Others", rate = 10.0)
            )
            itemDao.insertAll(defaultItems)

            // 2. Prepopulate sample customers
            val now = System.currentTimeMillis()
            val oneDayMs = 24 * 60 * 60 * 1000L

            val c1Id = customerDao.insertCustomer(
                Customer(
                    customId = "CUST-1001",
                    name = "Ramesh",
                    mobile = "9876543210",
                    address = "Market Chowk, Shop #4",
                    createdAt = now - (5 * oneDayMs),
                    notes = "Regular morning chai customer"
                )
            )

            val c2Id = customerDao.insertCustomer(
                Customer(
                    customId = "CUST-1002",
                    name = "Suresh",
                    mobile = "9876543211",
                    address = "Station Road",
                    createdAt = now - (4 * oneDayMs),
                    notes = "Prefers Masala Tea and Samosa"
                )
            )

            val c3Id = customerDao.insertCustomer(
                Customer(
                    customId = "CUST-1003",
                    name = "Mohan",
                    mobile = "9876543212",
                    address = "Near Bus Stand",
                    createdAt = now - (3 * oneDayMs),
                    notes = "Evening customer"
                )
            )

            // 3. Prepopulate sample orders for today & recent days
            // Ramesh Order 1: Today
            val ord1 = orderDao.insertOrder(
                Order(
                    orderNumber = "ORD-1001",
                    customerId = c1Id,
                    customerName = "Ramesh",
                    customerMobile = "9876543210",
                    orderDate = now,
                    totalAmount = 80.0,
                    paidAmount = 50.0,
                    balanceAmount = 30.0,
                    paymentMethod = "Cash",
                    status = "PARTIAL",
                    notes = "Paid 50 cash, 30 pending"
                )
            )
            orderDao.insertOrderItems(
                listOf(
                    OrderItem(orderId = ord1, itemName = "Tea", quantity = 2, rate = 10.0, amount = 20.0),
                    OrderItem(orderId = ord1, itemName = "Masala Tea", quantity = 1, rate = 20.0, amount = 20.0),
                    OrderItem(orderId = ord1, itemName = "Samosa", quantity = 2, rate = 15.0, amount = 30.0),
                    OrderItem(orderId = ord1, itemName = "Biscuit", quantity = 1, rate = 10.0, amount = 10.0)
                )
            )
            paymentDao.insertPayment(
                Payment(
                    customerId = c1Id,
                    customerName = "Ramesh",
                    orderId = ord1,
                    paymentDate = now,
                    amount = 50.0,
                    paymentMethod = "Cash",
                    notes = "Order ORD-1001 down payment"
                )
            )

            // Suresh Order: Today, fully paid by UPI
            val ord2 = orderDao.insertOrder(
                Order(
                    orderNumber = "ORD-1002",
                    customerId = c2Id,
                    customerName = "Suresh",
                    customerMobile = "9876543211",
                    orderDate = now,
                    totalAmount = 180.0,
                    paidAmount = 180.0,
                    balanceAmount = 0.0,
                    paymentMethod = "UPI",
                    status = "PAID",
                    notes = "Settled via PhonePe UPI"
                )
            )
            orderDao.insertOrderItems(
                listOf(
                    OrderItem(orderId = ord2, itemName = "Masala Tea", quantity = 3, rate = 20.0, amount = 60.0),
                    OrderItem(orderId = ord2, itemName = "Samosa", quantity = 4, rate = 15.0, amount = 60.0),
                    OrderItem(orderId = ord2, itemName = "Pakoda", quantity = 3, rate = 20.0, amount = 60.0)
                )
            )
            paymentDao.insertPayment(
                Payment(
                    customerId = c2Id,
                    customerName = "Suresh",
                    orderId = ord2,
                    paymentDate = now,
                    amount = 180.0,
                    paymentMethod = "UPI",
                    notes = "Order ORD-1002 PhonePe UPI"
                )
            )

            // Mohan Order: Today, partial payment
            val ord3 = orderDao.insertOrder(
                Order(
                    orderNumber = "ORD-1003",
                    customerId = c3Id,
                    customerName = "Mohan",
                    customerMobile = "9876543212",
                    orderDate = now,
                    totalAmount = 350.0,
                    paidAmount = 250.0,
                    balanceAmount = 100.0,
                    paymentMethod = "Google Pay",
                    status = "PARTIAL",
                    notes = "Paid 250 via GPay, 100 balance"
                )
            )
            orderDao.insertOrderItems(
                listOf(
                    OrderItem(orderId = ord3, itemName = "Maggi", quantity = 2, rate = 40.0, amount = 80.0),
                    OrderItem(orderId = ord3, itemName = "Coffee", quantity = 4, rate = 25.0, amount = 100.0),
                    OrderItem(orderId = ord3, itemName = "Pakoda", quantity = 5, rate = 20.0, amount = 100.0),
                    OrderItem(orderId = ord3, itemName = "Cold Drink", quantity = 2, rate = 25.0, amount = 50.0),
                    OrderItem(orderId = ord3, itemName = "Chips", quantity = 2, rate = 10.0, amount = 20.0)
                )
            )
            paymentDao.insertPayment(
                Payment(
                    customerId = c3Id,
                    customerName = "Mohan",
                    orderId = ord3,
                    paymentDate = now,
                    amount = 250.0,
                    paymentMethod = "Google Pay",
                    notes = "Order ORD-1003 GPay"
                )
            )
        }
    }
}
