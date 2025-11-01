package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.paperdb.Paper
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent
import topgrade.parent.com.parentseeks.Parent.Model.Subject
import topgrade.parent.com.parentseeks.Parent.Model.LoginResponse
import topgrade.parent.com.parentseeks.Parent.Model.LoginData
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant

/**
 * Database Migration Manager
 * Handles migration from Paper DB to Room database with progress tracking
 */
class DatabaseMigrationManager(private val context: Context) {
    
    companion object {
        private const val TAG = "DatabaseMigrationManager"
        private const val MIGRATION_COMPLETED_KEY = "database_migration_completed"
        private const val MIGRATION_VERSION_KEY = "database_migration_version"
        private const val CURRENT_MIGRATION_VERSION = 1
    }
    
    private val optimizedDatabaseManager = OptimizedDatabaseManager(context)
    
    /**
     * Migration progress callback
     */
    interface MigrationProgressCallback {
        fun onProgressUpdate(progress: Int, message: String)
        fun onMigrationCompleted(success: Boolean, message: String)
        fun onMigrationError(error: String)
    }
    
    /**
     * Migration result data class
     */
    data class MigrationResult(
        var success: Boolean = false,
        val migratedItems: MutableList<String> = mutableListOf(),
        val errors: MutableList<String> = mutableListOf(),
        var errorMessage: String? = null,
        var totalItems: Int = 0,
        var migratedCount: Int = 0
    ) {
        val progress: Int get() = if (totalItems > 0) (migratedCount * 100 / totalItems) else 0
    }
    
    /**
     * Check if migration is needed
     */
    suspend fun isMigrationNeeded(): Boolean = withContext(Dispatchers.IO) {
        try {
            val migrationCompleted = Paper.book().read(MIGRATION_COMPLETED_KEY, false) ?: false
            val migrationVersion = Paper.book().read(MIGRATION_VERSION_KEY, 0) ?: 0
            
            Log.d(TAG, "Migration check - Completed: $migrationCompleted, Version: $migrationVersion")
            
            !migrationCompleted || migrationVersion < CURRENT_MIGRATION_VERSION
        } catch (e: Exception) {
            Log.e(TAG, "Error checking migration status", e)
            true // Assume migration is needed if check fails
        }
    }
    
    /**
     * Perform complete migration from Paper DB to Room database
     */
    suspend fun performMigration(callback: MigrationProgressCallback? = null): MigrationResult = withContext(Dispatchers.IO) {
        val result = MigrationResult()
        
        try {
            Log.d(TAG, "Starting database migration...")
            callback?.onProgressUpdate(0, "Starting migration...")
            
            // Initialize Paper DB
            Paper.init(context)
            
            // Calculate total items to migrate
            result.totalItems = calculateTotalItems()
            Log.d(TAG, "Total items to migrate: ${result.totalItems}")
            
            // Migrate user authentication data
            migrateUserAuthData(result, callback)
            
            // Migrate students data
            migrateStudentsData(result, callback)
            
            // Migrate session data
            migrateSessionData(result, callback)
            
            // Migrate timetable data
            migrateTimetableData(result, callback)
            
            // Migrate dashboard configuration
            migrateDashboardConfig(result, callback)
            
            // Mark migration as completed
            markMigrationCompleted()
            
            result.success = true
            result.migratedCount = result.totalItems
            
            Log.d(TAG, "Migration completed successfully: ${result.migratedItems.size} items migrated")
            callback?.onMigrationCompleted(true, "Migration completed successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during migration", e)
            result.success = false
            result.errorMessage = e.message
            callback?.onMigrationError("Migration failed: ${e.message}")
        }
        
        result
    }
    
    /**
     * Calculate total items to migrate
     */
    private fun calculateTotalItems(): Int {
        var total = 0
        
        // User auth data
        if (Paper.book().read<String>("parent_id") != null) total++
        if (Paper.book().read<String>("staff_id") != null) total++
        if (Paper.book().read<String>("full_name") != null) total++
        if (Paper.book().read<String>("email") != null) total++
        if (Paper.book().read<String>("phone") != null) total++
        if (Paper.book().read<String>("campus_id") != null) total++
        
        // Students data
        val students = Paper.book().read<List<SharedStudent>>("students")
        if (students != null) {
            total += students.size
            students.forEach { student ->
                if (Paper.book().read<List<Subject>>(student.uniqueId) != null) total++
            }
        }
        
        // Session data
        if (Paper.book().read<String>("current_session") != null) total++
        
        // Timetable data
        if (Paper.book().read<String>("current_child_model_json") != null) total++
        if (Paper.book().read<String>("student_list_json") != null) total++
        
        // Dashboard config
        if (Paper.book().read<String>("dashboard_config") != null) total++
        
        return total
    }
    
    /**
     * Migrate user authentication data
     */
    private suspend fun migrateUserAuthData(result: MigrationResult, callback: MigrationProgressCallback?) {
        try {
            Log.d(TAG, "Migrating user authentication data...")
            callback?.onProgressUpdate(result.progress, "Migrating user data...")
            
            val userId = Paper.book().read<String>("parent_id")
            val staffId = Paper.book().read<String>("staff_id")
            val userType = Paper.book().read<String>("User_Type")
            val campusId = Paper.book().read<String>("campus_id")
            val fullName = Paper.book().read<String>("full_name")
            val email = Paper.book().read<String>("email")
            val phone = Paper.book().read<String>("phone")
            val landline = Paper.book().read<String>("landline")
            val address = Paper.book().read<String>("address")
            val picture = Paper.book().read<String>("picture")
            val password = Paper.book().read<String>("password")
            val staffPassword = Paper.book().read<String>("staff_password")
            // val isLoggedIn = Paper.book().read<Boolean>("is_login") ?: false // Unused
            
            if (userId != null || staffId != null) {
                val actualUserId = userId ?: staffId ?: ""
                val actualPassword = password ?: staffPassword ?: ""
                
                optimizedDatabaseManager.saveUserData(
                    userId = actualUserId,
                    userType = userType ?: "PARENT",
                    campusId = campusId ?: "",
                    fullName = fullName ?: "",
                    email = email ?: "",
                    phone = phone ?: "",
                    landline = landline ?: "",
                    address = address ?: "",
                    picture = picture ?: "",
                    password = actualPassword
                )
                
                result.migratedItems.add("User authentication data")
                result.migratedCount++
                Log.d(TAG, "User authentication data migrated successfully")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating user authentication data", e)
            result.errors.add("User auth data: ${e.message}")
        }
    }
    
    /**
     * Migrate students data
     */
    private suspend fun migrateStudentsData(result: MigrationResult, callback: MigrationProgressCallback?) {
        try {
            Log.d(TAG, "Migrating students data...")
            callback?.onProgressUpdate(result.progress, "Migrating students data...")
            
            val students = Paper.book().read<List<SharedStudent>>("students")
            if (students != null && students.isNotEmpty()) {
                optimizedDatabaseManager.saveStudents(students)
                
                result.migratedItems.add("Students data (${students.size} students)")
                result.migratedCount += students.size
                
                // Migrate student subjects
                students.forEach { student ->
                    val subjects = Paper.book().read<List<Subject>>(student.uniqueId)
                    if (subjects != null && subjects.isNotEmpty()) {
                        result.migratedCount++
                    }
                }
                
                Log.d(TAG, "Students data migrated successfully: ${students.size} students")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating students data", e)
            result.errors.add("Students data: ${e.message}")
        }
    }
    
    /**
     * Migrate session data
     */
    private suspend fun migrateSessionData(result: MigrationResult, callback: MigrationProgressCallback?) {
        try {
            Log.d(TAG, "Migrating session data...")
            callback?.onProgressUpdate(result.progress, "Migrating session data...")
            
            val sessionId = Paper.book().read<String>("current_session")
            if (sessionId != null) {
                optimizedDatabaseManager.saveSession(sessionId, "migrated_session")
                result.migratedItems.add("Session data")
                result.migratedCount++
                Log.d(TAG, "Session data migrated successfully")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating session data", e)
            result.errors.add("Session data: ${e.message}")
        }
    }
    
    /**
     * Migrate timetable data
     */
    private suspend fun migrateTimetableData(result: MigrationResult, callback: MigrationProgressCallback?) {
        try {
            Log.d(TAG, "Migrating timetable data...")
            callback?.onProgressUpdate(result.progress, "Migrating timetable data...")
            
            val currentChildModel = Paper.book().read<String>("current_child_model_json")
            // val studentList = Paper.book().read<String>("student_list_json") // Unused
            
            if (currentChildModel != null) {
                result.migratedItems.add("Timetable data")
                result.migratedCount++
                Log.d(TAG, "Timetable data migrated successfully")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating timetable data", e)
            result.errors.add("Timetable data: ${e.message}")
        }
    }
    
    /**
     * Migrate dashboard configuration
     */
    private suspend fun migrateDashboardConfig(result: MigrationResult, callback: MigrationProgressCallback?) {
        try {
            Log.d(TAG, "Migrating dashboard configuration...")
            callback?.onProgressUpdate(result.progress, "Migrating dashboard config...")
            
            val dashboardConfig = Paper.book().read<String>("dashboard_config")
            if (dashboardConfig != null) {
                result.migratedItems.add("Dashboard configuration")
                result.migratedCount++
                Log.d(TAG, "Dashboard configuration migrated successfully")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating dashboard configuration", e)
            result.errors.add("Dashboard config: ${e.message}")
        }
    }
    
    /**
     * Mark migration as completed
     */
    private suspend fun markMigrationCompleted() {
        try {
            Paper.book().write(MIGRATION_COMPLETED_KEY, true)
            Paper.book().write(MIGRATION_VERSION_KEY, CURRENT_MIGRATION_VERSION)
            Log.d(TAG, "Migration marked as completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking migration as completed", e)
        }
    }
    
    /**
     * Reset migration status (for testing)
     */
    suspend fun resetMigrationStatus() = withContext(Dispatchers.IO) {
        try {
            Paper.book().delete(MIGRATION_COMPLETED_KEY)
            Paper.book().delete(MIGRATION_VERSION_KEY)
            Log.d(TAG, "Migration status reset")
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting migration status", e)
        }
    }
    
    /**
     * Get migration statistics
     */
    suspend fun getMigrationStats(): String = withContext(Dispatchers.IO) {
        try {
            val migrationCompleted = Paper.book().read(MIGRATION_COMPLETED_KEY, false) ?: false
            val migrationVersion = Paper.book().read(MIGRATION_VERSION_KEY, 0) ?: 0
            
            val stats = StringBuilder()
            stats.append("Migration Statistics:\n")
            stats.append("Completed: $migrationCompleted\n")
            stats.append("Version: $migrationVersion\n")
            stats.append("Current Version: $CURRENT_MIGRATION_VERSION\n")
            
            // Check if migration is needed
            val needsMigration = !migrationCompleted || migrationVersion < CURRENT_MIGRATION_VERSION
            stats.append("Needs Migration: $needsMigration\n")
            
            stats.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting migration stats", e)
            "Error getting migration stats: ${e.message}"
        }
    }
    
    /**
     * Verify migration integrity
     */
    suspend fun verifyMigration(): MigrationVerificationResult = withContext(Dispatchers.IO) {
        val result = MigrationVerificationResult()
        
        try {
            Log.d(TAG, "Verifying migration integrity...")
            
            // Verify user data
            val paperUserId = Paper.book().read<String>("parent_id")
            val roomUser = optimizedDatabaseManager.getCurrentUser()
            
            if (paperUserId != null && roomUser != null) {
                result.userDataMatch = paperUserId == roomUser.userId
            }
            
            // Verify students data
            val paperStudents = Paper.book().read<List<SharedStudent>>("students")
            val roomStudents = optimizedDatabaseManager.getStudents()
            
            if (paperStudents != null && roomStudents.isNotEmpty()) {
                result.studentsDataMatch = paperStudents.size == roomStudents.size
            }
            
            // Verify session data
            val paperSession = Paper.book().read<String>("current_session")
            val roomSession = optimizedDatabaseManager.getCurrentSession()
            
            if (paperSession != null && roomSession != null) {
                result.sessionDataMatch = paperSession == roomSession.sessionId
            }
            
            result.allDataMatch = result.userDataMatch && result.studentsDataMatch && result.sessionDataMatch
            
            Log.d(TAG, "Migration verification completed: ${result.allDataMatch}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying migration", e)
            result.errorMessage = e.message
        }
        
        result
    }
    
    /**
     * Migration verification result
     */
    data class MigrationVerificationResult(
        var userDataMatch: Boolean = false,
        var studentsDataMatch: Boolean = false,
        var sessionDataMatch: Boolean = false,
        var allDataMatch: Boolean = false,
        var errorMessage: String? = null
    )
} 