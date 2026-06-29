package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val address: String,
    val defaultQuantity: Double, // in liters, e.g. 0.5, 1.0, 1.5, 2.0
    val pricePerLiter: Double = 60.0, // default price in Rupees
    val isPausedToday: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_deliveries")
data class DailyDeliveryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val dateStr: String, // Format: "YYYY-MM-DD"
    val deliveredQuantity: Double, // actual quantity delivered (0.0 if paused)
    val deliveryStatus: String, // "DELIVERED", "NOT_DELIVERED", "PAUSED_BY_OWNER", "PAUSED_BY_CUSTOMER"
    val pricePerLiter: Double
)

@Entity(tableName = "monthly_bills")
data class MonthlyBillEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val monthYearStr: String, // Format: "YYYY-MM" (e.g., "2026-06")
    val totalLiters: Double,
    val totalAmount: Double,
    val status: String, // "UNPAID", "PENDING_CONFIRMATION", "CONFIRMED_BY_OWNER", "CONFIRMED_BY_CUSTOMER", "FULLY_SETTLED"
    val ownerConfirmed: Boolean = false,
    val customerConfirmed: Boolean = false
)

@Entity(tableName = "quantity_requests")
data class QuantityChangeRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val requestedQuantity: Double,
    val effectiveDateStr: String, // Format: "YYYY-MM-DD"
    val requestDate: Long = System.currentTimeMillis(),
    val status: String = "PENDING" // "PENDING", "APPROVED", "REJECTED"
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int, // 0 or -1 indicates a broadcast to all customers
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "global_config")
data class GlobalConfigEntity(
    @PrimaryKey val id: Int = 1,
    val isOwnerPausedToday: Boolean = false,
    val ownerPauseDateStr: String = "", // Date on which owner paused (e.g., "2026-06-29")
    val broadcastMessage: String = ""
)
