package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomerWithSummary
import com.example.data.entity.Customer
import com.example.data.entity.MenuItem
import com.example.ui.OrderItemDraft
import com.example.ui.theme.*
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderEntryScreen(
    customers: List<CustomerWithSummary>,
    menuItems: List<MenuItem>,
    selectedCustomerId: Long?,
    customerName: String,
    customerMobile: String,
    draftItems: List<OrderItemDraft>,
    paidAmount: String,
    paymentMethod: String,
    onSelectCustomer: (Customer) -> Unit,
    onSetCustomerManual: (String, String) -> Unit,
    onAddDraftItem: (String, Double, Int) -> Unit,
    onRemoveDraftItem: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onUpdateRate: (String, Double) -> Unit,
    onSetPaidAmount: (String) -> Unit,
    onSetPaymentMethod: (String) -> Unit,
    onSubmitOrder: ((Long) -> Unit) -> Unit,
    onOrderSuccess: (Long) -> Unit
) {
    var showCustomerPicker by remember { mutableStateOf(false) }
    var showAddItemSheet by remember { mutableStateOf(false) }
    var selectedItemForAdd by remember { mutableStateOf<MenuItem?>(null) }
    var customItemName by remember { mutableStateOf("") }
    var customItemRate by remember { mutableStateOf("10") }
    var customItemQty by remember { mutableStateOf("1") }

    val subtotal = draftItems.sumOf { it.amount }
    val paidVal = paidAmount.toDoubleOrNull() ?: 0.0
    val balance = (subtotal - paidVal).coerceAtLeast(0.0)

    val paymentMethods = listOf("Cash", "UPI", "PhonePe", "Google Pay", "Paytm", "Other")

    Scaffold(
        bottomBar = {
            Surface(
                color = Color.White,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL AMOUNT",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ChaiTextSecondary
                            )
                            Text(
                                text = FormatUtils.formatRupees(subtotal),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                                color = ChaiPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "BALANCE DUE",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ChaiTextSecondary
                            )
                            Text(
                                text = FormatUtils.formatRupees(balance),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                                color = if (balance > 0) BalanceRed else PaidGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (draftItems.isNotEmpty()) {
                                onSubmitOrder { orderId ->
                                    onOrderSuccess(orderId)
                                }
                            }
                        },
                        enabled = draftItems.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_save_order"),
                        colors = ButtonDefaults.buttonColors(containerColor = ChaiPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SAVE & COMPLETE ORDER",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ChaiBackgroundLight),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order Date & Customer Header Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ChaiCardLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NEW ORDER DETAILS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = ChaiTextSecondary
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ChaiCardWarm
                            ) {
                                Text(
                                    text = FormatUtils.formatDate(System.currentTimeMillis()),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ChaiPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Customer Selection & Quick Entry
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { onSetCustomerManual(it, customerMobile) },
                                label = { Text("Customer Name *") },
                                placeholder = { Text("e.g. Ramesh or Walk-in") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("order_customer_name"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Button(
                                onClick = { showCustomerPicker = true },
                                modifier = Modifier
                                    .height(56.dp)
                                    .testTag("btn_select_customer"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ChaiSecondary)
                            ) {
                                Icon(Icons.Default.PersonSearch, contentDescription = "Pick")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SELECT")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = customerMobile,
                            onValueChange = { onSetCustomerManual(customerName, it) },
                            label = { Text("Customer Mobile (Optional)") },
                            placeholder = { Text("10-digit mobile number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("order_customer_mobile"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Quick Add Item Bar
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUICK ADD POPULAR ITEMS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = ChaiTextSecondary
                        )
                        TextButton(onClick = { showAddItemSheet = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Custom Item", fontWeight = FontWeight.Bold, color = ChaiPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val quickItems = listOf(
                        Pair("Tea", 10.0),
                        Pair("Masala Tea", 20.0),
                        Pair("Samosa", 15.0),
                        Pair("Biscuit", 10.0),
                        Pair("Coffee", 25.0),
                        Pair("Pakoda", 20.0),
                        Pair("Maggi", 40.0)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickItems.forEach { (name, rate) ->
                            SuggestionChip(
                                onClick = { onAddDraftItem(name, rate, 1) },
                                label = {
                                    Text(
                                        "$name ₹${rate.toInt()}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = ChaiCardWarm,
                                    labelColor = ChaiPrimaryDark
                                )
                            )
                        }
                    }
                }
            }

            // Items List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ORDER ITEMS (${draftItems.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = ChaiTextSecondary
                    )

                    Button(
                        onClick = { showAddItemSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ChaiPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ ADD ITEM", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (draftItems.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ChaiCardLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = ChaiTextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No items added yet", color = ChaiTextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Tap quick items above or click + ADD ITEM",
                                style = MaterialTheme.typography.bodySmall,
                                color = ChaiTextSecondary
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(draftItems) { index, draft ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ChaiCardLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Item name and rate edit
                            Column(modifier = Modifier.weight(1.3f)) {
                                Text(
                                    text = draft.itemName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ChaiTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Rate: ₹${draft.rate.toInt()} each",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ChaiTextSecondary
                                )
                            }

                            // Quantity Stepper: [-] [Qty] [+]
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = ChaiCardWarm,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { onUpdateQuantity(draft.id, -1) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("-", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ChaiPrimary)
                                    }
                                }

                                Text(
                                    text = "${draft.quantity}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    color = ChaiTextPrimary
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = ChaiCardWarm,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { onUpdateQuantity(draft.id, 1) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("+", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ChaiPrimary)
                                    }
                                }
                            }

                            // Total Amount and Remove
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = FormatUtils.formatRupees(draft.amount),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = ChaiTextPrimary
                                    )
                                    Text(
                                        text = "${draft.quantity} × ₹${draft.rate.toInt()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ChaiTextSecondary
                                    )
                                }

                                IconButton(
                                    onClick = { onRemoveDraftItem(draft.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove item",
                                        tint = BalanceRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Payment section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ChaiCardLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PAYMENT & BALANCE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = ChaiTextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick buttons: "FULL PAID", "UNPAID", "HALF"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { onSetPaidAmount(subtotal.toInt().toString()) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = PaidGreenLight,
                                    contentColor = PaidGreen
                                )
                            ) {
                                Text("FULL PAID", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            FilledTonalButton(
                                onClick = { onSetPaidAmount("0") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = BalanceRedLight,
                                    contentColor = BalanceRed
                                )
                            ) {
                                Text("UNPAID", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = paidAmount,
                                onValueChange = onSetPaidAmount,
                                label = { Text("Paid Amount (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("order_paid_amount"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            // Calculated balance indicator
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (balance > 0) BalanceRedLight else PaidGreenLight,
                                modifier = Modifier.weight(1f).height(56.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (balance > 0) "Balance Due" else "Fully Settled",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (balance > 0) BalanceRed else PaidGreen
                                    )
                                    Text(
                                        text = FormatUtils.formatRupees(balance),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = if (balance > 0) BalanceRed else PaidGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Payment Method:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ChaiTextSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            paymentMethods.forEach { method ->
                                FilterChip(
                                    selected = paymentMethod == method,
                                    onClick = { onSetPaymentMethod(method) },
                                    label = { Text(method, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ChaiPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Customer Picker Dialog
    if (showCustomerPicker) {
        var pickerSearch by remember { mutableStateOf("") }
        val filtered = customers.filter {
            pickerSearch.isBlank() ||
                    it.customer.name.contains(pickerSearch, ignoreCase = true) ||
                    it.customer.mobile.contains(pickerSearch)
        }

        AlertDialog(
            onDismissRequest = { showCustomerPicker = false },
            title = { Text("Select Customer", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = pickerSearch,
                        onValueChange = { pickerSearch = it },
                        placeholder = { Text("Search customer...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(filtered.size) { idx ->
                            val c = filtered[idx]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectCustomer(c.customer)
                                        showCustomerPicker = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(c.customer.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${c.customer.customId} • ${c.customer.mobile}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ChaiTextSecondary
                                    )
                                }
                                if (c.balance > 0) {
                                    Text(
                                        "Due: ${FormatUtils.formatRupees(c.balance)}",
                                        color = BalanceRed,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                            HorizontalDivider(color = ChaiBorder.copy(alpha = 0.5f))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomerPicker = false }) {
                    Text("CLOSE")
                }
            }
        )
    }

    // Add Item Dialog / Sheet
    if (showAddItemSheet) {
        AlertDialog(
            onDismissRequest = { showAddItemSheet = false },
            title = { Text("+ Add Item to Order", fontWeight = FontWeight.Bold, color = ChaiPrimary) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Select from Menu Master:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ChaiTextSecondary
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                    ) {
                        items(menuItems.size) { idx ->
                            val itm = menuItems[idx]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedItemForAdd = itm
                                        customItemName = itm.name
                                        customItemRate = itm.rate.toInt().toString()
                                    }
                                    .background(if (selectedItemForAdd?.id == itm.id) ChaiCardWarm else Color.Transparent)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(itm.name, fontWeight = FontWeight.Medium)
                                Text("₹${itm.rate.toInt()}", fontWeight = FontWeight.Bold, color = ChaiPrimary)
                            }
                        }
                    }

                    HorizontalDivider(color = ChaiBorder)

                    OutlinedTextField(
                        value = customItemName,
                        onValueChange = { customItemName = it },
                        label = { Text("Item Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customItemQty,
                            onValueChange = { customItemQty = it },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = customItemRate,
                            onValueChange = { customItemRate = it },
                            label = { Text("Rate (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = customItemQty.toIntOrNull() ?: 1
                        val rate = customItemRate.toDoubleOrNull() ?: 10.0
                        if (customItemName.isNotBlank() && qty > 0) {
                            onAddDraftItem(customItemName.trim(), rate, qty)
                            showAddItemSheet = false
                            customItemName = ""
                            customItemQty = "1"
                            selectedItemForAdd = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChaiPrimary)
                ) {
                    Text("ADD ITEM", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemSheet = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}
