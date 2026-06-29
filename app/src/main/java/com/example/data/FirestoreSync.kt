package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FirestoreSync {
    private const val TAG = "FirestoreSync"
    private var firestore: FirebaseFirestore? = null
    private var isInitialized = false

    fun getDb(context: Context): FirebaseFirestore? {
        if (isInitialized) return firestore
        synchronized(this) {
            if (isInitialized) return firestore
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context)
                }
                firestore = FirebaseFirestore.getInstance()
                isInitialized = true
                Log.d(TAG, "Firebase Firestore initialized successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Firebase/Firestore initialization failed: ${e.message}")
                isInitialized = true
                firestore = null
            }
            return firestore
        }
    }

    // --- User Operations ---
    fun syncUser(context: Context, profile: GoogleUserProfile?, role: UserRole, ownerCode: String, joinedOwnerCode: String, currentCustomerId: Int) {
        val user = profile ?: return
        val db = getDb(context) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = mapOf(
                    "email" to user.email,
                    "name" to user.name,
                    "role" to role.name,
                    "ownerCode" to ownerCode,
                    "joinedOwnerCode" to joinedOwnerCode,
                    "currentCustomerId" to currentCustomerId,
                    "lastActive" to System.currentTimeMillis()
                )
                db.collection("users").document(user.email)
                    .set(data, SetOptions.merge())
                Log.d(TAG, "User ${user.email} synced to Firestore.")
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing user: ${e.message}")
            }
        }
    }

    fun logoutUser(context: Context, email: String?) {
        val userEmail = email ?: return
        val db = getDb(context) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = mapOf(
                    "role" to "NONE",
                    "lastActive" to System.currentTimeMillis()
                )
                db.collection("users").document(userEmail)
                    .set(data, SetOptions.merge())
                Log.d(TAG, "User $userEmail logged out in Firestore.")
            } catch (e: Exception) {
                Log.e(TAG, "Error logging out user in Firestore: ${e.message}")
            }
        }
    }

    fun fetchUserProfile(context: Context, email: String, onComplete: (role: String?, ownerCode: String?, joinedOwnerCode: String?, currentCustomerId: Int?) -> Unit) {
        val db = getDb(context) ?: run {
            onComplete(null, null, null, null)
            return
        }
        db.collection("users").document(email).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val role = doc.getString("role")
                    val ownerCode = doc.getString("ownerCode")
                    val joinedOwnerCode = doc.getString("joinedOwnerCode")
                    val currentCustomerId = doc.getLong("currentCustomerId")?.toInt()
                    onComplete(role, ownerCode, joinedOwnerCode, currentCustomerId)
                } else {
                    onComplete(null, null, null, null)
                }
            }
            .addOnFailureListener {
                onComplete(null, null, null, null)
            }
    }

    // --- Customer Operations ---
    fun syncCustomer(context: Context, customer: CustomerEntity) {
        val db = getDb(context) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = mapOf(
                    "id" to customer.id,
                    "name" to customer.name,
                    "phone" to customer.phone,
                    "email" to customer.email,
                    "address" to customer.address,
                    "defaultQuantity" to customer.defaultQuantity,
                    "pricePerLiter" to customer.pricePerLiter,
                    "isPausedToday" to customer.isPausedToday,
                    "dateAdded" to customer.dateAdded
                )
                db.collection("customers").document(customer.id.toString())
                    .set(data, SetOptions.merge())
                Log.d(TAG, "Customer ${customer.name} synced to Firestore.")
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing customer: ${e.message}")
            }
        }
    }

    fun deleteCustomer(context: Context, customer: CustomerEntity) {
        val db = getDb(context) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.collection("customers").document(customer.id.toString()).delete()
                Log.d(TAG, "Customer ${customer.name} deleted from Firestore.")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting customer: ${e.message}")
            }
        }
    }

    // --- Daily Delivery Operations ---
    fun syncDailyDelivery(context: Context, delivery: DailyDeliveryEntity) {
        val db = getDb(context) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = mapOf(
                    "id" to delivery.id,
                    "customerId" to delivery.customerId,
                    "dateStr" to delivery.dateStr,
                    "deliveredQuantity" to delivery.deliveredQuantity,
                    "deliveryStatus" to delivery.deliveryStatus,
                    "pricePerLiter" to delivery.pricePerLiter
                )
                val docId = "${delivery.customerId}_${delivery.dateStr}"
                db.collection("deliveries").document(docId)
                    .set(data, SetOptions.merge())
                Log.d(TAG, "Delivery $docId synced to Firestore.")
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing delivery: ${e.message}")
            }
        }
    }

    // --- Monthly Bill Operations ---
    fun syncMonthlyBill(context: Context, bill: MonthlyBillEntity) {
        val db = getDb(context) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = mapOf(
                    "id" to bill.id,
                    "customerId" to bill.customerId,
                    "monthYearStr" to bill.monthYearStr,
                    "totalLiters" to bill.totalLiters,
                    "totalAmount" to bill.totalAmount,
                    "status" to bill.status,
                    "ownerConfirmed" to bill.ownerConfirmed,
                    "customerConfirmed" to bill.customerConfirmed
                )
                val docId = "${bill.customerId}_${bill.monthYearStr}"
                db.collection("bills").document(docId)
                    .set(data, SetOptions.merge())
                Log.d(TAG, "Monthly Bill $docId synced to Firestore.")
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing bill: ${e.message}")
            }
        }
    }

    // --- Quantity Change Request Operations ---
    fun syncQuantityRequest(context: Context, request: QuantityChangeRequestEntity) {
        val db = getDb(context) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = mapOf(
                    "id" to request.id,
                    "customerId" to request.customerId,
                    "requestedQuantity" to request.requestedQuantity,
                    "effectiveDateStr" to request.effectiveDateStr,
                    "requestDate" to request.requestDate,
                    "status" to request.status
                )
                db.collection("quantity_requests").document(request.id.toString())
                    .set(data, SetOptions.merge())
                Log.d(TAG, "Quantity request ${request.id} synced to Firestore.")
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing quantity request: ${e.message}")
            }
        }
    }

    // --- Notification Operations ---
    fun syncNotification(context: Context, notification: NotificationEntity) {
        val db = getDb(context) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = mapOf(
                    "id" to notification.id,
                    "customerId" to notification.customerId,
                    "title" to notification.title,
                    "message" to notification.message,
                    "timestamp" to notification.timestamp,
                    "isRead" to notification.isRead
                )
                db.collection("notifications").document(notification.id.toString())
                    .set(data, SetOptions.merge())
                Log.d(TAG, "Notification ${notification.id} synced to Firestore.")
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing notification: ${e.message}")
            }
        }
    }

    fun clearAllNotifications(context: Context) {
        val db = getDb(context) ?: return
        db.collection("notifications").get()
            .addOnSuccessListener { snapshots ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val batch = db.batch()
                        for (doc in snapshots.documents) {
                            batch.delete(doc.reference)
                        }
                        batch.commit()
                        Log.d(TAG, "All notifications cleared from Firestore.")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error clearing notifications in batch: ${e.message}")
                    }
                }
            }
    }

    // --- Global Config Operations ---
    fun syncGlobalConfig(context: Context, config: GlobalConfigEntity) {
        val db = getDb(context) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = mapOf(
                    "id" to config.id,
                    "isOwnerPausedToday" to config.isOwnerPausedToday,
                    "ownerPauseDateStr" to config.ownerPauseDateStr,
                    "broadcastMessage" to config.broadcastMessage
                )
                db.collection("global_config").document("config")
                    .set(data, SetOptions.merge())
                Log.d(TAG, "Global config synced to Firestore.")
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing global config: ${e.message}")
            }
        }
    }

    // --- Bi-directional Downward Sync ---
    fun syncDownFromFirestore(context: Context, milkDao: MilkDao, onComplete: () -> Unit = {}) {
        val db = getDb(context) ?: return
        Log.d(TAG, "Starting downward sync from Firestore...")

        // 1. Customers
        db.collection("customers").get()
            .addOnSuccessListener { customersSnap ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val customersList = customersSnap.documents.mapNotNull { doc ->
                            try {
                                CustomerEntity(
                                    id = (doc.getLong("id") ?: 0L).toInt(),
                                    name = doc.getString("name") ?: "",
                                    phone = doc.getString("phone") ?: "",
                                    email = doc.getString("email") ?: "",
                                    address = doc.getString("address") ?: "",
                                    defaultQuantity = doc.getDouble("defaultQuantity") ?: 1.0,
                                    pricePerLiter = doc.getDouble("pricePerLiter") ?: 60.0,
                                    isPausedToday = doc.getBoolean("isPausedToday") ?: false,
                                    dateAdded = doc.getLong("dateAdded") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        for (customer in customersList) {
                            milkDao.insertCustomer(customer)
                        }
                        Log.d(TAG, "Customers downloaded: ${customersList.size}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing downloaded customers: ${e.message}")
                    }

                    // 2. Deliveries
                    db.collection("deliveries").get()
                        .addOnSuccessListener { deliveriesSnap ->
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val deliveriesList = deliveriesSnap.documents.mapNotNull { doc ->
                                        try {
                                            DailyDeliveryEntity(
                                                id = (doc.getLong("id") ?: 0L).toInt(),
                                                customerId = (doc.getLong("customerId") ?: 0L).toInt(),
                                                dateStr = doc.getString("dateStr") ?: "",
                                                deliveredQuantity = doc.getDouble("deliveredQuantity") ?: 0.0,
                                                deliveryStatus = doc.getString("deliveryStatus") ?: "DELIVERED",
                                                pricePerLiter = doc.getDouble("pricePerLiter") ?: 60.0
                                            )
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                                    milkDao.insertDailyDeliveries(deliveriesList)
                                    Log.d(TAG, "Deliveries downloaded: ${deliveriesList.size}")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error processing downloaded deliveries: ${e.message}")
                                }

                                // 3. Bills
                                db.collection("bills").get()
                                    .addOnSuccessListener { billsSnap ->
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                val billsList = billsSnap.documents.mapNotNull { doc ->
                                                    try {
                                                        MonthlyBillEntity(
                                                            id = (doc.getLong("id") ?: 0L).toInt(),
                                                            customerId = (doc.getLong("customerId") ?: 0L).toInt(),
                                                            monthYearStr = doc.getString("monthYearStr") ?: "",
                                                            totalLiters = doc.getDouble("totalLiters") ?: 0.0,
                                                            totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                                                            status = doc.getString("status") ?: "UNPAID",
                                                            ownerConfirmed = doc.getBoolean("ownerConfirmed") ?: false,
                                                            customerConfirmed = doc.getBoolean("customerConfirmed") ?: false
                                                        )
                                                    } catch (e: Exception) {
                                                        null
                                                    }
                                                }
                                                for (bill in billsList) {
                                                    milkDao.insertMonthlyBill(bill)
                                                }
                                                Log.d(TAG, "Bills downloaded: ${billsList.size}")
                                            } catch (e: Exception) {
                                                Log.e(TAG, "Error processing downloaded bills: ${e.message}")
                                            }

                                            // 4. Quantity Requests
                                            db.collection("quantity_requests").get()
                                                .addOnSuccessListener { requestsSnap ->
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        try {
                                                            val requestsList = requestsSnap.documents.mapNotNull { doc ->
                                                                try {
                                                                    QuantityChangeRequestEntity(
                                                                        id = (doc.getLong("id") ?: 0L).toInt(),
                                                                        customerId = (doc.getLong("customerId") ?: 0L).toInt(),
                                                                        requestedQuantity = doc.getDouble("requestedQuantity") ?: 1.0,
                                                                        effectiveDateStr = doc.getString("effectiveDateStr") ?: "",
                                                                        requestDate = doc.getLong("requestDate") ?: System.currentTimeMillis(),
                                                                        status = doc.getString("status") ?: "PENDING"
                                                                    )
                                                                } catch (e: Exception) {
                                                                    null
                                                                }
                                                            }
                                                            for (req in requestsList) {
                                                                    milkDao.insertQuantityRequest(req)
                                                            }
                                                            Log.d(TAG, "Requests downloaded: ${requestsList.size}")
                                                        } catch (e: Exception) {
                                                            Log.e(TAG, "Error processing downloaded requests: ${e.message}")
                                                        }

                                                        // 5. Notifications
                                                        db.collection("notifications").get()
                                                            .addOnSuccessListener { notifsSnap ->
                                                                CoroutineScope(Dispatchers.IO).launch {
                                                                    try {
                                                                        val notifsList = notifsSnap.documents.mapNotNull { doc ->
                                                                            try {
                                                                                NotificationEntity(
                                                                                    id = (doc.getLong("id") ?: 0L).toInt(),
                                                                                    customerId = (doc.getLong("customerId") ?: 0L).toInt(),
                                                                                    title = doc.getString("title") ?: "",
                                                                                    message = doc.getString("message") ?: "",
                                                                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                                                                    isRead = doc.getBoolean("isRead") ?: false
                                                                                )
                                                                            } catch (e: Exception) {
                                                                                null
                                                                            }
                                                                        }
                                                                        for (notif in notifsList) {
                                                                            milkDao.insertNotification(notif)
                                                                        }
                                                                        Log.d(TAG, "Notifications downloaded: ${notifsList.size}")
                                                                    } catch (e: Exception) {
                                                                        Log.e(TAG, "Error processing downloaded notifications: ${e.message}")
                                                                    }

                                                                    // 6. Global Config
                                                                    db.collection("global_config").document("config").get()
                                                                        .addOnSuccessListener { configDoc ->
                                                                            CoroutineScope(Dispatchers.IO).launch {
                                                                                try {
                                                                                    if (configDoc.exists()) {
                                                                                        val config = GlobalConfigEntity(
                                                                                            id = 1,
                                                                                            isOwnerPausedToday = configDoc.getBoolean("isOwnerPausedToday") ?: false,
                                                                                            ownerPauseDateStr = configDoc.getString("ownerPauseDateStr") ?: "",
                                                                                            broadcastMessage = configDoc.getString("broadcastMessage") ?: ""
                                                                                        )
                                                                                        milkDao.insertOrUpdateGlobalConfig(config)
                                                                                        Log.d(TAG, "Global config downloaded.")
                                                                                    }
                                                                                } catch (e: Exception) {
                                                                                    Log.e(TAG, "Error processing downloaded global config: ${e.message}")
                                                                                }
                                                                                
                                                                                // Call final complete callback!
                                                                                onComplete()
                                                                            }
                                                                        }
                                                                }
                                                            }
                                                    }
                                                }
                                        }
                                    }
                            }
                        }
                }
            }
    }
}
