package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class MilkRepository(private val milkDao: MilkDao) {
    val allCustomers: Flow<List<CustomerEntity>> = milkDao.getAllCustomers()
    val allDailyDeliveries: Flow<List<DailyDeliveryEntity>> = milkDao.getAllDailyDeliveries()
    val allMonthlyBills: Flow<List<MonthlyBillEntity>> = milkDao.getAllMonthlyBills()
    val allQuantityRequests: Flow<List<QuantityChangeRequestEntity>> = milkDao.getAllQuantityRequests()
    val pendingQuantityRequests: Flow<List<QuantityChangeRequestEntity>> = milkDao.getPendingQuantityRequests()
    val allNotifications: Flow<List<NotificationEntity>> = milkDao.getAllNotifications()
    val globalConfig: Flow<GlobalConfigEntity?> = milkDao.getGlobalConfigFlow()

    suspend fun getCustomerById(id: Int): CustomerEntity? = milkDao.getCustomerById(id)
    suspend fun insertCustomer(customer: CustomerEntity): Long = milkDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = milkDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) = milkDao.deleteCustomer(customer)

    suspend fun insertDailyDelivery(delivery: DailyDeliveryEntity) = milkDao.insertDailyDelivery(delivery)
    suspend fun updateDailyDelivery(delivery: DailyDeliveryEntity) = milkDao.updateDailyDelivery(delivery)
    suspend fun getDeliveryByCustomerAndDate(customerId: Int, dateStr: String) = milkDao.getDeliveryByCustomerAndDate(customerId, dateStr)
    suspend fun getDeliveriesByCustomerAndMonth(customerId: Int, monthYear: String) = milkDao.getDeliveriesByCustomerAndMonth(customerId, monthYear)

    suspend fun getBillByCustomerAndMonth(customerId: Int, monthYear: String) = milkDao.getBillByCustomerAndMonth(customerId, monthYear)
    suspend fun insertMonthlyBill(bill: MonthlyBillEntity) = milkDao.insertMonthlyBill(bill)
    suspend fun updateMonthlyBill(bill: MonthlyBillEntity) = milkDao.updateMonthlyBill(bill)

    suspend fun insertQuantityRequest(request: QuantityChangeRequestEntity) = milkDao.insertQuantityRequest(request)
    suspend fun updateQuantityRequest(request: QuantityChangeRequestEntity) = milkDao.updateQuantityRequest(request)
    suspend fun getQuantityRequestById(id: Int) = milkDao.getQuantityRequestById(id)
    fun getRequestsByCustomer(customerId: Int) = milkDao.getRequestsByCustomer(customerId)

    suspend fun insertNotification(notification: NotificationEntity) = milkDao.insertNotification(notification)
    suspend fun updateNotification(notification: NotificationEntity) = milkDao.updateNotification(notification)
    suspend fun clearAllNotifications() = milkDao.clearAllNotifications()

    suspend fun updateGlobalConfig(config: GlobalConfigEntity) = milkDao.insertOrUpdateGlobalConfig(config)
    suspend fun getGlobalConfigSync() = milkDao.getGlobalConfigSync()

    suspend fun seedDatabaseIfEmpty() {
        val customers = milkDao.getAllCustomers().first()
        if (customers.isNotEmpty()) return

        // Create Global Config
        milkDao.insertOrUpdateGlobalConfig(GlobalConfigEntity(
            id = 1,
            isOwnerPausedToday = false,
            ownerPauseDateStr = "",
            broadcastMessage = "Fresh organic milk will be delivered daily between 6:00 AM and 7:30 AM. Please keep your milk canisters ready in a clean place!"
        ))

        // Create Customers
        val c1Id = milkDao.insertCustomer(CustomerEntity(
            name = "Ramesh Kumar",
            phone = "9876543210",
            address = "Ward No. 2, Near Hanuman Temple",
            defaultQuantity = 1.0,
            pricePerLiter = 60.0
        )).toInt()

        val c2Id = milkDao.insertCustomer(CustomerEntity(
            name = "Suresh Patel",
            phone = "9876501234",
            address = "Main Road, Opp. Panchayat Office",
            defaultQuantity = 0.5,
            pricePerLiter = 60.0
        )).toInt()

        val c3Id = milkDao.insertCustomer(CustomerEntity(
            name = "Savitha Rao",
            phone = "9876598765",
            address = "Green Meadows, Near Milk Cooperative",
            defaultQuantity = 2.0,
            pricePerLiter = 60.0
        )).toInt()

        val c4Id = milkDao.insertCustomer(CustomerEntity(
            name = "Gopi Krishna",
            phone = "9876555443",
            address = "Patel Street, House 42",
            defaultQuantity = 1.5,
            pricePerLiter = 65.0
        )).toInt()

        // Generate past 28 days of daily delivery records for June 2026 (Dates: 2026-06-01 to 2026-06-28)
        val deliveryList = mutableListOf<DailyDeliveryEntity>()

        for (day in 1..28) {
            val dateStr = "2026-06-${String.format("%02d", day)}"
            
            // Customer 1: Ramesh Kumar (1.0L, paused on 5, 12, 19, 26 - every Friday)
            val isPausedC1 = (day % 7 == 5)
            deliveryList.add(DailyDeliveryEntity(
                customerId = c1Id,
                dateStr = dateStr,
                deliveredQuantity = if (isPausedC1) 0.0 else 1.0,
                deliveryStatus = if (isPausedC1) "PAUSED_BY_CUSTOMER" else "DELIVERED",
                pricePerLiter = 60.0
            ))

            // Customer 2: Suresh Patel (0.5L, owner paused on day 10, customer paused on day 15)
            val isPausedByOwner = (day == 10)
            val isPausedC2 = (day == 15)
            val qtyC2 = if (isPausedByOwner || isPausedC2) 0.0 else 0.5
            val statusC2 = when {
                isPausedByOwner -> "PAUSED_BY_OWNER"
                isPausedC2 -> "PAUSED_BY_CUSTOMER"
                else -> "DELIVERED"
            }
            deliveryList.add(DailyDeliveryEntity(
                customerId = c2Id,
                dateStr = dateStr,
                deliveredQuantity = qtyC2,
                deliveryStatus = statusC2,
                pricePerLiter = 60.0
            ))

            // Customer 3: Savitha Rao (2.0L, delivered everyday except owner paused day 10)
            val qtyC3 = if (day == 10) 0.0 else 2.0
            val statusC3 = if (day == 10) "PAUSED_BY_OWNER" else "DELIVERED"
            deliveryList.add(DailyDeliveryEntity(
                customerId = c3Id,
                dateStr = dateStr,
                deliveredQuantity = qtyC3,
                deliveryStatus = statusC3,
                pricePerLiter = 60.0
            ))

            // Customer 4: Gopi Krishna (1.5L, paused on day 3, 7, 14, 21 - every Sunday)
            val isPausedC4 = (day % 7 == 0 || day == 3)
            val isPausedOwnerC4 = (day == 10)
            val qtyC4 = if (isPausedC4 || isPausedOwnerC4) 0.0 else 1.5
            val statusC4 = when {
                isPausedOwnerC4 -> "PAUSED_BY_OWNER"
                isPausedC4 -> "PAUSED_BY_CUSTOMER"
                else -> "DELIVERED"
            }
            deliveryList.add(DailyDeliveryEntity(
                customerId = c4Id,
                dateStr = dateStr,
                deliveredQuantity = qtyC4,
                deliveryStatus = statusC4,
                pricePerLiter = 65.0
            ))
        }

        milkDao.insertDailyDeliveries(deliveryList)

        // Seed some past bills (e.g. May 2026) to show bill history
        // Ramesh May Bill: 30 days * 1.0L = 30L * 60 = Rs. 1800, FULLY_SETTLED
        milkDao.insertMonthlyBill(MonthlyBillEntity(
            customerId = c1Id,
            monthYearStr = "2026-05",
            totalLiters = 30.0,
            totalAmount = 1800.0,
            status = "FULLY_SETTLED",
            ownerConfirmed = true,
            customerConfirmed = true
        ))

        // Suresh May Bill: 28 days * 0.5L = 14L * 60 = Rs. 840, FULLY_SETTLED
        milkDao.insertMonthlyBill(MonthlyBillEntity(
            customerId = c2Id,
            monthYearStr = "2026-05",
            totalLiters = 14.0,
            totalAmount = 840.0,
            status = "FULLY_SETTLED",
            ownerConfirmed = true,
            customerConfirmed = true
        ))

        // Seed some starting notifications
        milkDao.insertNotification(NotificationEntity(
            customerId = 0,
            title = "Welcome to Milora!",
            message = "We are glad to have you on our digital milk delivery system.",
            timestamp = System.currentTimeMillis() - 86400000 * 5 // 5 days ago
        ))

        milkDao.insertNotification(NotificationEntity(
            customerId = c2Id,
            title = "Delivery Paused",
            message = "Suresh Patel turned OFF milk delivery for June 15.",
            timestamp = System.currentTimeMillis() - 86400000 * 3
        ))

        // Seed a pending Quantity request
        milkDao.insertQuantityRequest(QuantityChangeRequestEntity(
            customerId = c1Id,
            requestedQuantity = 0.5,
            effectiveDateStr = "2026-06-29",
            status = "PENDING"
        ))

        milkDao.insertNotification(NotificationEntity(
            customerId = c1Id,
            title = "Quantity Change Requested",
            message = "Ramesh Kumar requested to change daily milk quantity from 1.0L to 0.5L effective from June 29.",
            timestamp = System.currentTimeMillis() - 3600000 * 2 // 2 hours ago
        ))
    }
}
