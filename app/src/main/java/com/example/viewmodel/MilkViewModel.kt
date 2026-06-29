package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MilkViewModel(private val repository: MilkRepository) : ViewModel() {

    // Roles and navigation state
    val activeRole = MutableStateFlow("SELECTION") // "SELECTION", "OWNER", "CUSTOMER"
    val activeCustomerId = MutableStateFlow(1) // Simulate logged-in customer ID

    // Owner navigation
    val ownerTab = MutableStateFlow("DELIVERIES") // "DELIVERIES", "CUSTOMERS", "BILLS", "REQUESTS", "NOTIFICATIONS"
    val selectedCustomerForDetail = MutableStateFlow<CustomerEntity?>(null)

    // Customer navigation
    val customerTab = MutableStateFlow("DASHBOARD") // "DASHBOARD", "PAUSE_DELIVERY", "BILL_HISTORY", "NOTIFICATIONS"

    // System Date simulation (Default to late June 2026 to show robust historical statistics)
    val simulatedDate = MutableStateFlow("2026-06-29")
    val simulatedMonth = MutableStateFlow("2026-06")

    // Core Database Flows
    val customers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyDeliveries: StateFlow<List<DailyDeliveryEntity>> = repository.allDailyDeliveries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyBills: StateFlow<List<MonthlyBillEntity>> = repository.allMonthlyBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quantityRequests: StateFlow<List<QuantityChangeRequestEntity>> = repository.allQuantityRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingRequests: StateFlow<List<QuantityChangeRequestEntity>> = repository.pendingQuantityRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalConfig: StateFlow<GlobalConfigEntity?> = repository.globalConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Seed the database if it's empty on launch
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            
            // Sync down from Firestore to load any existing remote state
            repository.syncFromFirestore {
                viewModelScope.launch {
                    val list = repository.allCustomers.first()
                    if (list.isNotEmpty()) {
                        activeCustomerId.value = list.first().id
                    }
                }
            }
        }
    }

    fun syncFromFirestore(onComplete: () -> Unit = {}) {
        repository.syncFromFirestore(onComplete)
    }

    fun fetchUserProfile(email: String, onComplete: (role: String?, ownerCode: String?, joinedOwnerCode: String?, currentCustomerId: Int?) -> Unit) {
        repository.fetchUserProfile(email, onComplete)
    }

    // --- OWNER BUSINESS ACTIONS ---

    // Toggle customer delivery status for today
    fun toggleDeliveryStatusForToday(customerId: Int) {
        viewModelScope.launch {
            val date = simulatedDate.value
            val month = simulatedMonth.value
            val customer = repository.getCustomerById(customerId) ?: return@launch

            val existing = repository.getDeliveryByCustomerAndDate(customerId, date)
            if (existing != null) {
                // Once confirmed, it is locked and unchangeable!
                if (existing.deliveryStatus == "CONFIRMED") return@launch

                // Toggle status
                val newStatus = if (existing.deliveryStatus == "DELIVERED") "NOT_DELIVERED" else "DELIVERED"
                val newQty = if (newStatus == "DELIVERED") customer.defaultQuantity else 0.0
                val updated = existing.copy(
                    deliveredQuantity = newQty,
                    deliveryStatus = newStatus
                )
                repository.updateDailyDelivery(updated)
            } else {
                // Create new
                val newDelivery = DailyDeliveryEntity(
                    customerId = customerId,
                    dateStr = date,
                    deliveredQuantity = customer.defaultQuantity,
                    deliveryStatus = "DELIVERED",
                    pricePerLiter = customer.pricePerLiter
                )
                repository.insertDailyDelivery(newDelivery)
            }

            // Recalculate bill for this customer and month
            recalculateBill(customerId, month)
        }
    }

    // Set delivery status specifically
    fun setDeliveryStatusForToday(customerId: Int, status: String) {
        viewModelScope.launch {
            val date = simulatedDate.value
            val month = simulatedMonth.value
            val customer = repository.getCustomerById(customerId) ?: return@launch

            val existing = repository.getDeliveryByCustomerAndDate(customerId, date)
            
            // Once confirmed, it is locked and unchangeable!
            if (existing?.deliveryStatus == "CONFIRMED") return@launch

            val qty = if (status == "DELIVERED") customer.defaultQuantity else 0.0
            
            if (existing != null) {
                repository.updateDailyDelivery(
                    existing.copy(
                        deliveredQuantity = qty,
                        deliveryStatus = status
                    )
                )
            } else {
                repository.insertDailyDelivery(
                    DailyDeliveryEntity(
                        customerId = customerId,
                        dateStr = date,
                        deliveredQuantity = qty,
                        deliveryStatus = status,
                        pricePerLiter = customer.pricePerLiter
                    )
                )
            }

            // Recalculate bill
            recalculateBill(customerId, month)
        }
    }

    // Confirm and lock delivery (unchangeable, notifies customer)
    fun confirmAndLockDelivery(customerId: Int, dateStr: String) {
        viewModelScope.launch {
            val month = simulatedMonth.value
            val customer = repository.getCustomerById(customerId) ?: return@launch
            val existing = repository.getDeliveryByCustomerAndDate(customerId, dateStr)
            
            val qty = existing?.deliveredQuantity ?: customer.defaultQuantity
            val price = existing?.pricePerLiter ?: customer.pricePerLiter

            if (existing != null) {
                repository.updateDailyDelivery(
                    existing.copy(
                        deliveryStatus = "CONFIRMED",
                        deliveredQuantity = qty
                    )
                )
            } else {
                repository.insertDailyDelivery(
                    DailyDeliveryEntity(
                        customerId = customerId,
                        dateStr = dateStr,
                        deliveredQuantity = qty,
                        deliveryStatus = "CONFIRMED",
                        pricePerLiter = price
                    )
                )
            }

            // Recalculate bill
            recalculateBill(customerId, month)

            // Notify customer
            repository.insertNotification(
                NotificationEntity(
                    customerId = customerId,
                    title = "Delivery Confirmed & Finalized",
                    message = "Vendor confirmed delivery of ${qty}L milk on $dateStr. This is now locked and unchangeable.",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Owner pause today's global delivery (Case A)
    fun toggleOwnerGlobalPause(pause: Boolean, message: String = "") {
        viewModelScope.launch {
            val date = simulatedDate.value
            val month = simulatedMonth.value
            val currentConfig = repository.getGlobalConfigSync() ?: GlobalConfigEntity()

            val updatedConfig = currentConfig.copy(
                isOwnerPausedToday = pause,
                ownerPauseDateStr = if (pause) date else "",
                broadcastMessage = if (message.isNotEmpty()) message else currentConfig.broadcastMessage
            )
            repository.updateGlobalConfig(updatedConfig)

            // Update all customers' deliveries for today
            val customerList = repository.allCustomers.first()
            for (c in customerList) {
                val existing = repository.getDeliveryByCustomerAndDate(c.id, date)
                // If already confirmed and locked, do not modify or overwrite!
                if (existing?.deliveryStatus == "CONFIRMED") continue

                if (pause) {
                    if (existing != null) {
                        repository.updateDailyDelivery(
                            existing.copy(
                                deliveredQuantity = 0.0,
                                deliveryStatus = "PAUSED_BY_OWNER"
                            )
                        )
                    } else {
                        repository.insertDailyDelivery(
                            DailyDeliveryEntity(
                                customerId = c.id,
                                dateStr = date,
                                deliveredQuantity = 0.0,
                                deliveryStatus = "PAUSED_BY_OWNER",
                                pricePerLiter = c.pricePerLiter
                            )
                        )
                    }
                } else {
                    // Revert to delivered (unless customer paused)
                    if (existing != null && existing.deliveryStatus == "PAUSED_BY_OWNER") {
                        repository.updateDailyDelivery(
                            existing.copy(
                                deliveredQuantity = c.defaultQuantity,
                                deliveryStatus = "DELIVERED"
                            )
                        )
                    }
                }
                recalculateBill(c.id, month)
            }

            // Insert notification
            if (pause) {
                repository.insertNotification(
                    NotificationEntity(
                        customerId = 0, // Broadcast
                        title = "No Delivery Today",
                        message = "Vendor paused delivery today. Reason: ${if (message.isNotEmpty()) message else "Busy / Personal Work"}. No charges apply."
                    )
                )
            } else {
                repository.insertNotification(
                    NotificationEntity(
                        customerId = 0, // Broadcast
                        title = "Delivery Resumed",
                        message = "Vendor resumed milk delivery for today."
                    )
                )
            }
        }
    }

    // Broadcast a custom announcement message
    fun broadcastAnnouncement(message: String) {
        viewModelScope.launch {
            val currentConfig = repository.getGlobalConfigSync() ?: GlobalConfigEntity()
            repository.updateGlobalConfig(currentConfig.copy(broadcastMessage = message))

            repository.insertNotification(
                NotificationEntity(
                    customerId = 0, // Broadcast
                    title = "Vendor Announcement",
                    message = message
                )
            )
        }
    }

    // Add new customer
    fun addNewCustomer(name: String, phone: String, email: String = "", address: String, defaultQty: Double, price: Double) {
        viewModelScope.launch {
            // Uniqueness check: Ensure no duplicate customers with the same phone or email
            val existingList = repository.allCustomers.first()
            val existing = existingList.find {
                it.phone == phone || (email.isNotEmpty() && it.email.equals(email, ignoreCase = true))
            }

            val customerId: Int
            if (existing != null) {
                // If they exist, update their info instead of inserting a duplicate
                val updatedCustomer = existing.copy(
                    name = name,
                    phone = phone,
                    email = if (email.isNotEmpty()) email else existing.email,
                    address = address,
                    defaultQuantity = defaultQty,
                    pricePerLiter = price
                )
                repository.updateCustomer(updatedCustomer)
                customerId = existing.id
            } else {
                val newCustomer = CustomerEntity(
                    name = name,
                    phone = phone,
                    email = email,
                    address = address,
                    defaultQuantity = defaultQty,
                    pricePerLiter = price
                )
                customerId = repository.insertCustomer(newCustomer).toInt()
            }

            // Initialize today's delivery record
            val date = simulatedDate.value
            val existingDelivery = repository.getDeliveryByCustomerAndDate(customerId, date)
            if (existingDelivery == null) {
                repository.insertDailyDelivery(
                    DailyDeliveryEntity(
                        customerId = customerId,
                        dateStr = date,
                        deliveredQuantity = defaultQty,
                        deliveryStatus = "DELIVERED",
                        pricePerLiter = price
                    )
                )
            } else if (existingDelivery.deliveryStatus != "CONFIRMED") {
                repository.updateDailyDelivery(
                    existingDelivery.copy(
                        deliveredQuantity = defaultQty,
                        pricePerLiter = price
                    )
                )
            }

            // Calculate starting bill
            recalculateBill(customerId, simulatedMonth.value)

            // Notify
            repository.insertNotification(
                NotificationEntity(
                    customerId = customerId,
                    title = if (existing != null) "Customer Profile Updated" else "New Customer Registered",
                    message = if (existing != null) "Updated profile details for $name." else "Successfully added customer $name with default quantity ${defaultQty}L."
                )
            )
        }
    }

    // Edit customer profile
    fun editCustomer(customerId: Int, name: String, phone: String, email: String = "", address: String, defaultQty: Double, price: Double) {
        viewModelScope.launch {
            val existing = repository.getCustomerById(customerId) ?: return@launch
            val updated = existing.copy(
                name = name,
                phone = phone,
                email = email,
                address = address,
                defaultQuantity = defaultQty,
                pricePerLiter = price
            )
            repository.updateCustomer(updated)
            
            // Recalculate bill for the simulated month
            recalculateBill(customerId, simulatedMonth.value)
        }
    }

    // Mark billing payment requested / received (Owner side)
    fun requestOrConfirmPaymentByOwner(customerId: Int, monthYear: String, isReceived: Boolean) {
        viewModelScope.launch {
            val bill = repository.getBillByCustomerAndMonth(customerId, monthYear)
            val customer = repository.getCustomerById(customerId) ?: return@launch

            val status = if (isReceived) "FULLY_SETTLED" else "PENDING_CONFIRMATION"

            if (bill != null) {
                repository.updateMonthlyBill(
                    bill.copy(
                        ownerConfirmed = true,
                        status = status
                    )
                )
            } else {
                // Build a temporary bill state based on records
                val records = repository.getDeliveriesByCustomerAndMonth(customerId, monthYear)
                val liters = records.sumOf { it.deliveredQuantity }
                val amount = records.sumOf { it.deliveredQuantity * it.pricePerLiter }
                repository.insertMonthlyBill(
                    MonthlyBillEntity(
                        customerId = customerId,
                        monthYearStr = monthYear,
                        totalLiters = liters,
                        totalAmount = amount,
                        ownerConfirmed = true,
                        status = status
                    )
                )
            }

            // Insert notification
            repository.insertNotification(
                NotificationEntity(
                    customerId = customerId,
                    title = "Payment Requested",
                    message = "Owner has marked payment for $monthYear as ${if (isReceived) "Received / Settled" else "Requested"}. Please confirm."
                )
            )
        }
    }

    // Approve customer quantity change request
    fun approveQuantityRequest(requestId: Int) {
        viewModelScope.launch {
            val req = repository.getQuantityRequestById(requestId) ?: return@launch
            val updatedReq = req.copy(status = "APPROVED")
            repository.updateQuantityRequest(updatedReq)

            // Update customer entity
            val customer = repository.getCustomerById(req.customerId) ?: return@launch
            val updatedCustomer = customer.copy(defaultQuantity = req.requestedQuantity)
            repository.updateCustomer(updatedCustomer)

            // Also update today's delivery if already present
            val todayStr = simulatedDate.value
            val existingDelivery = repository.getDeliveryByCustomerAndDate(req.customerId, todayStr)
            if (existingDelivery != null && existingDelivery.deliveryStatus == "DELIVERED") {
                repository.updateDailyDelivery(
                    existingDelivery.copy(deliveredQuantity = req.requestedQuantity)
                )
            }

            // Recalculate monthly bill
            recalculateBill(req.customerId, simulatedMonth.value)

            // Notify customer
            repository.insertNotification(
                NotificationEntity(
                    customerId = req.customerId,
                    title = "Quantity Request Approved",
                    message = "Your request to change daily milk quantity to ${req.requestedQuantity}L has been approved."
                )
            )
        }
    }

    // Reject customer quantity change request
    fun rejectQuantityRequest(requestId: Int) {
        viewModelScope.launch {
            val req = repository.getQuantityRequestById(requestId) ?: return@launch
            val updatedReq = req.copy(status = "REJECTED")
            repository.updateQuantityRequest(updatedReq)

            // Notify customer
            repository.insertNotification(
                NotificationEntity(
                    customerId = req.customerId,
                    title = "Quantity Request Declined",
                    message = "Your request to change milk quantity to ${req.requestedQuantity}L was declined."
                )
            )
        }
    }


    // --- CUSTOMER BUSINESS ACTIONS ---

    // Turn off/on milk delivery for today (Case B)
    fun toggleCustomerPauseToday(customerId: Int, pause: Boolean) {
        viewModelScope.launch {
            val date = simulatedDate.value
            val month = simulatedMonth.value
            val customer = repository.getCustomerById(customerId) ?: return@launch

            val existing = repository.getDeliveryByCustomerAndDate(customerId, date)
            
            // Once confirmed, it is locked and unchangeable!
            if (existing?.deliveryStatus == "CONFIRMED") return@launch

            val qty = if (pause) 0.0 else customer.defaultQuantity
            val status = if (pause) "PAUSED_BY_CUSTOMER" else "DELIVERED"

            if (existing != null) {
                repository.updateDailyDelivery(
                    existing.copy(
                        deliveredQuantity = qty,
                        deliveryStatus = status
                    )
                )
            } else {
                repository.insertDailyDelivery(
                    DailyDeliveryEntity(
                        customerId = customerId,
                        dateStr = date,
                        deliveredQuantity = qty,
                        deliveryStatus = status,
                        pricePerLiter = customer.pricePerLiter
                    )
                )
            }

            // Recalculate bill
            recalculateBill(customerId, month)

            // Notify owner
            repository.insertNotification(
                NotificationEntity(
                    customerId = customerId,
                    title = if (pause) "Milk Paused Today" else "Milk Resumed",
                    message = "${customer.name} has ${if (pause) "PAUSED" else "RESUMED"} delivery for today ($date)."
                )
            )
        }
    }

    // Send a pause reminder message to owner
    fun sendPauseReminderToOwner(customerId: Int, reason: String) {
        viewModelScope.launch {
            val customer = repository.getCustomerById(customerId) ?: return@launch
            repository.insertNotification(
                NotificationEntity(
                    customerId = customerId,
                    title = "Pause Reminder",
                    message = "${customer.name} sent a reminder: \"I do not need milk today. Reason: $reason\""
                )
            )
        }
    }

    // Request quantity edit from customer side
    fun submitQuantityChangeRequest(customerId: Int, qty: Double, effectiveDate: String) {
        viewModelScope.launch {
            val customer = repository.getCustomerById(customerId) ?: return@launch
            val req = QuantityChangeRequestEntity(
                customerId = customerId,
                requestedQuantity = qty,
                effectiveDateStr = effectiveDate,
                status = "PENDING"
            )
            repository.insertQuantityRequest(req)

            // Notification
            repository.insertNotification(
                NotificationEntity(
                    customerId = customerId,
                    title = "Quantity Request Submitted",
                    message = "${customer.name} requested daily quantity change to ${qty}L starting $effectiveDate."
                )
            )
        }
    }

    // Customer payment confirmation (One-time check per billing cycle)
    fun confirmPaymentByCustomer(customerId: Int, monthYear: String) {
        viewModelScope.launch {
            val bill = repository.getBillByCustomerAndMonth(customerId, monthYear)
            val customer = repository.getCustomerById(customerId) ?: return@launch

            if (bill != null) {
                // If owner already confirmed, then status is FULLY_SETTLED. Otherwise CONFIRMED_BY_CUSTOMER
                val finalStatus = if (bill.ownerConfirmed) "FULLY_SETTLED" else "CONFIRMED_BY_CUSTOMER"
                repository.updateMonthlyBill(
                    bill.copy(
                        customerConfirmed = true,
                        status = finalStatus
                    )
                )
            } else {
                // Create starting bill state
                val records = repository.getDeliveriesByCustomerAndMonth(customerId, monthYear)
                val liters = records.sumOf { it.deliveredQuantity }
                val amount = records.sumOf { it.deliveredQuantity * it.pricePerLiter }
                repository.insertMonthlyBill(
                    MonthlyBillEntity(
                        customerId = customerId,
                        monthYearStr = monthYear,
                        totalLiters = liters,
                        totalAmount = amount,
                        customerConfirmed = true,
                        status = "CONFIRMED_BY_CUSTOMER"
                    )
                )
            }

            // Notification
            repository.insertNotification(
                NotificationEntity(
                    customerId = customerId,
                    title = "Payment Confirmed",
                    message = "Customer ${customer.name} confirmed the payment of bill for $monthYear."
                )
            )
        }
    }


    // --- BILLING UTILITY ---

    // Automatically recalculate bill for a customer and a month
    suspend fun recalculateBill(customerId: Int, monthYear: String) {
        val records = repository.getDeliveriesByCustomerAndMonth(customerId, monthYear)
        val totalLiters = records.sumOf { it.deliveredQuantity }
        val totalAmount = records.sumOf { it.deliveredQuantity * it.pricePerLiter }

        val existingBill = repository.getBillByCustomerAndMonth(customerId, monthYear)
        if (existingBill != null) {
            // Retain the confirmation statuses but update the totals
            val updatedBill = existingBill.copy(
                totalLiters = totalLiters,
                totalAmount = totalAmount
            )
            repository.updateMonthlyBill(updatedBill)
        } else {
            // Create a brand new bill
            val newBill = MonthlyBillEntity(
                customerId = customerId,
                monthYearStr = monthYear,
                totalLiters = totalLiters,
                totalAmount = totalAmount,
                status = "UNPAID"
            )
            repository.insertMonthlyBill(newBill)
        }
    }

    // Force recalculate bills for everyone for simulated month
    fun forceRecalculateAll() {
        viewModelScope.launch {
            val list = repository.allCustomers.first()
            val month = simulatedMonth.value
            for (c in list) {
                recalculateBill(c.id, month)
            }
        }
    }

    // Clear notifications helper
    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }
}
