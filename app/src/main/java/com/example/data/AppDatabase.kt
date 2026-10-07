package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CustomerDao
import com.example.data.dao.CustomerLedgerDao
import com.example.data.dao.InventoryDao
import com.example.data.dao.MenuItemDao
import com.example.data.dao.OrderDao
import com.example.data.dao.PaymentDao
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Customer::class,
        MenuItem::class,
        Order::class,
        OrderItem::class,
        Payment::class,
        CustomerLedgerEntry::class,
        InventoryItem::class,
        InventoryConsumptionLog::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun orderDao(): OrderDao
    abstract fun paymentDao(): PaymentDao
    abstract fun customerLedgerDao(): CustomerLedgerDao
    abstract fun inventoryDao(): InventoryDao

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
                    .fallbackToDestructiveMigration()
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
            val ledgerDao = db.customerLedgerDao()
            val inventoryDao = db.inventoryDao()

            // 0. Prepopulate Raw Material Inventory
            val defaultInventory = listOf(
                InventoryItem(name = "Milk", unit = "Liters (L)", currentStock = 18.0, lowStockThreshold = 5.0, costPerUnit = 60.0),
                InventoryItem(name = "Tea Leaves (Chai Patti)", unit = "Kilograms (kg)", currentStock = 6.5, lowStockThreshold = 2.0, costPerUnit = 320.0),
                InventoryItem(name = "Sugar (Chini)", unit = "Kilograms (kg)", currentStock = 9.0, lowStockThreshold = 3.0, costPerUnit = 45.0),
                InventoryItem(name = "Fresh Ginger (Adrak)", unit = "Kilograms (kg)", currentStock = 2.5, lowStockThreshold = 1.0, costPerUnit = 120.0),
                InventoryItem(name = "Cardamom (Elaichi)", unit = "Grams (g)", currentStock = 200.0, lowStockThreshold = 50.0, costPerUnit = 2.5),
                InventoryItem(name = "Paper Cups (Kulhad)", unit = "Pieces (pcs)", currentStock = 450.0, lowStockThreshold = 100.0, costPerUnit = 0.8)
            )
            inventoryDao.insertAll(defaultInventory)

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

            // 3. Prepopulate sample orders and ledger entries for Ramesh
            // Ramesh Order 1: 2 days ago
            val ord1Time = now - (2 * oneDayMs)
            val ord1 = orderDao.insertOrder(
                Order(
                    orderNumber = "ORD-1001",
                    customerId = c1Id,
                    customerName = "Ramesh",
                    customerMobile = "9876543210",
                    orderDate = ord1Time,
                    totalAmount = 80.0,
                    paidAmount = 50.0,
                    balanceAmount = 30.0,
                    paymentMethod = "Cash",
                    status = "PARTIAL",
                    notes = "2 Tea + 1 Masala Tea + 2 Samosa + 1 Biscuit"
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
                    paymentDate = ord1Time,
                    amount = 50.0,
                    paymentMethod = "Cash",
                    notes = "Order ORD-1001 down payment"
                )
            )
            // Ledger: Debit 80, Credit 50
            ledgerDao.insertEntry(
                CustomerLedgerEntry(
                    customerId = c1Id,
                    date = ord1Time,
                    transactionType = "DEBIT",
                    amount = 80.0,
                    title = "Order #ORD-1001",
                    details = "Tea (2), Masala Tea (1), Samosa (2), Biscuit (1)",
                    paymentMode = "Credit Sale",
                    referenceNo = "ORD-1001"
                )
            )
            ledgerDao.insertEntry(
                CustomerLedgerEntry(
                    customerId = c1Id,
                    date = ord1Time,
                    transactionType = "CREDIT",
                    amount = 50.0,
                    title = "Cash Payment",
                    details = "Down payment for ORD-1001",
                    paymentMode = "Cash",
                    referenceNo = "ORD-1001"
                )
            )

            // Ramesh Order 2: Today
            val ord1b = orderDao.insertOrder(
                Order(
                    orderNumber = "ORD-1004",
                    customerId = c1Id,
                    customerName = "Ramesh",
                    customerMobile = "9876543210",
                    orderDate = now,
                    totalAmount = 50.0,
                    paidAmount = 20.0,
                    balanceAmount = 30.0,
                    paymentMethod = "Cash",
                    status = "PARTIAL",
                    notes = "Tea + Biscuit"
                )
            )
            orderDao.insertOrderItems(
                listOf(
                    OrderItem(orderId = ord1b, itemName = "Tea", quantity = 3, rate = 10.0, amount = 30.0),
                    OrderItem(orderId = ord1b, itemName = "Biscuit", quantity = 2, rate = 10.0, amount = 20.0)
                )
            )
            paymentDao.insertPayment(
                Payment(
                    customerId = c1Id,
                    customerName = "Ramesh",
                    orderId = ord1b,
                    paymentDate = now,
                    amount = 20.0,
                    paymentMethod = "Cash",
                    notes = "Order ORD-1004 partial payment"
                )
            )
            ledgerDao.insertEntry(
                CustomerLedgerEntry(
                    customerId = c1Id,
                    date = now,
                    transactionType = "DEBIT",
                    amount = 50.0,
                    title = "Order #ORD-1004",
                    details = "Tea (3), Biscuit (2)",
                    paymentMode = "Credit Sale",
                    referenceNo = "ORD-1004"
                )
            )
            ledgerDao.insertEntry(
                CustomerLedgerEntry(
                    customerId = c1Id,
                    date = now,
                    transactionType = "CREDIT",
                    amount = 20.0,
                    title = "Cash Payment",
                    details = "Partial settlement",
                    paymentMode = "Cash",
                    referenceNo = "ORD-1004"
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
            ledgerDao.insertEntry(
                CustomerLedgerEntry(
                    customerId = c2Id,
                    date = now,
                    transactionType = "DEBIT",
                    amount = 180.0,
                    title = "Order #ORD-1002",
                    details = "Masala Tea (3), Samosa (4), Pakoda (3)",
                    paymentMode = "Credit Sale",
                    referenceNo = "ORD-1002"
                )
            )
            ledgerDao.insertEntry(
                CustomerLedgerEntry(
                    customerId = c2Id,
                    date = now,
                    transactionType = "CREDIT",
                    amount = 180.0,
                    title = "PhonePe UPI Settlement",
                    details = "Instant UPI payment",
                    paymentMode = "UPI",
                    referenceNo = "ORD-1002"
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
            ledgerDao.insertEntry(
                CustomerLedgerEntry(
                    customerId = c3Id,
                    date = now,
                    transactionType = "DEBIT",
                    amount = 350.0,
                    title = "Order #ORD-1003",
                    details = "Maggi (2), Coffee (4), Pakoda (5), Cold Drink (2), Chips (2)",
                    paymentMode = "Credit Sale",
                    referenceNo = "ORD-1003"
                )
            )
            ledgerDao.insertEntry(
                CustomerLedgerEntry(
                    customerId = c3Id,
                    date = now,
                    transactionType = "CREDIT",
                    amount = 250.0,
                    title = "Google Pay Payment",
                    details = "GPay payment towards ORD-1003",
                    paymentMode = "Google Pay",
                    referenceNo = "ORD-1003"
                )
            )
        }
    }
}
