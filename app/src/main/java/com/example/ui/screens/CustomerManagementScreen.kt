package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomerWithSummary
import com.example.data.entity.Customer
import com.example.ui.theme.*
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerManagementScreen(
    customers: List<CustomerWithSummary>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onViewHistory: (Long) -> Unit,
    onSaveCustomer: (Long, String, String, String, String, String, () -> Unit) -> Unit,
    onDeleteCustomer: (Customer) -> Unit,
    onReceivePaymentForCustomer: (Customer) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    // Dialog state fields
    var dialogCustomId by remember { mutableStateOf("") }
    var dialogName by remember { mutableStateOf("") }
    var dialogMobile by remember { mutableStateOf("") }
    var dialogAddress by remember { mutableStateOf("") }
    var dialogNotes by remember { mutableStateOf("") }

    fun openAddDialog() {
        customerToEdit = null
        dialogCustomId = "CUST-${1000 + customers.size + 1}"
        dialogName = ""
        dialogMobile = ""
        dialogAddress = ""
        dialogNotes = ""
        showDialog = true
    }

    fun openEditDialog(c: Customer) {
        customerToEdit = c
        dialogCustomId = c.customId
        dialogName = c.name
        dialogMobile = c.mobile
        dialogAddress = c.address
        dialogNotes = c.notes
        showDialog = true
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { openAddDialog() },
                containerColor = ChaiPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("SAVE CUSTOMER", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("btn_add_customer")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ChaiBackgroundLight)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_customer_input"),
                placeholder = { Text("Search by Name, Mobile, Customer ID...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = ChaiPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChaiPrimary,
                    unfocusedBorderColor = ChaiBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Info Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${customers.size} Customers Total",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ChaiTextSecondary
                )
                val totalDue = customers.sumOf { it.balance }
                Text(
                    text = "Total Due: ${FormatUtils.formatRupees(totalDue)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (totalDue > 0) BalanceRed else PaidGreen
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (customers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonOutline,
                            contentDescription = null,
                            tint = ChaiTextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No customers found" else "No matching customers",
                            style = MaterialTheme.typography.bodyLarge,
                            color = ChaiTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 72.dp)
                ) {
                    items(customers, key = { it.customer.id }) { item ->
                        val c = item.customer
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = ChaiCardLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_card_${c.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Top row: Name, ID, Balance Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(ChaiCardWarm),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = c.name.take(1).uppercase(),
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = ChaiPrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = c.name,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = ChaiTextPrimary
                                            )
                                            Text(
                                                text = "${c.customId.ifBlank { "ID: #${c.id}" }} • ${c.mobile.ifBlank { "No Mobile" }}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ChaiTextSecondary
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        if (item.balance > 0) {
                                            Surface(
                                                color = BalanceRedLight,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "Bal: ${FormatUtils.formatRupees(item.balance)}",
                                                    color = BalanceRed,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Black
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        } else {
                                            Surface(
                                                color = PaidGreenLight,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "Settled",
                                                    color = PaidGreen,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (c.address.isNotBlank() || c.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = listOfNotNull(
                                            if (c.address.isNotBlank()) "📍 ${c.address}" else null,
                                            if (c.notes.isNotBlank()) "📝 ${c.notes}" else null
                                        ).joinToString(" | "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ChaiTextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = ChaiBorder.copy(alpha = 0.6f))
                                Spacer(modifier = Modifier.height(8.dp))

                                // Transaction summary numbers
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Orders: ${item.totalOrders}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = ChaiTextSecondary
                                        )
                                        Text(
                                            text = "Total: ${FormatUtils.formatRupees(item.totalAmount)}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = ChaiTextPrimary
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Paid: ${FormatUtils.formatRupees(item.totalPaid)}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = PaidGreen
                                        )
                                        Text(
                                            text = "Balance: ${FormatUtils.formatRupees(item.balance)}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (item.balance > 0) BalanceRed else ChaiTextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action Buttons Row: VIEW HISTORY, EDIT, DELETE
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { onViewHistory(c.id) },
                                        modifier = Modifier.weight(1.3f).height(38.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ChaiPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("VIEW HISTORY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (item.balance > 0) {
                                        FilledTonalButton(
                                            onClick = { onReceivePaymentForCustomer(c) },
                                            modifier = Modifier.weight(1.2f).height(38.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = PaidGreenLight,
                                                contentColor = PaidGreen
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("PAY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { openEditDialog(c) },
                                        modifier = Modifier.weight(0.9f).height(38.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("EDIT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    IconButton(
                                        onClick = { customerToDelete = c },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = BalanceRed
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

    // Add / Edit Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = if (customerToEdit == null) "New Customer" else "Edit Customer",
                    fontWeight = FontWeight.Bold,
                    color = ChaiPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dialogCustomId,
                        onValueChange = { dialogCustomId = it },
                        label = { Text("Customer ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dialogName,
                        onValueChange = { dialogName = it },
                        label = { Text("Customer Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dialogMobile,
                        onValueChange = { dialogMobile = it },
                        label = { Text("Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dialogAddress,
                        onValueChange = { dialogAddress = it },
                        label = { Text("Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dialogNotes,
                        onValueChange = { dialogNotes = it },
                        label = { Text("Notes") },
                        singleLine = false,
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dialogName.isNotBlank()) {
                            val id = customerToEdit?.id ?: 0L
                            onSaveCustomer(id, dialogCustomId, dialogName, dialogMobile, dialogAddress, dialogNotes) {
                                showDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChaiPrimary)
                ) {
                    Text("SAVE CUSTOMER", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Delete confirmation
    customerToDelete?.let { c ->
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("Delete Customer") },
            text = { Text("Are you sure you want to delete ${c.name}? All associated transaction history will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCustomer(c)
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BalanceRed)
                ) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("CANCEL")
                }
            }
        )
    }
}
