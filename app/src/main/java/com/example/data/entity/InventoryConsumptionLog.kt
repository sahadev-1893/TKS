package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory_logs",
    foreignKeys = [
        ForeignKey(
            entity = InventoryItem::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["itemId"])]
)
data class InventoryConsumptionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val itemName: String,
    val date: Long = System.currentTimeMillis(),
    val quantity: Double,
    val unit: String,
    val type: String, // "CONSUMPTION" or "RESTOCK"
    val remainingStockAfter: Double,
    val notes: String = ""
)
