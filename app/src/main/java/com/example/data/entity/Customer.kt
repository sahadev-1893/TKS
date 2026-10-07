package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customId: String = "",
    val name: String,
    val mobile: String,
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)
