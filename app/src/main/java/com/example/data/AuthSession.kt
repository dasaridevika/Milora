package com.example.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

enum class UserRole {
    NONE,
    OWNER,
    CUSTOMER
}

data class GoogleUserProfile(
    val email: String,
    val name: String,
    val photoUrl: String = ""
)

object AuthSession {
    var currentUser by mutableStateOf<GoogleUserProfile?>(null)
    var userRole by mutableStateOf(UserRole.NONE)
    var ownerCode by mutableStateOf("") // Generated code for dairy owners (e.g. "PM-9F4B")
    var joinedOwnerCode by mutableStateOf("") // Code entered by customer to join an owner
    var currentCustomerId by mutableStateOf(-1) // Local Customer ID if registered/linked under an owner

    fun init(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences("auth_session", Context.MODE_PRIVATE)
        val email = prefs.getString("google_email", "") ?: ""
        val name = prefs.getString("google_name", "") ?: ""
        val roleStr = prefs.getString("user_role", UserRole.NONE.name) ?: UserRole.NONE.name
        val code = prefs.getString("owner_code", "") ?: ""
        val joinedCode = prefs.getString("joined_owner_code", "") ?: ""
        val custId = prefs.getInt("customer_id", -1)

        if (email.isNotEmpty()) {
            currentUser = GoogleUserProfile(email, name)
        }
        userRole = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.NONE }
        ownerCode = code
        joinedOwnerCode = joinedCode
        currentCustomerId = custId
    }

    fun loginWithGoogle(context: Context, email: String, name: String) {
        val prefs = context.applicationContext.getSharedPreferences("auth_session", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("google_email", email)
            putString("google_name", name)
            apply()
        }
        currentUser = GoogleUserProfile(email, name)
        FirestoreSync.syncUser(context, currentUser, userRole, ownerCode, joinedOwnerCode, currentCustomerId)
    }

    fun selectRole(context: Context, role: UserRole) {
        val prefs = context.applicationContext.getSharedPreferences("auth_session", Context.MODE_PRIVATE)
        
        // Generate a random unique code if selecting OWNER for the first time
        if (role == UserRole.OWNER && ownerCode.isEmpty()) {
            val randomSegment = (1000..9999).random().toString()
            val shortEmail = currentUser?.email?.substringBefore("@")?.take(3)?.uppercase() ?: "SHR"
            ownerCode = "PM-$shortEmail-$randomSegment"
            prefs.edit().putString("owner_code", ownerCode).apply()
        }

        prefs.edit().putString("user_role", role.name).apply()
        userRole = role
        FirestoreSync.syncUser(context, currentUser, userRole, ownerCode, joinedOwnerCode, currentCustomerId)
    }

    fun joinOwner(context: Context, code: String, mappedCustomerId: Int) {
        val prefs = context.applicationContext.getSharedPreferences("auth_session", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("joined_owner_code", code)
            putInt("customer_id", mappedCustomerId)
            apply()
        }
        joinedOwnerCode = code
        currentCustomerId = mappedCustomerId
        FirestoreSync.syncUser(context, currentUser, userRole, ownerCode, joinedOwnerCode, currentCustomerId)
    }

    fun logout(context: Context) {
        currentUser?.email?.let { email ->
            FirestoreSync.logoutUser(context, email)
        }

        val prefs = context.applicationContext.getSharedPreferences("auth_session", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        
        currentUser = null
        userRole = UserRole.NONE
        ownerCode = ""
        joinedOwnerCode = ""
        currentCustomerId = -1
    }
}
