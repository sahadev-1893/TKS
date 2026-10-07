package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val orderId: Long? = null,
    val paymentDate: Long = System.currentTimeMillis(),
    val amount: Double,
    val paymentMethod: String = "Cash", // Cash, UPI, PhonePe, Google Pay, Paytm, Other
    val notes: String = ""
)
