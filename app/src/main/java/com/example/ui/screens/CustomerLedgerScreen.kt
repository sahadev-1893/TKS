package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomerWithSummary
import com.example.data.entity.Payment
import com.example.ui.LedgerRow
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.util.WhatsAppUtils

@Composable
fun CustomerLedgerScreen(
    customerSummary: CustomerWithSummary?,
    ledgerRows: List<LedgerRow>,
    payments: List<Payment>,
    onBack: () -> Unit,
    onReceivePayment: (CustomerWithSummary) -> Unit
) {
    val context = LocalContext.current

    if (customerSummary == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(ChaiBackgroundLight),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No customer selected", color = ChaiTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBack) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    val customer = customerSummary.customer
    val totalAmount = customerSummary.totalAmount
    val totalPaid = customerSummary.totalPaid
    val outstanding = customerSummary.balance

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ChaiBackgroundLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Customer Profile Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
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
                                Text(
                                    text = "CUSTOMER: ${customer.name.uppercase()}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = ChaiTextPrimary
                                )
                                Text(
                                    text = "${customer.customId} • ${customer.mobile.ifBlank { "No Mobile" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ChaiTextSecondary
                                )
                            }
                        }

                        if (outstanding > 0) {
                            Surface(
                                color = BalanceRedLight,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "DUE: ${FormatUtils.formatRupees(outstanding)}",
                                    color = BalanceRed,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Surface(
                                color = PaidGreenLight,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "ALL SETTLED",
                                    color = PaidGreen,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (customer.address.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📍 ${customer.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ChaiTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = ChaiBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Ledger Overall Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Purchases", style = MaterialTheme.typography.labelSmall, color = ChaiTextSecondary)
                            Text(
                                FormatUtils.formatRupees(totalAmount),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ChaiTextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Paid", style = MaterialTheme.typography.labelSmall, color = ChaiTextSecondary)
                            Text(
                                FormatUtils.formatRupees(totalPaid),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PaidGreen
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Outstanding", style = MaterialTheme.typography.labelSmall, color = ChaiTextSecondary)
                            Text(
                                FormatUtils.formatRupees(outstanding),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = if (outstanding > 0) BalanceRed else PaidGreen
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons Row: PRINT LEDGER, DOWNLOAD PDF, SHARE ON WHATSAPP
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Printing Ledger for ${customer.name}...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PRINT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Ledger statement downloaded for ${customer.name}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1.1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("DOWNLOAD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val lines = ledgerRows.map {
                            "${FormatUtils.formatDate(it.date)}: ${it.description} | Total: ${FormatUtils.formatRupees(it.totalAmount)} | Paid: ${FormatUtils.formatRupees(it.paidAmount)} | Bal: ${FormatUtils.formatRupees(it.balanceAmount)}"
                        }
                        val msg = WhatsAppUtils.generateCustomerLedgerMessage(
                            customerName = customer.name,
                            mobile = customer.mobile,
                            totalAmount = totalAmount,
                            totalPaid = totalPaid,
                            balance = outstanding,
                            lines = lines
                        )
                        WhatsAppUtils.shareToWhatsApp(context, msg, customer.mobile)
                    },
                    modifier = Modifier.weight(1.4f).height(44.dp).testTag("btn_share_ledger_whatsapp"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WHATSAPP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Ledger Transactions Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRANSACTION HISTORY (${ledgerRows.size} Orders)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = ChaiTextSecondary
                )

                if (outstanding > 0) {
                    TextButton(onClick = { onReceivePayment(customerSummary) }) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp), tint = PaidGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Record Payment", color = PaidGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (ledgerRows.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ChaiCardLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No order transactions found for this customer", color = ChaiTextSecondary)
                    }
                }
            }
        } else {
            items(ledgerRows) { row ->
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
                            Text(
                                text = "${FormatUtils.formatDate(row.date)} • ${row.orderNumber}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ChaiPrimary
                            )

                            Text(
                                text = "Balance: ${FormatUtils.formatRupees(row.balanceAmount)}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (row.balanceAmount > 0) BalanceRed else PaidGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = row.description,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = ChaiTextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = ChaiBorder.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total: ${FormatUtils.formatRupees(row.totalAmount)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = ChaiTextPrimary
                            )
                            Text(
                                text = "Paid: ${FormatUtils.formatRupees(row.paidAmount)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = PaidGreen
                            )
                        }
                    }
                }
            }
        }
    }
}
