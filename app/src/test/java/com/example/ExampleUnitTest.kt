package com.example

import com.example.util.FormatUtils
import com.example.util.WhatsAppUtils
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAmountAndBalanceCalculations() {
        val qty1 = 2
        val rate1 = 10.0
        val amount1 = qty1 * rate1
        assertEquals(20.0, amount1, 0.001)

        val qty2 = 1
        val rate2 = 20.0
        val amount2 = qty2 * rate2
        assertEquals(20.0, amount2, 0.001)

        val total = amount1 + amount2
        assertEquals(40.0, total, 0.001)

        val paid = 30.0
        val balance = total - paid
        assertEquals(10.0, balance, 0.001)

        // Partial payment later
        val newPayment = 10.0
        val remainingBalance = balance - newPayment
        assertEquals(0.0, remainingBalance, 0.001)
    }

    @Test
    fun testWhatsAppCustomerReminder() {
        val message = WhatsAppUtils.generateCustomerReminder("Ramesh", 350.0)
        assertTrue(message.contains("Dear Ramesh"))
        assertTrue(message.contains("pending amount at Tuna Kaka Tea Stall is ₹350"))
        assertTrue(message.contains("Please clear the pending amount at your convenience"))
    }

    @Test
    fun testWhatsAppDailyReportFormatting() {
        val report = WhatsAppUtils.generateDailyReport(
            dateStr = "06/10/2026",
            customerCount = 25,
            orderCount = 31,
            totalSales = 4850.0,
            totalPaid = 4500.0,
            totalBalance = 350.0,
            customerBreakdown = listOf(
                Triple("Ramesh", 500.0, 400.0),
                Triple("Suresh", 300.0, 300.0),
                Triple("Mohan", 450.0, 250.0)
            )
        )
        assertTrue(report.contains("TUNA KAKA TEA STALL"))
        assertTrue(report.contains("DAILY SALES REPORT"))
        assertTrue(report.contains("Customers:* 25"))
        assertTrue(report.contains("Orders:* 31"))
        assertTrue(report.contains("Total Sales:* ₹4,850"))
        assertTrue(report.contains("Total Paid:* ₹4,500"))
        assertTrue(report.contains("Total Balance:* ₹350"))
        assertTrue(report.contains("Ramesh"))
        assertTrue(report.contains("Suresh"))
        assertTrue(report.contains("Mohan"))
    }
}
