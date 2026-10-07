package com.example.data.dao

import androidx.room.*
import com.example.data.entity.Order
import com.example.data.entity.OrderItem
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY orderDate DESC, id DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun getOrderById(orderId: Long): Flow<Order?>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY orderDate DESC")
    fun getOrdersByCustomer(customerId: Long): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE orderDate >= :startDate AND orderDate <= :endDate ORDER BY orderDate DESC")
    fun getOrdersBetweenDates(startDate: Long, endDate: Long): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItem>)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItemsForOrder(orderId: Long): Flow<List<OrderItem>>

    @Query("SELECT * FROM order_items")
    fun getAllOrderItems(): Flow<List<OrderItem>>

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteItemsForOrder(orderId: Long)
}
