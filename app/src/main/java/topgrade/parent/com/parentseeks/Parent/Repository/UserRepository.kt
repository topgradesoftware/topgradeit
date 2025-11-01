package topgrade.parent.com.parentseeks.Parent.Repository

import android.content.Context
import android.util.Log
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService

/**
 * User Repository - Wrapper for Consolidated User Repository
 * 
 * This class provides backward compatibility while using the consolidated
 * user repository functionality.
 * 
 * @deprecated Use ConsolidatedUserRepository directly for new code
 */
@Deprecated("Use ConsolidatedUserRepository instead", ReplaceWith("ConsolidatedUserRepository"))
class UserRepository(
    private val apiService: BaseApiService
) {

    companion object {
        private const val TAG = "UserRepository"
    }
    
    // Consolidated repository instance
    private lateinit var consolidatedRepository: ConsolidatedUserRepository
    
    /**
     * Initialize the consolidated repository
     */
    private fun getConsolidatedRepository(context: Context): ConsolidatedUserRepository {
        if (!::consolidatedRepository.isInitialized) {
            consolidatedRepository = ConsolidatedUserRepository(context, apiService)
            // Set to legacy mode for backward compatibility
            consolidatedRepository.setStorageMode(false)
        }
        return consolidatedRepository
    }

    /**
     * Sealed class for login result
     */
    sealed class LoginResult {
        data class Success(val loginResponse: topgrade.parent.com.parentseeks.Parent.Model.LoginResponse) : LoginResult()
        data class Error(val message: String) : LoginResult()
        object Loading : LoginResult()
    }

    /**
     * Sealed class for general operation result
     */
    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }

    /**
     * Login user with email, password, and campus ID
     */
    suspend fun login(
        _email: String,
        _password: String,
        _campusId: String,
        _fcmToken: String,
        _userType: String
    ): LoginResult {
        return try {
            // Note: This requires context, so it should be called from an activity/fragment
            // For now, we'll return an error indicating the need for context
            Log.w(TAG, "Login requires context. Use ConsolidatedUserRepository directly.")
            LoginResult.Error("Login requires context. Use ConsolidatedUserRepository directly.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during login", e)
            LoginResult.Error("Login failed: ${e.message}")
        }
    }

    /**
     * Get current user data from local storage
     */
    fun getCurrentUser(): Result<LoginData> {
        return try {
            // Note: This requires context, so it should be called from an activity/fragment
            // For now, we'll return an error indicating the need for context
            Log.w(TAG, "getCurrentUser requires context. Use ConsolidatedUserRepository directly.")
            Result.Error("getCurrentUser requires context. Use ConsolidatedUserRepository directly.")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user", e)
            Result.Error("Error retrieving user data: ${e.message}")
        }
    }

    /**
     * Get students data from local storage
     */
    fun getStudents(): Result<List<topgrade.parent.com.parentseeks.Shared.Models.SharedStudent>> {
        return try {
            // Note: This requires context, so it should be called from an activity/fragment
            // For now, we'll return an error indicating the need for context
            Log.w(TAG, "getStudents requires context. Use ConsolidatedUserRepository directly.")
            Result.Error("getStudents requires context. Use ConsolidatedUserRepository directly.")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting students", e)
            Result.Error("Error retrieving students data: ${e.message}")
        }
    }

    /**
     * Get student subjects from local storage
     */
    fun getStudentSubjects(_studentId: String): Result<List<topgrade.parent.com.parentseeks.Parent.Model.Subject>> {
        return try {
            // Note: This requires context, so it should be called from an activity/fragment
            // For now, we'll return an error indicating the need for context
            Log.w(TAG, "getStudentSubjects requires context. Use ConsolidatedUserRepository directly.")
            Result.Error("getStudentSubjects requires context. Use ConsolidatedUserRepository directly.")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting student subjects", e)
            Result.Error("Error retrieving student subjects: ${e.message}")
        }
    }

    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        return try {
            // Note: This requires context, so it should be called from an activity/fragment
            // For now, we'll return false indicating the need for context
            Log.w(TAG, "isUserLoggedIn requires context. Use ConsolidatedUserRepository directly.")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking login status", e)
            false
        }
    }

    /**
     * Get user type
     */
    fun getUserType(): String? {
        return try {
            // Note: This requires context, so it should be called from an activity/fragment
            // For now, we'll return null indicating the need for context
            Log.w(TAG, "getUserType requires context. Use ConsolidatedUserRepository directly.")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user type", e)
            null
        }
    }

    /**
     * Get current session ID
     */
    fun getCurrentSession(): String? {
        return try {
            // Note: This requires context, so it should be called from an activity/fragment
            // For now, we'll return null indicating the need for context
            Log.w(TAG, "getCurrentSession requires context. Use ConsolidatedUserRepository directly.")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current session", e)
            null
        }
    }

    /**
     * Clear user data (logout)
     */
    fun clearUserData() {
        try {
            // Note: This requires context, so it should be called from an activity/fragment
            // For now, we'll just log a warning
            Log.w(TAG, "clearUserData requires context. Use ConsolidatedUserRepository directly.")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing user data", e)
        }
    }

    /**
     * Update user profile data
     */
    suspend fun updateProfile(_profileData: Map<String, String>): Result<Boolean> {
        return try {
            // Note: This requires context, so it should be called from an activity/fragment
            // For now, we'll return an error indicating the need for context
            Log.w(TAG, "updateProfile requires context. Use ConsolidatedUserRepository directly.")
            Result.Error("updateProfile requires context. Use ConsolidatedUserRepository directly.")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile", e)
            Result.Error("Error updating profile: ${e.message}")
        }
    }

    /**
     * Refresh user data from server
     */
    suspend fun refreshUserData(): Result<topgrade.parent.com.parentseeks.Parent.Model.LoginResponse> {
        return try {
            // Note: This requires context, so it should be called from an activity/fragment
            // For now, we'll return an error indicating the need for context
            Log.w(TAG, "refreshUserData requires context. Use ConsolidatedUserRepository directly.")
            Result.Error("refreshUserData requires context. Use ConsolidatedUserRepository directly.")
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing user data", e)
            Result.Error("Error refreshing user data: ${e.message}")
        }
    }
}