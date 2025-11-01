package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import topgrade.parent.com.parentseeks.Parent.Model.DashboardConfig
import topgrade.parent.com.parentseeks.Parent.Model.UserPreferences

/**
 * Migration helper to transition from Paper DB to DataStore
 * Handles the migration of dashboard configuration and user preferences
 */
class DashboardMigrationHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "DashboardMigration"
        private const val PREF_DASHBOARD_CONFIG = "dashboard_config"
        private const val PREF_USER_PREFERENCES = "user_preferences"
        private const val PREF_USER_ROLE = "user_role"
        private const val PREF_USER_PERMISSIONS = "user_permissions"
        private const val PREF_MIGRATION_COMPLETED = "dashboard_migration_completed"
    }
    
    private val gson = Gson()
    private val dashboardDataStore = DashboardDataStore(context)
    
    /**
     * Check if migration has been completed
     */
    suspend fun isMigrationCompleted(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val completed = Paper.book().read(PREF_MIGRATION_COMPLETED, false) ?: false
                Log.d(TAG, "Migration completed: $completed")
                completed
            } catch (e: Exception) {
                Log.e(TAG, "Error checking migration status", e)
                false
            }
        }
    }
    
    /**
     * Perform migration from Paper DB to DataStore
     */
    suspend fun migrateToDataStore() {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting dashboard migration to DataStore")
                
                // Migrate dashboard configuration
                migrateDashboardConfig()
                
                // Migrate user preferences
                migrateUserPreferences()
                
                // Migrate user role and permissions
                migrateUserRoleAndPermissions()
                
                // Mark migration as completed
                Paper.book().write(PREF_MIGRATION_COMPLETED, true)
                
                Log.d(TAG, "Dashboard migration completed successfully")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during dashboard migration", e)
                throw e
            }
        }
    }
    
    /**
     * Migrate dashboard configuration from Paper DB to DataStore
     */
    private suspend fun migrateDashboardConfig() {
        try {
            val configJson = Paper.book().read(PREF_DASHBOARD_CONFIG, "") ?: ""
            if (configJson.isNotEmpty()) {
                val config = gson.fromJson(configJson, DashboardConfig::class.java)
                dashboardDataStore.saveDashboardConfig(config)
                Log.d(TAG, "Dashboard config migrated successfully")
            } else {
                Log.d(TAG, "No dashboard config found in Paper DB")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating dashboard config", e)
        }
    }
    
    /**
     * Migrate user preferences from Paper DB to DataStore
     */
    private suspend fun migrateUserPreferences() {
        try {
            val prefsJson = Paper.book().read(PREF_USER_PREFERENCES, "") ?: ""
            if (prefsJson.isNotEmpty()) {
                val userPrefs = gson.fromJson(prefsJson, UserPreferences::class.java)
                dashboardDataStore.saveUserPreferences(userPrefs)
                Log.d(TAG, "User preferences migrated successfully")
            } else {
                Log.d(TAG, "No user preferences found in Paper DB")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating user preferences", e)
        }
    }
    
    /**
     * Migrate user role and permissions from Paper DB to DataStore
     */
    private suspend fun migrateUserRoleAndPermissions() {
        try {
            // Migrate user role
            val userRole = Paper.book().read(PREF_USER_ROLE, "teacher") ?: "teacher"
            dashboardDataStore.saveUserRole(userRole)
            
            // Migrate user permissions
            val permissionsJson = Paper.book().read(PREF_USER_PERMISSIONS, "[]") ?: "[]"
            val permissions = gson.fromJson(permissionsJson, Array<String>::class.java).toList()
            dashboardDataStore.saveUserPermissions(permissions)
            
            Log.d(TAG, "User role and permissions migrated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating user role and permissions", e)
        }
    }
    
    /**
     * Rollback migration (for testing purposes)
     */
    suspend fun rollbackMigration() {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Rolling back dashboard migration")
                Paper.book().write(PREF_MIGRATION_COMPLETED, false)
                Log.d(TAG, "Migration rollback completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error rolling back migration", e)
            }
        }
    }
    
    /**
     * Get migration status information
     */
    suspend fun getMigrationStatus(): MigrationStatus {
        return withContext(Dispatchers.IO) {
            try {
                val completed = Paper.book().read(PREF_MIGRATION_COMPLETED, false) ?: false
                val hasConfig = Paper.book().read(PREF_DASHBOARD_CONFIG, "")?.isNotEmpty() ?: false
                val hasPreferences = Paper.book().read(PREF_USER_PREFERENCES, "")?.isNotEmpty() ?: false
                
                MigrationStatus(
                    isCompleted = completed,
                    hasPaperData = hasConfig || hasPreferences,
                    hasConfig = hasConfig,
                    hasPreferences = hasPreferences
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error getting migration status", e)
                MigrationStatus(false, false, false, false)
            }
        }
    }
    
    /**
     * Data class for migration status
     */
    data class MigrationStatus(
        val isCompleted: Boolean,
        val hasPaperData: Boolean,
        val hasConfig: Boolean,
        val hasPreferences: Boolean
    )
} 