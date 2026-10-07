package com.example.ui.screens

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
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.util.WhatsAppUtils

@Composable
fun OutstandingReportScreen(
    customers: List<CustomerWithSummary>,
    onViewLedger: (Long) -> Unit,
    onReceivePayment: (CustomerWithSummary) -> Unit
) {
    val context = LocalContext.current

    // Filter to ONLY customers who have unpaid balances, sorted by highest balance first!
    val pendingCustomers = customers
        .filter { it.balance > 0.0 }
        .sortedByDescending { it.balance }

    val totalPendingAmount = pendingCustomers.sumOf { it.balance }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChaiBackgroundLight)
            .padding(16.dp)
    ) {
        // Outstanding Summary Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BalanceRedLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = BalanceRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TOTAL UNPAID DUES",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = BalanceRed
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BalanceRed
                    ) {
                        Text(
                            text = "${pendingCustomers.size} Customers",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = FormatUtils.formatRupees(totalPendingAmount),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = BalanceRed
                )

                Text(
                    text = "Sorted by highest balance first • Send one-click WhatsApp reminders",
                    style = MaterialTheme.typography.bodySmall,
                    color = ChaiTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "CUSTOMERS WITH PENDING BALANCE (${pendingCustomers.size})",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = ChaiTextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (pendingCustomers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PaidGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Great news! All customer balances are cleared.",
                        fontWeight = FontWeight.Bold,
                        color = PaidGreen
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(pendingCustomers, key = { it.customer.id }) { item ->
                    val c = item.customer
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ChaiCardLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("pending_cust_${c.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Top Row: Name, Mobile, Balance
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(BalanceRedLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = c.name.take(1).uppercase(),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = BalanceRed
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
                                            text = if (c.mobile.isNotBlank()) c.mobile else "No mobile",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ChaiTextSecondary
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = FormatUtils.formatRupees(item.balance),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = BalanceRed
                                    )
                                    Text(
                                        text = "Pending Dues",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BalanceRed
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = ChaiBorder.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Order and date details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Purchases: ${FormatUtils.formatRupees(item.totalAmount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ChaiTextSecondary
                                )
                                Text(
                                    text = "Total Paid: ${FormatUtils.formatRupees(item.totalPaid)}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = PaidGreen
                                )
                            }

                            if (item.lastOrderDate != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Last Order: ${FormatUtils.formatDate(item.lastOrderDate)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ChaiTextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 3 Required Action Buttons:
                            // VIEW LEDGER, RECEIVE PAYMENT, WHATSAPP REMINDER
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onViewLedger(c.id) },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("LEDGER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { onReceivePayment(item) },
                                    modifier = Modifier.weight(1.1f).height(40.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PaidGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PAYMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val reminderText = WhatsAppUtils.generateCustomerReminder(
                                            customerName = c.name,
                                            balanceAmount = item.balance
                                        )
                                        WhatsAppUtils.shareToWhatsApp(context, reminderText, c.mobile)
                                    },
                                    modifier = Modifier.weight(1.3f).height(40.dp).testTag("btn_wa_reminder_${c.id}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("REMINDER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
