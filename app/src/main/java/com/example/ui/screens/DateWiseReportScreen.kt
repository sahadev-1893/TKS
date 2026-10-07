package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Order
import com.example.ui.DateFilterType
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.util.WhatsAppUtils

@Composable
fun DateWiseReportScreen(
    dateFilterType: DateFilterType,
    startDate: Long,
    endDate: Long,
    ordersWithSummary: List<Pair<Order, String>>,
    onSelectDateFilter: (DateFilterType) -> Unit,
    onOpenOrder: (Order) -> Unit
) {
    val context = LocalContext.current

    val totalOrders = ordersWithSummary.size
    val distinctCustomers = ordersWithSummary.map { it.first.customerName }.distinct().size
    val totalSales = ordersWithSummary.sumOf { it.first.totalAmount }
    val totalPaid = ordersWithSummary.sumOf { it.first.paidAmount }
    val totalBalance = ordersWithSummary.sumOf { it.first.balanceAmount }

    Scaffold(
        bottomBar = {
            // Section 7 bottom summary bar: TOTAL CUSTOMERS, TOTAL SALES, TOTAL PAID, TOTAL BALANCE
            Surface(
                color = ChaiCardWarm,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL CUSTOMERS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ChaiTextSecondary
                            )
                            Text(
                                text = "$distinctCustomers ($totalOrders orders)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = ChaiTextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TOTAL SALES",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ChaiTextSecondary
                            )
                            Text(
                                text = FormatUtils.formatRupees(totalSales),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = ChaiPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TOTAL PAID",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ChaiTextSecondary
                            )
                            Text(
                                text = FormatUtils.formatRupees(totalPaid),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = PaidGreen
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "TOTAL BALANCE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ChaiTextSecondary
                            )
                            Text(
                                text = FormatUtils.formatRupees(totalBalance),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = if (totalBalance > 0) BalanceRed else PaidGreen
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ChaiBackgroundLight)
                .padding(16.dp)
        ) {
            // Header & WhatsApp Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DATE-WISE SALES REPORT",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = ChaiTextPrimary
                    )
                    Text(
                        text = "${FormatUtils.formatDate(startDate)} to ${FormatUtils.formatDate(endDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ChaiTextSecondary
                    )
                }

                Button(
                    onClick = {
                        val breakdown = ordersWithSummary.map {
                            Triple(it.first.customerName, it.first.totalAmount, it.first.paidAmount)
                        }
                        val msg = WhatsAppUtils.generateDailyReport(
                            dateStr = "${FormatUtils.formatDate(startDate)} - ${FormatUtils.formatDate(endDate)}",
                            customerCount = distinctCustomers,
                            orderCount = totalOrders,
                            totalSales = totalSales,
                            totalPaid = totalPaid,
                            totalBalance = totalBalance,
                            customerBreakdown = breakdown
                        )
                        WhatsAppUtils.shareToWhatsApp(context, msg)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SHARE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Buttons: TODAY, YESTERDAY, THIS WEEK, THIS MONTH, CUSTOM DATE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateFilterType.values().forEach { filter ->
                    val label = when (filter) {
                        DateFilterType.TODAY -> "TODAY"
                        DateFilterType.YESTERDAY -> "YESTERDAY"
                        DateFilterType.THIS_WEEK -> "THIS WEEK"
                        DateFilterType.THIS_MONTH -> "THIS MONTH"
                        DateFilterType.CUSTOM -> "CUSTOM DATE"
                    }
                    FilterChip(
                        selected = dateFilterType == filter,
                        onClick = { onSelectDateFilter(filter) },
                        label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChaiPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Orders list
            if (ordersWithSummary.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = ChaiTextSecondary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No sales orders recorded in this date range", color = ChaiTextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(ordersWithSummary) { (order, itemsText) ->
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
                                        Text(
                                            text = order.customerName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = ChaiTextPrimary
                                        )
                                        if (order.customerMobile.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(${order.customerMobile})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ChaiTextSecondary
                                            )
                                        }
                                    }

                                    StatusBadge(status = order.status)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "${FormatUtils.formatDate(order.orderDate)} • $itemsText",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ChaiTextSecondary
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = ChaiBorder.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Total: ${FormatUtils.formatRupees(order.totalAmount)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = ChaiTextPrimary
                                    )
                                    Text(
                                        text = "Paid: ${FormatUtils.formatRupees(order.paidAmount)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = PaidGreen
                                    )
                                    Text(
                                        text = "Balance: ${FormatUtils.formatRupees(order.balanceAmount)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (order.balanceAmount > 0) BalanceRed else PaidGreen
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
