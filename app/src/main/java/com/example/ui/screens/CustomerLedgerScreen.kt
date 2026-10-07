package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomerWithSummary
import com.example.data.entity.CustomerLedgerEntry
import com.example.ui.CustomerLedgerData
import com.example.ui.LedgerItemDisplay
import com.example.ui.components.WhatsAppReportDialog
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.util.WhatsAppUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLedgerScreen(
    ledgerData: CustomerLedgerData,
    allCustomers: List<CustomerWithSummary>,
    onSelectCustomer: (Long) -> Unit,
    onAddDebit: (Long, Double, String, String, () -> Unit) -> Unit,
    onAddCredit: (Long, Double, String, String, String, () -> Unit) -> Unit,
    onDeleteEntry: (CustomerLedgerEntry) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var filterType by remember { mutableStateOf("ALL") } // ALL, DEBIT, CREDIT
    var showCustomerPicker by remember { mutableStateOf(false) }

    // Dialog state for adding Debit (You Gave)
    var showDebitDialog by remember { mutableStateOf(false) }
    var debitAmount by remember { mutableStateOf("") }
    var debitTitle by remember { mutableStateOf("Tea & Snacks") }
    var debitDetails by remember { mutableStateOf("") }

    // Dialog state for adding Credit (You Got)
    var showCreditDialog by remember { mutableStateOf(false) }
    var creditAmount by remember { mutableStateOf("") }
    var creditTitle by remember { mutableStateOf("Payment Received") }
    var creditDetails by remember { mutableStateOf("") }
    var creditMode by remember { mutableStateOf("Cash") }

    var showWhatsAppPreview by remember { mutableStateOf(false) }
    var whatsAppPreviewMessage by remember { mutableStateOf("") }

    var entryToDelete by remember { mutableStateOf<CustomerLedgerEntry?>(null) }

    val customerSummary = ledgerData.customerSummary
    val customer = customerSummary?.customer

    val filteredEntries = ledgerData.entries.filter { item ->
        when (filterType) {
            "DEBIT" -> item.entry.transactionType.equals("DEBIT", ignoreCase = true)
            "CREDIT" -> item.entry.transactionType.equals("CREDIT", ignoreCase = true)
            else -> true
        }
    }

    if (customer == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChaiBackgroundLight)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = ChaiPrimary,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Customer Ledger Book",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ChaiTextPrimary
                )
                Text(
                    text = "Select a customer to track credit & debits",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ChaiTextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showCustomerPicker = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ChaiPrimary)
                ) {
                    Icon(Icons.Default.PersonSearch, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Select Customer")
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ChaiBackgroundLight)
        ) {
            // Top Customer Profile Bar
            Surface(
                color = ChaiSurfaceLight,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showCustomerPicker = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ChaiCardWarm),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = customer.name.take(1).uppercase(),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = ChaiPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = customer.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = ChaiTextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch Customer",
                                    tint = ChaiPrimary
                                )
                            }
                            Text(
                                text = "${customer.customId} • ${customer.mobile.ifBlank { "No Mobile" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ChaiTextSecondary
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showCustomerPicker = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("SWITCH", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Balance & Credit/Debit Summary Card
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
                                Column {
                                    Text(
                                        text = "NET BALANCE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = ChaiTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = FormatUtils.formatRupees(Math.abs(ledgerData.netBalance)),
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = if (ledgerData.netBalance > 0) BalanceRed else PaidGreen
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (ledgerData.netBalance > 0) BalanceRedLight else PaidGreenLight
                                ) {
                                    Text(
                                        text = if (ledgerData.netBalance > 0) "DUE TO COLLECT" else "SETTLED / ADVANCE",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (ledgerData.netBalance > 0) BalanceRed else PaidGreen,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = ChaiBorder)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Total Debits vs Total Credits
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Debits Column (You Gave / Udhar)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(BalanceRedLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowOutward,
                                            contentDescription = "Debit",
                                            tint = BalanceRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("TOTAL DEBITS", style = MaterialTheme.typography.labelSmall, color = ChaiTextSecondary)
                                        Text(
                                            FormatUtils.formatRupees(ledgerData.totalDebit),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = BalanceRed
                                        )
                                    }
                                }

                                // Credits Column (You Got / Payments)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(PaidGreenLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CallReceived,
                                            contentDescription = "Credit",
                                            tint = PaidGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("TOTAL CREDITS", style = MaterialTheme.typography.labelSmall, color = ChaiTextSecondary)
                                        Text(
                                            FormatUtils.formatRupees(ledgerData.totalCredit),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = PaidGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Two Primary Action Buttons: + GAVE (DEBIT) and + GOT (CREDIT)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                debitAmount = ""
                                debitTitle = "Tea & Snacks"
                                debitDetails = ""
                                showDebitDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("btn_ledger_you_gave"),
                            colors = ButtonDefaults.buttonColors(containerColor = BalanceRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ArrowOutward, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("YOU GAVE (DEBIT)", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                creditAmount = if (ledgerData.netBalance > 0) ledgerData.netBalance.toInt().toString() else ""
                                creditTitle = "Payment Received"
                                creditDetails = ""
                                creditMode = "Cash"
                                showCreditDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("btn_ledger_you_got"),
                            colors = ButtonDefaults.buttonColors(containerColor = PaidGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CallReceived, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("YOU GOT (CREDIT)", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }

                // Dedicated Pre-filled WhatsApp Statement Button
                item {
                    Button(
                        onClick = {
                            val statementLines = ledgerData.entries.reversed().map { item ->
                                val isDeb = item.entry.transactionType.equals("DEBIT", ignoreCase = true)
                                val typeLabel = if (isDeb) "🔴 DEBIT" else "🟢 CREDIT"
                                "${FormatUtils.formatDate(item.entry.date)}: $typeLabel ${FormatUtils.formatRupees(item.entry.amount)} (${item.entry.title}) | Due: ${FormatUtils.formatRupees(item.runningBalance)}"
                            }
                            whatsAppPreviewMessage = WhatsAppUtils.generateCustomerBalanceAndSummaryMessage(
                                customerName = customer.name,
                                mobile = customer.mobile,
                                currentBalance = ledgerData.netBalance.coerceAtLeast(0.0),
                                totalPurchases = ledgerData.totalDebit,
                                totalPaid = ledgerData.totalCredit,
                                recentTransactions = statementLines
                            )
                            showWhatsAppPreview = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_share_ledger_whatsapp_prominent"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SHARE ON WHATSAPP (BALANCE & SUMMARY)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                // Filter Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Filter Chips: ALL, DEBITS, CREDITS
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = filterType == "ALL",
                                onClick = { filterType = "ALL" },
                                label = { Text("All (${ledgerData.entries.size})", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = filterType == "DEBIT",
                                onClick = { filterType = "DEBIT" },
                                label = { Text("Debits", fontSize = 11.sp, color = BalanceRed) }
                            )
                            FilterChip(
                                selected = filterType == "CREDIT",
                                onClick = { filterType = "CREDIT" },
                                label = { Text("Credits", fontSize = 11.sp, color = PaidGreen) }
                            )
                        }
                    }
                }

                // Entries Feed
                if (filteredEntries.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = ChaiCardLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (ledgerData.entries.isEmpty()) "No ledger entries yet. Use buttons above to record debits or credits." else "No entries matching filter",
                                    color = ChaiTextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredEntries, key = { it.entry.id }) { item ->
                        val entry = item.entry
                        val isDebit = entry.transactionType.equals("DEBIT", ignoreCase = true)

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = ChaiCardLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isDebit) BalanceRedLight else PaidGreenLight
                                        ) {
                                            Text(
                                                text = if (isDebit) "DEBIT (YOU GAVE)" else "CREDIT (YOU GOT)",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isDebit) BalanceRed else PaidGreen,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (entry.paymentMode.isNotBlank() && !isDebit) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = ChaiCardWarm
                                            ) {
                                                Text(
                                                    text = entry.paymentMode,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = ChaiPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Entry Amount
                                    Text(
                                        text = (if (isDebit) "+ " else "- ") + FormatUtils.formatRupees(entry.amount),
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                        color = if (isDebit) BalanceRed else PaidGreen
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = entry.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ChaiTextPrimary
                                )

                                if (entry.details.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = entry.details,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ChaiTextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = ChaiBorder.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = FormatUtils.formatDateTime(entry.date),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ChaiTextSecondary
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Running Bal: ${FormatUtils.formatRupees(item.runningBalance)}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (item.runningBalance > 0) BalanceRed else PaidGreen
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        IconButton(
                                            onClick = { entryToDelete = entry },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "Delete entry",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Customer Switcher Modal
    if (showCustomerPicker) {
        var searchCustomerText by remember { mutableStateOf("") }
        val filteredCusts = allCustomers.filter {
            searchCustomerText.isBlank() ||
                    it.customer.name.contains(searchCustomerText, ignoreCase = true) ||
                    it.customer.mobile.contains(searchCustomerText) ||
                    it.customer.customId.contains(searchCustomerText, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { showCustomerPicker = false },
            title = { Text("Select Customer Ledger", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchCustomerText,
                        onValueChange = { searchCustomerText = it },
                        placeholder = { Text("Search by name, mobile, id...") },
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
                        items(filteredCusts) { c ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectCustomer(c.customer.id)
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
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                } else {
                                    Text(
                                        "Settled",
                                        color = PaidGreen,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall
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

    // Add Debit (You Gave) Dialog
    if (showDebitDialog && customer != null) {
        AlertDialog(
            onDismissRequest = { showDebitDialog = false },
            title = {
                Text(
                    text = "🔴 Record Debit (You Gave)",
                    fontWeight = FontWeight.Bold,
                    color = BalanceRed
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Customer: ${customer.name}", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = debitAmount,
                        onValueChange = { debitAmount = it },
                        label = { Text("Amount (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = debitTitle,
                        onValueChange = { debitTitle = it },
                        label = { Text("Title / Items *") },
                        placeholder = { Text("e.g. 2 Tea + 2 Samosa") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = debitDetails,
                        onValueChange = { debitDetails = it },
                        label = { Text("Notes (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = debitAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0 && debitTitle.isNotBlank()) {
                            onAddDebit(customer.id, amt, debitTitle.trim(), debitDetails.trim()) {
                                showDebitDialog = false
                                Toast.makeText(context, "Debit of ${FormatUtils.formatRupees(amt)} recorded!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BalanceRed)
                ) {
                    Text("SAVE DEBIT", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDebitDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Add Credit (You Got) Dialog
    if (showCreditDialog && customer != null) {
        val paymentModes = listOf("Cash", "UPI", "PhonePe", "Google Pay", "Paytm", "Other")

        AlertDialog(
            onDismissRequest = { showCreditDialog = false },
            title = {
                Text(
                    text = "🟢 Record Credit (You Got)",
                    fontWeight = FontWeight.Bold,
                    color = PaidGreen
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Customer: ${customer.name}", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = creditAmount,
                        onValueChange = { creditAmount = it },
                        label = { Text("Amount Received (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Payment Mode:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ChaiTextSecondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        paymentModes.forEach { mode ->
                            FilterChip(
                                selected = creditMode == mode,
                                onClick = { creditMode = mode },
                                label = { Text(mode, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PaidGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = creditTitle,
                        onValueChange = { creditTitle = it },
                        label = { Text("Reference / Receipt") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = creditDetails,
                        onValueChange = { creditDetails = it },
                        label = { Text("Notes (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = creditAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onAddCredit(customer.id, amt, creditTitle.trim(), creditDetails.trim(), creditMode) {
                                showCreditDialog = false
                                Toast.makeText(context, "Credit of ${FormatUtils.formatRupees(amt)} recorded!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PaidGreen)
                ) {
                    Text("SAVE CREDIT", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreditDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Delete Entry Confirmation
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this ${entry.transactionType} entry of ${FormatUtils.formatRupees(entry.amount)}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteEntry(entry)
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BalanceRed)
                ) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("CANCEL")
                }
            }
        )
    }

    if (showWhatsAppPreview) {
        WhatsAppReportDialog(
            reportText = whatsAppPreviewMessage,
            onDismiss = { showWhatsAppPreview = false }
        )
    }
}
