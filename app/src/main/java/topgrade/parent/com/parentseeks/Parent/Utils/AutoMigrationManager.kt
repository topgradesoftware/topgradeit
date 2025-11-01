package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.lifecycleScope
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Automatic Database Migration Manager
 * Handles migration automatically without user intervention
 */
class AutoMigrationManager(private val context: Context) {
    
    companion object {
        private const val TAG = "AutoMigrationManager"
        private const val MIGRATION_COMPLETED_KEY = "auto_migration_completed"
        private const val MIGRATION_VERSION_KEY = "auto_migration_version"
        private const val CURRENT_MIGRATION_VERSION = 1
    }
    
    private val migrationManager = DatabaseMigrationManager(context)
    private val optimizedDatabaseManager = OptimizedDatabaseManager(context)
    
    /**
     * Check if automatic migration is needed and perform it
     */
    suspend fun checkAndPerformAutoMigration(): AutoMigrationResult = withContext(Dispatchers.IO) {
        val result = AutoMigrationResult()
        
        try {
            Log.d(TAG, "Starting automatic migration check...")
            
            // Check if migration is already completed
            val migrationCompleted = Paper.book().read(MIGRATION_COMPLETED_KEY, false) ?: false
            val migrationVersion = Paper.book().read(MIGRATION_VERSION_KEY, 0) ?: 0
            
            Log.d(TAG, "Migration status - Completed: $migrationCompleted, Version: $migrationVersion")
            
            if (migrationCompleted && migrationVersion >= CURRENT_MIGRATION_VERSION) {
                Log.d(TAG, "Migration already completed, skipping...")
                result.status = AutoMigrationStatus.ALREADY_COMPLETED
                result.message = "Migration already completed"
                return@withContext result
            }
            
            // Check if migration is needed
            val needsMigration = migrationManager.isMigrationNeeded()
            
            if (!needsMigration) {
                Log.d(TAG, "No migration needed")
                result.status = AutoMigrationStatus.NOT_NEEDED
                result.message = "No migration needed"
                return@withContext result
            }
            
            // Perform automatic migration
            Log.d(TAG, "Starting automatic migration...")
            result.status = AutoMigrationStatus.IN_PROGRESS
            result.message = "Starting automatic migration..."
            
            val migrationResult = migrationManager.performMigration(object : DatabaseMigrationManager.MigrationProgressCallback {
                override fun onProgressUpdate(progress: Int, message: String) {
                    Log.d(TAG, "Migration progress: $progress% - $message")
                    result.progress = progress
                    result.message = message
                }
                
                override fun onMigrationCompleted(success: Boolean, message: String) {
                    if (success) {
                        Log.d(TAG, "Migration completed successfully")
                        result.status = AutoMigrationStatus.COMPLETED
                        result.message = "Migration completed successfully"
                        
                        // Mark migration as completed
                        Paper.book().write(MIGRATION_COMPLETED_KEY, true)
                        Paper.book().write(MIGRATION_VERSION_KEY, CURRENT_MIGRATION_VERSION)
                        
                    } else {
                        Log.e(TAG, "Migration failed: $message")
                        result.status = AutoMigrationStatus.FAILED
                        result.message = "Migration failed: $message"
                    }
                }
                
                override fun onMigrationError(error: String) {
                    Log.e(TAG, "Migration error: $error")
                    result.status = AutoMigrationStatus.FAILED
                    result.message = "Migration error: $error"
                }
            })
            
            // Verify migration if successful
            if (migrationResult.success) {
                Log.d(TAG, "Verifying migration...")
                val verificationResult = migrationManager.verifyMigration()
                
                if (verificationResult.allDataMatch) {
                    Log.d(TAG, "Migration verification passed")
                    result.verificationPassed = true
                } else {
                    Log.w(TAG, "Migration verification failed")
                    result.verificationPassed = false
                    result.verificationDetails = verificationResult
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during automatic migration", e)
            result.status = AutoMigrationStatus.FAILED
            result.message = "Error during migration: ${e.message}"
        }
        
        result
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
            stats.append("Auto Migration Statistics:\n")
            stats.append("Completed: $migrationCompleted\n")
            stats.append("Version: $migrationVersion\n")
            stats.append("Current Version: $CURRENT_MIGRATION_VERSION\n")
            
            val needsMigration = !migrationCompleted || migrationVersion < CURRENT_MIGRATION_VERSION
            stats.append("Needs Migration: $needsMigration\n")
            
            stats.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting migration stats", e)
            "Error getting migration stats: ${e.message}"
        }
    }
    
    /**
     * Auto migration result
     */
    data class AutoMigrationResult(
        var status: AutoMigrationStatus = AutoMigrationStatus.NOT_STARTED,
        var message: String = "",
        var progress: Int = 0,
        var verificationPassed: Boolean = false,
        var verificationDetails: DatabaseMigrationManager.MigrationVerificationResult? = null
    )
    
    /**
     * Auto migration status
     */
    enum class AutoMigrationStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        ALREADY_COMPLETED,
        NOT_NEEDED
    }
} 