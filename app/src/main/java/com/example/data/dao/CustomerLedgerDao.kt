package com.example.data.dao

import androidx.room.*
import com.example.data.entity.CustomerLedgerEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerLedgerDao {
    @Query("SELECT * FROM customer_ledger ORDER BY date DESC, id DESC")
    fun getAllEntries(): Flow<List<CustomerLedgerEntry>>

    @Query("SELECT * FROM customer_ledger WHERE customerId = :customerId ORDER BY date ASC, id ASC")
    fun getEntriesForCustomer(customerId: Long): Flow<List<CustomerLedgerEntry>>

    @Query("SELECT * FROM customer_ledger WHERE customerId = :customerId AND date >= :startDate AND date <= :endDate ORDER BY date ASC, id ASC")
    fun getEntriesForCustomerBetween(customerId: Long, startDate: Long, endDate: Long): Flow<List<CustomerLedgerEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: CustomerLedgerEntry): Long

    @Update
    suspend fun updateEntry(entry: CustomerLedgerEntry)

    @Delete
    suspend fun deleteEntry(entry: CustomerLedgerEntry)

    @Query("DELETE FROM customer_ledger WHERE customerId = :customerId")
    suspend fun deleteAllForCustomer(customerId: Long)
}
