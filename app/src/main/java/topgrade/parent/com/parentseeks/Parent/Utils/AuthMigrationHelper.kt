package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.util.Log
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import topgrade.parent.com.parentseeks.Parent.Utils.Constants
import topgrade.parent.com.parentseeks.Parent.Utils.UserAuthData

/**
 * Migration helper for transferring authentication data from Paper DB to DataStore
 * Provides backward compatibility during testing phase
 */
class AuthMigrationHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "AuthMigrationHelper"
        private const val MIGRATION_COMPLETE_KEY = "auth_migration_complete"
    }
    
    private val authDataStore = AuthDataStore(context)
    
    /**
     * Check if migration has been completed
     */
    suspend fun isMigrationComplete(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Paper.book().read<Boolean>(MIGRATION_COMPLETE_KEY) ?: false
            } catch (e: Exception) {
                Log.e(TAG, "Error checking migration status", e)
                false
            }
        }
    }
    
    /**
     * Migrate authentication data from Paper DB to DataStore
     */
    suspend fun migrateAuthData(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting authentication data migration...")
                
                // Check if already migrated
                if (isMigrationComplete()) {
                    Log.d(TAG, "Migration already completed")
                    return@withContext true
                }
                
                // Read data from Paper DB
                val userId = Paper.book().read<String>("parent_id") ?: ""
                val userType = Paper.book().read<String>(Constants.User_Type) ?: ""
                val campusId = Paper.book().read<String>("campus_id") ?: ""
                val fullName = Paper.book().read<String>("full_name") ?: ""
                val email = Paper.book().read<String>("email") ?: ""
                val phone = Paper.book().read<String>("phone") ?: ""
                val landline = Paper.book().read<String>("landline") ?: ""
                val address = Paper.book().read<String>("address") ?: ""
                val picture = Paper.book().read<String>("picture") ?: ""
                val password = Paper.book().read<String>("password") ?: ""
                val isLoggedIn = Paper.book().read<Boolean>(Constants.is_login) ?: false
                val currentSession = Paper.book().read<String>("current_session") ?: ""
                val campusName = Paper.book().read<String>("campus_name") ?: ""
                
                // Staff-specific data
                val staffId = Paper.book().read<String>("staff_id") ?: ""
                val staffPassword = Paper.book().read<String>("staff_password") ?: ""
                val campusAddress = Paper.book().read<String>("campus_address") ?: ""
                val campusPhone = Paper.book().read<String>("campus_phone") ?: ""
                
                // Save to DataStore
                if (userType.equals("Teacher", ignoreCase = true) || staffId.isNotEmpty()) {
                    // Staff user
                    authDataStore.saveStaffAuthData(
                        staffId = staffId,
                        userType = userType,
                        campusId = campusId,
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        picture = picture,
                        password = staffPassword,
                        campusName = campusName,
                        campusAddress = campusAddress,
                        campusPhone = campusPhone,
                        currentSession = currentSession
                    )
                } else {
                    // Parent/Student user
                    authDataStore.saveUserAuthData(
                        userId = userId,
                        userType = userType,
                        campusId = campusId,
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        landline = landline,
                        address = address,
                        picture = picture,
                        password = password,
                        currentSession = currentSession,
                        campusName = campusName
                    )
                }
                
                // Set login state
                authDataStore.setLoggedIn(isLoggedIn)
                
                // Mark migration as complete
                Paper.book().write(MIGRATION_COMPLETE_KEY, true)
                
                Log.d(TAG, "Authentication data migration completed successfully")
                Log.d(TAG, "Migrated data: UserType=$userType, UserId=$userId, IsLoggedIn=$isLoggedIn")
                
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error during authentication data migration", e)
                false
            }
        }
    }
    
    /**
     * Get authentication data with fallback to Paper DB
     */
    suspend fun getAuthDataWithFallback(): UserAuthData? {
        return withContext(Dispatchers.IO) {
            try {
                // Try DataStore first
                val dataStoreData = try {
                    authDataStore.userAuthData.first()
                } catch (e: Exception) {
                    UserAuthData("", "", "", "", "", "", "", "", "", "", false, "", "", "", "", "", "")
                }
                
                // If DataStore has data, return it
                if (dataStoreData.userId.isNotEmpty() || dataStoreData.staffId.isNotEmpty()) {
                    Log.d(TAG, "Using DataStore authentication data")
                    return@withContext dataStoreData
                }
                
                // Fallback to Paper DB
                Log.d(TAG, "DataStore empty, falling back to Paper DB")
                val userId = Paper.book().read<String>("parent_id") ?: ""
                val userType = Paper.book().read<String>(Constants.User_Type) ?: ""
                val campusId = Paper.book().read<String>("campus_id") ?: ""
                val fullName = Paper.book().read<String>("full_name") ?: ""
                val email = Paper.book().read<String>("email") ?: ""
                val phone = Paper.book().read<String>("phone") ?: ""
                val landline = Paper.book().read<String>("landline") ?: ""
                val address = Paper.book().read<String>("address") ?: ""
                val picture = Paper.book().read<String>("picture") ?: ""
                val password = Paper.book().read<String>("password") ?: ""
                val isLoggedIn = Paper.book().read<Boolean>(Constants.is_login) ?: false
                val currentSession = Paper.book().read<String>("current_session") ?: ""
                val campusName = Paper.book().read<String>("campus_name") ?: ""
                val staffId = Paper.book().read<String>("staff_id") ?: ""
                val staffPassword = Paper.book().read<String>("staff_password") ?: ""
                val campusAddress = Paper.book().read<String>("campus_address") ?: ""
                val campusPhone = Paper.book().read<String>("campus_phone") ?: ""
                
                UserAuthData(
                    userId = userId,
                    userType = userType,
                    campusId = campusId,
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    landline = landline,
                    address = address,
                    picture = picture,
                    password = password,
                    isLoggedIn = isLoggedIn,
                    currentSession = currentSession,
                    campusName = campusName,
                    staffId = staffId,
                    staffPassword = staffPassword,
                    campusAddress = campusAddress,
                    campusPhone = campusPhone
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error getting authentication data with fallback", e)
                null
            }
        }
    }
    
    /**
     * Check if user is logged in with fallback
     */
    suspend fun isUserLoggedInWithFallback(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Try DataStore first
                val dataStoreLoggedIn = authDataStore.isUserLoggedIn()
                if (dataStoreLoggedIn) {
                    return@withContext true
                }
                
                // Fallback to Paper DB
                Paper.book().read<Boolean>(Constants.is_login) ?: false
            } catch (e: Exception) {
                Log.e(TAG, "Error checking login status with fallback", e)
                false
            }
        }
    }
    
    /**
     * Get user type with fallback
     */
    suspend fun getUserTypeWithFallback(): String {
        return withContext(Dispatchers.IO) {
            try {
                // Try DataStore first
                val dataStoreUserType = authDataStore.getUserType()
                if (dataStoreUserType.isNotEmpty()) {
                    return@withContext dataStoreUserType
                }
                
                // Fallback to Paper DB
                Paper.book().read<String>(Constants.User_Type) ?: ""
            } catch (e: Exception) {
                Log.e(TAG, "Error getting user type with fallback", e)
                ""
            }
        }
    }
    
    /**
     * Clear all authentication data (both DataStore and Paper DB)
     */
    suspend fun clearAllAuthData() {
        withContext(Dispatchers.IO) {
            try {
                // Clear DataStore
                authDataStore.clearAuthData()
                
                // Clear Paper DB - Use the same keys as LogoutManager
                Paper.book().delete("parent_id")
                Paper.book().delete("staff_id")
                Paper.book().delete(Constants.User_Type)
                Paper.book().delete("campus_id")
                Paper.book().delete("full_name")
                Paper.book().delete("email")
                Paper.book().delete("phone")
                Paper.book().delete("landline")
                Paper.book().delete("address")
                Paper.book().delete("picture")
                Paper.book().delete("password")
                Paper.book().delete("staff_password")
                Paper.book().delete(Constants.is_login)
                Paper.book().delete("current_session")
                Paper.book().delete("campus_name")
                Paper.book().delete("campus_address")
                Paper.book().delete("campus_phone")
                Paper.book().delete("students") // Also clear students data
                Paper.book().delete(MIGRATION_COMPLETE_KEY)
                
                // Clear constants if available
                try {
                    val constantClass = Class.forName("topgrade.parent.com.parentseeks.Teacher.Utils.Constant")
                    val parentIdField = constantClass.getDeclaredField("parent_id")
                    val campusIdField = constantClass.getDeclaredField("campus_id")
                    val currentSessionField = constantClass.getDeclaredField("current_session")
                    val staffIdField = constantClass.getDeclaredField("staff_id")
                    
                    parentIdField.isAccessible = true
                    campusIdField.isAccessible = true
                    currentSessionField.isAccessible = true
                    staffIdField.isAccessible = true
                    
                    parentIdField.set(null, "")
                    campusIdField.set(null, "")
                    currentSessionField.set(null, "")
                    staffIdField.set(null, "")
                } catch (e: Exception) {
                    Log.d(TAG, "Could not clear constants: ${e.message}")
                }
                
                Log.d(TAG, "All authentication data cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing authentication data", e)
            }
        }
    }
    
    /**
     * Reset migration status (for testing)
     */
    suspend fun resetMigrationStatus() {
        withContext(Dispatchers.IO) {
            try {
                Paper.book().delete(MIGRATION_COMPLETE_KEY)
                Log.d(TAG, "Migration status reset")
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting migration status", e)
            }
        }
    }
} 