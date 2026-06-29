package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MilkDao {
    // Customers
    @Query("SELECT * FROM customers ORDER BY id DESC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Int): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    // Daily Deliveries
    @Query("SELECT * FROM daily_deliveries ORDER BY dateStr DESC")
    fun getAllDailyDeliveries(): Flow<List<DailyDeliveryEntity>>

    @Query("SELECT * FROM daily_deliveries WHERE customerId = :customerId ORDER BY dateStr DESC")
    fun getDeliveriesByCustomer(customerId: Int): Flow<List<DailyDeliveryEntity>>

    @Query("SELECT * FROM daily_deliveries WHERE customerId = :customerId AND dateStr LIKE :monthYear || '%' ORDER BY dateStr DESC")
    suspend fun getDeliveriesByCustomerAndMonth(customerId: Int, monthYear: String): List<DailyDeliveryEntity>

    @Query("SELECT * FROM daily_deliveries WHERE customerId = :customerId AND dateStr = :dateStr LIMIT 1")
    suspend fun getDeliveryByCustomerAndDate(customerId: Int, dateStr: String): DailyDeliveryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyDelivery(delivery: DailyDeliveryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyDeliveries(deliveries: List<DailyDeliveryEntity>)

    @Update
    suspend fun updateDailyDelivery(delivery: DailyDeliveryEntity)

    // Monthly Bills
    @Query("SELECT * FROM monthly_bills ORDER BY monthYearStr DESC")
    fun getAllMonthlyBills(): Flow<List<MonthlyBillEntity>>

    @Query("SELECT * FROM monthly_bills WHERE customerId = :customerId ORDER BY monthYearStr DESC")
    fun getBillsByCustomer(customerId: Int): Flow<List<MonthlyBillEntity>>

    @Query("SELECT * FROM monthly_bills WHERE customerId = :customerId AND monthYearStr = :monthYear LIMIT 1")
    suspend fun getBillByCustomerAndMonth(customerId: Int, monthYear: String): MonthlyBillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyBill(bill: MonthlyBillEntity): Long

    @Update
    suspend fun updateMonthlyBill(bill: MonthlyBillEntity)

    // Quantity Change Requests
    @Query("SELECT * FROM quantity_requests ORDER BY requestDate DESC")
    fun getAllQuantityRequests(): Flow<List<QuantityChangeRequestEntity>>

    @Query("SELECT * FROM quantity_requests WHERE status = 'PENDING' ORDER BY requestDate DESC")
    fun getPendingQuantityRequests(): Flow<List<QuantityChangeRequestEntity>>

    @Query("SELECT * FROM quantity_requests WHERE customerId = :customerId ORDER BY requestDate DESC")
    fun getRequestsByCustomer(customerId: Int): Flow<List<QuantityChangeRequestEntity>>

    @Query("SELECT * FROM quantity_requests WHERE id = :id")
    suspend fun getQuantityRequestById(id: Int): QuantityChangeRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuantityRequest(request: QuantityChangeRequestEntity)

    @Update
    suspend fun updateQuantityRequest(request: QuantityChangeRequestEntity)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    // Global Config
    @Query("SELECT * FROM global_config WHERE id = 1 LIMIT 1")
    fun getGlobalConfigFlow(): Flow<GlobalConfigEntity?>

    @Query("SELECT * FROM global_config WHERE id = 1 LIMIT 1")
    suspend fun getGlobalConfigSync(): GlobalConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGlobalConfig(config: GlobalConfigEntity)
}

@Database(
    entities = [
        CustomerEntity::class,
        DailyDeliveryEntity::class,
        MonthlyBillEntity::class,
        QuantityChangeRequestEntity::class,
        NotificationEntity::class,
        GlobalConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun milkDao(): MilkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "paala_mitra_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
