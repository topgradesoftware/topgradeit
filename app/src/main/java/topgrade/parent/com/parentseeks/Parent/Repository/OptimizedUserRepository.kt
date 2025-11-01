package topgrade.parent.com.parentseeks.Parent.Repository

import android.content.Context
import android.util.Log
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService

/**
 * Optimized User Repository - Wrapper for Consolidated User Repository
 * 
 * This class provides backward compatibility while using the consolidated
 * user repository functionality.
 * 
 * @deprecated Use ConsolidatedUserRepository directly for new code
 */
@Deprecated("Use ConsolidatedUserRepository instead", ReplaceWith("ConsolidatedUserRepository"))
class OptimizedUserRepository(
    private val context: Context,
    private val apiService: BaseApiService
) {
    
    companion object {
        private const val TAG = "OptimizedUserRepository"
    }
    
    // Consolidated repository instance
    private lateinit var consolidatedRepository: ConsolidatedUserRepository
    
    /**
     * Initialize the consolidated repository
     */
    private fun getConsolidatedRepository(): ConsolidatedUserRepository {
        if (!::consolidatedRepository.isInitialized) {
            consolidatedRepository = ConsolidatedUserRepository(context, apiService)
            // Set to optimized mode for backward compatibility
            consolidatedRepository.setStorageMode(true)
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
        email: String,
        password: String,
        campusId: String,
        fcmToken: String,
        userType: String
    ): LoginResult {
        return try {
            val result = getConsolidatedRepository().login(email, password, campusId, fcmToken, userType)
            when (result) {
                is ConsolidatedUserRepository.LoginResult.Success -> LoginResult.Success(result.loginResponse)
                is ConsolidatedUserRepository.LoginResult.Error -> LoginResult.Error(result.message)
                is ConsolidatedUserRepository.LoginResult.Loading -> LoginResult.Loading
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during login", e)
            LoginResult.Error("Login failed: ${e.message}")
        }
    }
    
    /**
     * Get current user data from optimized database
     */
    suspend fun getCurrentUser(): Result<topgrade.parent.com.parentseeks.Parent.Utils.UserEntity> {
        return try {
            val result = getConsolidatedRepository().getCurrentUser()
            when (result) {
                is ConsolidatedUserRepository.Result.Success -> {
                    val userData = result.data
                    if (userData is topgrade.parent.com.parentseeks.Parent.Utils.UserEntity) {
                        Result.Success(userData)
                    } else {
                        Result.Error("Unexpected data type returned")
                    }
                }
                is ConsolidatedUserRepository.Result.Error -> Result.Error(result.message)
                is ConsolidatedUserRepository.Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user", e)
            Result.Error("Error retrieving user data: ${e.message}")
        }
    }
    
    /**
     * Get students data from optimized database
     */
    suspend fun getStudents(): Result<List<topgrade.parent.com.parentseeks.Parent.Utils.StudentEntity>> {
        return try {
            val result = getConsolidatedRepository().getStudents()
            when (result) {
                is ConsolidatedUserRepository.Result.Success -> {
                    val studentsData = result.data
                    if (studentsData is List<*>) {
                        val students = studentsData.filterIsInstance<topgrade.parent.com.parentseeks.Parent.Utils.StudentEntity>()
                        if (students.isNotEmpty()) {
                            Result.Success(students)
                        } else {
                            Result.Error("No students data found")
                        }
                    } else {
                        Result.Error("Unexpected data type returned")
                    }
                }
                is ConsolidatedUserRepository.Result.Error -> Result.Error(result.message)
                is ConsolidatedUserRepository.Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting students", e)
            Result.Error("Error retrieving students data: ${e.message}")
        }
    }
    
    /**
     * Get student with subjects from optimized database
     */
    suspend fun getStudentWithSubjects(studentId: String): Result<topgrade.parent.com.parentseeks.Parent.Utils.StudentWithSubjects> {
        return try {
            val result = getConsolidatedRepository().getStudentSubjects(studentId)
            when (result) {
                is ConsolidatedUserRepository.Result.Success -> {
                    val studentData = result.data
                    if (studentData is topgrade.parent.com.parentseeks.Parent.Utils.StudentWithSubjects) {
                        Result.Success(studentData)
                    } else {
                        Result.Error("Unexpected data type returned")
                    }
                }
                is ConsolidatedUserRepository.Result.Error -> Result.Error(result.message)
                is ConsolidatedUserRepository.Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting student with subjects", e)
            Result.Error("Error retrieving student data: ${e.message}")
        }
    }
    
    /**
     * Check if user is logged in using optimized database
     */
    suspend fun isUserLoggedIn(): Boolean {
        return try {
            getConsolidatedRepository().isUserLoggedIn()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking login status", e)
            false
        }
    }
    
    /**
     * Get current session from optimized database
     */
    suspend fun getCurrentSession(): Result<topgrade.parent.com.parentseeks.Parent.Utils.SessionEntity> {
        return try {
            val result = getConsolidatedRepository().getCurrentSession()
            when (result) {
                is ConsolidatedUserRepository.Result.Success -> {
                    val sessionData = result.data
                    if (sessionData is topgrade.parent.com.parentseeks.Parent.Utils.SessionEntity) {
                        Result.Success(sessionData)
                    } else {
                        Result.Error("Unexpected data type returned")
                    }
                }
                is ConsolidatedUserRepository.Result.Error -> Result.Error(result.message)
                is ConsolidatedUserRepository.Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current session", e)
            Result.Error("Error retrieving session data: ${e.message}")
        }
    }
    
    /**
     * Clear user data (logout) from optimized database
     */
    suspend fun clearUserData() {
        try {
            getConsolidatedRepository().clearUserData()
            Log.d(TAG, "User data cleared from optimized database")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing user data", e)
            throw e
        }
    }
    
    /**
     * Migrate data from Paper DB to optimized database
     */
    suspend fun migrateFromPaperDB(): topgrade.parent.com.parentseeks.Parent.Utils.OptimizedDatabaseManager.MigrationResult {
        return try {
            Log.d(TAG, "Starting migration from Paper DB to optimized database...")
            val result = getConsolidatedRepository().migrateFromPaperDB()
            Log.d(TAG, "Migration completed: ${result.migratedItems.size} items migrated")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error during migration", e)
            topgrade.parent.com.parentseeks.Parent.Utils.OptimizedDatabaseManager.MigrationResult().apply {
                success = false
                errorMessage = e.message
            }
        }
    }
    
    /**
     * Update user profile data
     */
    suspend fun updateProfile(profileData: Map<String, String>): Result<Boolean> {
        return try {
            val result = getConsolidatedRepository().updateProfile(profileData)
            when (result) {
                is ConsolidatedUserRepository.Result.Success -> Result.Success(result.data)
                is ConsolidatedUserRepository.Result.Error -> Result.Error(result.message)
                is ConsolidatedUserRepository.Result.Loading -> Result.Loading
            }
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
            val result = getConsolidatedRepository().refreshUserData()
            when (result) {
                is ConsolidatedUserRepository.Result.Success -> Result.Success(result.data)
                is ConsolidatedUserRepository.Result.Error -> Result.Error(result.message)
                is ConsolidatedUserRepository.Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing user data", e)
            Result.Error("Error refreshing user data: ${e.message}")
        }
    }
    
    /**
     * Close database connection
     */
    fun close() {
        try {
            getConsolidatedRepository().close()
            Log.d(TAG, "Optimized user repository closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing optimized user repository", e)
        }
    }
} 