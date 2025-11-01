package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.util.Log
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import topgrade.parent.com.parentseeks.Parent.Model.ChildModel
import topgrade.parent.com.parentseeks.Parent.Model.timetable.StudentTimetableResponse
import topgrade.parent.com.parentseeks.Teacher.Model.TimetableModel

/**
 * Migration Helper for Timetable Data
 * Safely migrates data from Paper DB to TimetableDataStore
 */
class TimetableMigrationHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "TimetableMigrationHelper"
        private const val MIGRATION_COMPLETED_KEY = "timetable_migration_completed"
    }
    
    private val timetableDataStore = TimetableDataStore(context)
    
    /**
     * Check if migration has been completed
     */
    suspend fun isMigrationCompleted(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Paper.book().read(MIGRATION_COMPLETED_KEY, false) ?: false
            } catch (e: Exception) {
                Log.e(TAG, "Error checking migration status", e)
                false
            }
        }
    }
    
    /**
     * Mark migration as completed
     */
    private suspend fun markMigrationCompleted() {
        withContext(Dispatchers.IO) {
            try {
                Paper.book().write(MIGRATION_COMPLETED_KEY, true)
                Log.d(TAG, "Migration marked as completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error marking migration as completed", e)
            }
        }
    }
    
    /**
     * Reset migration status (for testing)
     */
    suspend fun resetMigrationStatus() {
        withContext(Dispatchers.IO) {
            try {
                Paper.book().delete(MIGRATION_COMPLETED_KEY)
                Log.d(TAG, "Migration status reset")
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting migration status", e)
            }
        }
    }
    
    /**
     * Perform complete migration from Paper DB to DataStore
     */
    suspend fun migrateTimetableData(): MigrationResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting timetable data migration...")
                
                val result = MigrationResult()
                
                // Migrate student data
                migrateStudentData(result)
                
                // Migrate staff data
                migrateStaffData(result)
                
                // Migrate general data
                migrateGeneralData(result)
                
                // Mark migration as completed
                markMigrationCompleted()
                
                Log.d(TAG, "Timetable migration completed successfully")
                result.success = true
                result
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during timetable migration", e)
                MigrationResult().apply {
                    success = false
                    errorMessage = e.message ?: "Unknown error during migration"
                }
            }
        }
    }
    
    /**
     * Migrate student-related data
     */
    private suspend fun migrateStudentData(result: MigrationResult) {
        try {
            // Migrate current child model
            val currentChild = Paper.book().read<ChildModel>("current_child_model")
            if (currentChild != null) {
                timetableDataStore.saveCurrentChildModel(currentChild)
                result.migratedItems.add("current_child_model")
                Log.d(TAG, "Migrated current_child_model")
            }
            
            // Migrate student list
            val studentList = Paper.book().read<List<ChildModel>>("students")
            if (studentList != null && studentList.isNotEmpty()) {
                timetableDataStore.saveStudentList(studentList)
                result.migratedItems.add("students")
                Log.d(TAG, "Migrated ${studentList.size} students")
            }
            
            // Migrate parent ID
            val parentId = Paper.book().read<String>("parent_id") ?: ""
            if (!parentId.isNullOrEmpty()) {
                timetableDataStore.saveParentId(parentId)
                result.migratedItems.add("parent_id")
                Log.d(TAG, "Migrated parent_id: $parentId")
            }
            
            // Migrate campus ID
            val campusId = Paper.book().read<String>("campus_id") ?: ""
            if (!campusId.isNullOrEmpty()) {
                timetableDataStore.saveCampusId(campusId)
                result.migratedItems.add("campus_id")
                Log.d(TAG, "Migrated campus_id: $campusId")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating student data", e)
            result.errors.add("Student data migration failed: ${e.message}")
        }
    }
    
    /**
     * Migrate staff-related data
     */
    private suspend fun migrateStaffData(result: MigrationResult) {
        try {
            // Migrate staff ID
            val staffId = Paper.book().read<String>("staff_id") ?: ""
            if (!staffId.isNullOrEmpty()) {
                timetableDataStore.saveStaffId(staffId)
                result.migratedItems.add("staff_id")
                Log.d(TAG, "Migrated staff_id: $staffId")
            }
            
            // Note: Staff timetable cache data is typically not stored in Paper DB
            // as it's fetched fresh each time, so we don't migrate it
            
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating staff data", e)
            result.errors.add("Staff data migration failed: ${e.message}")
        }
    }
    
    /**
     * Migrate general timetable data
     */
    private suspend fun migrateGeneralData(result: MigrationResult) {
        try {
            // Save last sync time
            timetableDataStore.saveLastSyncTime(System.currentTimeMillis())
            result.migratedItems.add("last_sync_time")
            Log.d(TAG, "Set last sync time")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating general data", e)
            result.errors.add("General data migration failed: ${e.message}")
        }
    }
    
    /**
     * Compare data between Paper DB and DataStore
     */
    suspend fun compareData(): ComparisonResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting data comparison...")
                
                val result = ComparisonResult()
                
                // Compare current child model
                val paperChild = Paper.book().read<ChildModel>("current_child_model")
                val dataStoreChild = try {
                    timetableDataStore.currentChildModel.first()
                } catch (e: Exception) {
                    null
                }
                
                if (paperChild != null && dataStoreChild != null) {
                    val paperStudentId = paperChild.students?.firstOrNull()?.uniqueId
                    val dataStoreStudentId = dataStoreChild.students?.firstOrNull()?.uniqueId
                    result.matches["current_child_model"] = paperStudentId == dataStoreStudentId
                } else {
                    result.matches["current_child_model"] = paperChild == dataStoreChild
                }
                
                // Compare student list
                val paperStudents = Paper.book().read<List<ChildModel>>("students")
                val dataStoreStudents = try {
                    timetableDataStore.studentList.first()
                } catch (e: Exception) {
                    emptyList()
                }
                
                result.matches["students"] = paperStudents?.size == dataStoreStudents.size
                
                // Compare parent ID
                val paperParentId = Paper.book().read<String>("parent_id") ?: ""
                val dataStoreParentId = try {
                    timetableDataStore.parentId.first()
                } catch (e: Exception) {
                    ""
                }
                
                result.matches["parent_id"] = paperParentId == dataStoreParentId
                
                // Compare campus ID
                val paperCampusId = Paper.book().read<String>("campus_id") ?: ""
                val dataStoreCampusId = try {
                    timetableDataStore.campusId.first()
                } catch (e: Exception) {
                    ""
                }
                
                result.matches["campus_id"] = paperCampusId == dataStoreCampusId
                
                // Compare staff ID
                val paperStaffId = Paper.book().read<String>("staff_id") ?: ""
                val dataStoreStaffId = try {
                    timetableDataStore.staffId.first()
                } catch (e: Exception) {
                    ""
                }
                
                result.matches["staff_id"] = paperStaffId == dataStoreStaffId
                
                Log.d(TAG, "Data comparison completed")
                result
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during data comparison", e)
                ComparisonResult().apply {
                    errorMessage = e.message ?: "Unknown error during comparison"
                }
            }
        }
    }
    
    /**
     * Clear migrated data from Paper DB (use with caution)
     */
    suspend fun clearMigratedPaperData() {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Clearing migrated data from Paper DB...")
                
                // Only clear if migration was successful
                if (isMigrationCompleted()) {
                    Paper.book().delete("current_child_model")
                    Paper.book().delete("students")
                    Paper.book().delete("parent_id")
                    Paper.book().delete("campus_id")
                    Paper.book().delete("staff_id")
                    
                    Log.d(TAG, "Migrated data cleared from Paper DB")
                } else {
                    Log.w(TAG, "Migration not completed, skipping Paper DB cleanup")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing migrated Paper DB data", e)
            }
        }
    }
    
    /**
     * Migration result data class
     */
    data class MigrationResult(
        var success: Boolean = false,
        val migratedItems: MutableList<String> = mutableListOf(),
        val errors: MutableList<String> = mutableListOf(),
        var errorMessage: String? = null
    ) {
        val totalMigrated: Int get() = migratedItems.size
        val totalErrors: Int get() = errors.size
    }
    
    /**
     * Comparison result data class
     */
    data class ComparisonResult(
        val matches: MutableMap<String, Boolean> = mutableMapOf(),
        var errorMessage: String? = null
    ) {
        val allMatch: Boolean get() = matches.values.all { it }
        val totalItems: Int get() = matches.size
        val matchingItems: Int get() = matches.values.count { it }
    }
} 