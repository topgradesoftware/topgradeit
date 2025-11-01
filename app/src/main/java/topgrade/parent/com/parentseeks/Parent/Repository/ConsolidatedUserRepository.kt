package topgrade.parent.com.parentseeks.Parent.Repository

import android.content.Context
import android.util.Log
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService
import topgrade.parent.com.parentseeks.Parent.Model.LoginResponse
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent
import topgrade.parent.com.parentseeks.Parent.Model.Subject
import topgrade.parent.com.parentseeks.Parent.Utils.Constants
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant
import topgrade.parent.com.parentseeks.Parent.Utils.OptimizedDatabaseManager
import topgrade.parent.com.parentseeks.Parent.Utils.UserEntity
import topgrade.parent.com.parentseeks.Parent.Utils.StudentEntity
import topgrade.parent.com.parentseeks.Parent.Utils.SessionEntity
import topgrade.parent.com.parentseeks.Parent.Utils.StudentWithSubjects
import java.io.IOException

/**
 * Consolidated User Repository
 * 
 * This class combines functionality from both UserRepository and OptimizedUserRepository
 * to provide comprehensive user data management across the entire application.
 * 
 * Features:
 * - Login functionality with API integration
 * - Local data storage using both Paper DB and Room database
 * - User profile management
 * - Student data management
 * - Session management
 * - Data migration capabilities
 * - Backward compatibility
 */
class ConsolidatedUserRepository(
    private val context: Context,
    private val apiService: BaseApiService
) {
    
    companion object {
        private const val TAG = "ConsolidatedUserRepository"
    }
    
    // Database manager for optimized operations
    private val databaseManager = OptimizedDatabaseManager(context)
    
    // Storage mode preference
    private var useOptimizedStorage = false // Default to Paper DB for compatibility
    
    /**
     * Sealed class for login result
     */
    sealed class LoginResult {
        data class Success(val loginResponse: LoginResponse) : LoginResult()
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
    
    // ==================== LOGIN OPERATIONS ====================
    
    /**
     * Login user with email, password, and campus ID
     */
    suspend fun login(
        email: String,
        password: String,
        campusId: String,
        fcmToken: String,
        userType: String
    ): LoginResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting login for user: $email with type: $userType")
            
            // Create request body
            val postParam = HashMap<String, String>().apply {
                put("login_email", email)
                put("login_pass", password)
                put("login_id", campusId)
                put("fcm_token", fcmToken)
            }
            
            val jsonBody = JSONObject(postParam as Map<*, *>).toString()
            val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
            
            // Make API call
            val response = apiService.login(requestBody).execute()
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                
                if (loginResponse.status.code == "1000") {
                    // Save user data using preferred storage method
                    if (useOptimizedStorage) {
                        saveUserDataOptimized(loginResponse, password, userType)
                    } else {
                        saveUserDataLegacy(loginResponse, password, userType)
                    }
                    Log.d(TAG, "Login successful for user: ${loginResponse.data.fullName}")
                    LoginResult.Success(loginResponse)
                } else {
                    Log.w(TAG, "Login failed: ${loginResponse.status.message}")
                    LoginResult.Error(loginResponse.status.message)
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Network error occurred"
                Log.e(TAG, "Network error: $errorMessage")
                LoginResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            LoginResult.Error("Login failed: ${e.message}")
        }
    }
    
    // ==================== DATA STORAGE OPERATIONS ====================
    
    /**
     * Save user data to optimized database (Room)
     */
    private suspend fun saveUserDataOptimized(loginResponse: LoginResponse, password: String, userType: String) {
        try {
            val data = loginResponse.data
            
            // Save user data to Room database
            databaseManager.saveUserData(
                userId = data.uniqueId,
                userType = userType,
                campusId = data.parentId,
                fullName = data.fullName,
                email = data.email,
                phone = data.phone,
                landline = data.landline,
                address = data.address,
                picture = data.picture ?: "",
                password = password
            )
            
            // Save students data to Room database
            loginResponse.students?.let { students ->
                if (students.isNotEmpty()) {
                    databaseManager.saveStudents(students)
                }
            }
            
            // Save session data to Room database
            loginResponse.campusSession?.let { session ->
                databaseManager.saveSession(session.uniqueId, "active_session")
            }
            
            // Update constants for backward compatibility
            Constant.parent_id = data.uniqueId
            Constant.current_session = loginResponse.campusSession?.uniqueId ?: ""
            
            Log.d(TAG, "User data saved to optimized database. User Type: $userType")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user data to optimized database", e)
            throw e
        }
    }
    
    /**
     * Save user data to legacy storage (Paper DB)
     */
    private fun saveUserDataLegacy(loginResponse: LoginResponse, password: String, userType: String) {
        try {
            val data = loginResponse.data
            
            // Save user profile data based on user type (same as staff logic)
            when (userType.uppercase()) {
                "STUDENT" -> {
                    // Student-specific keys
                    Paper.book().write("student_id", data.uniqueId)
                    Paper.book().write("student_name", data.fullName)
                    Paper.book().write("student_phone", data.phone)
                    Paper.book().write("student_picture", data.picture)
                    // ALSO store with standard keys for parent profile access
                    Paper.book().write("parent_id", data.uniqueId)  // ← FIX: Store parent_id in Paper DB
                    Paper.book().write("full_name", data.fullName)
                    Paper.book().write("phone", data.phone)
                    Paper.book().write("picture", data.picture)
                    Constant.parent_id = data.uniqueId
                    Log.d(TAG, "=== STUDENT LOGIN DATA DEBUG ===")
                    Log.d(TAG, "Stored student_id: ${data.uniqueId}")
                    Log.d(TAG, "Stored parent_id: ${data.uniqueId}")  // ← FIX: Log parent_id storage
                    Log.d(TAG, "Stored student_name: '${data.fullName}'")
                    Log.d(TAG, "Stored full_name: '${data.fullName}' (for parent profile)")
                }
                "PARENT" -> {
                    // Parent-specific keys
                    Paper.book().write("parent_id", data.uniqueId)
                    Paper.book().write("full_name", data.fullName)
                    // Save the complete Data model like Staff_Model
                    Paper.book().write("Parent_Model", data)
                    Constant.parent_id = data.uniqueId
                    Log.d(TAG, "=== PARENT LOGIN DATA DEBUG ===")
                    Log.d(TAG, "Stored parent_id: ${data.uniqueId}")
                    Log.d(TAG, "Stored full_name: '${data.fullName}'")
                    Log.d(TAG, "Stored Parent_Model: ${data}")
                }
                else -> {
                    // Default/fallback keys
                    Paper.book().write("parent_id", data.uniqueId)
                    Paper.book().write("full_name", data.fullName)
                    Constant.parent_id = data.uniqueId
                    Log.d(TAG, "=== DEFAULT LOGIN DATA DEBUG ===")
                    Log.d(TAG, "Stored parent_id: ${data.uniqueId}")
                    Log.d(TAG, "Stored full_name: '${data.fullName}'")
                }
            }
            
            // Common data for all user types
            Paper.book().write("campus_id", data.parentId)
            Paper.book().write("email", data.email)
            Paper.book().write("phone", data.phone)
            Paper.book().write("landline", data.landline)
            Paper.book().write("address", data.address)
            Paper.book().write("picture", data.picture)
            Paper.book().write("password", password)
            
            // Ensure full_name is available for both user types (for parent profile access)
            Paper.book().write("full_name", data.fullName)
            
            Log.d(TAG, "Stored campus_id: ${data.parentId}")
            Log.d(TAG, "Stored email: ${data.email}")
            Log.d(TAG, "User Type: $userType")
            
            // Save students data
            loginResponse.students?.let { students ->
                if (students.isNotEmpty()) {
                    // Debug logging for student data including roll numbers
                    Log.d(TAG, "=== STUDENT DATA FROM API DEBUG ===")
                    students.forEachIndexed { index, student ->
                        Log.d(TAG, "Student $index: name='${student.fullName}'")
                        Log.d(TAG, "Student $index: registrationNumber='${student.registrationNumber}'")
                        Log.d(TAG, "Student $index: rollNo='${student.rollNo}'")
                        Log.d(TAG, "Student $index rollNo is null: ${student.rollNo == null}")
                        Log.d(TAG, "Student $index rollNo is empty: ${student.rollNo?.isEmpty() ?: true}")
                        Log.d(TAG, "Student $index registrationNumber is null: ${student.registrationNumber == null}")
                        Log.d(TAG, "Student $index registrationNumber is empty: ${student.registrationNumber?.isEmpty() ?: true}")
                        
                        // Log the raw student object to see all available fields
                        Log.d(TAG, "Student $index RAW OBJECT: $student")
                    }
                    Log.d(TAG, "=== END STUDENT DATA FROM API DEBUG ===")
                    
                    Paper.book().write("students", students)
                }
            }
            
            // Save session data - EXACTLY as in working code
            Paper.book().write(Constants.is_login, true)
            Paper.book().write(Constants.User_Type, userType)
            
            loginResponse.campusSession?.let { session ->
                Paper.book().write("current_session", session.uniqueId)
                Constant.current_session = session.uniqueId
            }
            
            Log.d(TAG, "User data saved to legacy storage. User Type: $userType")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user data to legacy storage", e)
        }
    }
    
    // ==================== USER DATA OPERATIONS ====================
    
    /**
     * Get current user data
     */
    suspend fun getCurrentUser(): Result<Any> = withContext(Dispatchers.IO) {
        try {
            if (useOptimizedStorage) {
                val user = databaseManager.getCurrentUser()
                if (user != null) {
                    Result.Success(user)
                } else {
                    Result.Error("No user data found")
                }
            } else {
                val parentId = Paper.book().read<String>("parent_id")
                val fullName = Paper.book().read<String>("full_name")
                val campusId = Paper.book().read<String>("campus_id")
                val email = Paper.book().read<String>("email")
                val phone = Paper.book().read<String>("phone")
                val landline = Paper.book().read<String>("landline")
                val address = Paper.book().read<String>("address")
                val picture = Paper.book().read<String>("picture")
                
                if (parentId != null && fullName != null) {
                    val userData = LoginData(
                        uniqueId = parentId,
                        parentId = campusId ?: "",
                        fullName = fullName,
                        email = email ?: "",
                        phone = phone ?: "",
                        landline = landline ?: "",
                        address = address ?: "",
                        picture = picture ?: ""
                    )
                    Result.Success(userData)
                } else {
                    Result.Error("No user data found")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user", e)
            Result.Error("Error retrieving user data: ${e.message}")
        }
    }
    
    /**
     * Get students data
     */
    suspend fun getStudents(): Result<Any> = withContext(Dispatchers.IO) {
        try {
            if (useOptimizedStorage) {
                val students = databaseManager.getStudents()
                if (students.isNotEmpty()) {
                    Result.Success(students)
                } else {
                    Result.Error("No students data found")
                }
            } else {
                val students = Paper.book().read<List<SharedStudent>>("students")
                if (students != null) {
                    Result.Success(students)
                } else {
                    Result.Error("No students data found")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting students", e)
            Result.Error("Error retrieving students data: ${e.message}")
        }
    }
    
    /**
     * Get student subjects
     */
    suspend fun getStudentSubjects(studentId: String): Result<Any> = withContext(Dispatchers.IO) {
        try {
            if (useOptimizedStorage) {
                val studentWithSubjects = databaseManager.getStudentWithSubjects(studentId)
                if (studentWithSubjects != null) {
                    Result.Success(studentWithSubjects)
                } else {
                    Result.Error("No student found with ID: $studentId")
                }
            } else {
                val subjects = Paper.book().read<List<Subject>>(studentId)
                if (subjects != null) {
                    Result.Success(subjects)
                } else {
                    Result.Error("No subjects found for student: $studentId")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting student subjects", e)
            Result.Error("Error retrieving student subjects: ${e.message}")
        }
    }
    
    // ==================== SESSION OPERATIONS ====================
    
    /**
     * Check if user is logged in
     */
    suspend fun isUserLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (useOptimizedStorage) {
                databaseManager.isUserLoggedIn()
            } else {
                Paper.book().read<Boolean>(Constants.is_login) ?: false
            }
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
            if (useOptimizedStorage) {
                // For optimized storage, we need to get from database
                // This is a simplified version - in practice, you might want to cache this
                null // TODO: Implement for optimized storage
            } else {
                Paper.book().read<String>(Constants.User_Type)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user type", e)
            null
        }
    }
    
    /**
     * Get current session
     */
    suspend fun getCurrentSession(): Result<Any> = withContext(Dispatchers.IO) {
        try {
            if (useOptimizedStorage) {
                val session = databaseManager.getCurrentSession()
                if (session != null) {
                    Result.Success(session)
                } else {
                    Result.Error("No session data found")
                }
            } else {
                val session = Paper.book().read<String>("current_session")
                if (session != null) {
                    Result.Success(session)
                } else {
                    Result.Error("No session data found")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current session", e)
            Result.Error("Error retrieving session data: ${e.message}")
        }
    }
    
    // ==================== DATA MANAGEMENT OPERATIONS ====================
    
    /**
     * Clear user data (logout)
     */
    suspend fun clearUserData() = withContext(Dispatchers.IO) {
        try {
            if (useOptimizedStorage) {
                databaseManager.clearUserData()
                Log.d(TAG, "User data cleared from optimized database")
            } else {
                Paper.book().delete("parent_id")
                Paper.book().delete("full_name")
                Paper.book().delete("campus_id")
                Paper.book().delete("email")
                Paper.book().delete("phone")
                Paper.book().delete("landline")
                Paper.book().delete("address")
                Paper.book().delete("picture")
                Paper.book().delete("password")
                Paper.book().delete("students")
                Paper.book().delete(Constants.is_login)
                Paper.book().delete(Constants.User_Type)
                Paper.book().delete("current_session")
                
                // Clear student subjects
                getStudents().let { result ->
                    if (result is Result.Success) {
                        @Suppress("UNCHECKED_CAST")
                        val students = result.data as? List<SharedStudent>
                        students?.forEach { student ->
                            Paper.book().delete(student.uniqueId)
                        }
                    }
                }
                Log.d(TAG, "User data cleared from legacy storage")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing user data", e)
        }
    }
    
    /**
     * Update user profile data
     */
    suspend fun updateProfile(profileData: Map<String, String>): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (useOptimizedStorage) {
                // Get current user
                val currentUser = databaseManager.getCurrentUser()
                if (currentUser != null) {
                    // Update user data with new profile information
                    databaseManager.saveUserData(
                        userId = currentUser.userId,
                        userType = currentUser.userType,
                        campusId = currentUser.campusId,
                        fullName = profileData["fullName"] ?: currentUser.fullName,
                        email = profileData["email"] ?: currentUser.email,
                        phone = profileData["phone"] ?: currentUser.phone,
                        landline = profileData["landline"] ?: currentUser.landline,
                        address = profileData["address"] ?: currentUser.address,
                        picture = profileData["picture"] ?: currentUser.picture,
                        password = currentUser.password
                    )
                    Log.d(TAG, "Profile updated successfully in optimized storage")
                    Result.Success(true)
                } else {
                    Result.Error("No current user found")
                }
            } else {
                // Update local storage
                profileData.forEach { (key, value) ->
                    Paper.book().write(key, value)
                }
                Log.d(TAG, "Profile updated successfully in legacy storage")
                Result.Success(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile", e)
            Result.Error("Error updating profile: ${e.message}")
        }
    }
    
    // ==================== MIGRATION OPERATIONS ====================
    
    /**
     * Migrate data from Paper DB to optimized database
     */
    suspend fun migrateFromPaperDB(): OptimizedDatabaseManager.MigrationResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting migration from Paper DB to optimized database...")
            val result = databaseManager.migrateFromPaperDB()
            Log.d(TAG, "Migration completed: ${result.migratedItems.size} items migrated")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error during migration", e)
            OptimizedDatabaseManager.MigrationResult().apply {
                success = false
                errorMessage = e.message
            }
        }
    }
    
    /**
     * Set storage mode preference
     */
    fun setStorageMode(useOptimized: Boolean) {
        useOptimizedStorage = useOptimized
        Log.d(TAG, "Storage mode set to: ${if (useOptimized) "Optimized" else "Legacy"}")
    }
    
    /**
     * Get current storage mode
     */
    fun getStorageMode(): String {
        return if (useOptimizedStorage) "Optimized" else "Legacy"
    }
    
    // ==================== UTILITY OPERATIONS ====================
    
    /**
     * Refresh user data from server
     */
    suspend fun refreshUserData(): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            // This would typically make an API call to refresh user data
            // For now, we'll return an error indicating this needs to be implemented
            Result.Error("Refresh user data not implemented yet")
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
            databaseManager.close()
            Log.d(TAG, "Consolidated user repository closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing consolidated user repository", e)
        }
    }
}

/**
 * Data class for user profile data (Legacy compatibility)
 */
data class LoginData(
    val uniqueId: String,
    val parentId: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val landline: String,
    val address: String,
    val picture: String
)
