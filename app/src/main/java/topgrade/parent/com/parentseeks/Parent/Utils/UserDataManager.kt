package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.util.Log
import io.paperdb.Paper

/**
 * Centralized manager for user data operations
 * Eliminates code duplication across dashboard activities
 */
object UserDataManager {
    
    private const val TAG = "UserDataManager"
    
    /**
     * Initialize PaperDB (should be called once in Application class)
     */
    fun init(context: Context) {
        try {
            Paper.init(context)
            Log.d(TAG, "PaperDB initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing PaperDB", e)
        }
    }
    
    /**
     * Get current user type
     */
    fun getCurrentUserType(): UserType? {
        return try {
            val userTypeString = Paper.book().read<String>(DataKeys.USER_TYPE, null)
            UserType.fromString(userTypeString)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user type", e)
            null
        }
    }
    
    /**
     * Get current user name based on user type
     */
    fun getCurrentUserName(defaultName: String = "User"): String {
        return try {
            val userType = getCurrentUserType() ?: return defaultName
            val nameKey = DataKeys.getUserNameKey(userType)
            
            var userName = Paper.book().read<String>(nameKey, defaultName)
            
            // Handle "DEMO" special case
            if ("DEMO".equals(userName, ignoreCase = true)) {
                userName = userType.displayName
                Paper.book().write(nameKey, userName)
                Log.d(TAG, "Overrode DEMO value with: $userName")
            }
            
            userName ?: defaultName
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user name", e)
            defaultName
        }
    }
    
    /**
     * Get current user ID based on user type
     */
    fun getCurrentUserId(): String? {
        return try {
            val userType = getCurrentUserType() ?: return null
            val idKey = DataKeys.getUserIdKey(userType)
            Paper.book().read<String>(idKey, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user ID", e)
            null
        }
    }
    
    /**
     * Get campus ID
     */
    fun getCampusId(): String? {
        return try {
            Paper.book().read<String>(DataKeys.CAMPUS_ID, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting campus ID", e)
            null
        }
    }
    
    /**
     * Get campus name
     */
    fun getCampusName(): String? {
        return try {
            Paper.book().read<String>(DataKeys.CAMPUS_NAME, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting campus name", e)
            null
        }
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return try {
            Paper.book().read<Boolean>(DataKeys.IS_LOGIN, false) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking login status", e)
            false
        }
    }
    
    /**
     * Save user data
     */
    fun saveUserData(key: String, value: Any?) {
        try {
            if (value == null) {
                Paper.book().delete(key)
            } else {
                Paper.book().write(key, value)
            }
            Log.d(TAG, "Saved data for key: $key")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving data for key: $key", e)
        }
    }
    
    /**
     * Get user data
     */
    fun <T> getUserData(key: String, defaultValue: T? = null): T? {
        return try {
            Paper.book().read<T>(key, defaultValue)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading data for key: $key", e)
            defaultValue
        }
    }
    
    /**
     * Delete user data
     */
    fun deleteUserData(key: String) {
        try {
            Paper.book().delete(key)
            Log.d(TAG, "Deleted data for key: $key")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting data for key: $key", e)
        }
    }
    
    /**
     * Clear all user data (used during logout)
     */
    fun clearAllUserData() {
        try {
            DataKeys.getAllUserDataKeys().forEach { key ->
                Paper.book().delete(key)
            }
            Log.d(TAG, "All user data cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing user data", e)
        }
    }
    
    /**
     * Get user display name with proper fallback
     */
    fun getUserDisplayName(): String {
        val userType = getCurrentUserType()
        return getCurrentUserName(userType?.displayName ?: "User")
    }
    
    /**
     * Update user profile picture
     */
    fun updateUserPicture(pictureUrl: String) {
        val userType = getCurrentUserType() ?: return
        val pictureKey = DataKeys.getUserPictureKey(userType)
        saveUserData(pictureKey, pictureUrl)
    }
    
    /**
     * Get user profile picture
     */
    fun getUserPicture(): String? {
        val userType = getCurrentUserType() ?: return null
        val pictureKey = DataKeys.getUserPictureKey(userType)
        return getUserData<String>(pictureKey, null)
    }
    
    /**
     * Log current user data (for debugging)
     */
    fun logCurrentUserData() {
        try {
            Log.d(TAG, "=== CURRENT USER DATA ===")
            Log.d(TAG, "User Type: ${getCurrentUserType()}")
            Log.d(TAG, "User Name: ${getCurrentUserName()}")
            Log.d(TAG, "User ID: ${getCurrentUserId()}")
            Log.d(TAG, "Campus ID: ${getCampusId()}")
            Log.d(TAG, "Campus Name: ${getCampusName()}")
            Log.d(TAG, "Is Logged In: ${isLoggedIn()}")
            Log.d(TAG, "=========================")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging user data", e)
        }
    }
}

