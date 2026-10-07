package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object WhatsAppUtils {

    fun generateDailyReport(
        dateStr: String,
        customerCount: Int,
        orderCount: Int,
        totalSales: Double,
        totalPaid: Double,
        totalBalance: Double,
        customerBreakdown: List<Triple<String, Double, Double>> // Name, Total, Paid
    ): String {
        val sb = StringBuilder()
        sb.append("☕ *TUNA KAKA TEA STALL*\n")
        sb.append("📅 *DAILY SALES REPORT*\n")
        sb.append("Date: $dateStr\n\n")
        sb.append("━━━━━━━━━━━━━━━━\n\n")
        sb.append("👥 *Customers:* $customerCount\n")
        sb.append("🧾 *Orders:* $orderCount\n\n")
        sb.append("💰 *Total Sales:* ${FormatUtils.formatRupees(totalSales)}\n")
        sb.append("💵 *Total Paid:* ${FormatUtils.formatRupees(totalPaid)}\n")
        sb.append("⚠️ *Total Balance:* ${FormatUtils.formatRupees(totalBalance)}\n\n")
        sb.append("━━━━━━━━━━━━━━━━\n\n")
        sb.append("*CUSTOMER DETAILS:*\n\n")

        if (customerBreakdown.isEmpty()) {
            sb.append("No orders recorded yet today.\n\n")
        } else {
            customerBreakdown.forEachIndexed { index, item ->
                val (name, total, paid) = item
                val balance = (total - paid).coerceAtLeast(0.0)
                sb.append("${index + 1}. *$name*\n")
                sb.append("   Total: ${FormatUtils.formatRupees(total)}\n")
                sb.append("   Paid: ${FormatUtils.formatRupees(paid)}\n")
                sb.append("   Balance: ${FormatUtils.formatRupees(balance)}\n\n")
            }
        }

        sb.append("━━━━━━━━━━━━━━━━\n\n")
        sb.append("Thank you for visiting\n")
        sb.append("*Tuna Kaka Tea Stall* ☕")
        return sb.toString()
    }

    fun generateCustomerReminder(customerName: String, balanceAmount: Double): String {
        return "Dear $customerName,\n" +
                "Your pending amount at Tuna Kaka Tea Stall is ${FormatUtils.formatRupees(balanceAmount)}.\n" +
                "Please clear the pending amount at your convenience.\n" +
                "Thank you."
    }

    fun generateCustomerLedgerMessage(
        customerName: String,
        mobile: String,
        totalAmount: Double,
        totalPaid: Double,
        balance: Double,
        lines: List<String>
    ): String {
        val sb = StringBuilder()
        sb.append("☕ *TUNA KAKA TEA STALL*\n")
        sb.append("📋 *CUSTOMER ACCOUNT LEDGER*\n")
        sb.append("Customer: *$customerName* (${if (mobile.isNotBlank()) mobile else "N/A"})\n\n")
        sb.append("━━━━━━━━━━━━━━━━\n\n")
        if (lines.isNotEmpty()) {
            lines.forEach { line ->
                sb.append("• $line\n")
            }
            sb.append("\n")
        }
        sb.append("━━━━━━━━━━━━━━━━\n")
        sb.append("💰 *Total Purchases:* ${FormatUtils.formatRupees(totalAmount)}\n")
        sb.append("💵 *Total Paid:* ${FormatUtils.formatRupees(totalPaid)}\n")
        sb.append("⚠️ *Outstanding Balance:* ${FormatUtils.formatRupees(balance)}\n")
        sb.append("━━━━━━━━━━━━━━━━\n\n")
        sb.append("Thank you!\n*Tuna Kaka Tea Stall* ☕")
        return sb.toString()
    }

    fun shareToWhatsApp(context: Context, text: String, phone: String? = null) {
        val cleanPhone = phone?.filter { it.isDigit() } ?: ""
        try {
            if (cleanPhone.length >= 10) {
                val formattedPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                val encodedText = URLEncoder.encode(text, "UTF-8")
                val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedText"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            }
        } catch (_: Exception) {
            // Fall through to general share
        }

        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                this.type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                `package` = "com.whatsapp"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(sendIntent)
        } catch (_: Exception) {
            // General share chooser fallback
            try {
                val chooserIntent = Intent(Intent.ACTION_SEND).apply {
                    this.type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(chooserIntent, "Share Report"))
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open sharing app", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
