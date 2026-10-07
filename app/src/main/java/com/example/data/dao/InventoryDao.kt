package com.example.data.dao

import androidx.room.*
import com.example.data.entity.InventoryConsumptionLog
import com.example.data.entity.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllInventoryItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE currentStock <= lowStockThreshold")
    fun getLowStockItems(): Flow<List<InventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InventoryItem>)

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Delete
    suspend fun deleteItem(item: InventoryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: InventoryConsumptionLog): Long

    @Query("SELECT * FROM inventory_logs ORDER BY date DESC, id DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<InventoryConsumptionLog>>

    @Query("SELECT * FROM inventory_logs WHERE itemId = :itemId ORDER BY date DESC")
    fun getLogsForItem(itemId: Long): Flow<List<InventoryConsumptionLog>>

    @Query("SELECT COUNT(*) FROM inventory_items")
    suspend fun getItemCount(): Int
}
