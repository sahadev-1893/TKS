package com.example.data.dao

import androidx.room.*
import com.example.data.entity.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC, id DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY paymentDate DESC")
    fun getPaymentsByCustomer(customerId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE paymentDate >= :startDate AND paymentDate <= :endDate ORDER BY paymentDate DESC")
    fun getPaymentsBetweenDates(startDate: Long, endDate: Long): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Delete
    suspend fun deletePayment(payment: Payment)
}
