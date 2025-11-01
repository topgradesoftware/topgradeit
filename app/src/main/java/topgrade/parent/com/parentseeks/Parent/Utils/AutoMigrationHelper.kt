package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Auto Migration Helper
 * Provides Java-compatible methods for automatic database migration
 */
object AutoMigrationHelper {
    
    private const val TAG = "AutoMigrationHelper"
    
    /**
     * Start automatic migration (Java-compatible) - Lightweight version
     */
    @JvmStatic
    fun startAutoMigration(context: Context) {
        Log.d(TAG, "Starting lightweight automatic migration from Java...")
        
        // Use a lightweight coroutine scope for better performance
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if migration is already completed first (fast check)
                val migrationCompleted = io.paperdb.Paper.book().read("auto_migration_completed", false) ?: false
                val migrationVersion = io.paperdb.Paper.book().read("auto_migration_version", 0) ?: 0
                
                if (migrationCompleted && migrationVersion >= 1) {
                    Log.d(TAG, "Migration already completed, skipping...")
                    return@launch
                }
                
                // Only perform full migration if needed
                val autoMigrationManager = AutoMigrationManager(context)
                val result = autoMigrationManager.checkAndPerformAutoMigration()
                
                when (result.status) {
                    AutoMigrationManager.AutoMigrationStatus.COMPLETED -> {
                        Log.d(TAG, "Auto migration completed successfully")
                        if (result.verificationPassed) {
                            Log.d(TAG, "Migration verification passed")
                        } else {
                            Log.w(TAG, "Migration verification failed")
                        }
                    }
                    AutoMigrationManager.AutoMigrationStatus.ALREADY_COMPLETED -> {
                        Log.d(TAG, "Migration already completed")
                    }
                    AutoMigrationManager.AutoMigrationStatus.NOT_NEEDED -> {
                        Log.d(TAG, "No migration needed")
                    }
                    AutoMigrationManager.AutoMigrationStatus.FAILED -> {
                        Log.e(TAG, "Migration failed: ${result.message}")
                    }
                    else -> {
                        Log.d(TAG, "Migration status: ${result.status}")
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during auto migration", e)
            }
        }
    }
    
    /**
     * Quick migration check (Java-compatible) - Non-blocking
     */
    @JvmStatic
    fun quickMigrationCheck(_context: Context): Boolean {
        return try {
            val migrationCompleted = io.paperdb.Paper.book().read("auto_migration_completed", false) ?: false
            val migrationVersion = io.paperdb.Paper.book().read("auto_migration_version", 0) ?: 0
            migrationCompleted && migrationVersion >= 1
        } catch (e: Exception) {
            Log.e(TAG, "Error in quick migration check", e)
            false
        }
    }
    
    /**
     * Check migration status (Java-compatible)
     */
    @JvmStatic
    fun checkMigrationStatus(context: Context, callback: MigrationStatusCallback?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val autoMigrationManager = AutoMigrationManager(context)
                val stats = autoMigrationManager.getMigrationStats()
                
                Log.d(TAG, "Migration stats: $stats")
                
                // Call callback on main thread if provided
                callback?.onStatusReceived(stats)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error checking migration status", e)
                callback?.onError(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Reset migration status (Java-compatible)
     */
    @JvmStatic
    fun resetMigrationStatus(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val autoMigrationManager = AutoMigrationManager(context)
                autoMigrationManager.resetMigrationStatus()
                Log.d(TAG, "Migration status reset")
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting migration status", e)
            }
        }
    }
    
    /**
     * Migration status callback interface for Java
     */
    interface MigrationStatusCallback {
        fun onStatusReceived(status: String)
        fun onError(error: String)
    }
} 