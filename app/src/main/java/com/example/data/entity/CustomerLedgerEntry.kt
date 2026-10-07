package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customer_ledger",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["customerId"])]
)
data class CustomerLedgerEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val date: Long = System.currentTimeMillis(),
    val transactionType: String, // "DEBIT" (+ You Gave / Udhar / Purchase) or "CREDIT" (- You Got / Jama / Payment)
    val amount: Double,
    val title: String, // e.g. "Tea + Samosa", "Cash Payment", "Opening Udhar"
    val details: String = "",
    val paymentMode: String = "Cash", // Cash, UPI, PhonePe, Google Pay, Paytm, Credit Sale, Other
    val referenceNo: String = ""
)
