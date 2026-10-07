package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String = "",
    val customerId: Long = 0,
    val customerName: String,
    val customerMobile: String = "",
    val orderDate: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val paidAmount: Double,
    val balanceAmount: Double,
    val paymentMethod: String = "Cash",
    val status: String = "PAID", // PAID, PARTIAL, UNPAID
    val notes: String = ""
)
