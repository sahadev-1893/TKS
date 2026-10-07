package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Order
import com.example.data.entity.Payment
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.util.WhatsAppUtils

@Composable
fun DailyClosingReportScreen(
    closingDate: Long,
    orders: List<Order>,
    payments: List<Payment>,
    onSelectDate: (Long) -> Unit
) {
    val context = LocalContext.current

    val startOfDay = FormatUtils.getStartOfDay(closingDate)
    val endOfDay = FormatUtils.getEndOfDay(closingDate)

    val dayOrders = orders.filter { it.orderDate in startOfDay..endOfDay }
    val dayPayments = payments.filter { it.paymentDate in startOfDay..endOfDay }

    val customerCount = (dayOrders.map { it.customerId } + dayPayments.map { it.customerId }).distinct().size
    val orderCount = dayOrders.size
    val totalSales = dayOrders.sumOf { it.totalAmount }

    // Breakdown collections
    val cashPayments = dayPayments.filter { it.paymentMethod.equals("Cash", ignoreCase = true) }.sumOf { it.amount }
    val upiPayments = dayPayments.filter {
        it.paymentMethod.equals("UPI", ignoreCase = true) ||
                it.paymentMethod.equals("PhonePe", ignoreCase = true) ||
                it.paymentMethod.equals("Google Pay", ignoreCase = true) ||
                it.paymentMethod.equals("Paytm", ignoreCase = true)
    }.sumOf { it.amount }
    val otherPayments = dayPayments.filter {
        !it.paymentMethod.equals("Cash", ignoreCase = true) &&
                !it.paymentMethod.equals("UPI", ignoreCase = true) &&
                !it.paymentMethod.equals("PhonePe", ignoreCase = true) &&
                !it.paymentMethod.equals("Google Pay", ignoreCase = true) &&
                !it.paymentMethod.equals("Paytm", ignoreCase = true)
    }.sumOf { it.amount }

    val totalPaid = dayPayments.sumOf { it.amount }
    val outstanding = (totalSales - totalPaid).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChaiBackgroundLight)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Text(
            text = "DAILY CLOSING REPORT",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            ),
            color = ChaiTextPrimary
        )

        // Receipt Card Preview
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ChaiCardLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Stall Branding
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalCafe,
                        contentDescription = null,
                        tint = ChaiPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "TUNA KAKA TEA STALL",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = ChaiPrimary
                    )
                    Text(
                        text = "DAILY CLOSING REGISTER",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = ChaiTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = FormatUtils.formatDateReadable(closingDate),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = ChaiTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = ChaiBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Customers & Orders counts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Customers:",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = ChaiTextPrimary
                    )
                    Text(
                        text = "$customerCount",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black),
                        color = ChaiTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Orders:",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = ChaiTextPrimary
                    )
                    Text(
                        text = "$orderCount",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black),
                        color = ChaiTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ChaiBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Total Sales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Sales:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ChaiTextPrimary
                    )
                    Text(
                        text = FormatUtils.formatRupees(totalSales),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = ChaiPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Breakdown collections: Cash, UPI, Other
                Surface(
                    color = ChaiCardWarm,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "COLLECTION BREAKDOWN",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ChaiTextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("💵 Cash:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                FormatUtils.formatRupees(cashPayments),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("📱 UPI (GPay/PhonePe):", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                FormatUtils.formatRupees(upiPayments),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("💳 Other Collection:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                FormatUtils.formatRupees(otherPayments),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = ChaiBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Total Paid & Outstanding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Paid:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PaidGreen
                    )
                    Text(
                        text = FormatUtils.formatRupees(totalPaid),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = PaidGreen
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Outstanding:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (outstanding > 0) BalanceRed else ChaiTextSecondary
                    )
                    Text(
                        text = FormatUtils.formatRupees(outstanding),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = if (outstanding > 0) BalanceRed else PaidGreen
                    )
                }
            }
        }

        // WhatsApp Share Closing Report Button
        Button(
            onClick = {
                val sb = StringBuilder()
                sb.append("☕ *TUNA KAKA TEA STALL*\n")
                sb.append("📋 *DAILY CLOSING REPORT*\n")
                sb.append("📅 Date: ${FormatUtils.formatDateReadable(closingDate)}\n\n")
                sb.append("━━━━━━━━━━━━━━━━\n\n")
                sb.append("👥 *Customers:* $customerCount\n")
                sb.append("🧾 *Orders:* $orderCount\n\n")
                sb.append("💰 *Total Sales:* ${FormatUtils.formatRupees(totalSales)}\n")
                sb.append("💵 *Cash Collection:* ${FormatUtils.formatRupees(cashPayments)}\n")
                sb.append("📱 *UPI Collection:* ${FormatUtils.formatRupees(upiPayments)}\n")
                sb.append("💳 *Other Collection:* ${FormatUtils.formatRupees(otherPayments)}\n\n")
                sb.append("━━━━━━━━━━━━━━━━\n\n")
                sb.append("✅ *Total Paid:* ${FormatUtils.formatRupees(totalPaid)}\n")
                sb.append("⚠️ *Outstanding:* ${FormatUtils.formatRupees(outstanding)}\n\n")
                sb.append("━━━━━━━━━━━━━━━━\n\n")
                sb.append("Closing signed: *Tuna Kaka Tea Stall* ☕")

                WhatsAppUtils.shareToWhatsApp(context, sb.toString())
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_share_closing_whatsapp"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SHARE CLOSING ON WHATSAPP",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
