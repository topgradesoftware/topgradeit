package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import io.paperdb.Paper
import topgrade.parent.com.parentseeks.Parent.Activity.SelectRole
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody

/**
 * Centralized logout manager for consistent logout functionality across the app
 * REFACTORED: Now uses UserDataManager, DataKeys, and UserType for better maintainability
 */
object LogoutManager {
    
    private const val TAG = "LogoutManager"
    
    /**
     * Show logout confirmation dialog
     */
    fun showLogoutDialog(context: Context, onConfirm: () -> Unit) {
        try {
            AlertDialog.Builder(context)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    onConfirm()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(true)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing logout dialog", e)
            // If dialog fails, just call onConfirm
            onConfirm()
        }
    }
    
    /**
     * Perform logout with server call and local data clearing
     * REFACTORED: Uses UserType enum and simplified logic
     */
    fun performLogout(
        context: Context, 
        apiService: BaseApiService, 
        onComplete: () -> Unit,
        showLoadingCallback: ((Boolean) -> Unit)? = null
    ) {
        try {
            showLoadingCallback?.invoke(true)
            
            val userType = UserDataManager.getCurrentUserType()
            val userId = UserDataManager.getCurrentUserId() ?: ""
            val campusId = UserDataManager.getCampusId() ?: ""
            
            Log.d(TAG, "Starting logout for user type: $userType, userId: $userId")
            
            // Build request based on user type
            val postParam = buildLogoutRequest(userType, userId, campusId)
            
            val body = JSONObject(postParam).toString().toRequestBody(
                "application/json; charset=utf-8".toMediaType()
            )
            
            // Get appropriate logout API call
            val logoutCall = getLogoutApiCall(apiService, userType, body)
            
            logoutCall.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    showLoadingCallback?.invoke(false)
                    Log.d(TAG, "Logout API response - Code: ${response.code()}")
                    
                    // Always clear local data regardless of server response
                    clearLoginData(context)
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
                
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    showLoadingCallback?.invoke(false)
                    Log.e(TAG, "Server logout failed", t)
                    
                    // Clear local data even if server logout fails
                    clearLoginData(context)
                    Toast.makeText(context, "Logged out locally", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
            })
        } catch (e: Exception) {
            showLoadingCallback?.invoke(false)
            Log.e(TAG, "Error during logout", e)
            
            // Clear local data even if request creation fails
            clearLoginData(context)
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            onComplete()
        }
    }
    
    /**
     * Build logout request parameters based on user type
     */
    private fun buildLogoutRequest(userType: UserType?, userId: String, campusId: String): Map<String, String> {
        return when (userType) {
            UserType.PARENT -> hashMapOf(
                "parent_id" to userId,
                "campus_id" to campusId
            )
            UserType.STUDENT -> hashMapOf(
                "student_id" to userId,
                "campus_id" to campusId
            )
            UserType.TEACHER, UserType.STAFF -> hashMapOf(
                "staff_id" to userId,
                "campus_id" to campusId
            )
            null -> hashMapOf(
                "user_id" to userId,
                "campus_id" to campusId
            )
        }
    }
    
    /**
     * Get appropriate logout API call based on user type
     */
    private fun getLogoutApiCall(
        apiService: BaseApiService, 
        userType: UserType?, 
        body: okhttp3.RequestBody
    ): Call<ResponseBody> {
        return when (userType) {
            UserType.PARENT -> apiService.logout_parent(body)
            UserType.STUDENT -> apiService.logout_student(body)
            UserType.TEACHER, UserType.STAFF -> apiService.logout_teacher(body)
            null -> apiService.logout_parent(body) // Default to parent
        }
    }
    
    /**
     * Clear all login-related data
     * REFACTORED: Uses UserDataManager and DataKeys
     */
    fun clearLoginData(context: Context) {
        try {
            Log.d(TAG, "Clearing all login data...")
            
            // Use centralized user data manager to clear all data
            UserDataManager.clearAllUserData()
            
            // Clear biometric data if exists
            try {
                val biometricManager = BiometricManager(context)
                biometricManager.clearAllBiometricData()
                Log.d(TAG, "Biometric data cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing biometric data", e)
            }
            
            // Clear legacy constants (for backward compatibility)
            try {
                Constant.parent_id = ""
                Constant.campus_id = ""
                Constant.current_session = ""
                Constant.staff_id = ""
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing legacy constants", e)
            }
            
            Log.d(TAG, "Login data cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing login data", e)
        }
    }
    
    /**
     * Navigate to appropriate screen after logout
     */
    fun navigateAfterLogout(context: Context) {
        try {
            val intent = Intent(context, SelectRole::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
            Log.d(TAG, "Navigated to SelectRole screen")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating after logout", e)
        }
    }
    
    /**
     * Check if user is logged in
     * REFACTORED: Uses UserDataManager
     */
    fun isLoggedIn(): Boolean {
        return UserDataManager.isLoggedIn()
    }
    
    /**
     * Get current user type
     * REFACTORED: Returns UserType enum instead of String
     */
    fun getUserType(): UserType? {
        return UserDataManager.getCurrentUserType()
    }
    
    /**
     * Get current user name
     * REFACTORED: Uses UserDataManager
     */
    fun getUserName(): String {
        return UserDataManager.getUserDisplayName()
    }
    
    /**
     * Perform complete logout flow (dialog + logout + navigation)
     */
    fun performCompleteLogout(
        context: Context, 
        apiService: BaseApiService,
        showLoadingCallback: ((Boolean) -> Unit)? = null
    ) {
        showLogoutDialog(context) {
            performLogout(context, apiService, {
                navigateAfterLogout(context)
            }, showLoadingCallback)
        }
    }
} 