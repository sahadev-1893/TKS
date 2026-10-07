package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g. "Milk", "Tea Leaves", "Sugar"
    val unit: String = "kg", // "Liters (L)", "Kilograms (kg)", "Grams (g)", "Pieces (pcs)"
    val currentStock: Double,
    val lowStockThreshold: Double,
    val costPerUnit: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean get() = currentStock <= lowStockThreshold
}
