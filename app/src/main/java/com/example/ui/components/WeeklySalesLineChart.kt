package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DailySalesTrend
import com.example.ui.theme.*
import com.example.util.FormatUtils

@Composable
fun WeeklySalesLineChart(
    trends: List<DailySalesTrend>,
    modifier: Modifier = Modifier
) {
    if (trends.isEmpty()) return

    val total7DaySales = trends.sumOf { it.salesAmount }
    val total7DayPaid = trends.sumOf { it.paidAmount }
    val avgDailySales = if (trends.isNotEmpty()) total7DaySales / trends.size else 0.0
    val maxSales = (trends.maxOfOrNull { maxOf(it.salesAmount, it.paidAmount) } ?: 100.0).coerceAtLeast(100.0)

    var selectedIndex by remember { mutableStateOf(trends.size - 1) }
    val selectedTrend = trends.getOrNull(selectedIndex)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ChaiCardLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_sales_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(ChaiSecondary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Trend",
                            tint = ChaiSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "SALES & REVENUE TREND",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = ChaiTextPrimary
                        )
                        Text(
                            text = "Daily performance over the last 7 days",
                            style = MaterialTheme.typography.bodySmall,
                            color = ChaiTextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ChaiCardWarm
                ) {
                    Text(
                        text = "Last 7 Days",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ChaiPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metric Summary Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Revenue",
                        style = MaterialTheme.typography.labelSmall,
                        color = ChaiTextSecondary
                    )
                    Text(
                        text = FormatUtils.formatRupees(total7DaySales),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = ChaiSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Collected (Paid)",
                        style = MaterialTheme.typography.labelSmall,
                        color = ChaiTextSecondary
                    )
                    Text(
                        text = FormatUtils.formatRupees(total7DayPaid),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PaidGreen
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Daily Average",
                        style = MaterialTheme.typography.labelSmall,
                        color = ChaiTextSecondary
                    )
                    Text(
                        text = FormatUtils.formatRupees(avgDailySales),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ChaiTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active day inspection tooltip banner
            selectedTrend?.let { trend ->
                Surface(
                    color = ChaiCardWarm,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${trend.dayLabel}, ${trend.dateStr}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ChaiPrimaryDark
                            )
                            Text(
                                text = "${trend.orderCount} orders placed",
                                style = MaterialTheme.typography.labelSmall,
                                color = ChaiTextSecondary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Sales", style = MaterialTheme.typography.labelSmall, color = ChaiTextSecondary)
                                Text(
                                    FormatUtils.formatRupees(trend.salesAmount),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                    color = ChaiSecondary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Paid", style = MaterialTheme.typography.labelSmall, color = ChaiTextSecondary)
                                Text(
                                    FormatUtils.formatRupees(trend.paidAmount),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = PaidGreen
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(trends) {
                            detectTapGestures { offset ->
                                val stepX = size.width / (trends.size - 1).coerceAtLeast(1)
                                val nearestIndex = ((offset.x + (stepX / 2)) / stepX).toInt().coerceIn(0, trends.size - 1)
                                selectedIndex = nearestIndex
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val paddingBottom = 20.dp.toPx()
                    val paddingTop = 15.dp.toPx()
                    val chartHeight = height - paddingTop - paddingBottom

                    // Draw 3 subtle horizontal grid guide lines
                    val gridSteps = 3
                    for (i in 0..gridSteps) {
                        val y = paddingTop + (chartHeight * i / gridSteps)
                        drawLine(
                            color = ChaiBorder.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    if (trends.size < 2) return@Canvas

                    val stepX = width / (trends.size - 1)
                    val points = trends.mapIndexed { index, item ->
                        val x = index * stepX
                        val normalized = (item.salesAmount / maxSales).toFloat().coerceIn(0f, 1f)
                        val y = paddingTop + (chartHeight * (1f - normalized))
                        Offset(x, y)
                    }

                    val paidPoints = trends.mapIndexed { index, item ->
                        val x = index * stepX
                        val normalized = (item.paidAmount / maxSales).toFloat().coerceIn(0f, 1f)
                        val y = paddingTop + (chartHeight * (1f - normalized))
                        Offset(x, y)
                    }

                    // 1. Shaded Area Under Sales Curve (Gradient Fill)
                    val fillPath = Path().apply {
                        moveTo(points.first().x, paddingTop + chartHeight)
                        lineTo(points.first().x, points.first().y)

                        for (i in 0 until points.size - 1) {
                            val p1 = points[i]
                            val p2 = points[i + 1]
                            val cx1 = (p1.x + p2.x) / 2f
                            val cy1 = p1.y
                            val cx2 = (p1.x + p2.x) / 2f
                            val cy2 = p2.y
                            cubicTo(cx1, cy1, cx2, cy2, p2.x, p2.y)
                        }

                        lineTo(points.last().x, paddingTop + chartHeight)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                ChaiSecondary.copy(alpha = 0.35f),
                                ChaiSecondary.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            startY = paddingTop,
                            endY = paddingTop + chartHeight
                        )
                    )

                    // 2. Paid / Collection Curve (Green dashed / subtle line)
                    val paidStrokePath = Path().apply {
                        moveTo(paidPoints.first().x, paidPoints.first().y)
                        for (i in 0 until paidPoints.size - 1) {
                            val p1 = paidPoints[i]
                            val p2 = paidPoints[i + 1]
                            val cx1 = (p1.x + p2.x) / 2f
                            val cx2 = (p1.x + p2.x) / 2f
                            cubicTo(cx1, p1.y, cx2, p2.y, p2.x, p2.y)
                        }
                    }

                    drawPath(
                        path = paidStrokePath,
                        color = PaidGreen.copy(alpha = 0.8f),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // 3. Sales Stroke Curve (Terracotta Orange)
                    val strokePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 0 until points.size - 1) {
                            val p1 = points[i]
                            val p2 = points[i + 1]
                            val cx1 = (p1.x + p2.x) / 2f
                            val cx2 = (p1.x + p2.x) / 2f
                            cubicTo(cx1, p1.y, cx2, p2.y, p2.x, p2.y)
                        }
                    }

                    drawPath(
                        path = strokePath,
                        color = ChaiSecondary,
                        style = Stroke(
                            width = 3.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // 4. Data Points & Active Selection Highlight
                    points.forEachIndexed { index, pt ->
                        val isSelected = index == selectedIndex

                        if (isSelected) {
                            // Vertical guide cursor line
                            drawLine(
                                color = ChaiPrimary.copy(alpha = 0.4f),
                                start = Offset(pt.x, paddingTop),
                                end = Offset(pt.x, paddingTop + chartHeight),
                                strokeWidth = 1.5.dp.toPx()
                            )

                            // Outer pulse ring
                            drawCircle(
                                color = ChaiSecondary.copy(alpha = 0.25f),
                                radius = 12.dp.toPx(),
                                center = pt
                            )
                        }

                        // Point outer circle
                        drawCircle(
                            color = ChaiSecondary,
                            radius = if (isSelected) 6.dp.toPx() else 4.5.dp.toPx(),
                            center = pt
                        )

                        // Point inner white dot
                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected) 3.5.dp.toPx() else 2.5.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            // Bottom X-Axis Days Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trends.forEachIndexed { index, trend ->
                    val isSelected = index == selectedIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedIndex = index }
                    ) {
                        Text(
                            text = trend.dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                            ),
                            color = if (isSelected) ChaiSecondary else ChaiTextSecondary
                        )
                        Text(
                            text = trend.dateStr,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (isSelected) ChaiPrimary else ChaiTextSecondary.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ChaiBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Chart Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(ChaiSecondary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sales Trend (₹)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ChaiTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(PaidGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Paid Collection (₹)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ChaiTextPrimary
                    )
                }
            }
        }
    }
}
