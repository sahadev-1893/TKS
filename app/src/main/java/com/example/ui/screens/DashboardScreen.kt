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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Order
import com.example.ui.AppScreen
import com.example.ui.DailySalesTrend
import com.example.ui.DashboardSummary
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.WeeklySalesLineChart
import com.example.data.supabase.SupabaseConfig
import com.example.ui.theme.*
import com.example.util.FormatUtils

@Composable
fun DashboardScreen(
    summary: DashboardSummary,
    recentOrders: List<Order>,
    weeklyTrends: List<DailySalesTrend>,
    lowStockCount: Int = 0,
    supabaseConfig: SupabaseConfig? = null,
    onQuickSync: (() -> Unit)? = null,
    onNavigate: (AppScreen) -> Unit,
    onOpenWhatsAppReport: () -> Unit,
    onOpenReceivePayment: () -> Unit,
    onOpenNewCustomer: () -> Unit,
    onOpenOrder: (Order) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ChaiBackgroundLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ChaiPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(ChaiPrimaryDark, ChaiPrimary, ChaiSecondary)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = ChaiCardWarm.copy(alpha = 0.25f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.LocalCafe,
                                            contentDescription = "Chai",
                                            tint = ChaiCardWarm,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "TUNA KAKA TEA STALL",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Customer, Order & Payment Management",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ChaiCardWarm.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Date",
                                    tint = ChaiCardWarm,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = summary.todayDateStr,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color.White
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${summary.todayOrdersCount} Orders Today",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Action Buttons Grid
        item {
            Text(
                text = "QUICK ACTIONS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = ChaiTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenNewCustomer,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("action_new_customer"),
                        colors = ButtonDefaults.buttonColors(containerColor = ChaiPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ CUSTOMER", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { onNavigate(AppScreen.ORDER_ENTRY) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("action_new_order"),
                        colors = ButtonDefaults.buttonColors(containerColor = ChaiSecondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ NEW ORDER", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenReceivePayment,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("action_payment"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PaidGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PAYMENT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { onNavigate(AppScreen.DATE_WISE_REPORT) },
                        modifier = Modifier
                            .weight(1.1f)
                            .height(44.dp)
                            .testTag("action_today_report"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ChaiPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TODAY'S REPORT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = onOpenWhatsAppReport,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .testTag("action_whatsapp_report"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WHATSAPP", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                    }
                }

                // Inventory & Raw Material Quick Button
                OutlinedButton(
                    onClick = { onNavigate(AppScreen.INVENTORY) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("action_open_inventory"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = ChaiPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("MANAGE INVENTORY (MILK, TEA LEAVES, SUGAR)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ChaiPrimary)
                    if (lowStockCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = BalanceRed,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "$lowStockCount LOW",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Low Stock Urgent Alert Card on Dashboard
        if (lowStockCount > 0) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BalanceRedLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(AppScreen.INVENTORY) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = BalanceRed, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "LOW STOCK WARNING",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = BalanceRed
                                )
                                Text(
                                    text = "$lowStockCount raw materials need immediate restocking",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ChaiTextPrimary
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = { onNavigate(AppScreen.INVENTORY) },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = BalanceRed, contentColor = Color.White)
                        ) {
                            Text("RESTOCK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Supabase Cloud Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (supabaseConfig?.isConnected == true) PaidGreenLight else ChaiCardWarm
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(AppScreen.SUPABASE_SYNC) }
                    .testTag("dashboard_supabase_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (supabaseConfig?.isConnected == true) PaidGreen else ChaiSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (supabaseConfig?.isConnected == true) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                contentDescription = "Supabase Status",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SUPABASE CLOUD SYNC",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = if (supabaseConfig?.isConnected == true) PaidGreen else ChaiPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (supabaseConfig?.isConnected == true) PaidGreen.copy(alpha = 0.15f) else ChaiBorder
                                ) {
                                    Text(
                                        text = if (supabaseConfig?.isConnected == true) "CONNECTED" else if (supabaseConfig?.isConfigured == true) "CONFIGURED" else "NOT CONNECTED",
                                        color = if (supabaseConfig?.isConnected == true) PaidGreen else ChaiTextSecondary,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if ((supabaseConfig?.lastSyncTimestamp ?: 0L) > 0)
                                    "Last synced: ${FormatUtils.formatDateTime(supabaseConfig!!.lastSyncTimestamp)}"
                                else
                                    "Tap to sync database with Supabase cloud",
                                style = MaterialTheme.typography.bodySmall,
                                color = ChaiTextSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (onQuickSync != null && supabaseConfig?.isConfigured == true) {
                            Button(
                                onClick = onQuickSync,
                                modifier = Modifier.height(36.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (supabaseConfig.isConnected) PaidGreen else ChaiPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SYNC", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onNavigate(AppScreen.SUPABASE_SYNC) },
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Text("CONNECT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Section 1 Cards: TOTAL CUSTOMERS, TOTAL SALES, TOTAL PAID, TOTAL BALANCE
        item {
            Text(
                text = "TODAY'S OVERVIEW",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = ChaiTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Customers",
                    value = "${summary.todayCustomersCount}",
                    subtitle = "Active today",
                    icon = Icons.Default.People,
                    iconTint = ChaiPrimary,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Total Sales",
                    value = FormatUtils.formatRupees(summary.todayTotalSales),
                    subtitle = "${summary.todayOrdersCount} orders",
                    icon = Icons.Default.PointOfSale,
                    iconTint = ChaiSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Paid",
                    value = FormatUtils.formatRupees(summary.todayTotalPaid),
                    subtitle = "Collected today",
                    icon = Icons.Default.CheckCircle,
                    iconTint = PaidGreen,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Total Balance",
                    value = FormatUtils.formatRupees(summary.todayTotalBalance),
                    subtitle = "Pending today",
                    icon = Icons.Default.Pending,
                    iconTint = BalanceRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Stall Total Outstanding & Pending Customers Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BalanceRedLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(AppScreen.OUTSTANDING_REPORT) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = BalanceRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TOTAL OUTSTANDING BALANCE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = BalanceRed
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = FormatUtils.formatRupees(summary.totalOutstandingBalance),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = BalanceRed
                        )
                        Text(
                            text = "${summary.pendingCustomersCount} customers have unpaid balances",
                            style = MaterialTheme.typography.bodySmall,
                            color = ChaiTextSecondary
                        )
                    }

                    FilledTonalButton(
                        onClick = { onNavigate(AppScreen.OUTSTANDING_REPORT) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = BalanceRed,
                            contentColor = Color.White
                        )
                    ) {
                        Text("VIEW LIST", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 7-Day Tea Sales & Revenue Trend Chart
        item {
            WeeklySalesLineChart(trends = weeklyTrends)
        }

        // Recent Orders Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RECENT ORDERS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = ChaiTextSecondary
                )
                TextButton(onClick = { onNavigate(AppScreen.DATE_WISE_REPORT) }) {
                    Text("View All", color = ChaiPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (recentOrders.isEmpty()) {
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
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = ChaiTextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No orders placed yet", color = ChaiTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onNavigate(AppScreen.ORDER_ENTRY) },
                            colors = ButtonDefaults.buttonColors(containerColor = ChaiPrimary)
                        ) {
                            Text("Create First Order")
                        }
                    }
                }
            }
        } else {
            items(recentOrders.take(5)) { order ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ChaiCardLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenOrder(order) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = order.customerName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ChaiTextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(status = order.status)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${order.orderNumber} • ${FormatUtils.formatDate(order.orderDate)} • ${order.paymentMethod}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ChaiTextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = FormatUtils.formatRupees(order.totalAmount),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = ChaiTextPrimary
                            )
                            if (order.balanceAmount > 0) {
                                Text(
                                    text = "Due: ${FormatUtils.formatRupees(order.balanceAmount)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = BalanceRed
                                )
                            } else {
                                Text(
                                    text = "Paid",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = PaidGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
