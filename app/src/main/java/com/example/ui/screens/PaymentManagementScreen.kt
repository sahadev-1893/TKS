package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomerWithSummary
import com.example.data.entity.Customer
import com.example.data.entity.Payment
import com.example.ui.theme.*
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentManagementScreen(
    customers: List<CustomerWithSummary>,
    payments: List<Payment>,
    onRecordPayment: (Long, String, Double, String, String, () -> Unit) -> Unit,
    onViewCustomerLedger: (Long) -> Unit
) {
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<CustomerWithSummary?>(null) }
    var paymentAmountInput by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    var paymentNotesInput by remember { mutableStateOf("") }
    var paymentSearchQuery by remember { mutableStateOf("") }

    val paymentMethods = listOf("Cash", "UPI", "PhonePe", "Google Pay", "Paytm", "Other")

    fun openPaymentFor(cust: CustomerWithSummary) {
        selectedCustomer = cust
        paymentAmountInput = cust.balance.toInt().toString()
        selectedPaymentMethod = "Cash"
        paymentNotesInput = "Dues clearance"
        showPaymentDialog = true
    }

    val filteredPayments = payments.filter {
        paymentSearchQuery.isBlank() ||
                it.customerName.contains(paymentSearchQuery, ignoreCase = true) ||
                it.paymentMethod.contains(paymentSearchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val pendingCustomer = customers.firstOrNull { it.balance > 0 } ?: customers.firstOrNull()
                    if (pendingCustomer != null) {
                        openPaymentFor(pendingCustomer)
                    }
                },
                containerColor = PaidGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Payment, contentDescription = null) },
                text = { Text("RECEIVE PAYMENT", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("btn_record_payment")
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
            // Header summary
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PaidGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOTAL COLLECTIONS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = FormatUtils.formatRupees(payments.sumOf { it.amount }),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "${payments.size} payments collected across stall",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search payment history
            OutlinedTextField(
                value = paymentSearchQuery,
                onValueChange = { paymentSearchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search payment by customer or mode...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "PAYMENT HISTORY LOG (${filteredPayments.size})",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = ChaiTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredPayments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No payment records found", color = ChaiTextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 72.dp)
                ) {
                    items(filteredPayments, key = { it.id }) { pay ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = ChaiCardLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(PaidGreenLight, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = PaidGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = pay.customerName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = ChaiTextPrimary
                                        )
                                        Text(
                                            text = "${FormatUtils.formatDateTime(pay.paymentDate)} • ${pay.paymentMethod}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ChaiTextSecondary
                                        )
                                        if (pay.notes.isNotBlank()) {
                                            Text(
                                                text = pay.notes,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ChaiTextSecondary
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "+${FormatUtils.formatRupees(pay.amount)}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = PaidGreen
                                    )

                                    TextButton(
                                        onClick = { onViewCustomerLedger(pay.customerId) },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Ledger >", fontSize = 11.sp, color = ChaiPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Record Payment Dialog with Live Formula Math:
    // Balance = Previous Balance - New Payment
    if (showPaymentDialog && selectedCustomer != null) {
        val cust = selectedCustomer!!
        val prevBalance = cust.balance
        val newPaymentVal = paymentAmountInput.toDoubleOrNull() ?: 0.0
        val remainingBalance = (prevBalance - newPaymentVal).coerceAtLeast(0.0)

        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = {
                Text(
                    text = "Receive Payment",
                    fontWeight = FontWeight.Bold,
                    color = PaidGreen
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Customer dropdown / picker
                    Text(
                        text = "Customer: ${cust.customer.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ChaiTextPrimary
                    )

                    // Previous Balance display
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ChaiCardWarm),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Previous Balance:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    FormatUtils.formatRupees(prevBalance),
                                    fontWeight = FontWeight.Bold,
                                    color = BalanceRed
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("New Payment:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    FormatUtils.formatRupees(newPaymentVal),
                                    fontWeight = FontWeight.Bold,
                                    color = PaidGreen
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Remaining Balance:", fontWeight = FontWeight.Bold)
                                Text(
                                    FormatUtils.formatRupees(remainingBalance),
                                    fontWeight = FontWeight.Black,
                                    color = if (remainingBalance > 0) BalanceRed else PaidGreen
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = paymentAmountInput,
                        onValueChange = { paymentAmountInput = it },
                        label = { Text("Payment Amount (₹) *") },
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
                        paymentMethods.forEach { method ->
                            FilterChip(
                                selected = selectedPaymentMethod == method,
                                onClick = { selectedPaymentMethod = method },
                                label = { Text(method, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PaidGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = paymentNotesInput,
                        onValueChange = { paymentNotesInput = it },
                        label = { Text("Notes (e.g. UTR / Cashier)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPaymentVal > 0) {
                            onRecordPayment(
                                cust.customer.id,
                                cust.customer.name,
                                newPaymentVal,
                                selectedPaymentMethod,
                                paymentNotesInput
                            ) {
                                showPaymentDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PaidGreen)
                ) {
                    Text("RECORD PAYMENT", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}
