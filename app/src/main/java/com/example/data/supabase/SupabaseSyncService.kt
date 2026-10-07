package com.example.data.supabase

import com.example.data.AppDatabase
import com.example.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class SupabaseSyncResult {
    data class Success(val message: String, val syncedCounts: Map<String, Int>) : SupabaseSyncResult()
    data class Error(val error: String) : SupabaseSyncResult()
}

class SupabaseSyncService(
    private val config: SupabaseConfig,
    private val database: AppDatabase
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getSanitizedBaseUrl(): String {
        var url = config.supabaseUrl.trim().trimEnd('/')
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        return url
    }

    suspend fun testConnection(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!config.isConfigured) {
            return@withContext Pair(false, "Supabase Project URL and API Key must not be empty.")
        }

        try {
            val baseUrl = getSanitizedBaseUrl()
            val url = "$baseUrl/rest/v1/"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", config.supabaseKey)
                .addHeader("Authorization", "Bearer ${config.supabaseKey}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.code in 200..299 || response.code == 404) {
                    // Test if tables exist
                    val tableCheckUrl = "$baseUrl/rest/v1/customers?select=id&limit=1"
                    val tableRequest = Request.Builder()
                        .url(tableCheckUrl)
                        .addHeader("apikey", config.supabaseKey)
                        .addHeader("Authorization", "Bearer ${config.supabaseKey}")
                        .get()
                        .build()

                    var tablesExist = false
                    try {
                        client.newCall(tableRequest).execute().use { tableResponse ->
                            tablesExist = tableResponse.isSuccessful
                        }
                    } catch (_: Exception) {}

                    config.isConnected = true
                    if (tablesExist) {
                        Pair(true, "Successfully connected! Supabase project & tables are ready.")
                    } else {
                        Pair(true, "Connected to Supabase! Note: Database tables are not yet created. Copy and run the SQL schema script below in the Supabase SQL Editor.")
                    }
                } else if (response.code == 401) {
                    config.isConnected = false
                    Pair(false, "Authentication Failed (HTTP 401): Invalid Supabase API Key. Please verify your Anon key in Supabase Settings > API.")
                } else {
                    config.isConnected = false
                    Pair(false, "Connection failed: HTTP ${response.code} (${response.message})")
                }
            }
        } catch (e: Exception) {
            config.isConnected = false
            Pair(false, "Connection error: ${e.localizedMessage ?: "Network error"}")
        }
    }

    /**
     * Upload / Push all local Room SQLite data to Supabase PostgreSQL (Upsert).
     */
    suspend fun syncAllData(): SupabaseSyncResult = withContext(Dispatchers.IO) {
        if (!config.isConfigured) {
            return@withContext SupabaseSyncResult.Error("Please configure Supabase URL and Key first.")
        }

        try {
            val counts = mutableMapOf<String, Int>()

            // 1. Sync Customers
            val customers = database.customerDao().getAllCustomers().first()
            if (customers.isNotEmpty()) {
                val array = JSONArray()
                customers.forEach { c ->
                    val obj = JSONObject().apply {
                        put("id", c.id)
                        put("custom_id", c.customId)
                        put("name", c.name)
                        put("mobile", c.mobile)
                        put("address", c.address)
                        put("notes", c.notes)
                        put("created_at", c.createdAt)
                    }
                    array.put(obj)
                }
                upsertTable("customers", array)
                counts["Customers"] = customers.size
            }

            // 2. Sync Menu Items
            val menuItems = database.menuItemDao().getAllMenuItems().first()
            if (menuItems.isNotEmpty()) {
                val array = JSONArray()
                menuItems.forEach { m ->
                    val obj = JSONObject().apply {
                        put("id", m.id)
                        put("name", m.name)
                        put("category", m.category)
                        put("rate", m.rate)
                        put("is_active", m.isActive)
                    }
                    array.put(obj)
                }
                upsertTable("menu_items", array)
                counts["Menu Items"] = menuItems.size
            }

            // 3. Sync Orders
            val orders = database.orderDao().getAllOrders().first()
            if (orders.isNotEmpty()) {
                val array = JSONArray()
                orders.forEach { o ->
                    val obj = JSONObject().apply {
                        put("id", o.id)
                        put("order_number", o.orderNumber)
                        put("customer_id", o.customerId)
                        put("customer_name", o.customerName)
                        put("customer_mobile", o.customerMobile)
                        put("order_date", o.orderDate)
                        put("total_amount", o.totalAmount)
                        put("paid_amount", o.paidAmount)
                        put("balance_amount", o.balanceAmount)
                        put("payment_method", o.paymentMethod)
                        put("status", o.status)
                        put("notes", o.notes)
                    }
                    array.put(obj)
                }
                upsertTable("orders", array)
                counts["Orders"] = orders.size
            }

            // 4. Sync Payments
            val payments = database.paymentDao().getAllPayments().first()
            if (payments.isNotEmpty()) {
                val array = JSONArray()
                payments.forEach { p ->
                    val obj = JSONObject().apply {
                        put("id", p.id)
                        put("customer_id", p.customerId)
                        put("customer_name", p.customerName)
                        put("order_id", p.orderId ?: JSONObject.NULL)
                        put("payment_date", p.paymentDate)
                        put("amount", p.amount)
                        put("payment_method", p.paymentMethod)
                        put("notes", p.notes)
                    }
                    array.put(obj)
                }
                upsertTable("payments", array)
                counts["Payments"] = payments.size
            }

            // 5. Sync Customer Ledger
            val ledgerEntries = database.customerLedgerDao().getAllEntries().first()
            if (ledgerEntries.isNotEmpty()) {
                val array = JSONArray()
                ledgerEntries.forEach { l ->
                    val obj = JSONObject().apply {
                        put("id", l.id)
                        put("customer_id", l.customerId)
                        put("date", l.date)
                        put("transaction_type", l.transactionType)
                        put("amount", l.amount)
                        put("title", l.title)
                        put("details", l.details)
                        put("payment_mode", l.paymentMode)
                        put("reference_no", l.referenceNo)
                    }
                    array.put(obj)
                }
                upsertTable("customer_ledger", array)
                counts["Ledger Entries"] = ledgerEntries.size
            }

            // 6. Sync Raw Material Inventory
            val inventoryItems = database.inventoryDao().getAllInventoryItems().first()
            if (inventoryItems.isNotEmpty()) {
                val array = JSONArray()
                inventoryItems.forEach { i ->
                    val obj = JSONObject().apply {
                        put("id", i.id)
                        put("name", i.name)
                        put("unit", i.unit)
                        put("current_stock", i.currentStock)
                        put("low_stock_threshold", i.lowStockThreshold)
                        put("cost_per_unit", i.costPerUnit)
                        put("last_updated", i.lastUpdated)
                    }
                    array.put(obj)
                }
                upsertTable("inventory_items", array)
                counts["Raw Materials"] = inventoryItems.size
            }

            config.lastSyncTimestamp = System.currentTimeMillis()
            config.isConnected = true
            SupabaseSyncResult.Success("Cloud backup completed successfully!", counts)
        } catch (e: Exception) {
            SupabaseSyncResult.Error("Sync failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Download / Pull all cloud records from Supabase into local Room database (Bi-directional sync / Restore).
     */
    suspend fun pullAllDataFromCloud(): SupabaseSyncResult = withContext(Dispatchers.IO) {
        if (!config.isConfigured) {
            return@withContext SupabaseSyncResult.Error("Please configure Supabase URL and Key first.")
        }

        try {
            val counts = mutableMapOf<String, Int>()

            // 1. Pull Customers
            val customersArray = fetchTable("customers")
            for (i in 0 until customersArray.length()) {
                val obj = customersArray.getJSONObject(i)
                val customer = Customer(
                    id = obj.optLong("id", 0L),
                    customId = obj.optString("custom_id", ""),
                    name = obj.optString("name", "Customer"),
                    mobile = obj.optString("mobile", ""),
                    address = obj.optString("address", ""),
                    notes = obj.optString("notes", ""),
                    createdAt = obj.optLong("created_at", System.currentTimeMillis())
                )
                database.customerDao().insertCustomer(customer)
            }
            counts["Customers"] = customersArray.length()

            // 2. Pull Menu Items
            val menuArray = fetchTable("menu_items")
            for (i in 0 until menuArray.length()) {
                val obj = menuArray.getJSONObject(i)
                val item = MenuItem(
                    id = obj.optLong("id", 0L),
                    name = obj.optString("name", "Item"),
                    category = obj.optString("category", "General"),
                    rate = obj.optDouble("rate", 10.0),
                    isActive = obj.optBoolean("is_active", true)
                )
                database.menuItemDao().insertMenuItem(item)
            }
            counts["Menu Items"] = menuArray.length()

            // 3. Pull Orders
            val ordersArray = fetchTable("orders")
            for (i in 0 until ordersArray.length()) {
                val obj = ordersArray.getJSONObject(i)
                val order = Order(
                    id = obj.optLong("id", 0L),
                    orderNumber = obj.optString("order_number", ""),
                    customerId = obj.optLong("customer_id", 0L),
                    customerName = obj.optString("customer_name", ""),
                    customerMobile = obj.optString("customer_mobile", ""),
                    orderDate = obj.optLong("order_date", System.currentTimeMillis()),
                    totalAmount = obj.optDouble("total_amount", 0.0),
                    paidAmount = obj.optDouble("paid_amount", 0.0),
                    balanceAmount = obj.optDouble("balance_amount", 0.0),
                    paymentMethod = obj.optString("payment_method", "Cash"),
                    status = obj.optString("status", "COMPLETED"),
                    notes = obj.optString("notes", "")
                )
                database.orderDao().insertOrder(order)
            }
            counts["Orders"] = ordersArray.length()

            // 4. Pull Payments
            val paymentsArray = fetchTable("payments")
            for (i in 0 until paymentsArray.length()) {
                val obj = paymentsArray.getJSONObject(i)
                val orderId = if (obj.has("order_id") && !obj.isNull("order_id")) obj.optLong("order_id") else null
                val payment = Payment(
                    id = obj.optLong("id", 0L),
                    customerId = obj.optLong("customer_id", 0L),
                    customerName = obj.optString("customer_name", ""),
                    orderId = orderId,
                    paymentDate = obj.optLong("payment_date", System.currentTimeMillis()),
                    amount = obj.optDouble("amount", 0.0),
                    paymentMethod = obj.optString("payment_method", "Cash"),
                    notes = obj.optString("notes", "")
                )
                database.paymentDao().insertPayment(payment)
            }
            counts["Payments"] = paymentsArray.length()

            // 5. Pull Customer Ledger
            val ledgerArray = fetchTable("customer_ledger")
            for (i in 0 until ledgerArray.length()) {
                val obj = ledgerArray.getJSONObject(i)
                val entry = CustomerLedgerEntry(
                    id = obj.optLong("id", 0L),
                    customerId = obj.optLong("customer_id", 0L),
                    date = obj.optLong("date", System.currentTimeMillis()),
                    transactionType = obj.optString("transaction_type", "DEBIT"),
                    amount = obj.optDouble("amount", 0.0),
                    title = obj.optString("title", ""),
                    details = obj.optString("details", ""),
                    paymentMode = obj.optString("payment_mode", "Cash"),
                    referenceNo = obj.optString("reference_no", "")
                )
                database.customerLedgerDao().insertEntry(entry)
            }
            counts["Ledger Entries"] = ledgerArray.length()

            // 6. Pull Inventory Items
            val inventoryArray = fetchTable("inventory_items")
            for (i in 0 until inventoryArray.length()) {
                val obj = inventoryArray.getJSONObject(i)
                val inv = InventoryItem(
                    id = obj.optLong("id", 0L),
                    name = obj.optString("name", ""),
                    unit = obj.optString("unit", "kg"),
                    currentStock = obj.optDouble("current_stock", 0.0),
                    lowStockThreshold = obj.optDouble("low_stock_threshold", 2.0),
                    costPerUnit = obj.optDouble("cost_per_unit", 0.0),
                    lastUpdated = obj.optLong("last_updated", System.currentTimeMillis())
                )
                database.inventoryDao().insertItem(inv)
            }
            counts["Raw Materials"] = inventoryArray.length()

            config.lastSyncTimestamp = System.currentTimeMillis()
            config.isConnected = true
            SupabaseSyncResult.Success("Cloud data downloaded and restored successfully!", counts)
        } catch (e: Exception) {
            SupabaseSyncResult.Error("Download from cloud failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private fun upsertTable(tableName: String, jsonArray: JSONArray) {
        val baseUrl = getSanitizedBaseUrl()
        val url = "$baseUrl/rest/v1/$tableName"
        val body = jsonArray.toString().toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", config.supabaseKey)
            .addHeader("Authorization", "Bearer ${config.supabaseKey}")
            .addHeader("Prefer", "resolution=merge-duplicates")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful && response.code != 201 && response.code != 200) {
                val errorBody = response.body?.string() ?: ""
                throw RuntimeException("Supabase $tableName sync failed (HTTP ${response.code}): $errorBody")
            }
        }
    }

    private fun fetchTable(tableName: String): JSONArray {
        val baseUrl = getSanitizedBaseUrl()
        val url = "$baseUrl/rest/v1/$tableName?select=*"

        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", config.supabaseKey)
            .addHeader("Authorization", "Bearer ${config.supabaseKey}")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                throw RuntimeException("Fetch $tableName failed (HTTP ${response.code}): $errorBody")
            }
            val responseBody = response.body?.string() ?: "[]"
            return JSONArray(responseBody)
        }
    }

    companion object {
        fun generatePostgresSqlSchema(): String {
            return """
-- =========================================================================
-- TUNA KAKA TEA STALL: SUPABASE POSTGRESQL SCHEMA
-- Copy & run this script in your Supabase project's SQL Editor
-- (Dashboard > SQL Editor > New query > Paste & Run)
-- =========================================================================

-- 1. Customers Table
CREATE TABLE IF NOT EXISTS public.customers (
    id BIGINT PRIMARY KEY,
    custom_id TEXT,
    name TEXT NOT NULL,
    mobile TEXT,
    address TEXT,
    notes TEXT,
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_customers_name ON public.customers(name);
CREATE INDEX IF NOT EXISTS idx_customers_mobile ON public.customers(mobile);

-- 2. Menu Items Table
CREATE TABLE IF NOT EXISTS public.menu_items (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL,
    category TEXT,
    rate NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 3. Orders Table
CREATE TABLE IF NOT EXISTS public.orders (
    id BIGINT PRIMARY KEY,
    order_number TEXT,
    customer_id BIGINT,
    customer_name TEXT,
    customer_mobile TEXT,
    order_date BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    total_amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    paid_amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    balance_amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    payment_method TEXT DEFAULT 'Cash',
    status TEXT DEFAULT 'COMPLETED',
    notes TEXT
);

CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON public.orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_order_date ON public.orders(order_date);

-- 4. Order Items Table
CREATE TABLE IF NOT EXISTS public.order_items (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    order_id BIGINT,
    item_name TEXT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    rate NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00
);

-- 5. Payments Table
CREATE TABLE IF NOT EXISTS public.payments (
    id BIGINT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    customer_name TEXT,
    order_id BIGINT,
    payment_date BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    payment_method TEXT DEFAULT 'Cash',
    notes TEXT
);

CREATE INDEX IF NOT EXISTS idx_payments_customer_id ON public.payments(customer_id);

-- 6. Customer Ledger Table (Credits & Debits)
CREATE TABLE IF NOT EXISTS public.customer_ledger (
    id BIGINT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    date BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    transaction_type TEXT NOT NULL,
    amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    title TEXT,
    details TEXT,
    payment_mode TEXT,
    reference_no TEXT
);

CREATE INDEX IF NOT EXISTS idx_ledger_customer_id ON public.customer_ledger(customer_id);

-- 7. Inventory Items (Raw Materials: Milk, Tea, Sugar)
CREATE TABLE IF NOT EXISTS public.inventory_items (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL,
    unit TEXT DEFAULT 'kg',
    current_stock NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    low_stock_threshold NUMERIC(10, 2) NOT NULL DEFAULT 2.00,
    cost_per_unit NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    last_updated BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

-- 8. Inventory Logs
CREATE TABLE IF NOT EXISTS public.inventory_logs (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    item_id BIGINT,
    item_name TEXT NOT NULL,
    quantity NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    unit TEXT,
    date BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    type TEXT,
    remaining_stock_after NUMERIC(10, 2),
    notes TEXT
);

-- Enable Row Level Security (RLS)
ALTER TABLE public.customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.menu_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.customer_ledger ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.inventory_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.inventory_logs ENABLE ROW LEVEL SECURITY;

-- Allow anon public read and write access for mobile client sync
DROP POLICY IF EXISTS "Allow anon all on customers" ON public.customers;
CREATE POLICY "Allow anon all on customers" ON public.customers FOR ALL USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Allow anon all on menu_items" ON public.menu_items;
CREATE POLICY "Allow anon all on menu_items" ON public.menu_items FOR ALL USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Allow anon all on orders" ON public.orders;
CREATE POLICY "Allow anon all on orders" ON public.orders FOR ALL USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Allow anon all on order_items" ON public.order_items;
CREATE POLICY "Allow anon all on order_items" ON public.order_items FOR ALL USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Allow anon all on payments" ON public.payments;
CREATE POLICY "Allow anon all on payments" ON public.payments FOR ALL USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Allow anon all on customer_ledger" ON public.customer_ledger;
CREATE POLICY "Allow anon all on customer_ledger" ON public.customer_ledger FOR ALL USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Allow anon all on inventory_items" ON public.inventory_items;
CREATE POLICY "Allow anon all on inventory_items" ON public.inventory_items FOR ALL USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Allow anon all on inventory_logs" ON public.inventory_logs;
CREATE POLICY "Allow anon all on inventory_logs" ON public.inventory_logs FOR ALL USING (true) WITH CHECK (true);
            """.trimIndent()
        }
    }
}
