package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class MilkRepository(private val milkDao: MilkDao, private val context: Context) {
    val allCustomers: Flow<List<CustomerEntity>> = milkDao.getAllCustomers()
    val allDailyDeliveries: Flow<List<DailyDeliveryEntity>> = milkDao.getAllDailyDeliveries()
    val allMonthlyBills: Flow<List<MonthlyBillEntity>> = milkDao.getAllMonthlyBills()
    val allQuantityRequests: Flow<List<QuantityChangeRequestEntity>> = milkDao.getAllQuantityRequests()
    val pendingQuantityRequests: Flow<List<QuantityChangeRequestEntity>> = milkDao.getPendingQuantityRequests()
    val allNotifications: Flow<List<NotificationEntity>> = milkDao.getAllNotifications()
    val globalConfig: Flow<GlobalConfigEntity?> = milkDao.getGlobalConfigFlow()

    suspend fun getCustomerById(id: Int): CustomerEntity? = milkDao.getCustomerById(id)

    suspend fun insertCustomer(customer: CustomerEntity): Long {
        val id = milkDao.insertCustomer(customer)
        val finalCustomer = if (customer.id == 0) customer.copy(id = id.toInt()) else customer
        FirestoreSync.syncCustomer(context, finalCustomer)
        return id
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        milkDao.updateCustomer(customer)
        FirestoreSync.syncCustomer(context, customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        milkDao.deleteCustomer(customer)
        FirestoreSync.deleteCustomer(context, customer)
    }

    suspend fun insertDailyDelivery(delivery: DailyDeliveryEntity): Long {
        val id = milkDao.insertDailyDelivery(delivery)
        val finalDelivery = if (delivery.id == 0) delivery.copy(id = id.toInt()) else delivery
        FirestoreSync.syncDailyDelivery(context, finalDelivery)
        return id
    }

    suspend fun updateDailyDelivery(delivery: DailyDeliveryEntity) {
        milkDao.updateDailyDelivery(delivery)
        FirestoreSync.syncDailyDelivery(context, delivery)
    }

    suspend fun getDeliveryByCustomerAndDate(customerId: Int, dateStr: String) = milkDao.getDeliveryByCustomerAndDate(customerId, dateStr)
    suspend fun getDeliveriesByCustomerAndMonth(customerId: Int, monthYear: String) = milkDao.getDeliveriesByCustomerAndMonth(customerId, monthYear)

    suspend fun getBillByCustomerAndMonth(customerId: Int, monthYear: String) = milkDao.getBillByCustomerAndMonth(customerId, monthYear)

    suspend fun insertMonthlyBill(bill: MonthlyBillEntity): Long {
        val id = milkDao.insertMonthlyBill(bill)
        val finalBill = if (bill.id == 0) bill.copy(id = id.toInt()) else bill
        FirestoreSync.syncMonthlyBill(context, finalBill)
        return id
    }

    suspend fun updateMonthlyBill(bill: MonthlyBillEntity) {
        milkDao.updateMonthlyBill(bill)
        FirestoreSync.syncMonthlyBill(context, bill)
    }

    suspend fun insertQuantityRequest(request: QuantityChangeRequestEntity): Long {
        val id = milkDao.insertQuantityRequest(request)
        val finalRequest = if (request.id == 0) request.copy(id = id.toInt()) else request
        FirestoreSync.syncQuantityRequest(context, finalRequest)
        return id
    }

    suspend fun updateQuantityRequest(request: QuantityChangeRequestEntity) {
        milkDao.updateQuantityRequest(request)
        FirestoreSync.syncQuantityRequest(context, request)
    }

    suspend fun getQuantityRequestById(id: Int) = milkDao.getQuantityRequestById(id)
    fun getRequestsByCustomer(customerId: Int) = milkDao.getRequestsByCustomer(customerId)

    suspend fun insertNotification(notification: NotificationEntity): Long {
        val id = milkDao.insertNotification(notification)
        val finalNotification = if (notification.id == 0) notification.copy(id = id.toInt()) else notification
        FirestoreSync.syncNotification(context, finalNotification)
        return id
    }

    suspend fun updateNotification(notification: NotificationEntity) {
        milkDao.updateNotification(notification)
        FirestoreSync.syncNotification(context, notification)
    }

    suspend fun clearAllNotifications() {
        milkDao.clearAllNotifications()
        FirestoreSync.clearAllNotifications(context)
    }

    suspend fun updateGlobalConfig(config: GlobalConfigEntity) {
        milkDao.insertOrUpdateGlobalConfig(config)
        FirestoreSync.syncGlobalConfig(context, config)
    }

    suspend fun getGlobalConfigSync() = milkDao.getGlobalConfigSync()

    fun syncFromFirestore(onComplete: () -> Unit = {}) {
        FirestoreSync.syncDownFromFirestore(context, milkDao, onComplete)
    }

    fun fetchUserProfile(email: String, onComplete: (role: String?, ownerCode: String?, joinedOwnerCode: String?, currentCustomerId: Int?) -> Unit) {
        FirestoreSync.fetchUserProfile(context, email, onComplete)
    }

    suspend fun seedDatabaseIfEmpty() {
        val config = milkDao.getGlobalConfigSync()
        if (config != null) return

        // Create Global Config with empty/clean default
        val defaultConfig = GlobalConfigEntity(
            id = 1,
            isOwnerPausedToday = false,
            ownerPauseDateStr = "",
            broadcastMessage = ""
        )
        milkDao.insertOrUpdateGlobalConfig(defaultConfig)
        FirestoreSync.syncGlobalConfig(context, defaultConfig)
    }
}
